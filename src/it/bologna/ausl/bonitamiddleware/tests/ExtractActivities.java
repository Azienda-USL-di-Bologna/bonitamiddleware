/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.bonitamiddleware.tests;

import it.bologna.ausl.bonitamiddleware.ProcessesHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.security.auth.login.LoginException;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableProcessException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;

/**
 *
 * @author gdm
 */
public class ExtractActivities {
    public static void main(String[] args) throws LoginException, ProcessNotFoundException, UndeletableProcessException, UndeletableInstanceException, InstanceNotFoundException, ActivityNotFoundException {
        ProcessesHandler ph = new ProcessesHandler("http://babelservice1:9081/bonita-server-rest");
        ph.setBonitaJaasPropertyFile(ProcessesHandler.DEFAULT_BONITA_JAAS_PROPERTY_FILE);
        ph.login();
        ProcessInstanceUUID process = new ProcessInstanceUUID("Smistamento--0.1.4--288216");
        
        
        Set<ActivityInstance> activityInstances = ph.getQueryRuntimeAPI().getActivityInstances(process);
        ActivityInstance[] toArray = (ActivityInstance[]) activityInstances.toArray(new ActivityInstance[0]);
        for (ActivityInstance act : toArray) {
            System.out.println(act.toString());
        }
        
        ActivityInstanceUUID act = new ActivityInstanceUUID("Smistamento--0.1.4--288216--Destinatari--itcf4710af-ab9d-4000-8486-52ca5ec99ec9--mainActivityInstance--lp9cf00738-3a3a-4374-8911-f50924c60154");
        ActivityInstance activityInstance = ph.getQueryRuntimeAPI().getActivityInstance(act);
        Date lastUpdateDate = activityInstance.getLastUpdateDate();
        
        if (true)
            return;
        
//        ArrayList<String> res = new ArrayList<String>();
//            Iterator<LightActivityInstance> iterator = activityInstances.iterator();
//            int i = 0;
//            Map.Entry<String, Date>[] activityDetailsAndDate = new Map.Entry[activityInstances.size()];
//            while (iterator.hasNext()) {
//                LightActivityInstance activityInstance = iterator.next();
//                Map.Entry<String, Date> entry = new HashMap.SimpleEntry<String, Date>(activityInstance.getActivityName() + " - " + activityInstance.getState() + " - " + activityInstance.getUUID(), activityInstance.getEndedDate());
//                activityDetailsAndDate[i] = entry;
//                i++;
//            }
//        
//    //        for (int j = 0; j< activityDetailsAndDate.length; j++) {
//    //            System.out.println(activityDetailsAndDate[j]);
//    //        }
//    //        System.exit(0);
//    //        System.out.println(Arrays.toString(activityDetailsAndDate));
//            Comparator myComparator = new Comparator() {
//                @Override
//                public int compare(Object o1, Object o2) {
//                Map.Entry<String, Date> elem1 = (Map.Entry<String, Date>)o1;
//                Map.Entry<String, Date> elem2 = (Map.Entry<String, Date>)o2;
//                if(elem1.getValue() == null) 
//                    return -1;
//                else if(elem2.getValue() == null) 
//                    return 1;
//                else if (elem1.getValue().before(elem2.getValue()))
//                    return -1;
//                else if (elem1.getValue().equals(elem2.getValue()))
//                    return 0;
//                else
//                    return 1;
//                }
//            };
//            Arrays.sort(activityDetailsAndDate, myComparator);
//            for (i=0; i<activityDetailsAndDate.length; i++) {
//                res.add(activityDetailsAndDate[i].getValue() + ": " + activityDetailsAndDate[i].getKey());
//            }
//
//            for (String details:res) {
//                System.out.println(details);
//            }
    }
}
