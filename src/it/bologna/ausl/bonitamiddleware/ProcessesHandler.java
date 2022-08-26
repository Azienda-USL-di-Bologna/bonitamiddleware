package it.bologna.ausl.bonitamiddleware;

import it.bologna.ausl.bonitamiddleware.exceptions.BonitaMiddlewareException;
import it.bologna.ausl.proctonutils.SVAction;
import it.bologna.ausl.proctonutils.SVActionArrayUtils;
import it.bologna.ausl.proctonutils.exceptions.SVActionException;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.ow2.bonita.facade.CommandAPI;
import org.ow2.bonita.facade.IdentityAPI;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RepairAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.def.majorElement.impl.ActivityDefinitionImpl;
import org.ow2.bonita.facade.exception.*;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.SimpleCallbackHandler;

/**
 *
 * @author Giuseppe De Marco (gdm)
 */
public class ProcessesHandler {
public static final String DEFAULT_BONITA_JAAS_PROPERTY_FILE = ProcessesHandler.class.getResource("/it/bologna/ausl/bonitamiddleware/resources/jaas-tomcat.cfg").toString().replace("%20", " ");
public static final String DEFAULT_BONITA_HOME_DIR = "lib/resources/bonita";
public final String BONITA_REQUIRED_CLASS = "lib/resources/bonita-5.4.1.jar";

// variabili per la gestione dei processi
private final ManagementAPI managementAPI;
private final QueryDefinitionAPI queryDefinitionAPI;
private final RuntimeAPI runtimeAPI;
private final QueryRuntimeAPI queryRuntimeAPI;
private final IdentityAPI identityAPI;
private final RepairAPI repairAPI;
private final CommandAPI commandAPI;

// variabili per l'autenticazione
private SimpleCallbackHandler simpleCallbackHandler;
private LoginContext loginContext;

private Map resources;

    /** Costruice l'oggetto ProcessesHandler
     * @param bonitaRestURL URL del server rest di bonita (es. "http://localhost:8080/bonita-server-rest")
     */
    public ProcessesHandler(String bonitaRestURL) {
        createRestConfiguration(bonitaRestURL);
        this.managementAPI = AccessorUtil.getManagementAPI();
        this.queryDefinitionAPI = AccessorUtil.getQueryDefinitionAPI();
        this.runtimeAPI = AccessorUtil.getRuntimeAPI();
        this.queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
        this.identityAPI = AccessorUtil.getIdentityAPI();
        this.repairAPI = AccessorUtil.getRepairAPI();
        this.commandAPI = AccessorUtil.getCommandAPI();
    }

    /** Crea la configurazione necessaria per il funzionamento
     *
     * @param bonitaRestURL URL del server rest di bonita (es. "http://localhost:8080/bonita-server-rest")
     */
    private void createRestConfiguration(String bonitaRestURL) {
        System.setProperty(BonitaConstants.REST_SERVER_ADDRESS_PROPERTY, bonitaRestURL);
        System.setProperty(BonitaConstants.API_TYPE_PROPERTY, "REST");
        System.setProperty(BonitaConstants.REST_SERVER_EXCEPTION, "");
        //System.setProperty("BONITA_HOME", BONITA_HOME_DIR);
    }

    /** imposta la variabile d'ambiente BonitaHome sulla libreria
     * 
     * @param bonitaHome
     */
    public void setBonitaHome(String bonitaHome) {
        System.setProperty(BonitaConstants.HOME, bonitaHome);
    }

    /** indica il file per l'autenticazione jaas
     * 
     * @param jaasProperty il percorso del file per l'autenticazione jaas
     */
    public void setBonitaJaasPropertyFile(String jaasProperty) {
        System.setProperty(BonitaConstants.JAAS_PROPERTY, jaasProperty);
    }

    /** ritorna il percorso del file jaas per l'autenticazione
     * 
     * @return il percorso del file jaas per l'autenticazione
     */
    public String getBonitaJaasPropertyFile() {
        return System.getProperty(BonitaConstants.JAAS_PROPERTY);
    }


    /** Esegue il login verso bonita come utente predefinito (admin). Dovrà invocarlo ogni thread
     *
     * @throws LoginException
     */
    public void login() throws LoginException {
        System.out.println("---------------------login-------------------");
        System.out.println(System.getProperty(BonitaConstants.JAAS_PROPERTY));
        System.out.println("callbackHandler prima : " + simpleCallbackHandler);
        System.out.println("loginContext prima: " + loginContext);
        if (simpleCallbackHandler == null)
            simpleCallbackHandler = new SimpleCallbackHandler("admin", "");
        if (loginContext == null)
            loginContext = new LoginContext("BonitaStore", simpleCallbackHandler);
        loginContext.login();  
        System.out.println("callbackHandler dopo : " + simpleCallbackHandler);
        System.out.println("loginContext dopo: " + loginContext);
        System.out.println("----------------------------------------------");
    }

    /** Esegue il login verso bonita come un utente specifico. Dovrà invocarlo ogni thread
     *
     * @param user l'utente con il quale autenticarsi
     * @throws LoginException
     */
    public void login(String user) throws LoginException {
        simpleCallbackHandler = new SimpleCallbackHandler(user, "");
        loginContext = new LoginContext("BonitaStore",simpleCallbackHandler);
        loginContext.login();
    }

    /** Esegue il logout verso Bonita. Da invocare solo una volta alla fine
     *
     * @throws LoginException
     */
    public void logout() throws LoginException {
        loginContext.logout();
    }

    /** aggiunge una risorsa da deployare
     *
     * @param jarFilePath del file "jar" da aggiungere
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void addResource (String jarFilePath) throws FileNotFoundException, IOException {
        if (resources == null) {
            resources = new HashMap();
        }
        File jarLib = new File(jarFilePath);
        DataInputStream datais = new DataInputStream(new FileInputStream(jarLib));
        byte[] bytes = new byte[(int)jarLib.length()];
        datais.readFully(bytes);
        resources.put(jarLib.getName(), bytes);
    }
    /** SPERIMENTALE. Deploya un processo iniettando le risorse contenute nella variabile resources
     *
     * @param processDefinition Il processo creato "a mano"
     * @return il processo deployato
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws DeploymentException
     */
    public ProcessDefinition deployProcessWithResources(ProcessDefinition processDefinition) throws IOException, ClassNotFoundException, DeploymentException {
        BusinessArchive ba = BusinessArchiveFactory.getBusinessArchive(processDefinition, resources);
        return managementAPI.deploy(ba);
    }

    /** Esegue il deploy di un file .bar esportato da bonita
     *
     * @param file il file da deployare
     * @return un oggetto ProcessDefinition identificante il processo definito nel file
     * @throws DeploymentException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public ProcessDefinition deployProcess(File file) throws DeploymentException, IOException, ClassNotFoundException {
        BusinessArchive ba = BusinessArchiveFactory.getBusinessArchive(file);
        return managementAPI.deploy(ba);
    }

    /** Ritorna l'elenco dei processi installati su Bonita
     *
     * @return l'elenco dei processi installati
     */
    public Set<LightProcessDefinition> listAvailableProcesses() {
        return queryDefinitionAPI.getLightProcesses();
    }

