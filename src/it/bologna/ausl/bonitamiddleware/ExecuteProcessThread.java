/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.bologna.ausl.bonitamiddleware;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.security.auth.login.LoginException;
import org.ow2.bonita.facade.def.majorElement.impl.ProcessDefinitionImpl;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.IllegalTaskStateException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.TaskNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import sun.misc.BASE64Encoder;

/**
 *
 * @author GiuseppeNew
 */


    public class ExecuteProcessThread extends Thread{
        String id;
        String processID = null;
        boolean rand;

        ProcessesHandler ph;
        public ExecuteProcessThread(boolean rand, String id, String processID, ProcessesHandler ph) throws LoginException {
            this.id = id;
            this.processID = processID;
            this.rand = rand;
            this.ph = ph;
//            ph = new ProcessesHandler();
//            ph.login();
//        Class<? extends ExecuteProcessThread> aClass = getClass();
//            this.managementAPI = AccessorUtil.getManagementAPI();
//            this.runtimeAPI = AccessorUtil.getRuntimeAPI();
//            this.queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
//            this.identityAPI = AccessorUtil.getIdentityAPI();
        }


        @Override
        public void run() {
                System.out.println("inizio flusso processo: " + id + " ...");
            try {
                Calendar cal = Calendar.getInstance();
                long start = System.currentTimeMillis();
                try {
                    executeAProcess(rand, processID);
                }
                catch (Exception ex) {
                    ex.printStackTrace(System.out);
                }
                long end = System.currentTimeMillis();

                System.out.println("flusso processo: " + id + " terminato, tempo: " + (end - start) / 1000f + " sec.");
            }
            catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
        }

        private void executeAProcess(boolean rand, String processID) throws LoginException, ProcessNotFoundException, TaskNotFoundException, IllegalTaskStateException, ActivityNotFoundException, VariableNotFoundException, InstanceNotFoundException, IOException, ClassNotFoundException {
        String[] randStato = new String[]{"firma", "funzionario", "redattore"};
        //        System.setProperty(BonitaConstants.LOGGED_USER, "admin");
        //        System.setProperty(BonitaConstants.HOME, "C:/BOS-5.4.1-Tomcat-6.0.29");
        //        System.setProperty(BonitaConstants.LOGIN_MODE_PROPERTY, "REST");
        // login
        //        SimpleCallbackHandler simpleCallbackHandler = new SimpleCallbackHandler("admin", "");
        //
        //        final LoginContext loginContext = new LoginContext("BonitaStore",simpleCallbackHandler);
        //
        //        loginContext.login();
        //        Set<ProcessDefinition> processes = AccessorUtil.getQueryDefinitionAPI().getProcesses();
        //
        //        System.exit(0);
//        ProcessesHandler ph = new ProcessesHandler();
//        ph.login();
        Map<String, Object> initVariables= new HashMap<String, Object>();
        initVariables.put("listaUtenti", new ArrayList<String>());
         initVariables.put("svUrl", "ng");
         initVariables.put("svUsername", "s");
         initVariables.put("svPassword", "gsg");
        ProcessInstanceUUID instanceID = ph.startNewProcessInstanceWithVariable(processID, initVariables);
        Set<LightActivityInstance> listActivityInstances = ph.listActivityInstances(instanceID, ActivityState.READY);
        System.out.println(Arrays.toString(listActivityInstances.toArray()));
        LightActivityInstance[] acts= new LightActivityInstance[1];
        listActivityInstances.toArray(acts);
        LightActivityInstance act = acts[0];
        Map<String, Object> variables1 = ph.getVariables(act.getUUID());
        if (true) return;
        while(!ph.isTerminated(new ProcessInstanceUUID(instanceID))) {
            Set<LightActivityInstance> tasks = ph.listActivityInstances(instanceID, ActivityState.READY);

            for (LightActivityInstance taskInstance : tasks) {
//                System.out.println("processo " + id + ", task: " + taskInstance.getUUID() + "...");
//                runtimeAPI.startTask(taskInstance.getUUID(), true);
                // setto casualmente le variabili
                Map<String, Object> variables = ph.getVariables(taskInstance.getUUID());
                Set<String> keySet = variables.keySet();
                Iterator<String> iterator = keySet.iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    Object var = variables.get(key);
                    if (var instanceof Boolean) {
                        boolean nextBoolean = true;
                        if (rand) {
                            nextBoolean = new Random().nextBoolean();
                        }
                        variables.put(key, nextBoolean);
//                        ph.setVariable(taskInstance.getUUID(), key, nextBoolean);
                    }
                    else if (var instanceof String) {
                        String state = "firma";
                        if (rand) {
                            state = randStato[new Random().nextInt(2)];
                        }
                        variables.put(key, state);
//                        ph.setVariable(taskInstance.getUUID(), key, state);
                    }
                }
                ph.setVariables(taskInstance.getUUID(), variables);
                ph.stepOn(taskInstance.getUUID());
            }
        }
    }


    private static String callRestAPI(String targetUrl, String postParameter) throws ProtocolException, IOException {
            System.out.println("connessione...");

            URL url = new URL(targetUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            BASE64Encoder enc = new BASE64Encoder();
            String userpassword = "restuser" + ":" + "restbpm";
            String encodedAuthorization = enc.encode(userpassword.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(postParameter.getBytes().length));
            connection.setUseCaches (false);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
            wr.writeBytes(postParameter);
            wr.flush();
            wr.close();
            InputStream resultStream = connection.getInputStream();

            System.out.println("risposta: " + connection.getResponseCode() + " - " + connection.getResponseMessage());

            String resultString = inputStreamToString(resultStream);
            resultStream.close();
            connection.disconnect();
            return resultString;
    }

    private static String inputStreamToString(InputStream is) throws UnsupportedEncodingException, IOException {
        Writer stringWriter = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                stringWriter.write(buffer, 0, n);
            }
        }
        finally {
        }
        return stringWriter.toString();
    }

    private InputStream stringToInputStream(String str) {
        try {
            InputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"));
            return is;
        }
        catch (Exception ex) {
            ex.printStackTrace(System.out);
            return null;
        }
    }

    }

