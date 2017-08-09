package com.cloupia.feature.purestorage.tasks;


import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.feature.purestorage.lovs.FlashArrayAccountsNameProvider;
import com.cloupia.model.cIM.FormFieldDefinition;
import com.cloupia.service.cIM.inframgr.TaskConfigIf;
import com.cloupia.service.cIM.inframgr.customactions.UserInputField;
import com.cloupia.service.cIM.inframgr.customactions.WorkflowInputFieldTypeDeclaration;
import com.cloupia.service.cIM.inframgr.forms.wizard.FormField;


@PersistenceCapable(detachable = "true", table = "psucs_new__pure_host_task_config")
public class NewHostTaskConfig implements TaskConfigIf
{

    @FormField(label = "FlashArray Account", help = "FlashArray Account", mandatory=true, type=FormFieldDefinition.FIELD_TYPE_EMBEDDED_LOV,
            lovProvider = FlashArrayAccountsNameProvider.NAME)
    @UserInputField(type = PureConstants.PURE_FLASHARRAY_ACCOUNT_LOV_NAME)
    @Persistent
    private String accountName;

    @FormField(label = "Host Name", help = "FlashArray Host Name", mandatory = true)
    @UserInputField(type = WorkflowInputFieldTypeDeclaration.GENERIC_TEXT)
    @Persistent
    private String hostName;

    @FormField(label = "Configure Fiber Channel WWPNs", help = "Fiber Channel WWNs for Host. Use ',' to seperate WWN", mandatory = false)
    @UserInputField(type = WorkflowInputFieldTypeDeclaration.GENERIC_TEXT)
    @Persistent
    private String wwns;

    @FormField(label = "Configure iSCSI IQNs", help = "iSCSI IQNs for Host. Use ',' to seperate WWN", mandatory = false)
    @UserInputField(type = WorkflowInputFieldTypeDeclaration.GENERIC_TEXT)
    @Persistent
    private String iqns;

    private String privateVolumes;

    private String hostGroupName;

    private Boolean deleteHostFlag;

    private Boolean newHostFlag;

    private Boolean existHost;
    
    private int noOfVolume;
    
    private long provisionedSizeVolumes;
    
    private String connectedVolumes;
    
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


    public NewHostTaskConfig(){};

    public NewHostTaskConfig(DeleteHostTaskConfig config)
    {
        this.accountName = config.getAccountName();
        this.hostName = config.getHostName();
        this.privateVolumes = config.getPrivateVolumes();
        this.hostGroupName = config.getHostGroupName();
        this.deleteHostFlag = config.getDeleteHostFlag();
        this.wwns = config.getWwns();
        this.iqns = config.getIqns();
    }

    public String getDisplayLabel()
    {
    	return PureConstants.TASK_NAME_NEW_HOST;
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
    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }
    public String getWwns()
    {
        return wwns;
    }
    public void setWwns(String wwns)
    {
        this.wwns = wwns;
    }
    public String getIqns()
    {
        return iqns;
    }
    public void setIqns(String iqns)
    {
        this.iqns = iqns;
    }
    public String getPrivateVolumes()
    {
        return privateVolumes;
    }

    public String getHostGroupName()
    {
        return hostGroupName;
    }
    
    public void setHostGroupName(String hostGroupName)
    {
        this.hostGroupName = hostGroupName;
    }


    public Boolean getDeleteHostFlag()
    {
        return deleteHostFlag;
    }

    public void setNewHostFlag(boolean newHostFlag)
    {
        this.newHostFlag = newHostFlag;
    }

    public Boolean getNewHostFlage()
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
    
    public int getNoOfVolume()
    {
        return noOfVolume;
    }
    
    public void setNoOfVolume(int noOfVolume)
    {
        this.noOfVolume = noOfVolume;
    }
    
    public long getProvisionedSizeVolumes()
    {
        return provisionedSizeVolumes;
    }
    
    public void setProvisionedSizeVolumes(long provisionedSizeVolumes)
    {
        this.provisionedSizeVolumes = provisionedSizeVolumes;
    }
    
    public String getConnectedVolumes()
    {
        return connectedVolumes;
    }
    
    public void setConnectedVolumes(String connectedVolumes)
    {
        this.connectedVolumes = connectedVolumes;
    }
   

}