    /** Ritorna l'elenco dei processi installati su Bonita che sono in un particolare stato
     *
     * @param state lo stato dei processi che si vogliono ottenere
     * @return l'elenco dei processi installati nello stato specificato
     */
    public Set<LightProcessDefinition> listAvailableProcesses(ProcessState state) {
        Set<LightProcessDefinition> processDefinitions = queryDefinitionAPI.getLightProcesses();
        Iterator<LightProcessDefinition> iterator = processDefinitions.iterator();
        while(iterator.hasNext()) {
            LightProcessDefinition processDefinition = iterator.next();
            if (processDefinition.getState() != state) {
                iterator.remove();
            }
        }
        return processDefinitions;
    }

    /** Ritorna l'elenco delle attività non automatiche del processo passato
     *
     * @param processDefinitionUUID l'uuid del processo del quale si vuole sapere l'elenco delle attività non automatiche
     * @return l'elenco delle attività non automatiche del processo passato
     * @throws ProcessNotFoundException
     */
    public ArrayList<String> listAllActivityNames(ProcessDefinitionUUID processDefinitionUUID) throws ProcessNotFoundException {
    ArrayList<String> activityNames = new ArrayList<String>();
        Set<ActivityDefinition> activities = queryDefinitionAPI.getProcessActivities(processDefinitionUUID);
        Iterator<ActivityDefinition> iterator = activities.iterator();
        while (iterator.hasNext()) {
            ActivityDefinitionImpl activity = (ActivityDefinitionImpl)iterator.next();
            if (!activity.isAutomatic()) {
                String activityName = activity.getName();
                activityNames.add(activityName);
            }
        }
        return activityNames;
    }

     /** Ritorna l'elenco delle attività non automatiche del processo passato
     *
     * @param processDefinitionUUID stringa rappresentante l'uuid del processo del quale si vuole sapere l'elenco delle attività non automatiche
     * @return l'elenco delle attività non automatiche del processo passato
     * @throws ProcessNotFoundException
     */
    public ArrayList<String> listAllActivityNames(String processDefinitionUUID) throws ProcessNotFoundException {
        return listAllActivityNames(new ProcessDefinitionUUID(processDefinitionUUID));
    }
    
    
    /** Avvia una nuova instanza del processo passato
     *
     * @param processDefinitionUUID il processo da avviare
     * @return l'istanza creata
     * @throws ProcessNotFoundException
     */
    private ProcessInstanceUUID startNewProcessInstance(ProcessDefinitionUUID processDefinitionUUID) throws ProcessNotFoundException {
        return runtimeAPI.instantiateProcess(processDefinitionUUID);
    }

    /** Avvia una nuova instanza del processo passato
     *
     * @param processDefinitionUUID stringa rappresentante il processo da avviare
     * @return l'istanza creata
     * @throws ProcessNotFoundException
     */
    public ProcessInstanceUUID startNewProcessInstance(String processDefinitionUUID) throws ProcessNotFoundException {
        return startNewProcessInstance(new ProcessDefinitionUUID(processDefinitionUUID));
    }

    /** Avvia una nuova instanza del processo passato valorizzando le variabili globali
     *
     * @param processDefinitionUUID il processo da avviare
     * @param variables un elenco (nome, valore) di variabili da settare
     * @return l'istanza creata
     * @throws ProcessNotFoundException
     */
    private ProcessInstanceUUID startNewProcessInstanceWithVariable(ProcessDefinitionUUID processDefinitionUUID, Map<String,Object> variables) throws ProcessNotFoundException, VariableNotFoundException {
        return runtimeAPI.instantiateProcess(processDefinitionUUID, variables);
    }

    /** Avvia una nuova instanza del processo passato valorizzando le variabili globali
     *
     * @param processDefinitionUUID stringa rappresentante il processo da avviare
     * @param variables un elenco (nome, valore) di variabili da settare
     * @return l'istanza creata
     * @throws ProcessNotFoundException
     */
    public ProcessInstanceUUID startNewProcessInstanceWithVariable(String processDefinitionUUID, Map<String,Object> variables) throws ProcessNotFoundException, VariableNotFoundException {
        return startNewProcessInstanceWithVariable(new ProcessDefinitionUUID(processDefinitionUUID), variables);
    }
    
    /** Annulla un'istanza di processo
     * 
     * @param processInstanceUUID l'uuid dell'istanza del processo da annullare
     * @throws InstanceNotFoundException
     * @throws UncancellableInstanceException 
     */
    public void cancelProcessInstance(ProcessInstanceUUID processInstanceUUID) throws InstanceNotFoundException, UncancellableInstanceException {
        runtimeAPI.cancelProcessInstance(processInstanceUUID);
    }
    
    /** Annulla un'istanza di processo
     * 
     * @param processInstanceUUID l'uuid dell'istanza del processo da annullare
     * @throws InstanceNotFoundException
     * @throws UncancellableInstanceException 
     */
    public void cancelProcessInstance(String processInstanceUUID) throws InstanceNotFoundException, UncancellableInstanceException {
        cancelProcessInstance(new ProcessInstanceUUID(processInstanceUUID));
    }
    
     /** Elimina un'istanza di processo
     * 
     * @param processInstanceUUID l'uuid dell'istanza del processo da eliminare
     * @throws InstanceNotFoundException
     * @throws UndeletableInstanceException 
     */
    public void deleteProcessInstance(ProcessInstanceUUID processInstanceUUID) throws InstanceNotFoundException, UndeletableInstanceException {
        runtimeAPI.deleteProcessInstance(processInstanceUUID);
    }
    
    /** Elimina un'istanza di processo
     * 
     * @param processInstanceUUID l'uuid dell'istanza del processo da eliminare
     * @throws InstanceNotFoundException
     * @throws UndeletableInstanceException 
     */
    public void deleteProcessInstance(String processInstanceUUID) throws InstanceNotFoundException, UndeletableInstanceException {
        deleteProcessInstance(new ProcessInstanceUUID(processInstanceUUID));
    }

    /** Ritorna l'elenco di tutte le istanze indipendentemente dal loro stato (Started, Finished, ecc.)
     *
     * @return l'elenco di tutte le istanze
     */
    public Set<LightProcessInstance> listProcessInstances() {
        return queryRuntimeAPI.getLightProcessInstances();
    }

    /**  Ritorna l'elenco di tutte le istanze in un particolare stato
     *
     * @param state lo stato delle istanze che si vogliono ottenere
     * @return l'elenco di tutte le istanze nello stato specificato
     */
    public Set<LightProcessInstance> listProcessInstances(InstanceState state) {
        Set<LightProcessInstance> processInstances = queryRuntimeAPI.getLightProcessInstances();
        Iterator<LightProcessInstance> iterator = processInstances.iterator();
        while(iterator.hasNext()) {
            LightProcessInstance processInstance = iterator.next();
            if (processInstance.getInstanceState() != state) {
                iterator.remove();
            }
        }
        return processInstances;
    }

