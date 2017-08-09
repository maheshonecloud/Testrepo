package com.cloupia.feature.purestorage.tasks;


import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.host.PureHost;
import com.purestorage.rest.host.PureHostConnection;
import com.purestorage.rest.networking.PureNetworkInterfaceAttributes;

import com.purestorage.rest.volume.PureVolume;
import com.cisco.cuic.api.client.WorkflowInputFieldTypeDeclaration;
import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.UcsdCmdbUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.SnapshotInventoryConfig;
import com.cloupia.feature.purestorage.accounts.VolumeInventoryConfig;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.fw.objstore.ObjStore;
import com.cloupia.fw.objstore.ObjStoreHelper;
import com.cloupia.service.cIM.inframgr.AbstractTask;
import com.cloupia.service.cIM.inframgr.TaskConfigIf;
import com.cloupia.service.cIM.inframgr.TaskOutputDefinition;
import com.cloupia.service.cIM.inframgr.customactions.CustomActionLogger;
import com.cloupia.service.cIM.inframgr.customactions.CustomActionTriggerContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;


public class CloneSnapshotTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(CloneSnapshotTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
		CloneSnapshotTaskConfig config = (CloneSnapshotTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);
        
        String snapshotPreName = config.getSnapshotPreName();
        String snapshotName = config.getSnapshotName();

        actionlogger.addInfo("Snapshot Name is : "+snapshotName+" and Cloned Snapshot Name is : "+snapshotPreName);
    	logger.info("Snapshot Name is : "+snapshotName+" and Cloned Snapshot Name is : "+snapshotPreName);
    	CLIENT.volumes().create(snapshotPreName, snapshotName);
    	
    	
    	 PureVolume volume =  CLIENT.volumes().get(snapshotPreName);
      
        	
        	String connVol=accountName+"@"+snapshotName;	
        	
        	
         	String outVolName=volume.getName();
         	String outSer=volume.getSerial();
         	
         	
         	 ObjStore<VolumeInventoryConfig> store = ObjStoreHelper.getStore(VolumeInventoryConfig.class);
       	   
              
              	VolumeInventoryConfig volumeConfig = new VolumeInventoryConfig();
              	volumeConfig.setId(accountName+"@"+volume.getName());
              	volumeConfig.setAccountName(accountName);
              	volumeConfig.setVolumeName(volume.getName());
              	volumeConfig.setSize(volume.getSize()/(1024*1024*1024)); // Name
                  volumeConfig.setSource(volume.getSource()); //HostGroup
                  volumeConfig.setCreation(volume.getCreatedTimestamp().getTime());
                       // ObjStore<NewHostTaskConfig> store = ObjStoreHelper.getStore(NewHostTaskConfig.class);
                  store.insert(volumeConfig);
              
             
                  actionlogger.addInfo("Successfully Cloned Volume " + snapshotPreName + " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
                  context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY, connVol);
              	actionlogger.addInfo("Volume Identities as Output is saved");
                  	
                  	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_NAME, outVolName);
                  	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_NAMES, outVolName);
                  	actionlogger.addInfo("Volume Name as Output is saved");
                  	
                      
                  	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_SERIAL, outSer);
                  	actionlogger.addInfo("The Serial Number of this volume is "+outSer);
                  	actionlogger.addInfo("Serial number as Output is saved");
                  
                  	//UcsdCmdbUtils cmdb=new UcsdCmdbUtils();
                      String description="FlashArray Volume is created. Details are : Account Name = "+config.getAccountName()+" , New Volume Name = "+ snapshotPreName+" , Source Volume Name = "+snapshotName;
                      
                      UcsdCmdbUtils.updateRecord("FlashArray Volume", description, 1, context.getUserId(), snapshotPreName,description);
                      
                      context.getChangeTracker().resourceAdded("FlashArray Volume", connVol, snapshotPreName, description);

                      // context.getChangeTracker().undoableResourceAdded("FlashArray Volume : Created", connVol, snapshotPreName,
                      //   description,
                       //  new DestroyVolumesTask().getTaskName(), new DestroyVolumesTaskConfig(config));
          
          }

          @Override
          public TaskOutputDefinition[] getTaskOutputDefinitions()
          {
          	TaskOutputDefinition[] ops = new TaskOutputDefinition[4];
         		ops[0] = new TaskOutputDefinition(
         				PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY,
         				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
         				"Volume Identity(s)");
         		
         		ops[1] = new TaskOutputDefinition(
         				PureConstants.TASK_OUTPUT_NAME_VOLUME_NAMES,
         				PureConstants.PURE_VOLUME_LIST_TABLE_NAMES,
         				"Volume Name(s)");
         		ops[2] = new TaskOutputDefinition(
         				PureConstants.TASK_OUTPUT_NAME_VOLUME_NAME,
         				PureConstants.PURE_VOLUME_LIST_TABLE_NAME,
         				"Volume Name");

         		ops[3] = new TaskOutputDefinition(
         				PureConstants.TASK_OUTPUT_NAME_VOLUME_SERIAL,
         				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
         				"Serial number(s) of volume(s)");
         		
   		
   		return ops;

    }

	@Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new CloneSnapshotTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_CLONE_SNAPSHOT_TASK;
	}


    
}
