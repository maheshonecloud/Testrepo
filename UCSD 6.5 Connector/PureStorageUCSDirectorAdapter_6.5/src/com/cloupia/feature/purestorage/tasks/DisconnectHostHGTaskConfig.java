package com.cloupia.feature.purestorage.tasks;


import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.feature.purestorage.lovs.FlashArrayAccountsNameProvider;
import com.cloupia.feature.purestorage.lovs.HostGroupTabularProvider;
import com.cloupia.feature.purestorage.lovs.HostTabularProvider;
import com.cloupia.model.cIM.FormFieldDefinition;
import com.cloupia.service.cIM.inframgr.TaskConfigIf;
import com.cloupia.service.cIM.inframgr.customactions.UserInputField;
import com.cloupia.service.cIM.inframgr.customactions.WorkflowInputFieldTypeDeclaration;
import com.cloupia.service.cIM.inframgr.forms.wizard.FormField;


@PersistenceCapable(detachable = "true", table = "psucs_disconnect_host_hg_task_config")
public class DisconnectHostHGTaskConfig implements TaskConfigIf 
{

	@FormField(label = "FlashArray Account", help = "FlashArray Account", mandatory=true, type=FormFieldDefinition.FIELD_TYPE_EMBEDDED_LOV,
            lovProvider = FlashArrayAccountsNameProvider.NAME)
    @UserInputField(type = PureConstants.PURE_FLASHARRAY_ACCOUNT_LOV_NAME)
    @Persistent
    private String accountName;

    @FormField(label = "Host Name", help = "Use ',' to seperate hosts name", mandatory = true,multiline = true,type=FormFieldDefinition.FIELD_TYPE_TABULAR_POPUP, table= HostTabularProvider.TABULAR_PROVIDER)
    @UserInputField(type = PureConstants.PURE_HOST_LIST_TABLE_NAMES)
    @Persistent
    private String hostName;

    @FormField(label = "HostGroup Name", help = "FlashArray HostGroup Name", mandatory = true,type=FormFieldDefinition.FIELD_TYPE_TABULAR_POPUP, table= HostGroupTabularProvider.TABULAR_PROVIDER)
    @UserInputField(type = PureConstants.PURE_HOSTGROUP_NAME)
    @Persistent
    private String hostGroupName;

    @Persistent
    private long configEntryId;

    @Persistent
    private long actionId;


    @Override
    public long getActionId(){ return actionId;}

    @Override
    public long getConfigEntryId(){return configEntryId;}

    public DisconnectHostHGTaskConfig(ConnectHostToHGTaskConfig config)
    {
        this.accountName = config.getAccountName();
        this.hostGroupName = config.getHostGroupName();
        this.hostName = config.getHostName();
        
    }
    public DisconnectHostHGTaskConfig()
    {
        
    }

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
        return PureConstants.TASK_NAME_DISCONNECT_HOSTS_WITH_HOSTGROUP;
    }

    
    
    public String getAccountName()
    {
        return accountName;
    }
    
    public void setAccountName(String accountName)
    {
        this.accountName = accountName;
    }
    
    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public String getHostName()
    {
        return hostName;
    }

    
    public void setHostGroupName(String hostGroupName)
    {
        this.hostGroupName = hostGroupName;
    }
    public String getHostGroupName()
    {
        return hostGroupName;
    }

}