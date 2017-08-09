package com.cloupia.feature.purestorage.tasks;


import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.exceptions.PureException;
import com.purestorage.rest.host.PureHost;
import com.purestorage.rest.host.PureHostConnection;
import com.purestorage.rest.hostgroup.PureHostGroup;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cisco.cuic.api.client.WorkflowInputFieldTypeDeclaration;
import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.HostGroupInventoryConfig;
import com.cloupia.feature.purestorage.accounts.HostInventoryConfig;
import com.cloupia.feature.purestorage.accounts.SpaceInventoryConfig;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.fw.objstore.ObjStore;
import com.cloupia.fw.objstore.ObjStoreHelper;
import com.cloupia.service.cIM.inframgr.AbstractTask;
import com.cloupia.service.cIM.inframgr.TaskConfigIf;
import com.cloupia.service.cIM.inframgr.TaskOutputDefinition;
import com.cloupia.service.cIM.inframgr.customactions.CustomActionLogger;
import com.cloupia.service.cIM.inframgr.customactions.CustomActionTriggerContext;


public class DisconnectHostHGTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(DisconnectHostHGTask.class);


@Override
public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
{
	
	long configEntryId = context.getConfigEntry().getConfigEntryId();
	//retrieving the corresponding config object for this handler
	DisconnectHostHGTaskConfig config = (DisconnectHostHGTaskConfig) context.loadConfigObject();
	 PureRestClient CLIENT = null;
	 String accountName = config.getAccountName();
    actionlogger.addInfo("finished checking Task accoutname "+accountName);
    FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
    CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);
    

        String allHostName = config.getHostName();
        String hostGroupName = config.getHostGroupName();
        String[] hostNameList = allHostName.split(",");
        //String testFlag = config.getTestFlag();
        List<String> hostNameList1= new ArrayList<String>();

        for(int i=0; i<hostNameList.length; i++)
        {
            String hostName = hostNameList[i];
            hostNameList1.add(hostName);
        }
        try
        {
            CLIENT.hostGroups().removeHosts(hostGroupName, hostNameList1);
        }
        catch (PureException e)
        {
            actionlogger.addError("Error happens when disconnecting " + allHostName + " with host group " + hostGroupName );
        }
        actionlogger.addInfo("Successfully Disconnected host(s) to hostgroup " + hostGroupName + " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
		
        String connHost="";
        for(int i=0; i<hostNameList.length; i++)
        {
            String hostName = hostNameList[i];
            actionlogger.addInfo("Host Name is : " +hostName);
            
            PureHost host =CLIENT.hosts().get(hostName);
   		 String id=accountName+"@"+hostName;
   		 List<PureHostConnection> privateConnections = CLIENT.hosts().getPrivateConnections(host.getName());
            List<PureHostConnection> sharedConnections = CLIENT.hosts().getSharedConnections(host.getName());
            // private and shared connections cannot overlap (i.e. same vol cannot be part of both shared and private connections)
            int vol =(privateConnections.size() + sharedConnections.size()); // Number of volumes
            long totalSize = 0;
            String connVolumes="";
            for (PureHostConnection connection: privateConnections)
            {
                totalSize += CLIENT.volumes().get(connection.getVolumeName()).getSize();
                if(connVolumes=="")
                {
                	connVolumes=connection.getVolumeName();	
                }
                else
                {
                connVolumes=connVolumes+","+connection.getVolumeName();
                }
            }
            for (PureHostConnection connection: sharedConnections)
            {
                totalSize += CLIENT.volumes().get(connection.getVolumeName()).getSize();
                if(connVolumes=="")
                {
                	connVolumes=connection.getVolumeName();	
                }
                else
                {
                connVolumes=connVolumes+","+connection.getVolumeName();
                }
            }
            
            double provSize =totalSize/(1024*1024*1024); 
            PureUtils.deleteHost(id);
            PureUtils.insertHost(id, accountName, hostName, host.getHostGroupName(),vol,connVolumes,provSize);
            
            if(connHost=="")
            {
            	
            	connHost=accountName+"@"+hostName;	
            }
            else
            {
            	connHost=connHost+","+accountName+"@"+hostName;
            }  
        }

        
        

       String hostGroupIdentity =accountName+"@"+hostGroupName;
		PureHostGroup hostgroup =CLIENT.hostGroups().get(hostGroupName);
		List<String> hostsList1 =hostgroup.getHosts();
        String hostsList="";
    	actionlogger.addInfo("Hosts in Hostgroup :"+ hostsList1);
    	for(String hos : hostsList1)
    	{
    		 if(hostsList == "")
            {
    			hostsList=hos;
            }
    		else
            {
    		hostsList=hostsList+","+hos;
            }
    	}
    	actionlogger.addInfo("Hosts in Hostgroup :"+ hostsList);
        context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_VOLUME_IDENTITY, connHost);
    	actionlogger.addInfo("Host Identities as Output is saved");
    	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_HOSTGROUP_IDENTITY, hostGroupIdentity);
    	actionlogger.addInfo("Host Group Identity as Output is saved");
    	 ObjStore<HostGroupInventoryConfig> store = ObjStoreHelper.getStore(HostGroupInventoryConfig.class);
  	   
         String query = "id == '" + hostGroupIdentity + "'";
         List<HostGroupInventoryConfig> hgconfig = store.query(query);
         actionlogger.addInfo("Hosts Id :"+ hgconfig.get(0).getId());
         hgconfig.get(0).setHostsList(hostsList);
         hgconfig.get(0).setHosts(hostgroup.getHosts().size());
         store.modifySingleObject("hostsList == '" + hostsList + "' && hosts == '" + hostgroup.getHosts().size() +"'",  hgconfig.get(0));
         String query1 = "id == '" + hostGroupIdentity + "'";
         List<HostGroupInventoryConfig> hgconfig1 = store.query(query1);
         actionlogger.addInfo("Hosts List :"+ hgconfig1.get(0).getHostsList()+ "  "+hgconfig1.get(0).getHosts() );
         
         
         
         
    }

    @Override
    public TaskOutputDefinition[] getTaskOutputDefinitions()
    {
    	TaskOutputDefinition[] ops = new TaskOutputDefinition[2];
   		ops[0] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_HOST_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Host Identity(s)");

   		ops[1] = new TaskOutputDefinition(
   				PureConstants.TASK_OUTPUT_NAME_HOSTGROUP_IDENTITY,
   				WorkflowInputFieldTypeDeclaration.GENERIC_TEXT,
   				"Host Group Identity");
   		return ops;
    }

    @Override
	public TaskConfigIf getTaskConfigImplementation() {
		// TODO Auto-generated method stub
		return new DisconnectHostHGTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_DISCONNECT_HOSTS_WITH_HOSTGROUP;
	}



}
