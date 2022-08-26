package it.bologna.ausl.bonitamiddleware.utils;

import java.util.Iterator;
import java.util.Set;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;

/**
 *
 * @author GiuseppeNew
 */
public class UtilityFunctions {

    /** Ritorna l'uuid dell'i-esima LightActivityInstance del Set di LightActivityInstance passato
     * 
     * @param lightActivityInstances Set di LightActivityInstance
     * @param index indice
     * @return l'uuid dell'i-esima LightActivityInstance del Set di LightActivityInstance passato
     */
    public static ActivityInstanceUUID getActivityInstanceUUID(Set<LightActivityInstance> lightActivityInstances, int index) {
        Iterator<LightActivityInstance> iterator = lightActivityInstances.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            LightActivityInstance lightActivityInstance = iterator.next();
            i++;
            if (i == index)
                return lightActivityInstance.getUUID();
        }
        return null;
    }

    /** Ritorna l'i-esima LightActivityInstance del Set di LightActivityInstance passato
     * 
     * @param lightActivityInstances Set di LightActivityInstance
     * @param index indice
     * @return l'i-esima LightActivityInstance del Set di LightActivityInstance passato
     */
    public static LightActivityInstance getActivityInstance(Set<LightActivityInstance> lightActivityInstances, int index) {
        Iterator<LightActivityInstance> iterator = lightActivityInstances.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            LightActivityInstance lightActivityInstance = iterator.next();
            i++;
            if (i == index)
                return lightActivityInstance;
        }
        return null;
    }
}