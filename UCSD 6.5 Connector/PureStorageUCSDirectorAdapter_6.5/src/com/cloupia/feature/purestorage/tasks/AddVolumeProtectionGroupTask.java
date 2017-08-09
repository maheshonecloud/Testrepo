package com.cloupia.feature.purestorage.tasks;


import java.util.Arrays;

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


public class AddVolumeProtectionGroupTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(AddVolumeProtectionGroupTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	AddVolumeProtectionGroupTaskConfig config = (AddVolumeProtectionGroupTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking task accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);

        final String volumeName = config.getVolumeName();
        String protectionGroupName = config.getProtectionGroup();
        
            

            try
            {
                CLIENT.protectionGroups().addVolumes(protectionGroupName,Arrays.asList(volumeName));
            }
            catch(Exception e) {
                actionlogger.addError("Please check the Protection Group Name " + protectionGroupName + "and volume Details.");
                throw e;
            }

           
            
             actionlogger.addInfo("Added volume " + volumeName + " in Protection Group on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
        
           context.getChangeTracker().undoableResourceModified("AssetType", "idstring", "Scheduled Snapshot",
                    "Snapshots have been scheduled" + config.getAccountName(),
                    new RemoveVolumeProtectionGroupTask().getTaskName(), new RemoveVolumeProtectionGroupTaskConfig(config));
            String volIdentity =accountName+"@"+volumeName;
            //String snapIdentity =accountName+"@"+snapShotName;
            
            
        	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY, volIdentity);
        	actionlogger.addInfo("Volume Identity as Output is saved");
        }


    
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
		return new AddVolumeProtectionGroupTaskConfig();
	}
	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_ADD_VOLUME_PROTECTIONGROUP_TASK;
	}

}