    /** Ritorna l'elenco di tutte le istanze relative al processo passato indipendentemente dal loro stato
     *
     * @param processUUID il processo del quale si vogliono otenere le istanze
     * @return l'elenco di tutte le istanze relative al processo passato
     */
    public Set<LightProcessInstance> listProcessInstances(ProcessDefinitionUUID processUUID) {
        return queryRuntimeAPI.getLightProcessInstances(processUUID);
    }

    /** Ritorna l'elenco di tutte le istanze relative al processo passato indipendentemente dal loro stato
     *
     * @param processUUID stringa identificante il processo del quale si vogliono otenere le istanze
     * @return l'elenco di tutte le istanze relative al processo passato
     */
    public Set<LightProcessInstance> listProcessInstances(String processUUID) {
        return listProcessInstances(new ProcessDefinitionUUID(processUUID));
    }

    /** Ritorna l'elenco di tutte le istanze relative al processo passato in un particolare stato
     *
     * @param processUUID il processo del quale si vogliono otenere le istanze
     * @param state lo stato delle istanze che si vogliono ottenere
     * @return l'elenco di tutte le istanze relative al processo passato nello stato specificato
     */
    public Set<LightProcessInstance> listProcessInstances(ProcessDefinitionUUID processUUID, InstanceState state) {
        Set<LightProcessInstance> processInstances = queryRuntimeAPI.getLightProcessInstances(processUUID);
        Iterator<LightProcessInstance> iterator = processInstances.iterator();
        while(iterator.hasNext()) {
            LightProcessInstance processInstance = iterator.next();
            if (processInstance.getInstanceState() != state) {
                iterator.remove();
            }
        }
        return processInstances;
    }

    /** Ritorna l'elenco di tutte le istanze relative al processo passato in un particolare stato
     *
     * @param processUUID stringa identificante il processo del quale si vogliono otenere le istanze
     * @param state lo stato delle istanze che si vogliono ottenere
     * @return l'elenco di tutte le istanze relative al processo passato nello stato specificato
     */
    public Set<LightProcessInstance> listProcessInstances(String processUUID, InstanceState state) {
        return listProcessInstances(new ProcessDefinitionUUID(processUUID), state);
    }

    /** Ritnorna l'elenco globale delle attività indipendentemente dal loro stato
     * 
     * @return l'elenco globale delle attività
     * @throws InstanceNotFoundException
     */
    public Set<LightActivityInstance> listActivityInstances() throws InstanceNotFoundException {
        Set<LightProcessInstance> processInstances = listProcessInstances(InstanceState.STARTED);
        Iterator<LightProcessInstance> iterator = processInstances.iterator();
        Set<LightActivityInstance> taskInstances = new HashSet<LightActivityInstance>();
        while (iterator.hasNext()) {
            LightProcessInstance processInstance = iterator.next();
            Set<LightActivityInstance> currentTaskInstances = listActivityInstances(processInstance.getUUID());
            taskInstances.addAll(currentTaskInstances);
        }
        return taskInstances;
    }

    /** Ritnorna l'elenco delle attivita di una determinata istanza di un processo indipendentemente dal loro stato
     *
     * @param processInstanceUUID l'istanza del processo del quale si vuole ottenere la lista della attività
     * @return l'elenco globale di tutte le attivita
     * @throws InstanceNotFoundException
     */
    public Set<LightActivityInstance> listActivityInstances(ProcessInstanceUUID processInstanceUUID) throws InstanceNotFoundException {
        return queryRuntimeAPI.getLightActivityInstances(processInstanceUUID);
//        Set<LightProcessInstance> processInstances = listProcessInstances(InstanceState.STARTED);
//        return queryRuntimeAPI.getLightTaskList(null, ActivityState.READY);
    }

    /** Ritnorna l'elenco delle attività di una determinata istanza di un processo in un determinato stato
     *
     * @param processInstanceUUID l'istanza del processo del quale si vuole ottenere la lista della attività
     * @param state lo stato della attività che si vogliono ottenere
     * @return l'elenco delle attività
     * @throws InstanceNotFoundException
     */
    public Set<LightActivityInstance> listActivityInstances(ProcessInstanceUUID processInstanceUUID, ActivityState state) throws InstanceNotFoundException {
        Set<LightActivityInstance> activityInstances = listActivityInstances(processInstanceUUID);
        Iterator<LightActivityInstance> iterator = activityInstances.iterator();
        while (iterator.hasNext()) {
            LightActivityInstance activityInstance = iterator.next();
            if (activityInstance.getState() != state)
                iterator.remove();
        }
        return activityInstances;
    }

    /** Ritnorna l'elenco globale delle attività in un determinato stato
     *
     * @param state lo stato della attività che si vogliono ottenere
     * @return l'elenco globale delle attività
     * @throws InstanceNotFoundException
     */
    public Set<LightActivityInstance> listActivityInstances(ActivityState state) throws InstanceNotFoundException {
        Set<LightActivityInstance> activityInstances = listActivityInstances();
        Iterator<LightActivityInstance> iterator = activityInstances.iterator();
        while (iterator.hasNext()) {
            LightActivityInstance activityInstance = iterator.next();
            if (activityInstance.getState() != state)
                iterator.remove();
        }
        return activityInstances;
    }

    /** Ritorna il valore di una variabile di una certa istanza di un'attività (comprese le globali)
     * 
     * @param activityInstanceUUID l'istanza di un'attività da cui si vuole estrarre il valore della variabile
     * @param variableName il nome della variabile da estrarre
     * @return il valore della variabile richiesta (il tipo è un Object generico perché non è possibile sapere a propri il suo tipo)
     * @throws ActivityNotFoundException
     * @throws VariableNotFoundException
     */
    public Object getVariable(ActivityInstanceUUID activityInstanceUUID, String variableName) throws ActivityNotFoundException, VariableNotFoundException {
        return queryRuntimeAPI.getVariable(activityInstanceUUID, variableName);
    }

    /** Ritorna il valore di una variabile globale dell'intanza di un processo
     *
     * @param processInstanceUUID l'istanza del processo dal quale si vuole estrarre il valore della variabile
     * @param variableName il nome della variabile da estrarre
     * @return il valore della variabile richiesta (il tipo è un Object generico perché non è possibile sapere a propri il suo tipo)
     * @throws InstanceNotFoundException
     * @throws VariableNotFoundException
     */
    public Object getProcessInstanceVariable(ProcessInstanceUUID processInstanceUUID, String variableName) throws InstanceNotFoundException, VariableNotFoundException {
        return queryRuntimeAPI.getProcessInstanceVariable(processInstanceUUID, variableName);
    }

    /** Ritorna il valore di una variabile di una certa istanza di un'attività (solo le variabili locali dell'instanza)
     *
     * @param activityInstanceUUID l'istanza di un'attività da cui si vuole estrarre il valore della variabile
     * @param variableName il nome della variabile da estrarre
     * @return il valore della variabile richiesta (il tipo è un Object generico perché non è possibile sapere a propri il suo tipo)
     * @throws ActivityNotFoundException
     * @throws VariableNotFoundException
     */
    public Object getActivityVariable(ActivityInstanceUUID activityInstanceUUID, String variableName) throws ActivityNotFoundException, VariableNotFoundException {
        return queryRuntimeAPI.getActivityInstanceVariable(activityInstanceUUID, variableName);
    }

