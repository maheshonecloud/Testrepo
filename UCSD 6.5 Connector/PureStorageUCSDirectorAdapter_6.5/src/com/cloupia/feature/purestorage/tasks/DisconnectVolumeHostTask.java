package com.cloupia.feature.purestorage.tasks;


import java.util.List;

import org.apache.log4j.Logger;

import com.cisco.cuic.api.client.WorkflowInputFieldTypeDeclaration;
import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.HostGroupInventoryConfig;
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
import com.purestorage.rest.host.PureHostConnection;
import com.purestorage.rest.hostgroup.PureHostGroupConnection;


public class DisconnectVolumeHostTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(DisconnectVolumeHostTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	DisconnectVolumeHostTaskConfig config = (DisconnectVolumeHostTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);

        String allVolumeName = config.getVolumeName();
        String hostName = config.getHostName();
        String[] volumeNameList = allVolumeName.split(",");
        actionlogger.addInfo("Starting disconnecting volume(s) from host " + hostName +
                " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
        String connVol="";
        try
        {
            for(int i=0; i<volumeNameList.length; i++)
            {
                String volumeName = volumeNameList[i];
                CLIENT.hosts().disconnectVolume(hostName, volumeName);
                actionlogger.addInfo("Host Name is : " +hostName);
                
                
                if(connVol=="")
                {
                	connVol=accountName+"@"+volumeName;	
                }
                else
                {
                	connVol=connVol+","+accountName+"@"+volumeName;
                }
                
            }
        }
        catch (PureException e)
        {
            actionlogger.addError("Error happened when disconnect volume with hosts" + "Exception: " + e.getMessage());
        }

        actionlogger.addInfo("Successfully Disconnected volumes with host " + hostName + " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
		String hostIdentity =accountName+"@"+hostName;
		PureHost host = CLIENT.hosts().get(hostName);
        context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY, connVol);
    	actionlogger.addInfo("Volume Identities as Output is saved");
    	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_HOST_IDENTITY, hostIdentity);
    	actionlogger.addInfo("Host Identity as Output is saved");

        ObjStore<HostInventoryConfig> store2 = ObjStoreHelper.getStore(HostInventoryConfig.class);
 	   
        String query3 = "id == '" + accountName+"@"+hostName + "'";
        List<HostInventoryConfig> hostConfig = store2.query(query3);
        actionlogger.addInfo("Hosts Id :"+ hostConfig.get(0).getId());
        List<PureHostConnection> privateConnections = CLIENT.hosts().getPrivateConnections(host.getName());
        List<PureHostConnection> sharedConnections = CLIENT.hosts().getSharedConnections(host.getName());
        // private and shared connections cannot overlap (i.e. same vol cannot be part of both shared and private connections)
        hostConfig.get(0).setVolumes(privateConnections.size() + sharedConnections.size()); // Number of volumes
        int noOfVol = privateConnections.size() + sharedConnections.size();
        long totalSize = 0;
        String connVolumes="";
        for (PureHostConnection connection: privateConnections)
        {
            totalSize += CLIENT.volumes().get(connection.getVolumeName()).getSize();
            if(connVolumes=="")
            {
            	connVolumes=connection.getVolumeName();	
            }
            else
            {
            connVolumes=connVolumes+","+connection.getVolumeName();
            }
        }
        for (PureHostConnection connection: sharedConnections)
        {
            totalSize += CLIENT.volumes().get(connection.getVolumeName()).getSize();
            if(connVolumes=="")
            {
            	connVolumes=connection.getVolumeName();	
            }
            else
            {
            connVolumes=connVolumes+","+connection.getVolumeName();
            }
        }
        hostConfig.get(0).setConnectedVolumes(connVolumes);
        hostConfig.get(0).setProvisionedSize(totalSize/(1024*1024*1024)); 
        double size = totalSize/(1024*1024*1024);
        
        if(noOfVol == 0)
        {
        	store2.modifySingleObject("volumes == '" + noOfVol + "' && connectedVolumes == '" + connVolumes + "'"+" && provisionedSize == " + size ,  hostConfig.get(0));
            	
        }
        else
        {
        store2.modifySingleObject("volumes == " + noOfVol + " && connectedVolumes == '" + connVolumes + "'"+" && provisionedSize == " + size ,  hostConfig.get(0));
        }
        
        String query4 = "id == '" + accountName+"@"+hostName + "'";
        List<HostInventoryConfig> hconfig1 = store2.query(query4);
        actionlogger.addInfo("Connected Volumes are :"+ hconfig1.get(0).getConnectedVolumes());
    	
    
    }

    @Override
    public TaskOutputDefinition[] getTaskOutputDefinitions()
    {
    	TaskOutputDefinition[] ops = new TaskOutputDefinition[2];
   		ops[0] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Volume Identity(s)");

   		ops[1] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_HOST_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Host Identity");
   		return ops;
    }

	@Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new DisconnectVolumeHostTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_DISCONNECT_VOLUMES_WITH_HOST;
	}


}
