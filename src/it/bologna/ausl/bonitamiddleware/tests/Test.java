package it.bologna.ausl.bonitamiddleware.tests;

import it.bologna.ausl.bonitamiddleware.ProcessesHandler;
import it.bologna.ausl.bonitamiddleware.utils.UtilityFunctions;
import it.bologna.ausl.proctonutils.SVAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.exception.IllegalTaskStateException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.TaskNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;

/**
 *
 * @author Giuseppe De Maroco (gdm)
 */
public class Test {
    public static void main(String args[]) throws Exception {
        ProcessesHandler ph = new ProcessesHandler("http://gdml:9080/bonita-server-rest");
//        ProcessesHandler ph = new ProcessesHandler("http://babelservice1:9081/bonita-server-rest");
//        ProcessesHandler ph = new ProcessesHandler("http://babel-prototipo.internal.ausl.bologna.it:8083/bonita-server-rest");
        ph.setBonitaJaasPropertyFile(ProcessesHandler.DEFAULT_BONITA_JAAS_PROPERTY_FILE);
        
//        test3(ph);
 //       test2(ph);

        ph.login();
//        ph.getManagementAPI().deleteProcess(new ProcessDefinitionUUID("Protocollo_in_entrata--0.2.6.1"));
//        System.exit(0);
        Set<ActivityInstance> activityInstances = new HashSet<ActivityInstance>();
//        Set<LightProcessInstance> processInstances = ph.getQueryRuntimeAPI().getLightProcessInstances(new ProcessDefinitionUUID("Determine--0.1.2"));
//        Set<ProcessInstance> processInstances = ph.getQueryRuntimeAPI().getProcessInstances(new ProcessDefinitionUUID("GenerazioneNumero--1.2.1"));
        Set<ProcessInstance> processInstances = ph.getQueryRuntimeAPI().getProcessInstances();
        for(ProcessInstance l: processInstances) {
            System.out.println(l);
//            Set<LightActivityInstance> listActivityInstances = ph.listActivityInstances(l.getUUID(), ActivityState.EXECUTING);
//            Set<LightActivityInstance> listActivityInstances = ph.listActivityInstances(l.getUUID(), ActivityState.FAILED);
            Set<ActivityInstance> activities = l.getActivities();
            for (ActivityInstance a:activities) {
//                if (a.getState() == ActivityState.FAILED) {
                    activityInstances.add(a);
//                }
            }  
        }
//        System.exit(0);
        
        
        for(LightActivityInstance a : activityInstances) {
//                System.out.println(a.getUUID().getValue());
//                System.out.println(a.toString());
        }
//        
        System.exit(0);
        
        ArrayList<String> res = new ArrayList<String>();
            Iterator<ActivityInstance> iterator = activityInstances.iterator();
            int i = 0;
            Map.Entry<String, String>[] activityDetailsAndDate = new Map.Entry[activityInstances.size()];
            while (iterator.hasNext()) {
                ActivityInstance activityInstance = iterator.next();
                Map.Entry<String, String> entry = new HashMap.SimpleEntry<String, String>(activityInstance.getActivityName() + " - " + activityInstance.getState() + " - " + activityInstance.getUUID(), activityInstance.getRootInstanceUUID().getValue());
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
                Map.Entry<String, String> elem1 = (Map.Entry<String, String>)o1;
                Map.Entry<String, String> elem2 = (Map.Entry<String, String>)o2;
                if(elem1.getValue() == null) 
                    return -1;
                else if(elem2.getValue() == null) 
                    return 1;
                else if (elem1.getValue().compareTo(elem2.getValue()) < 0)
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
//        int j = 0;
        for (Map.Entry<String, String> e : activityDetailsAndDate) {
//            if (j == 0) {
                System.out.println("deleting: " + e.getValue());
//                ph.getRuntimeAPI().finishTask(new ActivityInstanceUUID(e.getValue()), true);
                try {
//                ph.getRuntimeAPI().skipTask(new ActivityInstanceUUID(e.getValue()), null);
                ph.getRuntimeAPI().deleteProcessInstance(new ProcessInstanceUUID(e.getValue()));
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
//            }
//            j++;
        }
        
        System.exit(0);

        
        ArrayList<String[]> transitionNamesStartWithPrefix = ph.getTransitionNamesStartWithPrefix("Generazione_Numero_Delibera", new ProcessDefinitionUUID("Delibere--0.1.0"), "f_");
        System.out.println(Arrays.deepToString(transitionNamesStartWithPrefix.toArray()));
        System.exit(0);
        
        ArrayList<String> processInstanceVariable = (ArrayList<String>)ph.getProcessInstanceVariable(new ProcessInstanceUUID("Verbali--0.0.1--7"), "svActionList");
        
        System.out.println(Arrays.toString(processInstanceVariable.toArray()));
        System.exit(0);
        
        Set<LightActivityInstance> listActivityInstances1 = ph.listActivityInstances(new ProcessInstanceUUID("Verbali--0.0.1--11"), ActivityState.READY);
        for (LightActivityInstance lai : listActivityInstances1) {
            System.out.println(lai.toString());
        }
        System.exit(0);
        
        Scanner input; //opens a scanner, keyboard
        String enteredString;
        
        ProcessInstanceUUID processInstanceUUID = kickOff(ph, "g.zoli");
        System.out.println("processInstanceUUID: " + processInstanceUUID);
        
        input = new Scanner(System.in);
        System.out.print("Enter gdm:");
        enteredString = input.nextLine();
        
        ActivityInstanceUUID activityInstanceUUID;
        if (enteredString.equalsIgnoreCase("gdm"))
            activityInstanceUUID = stepOn(ph, 
                    processInstanceUUID, 
                    UtilityFunctions.getActivityInstanceUUID(ph.listActivityInstances(processInstanceUUID, ActivityState.READY), 1), 
                    "g.demarco", 
                    "ok", 
                    "g.zoli", 
                    "Raccolta_Pareri");
        
        
        input = new Scanner(System.in);
        System.out.print("Enter gdm:");
        enteredString = input.nextLine();
        
        if (enteredString.equalsIgnoreCase("gdm"))
            activityInstanceUUID = stepOn(ph, 
                    processInstanceUUID, 
                    UtilityFunctions.getActivityInstanceUUID(ph.listActivityInstances(processInstanceUUID, ActivityState.READY), 1),
                    "pasquini2", 
                    "ok", 
                    "g.demarco", 
                    "Raccolta_Pareri");
        
        input = new Scanner(System.in);
        System.out.print("Enter gdm:");
        enteredString = input.nextLine();
        
        if (enteredString.equalsIgnoreCase("gdm"))
            activityInstanceUUID = stepOn(ph, 
                    processInstanceUUID, 
                    UtilityFunctions.getActivityInstanceUUID(ph.listActivityInstances(processInstanceUUID, ActivityState.READY), 1),
                    "c.fiesoli", 
                    "abort", 
                    "pasquini2", 
                    "Redazione");
        
    }
    
    private static ProcessInstanceUUID kickOff(ProcessesHandler ph, String user) throws ProcessNotFoundException, VariableNotFoundException {   
        Map<String, Object> variables = new HashMap<String, Object>();
        ArrayList<String> svActionList = new ArrayList<String>();
        SVAction sVAction = new SVAction(user, "Avvia", "http://gdml:9081/Procton/Procton.htm", "oggetto gdm 1");
        sVAction.setIdTipiAttivita("1");
        sVAction.setDescrizioneAttivita("Redazione");
        sVAction.setNoteAttivita("note");
        svActionList.add(sVAction.toString());
        variables.put("svActionList", svActionList);
        variables.put("bonitaAuthenticationUsername", "verba");
        variables.put("bonitaAuthenticationPassword", "verba");
        variables.put("actionType", "ok");
        variables.put("guidDocumento", "8AA70187-1E18-C7C1-EFB2-D15AFAED46F4");
        variables.put("tipoDocumento", "Verbali");
        variables.put("masterChefHost", "babelservice1");
        variables.put("masterChefPushingQueue", "chefingdml");
        variables.put("provenienza", "Giuseppe De Marco");
        variables.put("serverName", "gdml");
        variables.put("masterChefPushingQueue", "chefingdml");
        variables.put("setStatoServletUrl", "http://gdml:9081/bds_tools/SetCurrentActivity");
        variables.put("utenteInAzione", user);

        
        return ph.startNewProcessInstanceWithVariable("Verbali--0.0.1", variables);
    }
    
    private static ActivityInstanceUUID stepOn(ProcessesHandler ph, 
                                                ProcessInstanceUUID processInstanceUUID, 
                                                ActivityInstanceUUID activityInstanceUUID, 
                                                String newUser,
                                                String actionType,
                                                String utenteInAzione,
                                                String nextTask
                                                )
    throws ProcessNotFoundException, VariableNotFoundException, TaskNotFoundException, IllegalTaskStateException, InstanceNotFoundException {
                
        Map<String, Object> variables = new HashMap<String, Object>();
        
        ArrayList<String> svActionList = new ArrayList<String>();
        SVAction sVAction = new SVAction(newUser, "Avvia", "http://gdml:9081/Procton/Procton.htm", "oggetto gdm 1");
        sVAction.setIdTipiAttivita("1");
        sVAction.setDescrizioneAttivita(nextTask);
        sVAction.setNoteAttivita("note");
        svActionList.add(sVAction.toString());
        
        svActionList.add(sVAction.toString());
        ph.setProcessInstanceVariable(processInstanceUUID, "svActionList", svActionList);
        ph.setProcessInstanceVariable(processInstanceUUID, "actionType", actionType);
        ph.setProcessInstanceVariable(processInstanceUUID, "bonitaAuthenticationUsername", "verba");
        ph.setProcessInstanceVariable(processInstanceUUID, "bonitaAuthenticationPassword", "verba");
        if (!actionType.equalsIgnoreCase("ok"))
            ph.setProcessInstanceVariable(processInstanceUUID, "nextTask", nextTask);
        ph.setProcessInstanceVariable(processInstanceUUID, "utenteInAzione", utenteInAzione);
        ph.stepOn(activityInstanceUUID);
        
        return  UtilityFunctions.getActivityInstanceUUID(ph.listActivityInstances(processInstanceUUID, ActivityState.READY), 1);
    }
    
    public static void test1(ProcessesHandler ph) {
        try {
            ph.login();
            ActivityInstanceUUID a = new ActivityInstanceUUID("Determine--0.1.2--559--Redazione--it52f27138-7301-4a34-a1f3-04d748b5b87e--mainActivityInstance--noLoop");
            ProcessInstanceUUID p = new ProcessInstanceUUID("Determine--0.1.2--559");
            Object variable = ph.getVariable(a, "listaAzioni");
            System.out.println(variable.toString());
            ArrayList<String> al = (ArrayList<String>)variable;
            ArrayList al1 = (ArrayList)variable;
            
            System.out.println(Arrays.toString(al.toArray()));
            System.out.println(Arrays.toString(al1.toArray()));
        }
        
        catch (Exception ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
     public static void test2(ProcessesHandler ph) {
        try {
            ph.login();
            ActivityInstanceUUID a = new ActivityInstanceUUID("Determine--0.1.2--559--Redazione--it52f27138-7301-4a34-a1f3-04d748b5b87e--mainActivityInstance--noLoop");
            System.out.println(ph.getProcessInstanceUUID(a));
        }
        
        catch (Exception ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
    public static void test3(ProcessesHandler ph) throws LoginException, InstanceNotFoundException, UndeletableInstanceException {
        ph.login();
        Set<LightActivityInstance> listActivityInstances = ph.listActivityInstances(ActivityState.FAILED);
        int i = 1;
        for (LightActivityInstance l: listActivityInstances) {
            System.out.println(i++ + l.toString());
//            ph.deleteProcessInstance(l.getProcessInstanceUUID());
        }
    }
     
}
