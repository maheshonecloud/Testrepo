package com.cloupia.feature.purestorage.tasks;


import org.apache.log4j.Logger;

import com.cisco.cuic.api.client.WorkflowInputFieldTypeDeclaration;
import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.service.cIM.inframgr.AbstractTask;
import com.cloupia.service.cIM.inframgr.TaskConfigIf;
import com.cloupia.service.cIM.inframgr.TaskOutputDefinition;
import com.cloupia.service.cIM.inframgr.customactions.CustomActionLogger;
import com.cloupia.service.cIM.inframgr.customactions.CustomActionTriggerContext;
import com.purestorage.rest.PureRestClient;


public class RestoreVolumeTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(RestoreVolumeTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	RestoreVolumeTaskConfig config = (RestoreVolumeTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);

        String volumeName = config.getVolumeName();
        String snapShotName = config.getSnapshotName();

        actionlogger.addInfo("Restoring " + volumeName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
        CLIENT.volumes().create(volumeName, snapShotName, true);
        
        actionlogger.addInfo("Successfully restore volume " + volumeName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
        String volIdentity =accountName+"@"+volumeName;
        String snapIdentity =accountName+"@"+snapShotName;
        
        
    	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY, volIdentity);
    	actionlogger.addInfo("Volume Identity as Output is saved");
    	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_SNAPSHOT_IDENTITY, snapIdentity);
    	actionlogger.addInfo("Snapshot Identity as Output is saved");
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
                PureConstants.TASK_OUTPUT_NAME_SNAPSHOT_IDENTITY,
                WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
                "Snapshot Identity");
        return ops;
    }

	@Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new RestoreVolumeTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_RESTORE_VOLUME_FROM_SNAPSHOT;
	}


   
}
