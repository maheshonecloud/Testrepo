package com.cloupia.feature.purestorage.tasks;


import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.feature.purestorage.lovs.FlashArrayAccountsNameProvider;
import com.cloupia.feature.purestorage.lovs.HostTabularProvider;
import com.cloupia.model.cIM.FormFieldDefinition;
import com.cloupia.service.cIM.inframgr.TaskConfigIf;
import com.cloupia.service.cIM.inframgr.customactions.UserInputField;
import com.cloupia.service.cIM.inframgr.forms.wizard.FormField;


@PersistenceCapable(detachable = "true", table = "psucs_delete_host_task_config")
public class DeleteHostTaskConfig implements TaskConfigIf
{

    @FormField(label = "FlashArray Account", help = "FlashArray Account", mandatory=true, type=FormFieldDefinition.FIELD_TYPE_EMBEDDED_LOV,
            lovProvider = FlashArrayAccountsNameProvider.NAME)
    @UserInputField(type = PureConstants.PURE_FLASHARRAY_ACCOUNT_LOV_NAME)
    @Persistent
    private String accountName;

    @FormField(label = "Host Name", help = "Use ',' to seperate hosts name", mandatory = true,type=FormFieldDefinition.FIELD_TYPE_TABULAR_POPUP, table= HostTabularProvider.TABULAR_PROVIDER)
    @UserInputField(type = PureConstants.PURE_HOST_LIST_TABLE_NAME)
    @Persistent
    private String hostName;

    private String wwns;

    private String iqns;

    private String privateVolumes;

    private String hostGroupName;

    private Boolean deleteHostFlag;

    private Boolean newHostFlag;

    private Boolean existHost;

    public DeleteHostTaskConfig() {}

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
    
    
    public DeleteHostTaskConfig(NewHostTaskConfig config)
    {
        this.accountName = config.getAccountName();
        this.hostName = config.getHostName();
        this.newHostFlag = config.getNewHostFlage();
        this.existHost = config.getExistHost();
    }

    public String getDisplayLabel()
    {
        return PureConstants.TASK_NAME_DELETE_HOST;
    }

    public String getAccountName()
    {
        return accountName;
    }
    public void setAccountName(String accountName)
    {
        this.accountName = accountName;
    }
    public String getHostName()
    {
        return hostName;
    }
    public void setHostName(String hostName){
        this.hostName = hostName;
    }

    
    public void setWwns(String wwns){
        this.wwns = wwns;
    }

    public String getWwns()
    {
        return wwns;
    }

    public void setIqns(String iqns)
    {
        this.iqns = iqns;
    }

    public String getIqns()
    {return iqns;}

    public void setPrivateVolumes(String privateVolumes)
    {
        this.privateVolumes = privateVolumes;
    }

    public String getPrivateVolumes()
    {
        return privateVolumes;
    }

    public void setHostGroupName(String hostGroupName)
    {
        this.hostGroupName = hostGroupName;
    }

    public String getHostGroupName()
    {
        return hostGroupName;
    }

    public void setDeleteHostFlag(Boolean deleteHostFlag)
    {
        this.deleteHostFlag = deleteHostFlag;
    }

    public Boolean getDeleteHostFlag()
    {
        return deleteHostFlag;
    }

    public Boolean getNewHostFlag()
    {
        return newHostFlag;
    }

    public void setExistHost(Boolean existHost)
    {
        this.existHost = existHost;
    }
    public Boolean getExistHost()
    {
        return existHost;
    }

}
