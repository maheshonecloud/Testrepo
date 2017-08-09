package com.cloupia.feature.purestorage.tasks;


import com.cisco.cuic.api.client.WorkflowInputFieldTypeDeclaration;
import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.UcsdCmdbUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.HostInventoryConfig;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.fw.objstore.ObjStore;
import com.cloupia.fw.objstore.ObjStoreHelper;
import com.cloupia.service.cIM.inframgr.AbstractTask;
import com.cloupia.service.cIM.inframgr.TaskConfigIf;
import com.cloupia.service.cIM.inframgr.TaskOutputDefinition;
import com.cloupia.service.cIM.inframgr.customactions.CustomActionLogger;
import com.cloupia.service.cIM.inframgr.customactions.CustomActionTriggerContext;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.exceptions.PureException;
import com.purestorage.rest.host.PureHost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;


public class NewHostTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(NewHostTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	NewHostTaskConfig config = (NewHostTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);

        String hostName = config.getHostName();
        String wwns = config.getWwns();
        String iqns = config.getIqns();
        Boolean deleteHostFlag = config.getDeleteHostFlag();
        String privateVolumes = config.getPrivateVolumes();
        String hostGroupName = config.getHostGroupName();
        List<PureHost> allHost = CLIENT.hosts().list();
        List<String> allHostName = new ArrayList<String>();
        Boolean existHost = false;

        if(wwns == null)
        {
        	wwns="";
        }
        if(iqns == null)
        {
        	iqns="";
        }
        
        
        for(PureHost oneHost : allHost)
        {
            if(oneHost.getName().equals(hostName))
            {
                existHost = true;
            }
        }
        config.setNewHostFlag(true);
        config.setExistHost(existHost);

        

        if(deleteHostFlag == null)
        {
            actionlogger.addInfo("Start creating host " + hostName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
            CLIENT.hosts().create(hostName);

            if(!wwns.equals(""))
            {
                String[] wwnArray = wwns.split(",");
                CLIENT.hosts().addWwnList(hostName, Arrays.asList(wwnArray));
            }
            
            if(!iqns.equals("")){
                String[] iqnArray = iqns.split(",");
                CLIENT.hosts().addIqnList(hostName, Arrays.asList(iqnArray));
            }
            actionlogger.addInfo("Successfully created host " + hostName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
           context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_HOST_NAME, hostName);
           context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_HOST_NAMES, hostName);
        	actionlogger.addInfo("Host Name as Output is saved");
            
        	ObjStore<HostInventoryConfig> store = ObjStoreHelper.getStore(HostInventoryConfig.class);
            
            String hostIdentity =accountName+"@"+hostName;
            HostInventoryConfig hostConfig = new HostInventoryConfig();
        	hostConfig.setId(hostIdentity);
        	   hostConfig.setAccountName(accountName);
            hostConfig.setHost(hostName); // Name
            hostConfig.setHostGroup("");
            hostConfig.setVolumes(0);
            hostConfig.setProvisionedSize(0.0);
            hostConfig.setConnectedVolumes("");
            store.insert(hostConfig);
            
            
        	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_HOST_IDENTITY, hostIdentity);
        	actionlogger.addInfo("Host Identity as Output is saved");
        	//UcsdCmdbUtils cmdb=new UcsdCmdbUtils();
            String description="FlashArray Host is created. Details are : Account Name = "+config.getAccountName()+" , Host Name = "+ hostName+" , WWNS = "+wwns+" , IQNS = "+iqns;
            
            UcsdCmdbUtils.updateRecord("FlashArray Host", description, 1, context.getUserId(), hostName,description);
            
            //context.getChangeTracker().resourceAdded("FlashArray Host", hostIdentity, hostName, description);
            context.getChangeTracker().undoableResourceAdded("FlashArray Host : Created", hostIdentity, hostName, description,
                    new DeleteHostTask().getTaskName(), new DeleteHostTaskConfig(config));
            
        }

        else
        {
            actionlogger.addInfo("This is a rollback task to create the deleted host " + hostName);

            try
            {
                CLIENT.hosts().create(hostName);
            }
            catch(PureException e)
            {
                actionlogger.addInfo("Error happens when rollback task trys to create the deleted host " + " Exception: " + e.getMessage());
            }

            if(!wwns.equals(""))
            {
                String[] wwnArray = wwns.split(",");
                try
                {
                    CLIENT.hosts().addWwnList(hostName, Arrays.asList(wwnArray));
                }catch (PureException e)
                {
                    actionlogger.addInfo("Error happens when rollback task trys to add wwns to the deleted host " + " Exception: " + e.getMessage());
                }
            }
            
            if(!iqns.equals(""))
            {
                String[] iqnArray = iqns.split(",");
                try
                {
                    CLIENT.hosts().addIqnList(hostName, Arrays.asList(iqnArray));
                }
                catch (PureException e)
                {
                    actionlogger.addInfo("Error happens when rollback task trys to add iqns to the deleted host " + " Exception: " + e.getMessage());
                }
            }
        }

        if(privateVolumes != null && !privateVolumes.equals(""))
        {
            String[] volumes = privateVolumes.split(",");
            for(String volume : volumes)
            {
                try
                {
                    CLIENT.hosts().connectVolume(hostName, volume);
                }
                catch (PureException e)
                {
                    actionlogger.addInfo("Error happens when connecting with volume " + volume + " Exception: " + e.getMessage());
                }
            }
        }

        //actionlogger.addInfo("hostGroup name is " + hostGroupName);

        if(hostGroupName != null && ! hostGroupName.equals(""))
        {
            List<String> hostList = new ArrayList<String>();
            hostList.add(hostName);
            try
            {
                CLIENT.hostGroups().addHosts(hostGroupName, hostList);
            }
            catch (PureException e)
            {
                actionlogger.addInfo("Error happens when connecting with host group " + hostGroupName + " Exception: " + e.getMessage());
            }
        }
    }

    @Override
   	public TaskOutputDefinition[] getTaskOutputDefinitions() {
   		TaskOutputDefinition[] ops = new TaskOutputDefinition[3];

   		ops[0] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_HOST_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Host Identity");

   		ops[1] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_HOST_NAME,
   				PureConstants.PURE_HOST_LIST_TABLE_NAME,
   				"FlashArray Host Name");
   		
   		ops[2] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_HOST_NAMES,
   				PureConstants.PURE_HOST_LIST_TABLE_NAMES,
   				"FlashArray Host Name(s)");
   		return ops;
    }

	@Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new NewHostTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_NEW_HOST;
	}
}