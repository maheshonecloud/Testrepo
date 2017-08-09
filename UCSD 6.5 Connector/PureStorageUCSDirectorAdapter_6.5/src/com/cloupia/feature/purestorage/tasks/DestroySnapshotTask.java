package com.cloupia.feature.purestorage.tasks;


import com.cisco.cuic.api.client.WorkflowInputFieldTypeDeclaration;
import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.UcsdCmdbUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.SnapshotInventoryConfig;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.fw.objstore.ObjStore;
import com.cloupia.fw.objstore.ObjStoreHelper;
import com.cloupia.service.cIM.inframgr.AbstractTask;
import com.cloupia.service.cIM.inframgr.TaskConfigIf;
import com.cloupia.service.cIM.inframgr.TaskOutputDefinition;
import com.cloupia.service.cIM.inframgr.customactions.CustomActionLogger;
import com.cloupia.service.cIM.inframgr.customactions.CustomActionTriggerContext;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.exceptions.PureException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;


public class DestroySnapshotTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(DestroySnapshotTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	DestroySnapshotTaskConfig config = (DestroySnapshotTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);


        String snapshotName = config.getSnapshotName();
        Boolean eradicate = config.isEradicate();

       
  

        actionlogger.addInfo("starting destroying snapshot(s)");

        
        String connVol="";
        
            try
            {
            	 CLIENT.volumes().destroy(snapshotName);
            	 if(eradicate==true)
                 {
                     CLIENT.volumes().destroy(snapshotName, eradicate);
                 }
                
                   
                    actionlogger.addInfo("Destroying Snapshot : " + snapshotName);
                    
                    ObjStore<SnapshotInventoryConfig> store2 = ObjStoreHelper.getStore(SnapshotInventoryConfig.class);
               	   
                    String query3 = "id == '" + accountName+"@"+snapshotName + "'";
                    List<SnapshotInventoryConfig> hconfig = store2.query(query3);
                    actionlogger.addInfo("Snapshot Id :"+ hconfig.get(0).getId());
                
                    long s = store2.delete(query3);
                    actionlogger.addInfo("Deleted in Inventory :" + s);
                
            }
            catch(PureException e)
            {
                actionlogger.addError("Error happens while destroying snapshot: " + snapshotName + " Exception: "+ e.getMessage());
                throw e;
            }
            if(connVol=="")
            {
            	connVol=accountName+"@"+snapshotName;	
            }
            else
            {
            	connVol=connVol+","+accountName+"@"+snapshotName;
            }
        

        actionlogger.addInfo("successfully destroying snapshot " + snapshotName +
                " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
        context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY, connVol);
    	actionlogger.addInfo("Snapshot Identities as Output is saved");
    	
    	String description="FlashArray Snapshot is Deleted. Details are : Account Name = "+config.getAccountName()+" , Snapshot Name = "+ snapshotName;
        
        UcsdCmdbUtils.updateRecord("FlashArray Snapshot", description, 2, context.getUserId(), snapshotName,description);
        
        context.getChangeTracker().resourceAdded("FlashArray Snapshot : Deleted", connVol, snapshotName, description);
       //context.getChangeTracker().undoableResourceDeleted("FlashArray Snapshot", connVol, snapshotName, description,
          //      new NewSnapshotTask().getTaskName(), new NewSnapshotTaskConfig(config));
    }

    
    @Override
    public TaskOutputDefinition[] getTaskOutputDefinitions()
    {
    	TaskOutputDefinition[] ops = new TaskOutputDefinition[1];
   		

   		ops[0] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_SNAPSHOT_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Snapshot Identity(s)");
   		return ops;
    }

	@Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new DestroySnapshotTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_DESTROY_SNAPSHOT;
	}
}
