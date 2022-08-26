package it.bologna.ausl.bonitamiddleware.tests;

import it.bologna.ausl.bonitamiddleware.ProcessesHandler;
import it.bologna.ausl.bonitamiddleware.utils.UtilityFunctions;
import it.bologna.ausl.proctonutils.SVAction;
import it.bologna.ausl.proctonutils.SVActionArrayUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import org.apache.http.conn.params.ConnConnectionPNames;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.element.RoleMapperDefinition;
import org.ow2.bonita.facade.def.element.impl.BusinessArchiveImpl;
import org.ow2.bonita.facade.def.element.impl.ConnectorDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.impl.ActivityDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.DataFieldDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.ParticipantDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.ProcessDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.TransitionDefinitionImpl;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.Comment;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.InstanceStateUpdate;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.VariableUpdate;
import org.ow2.bonita.facade.runtime.impl.ActivityInstanceImpl;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.impl.LightActivityInstanceImpl;
import org.ow2.bonita.parsing.def.XmlDefParser;
import org.ow2.bonita.parsing.def.binding.DataFieldBinding;
import org.ow2.bonita.util.AccessorUtil;


/**
 *
 * @author GiuseppeNew
 */
public class Main {
private static final int N_TIME = 1;
private final static String mutex = "";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        //        MessageSender.send("c3", "coda1");
        //        System.exit(0);
        //        ProcessesHandler ph = new ProcessesHandler("http://localhost:8080/bonita-server-rest");
        //        ph.login();
        //        Set<LightProcessDefinition> availableProcesses = ph.listAvailableProcesses();
        //        LightProcessDefinition[] toArray = availableProcesses.toArray(new LightProcessDefinition[0]);
        //
        //        for (int i=0; i<toArray.length; i++) {
        //            System.out.println(toArray[i].getUUID() + " - " + toArray[i].getState());
        //        }
        //
        //        Set<LightProcessInstance> availableInstance = ph.listProcessInstances(InstanceState.STARTED);
        //        LightProcessInstance[] toArray1 = availableInstance.toArray(new LightProcessInstance[0]);
        //
        //        ProcessInstanceUUID uUID = null;
        //        for (int i=0; i<toArray1.length; i++) {
        //            uUID = toArray1[i].getUUID();
        //            System.out.println(uUID + " - " + toArray1[i].getInstanceState());
        //        }
        //        List<LightTaskInstance> listTaskInstances = ph.listTaskInstances(uUID);
        //
        //        System.exit(0);
        //        System.out.println(args[0]);
        //        if (args.length == 0)
        //            args = new String[]{"0"};
        //        System.setProperty(BonitaConstants.JAAS_PROPERTY, Main.class.getResource("/configuration/jaas-tomcat.cfg").toString());
        //
        ////        System.setProperty(BonitaConstants.ENVIRONMENT_PROPERTY, "bonita-environment.xml");
        ////        System.setProperty(
        ////                BonitaConstants.ACTIVATE_REST_AUTHENTICATION_PROPERTY, "true");
        //        System.setProperty(BonitaConstants.REST_SERVER_ADDRESS_PROPERTY, "http://gdml:9080/bonita-server-rest");
        //        System.setProperty(BonitaConstants.API_TYPE_PROPERTY, "REST");
        //        System.setProperty(BonitaConstants.REST_SERVER_EXCEPTION, "");
        //System.setProperty("BONITA_HOME", "bonita");