    /** Ritorna l'elenco (nome, valore) delle variabili di un'istanza di un'attività (comprese anche le globali)
     *
     * @param activityInstanceUUID l'istanza di un'attività da cui si vogliono estrarre le variabili
     * @return l'elenco (nome, valore) delle variabili
     * @throws ActivityNotFoundException
     * @throws InstanceNotFoundException
     */
    public Map<String, Object> getVariables(ActivityInstanceUUID activityInstanceUUID) throws InstanceNotFoundException, ActivityNotFoundException {
        return queryRuntimeAPI.getVariables(activityInstanceUUID);
    }

    /** Ritorna l'elenco (nome, valore) delle variabili globali dell'istanza di un processo
     *
     * @param processInstanceUUID l'istanza del processo dal quale si vogliono estrarre le variabili
     * @return l'elenco (nome, valore) delle variabili
     * @throws InstanceNotFoundException
     */
    public Map<String, Object> getProcessInstanceVariables(ProcessInstanceUUID processInstanceUUID) throws InstanceNotFoundException {
        return queryRuntimeAPI.getProcessInstanceVariables(processInstanceUUID);
    }

    /** Ritorna l'elenco (nome, valore) delle variabili di un'istanza di un'attività (solo le variabili locali dell'instanza)
     *
     * @param activityInstanceUUID l'istanza di un'attività da cui si vogliono estrarre le variabili
     * @return l'elenco (nome, valore) delle variabili
     * @throws ActivityNotFoundException
     */
    public Map<String, Object> getActivityVariables(ActivityInstanceUUID activityInstanceUUID) throws ActivityNotFoundException {
        return queryRuntimeAPI.getActivityInstanceVariables(activityInstanceUUID);
    }

    /** Ritorna l'elenco delle variabili globali di processo
     *
     * @param processDefinitionUUID il processo di cui si vogliono conoscere le variabili
     * @return l'elenco (nome, valore) delle variabili
     * @throws ProcessNotFoundException
     */
    public Map<String, Object> getProcessVariables(String processDefinitionUUID) throws ProcessNotFoundException {
        Set<DataFieldDefinition> processDataFields = queryDefinitionAPI.getProcessDataFields(new ProcessDefinitionUUID(processDefinitionUUID));
        Iterator<DataFieldDefinition> iterator = processDataFields.iterator();
        Map<String, Object> variables = new HashMap<String, Object>();
        while (iterator.hasNext()) {
            DataFieldDefinition variable = iterator.next();
            variables.put(variable.getName(), variable.getInitialValue());
        }
        return variables;
    }

