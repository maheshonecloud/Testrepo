package com.cloupia.feature.purestorage.tasks;


import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.cloupia.feature.purestorage.lovs.FlashArrayAccountsNameProvider;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.feature.purestorage.lovs.TimeClockProvider;
import com.cloupia.feature.purestorage.lovs.TimeUnitProvider;
import com.cloupia.feature.purestorage.lovs.VolumeTabularProvider;
import com.cloupia.model.cIM.FormFieldDefinition;
import com.cloupia.service.cIM.inframgr.TaskConfigIf;
import com.cloupia.service.cIM.inframgr.customactions.UserInputField;
import com.cloupia.service.cIM.inframgr.customactions.WorkflowInputFieldTypeDeclaration;
import com.cloupia.service.cIM.inframgr.forms.wizard.FormField;


@PersistenceCapable(detachable = "true", table = "psucs_add_a_volume_protection_config")
public class AddVolumeProtectionGroupTaskConfig implements TaskConfigIf
{

    @FormField(label = "FlashArray Account", help = "FlashArray Account", mandatory = true, type = FormFieldDefinition.FIELD_TYPE_EMBEDDED_LOV,
            lovProvider = FlashArrayAccountsNameProvider.NAME)
    @UserInputField(type = PureConstants.PURE_FLASHARRAY_ACCOUNT_LOV_NAME)
    @Persistent
    private String accountName;

    @FormField(label = "Volume Name", help = "Use ',' to seperate volumes name", mandatory = true,type=FormFieldDefinition.FIELD_TYPE_TABULAR_POPUP, table= VolumeTabularProvider.TABULAR_PROVIDER)
    @UserInputField(type = PureConstants.PURE_VOLUME_LIST_TABLE_NAME)
    @Persistent
    private String volumeName;

   

    @FormField(label="Protection Group Name",help="Provide the Protection Group Name", mandatory=true)
    @UserInputField(type=WorkflowInputFieldTypeDeclaration.GENERIC_TEXT)
    @Persistent
    private String protectionGroup;

        
    public String getProtectionGroup() {
		return protectionGroup;
	}

	public void setProtectionGroup(String protectionGroup) {
		this.protectionGroup = protectionGroup;
	}
	@Persistent
    private long configEntryId;

    @Persistent
    private long actionId;


    @Override
    public long getActionId(){ return actionId;}

    @Override
    public long getConfigEntryId(){return configEntryId;}

 

    @Override
	public void setActionId(long arg0) {
		this.actionId = arg0;

	}

	@Override
	public void setConfigEntryId(long arg0) {
		this.configEntryId = arg0;

	}


    public AddVolumeProtectionGroupTaskConfig(){}

    /*public AddVolumeProtectionGroupTaskConfig(DeleteScheduleSnapshotTaskConfig config)
    {
        this.accountName = config.getAccountName();
        this.volumeName = config.getVolumeName();
        //this.deleteScheduleSnapshotFlag = config.getDeleteScheduleSnapshotFlag();
    }*/

    public String getDisplayLabel()
    {
        return PureConstants.TASK_NAME_ADD_VOLUME_PROTECTIONGROUP_TASK;
    }

    public String getAccountName()
    {
        return accountName;
    }
    public void setAccountName(String accountName)
    {
        this.accountName = accountName;
    }
    public String getVolumeName()
    {
        return volumeName;
    }
    public void setVolumeName(String volumeName)
    {
        this.volumeName= volumeName;
    }
   

}