 /*
                ProcessDefinitionImpl proc = new ProcessDefinitionImpl("miaprova", "1.0");
                ParticipantDefinitionImpl group = new ParticipantDefinitionImpl(proc.getUUID(), "Initiator");
        
                RoleMapperDefinition rmd = new ConnectorDefinitionImpl("org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver");
        
                group.setResolver(rmd);
                proc.addGroup(group);
        //        ActivityDefinitionImpl act1 = new ActivityDefinitionImpl(null);
                Set actorsSet = new HashSet<String>();
                actorsSet.add("Initiator");

                // task Start
                ActivityDefinitionImpl act0 = ActivityDefinitionImpl.createAutomaticActivity(proc.getUUID(), "Start");

                // task Human Normale
                ActivityDefinitionImpl act1 = ActivityDefinitionImpl.createHumanActivity(proc.getUUID(), "step1", actorsSet);

                // task Sub Process
                ActivityDefinitionImpl act2 = ActivityDefinitionImpl.createSubflowActivity(proc.getUUID(), "step2", "SubProcess1", null);

                // task Human Looped
                ActivityDefinitionImpl act3 = ActivityDefinitionImpl.createHumanActivity(proc.getUUID(), "step3", actorsSet);
                
                DataFieldDefinitionImpl var1 = new DataFieldDefinitionImpl(proc.getUUID(), act3.getUUID(), "loop", "java.​lang.String");
                var1.setLabel("loop");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(new Boolean(false));
                oos.close();

                Boolean prova = false;

//                var1.setInitialValue("aaa");
                act3.addData(var1);
                act3.setLoop("true", false, null);

                // task End
                ActivityDefinitionImpl actn = ActivityDefinitionImpl.createAutomaticActivity(proc.getUUID(), "End");



//                ConnectorDefinitionImpl conn1 = new ConnectorDefinitionImpl("org.bonitasoft.connectors.email.EmailConnector");
//                ArrayList<String> header = new ArrayList<String>();
//                ArrayList<ArrayList<String>> headers = new ArrayList<ArrayList<String>>();
//                header.add("header1");
//                header.add("val_header1");
//                headers.add(header);
//                header = new ArrayList<String>();
//                header.add("header2");
//                header.add("val_header2");
//                headers.add(header);
//                conn1.addParameter("setHeaders", new Object[]{headers});
//
//                conn1.addParameter("setMessage", new Object[]{"ciao"});
//                conn1.addParameter("setSmtpHost", new Object[]{"EXCH01.NSI2005.local"});
//
//                ArrayList<String> attachments = new ArrayList<String>();
//                attachments.add("c:/ciao.txt");
//                conn1.addParameter("setAttachments", new Object[]{attachments});
//
//                conn1.addParameter("setFrom", new Object[]{"s.dascia@nsi-mail.it"});
//                conn1.addParameter("setSslSupport", new Object[]{false});
//                conn1.addParameter("setTo", new Object[]{"s.dascia@nsi-mail.it"});
//                conn1.addParameter("setBcc", new Object[]{"g.demarco@ausl.bologna.it"});
//                conn1.addParameter("setCc", new Object[]{"g.demarco@nsi-mail.it"});
//                conn1.addParameter("setUserName", new Object[]{null});
//                conn1.addParameter("setCharset", new Object[]{"utf-8"});
//                conn1.addParameter("setStarttlsSupport", new Object[]{false});
//                conn1.addParameter("setSmtpPort", new Object[]{"25"});
//                conn1.addParameter("setHtml", new Object[]{false});
//                conn1.addParameter("setPassword", new Object[]{null});
//                conn1.addParameter("setSubject", new Object[]{"prova"});
//
//                conn1.setEvent(HookDefinition.Event.taskOnFinish);
//                act1.addConnector(conn1);
        
                TransitionDefinitionImpl trans01 = new TransitionDefinitionImpl(proc.getUUID(), "trans01", act0.getName(), act1.getName());
                TransitionDefinitionImpl trans12 = new TransitionDefinitionImpl(proc.getUUID(), "trans12", act1.getName(), act2.getName());
                TransitionDefinitionImpl trans23 = new TransitionDefinitionImpl(proc.getUUID(), "trans23", act2.getName(), act3.getName());
                TransitionDefinitionImpl trans3n = new TransitionDefinitionImpl(proc.getUUID(), "trans3n", act3.getName(), actn.getName());

                act0.addOutgoingTransition(trans01);

                act1.addIncomingTransition(trans01);
                act1.addOutgoingTransition(trans12);

                act2.addIncomingTransition(trans12);
                act2.addOutgoingTransition(trans23);

                act3.addIncomingTransition(trans23);
                act3.addOutgoingTransition(trans3n);

                actn.addIncomingTransition(trans3n);

                proc.addActivity(act0);
                proc.addActivity(act1);
                proc.addActivity(act2);
                proc.addActivity(act3);
                proc.addActivity(actn);
        

*/
        
//        ProcessesHandler.createConfiguration("http://gdml:9080/bonita-server-rest");

//        ProcessesHandler.createConfiguration(args[0]);
        
        
        ArrayList<String> listaAzioni = new ArrayList<String>();
        SVAction sva = new SVAction();
        sva.setDescrizioneAttivita("descr1");
        sva.setTaskName("Redazione");
        sva.setIdTipiAttivita("1");
//        sva.setActionType("ok");
        sva.setIdUtenti("g.demarco");
        sva.setLabelUrlCommand("prova label");
        sva.setUrlCommand("www.google.it");
        sva.setServizioAppartenenza("01-6-61-204-2009-09-01");
        listaAzioni.add(sva.toString());
        
