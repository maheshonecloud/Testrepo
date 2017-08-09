package com.cloupia.feature.purestorage.tasks;


import com.cisco.cuic.api.client.WorkflowInputFieldTypeDeclaration;
import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.UcsdCmdbUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.HostGroupInventoryConfig;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;


public class NewHostGroupTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(NewHostGroupTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	NewHostGroupTaskConfig config = (NewHostGroupTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);

        String hostGroupPreName = config.getHostGroupPreName();
        String startNumber = config.getStartNumber();
        String endNumber = config.getEndNumber();
        String hostConnection = config.getHostConnection();
        String volumeConnection = config.getVolumeConnection();
        String existHostGroup = config.getExistHostGroups();
        List<PureHostGroup> allHostGroup = CLIENT.hostGroups().list();
        List<String> allHostGroupName = new ArrayList<String>();
        List<String> noRollBackHostGroupName = new ArrayList<String>();
        actionlogger.addInfo("finished "+startNumber+endNumber);
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

        List<String> hostGroupNameList = new ArrayList<String>();

        if(startNumber.equals("") && endNumber.equals(""))
        {
            String hostGroupName = hostGroupPreName;
            hostGroupNameList.add(hostGroupName);
            if(allHostGroupName != null && allHostGroupName.contains(hostGroupName))
            {
                noRollBackHostGroupName.add(hostGroupName);
            }
        }

        else
        {
            if(startNumber.equals("")) startNumber = endNumber;
            if(endNumber.equals("")) endNumber = startNumber;

            for(int i = Integer.parseInt(startNumber);i <= Integer.parseInt(endNumber);i++)
            {
                String hostGroupName = hostGroupPreName + Integer.toString(i);
                hostGroupNameList.add(hostGroupName);
                if(allHostGroupName != null && allHostGroupName.contains(hostGroupName))
                {
                    noRollBackHostGroupName.add(hostGroupName);
                }
            }
        }

        config.setNewHostGroupTaskFlag(true);
        config.setNoRollBackHostGroupName(StringUtils.join(noRollBackHostGroupName, ","));
        String hostGroupIdentity ="",hostGroups="",hostGroup="";
        ObjStore<HostGroupInventoryConfig> store = ObjStoreHelper.getStore(HostGroupInventoryConfig.class);
       
        if(existHostGroup == null)
        {
            actionlogger.addInfo("it is not a rollback task");
            for(String hostGroupName : hostGroupNameList)
            {
                CLIENT.hostGroups().create(hostGroupName);
                actionlogger.addInfo("Creating Hostgroup : " + hostGroupName);
                PureHostGroup hostgroup =CLIENT.hostGroups().get(hostGroupName);
                HostGroupInventoryConfig hostGroupConfig = new HostGroupInventoryConfig();
            	hostGroupConfig.setId(accountName+"@"+hostgroup.getName());
            	hostGroupConfig.setAccountName(accountName);
            	hostGroupConfig.setHostGroupName(hostgroup.getName()); // Name
            	hostGroupConfig.setHosts(hostgroup.getHosts().size()); //No. of Hosts
            	 hostGroupConfig.setConnectedVolumes("");
                 hostGroupConfig.setProvisionedSize(0.0); // Provisioned size of attached volumes
                 hostGroupConfig.setVolumeCapacity(CLIENT.hostGroups().getSpaceMetrics(hostgroup.getName()).getVolumes()/(1024*1024*1024)); // Provisioned size of attached volumes
                 hostGroupConfig.setReduction(CLIENT.hostGroups().getSpaceMetrics(hostgroup.getName()).getDataReduction());
                 hostGroupConfig.setHostsList("");
                 
                 store.insert(hostGroupConfig);
                if(hostGroupNameList.size()==1)
                {
                	hostGroup=hostGroupName;
                	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_HOSTGROUP_NAME, hostGroup);
                	
                }
                if(hostGroups=="")
                {
                	hostGroups=hostGroupName;	
                }
                else
                {
                	hostGroups=hostGroups+","+hostGroupName;
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
            actionlogger.addInfo("Successfully created hostgroup(s) " + hostGroupPreName + " with range " + startNumber + "-" + endNumber +
                    " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
           
            
            context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_HOSTGROUP_IDENTITY, hostGroupIdentity);
            context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_HOSTGROUP_NAMES, hostGroups);
        	
        	actionlogger.addInfo("Host Group Identity as Output is saved");
        	
        	//UcsdCmdbUtils cmdb=new UcsdCmdbUtils();
            String description="FlashArray HostGroup is created. Details are : Account Name = "+config.getAccountName()+" , HostGroup Name = "+ hostGroupPreName+" , Start Number = "+startNumber+" , End Number = "+endNumber;
            
            UcsdCmdbUtils.updateRecord("FlashArray HostGroup", description, 1, context.getUserId(), hostGroupPreName,description);
            
            //context.getChangeTracker().resourceAdded("FlashArray HostGroup", hostGroupIdentity, hostGroupPreName, description);
            context.getChangeTracker().undoableResourceAdded("FlashArray HostGroup : Created", hostGroupIdentity, hostGroupPreName,description,
                    new DeleteHGSuffixRangeTask().getTaskName(), new DeleteHGSuffixRangeTaskConfig(config));

            
        }

        else
        {
            actionlogger.addInfo("connectedHost is a rollback task and hostgroups are " + existHostGroup);

            HashMap<String, List<String>> hostMap = splitConnection(hostConnection);
            HashMap<String, List<String>> volumeMap = splitConnection(volumeConnection);

            if(!existHostGroup.equals(""))
            {
                String[] hostGroupArrays = existHostGroup.split(",");

                for(String oneHostGroup : hostGroupArrays)
                {
                    try
                    {
                        CLIENT.hostGroups().create(oneHostGroup);
                        actionlogger.addInfo("Creating Hostgroup : " + oneHostGroup);
                        if(hostMap != null && hostMap.containsKey(oneHostGroup))
                        {
                            CLIENT.hostGroups().addHosts(oneHostGroup, hostMap.get(oneHostGroup));
                        }
                        if(volumeMap != null && volumeMap.containsKey(oneHostGroup))
                        {
                            List<String> volumeList = volumeMap.get(oneHostGroup);
                            for(String oneVolume : volumeList)
                            {
                                CLIENT.hostGroups().connectVolume(oneHostGroup, oneVolume);
                            }
                        }
                    }
                    catch (PureException e)
                    {
                        actionlogger.addInfo("Error happens when creating host group for rollback deleting host group task" + oneHostGroup + " Exception: " + e.getMessage());
                    }
                }
                actionlogger.addInfo("Finished creating host groups for rollback deleting host group task");
            }
            else
            {
                actionlogger.addInfo("No host group need to be created for rollback deleting host group task");
            }
        }
    }

    public HashMap<String, List<String>> splitConnection(String connections)
    {
        HashMap<String, List<String>> result = new HashMap<String,List<String>>();

        if(connections.equals("")) return null;
        String[] connectionArrays = connections.split("!");
        for(String oneConnection : connectionArrays)
        {
            String[] oneConnectionArrays = oneConnection.split(":");
            result.put(oneConnectionArrays[0], Arrays.asList(oneConnectionArrays[1].split(",")));
        }

        return result;
    }

    @Override
    public TaskOutputDefinition[] getTaskOutputDefinitions()
    {
    	TaskOutputDefinition[] ops = new TaskOutputDefinition[3];
   		

   		ops[0] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_HOSTGROUP_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Host Group Identity(s)");
   		ops[1] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_HOSTGROUP_NAME,
   				PureConstants.PURE_HOSTGROUP_NAME,
   				"FlashArray Host Group Name");
   		
   		ops[2] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_HOSTGROUP_NAMES,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"FlashArray Host Group Name(s)");
   		return ops;
    }

	@Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new NewHostGroupTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_NEW_HOSTGROUP;
	}

}
