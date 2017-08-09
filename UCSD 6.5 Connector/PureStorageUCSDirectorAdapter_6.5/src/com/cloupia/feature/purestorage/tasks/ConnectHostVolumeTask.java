package com.cloupia.feature.purestorage.tasks;


import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.host.PureHost;
import com.purestorage.rest.host.PureHostConnection;
import com.cisco.cuic.api.client.WorkflowInputFieldTypeDeclaration;
import com.cloupia.feature.purestorage.PureUtils;
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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


public class ConnectHostVolumeTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(ConnectHostVolumeTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	ConnectHostVolumeTaskConfig config = (ConnectHostVolumeTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);
        

        String allVolumeName = config.getVolumeName();
        String hostName = config.getHostName();
        boolean isLun = config.getIsStatusChange();
        String lunIds = config.getAllLunId();
        if(lunIds==null)
        {
        	lunIds="";
        }
        String[] lunIdList = lunIds.split(",");
        String[] volumeNameList = allVolumeName.split(",");
        actionlogger.addInfo("Starting connecting volume(s) to host " + hostName +
                " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");

        

        List<PureHostConnection> connectedVolume = CLIENT.hosts().getConnections(hostName);
        List<String> connectedVolName = new ArrayList<String>();


        for(PureHostConnection conn : connectedVolume)
        {
            connectedVolName.add(conn.getVolumeName());
        }
        String connVol="";
        for(int i=0; i<volumeNameList.length; i++)
        {
            String volumeName = volumeNameList[i];
            if(connectedVolName.contains(volumeName))
            {
                actionlogger.addError(volumeName + " has already been connected to host " + hostName);
                continue;
            }
            if(isLun==true && lunIds!="" && lunIdList.length>i )
            {
                
            	int lunId=Integer.parseInt(lunIdList[i]);
            	CLIENT.hosts().connectVolume(hostName, volumeName,lunId);
               
            }
            else
            {
            CLIENT.hosts().connectVolume(hostName, volumeName);
            }
            if(connVol=="")
            {
            	connVol=accountName+"@"+volumeName;	
            }
            else
            {
            	connVol=connVol+","+accountName+"@"+volumeName;
            }
        }

        actionlogger.addInfo("Successfully Connected volumes to host " + hostName + " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
        context.getChangeTracker().undoableResourceModified("AssetType", "idstring", "ConnectVolumesToHost",
                "Volumes has been connected to host " + config.getAccountName(),
                new DisconnectVolumeHostTask().getTaskName(), new DisconnectVolumeHostTaskConfig(config));
String hostIdentity =accountName+"@"+hostName;
        
        context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY, connVol);
    	actionlogger.addInfo("Volume Identities as Output is saved");
    	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_HOST_IDENTITY, hostIdentity);
    	actionlogger.addInfo("Host Identity as Output is saved");
    	PureHost host = CLIENT.hosts().get(hostName);
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
		return new ConnectHostVolumeTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_CONNECT_HOST_VOLUME;
	}

}