        sva = new SVAction();
        sva.setDescrizioneAttivita("descr2");
        sva.setTaskName("Pareri");
        sva.setIdTipiAttivita("1");
//        sva.setActionType("ok");
//        sva.setActionType("abort");
        sva.setIdUtenti("g.anastasi");
        sva.setLabelUrlCommand("prova label");
        sva.setUrlCommand("www.google.it");
        sva.setServizioAppartenenza("01-6-61-204-2009-09-01");
        listaAzioni.add(sva.toString());
        
        sva = new SVAction();
        sva.setDescrizioneAttivita("descr3");
        sva.setTaskName("Pareri");
        sva.setIdTipiAttivita("1");
//        sva.setActionType("1");
        sva.setIdUtenti("c.fiesoli");
        sva.setLabelUrlCommand("prova label");
        sva.setUrlCommand("www.google.it");
        sva.setServizioAppartenenza("01-6-61-204-2009-09-01");
        listaAzioni.add(sva.toString());
        
//        ProcessesHandler processesHandler = new ProcessesHandler("http://gdml:9080/bonita-server-rest");
        ProcessesHandler processesHandler = new ProcessesHandler("http://babel960service-auslbo.avec.emr.it:8083/bonita-server-rest");
//        ProcessesHandler processesHandler = new ProcessesHandler("http://babelservice1:9081/bonita-server-rest");
        processesHandler.setBonitaJaasPropertyFile(ProcessesHandler.DEFAULT_BONITA_JAAS_PROPERTY_FILE);
        processesHandler.setBonitaHome(ProcessesHandler.DEFAULT_BONITA_HOME_DIR);
//        processesHandler.login();
//        processesHandler.deployProcess(new File("C:\\Progetti\\Bonita\\Processi\\Delibere--0.1.5.bar"));
        
//        System.exit(0);
        
        java.util.HashMap v_JHM = new java.util.HashMap();
        v_JHM.put("svUrl","http://gdml:9081/svmanager/update"); 
      v_JHM.put("svUsername","procton"); 
      v_JHM.put("svPassword","procton"); 
      v_JHM.put("masterChefHost","vm6-kvm-procton"); 
      v_JHM.put("masterChefPushingQueue","chefin"); 
      v_JHM.put("ultimoUtenteModificante",""); 
      v_JHM.put("guidDete","EC2049E4-A2D6-DD99-6A72-20A02E38B463"); 
      v_JHM.put("generateDeteNumberServletUrl","http://gdml:9081/dete_tools/SetDeteNumber"); 
      v_JHM.put("bonitaAuthenticationUsername","bonita"); 
      v_JHM.put("bonitaAuthenticationPassword","bonita"); 
      v_JHM.put("generateDeteNumberEmailsAddressToAlert","demarcog83@hotmail.com,g.demarco@nsi-mail.it"); 
      v_JHM.put("generateDeteNumberMaxRetries",10); 
      v_JHM.put("generateDeteNumberRetryTime",30000); 
      v_JHM.put("mailServerSmtpUrl","smtp"); 
      v_JHM.put("mailServerSmtpPort","25"); 
//      v_JHM.put("proctonPecManagerServletUrl","http://gdml:9081/procton_tools/ProctonPecManager"); 
      v_JHM.put("deteSetStatoDeterminaServletUrl","http://gdml:9081/dete_tools/SetStatoDetermina"); 
      v_JHM.put("anteprimaUUID","asfasf"); 
      v_JHM.put("listaAzioni",listaAzioni); 
      processesHandler.login();
      
