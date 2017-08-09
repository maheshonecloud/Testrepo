package com.cloupia.feature.purestorage.tasks;


import com.cisco.cuic.api.client.WorkflowInputFieldTypeDeclaration;
import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.UcsdCmdbUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.HostGroupInventoryConfig;
import com.cloupia.feature.purestorage.accounts.HostInventoryConfig;
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
import com.purestorage.rest.hostgroup.PureHostGroup;
import com.purestorage.rest.hostgroup.PureHostGroupConnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;


public class DeleteHGSuffixRangeTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(DeleteHGSuffixRangeTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	DeleteHGSuffixRangeTaskConfig config = (DeleteHGSuffixRangeTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);


        String hostGroupPreName = config.getHostGroupPreName();
        String startNumber = config.getStartNumber();
        String endNumber = config.getEndNumber();
        Boolean newHostGroupTaskFlag = config.getNewHostGroupTaskFlag();
        String noRollBackHostGroupName = config.getNoRollBackHostGroupName();
        List<String> noRollBackHostGroupList = new ArrayList<String>();
        List<String> hostGroupNameList = new ArrayList<String>();
        List<PureHostGroup> allHostGroup = CLIENT.hostGroups().list();
        List<String> allHostGroupName = new ArrayList<String>();
        StringBuilder existHostGroups = new StringBuilder();
        StringBuilder volumeConnection = new StringBuilder();
        StringBuilder hostConnection = new StringBuilder();

        if(startNumber == null )
        {
        	startNumber="";
        	
        }
        if( endNumber == null)
        {
        	
        	endNumber="";
        }
        
        for(PureHostGroup oneHostGroup : allHostGroup)
        {
            allHostGroupName.add(oneHostGroup.getName());
        }

        if(startNumber.equals("") && endNumber.equals(""))
        {
            String hostGroupName = hostGroupPreName;
            hostGroupNameList.add(hostGroupName);
        }

        else
        {
            if(startNumber.equals("")) startNumber = endNumber;
            if(endNumber.equals("") ) endNumber = startNumber;

            for(int i = Integer.parseInt(startNumber);i <= Integer.parseInt(endNumber);i++)
            {
                String hostGroupName = hostGroupPreName + Integer.toString(i);
                hostGroupNameList.add(hostGroupName);
            }
        }

        for(String hostGroupName : hostGroupNameList)
        {
            if(allHostGroupName.contains(hostGroupName))
            {
                existHostGroups.append(hostGroupName +",");
                List<String> hostNames = CLIENT.hostGroups().get(hostGroupName).getHosts();
                List<PureHostGroupConnection> volumeHostGroup = CLIENT.hostGroups().getConnections(hostGroupName);
                if(hostNames.size()>0)
                {
                    hostConnection.append(hostGroupName + ":");
                    for(String oneHostName : hostNames)
                    {
                        hostConnection.append(oneHostName + ",");
                    }
                    hostConnection.append("!");
                }
                if(volumeHostGroup.size()>0)
                {
                    volumeConnection.append(hostGroupName + ":");
                    for(PureHostGroupConnection oneConnection : volumeHostGroup)
                    {
                        volumeConnection.append(oneConnection.getVolumeName() + ",");
                    }
                    volumeConnection.append("!");
                }
            }
        }
        config.setExistHostGroup(existHostGroups.toString());
        config.setHostConnection(hostConnection.toString());
        config.setVolumeConnection(volumeConnection.toString());

        

        actionlogger.addInfo("starting deleting host group(s)");

        if(newHostGroupTaskFlag != null)
        {
            actionlogger.addInfo("This is a rollback task for creating new host groups");
            noRollBackHostGroupList = Arrays.asList(noRollBackHostGroupName.split(","));
        }
        String hostGroupIdentity ="";
        for(String hostGroupName : hostGroupNameList)
        {
            try
            {
                if(noRollBackHostGroupList == null || !noRollBackHostGroupList.contains(hostGroupName))
                {
                    deleteHostGroup(hostGroupName, CLIENT);
                    actionlogger.addInfo("deleting hostgroup" + hostGroupName);
                    ObjStore<HostGroupInventoryConfig> store2 = ObjStoreHelper.getStore(HostGroupInventoryConfig.class);
             	   
                    String query3 = "id == '" + accountName+"@"+hostGroupName + "'";
                    List<HostGroupInventoryConfig> hconfig = store2.query(query3);
                    actionlogger.addInfo("Host Id :"+ hconfig.get(0).getId());
                
                    long s = store2.delete(query3);
                    actionlogger.addInfo("Deleted in Inventory :" + s);
                }
            }
            catch (PureException e)
            {
                actionlogger.addError("Error happens while deleting host group " + hostGroupName + "Exception: " + e.getMessage());
                throw e;
            }
            if(hostGroupIdentity=="")
            {
            	hostGroupIdentity=accountName+"@"+hostGroupName;	
            }
            else
            {
            	hostGroupIdentity=hostGroupIdentity+","+accountName+"@"+hostGroupName;
            }
        }
       
        
        
    	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_HOSTGROUP_IDENTITY, hostGroupIdentity);
    	actionlogger.addInfo("Host Group Identity as Output is saved");
    	
    	String description="FlashArray HostGroup is Deleted. Details are : Account Name = "+config.getAccountName()+" , HostGroup Name = "+ hostGroupPreName+" , Start Number = "+startNumber+" , End Number = "+endNumber;
        
        UcsdCmdbUtils.updateRecord("FlashArray HostGroup", description, 2, context.getUserId(), hostGroupPreName,description);
        
        context.getChangeTracker().resourceAdded("FlashArray HostGroup : Deleted", hostGroupIdentity, hostGroupPreName, description);
        context.getChangeTracker().undoableResourceDeleted("FlashArray HostGroup", hostGroupIdentity, hostGroupPreName, description,
                new NewHostGroupTask().getTaskName(), new NewHostGroupTaskConfig(config));
    
    }

    @Override
    public TaskOutputDefinition[] getTaskOutputDefinitions()
    {
    	TaskOutputDefinition[] ops = new TaskOutputDefinition[1];
   		

   		ops[0] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_HOSTGROUP_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Host Group Identity(s)");
   		return ops;
    }


    public void deleteHostGroup(String hostGroupName, PureRestClient CLIENT)
    {
        PureHostGroup tempHG = CLIENT.hostGroups().get(hostGroupName);
        List<PureHostGroupConnection> connectedVolumes = CLIENT.hostGroups().getConnections(hostGroupName);
        List<String> connectedHosts = tempHG.getHosts();

        CLIENT.hostGroups().removeHosts(hostGroupName, connectedHosts);

        for(PureHostGroupConnection hgVolumeConnection : connectedVolumes)
        {
            String tempVolume = hgVolumeConnection.getVolumeName();
            CLIENT.hostGroups().disconnectVolume(hostGroupName,tempVolume);
        }

        CLIENT.hostGroups().delete(hostGroupName);
    }

	@Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new DeleteHGSuffixRangeTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_DELETE_HG_SUFFIX_RANGE;
	}

}