    /** Setta i valori passati alle variabili di una istanza di un'attività
     *
     * @param activityInstanceUUID l'istanza di un attività alla quale applicare le variabili
     * @param variables un elenco (nome, valore) di variabili da settare all'istanza dell'attività passata
     */
    public void setVariables(ActivityInstanceUUID activityInstanceUUID, Map<String, Object> variables) throws ActivityNotFoundException, VariableNotFoundException {
        Set<String> keySet = variables.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String variableName = iterator.next();
            Object variableValue = variables.get(variableName);
            setVariable(activityInstanceUUID, variableName, variableValue);
        }
    }

    /** Setta il valore di una variabile di una certa istanza di un'attività
     *
     * @param activityInstanceUUID l'istanza di un attività alla quale settare il valore della variabile
     * @param variableName il nome della variabile da settare
     * @param variableValue il valore da assegnare alla variabile
     */
    public void setVariable(ActivityInstanceUUID activityInstanceUUID, String variableName, Object variableValue) throws ActivityNotFoundException, VariableNotFoundException {
        if (variableValue == null)
            variableValue = "";
        runtimeAPI.setVariable(activityInstanceUUID, variableName, variableValue);
    }

    /** Setta il valore di una variabile globale di una istanza di un processo
     *
     * @param processInstanceUUID l'istanza del processo del quale settare il valore della variabile
     * @param variableName il nome della variabile da settare
     * @param variableValue il valore da assegnare alla variabile
     */
    public void setProcessInstanceVariable(ProcessInstanceUUID processInstanceUUID, String variableName, Object variableValue) throws InstanceNotFoundException, VariableNotFoundException {
        if (variableValue == null)
            variableValue = "";
        runtimeAPI.setProcessInstanceVariable(processInstanceUUID, variableName, variableValue);
    }

    /** Esegue l'attività
     *
     * @param activityInstanceUUID l'attività da eseguire
     * @throws TaskNotFoundException
     * @throws IllegalTaskStateException
     * @throws InstanceNotFoundException
     */
    public void stepOn(ActivityInstanceUUID activityInstanceUUID) throws TaskNotFoundException, IllegalTaskStateException, InstanceNotFoundException {
        runtimeAPI.executeTask(activityInstanceUUID, true);
    }

    /** Ritorna lo stato corrente dell'istanza del processo passato
     *
     * @param processInstanceUUID l'id dell'istanza del processo del quale si vuole conoscere lo stato
     * @return lo stato
     * @throws InstanceNotFoundException
     */
    public InstanceState getProcessInstanceState(ProcessInstanceUUID processInstanceUUID) throws InstanceNotFoundException {
        return queryRuntimeAPI.getLightProcessInstance(processInstanceUUID).getInstanceState();
    }

    /** Indica se il proesso è terminata
     *
     * @param processInstanceUUID l'istanza del processo che si vuole sapere se è terminato
     * @return "true" se il processo è terminato, "false" altrimenti
     * @throws InstanceNotFoundException
     */
    public boolean isTerminated(ProcessInstanceUUID processInstanceUUID) throws InstanceNotFoundException {
        return queryRuntimeAPI.getLightProcessInstance(processInstanceUUID).getInstanceState() == InstanceState.FINISHED;
    }
    
    /** Ritorna il tipo di attività (Human, Automatic, ecc.)
     * 
     * @param processDefinitionUUID l'uuid del processo
     * @param activityName il nome dell'attività
     * @return il tipo di attività (Human, Automatic, ecc.)
     * @throws ProcessNotFoundException
     * @throws ActivityNotFoundException 
     */
    public ActivityDefinition.Type getActivityType(String processDefinitionUUID, String activityName) throws ProcessNotFoundException, ActivityNotFoundException {
        return queryDefinitionAPI.getProcessActivity(new ProcessDefinitionUUID(processDefinitionUUID), activityName).getType();
    }

    /** Ritorna la lista di tutti gli attachments di un'istanza di un processo
     *
     * @param processInstanceUUID l'istanza del processo del quale si vogliono ottenere gli attachments
     * @return la lista degli attachments trovati
     */
    public List<AttachmentInstance> listAttachments(ProcessInstanceUUID processInstanceUUID) {
        ArrayList<AttachmentInstance> attachmentsList = new ArrayList<AttachmentInstance>();
        Set<String> attachmentNames = queryRuntimeAPI.getAttachmentNames(processInstanceUUID);
        Iterator<String> iterator = attachmentNames.iterator();
        while (iterator.hasNext()) {
            String attachmentName = iterator.next();
//            List<AttachmentInstance> attachments = queryRuntimeAPI.getAttachments(processInstanceUUID, attachmentName);
            AttachmentInstance attachment = queryRuntimeAPI.getLastAttachment(processInstanceUUID, attachmentName);
            attachmentsList.add(attachment);
        }
        return attachmentsList;
    }

    /** Ritorna la lista degli attachments di un istanza di un processo con il nome indicato
     *
     * @param processInstanceUUID l'istanza del processo del quale si vogliono ottenere gli attachments
     * @param attachmentName il nome degli attachments da cercare
     * @return la lista degli attachments trovati
     */
    public List<AttachmentInstance> listAttachments(ProcessInstanceUUID processInstanceUUID, String attachmentName) {
        return queryRuntimeAPI.getAttachments(processInstanceUUID, attachmentName);
    }

    /** Ritorna l'ultima versione dell'attachment di un istanza di un processo con il nome indicato
     *
     * @param processInstanceUUID l'istanza del processo del quale si vogliono ottenere gli attachments
     * @param attachmentName il nome degli attachments da cercare
     * @return l'ultima versione dell'attachment trovata
     */
    public AttachmentInstance lastAttachment(ProcessInstanceUUID processInstanceUUID, String attachmentName) {
        return queryRuntimeAPI.getLastAttachment(processInstanceUUID, attachmentName);
    }

    /** Ritorna il valore in bytes di un attachment
     * 
     * @param attachmentInstance istanza rappresentate un attachment
     * @return il valore in bytes dell'attachment richiesto
     */
    public byte[] getAttachmentValue(AttachmentInstance attachmentInstance) {
        return queryRuntimeAPI.getAttachmentValue(attachmentInstance);
    }
    
    /** Ritorna l'elenco delle frecce uscenti dall'attività rappresentata dall id dell'istanza passata
     *
     * @param activityInstanceUUID l'id dell'istanza dell'attività della quale si vogliono conoscere le frecce uscenti
     * @return l'elenco delle frecce uscenti dall'attività
     * @throws TaskNotFoundException
     * @throws ProcessNotFoundException
     * @throws ActivityNotFoundException
     */
    public Set<TransitionDefinition> getNextActivitiesTransition(ActivityInstanceUUID activityInstanceUUID) throws TaskNotFoundException, ProcessNotFoundException, ActivityNotFoundException {
        TaskInstance task = queryRuntimeAPI.getTask(activityInstanceUUID);
        String activityName = task.getActivityName();
        ProcessDefinitionUUID processDefinitionUUID = task.getProcessDefinitionUUID();
        return getNextActivitiesTransition(activityName, processDefinitionUUID);
    }
    
    /** Ritorna l'elenco delle frecce uscenti dall'attività rappresentata dall id dell'istanza passata
     *
     * @param activityName il nome dell'attività della quale si vogliono conoscere le frecce uscenti
     * @param processDefinitionUUID l'UUID della definizione del processo da interrogare
     * @return l'elenco delle frecce uscenti dall'attività
     * @throws TaskNotFoundException
     * @throws ProcessNotFoundException
     * @throws ActivityNotFoundException
     */
    public Set<TransitionDefinition> getNextActivitiesTransition(String activityName, ProcessDefinitionUUID processDefinitionUUID) throws TaskNotFoundException, ProcessNotFoundException, ActivityNotFoundException {
        ActivityDefinition processActivity = queryDefinitionAPI.getProcessActivity(processDefinitionUUID, activityName);
        return processActivity.getOutgoingTransitions();
    }

    /** Ritorna l'elenco dei nomi delle atività raggiunte dalle frecce uscenti dall'attività rappresentata dall id dell'istanza passata
     *
     * @param activityInstanceUUID l'id dell'istanza dell'attività della quale si vogliono le attività raggiunte dalle frecce uscenti
     * @return l'elenco dei nomi delle atività raggiunte dalle frecce uscenti dall'attività
     * @throws TaskNotFoundException
     * @throws ProcessNotFoundException
     * @throws ActivityNotFoundException
     */
        public ArrayList<String> getNextActivitiesName(ActivityInstanceUUID activityInstanceUUID) throws TaskNotFoundException, ProcessNotFoundException, ActivityNotFoundException {
        ArrayList<String> nextActivitiesName = new ArrayList<String>();
        Set<TransitionDefinition> outgoingTransitions = getNextActivitiesTransition(activityInstanceUUID);
        Iterator<TransitionDefinition> iterator = outgoingTransitions.iterator();
        while (iterator.hasNext()) {
            TransitionDefinition nextTransition = iterator.next();
            String nextActivityName = nextTransition.getTo();
            nextActivitiesName.add(nextActivityName);
        }
        return nextActivitiesName;
    }

    /** Ritorna la freccia uscente di default dall'attività rappresentata dall id dell'istanza passata
     *
     * @param activityInstanceUUID l'id dell'istanza dell'attività della quale si vuule conoscere la freccia uscente di default
     * @return la freccia uscente di default
     * @throws TaskNotFoundException
     * @throws ProcessNotFoundException
     * @throws ActivityNotFoundException
     */
    public TransitionDefinition getDefaultNextActivityTransition(ActivityInstanceUUID activityInstanceUUID) throws TaskNotFoundException, ProcessNotFoundException, ActivityNotFoundException {
        Set<TransitionDefinition> outgoingTransitions = getNextActivitiesTransition(activityInstanceUUID);
        Iterator<TransitionDefinition> iterator = outgoingTransitions.iterator();
        while (iterator.hasNext()) {
            TransitionDefinition nextTransition = iterator.next();
            if (nextTransition.isDefault())
                return nextTransition;
        }
        return null;
    }
    
    /** Ritorna il ProcessInstanceUUID relativo all'istanza di attività passata
     * 
     * @param activityInstanceUUID l'istanza di attività della quale si vuole conoscere il ProcessInstanceUUID
     * @return il ProcessInstanceUUID relativo all'istanza di attività passata
     * @throws ActivityNotFoundException 
     */
    public ProcessInstanceUUID getProcessInstanceUUID(ActivityInstanceUUID activityInstanceUUID) throws ActivityNotFoundException {
        return queryRuntimeAPI.getActivityInstance(activityInstanceUUID).getProcessInstanceUUID();
    }
    
    /** Ritorna il ProcessDefinitionUUID relativo all'istanza di processo passata
     * 
     * @param processInstanceUUID l' istanza di processo della quale si vuole conoscere il process ProcessDefinitionUUID
     * @return il ProcessDefinitionUUID relativo all'istanza di processo passata
     * @throws InstanceNotFoundException 
     */
    public ProcessDefinitionUUID getProcessDefinitionUUID(ProcessInstanceUUID processInstanceUUID) throws InstanceNotFoundException {
        return queryRuntimeAPI.getProcessInstance(processInstanceUUID).getProcessDefinitionUUID();
    }
    
    /** Ritorna il ProcessDefinitionUUID relativo all'istanza di attività passata
     * 
     * @param processInstanceUUID l' istanza di processo della quale si vuole conoscere il process ProcessDefinitionUUID
     * @return il ProcessDefinitionUUID relativo all'istanza di processo passata
     * @throws InstanceNotFoundException 
     */
    public ProcessDefinitionUUID getProcessDefinitionUUID(ActivityInstanceUUID activityInstanceUUID) throws ActivityNotFoundException {
        return queryRuntimeAPI.getActivityInstance(activityInstanceUUID).getProcessDefinitionUUID();
    }

    /** Ritorna il nome dell'attività identificata dall'activityInstanceUUID passato
     *
     * @param activityInstanceUUID l'activityInstanceUUID dell'attività della quale si vuole conoscere il nome
     * @return il nome dell'attività identificata dall'activityInstanceUUID passato
     * @throws ActivityNotFoundException
     */
    public String geActivityName(ActivityInstanceUUID activityInstanceUUID) throws ActivityNotFoundException {
        return queryRuntimeAPI.getActivityInstance(activityInstanceUUID).getActivityName();
    }

    /** Ritorna il nome dell'attività raggiunta dalla freccia uscente di default dall'attività rappresentata dall'id dell'istanza passata
     *
     * @param activityInstanceUUID l'id dell'istanza dell'attività della quale si vuole conoscere il nome dell'attività raggiunta dalla freccia uscente di default
     * @return il nome dell'attività raggiunta dalla freccia uscente di default
     * @throws TaskNotFoundException
     * @throws ProcessNotFoundException
     * @throws ActivityNotFoundException
     */
    public String getDefaultNextActivityName(ActivityInstanceUUID activityInstanceUUID) throws TaskNotFoundException, ProcessNotFoundException, ActivityNotFoundException  {
        TransitionDefinition defaultNextActivityTransition = getDefaultNextActivityTransition(activityInstanceUUID);
        if (defaultNextActivityTransition == null)
            return null;
        else
            return defaultNextActivityTransition.getTo();
    }
    
    /** Ritorna la lista delle frecce uscenti il cui nome comincia con il prefisso passato e l'ordine di priorità di ognuna
     * 
     * @param activityInstanceUUID l'id dell'istanza dell'attività della quale si vogliono conoscere i nomi delle frecce uscenti
     * @param prefix il prefisso secondo il quale filtrare le frecce
     * @return la lista delle frecce uscenti il cui nome comincia con il prefisso passato
     * @throws TaskNotFoundException
     * @throws ProcessNotFoundException
     * @throws ActivityNotFoundException
     */
    public ArrayList<String[]> getTransitionNamesStartWithPrefix(ActivityInstanceUUID activityInstanceUUID, String prefix) throws TaskNotFoundException, ProcessNotFoundException, ActivityNotFoundException {

        Set<TransitionDefinition> outgoingTransitions = getNextActivitiesTransition(activityInstanceUUID);
        return getTransitionNamesStartWithPrefixInternal(outgoingTransitions, prefix);
    }
    
    /** Ritorna la lista delle frecce uscenti il cui nome comincia con il prefisso passato e l'ordine di priorità di ognuna
     * 
     * @param activityName il nome dell'attività della quale si vogliono conoscere i nomi delle frecce uscenti
     * @param processDefinitionUUID l'UUID della definizione del processo da interrogare
     * @param prefix il prefisso secondo il quale filtrare le frecce
     * @return la lista delle frecce uscenti il cui nome comincia con il prefisso passato
     * @throws TaskNotFoundException
     * @throws ProcessNotFoundException
     * @throws ActivityNotFoundException
     */
    public ArrayList<String[]> getTransitionNamesStartWithPrefix(String activityName, ProcessDefinitionUUID processDefinitionUUID, String prefix) throws TaskNotFoundException, ProcessNotFoundException, ActivityNotFoundException {

        Set<TransitionDefinition> outgoingTransitions = getNextActivitiesTransition(activityName, processDefinitionUUID);
        return getTransitionNamesStartWithPrefixInternal(outgoingTransitions, prefix);
    }

    private ArrayList<String[]> getTransitionNamesStartWithPrefixInternal(Set<TransitionDefinition> outgoingTransitions, String prefix) throws TaskNotFoundException, ProcessNotFoundException, ActivityNotFoundException {

        ArrayList<String[]> transitionNames = new ArrayList<String[]>();
        // se presente tolgo il carattere di underscore ("_") alla fine del prefisso
        if (prefix.substring(prefix.length() - 1).equals("_"))
            prefix = prefix.substring(0, prefix.length() - 1);
//        String compiledPrefix = prefix + "\\d\\d";
        Iterator<TransitionDefinition> iterator = outgoingTransitions.iterator();
        while (iterator.hasNext()) {
            TransitionDefinition transition = iterator.next();
            String name = transition.getLabel();

            if (name != null) {
                int underScorePosition = name.indexOf("_");
                if (underScorePosition != -1) {
                    String namePrefix = name.substring(0, underScorePosition);
                    Pattern pattern = Pattern.compile(prefix);
                    Matcher matcher = pattern.matcher(namePrefix);
                    if (matcher.find(0)) {
                        String[] element = new String[2];
                        element[0] = transition.getTo();
                        Pattern digitPattern = Pattern.compile("\\d");
                        matcher = digitPattern.matcher(namePrefix);
                        matcher.find(0);
                        element[1] = namePrefix.substring(matcher.start());
                        transitionNames.add(element);
                    }
                }
            }
        }
        return transitionNames;
    }

    /** Ritorna l'elenco delle freccie uscenti (e quindi degli stati raggiungibili) a seconda del contesto (cioè della lista degli attori) relative ad un utente
     *
     * @param activityInstanceUUID l'id dell'istanza dell'attività della quale si vuole conoscere l'elenco delle freccie uscenti
     * @param svActions la lista di SVAction rappresentate la lista degli attori correnti del processo
     * @param idUtente l'utente attuale
     * @param documentoModificato indica se il documento è stato modificato (se "true" verranno mostrate le freccie "s_" (self)
     * @return l'elenco delle freccie uscenti a seconda del contesto relative ad un utente
     * @throws ActivityNotFoundException
     * @throws BonitaMiddlewareException
     * @throws TaskNotFoundException
     * @throws ProcessNotFoundException
     */
    public ArrayList<String[]> getTransitionNamesByContext(ActivityInstanceUUID activityInstanceUUID, ArrayList svActions, String idUtente, boolean documentoModificato) throws ActivityNotFoundException, BonitaMiddlewareException, TaskNotFoundException, ProcessNotFoundException {
        ArrayList<String[]> transitionNamesResult = new ArrayList<String[]>();

        // individuo il servizio di apparteneza dell'utente passato
        String currentActivityName = queryRuntimeAPI.getActivityInstance(activityInstanceUUID).getActivityName();
        String servizioAppartenenza = null;
        try {
            SVAction sVAction = SVActionArrayUtils.getSVActionByUserAndTaskName(svActions, idUtente, currentActivityName);
            servizioAppartenenza = sVAction.getServizioAppartenenza();
        }
        catch (SVActionException ex) {
            throw new BonitaMiddlewareException(ex.getCause());
        }
        // per ogni freccia uscente "bo_"(back out) controllo se esiste almeno un utente con "Servizio di appartenenza" uguale al servizio di appartenenza passato
        // se esiste aggiungo la freccia uscente all'elenco delle frecce uscente risultanti
        boolean atLeastOneBackOut = false;
        ArrayList<String[]> bo_transitions = getTransitionNamesStartWithPrefix(activityInstanceUUID, "bo_");
        for (int i=0; i<bo_transitions.size(); i++) {
            String[] destinationActivity = bo_transitions.get(i);
            try {
                // se esiste almeno un utente con "Servizio di appartenenza" uguale al servizio di appartenenza passato
                if (SVActionArrayUtils.isNomeServizioInSVActionList(svActions, servizioAppartenenza, destinationActivity[0])) {
                    transitionNamesResult.add(destinationActivity);
                    atLeastOneBackOut = true;
                }
            }
            catch (SVActionException ex) {
                throw new BonitaMiddlewareException(ex.getCause());
            }
        }

        // solo se non ho aggiunto nessuna freccia "bo_"(back out), per ogni freccia "b_" (back) controllo se è presente almeno un utente appartienente al task e in caso aggiungo la freccia uscente all'elenco delle frecce uscente risultanti
        if (!atLeastOneBackOut) {
            ArrayList<String[]> b_transitions = getTransitionNamesStartWithPrefix(activityInstanceUUID, "b_");
            for (int i=0; i<b_transitions.size(); i++) {
                String[] destinationActivity = b_transitions.get(i);
                try {
                    // se è presente almeno un utente appartenente al task
                    if (SVActionArrayUtils.isTaskNameInSVActionList(svActions, b_transitions.get(i)[0])) {
                        transitionNamesResult.add(destinationActivity);
                    }
                }
                catch (SVActionException ex) {
                    throw new BonitaMiddlewareException(ex.getCause());
                }
            }
        }
        // se il documento è stato modificato e c'è più di un untente nel task corrente aggiungo la freccia self
        try {          
            if (documentoModificato && SVActionArrayUtils.filterSVActionList(svActions, currentActivityName).size() > 1) {
                transitionNamesResult.addAll(getTransitionNamesStartWithPrefix(activityInstanceUUID, "s_"));
            }
        }
        catch (SVActionException ex) {
            throw new BonitaMiddlewareException(ex.getCause());
        }
        return transitionNamesResult;

    }
    
    @Deprecated
    /** Ritorna l'elenco delle freccie uscenti (e quindi degli stati raggiungibili) a seconda del contesto (cioè della lista degli attori) relative ad un utente
     *
     * @param activityInstanceUUID l'id dell'istanza dell'attività della quale si vuole conoscere l'elenco delle freccie uscenti
     * @param svActions la lista di SVAction rappresentate la lista degli attori correnti del processo
     * @param idUtente l'utente attuale
     * @param documentoModificato indica se il documento è stato modificato (se "true" verranno mostrate le freccie "s_" (self)
     * @return l'elenco delle freccie uscenti a seconda del contesto relative ad un utente
     * @throws ActivityNotFoundException
     * @throws BonitaMiddlewareException
     * @throws TaskNotFoundException
     * @throws ProcessNotFoundException
     */
    public ArrayList<String[]> getTransitionNamesByContext_bak(ActivityInstanceUUID activityInstanceUUID, ArrayList svActions, String idUtente, boolean documentoModificato) throws ActivityNotFoundException, BonitaMiddlewareException, TaskNotFoundException, ProcessNotFoundException {
        ArrayList<String[]> transitionNamesResult = new ArrayList<String[]>();

        // individuo il servizio di apparteneza dell'utente passato
        String currentActivityName = queryRuntimeAPI.getActivityInstance(activityInstanceUUID).getActivityName();
        String servizioAppartenenza = null;
        try {
            SVAction sVAction = SVActionArrayUtils.getSVActionByUserAndTaskName(svActions, idUtente, currentActivityName);
            servizioAppartenenza = sVAction.getServizioAppartenenza();
        }
        catch (SVActionException ex) {
            throw new BonitaMiddlewareException(ex.getCause());
        }
        // per ogni freccia uscente "b_"(back) controllo se esiste almeno un utente con "Servizio di appartenenza" uguale al servizio di appartenenza passato
        // se esiste aggiungo la freccia uscente all'elenco delle frecce uscente risultanti
        //boolean atLeastOneBackOut = false;
        ArrayList<String[]> b_transitions = getTransitionNamesStartWithPrefix(activityInstanceUUID, "b_");
        for (int i=0; i<b_transitions.size(); i++) {
            String[] destinationActivity = b_transitions.get(i);
            try {
                // se esiste almeno un utente con "Servizio di appartenenza" uguale al servizio di appartenenza passato
                if (SVActionArrayUtils.isNomeServizioInSVActionList(svActions, servizioAppartenenza, destinationActivity[0])) {
                    transitionNamesResult.add(destinationActivity);
                    //atLeastOneBackOut = true;
                }
            }
            catch (SVActionException ex) {
                throw new BonitaMiddlewareException(ex.getCause());
            }
        }

        // se il documento è stato modificato e c'è più di un untente nel task corrente aggiungo la freccia self
        try {          
            if (documentoModificato && SVActionArrayUtils.filterSVActionList(svActions, currentActivityName).size() > 1) {
                transitionNamesResult.addAll(getTransitionNamesStartWithPrefix(activityInstanceUUID, "s_"));
            }
        }
        catch (SVActionException ex) {
            throw new BonitaMiddlewareException(ex.getCause());
        }
        return transitionNamesResult;

    }

    /** Ritorna l'elenco delle attività eseguite
     *
     * @param processInstanceUUID l'instanza del processo del quale si vogliono conoscere le attività precedenti terminate
     * @return l'elenco delle attività eseguite
     * @throws InstanceNotFoundException
     * @throws TaskNotFoundException
     */
    public ArrayList<String> getPreviosExecutedActivitiesByProcessInstanceUUID(ProcessInstanceUUID processInstanceUUID) throws InstanceNotFoundException, TaskNotFoundException {
        Set<LightActivityInstance> activityInstances = listActivityInstances(processInstanceUUID, ActivityState.FINISHED);
        ArrayList<String> previosExecutedActivities = new ArrayList<String>();
        Iterator<LightActivityInstance> iterator = activityInstances.iterator();
        int i = 0;
        Map.Entry<String, Date>[] activityNamesAndDate = new Map.Entry[activityInstances.size()];
        while (iterator.hasNext()) {
            LightActivityInstance activityInstance = iterator.next();
            Map.Entry<String, Date> entry = new HashMap.SimpleEntry<String, Date>(activityInstance.getActivityName(), activityInstance.getEndedDate());
            activityNamesAndDate[i] = entry;
            i++;
        }

        Comparator myComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
               Map.Entry<String, Date> elem1 = (Map.Entry<String, Date>)o1;
               Map.Entry<String, Date> elem2 = (Map.Entry<String, Date>)o2;
               if (elem1.getValue().before(elem2.getValue()))
                   return -1;
               else if (elem1.getValue().equals(elem2.getValue()))
                   return 0;
               else
                   return 1;
            }
        };
        Arrays.sort(activityNamesAndDate, myComparator);
        for (i=0; i<activityNamesAndDate.length; i++) {
            previosExecutedActivities.add(activityNamesAndDate[i].getKey());
        }

        return previosExecutedActivities;
    }

    /** Ritorna l'elenco delle attività eseguite
     *
     * @param activityInstanceUUID l'instanza dell'attività della quale si vogliono conoscere le attività precedenti terminate
     * @return l'elenco delle attività eseguite
     * @throws InstanceNotFoundException
     * @throws TaskNotFoundException
     */
    public ArrayList<String> getPreviosExecutedActivitiesByActivityInstanceUUID(ActivityInstanceUUID activityInstanceUUID) throws InstanceNotFoundException, TaskNotFoundException {
        ProcessInstanceUUID processInstanceUUID = queryRuntimeAPI.getTask(activityInstanceUUID).getProcessInstanceUUID();
        return getPreviosExecutedActivitiesByProcessInstanceUUID(processInstanceUUID);
    }

    /** Ritorna l'ultima attività eseguita
     *
     * @param processInstanceUUID l'instanza del processo del quale si vuole conoscere il nome dell'ultima attività eseguita
     * @return
     * @throws InstanceNotFoundException
     * @throws TaskNotFoundException
     */
    public String getLastExecutedActivityByProcessInstanceUUID(ProcessInstanceUUID processInstanceUUID) throws InstanceNotFoundException, TaskNotFoundException {
        ArrayList<String> previosExecutedActivitiesList = getPreviosExecutedActivitiesByProcessInstanceUUID(processInstanceUUID);
        int lastElementIndex = previosExecutedActivitiesList.size() - 1;
        if (lastElementIndex >= 0)
            return previosExecutedActivitiesList.get(lastElementIndex);
        else
            return null;
    }

    /** Ritorna l'ultima attività eseguita
     *
     * @param activityInstanceUUID l'instanza dell'attività della quale si vuole conoscere il nome dell'ultima attività eseguita
     * @return
     * @throws TaskNotFoundException
     * @throws InstanceNotFoundException
     */
    public String getLastExecutedActivityByActivityInstanceUUID(ActivityInstanceUUID activityInstanceUUID) throws TaskNotFoundException, InstanceNotFoundException {
        ProcessInstanceUUID processInstanceUUID = queryRuntimeAPI.getTask(activityInstanceUUID).getProcessInstanceUUID();
        return getLastExecutedActivityByProcessInstanceUUID(processInstanceUUID);
    }

    /** Ritorna "true" se l'attività passata è stata eseguita almeno una volta, "false" altrimenti
     * 
     * @param processInstanceUUID l'istanza del processo nel quale cercare l'attività
     * @param ActivityName il nome dell'attività da cercare
     * @return "true" sel il nome dell'attività passata è presente nella lista delle attività eseguita dall'istanza del processo passato, "false" altrimenti
     * @throws InstanceNotFoundException
     * @throws TaskNotFoundException
     */
    public boolean isActivityInExecutedActivitiesByProcessInstanceUUID(ProcessInstanceUUID processInstanceUUID, String ActivityName) throws InstanceNotFoundException, TaskNotFoundException {
        ArrayList<String> executedActivitiesList = getPreviosExecutedActivitiesByProcessInstanceUUID(processInstanceUUID);
        return executedActivitiesList.contains(ActivityName);
    }

    /** Ritorna "true" se l'attività passata è stata eseguita almeno una volta, "false" altrimenti
     *
     * @param activityInstanceUUID l'istanza dell'attività corrente dalla quale ricavarsi l'istanza del processo nel quale cercare l'attività
     * @param ActivityName il nome dell'attività da cercare
     * @return "true" sel il nome dell'attività passata è presente nella lista delle attività eseguita dall'istanza del processo passato, "false" altrimenti
     * @throws InstanceNotFoundException
     * @throws TaskNotFoundException
     */
    public boolean isActivityInExecutedActivitiesByActivityInstanceUUID(ActivityInstanceUUID activityInstanceUUID, String ActivityName) throws InstanceNotFoundException, TaskNotFoundException {
        ArrayList<String> executedActivitiesList = getPreviosExecutedActivitiesByActivityInstanceUUID(activityInstanceUUID);
        return executedActivitiesList.contains(ActivityName);
    }
    
    /** Ordina l'elenco delle attività con priorità passate in base alla priorita (01 priorità maggiore)
     * 
     * @param activities l'elenco delle attività con priorità da ordinare
     * @return l'elenco delle attività con priorità ordinate in base alla priorita
     */
    public ArrayList<String[]> sortTransitionNameByPriority(ArrayList<String[]> activities) {
        Object[] activitiesArray = activities.toArray();
        Comparator myComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
               String[] elem1 = (String[])o1;
               String[] elem2 = (String[])o2;
               if (Integer.parseInt(elem1[1]) < Integer.parseInt(elem2[1]))
                   return -1;
               else if (Integer.parseInt(elem1[1]) == Integer.parseInt(elem2[1]))
                   return 0;
               else
                   return 1;
            }
        };
        Arrays.sort(activitiesArray, myComparator);
        ArrayList<String[]> activitiesSorted = new ArrayList<String[]>();
        for (int i=0; i<activitiesArray.length; i++)
            activitiesSorted.add((String[])activitiesArray[i]);
        return activitiesSorted;
    }

    /** Ritorna l'istanza alle ManagementAPI della libreria
     * 
     * @return l'istanza alle ManagementAPI della libreria
     */
    public ManagementAPI getManagementAPI() {
        return this.managementAPI;
    }

    /** Ritorna l'istanza alle QueryDefinitionAPI della libreria
     * 
     * @return l'istanza alle QueryDefinitionAPI della libreria
     */
    public QueryDefinitionAPI getQueryDefinitionAPI() {
        return this.queryDefinitionAPI;
    }

    /** Ritorna l'istanza alle QueryRuntimeAPI della libreria
     * 
     * @return l'istanza alle QueryRuntimeAPI della libreria
     */
    public QueryRuntimeAPI getQueryRuntimeAPI() {
        return this.queryRuntimeAPI;
    }

    /** Ritorna l'istanza alle RuntimeAPI della libreria
     * 
     * @return l'istanza alle RuntimeAPI della libreria
     */
    public RuntimeAPI getRuntimeAPI() {
        return this.runtimeAPI;
    }

    /** Ritorna l'istanza alle IdentityAPI della libreria
     * 
     * @return l'istanza alle IdentityAPI della libreria
     */
    public IdentityAPI getIdentityAPI() {
        return this.identityAPI;
    }
    
    /** Ritorna l'istanza alle RepairAPI della libreria
     * 
     * @return l'istanza alle RepairAPI della libreria
     */
    public RepairAPI getRepairAPI() {
        return this.repairAPI;
    }
    
    /** Ritorna l'istanza alle CommandAPI della libreria
     * 
     * @return l'istanza alle CommandAPI della libreria
     */
    public CommandAPI getCommandAPI() {
       return this.commandAPI;
    }
}