      java.util.HashMap v_JHM_n = new java.util.HashMap();
      v_JHM_n.put("username","babel");
      v_JHM_n.put("password","babel");
      v_JHM_n.put("maxRetries",10);
      v_JHM_n.put("retryTime",30000);
      v_JHM_n.put("mailServerSmtpUrl","smtp");
      v_JHM_n.put("nomeSequenza","gddocs");
      v_JHM_n.put("servletUrl","http://gdml:9081/bds_tools/SetDocumentNumber");
      v_JHM_n.put("mailServerSmtpPort","25");
      v_JHM_n.put("emailsAddressToAlert","babel.alert@ausl.bologna.it");
      v_JHM_n.put("idDocumento","AE875602-A809-EFC2-26D7-FA6B434D9CC1");
//    ProcessInstanceUUID startNewProcessInstanceWithVariable = processesHandler.startNewProcessInstanceWithVariable("GenerazioneNumero--1.2", v_JHM_n);
//      System.exit(0);
      
      ActivityInstanceUUID act = new ActivityInstanceUUID("Protocollo_in_uscita--0.3.3--161--Raccolta_Pareri_Pre_Firma--itfc159c89-8ef0-4cbf-a139-ca805b70f759--mainActivityInstance--lpc49e7893-4999-48e2-b4f4-2b18829532f1");
      ActivityInstanceUUID act2 = new ActivityInstanceUUID("Protocollo_in_entrata--0.2.2--528--Ricezione--it1--mainActivityInstance--noLoop");
      ActivityInstanceUUID act3 = new ActivityInstanceUUID("Smistamento--0.1.4--577230--Smistamento--itd04c6bc4-c835-434c-8da5-d3bfe37d1d1d--mainActivityInstance--noLoop");
      ProcessInstanceUUID pin = new ProcessInstanceUUID("Protocollo_in_uscita--0.3.6--84689");
      ProcessInstanceUUID pin0 = new ProcessInstanceUUID("Protocollo_in_uscita--0.3.7--1143");
      ProcessInstanceUUID pin2 = new ProcessInstanceUUID("Smistamento--0.1.4--4292");
      ProcessInstanceUUID pin3 = new ProcessInstanceUUID("Determine--0.1.2--3197");
      ProcessInstanceUUID pin4 = new ProcessInstanceUUID("Protocollo_in_entrata--0.2.6--15495");
      ProcessInstanceUUID pin5 = new ProcessInstanceUUID("Delibere--0.1.3--11");

        ProcessDefinitionUUID processDefinitionUUID = processesHandler.getProcessDefinitionUUID(act3);
        System.out.println(processDefinitionUUID);
//        Object processInstanceVariable = processesHandler.getProcessInstanceVariable(pin, "listaOk");
//        System.out.println(processInstanceVariable);
//      processesHandler.getRuntimeAPI().resumeTask(new ActivityInstanceUUID("Protocollo_in_uscita--0.3.6--26422--Validazione_Segreteria--it5d253796-00a0-481b-8cb6-e61de1a5bde0--mainActivityInstance--noLoop"), true);
//      ArrayList<String[]> transitionNamesStartWithPrefix = processesHandler.getTransitionNamesStartWithPrefix("Direttore_Generale", new ProcessDefinitionUUID("Delibere--0.0.7"), "f_");
//      processesHandler.setProcessInstanceVariable(pin0, "ultimoUtenteModificante", "");
//      Object set = processesHandler.getProcessInstanceVariable(pin3, "idSmistamento");
//      Object set2 = processesHandler.getVariable(act3, "listaAzioni");
//      System.out.println(set.toString());
////      System.out.println(processesHandler.get);
//      System.out.println(set2.toString());
//      System.out.println(Arrays.deepToString(((ArrayList)transitionNamesStartWithPrefix).toArray()));
      System.exit(0);

/*
        Set<LightActivityInstance> activityInstances = processesHandler.listActivityInstances(ActivityState.FAILED);
        for (LightActivityInstance activityInstance: activityInstances) {
            if (activityInstance.getProcessDefinitionUUID().getProcessName().equals("Smistamento") || 
                activityInstance.getProcessDefinitionUUID().getProcessName().equals("Protocollo_in_entrata") ||
                    activityInstance.getProcessDefinitionUUID().getProcessName().equals("Protocollo_in_uscita")
                ) {
                String guid = null;
                try {
//                    if (activityInstance.getProcessDefinitionUUID().getProcessName().equals("Determine")) 
//                        guid = (String) processesHandler.getVariable(activityInstance.getUUID(), "guidDete");
//                    else 
                        guid = (String) processesHandler.getVariable(activityInstance.getUUID(), "idDocumento");
                }
                catch (Exception ex) {
//                    ex.printStackTrace();
                }
                
                System.out.println("'" + guid + "',");
            }
        }
 */       
        
