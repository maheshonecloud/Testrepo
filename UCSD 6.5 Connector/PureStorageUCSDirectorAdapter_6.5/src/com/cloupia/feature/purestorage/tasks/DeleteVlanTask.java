package com.cloupia.feature.purestorage.tasks;


import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.host.PureHost;
import com.purestorage.rest.host.PureHostConnection;
import com.purestorage.rest.networking.PureNetworkInterfaceAttributes;
import com.cisco.cuic.api.client.WorkflowInputFieldTypeDeclaration;
import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.UcsdCmdbUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.service.cIM.inframgr.AbstractTask;
import com.cloupia.service.cIM.inframgr.TaskConfigIf;
import com.cloupia.service.cIM.inframgr.TaskOutputDefinition;
import com.cloupia.service.cIM.inframgr.customactions.CustomActionLogger;
import com.cloupia.service.cIM.inframgr.customactions.CustomActionTriggerContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;


public class DeleteVlanTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(DeleteVlanTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
		DeleteVlanTaskConfig config = (DeleteVlanTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);

        String networkInterface = config.getNetworkInterface();
        String vlan = config.getVlan();
        String vlanInterface = networkInterface+"."+vlan;
       
    
              
        //PureNetworkInterfaceAttributes vif = CLIENT.networking().createVlanInterface(vlanInterface, subnet,address);
	     CLIENT.networking().deleteVlanInterface(vlanInterface);
        
            actionlogger.addInfo("Successfully deleted vlan interface " + vlanInterface + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
              
            String vlanIdentity = accountName+"@"+vlanInterface;
        
    	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VLAN_IDENTITY, vlanIdentity);
    	actionlogger.addInfo("Vlan Inteface Identity as Output is saved");
    	
    	//UcsdCmdbUtils cmdb=new UcsdCmdbUtils();
        String description="FlashArray vLan is deleted. Details are : Account Name = "+config.getAccountName()+" , vLan Interface = "+ vlanInterface;
        
        UcsdCmdbUtils.updateRecord("FlashArray vLan", description, 2, context.getUserId(), vlanInterface,description);
        context.getChangeTracker().resourceAdded("FlashArray vLan : Deleted", vlanIdentity, vlanInterface, description);
        
        //context.getChangeTracker().resourceAdded("FlashArray Volume", connVol, volumePreName, description);

         
    
    }

    @Override
    public TaskOutputDefinition[] getTaskOutputDefinitions()
    {
    	TaskOutputDefinition[] ops = new TaskOutputDefinition[1];
   		

   		ops[0] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_VLAN_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Vlan Identity");
   		return ops;
    }

    @Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new DeleteVlanTaskConfig();
	}


	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_DELETE_VLAN_TASK;
	}


    
}
