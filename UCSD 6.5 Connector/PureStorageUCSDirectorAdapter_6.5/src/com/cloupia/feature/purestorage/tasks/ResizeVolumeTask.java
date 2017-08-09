package com.cloupia.feature.purestorage.tasks;


import java.util.List;

import org.apache.log4j.Logger;

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


public class ResizeVolumeTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(ResizeVolumeTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	ResizeVolumeTaskConfig config = (ResizeVolumeTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);

        String volumeName = config.getVolumeName();
        String volumeSizeNumber = config.getVolumeSizeNumber();
        String vSU=config.getVolumeSizeUnit();
        int count = 0;
        if(vSU.equals("KB")) count = 1;
        if(vSU.equals("MB")) count = 2;
        if(vSU.equals("GB")) count = 3;
        if(vSU.equals("TB")) count = 4;
        if(vSU.equals("PB")) count = 5;
        
        double volumeSizeUnit = Math.pow(1024, count);
        long resetSize = (new Double(volumeSizeUnit)).longValue() * Long.parseLong(volumeSizeNumber);
        Boolean truncate = config.getTruncate();
        long originSize = CLIENT.volumes().get(volumeName).getSize();
        config.setOriginSize(originSize);
        logger.info(" Original Size : "+config.getOriginSize());
    
        if(truncate == null)
        {
        truncate = false;
        }
        
       
        actionlogger.addInfo("Resizing volume " + volumeName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
        CLIENT.volumes().resize(volumeName, resetSize, truncate);
        context.saveOutputValue(PureConstants.TASK_TYPE_VOLUME, volumeName);
        
        
        
        String volIdentity =accountName+"@"+volumeName;
        
        
    	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY, volIdentity);
    	actionlogger.addInfo("Volume Identity as Output is saved"); 
    	
    	String description="FlashArray Volume is Modified. Details are : Account Name = "+config.getAccountName()+" , Volume Name = "+ volumeName+" , Volume Size = "+volumeSizeNumber+volumeSizeUnit;
        
        UcsdCmdbUtils.updateRecord("FlashArray Volume", description, 0, context.getUserId(), volumeName,description);
        
       context.getChangeTracker().resourceAdded("FlashArray Volume : Modified", volIdentity, volumeName, description);
        if(truncate)
        {
            context.getChangeTracker().undoableResourceDeleted("FlashArray Volume", volIdentity, volumeName, description,
                    new RollbackResizeVolumeTask().getTaskName(), new RollbackResizeVolumeTaskConfig(config));
        }
        
        ObjStore<VolumeInventoryConfig> store2 = ObjStoreHelper.getStore(VolumeInventoryConfig.class);
  	   
        String query3 = "id == '" + accountName+"@"+volumeName + "'";
        List<VolumeInventoryConfig> hconfig = store2.query(query3);
        actionlogger.addInfo("Volume Id :"+ hconfig.get(0).getId());
        hconfig.get(0).setSize(resetSize);
        
        store2.modifySingleObject("size == " + resetSize ,  hconfig.get(0));
        String query4 = "id == '" + accountName+"@"+volumeName + "'";
        List<VolumeInventoryConfig> hconfig1 = store2.query(query4);
        actionlogger.addInfo("Volume Size :"+ hconfig1.get(0).getSize());
    }

    @Override
    public TaskOutputDefinition[] getTaskOutputDefinitions()
    {
    	TaskOutputDefinition[] ops = new TaskOutputDefinition[2];
   		

   		ops[0] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Volume Identity");
        ops[1] = new TaskOutputDefinition(
                PureConstants.TASK_OUTPUT_NAME_RESIZE_VOLUME,
                WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
                "Volume Resized");
        return ops;
    }

	@Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new ResizeVolumeTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_RESIZE_VOLUME;
	}

}
