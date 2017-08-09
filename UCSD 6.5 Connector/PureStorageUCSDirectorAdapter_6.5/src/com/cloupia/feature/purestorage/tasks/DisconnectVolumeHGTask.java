package com.cloupia.feature.purestorage.tasks;


import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.exceptions.PureException;
import com.purestorage.rest.hostgroup.PureHostGroup;
import com.purestorage.rest.hostgroup.PureHostGroupConnection;

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


public class DisconnectVolumeHGTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(DisconnectVolumeHGTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	DisconnectVolumeHGTaskConfig config = (DisconnectVolumeHGTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);


        String allVolumeName = config.getVolumeName();
        String hostGroupName = config.getHostGroupName();
        String[] volumeNameList = allVolumeName.split(",");
        String testFlag = config.getTestFlag();

        actionlogger.addInfo("get test Flag: " + testFlag);

        String connVol="";
        for(int i=0; i<volumeNameList.length; i++)
        {
            String volumeName = volumeNameList[i];
            try
            {
                CLIENT.hostGroups().disconnectVolume(hostGroupName, volumeName);
            }
            catch (PureException e)
            {
                actionlogger.addError("Error happens when disconnecting " + volumeName + " with host group " + hostGroupName );
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


        actionlogger.addInfo("Successfully Disconnected volume(s) to hostgroup " + hostGroupName + " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
        String hostGroupIdentity =accountName+"@"+hostGroupName;
        
        context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY, connVol);
    	actionlogger.addInfo("Volume Identities as Output is saved");
    	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_HOSTGROUP_IDENTITY, hostGroupIdentity);
    	actionlogger.addInfo("Host Group Identity as Output is saved");
    	
    	
    	PureHostGroup hostgroup =CLIENT.hostGroups().get(hostGroupName);
    	ObjStore<HostGroupInventoryConfig> store = ObjStoreHelper.getStore(HostGroupInventoryConfig.class);
    	String query = "id == '" + hostGroupIdentity + "'";
    	List<HostGroupInventoryConfig> hostGroupConfig = store.query(query);
    	 
    	
    	List<PureHostGroupConnection> connections = CLIENT.hostGroups().getConnections(hostgroup.getName());
        
        // private and shared connections cannot overlap (i.e. same vol cannot be part of both shared and private connections)
        hostGroupConfig.get(0).setVolumes(connections.size()); // Number of volumes
        long totalSize = 0;
        String connVolumes="";
        for (PureHostGroupConnection connection: connections)
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
        hostGroupConfig.get(0).setConnectedVolumes(connVolumes);
        hostGroupConfig.get(0).setProvisionedSize(totalSize/(1024*1024*1024)); // Provisioned size of attached volumes
        hostGroupConfig.get(0).setVolumeCapacity(CLIENT.hostGroups().getSpaceMetrics(hostgroup.getName()).getVolumes()/(1024*1024*1024)); // Provisioned size of attached volumes
        hostGroupConfig.get(0).setReduction(CLIENT.hostGroups().getSpaceMetrics(hostgroup.getName()).getDataReduction()); // Provisioned size of attached volumes
        double size = totalSize/(1024*1024*1024);
        int noOfVol = connections.size();
        double volCapacity = CLIENT.hostGroups().getSpaceMetrics(hostgroup.getName()).getVolumes()/(1024*1024*1024);
        double reduction = CLIENT.hostGroups().getSpaceMetrics(hostgroup.getName()).getDataReduction();
    	
    	 
        
        
        actionlogger.addInfo("Hosts Id :"+ hostGroupConfig.get(0).getId()+"  (Query) volumes == " + noOfVol + " && connectedVolumes == '" + connVolumes + "'"+" && provisionedSize == " + size + " && volumeCapacity == " + volCapacity +" && reduction == " + reduction);
        if (noOfVol == 0)
        {
        	store.modifySingleObject("volumes == '" + noOfVol + "' && connectedVolumes == '" + connVolumes + "'"+" && provisionedSize == " + size + " && volumeCapacity == " + volCapacity +" && reduction == " + reduction ,  hostGroupConfig.get(0));
            	
        }
        else
        {
        	store.modifySingleObject("volumes == " + noOfVol + " && connectedVolumes == '" + connVolumes + "'"+" && provisionedSize == " + size + " && volumeCapacity == " + volCapacity +" && reduction == " + reduction ,  hostGroupConfig.get(0));
            
        }
        String query1 = "id == '" + hostGroupIdentity + "'";
        List<HostGroupInventoryConfig> hgconfig1 = store.query(query1);
        actionlogger.addInfo("Volumes List :"+ hgconfig1.get(0).getVolumes()+ "  "+hgconfig1.get(0).getConnectedVolumes() );
        
    
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
   				PureConstants.TASK_OUTPUT_NAME_HOSTGROUP_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Host Group Identity");
   		return ops;
    }

	@Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new DisconnectVolumeHGTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_DISCONNECT_VOLUMES_WITH_HOSTGROUP;
	}



}
