package com.cloupia.feature.purestorage.tasks;


import com.cisco.cuic.api.client.WorkflowInputFieldTypeDeclaration;
import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.UcsdCmdbUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
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
import com.purestorage.rest.exceptions.PureException;
import com.purestorage.rest.volume.PureVolume;
import com.purestorage.rest.volume.PureVolumeConnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;


public class DestroyVolumesTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(DestroyVolumesTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	DestroyVolumesTaskConfig config = (DestroyVolumesTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);


        String volumePreName = config.getVolumePreName();
        String startNumber = config.getStartNumber();
        String endNumber = config.getEndNumber();
        Boolean eradicate = config.getEradicate();
        String noRollBackVolumeName = config.getNoRollBackVolumeName();
        List<String> noRollBackVolumeList = new ArrayList<String>();
        Boolean newVolumeTaskFlag = config.getNewVolumeTaskFlag();
        List<String> volumeNameList = new ArrayList<String>();
        StringBuilder hostConnection = new StringBuilder();
        StringBuilder hostGroupConnection = new StringBuilder();

        if(startNumber == null )
        {
        	startNumber="";
        	
        }
        if( endNumber == null)
        {
        	
        	endNumber="";
        }
        if (eradicate == null)
        {
        	eradicate = false;
        }
        
        
        
        if(startNumber.equals("") && endNumber.equals(""))
        {
            String volumeName = volumePreName;
            volumeNameList.add(volumeName);
        }
        else
        {
            if(startNumber.equals("")) startNumber = endNumber;
            if(endNumber.equals("") ) endNumber = startNumber;

            for(int i = Integer.parseInt(startNumber);i <= Integer.parseInt(endNumber);i++)
            {
                String volumeName = volumePreName + Integer.toString(i);
                volumeNameList.add(volumeName);
            }
        }

        for(String volumeName : volumeNameList)
        {
            try
            {
                List<PureVolumeConnection> connectedHost = CLIENT.volumes().getHostConnections(volumeName);
                List<PureVolumeConnection> connectedHostGroup = CLIENT.volumes().getHostGroupConnections(volumeName);
                if(connectedHost.size()>0)
                {
                    hostConnection.append(volumeName + ":");
                    for(PureVolumeConnection host : connectedHost)
                    {
                        hostConnection.append(host.getHost() + ",");
                    }
                    hostConnection.append("!");
                }
                if(connectedHostGroup.size()>0)
                {
                    hostGroupConnection.append(volumeName + ":");
                    for(PureVolumeConnection hostGroup : connectedHostGroup)
                    {
                        hostGroupConnection.append(hostGroup.getHostGroupName() + ",");
                    }
                    hostGroupConnection.append("!");
                }
            }
            catch (PureException e)
            {
                actionlogger.addInfo("Error happens when trying to get host connection and host group connection with volume "
                + volumeName + e.getMessage());
            }
        }

        config.setDestroyVolumeTaskFlag(true);
        config.setHostConnection(hostConnection.toString());
        config.setHostGroupConnection(hostGroupConnection.toString());
        

        actionlogger.addInfo("starting destroying volume(s)");

        if(newVolumeTaskFlag != null)
        {
            actionlogger.addInfo("This is a rollback task for the task of creating new volumes");
            noRollBackVolumeList = Arrays.asList(noRollBackVolumeName.split(","));
        }
        String connVol="";
        for(String volumeName : volumeNameList)
        {
            try
            {
                if(noRollBackVolumeList == null || !noRollBackVolumeList.contains(volumeName))
                {
                    destroyVolume(volumeName, CLIENT, eradicate);
                    actionlogger.addInfo("Destroying Volume : " + volumeName);
                    
                    ObjStore<VolumeInventoryConfig> store2 = ObjStoreHelper.getStore(VolumeInventoryConfig.class);
               	   
                    String query3 = "id == '" + accountName+"@"+volumeName + "'";
                    List<VolumeInventoryConfig> hconfig = store2.query(query3);
                    actionlogger.addInfo("Volume Id :"+ hconfig.get(0).getId());
                
                    long s = store2.delete(query3);
                    actionlogger.addInfo("Deleted in Inventory :" + s);
                }
            }
            catch(PureException e)
            {
                actionlogger.addError("Error happens while destroying volume: " + volumeName + " Exception: "+ e.getMessage());
                throw e;
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

        actionlogger.addInfo("successfully destroying volumes " + volumePreName + " with range " + startNumber + "-" + endNumber +
                " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
        context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY, connVol);
    	actionlogger.addInfo("Volume Identities as Output is saved");
    	
    	String description="FlashArray Volume is Deleted. Details are : Account Name = "+config.getAccountName()+" , Volume Name = "+ volumePreName+" , Start Number = "+startNumber+" , End Number = "+endNumber;
        
        UcsdCmdbUtils.updateRecord("FlashArray Volume", description, 2, context.getUserId(), volumePreName,description);
        
        context.getChangeTracker().resourceAdded("FlashArray Volume : Deleted", connVol, volumePreName, description);
        context.getChangeTracker().undoableResourceDeleted("FlashArray Volume", connVol, volumePreName, description,
                new NewVolumeTask().getTaskName(), new NewVolumeTaskConfig(config));
    }

    public void destroyVolume(String volumeName, PureRestClient CLIENT, Boolean eradicate)
    {
        List<PureVolumeConnection> connectedHostGroup = CLIENT.volumes().getHostGroupConnections(volumeName);
        List<PureVolumeConnection> connectedHost = CLIENT.volumes().getHostConnections(volumeName);
        List<String> hostGroupList = new ArrayList<String>();

        for(PureVolumeConnection hostGroupVolumeConnection : connectedHostGroup)
        {
            String hostGroupName = hostGroupVolumeConnection.getHostGroupName();
            if(!hostGroupList.contains(hostGroupName))
            {
                hostGroupList.add(hostGroupName);
            }
        }

        for(String hgName : hostGroupList)
        {
            CLIENT.hostGroups().disconnectVolume(hgName,volumeName);
        }

        for(PureVolumeConnection hostVolumeConnection : connectedHost)
        {
            String hostName = hostVolumeConnection.getHost();
            CLIENT.hosts().disconnectVolume(hostName,volumeName);
        }

        CLIENT.volumes().destroy(volumeName);
        if(eradicate==true)
        {
            CLIENT.volumes().destroy(volumeName, eradicate);
        }
    }
    @Override
    public TaskOutputDefinition[] getTaskOutputDefinitions()
    {
    	TaskOutputDefinition[] ops = new TaskOutputDefinition[1];
   		

   		ops[0] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Volume Identity(s)");
   		return ops;
    }

	@Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new DestroyVolumesTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_DESTROY_VOLUMES_SUFFIX_RANGE;
	}
}
