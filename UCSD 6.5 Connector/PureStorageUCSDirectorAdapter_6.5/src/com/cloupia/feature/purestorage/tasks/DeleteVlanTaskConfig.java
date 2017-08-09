package com.cloupia.feature.purestorage.tasks;


import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.feature.purestorage.lovs.FlashArrayAccountsNameProvider;
import com.cloupia.feature.purestorage.lovs.HostTabularProvider;
import com.cloupia.model.cIM.FormFieldDefinition;
import com.cloupia.service.cIM.inframgr.TaskConfigIf;
import com.cloupia.service.cIM.inframgr.customactions.UserInputField;
import com.cloupia.service.cIM.inframgr.customactions.WorkflowInputFieldTypeDeclaration;
import com.cloupia.service.cIM.inframgr.forms.wizard.FormField;


@PersistenceCapable(detachable = "true", table = "psucs_delete_vlan_task_config")
public class DeleteVlanTaskConfig implements TaskConfigIf
{

    @FormField(label = "FlashArray Account", help = "FlashArray Account", mandatory=true, type=FormFieldDefinition.FIELD_TYPE_EMBEDDED_LOV,
            lovProvider = FlashArrayAccountsNameProvider.NAME)
    @UserInputField(type = PureConstants.PURE_FLASHARRAY_ACCOUNT_LOV_NAME)
    @Persistent
    private String accountName;

    
    
    @FormField(label = "Network Interface", help = "Network Interface", mandatory = true)
    @UserInputField(type = WorkflowInputFieldTypeDeclaration.GENERIC_TEXT)
    @Persistent
    private String networkInterface;

    @FormField(label = "Vlan Id", help = "Vlan Id", mandatory = true)
    @UserInputField(type = WorkflowInputFieldTypeDeclaration.GENERIC_TEXT)
    @Persistent
    private String vlan;
    
    
    
    @Persistent
    private long configEntryId;

    @Persistent
    private long actionId;

    public DeleteVlanTaskConfig(CreateVlanTaskConfig config)
    {
        this.accountName = config.getAccountName();
        this.vlan= config.getVlan();
        this.networkInterface = config.getNetworkInterface();
        
    }

    public DeleteVlanTaskConfig()
    {
        
        
    }
    
    
    @Override
    public long getActionId(){ return actionId;}

    @Override
    public long getConfigEntryId(){return configEntryId;}

 

    @Override
    public void setActionId(long actionId){ this.actionId = actionId;}

    @Override
    public void setConfigEntryId(long configEntryId){ this.configEntryId = configEntryId;}

    public String getDisplayLabel()
    {
        return PureConstants.TASK_NAME_DELETE_VLAN_TASK;
    }

    public String getAccountName()
    {
        return accountName;
    }
    public void setAccountName(String accountName)
    {
        this.accountName = accountName;
    }
    public String getNetworkInterface()
    {
        return networkInterface;
    }
    public void setNetworkInterface(String networkInterface)
    {
        this.networkInterface = networkInterface;
    }
    public String getVlan()
    {
        return vlan;
    }
    public void setVlan(String vlan)
    {
        this.vlan = vlan;
    }

    


}