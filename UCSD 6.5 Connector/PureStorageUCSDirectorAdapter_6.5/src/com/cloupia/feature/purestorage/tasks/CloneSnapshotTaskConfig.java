package com.cloupia.feature.purestorage.tasks;


import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.feature.purestorage.lovs.FlashArrayAccountsNameProvider;
import com.cloupia.feature.purestorage.lovs.HostTabularProvider;
import com.cloupia.feature.purestorage.lovs.SnapshotTabularProvider;
import com.cloupia.model.cIM.FormFieldDefinition;
import com.cloupia.service.cIM.inframgr.TaskConfigIf;
import com.cloupia.service.cIM.inframgr.customactions.UserInputField;
import com.cloupia.service.cIM.inframgr.customactions.WorkflowInputFieldTypeDeclaration;
import com.cloupia.service.cIM.inframgr.forms.wizard.FormField;


@PersistenceCapable(detachable = "true", table = "psucs_clone_snapshot_task_config")
public class CloneSnapshotTaskConfig implements TaskConfigIf 
{

    @FormField(label = "FlashArray Account", help = "FlashArray Account", mandatory=true, type=FormFieldDefinition.FIELD_TYPE_EMBEDDED_LOV,
            lovProvider = FlashArrayAccountsNameProvider.NAME)
    @UserInputField(type = PureConstants.PURE_FLASHARRAY_ACCOUNT_LOV_NAME)
    @Persistent
    private String accountName;

    
    
    @FormField(label = "Select Snapshot", help = "Use ',' to seperate snapshots name", mandatory = true,multiline = true,type=FormFieldDefinition.FIELD_TYPE_TABULAR_POPUP, table= SnapshotTabularProvider.TABULAR_PROVIDER)
    @UserInputField(type = PureConstants.PURE_SNAPSHOT_LIST_TABLE_NAMES)
    @Persistent
    private String snapshotName;
    
    
    
    
    @FormField(label = "New Volume Name", help = "Letters, numbers, -, and _", mandatory = true )
    @UserInputField(type = WorkflowInputFieldTypeDeclaration.GENERIC_TEXT)
    @Persistent
    private String snapshotPreName;

    
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

    public String getDisplayLabel()
    {
        return PureConstants.TASK_NAME_CLONE_SNAPSHOT_TASK;
    }

    public String getAccountName()
    {
        return accountName;
    }
    public void setAccountName(String accountName)
    {
        this.accountName = accountName;
    }
    
    public String getSnapshotPreName()
    {
        return snapshotPreName;
    }
    public void setSnapshotPreName(String snapshotPreName)
    {
        this.snapshotPreName = snapshotPreName;
    }
    
    public String getSnapshotName()
    {
        return snapshotName;
    }
    public void setSnapshotName(String snapshotName)
    {
        this.snapshotName = snapshotName;
    }

}