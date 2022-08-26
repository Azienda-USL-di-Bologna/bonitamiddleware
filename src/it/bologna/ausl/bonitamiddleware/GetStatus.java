package it.bologna.ausl.bonitamiddleware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import javax.security.auth.login.LoginException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;

/**
 *
 * @author Gdm
 */
public class GetStatus {
    public static void main(String[] args) throws LoginException, InstanceNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Inserisci il processo: ");
        String processo = reader.readLine();
        System.out.print("Inserisci la versione: ");
        String versione = reader.readLine();
        System.out.print("Inserisci l'istanza: ");
        String nIst = reader.readLine();
        
        ProcessesHandler processesHandler = new ProcessesHandler("http://vm6-kvm-procton:9081/bonita-server-rest");
        processesHandler.setBonitaJaasPropertyFile(ProcessesHandler.DEFAULT_BONITA_JAAS_PROPERTY_FILE);
        processesHandler.login();
        ProcessInstanceUUID processInstance = new ProcessInstanceUUID(processo + "--" + versione + "--" + nIst);
        Set<LightActivityInstance> activityInstances = processesHandler.listActivityInstances(processInstance);
      
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
            res.add(activityDetailsAndDate[i].getKey());
        }
        
        for (String details:res) {
            System.out.println(details);
        }
    }
}
