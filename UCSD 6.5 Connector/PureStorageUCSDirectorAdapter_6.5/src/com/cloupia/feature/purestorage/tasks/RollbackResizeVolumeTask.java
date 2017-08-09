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
import com.purestorage.rest.exceptions.PureException;


public class RollbackResizeVolumeTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(RollbackResizeVolumeTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	RollbackResizeVolumeTaskConfig config = (RollbackResizeVolumeTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);

        String volumeName = config.getVolumeName();
        Boolean truncate = config.getTruncate();
        long originSize = config.getOriginSize();
        actionlogger.addInfo("Volume Original Size is "+config.getOriginSize());

        actionlogger.addInfo("Rollback Resizing volume " + volumeName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
        if(truncate == null)
        {
        	truncate = false;
        }
        if(truncate)
        {
            try{
                CLIENT.volumes().resize(volumeName, originSize, !truncate);
                String volIdentity =accountName+"@"+volumeName;
                context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY, volIdentity);
            	actionlogger.addInfo("Volume Identity as Output is saved");
            }
            catch (PureException e)
            {
                actionlogger.addInfo("Error happens when rollback ResizeVolumeTask" + "Exception: " + e.getMessage());
            }
        }
        else
        {
            actionlogger.addInfo("This task cannot be rolled back. Because some of data may be irretrievably lost!");
        }
        
    	
    	
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
		return new RollbackResizeVolumeTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_ROLLBACK_RESIZE_VOLUME_TASK;
	}
   


}