        Set<LightActivityInstance> activityInstances = processesHandler.listActivityInstances(pin2);
        ArrayList<String> res = new ArrayList<String>();
            Iterator<LightActivityInstance> iterator = activityInstances.iterator();
            int i = 0;
            Map.Entry<String, Date>[] activityDetailsAndDate = new Map.Entry[activityInstances.size()];
            while (iterator.hasNext()) {
                LightActivityInstance activityInstance = iterator.next();
                Map.Entry<String, Date> entry = new HashMap.SimpleEntry<String, Date>(activityInstance.getActivityName() + " - " + activityInstance.getState() + " - " + activityInstance.getUUID(), activityInstance.getEndedDate());
                activityDetailsAndDate[i] = entry;
                i++;
            }
        
    //        for (int j = 0; j< activityDetailsAndDate.length; j++) {
    //            System.out.println(activityDetailsAndDate[j]);
    //        }
    //        System.exit(0);
    //        System.out.println(Arrays.toString(activityDetailsAndDate));
            Comparator myComparator = new Comparator() {
                public int compare(Object o1, Object o2) {
                Map.Entry<String, Date> elem1 = (Map.Entry<String, Date>)o1;
                Map.Entry<String, Date> elem2 = (Map.Entry<String, Date>)o2;
                if(elem1.getValue() == null) 
                    return -1;
                else if(elem2.getValue() == null) 
                    return 1;
                else if (elem1.getValue().before(elem2.getValue()))
                    return -1;
                else if (elem1.getValue().equals(elem2.getValue()))
                    return 0;
                else
                    return 1;
                }
            };
            Arrays.sort(activityDetailsAndDate, myComparator);
            for (i=0; i<activityDetailsAndDate.length; i++) {
                res.add(activityDetailsAndDate[i].getValue() + ": " + activityDetailsAndDate[i].getKey());
            }

            for (String details:res) {
                System.out.println(details);
            }
        
System.exit(0);

//        Object[] toArray1 = actinstance.toArray();
//      System.out.println(Arrays.toString(toArray1));
//      for (LightActivityInstance activityInstance:actinstance) {
//          System.out.println(activityInstance.getActivityName() + " - " + activityInstance.getState() + " - " + activityInstance.getUUID());
//      }
//        String processInstanceVariable = (String)processesHandler.getProcessInstanceVariable(pin0, "idDocumento");
//        System.out.println(processInstanceVariable);
//      processesHandler.getRuntimeAPI().deleteProcessInstance(new ProcessInstanceUUID("Protocollo_in_entrata--0.2.5"));
//      processesHandler.getManagementAPI().deleteProcess(new ProcessDefinitionUUID("Protocollo_in_entrata--0.2.6"));
//      processesHandler.getManagementAPI().deleteProcess(new ProcessDefinitionUUID("Determine--0.1.2"));
//      processesHandler.getManagementAPI().deleteProcess(new ProcessDefinitionUUID("Verbali--0.0.2"));
      processesHandler.getManagementAPI().deleteProcess(new ProcessDefinitionUUID("Delibere--0.1.3"));
      
//      processesHandler.startNewProcessInstanceWithVariable("Protocollo_in_entrata--0.2.5", v_JHM);
            System.exit(0);
        Set<LightProcessInstance> listProcessInstances = processesHandler.listProcessInstances(InstanceState.STARTED);
        int cont = 0;
        System.out.println("fine richiesta processi avviati: " + listProcessInstances.size());
        for (LightProcessInstance pi:listProcessInstances) {
//            InstanceState state = processesHandler.getProcessInstanceState(pi.getProcessInstanceUUID());
            try {
                cont++;
//                ProcessInstanceUUID processInstanceUUID = pi.getProcessInstanceUUID();
                Set<LightActivityInstance> listActivityInstances = processesHandler.listActivityInstances(ActivityState.FAILED);
                for (LightActivityInstance ai:listActivityInstances) {
                    System.out.println("actId: " + ai.getUUID().getValue() + " state: " + ai.getState());
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                cont--;
            }
//            System.out.println(++cont + "deleti" + pi.getUUID().toString());
        }
        System.out.println("fine: " + cont);
    }

   }
