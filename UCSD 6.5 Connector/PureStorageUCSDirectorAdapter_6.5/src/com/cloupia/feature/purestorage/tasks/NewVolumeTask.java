package com.cloupia.feature.purestorage.tasks;


import com.cisco.cuic.api.client.WorkflowInputFieldTypeDeclaration;
import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.UcsdCmdbUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.VolumeInventoryConfig;
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
import com.purestorage.rest.volume.PureVolume;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;


public class NewVolumeTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(NewVolumeTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	NewVolumeTaskConfig config = (NewVolumeTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);

        String volumePreName = config.getVolumePreName();
        String startNumber = config.getStartNumber();
        String endNumber = config.getEndNumber();
        String volumeSizeUnit = config.getVolumeSizeUnit();
        String volumeSizeNumber = config.getVolumeSizeNumber();
        Boolean destroyVolumeTaskFlag = config.getDestroyVolumeTaskFlag();
        String hostConnection = config.getHostConnection();
        String hostGroupConnection = config.getHostGroupConnection();
        List<String> volumeNameList = new ArrayList<String>();
        List<PureVolume> allVolume = CLIENT.volumes().list();
        List<String> allVolumeName = new ArrayList<String>();
        List<String> noRollBackVolumeName = new ArrayList<String>();

        //actionlogger.addInfo("Parameters are initialized");
        if(startNumber == null )
        {
        	startNumber="";
        	
        }
        if( endNumber == null)
        {
        	
        	endNumber="";
        }
        for(PureVolume oneVolume : allVolume)
        {
            allVolumeName.add(oneVolume.getName());
        }
        //actionlogger.addInfo("Parameters are initialized1 "+startNumber +" e "+endNumber);
        if((startNumber.equals("") && endNumber.equals("")) || (startNumber == null && endNumber == null))
        {
            String volumeName = volumePreName;
            volumeNameList.add(volumeName);
            if(allVolumeName != null && allVolumeName.contains(volumeName))
            {
            	                noRollBackVolumeName.add(volumeName);
            }
        }

        else
        {
        	actionlogger.addInfo("Parameters are initialized5");
            if(startNumber.equals("")||startNumber == null) startNumber = endNumber;
            if(endNumber.equals("")||endNumber == null) endNumber = startNumber;
           // actionlogger.addInfo("Parameters are initialized6");
            for(int i = Integer.parseInt(startNumber);i <= Integer.parseInt(endNumber);i++)
            {
                String volumeName = volumePreName + Integer.toString(i);
                volumeNameList.add(volumeName);
                if(allVolumeName != null && allVolumeName.contains(volumeName))
                {
                	//actionlogger.addInfo("Parameters are initialized7");
                    noRollBackVolumeName.add(volumeName);
                }
            }
        }
        actionlogger.addInfo("Checked volume name in the volume list ");
        config.setNewVolumeTaskFlag(true);
        config.setNoRollBackVolumeName(StringUtils.join(noRollBackVolumeName, ","));
        actionlogger.addInfo("Set volume task flag ");
        logger.info("Set volume task flag ");
        
       
        actionlogger.addInfo("Starting creating volume(s) "+destroyVolumeTaskFlag);
        logger.info("Starting creating volume(s) "+destroyVolumeTaskFlag);

        if(destroyVolumeTaskFlag == null)
         {
        	//actionlogger.addInfo("Starting creating volume(s)### "+volumeNameList);
        	String outVolName="";
        	String outSer="";
        	String connVol="";
            for(String volumeName : volumeNameList)
            {
            	actionlogger.addInfo("Volume Name is : "+volumeName+" , Size is : "+volumeSizeNumber+volumeSizeUnit);
            	logger.info("Volume Name is : "+volumeName+" , Size is : "+volumeSizeNumber+volumeSizeUnit);
            	CLIENT.volumes().create(volumeName, Long.parseLong(volumeSizeNumber), volumeSizeUnit);
            	
                PureVolume volume =  CLIENT.volumes().get(volumeName);
                ObjStore<VolumeInventoryConfig> store = ObjStoreHelper.getStore(VolumeInventoryConfig.class);
           	   
                
             	VolumeInventoryConfig volumeConfig = new VolumeInventoryConfig();
             	volumeConfig.setId(accountName+"@"+volume.getName());
             	volumeConfig.setAccountName(accountName);
             	volumeConfig.setVolumeName(volume.getName());
             	volumeConfig.setSize(volume.getSize()/(1024*1024*1024)); // Name
                 volumeConfig.setSource(volume.getSource()); //HostGroup
                 volumeConfig.setCreation(volume.getCreatedTimestamp().getTime());
                      // ObjStore<NewHostTaskConfig> store = ObjStoreHelper.getStore(NewHostTaskConfig.class);
                 store.insert(volumeConfig);
                if(volumeNameList.size()==1)
                {
                	String outVolName1=volume.getName();
                	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_NAME, outVolName1);
                	
                }
                if(connVol=="")
                {
                	connVol=accountName+"@"+volumeName;	
                }
                else
                {
                	connVol=connVol+","+accountName+"@"+volumeName;
                }
                if(outVolName == "")
                {
                	outVolName=volume.getName();
                }else
                {
                	outVolName=outVolName+","+volume.getName();
                }
                if(outSer == "")
                {
                	outSer=volume.getSerial();
                }else
                {
                	outSer=outSer+","+volume.getSerial();
                }
                
            }
            actionlogger.addInfo("Successfully created volume(s) " + volumePreName + " with range " + startNumber + "-" + endNumber +
                    " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
            
            	//PureVolume volume =  CLIENT.volumes().get(volumePreName);
            context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY, connVol);
        	actionlogger.addInfo("Volume Identities as Output is saved");
            	
            	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_NAMES, outVolName);
            	actionlogger.addInfo("Volume Name as Output is saved");
            	
                
            	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_SERIAL, outSer);
            	actionlogger.addInfo("The Serial Number of this volume is "+outSer);
            	actionlogger.addInfo("Serial number as Output is saved");
            

           	 //UcsdCmdbUtils cmdb=new UcsdCmdbUtils();
                String description="FlashArray Volume is created. Details are : Account Name = "+config.getAccountName()+" , Volume Name = "+ volumePreName+" , Start Number = "+startNumber+" , End Number = "+endNumber+" , Volume Size = "+volumeSizeNumber+volumeSizeUnit;
                
                UcsdCmdbUtils.updateRecord("FlashArray Volume", description, 1, context.getUserId(), volumePreName,description);
                
                //context.getChangeTracker().resourceAdded("FlashArray Volume", connVol, volumePreName, description);

                 context.getChangeTracker().undoableResourceAdded("FlashArray Volume : Created", connVol, volumePreName,
                   description,
                   new DestroyVolumesTask().getTaskName(), new DestroyVolumesTaskConfig(config));
        }

        else
        {
            actionlogger.addInfo("This is a rollback task for the task of destroying volumes");
            HashMap<String, List<String>> hostMap = splitConnection(hostConnection);
            HashMap<String, List<String>> hostGroupMap = splitConnection(hostGroupConnection);
            for(String volumeName : volumeNameList)
            {
                try
                {
                    CLIENT.volumes().recover(volumeName);
                    if(hostMap != null && hostMap.containsKey(volumeName))
                    {
                        List<String> hostList = hostMap.get(volumeName);
                        for(String host : hostList)
                        {
                            CLIENT.hosts().connectVolume(host, volumeName);
                        }
                    }
                    if(hostGroupMap != null && hostGroupMap.containsKey(volumeName))
                    {
                        List<String> hostGroupList = hostGroupMap.get(volumeName);
                        for(String hostGroup : hostGroupList)
                        {
                            CLIENT.hostGroups().connectVolume(hostGroup, volumeName);
                        }
                    }
                }
                catch (PureException e)
                {
                    actionlogger.addInfo("Error happens when recovering volume " + volumeName + "Exception: " + e.getMessage());
                }
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
   	public TaskOutputDefinition[] getTaskOutputDefinitions() {
   		TaskOutputDefinition[] ops = new TaskOutputDefinition[4];
   		ops[0] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Volume Identity(s)");
   		
   		ops[1] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_VOLUME_NAMES,
   				PureConstants.PURE_VOLUME_LIST_TABLE_NAMES,
   				"Volume Name(s)");
   		ops[2] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_VOLUME_NAME,
   				PureConstants.PURE_VOLUME_LIST_TABLE_NAME,
   				"Volume Name");

   		ops[3] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_VOLUME_SERIAL,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Serial number(s) of volume(s)");
   		return ops;
   	}

	@Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new NewVolumeTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_NEW_VOLUME;
	}

}
