package com.cloupia.feature.purestorage.tasks;


import com.cisco.cuic.api.client.WorkflowInputFieldTypeDeclaration;
import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.UcsdCmdbUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.HostInventoryConfig;
import com.cloupia.feature.purestorage.accounts.VolumeInventoryConfig;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.fw.objstore.ObjStore;
import com.cloupia.fw.objstore.ObjStoreHelper;
import com.cloupia.service.cIM.inframgr.AbstractTask;
import com.cloupia.service.cIM.inframgr.TaskConfigIf;
import com.cloupia.service.cIM.inframgr.TaskOutputDefinition;
import com.cloupia.service.cIM.inframgr.customactions.CustomActionLogger;
import com.cloupia.service.cIM.inframgr.customactions.CustomActionTriggerContext;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.host.PureHostConnection;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


public class DeleteHostTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(DeleteHostTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	DeleteHostTaskConfig config = (DeleteHostTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);

        String hostName = config.getHostName();
        Boolean newHostTaskFlag = config.getNewHostFlag();
        Boolean existHost = config.getExistHost();
        String hostGroupName = "";
        String wwns = "";
        String iqns = "";
        StringBuilder privateVolumes = new StringBuilder();
        List<String> wwnsList = CLIENT.hosts().get(hostName).getWwnList();
        List<String> iqnsList = CLIENT.hosts().get(hostName).getIqnList();
        List<PureHostConnection> privateConnectedVolumes = CLIENT.hosts().getPrivateConnections(hostName);
        List<PureHostConnection> sharedConnectedVolumes = CLIENT.hosts().getSharedConnections(hostName);
        config.setDeleteHostFlag(true);

        if(newHostTaskFlag != null)
        {
            actionlogger.addInfo("This is a rollback task to delete a new host " + hostName);
        }
        else
        {
        	existHost=false;
        }
        actionlogger.addInfo("Deleting host " + hostName + " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");

        wwns = StringUtils.join(wwnsList, ",");
        iqns = StringUtils.join(iqnsList, ",");

        for(PureHostConnection volumeConnection : privateConnectedVolumes)
        {
            String volumeName = volumeConnection.getVolumeName();
            privateVolumes.append(volumeName + ",");
        }
        actionlogger.addInfo("private volume is " + privateVolumes);

        if(sharedConnectedVolumes.size()>0)
        {
            hostGroupName = sharedConnectedVolumes.get(0).getHostGroupName();
            actionlogger.addInfo("hostgroup name is " + hostGroupName);
        }

        config.setWwns(wwns);
        config.setIqns(iqns);
        config.setPrivateVolumes(privateVolumes.toString());
        config.setHostGroupName(hostGroupName);
        //config.setExistHost(existHost);

       // actionlogger.addInfo("Exist Host "+existHost);
        if(existHost != null && !existHost)
        {
            for(PureHostConnection volumeConnection : privateConnectedVolumes)
            {
                String volumeName = volumeConnection.getVolumeName();
                CLIENT.hosts().disconnectVolume(hostName, volumeName);
            }
            if(!hostGroupName.equals(""))
            {
                List<String> tempHostList  = new ArrayList<String>();
                tempHostList.add(hostName);
                CLIENT.hostGroups().removeHosts(hostGroupName, tempHostList);
            }
            CLIENT.hosts().delete(hostName);
            actionlogger.addInfo("Successfully deleted host " + hostName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
        
           
            String hostIdentity =accountName+"@"+hostName;
            
            
        	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_HOST_IDENTITY, hostIdentity);
        	actionlogger.addInfo("Host Identity as Output is saved");
        	
        	String description="FlashArray Host is Deleted. Details are : Account Name = "+config.getAccountName()+" , Host Name = "+ hostName+" , WWNS = "+wwns+" , IQNS = "+iqns;
            
            UcsdCmdbUtils.updateRecord("FlashArray Host", description, 2, context.getUserId(), hostName,description);
            
            context.getChangeTracker().resourceAdded("FlashArray Host : Deleted", hostIdentity, hostName, description);
            context.getChangeTracker().undoableResourceDeleted("FlashArray Host", hostIdentity, hostName, description,
                    new NewHostTask().getTaskName(), new NewHostTaskConfig(config));
            
            ObjStore<HostInventoryConfig> store2 = ObjStoreHelper.getStore(HostInventoryConfig.class);
        	   
            String query3 = "id == '" + accountName+"@"+hostName + "'";
            List<HostInventoryConfig> hconfig = store2.query(query3);
            actionlogger.addInfo("Host Id :"+ hconfig.get(0).getId());
        
            long s = store2.delete(query3);
            actionlogger.addInfo("Deleted in Inventory :" + s);
        }

        else
        {
            actionlogger.addError("This host cannot be deleted!");
        }

    
    }

    @Override
    public TaskOutputDefinition[] getTaskOutputDefinitions()
    {
    	TaskOutputDefinition[] ops = new TaskOutputDefinition[1];
   		

   		ops[0] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_HOST_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Host Identity");
   		return ops;
    }

	@Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new DeleteHostTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_DELETE_HOST;
	}


}
