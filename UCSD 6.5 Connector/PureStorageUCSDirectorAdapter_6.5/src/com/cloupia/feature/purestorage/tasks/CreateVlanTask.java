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


public class CreateVlanTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(CreateVlanTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
		CreateVlanTaskConfig config = (CreateVlanTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);

        String networkInterface = config.getNetworkInterface();
        String vlan = config.getVlan();
        String vlanInterface = networkInterface+"."+vlan;
        
        String subnet = config.getSubnet();
        String address = config.getAddress();
    
        PureNetworkInterfaceAttributes vif;
        if (address == null)
        {
       address = "";	
        }
        
        if (address=="" ||address.isEmpty())
        {
        	vif = CLIENT.networking().createVlanInterface(vlanInterface, subnet);
    	    	
        }
        else
        {
         vif = CLIENT.networking().createVlanInterface(vlanInterface, subnet,address);
	     //client_15.networking().deleteVlanInterface("ct0.eth2.104");
        }
            actionlogger.addInfo("Successfully created vlan interface " + vlanInterface + " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
            
           

            
            String vlanIdentity = accountName+"@"+vlanInterface;
        
    	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VLAN_IDENTITY, vlanIdentity);
    	actionlogger.addInfo("Vlan Inteface Identity as Output is saved");
    	
    	//UcsdCmdbUtils cmdb=new UcsdCmdbUtils();
        String description="FlashArray vLan is created. Details are : Account Name = "+config.getAccountName()+" , vLan Interface = "+ vlanInterface;
        
        UcsdCmdbUtils.updateRecord("FlashArray vLan", description, 1, context.getUserId(), vlanInterface,description);
        
        //context.getChangeTracker().resourceAdded("FlashArray Volume", connVol, volumePreName, description);

         context.getChangeTracker().undoableResourceAdded("FlashArray vLan : Created", vlanIdentity, vlanInterface,
           description,
           new DeleteVlanTask().getTaskName(), new DeleteVlanTaskConfig(config));
    
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
		return new CreateVlanTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_CREATE_VLAN_TASK;
	}


    
}
