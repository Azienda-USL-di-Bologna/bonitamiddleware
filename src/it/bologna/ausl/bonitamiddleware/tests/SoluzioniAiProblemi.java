/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.bonitamiddleware.tests;

import it.bologna.ausl.bonitamiddleware.ProcessesHandler;
import it.bologna.ausl.proctonutils.SVAction;
import java.util.ArrayList;
import java.util.Set;
import javax.security.auth.login.LoginException;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.IllegalTaskStateException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;

/**
 *
 * @author Salo
 */
public class SoluzioniAiProblemi {

    private static ProcessesHandler processesHandler;
    private static String parametroConnessione;

    public static void laSolitaVecchiaStoria() throws InstanceNotFoundException {
        Set<LightActivityInstance> lista = processesHandler.listActivityInstances(new ProcessInstanceUUID("Delibere--0.1.5--1749"));

        for (LightActivityInstance act : lista) {
            //System.out.println(act.toString());
            if (act.getState() == ActivityState.FAILED) {
                System.out.println("FAILED=\n" + act.toString());
                //processesHandler.getRuntimeAPI().skip(act.getUUID(), null);
            }
        }

        System.exit(0);
    }

    public static void main(String args[]) throws Exception {
        // parametri_pubblici => nome_parametro = 'bonitaUrl'
        parametroConnessione = "http://babel902service-auslbo.avec.emr.it:8083/bonita-server-rest";
        String processInstanceUUIDString = "Delibere--0.1.5--1749";

        faiLogin();

        Set<LightActivityInstance> listaccia = getListaIstanzeAttivitaSuProcesso(processInstanceUUIDString);

        ciclaSuListaAttivitaProcesso(listaccia);

        skippaProcessoFailed(listaccia);

    }

    public static Set<LightActivityInstance> getListaIstanzeAttivitaSuProcesso(String processInstanceUUIDString) throws InstanceNotFoundException {
        System.out.println("prendo la lista di istanze attivita...");
        Set<LightActivityInstance> lista = processesHandler.listActivityInstances(new ProcessInstanceUUID(processInstanceUUIDString));
        System.out.println("Size ?  " + lista.size());
        return lista;
    }

    public static void ciclaSuListaAttivitaProcesso(Set<LightActivityInstance> listActivityInstances) {
        System.out.println("Ciclo...");
        for (LightActivityInstance activityInstance : listActivityInstances) {
            System.out.println(activityInstance);
        }
    }

    public static void faiLogin() throws LoginException {
        System.out.println("Creo processesHandler...");
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
        processesHandler = new ProcessesHandler(parametroConnessione);
//        ProcessesHandler processesHandler = new ProcessesHandler("http://babelservice1:9081/bonita-server-rest");
        processesHandler.setBonitaJaasPropertyFile(ProcessesHandler.DEFAULT_BONITA_JAAS_PROPERTY_FILE);
        processesHandler.setBonitaHome(ProcessesHandler.DEFAULT_BONITA_HOME_DIR);
        processesHandler.login();
    }

    public static void skippaProcessoFailed(Set<LightActivityInstance> lista) throws ActivityNotFoundException, IllegalTaskStateException {
        for (LightActivityInstance act : lista) {
            //System.out.println(act.toString());
            if (act.getState() == ActivityState.FAILED) {
                System.out.println("FAILED=\n" + act.toString());
                // DECOMMENTARE SOLO SE SI E' DAVVERO SICURI!
                //skippaActGuid(act);
            }
        }
    }

    public static void skippaActGuid(LightActivityInstance act) throws ActivityNotFoundException, IllegalTaskStateException {
        System.out.println("SKIPPO " + act.toString());
        processesHandler.getRuntimeAPI().skip(act.getUUID(), null);
        System.out.println("SKIPPATO!!");
    }
}
