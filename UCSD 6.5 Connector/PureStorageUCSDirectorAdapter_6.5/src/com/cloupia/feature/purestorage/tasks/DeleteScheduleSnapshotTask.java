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


public class DeleteScheduleSnapshotTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(DeleteScheduleSnapshotTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	DeleteScheduleSnapshotTaskConfig config = (DeleteScheduleSnapshotTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);


        final String volumeName = config.getVolumeName();
        String protectionGroupName = volumeName + "PGroup";
        Boolean scheduleSnapshotTaskFlag = config.getScheduleSnapshotFlag();
        config.setDeleteScheduleSnapshotFlag(true);

       
        if(!(scheduleSnapshotTaskFlag == null))
        {
            actionlogger.addInfo("This is a rollback task for scheudling snapshot for volume: " + volumeName);
        }

        CLIENT.protectionGroups().disableSnapshot(protectionGroupName);
        actionlogger.addInfo("Disabled schedule snapshot for volume " + volumeName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");

        context.getChangeTracker().undoableResourceModified("AssetType", "idstring", "Destroy Scheduled Snapshot",
                "Scheduled Snapshots has been destoryed" + config.getAccountName(),
                new ScheduleVolumeSnapshotTask().getTaskName(), new ScheduleVolumeSnapshotTaskConfig(config));

        String volIdentity =accountName+"@"+volumeName;
        
        
    	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY, volIdentity);
    	actionlogger.addInfo("Volume Identity as Output is saved"); 
    }

    @Override
    public TaskOutputDefinition[] getTaskOutputDefinitions()
    {
    	TaskOutputDefinition[] ops = new TaskOutputDefinition[1];
   		

   		ops[0] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Volume Identity");
   		return ops;
    }

	@Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new DeleteScheduleSnapshotTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_DELETE_SCHEDULE_SNAPSHOT;
	}

}