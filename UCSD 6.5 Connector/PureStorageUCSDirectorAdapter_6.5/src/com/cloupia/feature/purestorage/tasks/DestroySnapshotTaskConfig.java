package com.cloupia.feature.purestorage.tasks;


import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.feature.purestorage.lovs.FlashArrayAccountsNameProvider;
import com.cloupia.feature.purestorage.lovs.SnapshotTabularProvider;
import com.cloupia.model.cIM.FormFieldDefinition;
import com.cloupia.service.cIM.inframgr.TaskConfigIf;
import com.cloupia.service.cIM.inframgr.customactions.UserInputField;
import com.cloupia.service.cIM.inframgr.customactions.WorkflowInputFieldTypeDeclaration;
import com.cloupia.service.cIM.inframgr.forms.wizard.FormField;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;


@PersistenceCapable(detachable = "true", table = "psucs_destroy_snapshot_task_config")
public class DestroySnapshotTaskConfig implements TaskConfigIf{

    @FormField(label = "FlashArray Account", help = "FlashArray Account", mandatory=true, type=FormFieldDefinition.FIELD_TYPE_EMBEDDED_LOV,
            lovProvider = FlashArrayAccountsNameProvider.NAME)
    @UserInputField(type = PureConstants.PURE_FLASHARRAY_ACCOUNT_LOV_NAME)
    @Persistent
    private String accountName;

    @FormField(label = "Select Snapshot", help = "Use ',' to seperate snapshots name", mandatory = true,multiline = true,type=FormFieldDefinition.FIELD_TYPE_TABULAR_POPUP, table= SnapshotTabularProvider.TABULAR_PROVIDER)
    @UserInputField(type = PureConstants.PURE_SNAPSHOT_LIST_TABLE_NAMES)
    @Persistent
    private String snapshotName;

    @FormField(label = "Eradicate", help = "Check box to eradicate volumes", mandatory = false, type = FormFieldDefinition.FIELD_TYPE_BOOLEAN)
    @UserInputField(type = WorkflowInputFieldTypeDeclaration.BOOLEAN)
    @Persistent
    private boolean eradicate;

    

    
    public boolean isEradicate() {
		return eradicate;
	}

	public void setEradicate(boolean eradicate) {
		this.eradicate = eradicate;
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

    public DestroySnapshotTaskConfig(){}

   
    public DestroySnapshotTaskConfig(CloneSnapshotTaskConfig config)
    {
        this.accountName = config.getAccountName();
        this.snapshotName = config.getSnapshotPreName();
        
        
     }
    
    public String getDisplayLabel()
    {
        return PureConstants.TASK_NAME_DESTROY_VOLUMES_SUFFIX_RANGE;
    }

    public String getAccountName()
    {
        return accountName;
    }
    public void setAccountName(String accountName)
    {
        this.accountName = accountName;
    }

	public String getSnapshotName() {
		return snapshotName;
	}

	public void setSnapshotName(String snapshotName) {
		this.snapshotName = snapshotName;
	}
    
}
