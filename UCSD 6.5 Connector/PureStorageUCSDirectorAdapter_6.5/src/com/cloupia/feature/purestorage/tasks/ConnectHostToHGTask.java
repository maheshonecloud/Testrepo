package com.cloupia.feature.purestorage.tasks;


import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.host.PureHost;
import com.purestorage.rest.host.PureHostConnection;
import com.purestorage.rest.hostgroup.PureHostGroup;
import com.purestorage.rest.hostgroup.PureHostGroupConnection;
import com.cisco.cuic.api.client.WorkflowInputFieldTypeDeclaration;
import com.cloupia.feature.purestorage.PureUtils;
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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


public class ConnectHostToHGTask extends AbstractTask
{
	static Logger logger = Logger.getLogger(ConnectHostToHGTask.class);
	

    @Override
    public void executeCustomAction(CustomActionTriggerContext context, CustomActionLogger actionlogger) throws Exception
    {
    	
    	long configEntryId = context.getConfigEntry().getConfigEntryId();
		//retrieving the corresponding config object for this handler
    	ConnectHostToHGTaskConfig config = (ConnectHostToHGTaskConfig) context.loadConfigObject();
		 PureRestClient CLIENT = null;
		 String accountName = config.getAccountName();
        actionlogger.addInfo("finished checking NewHostTask accoutname");
        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);
        

        String allHostName = config.getHostName();
        String hostGroupName = config.getHostGroupName();
        
        String[] hostNameList = allHostName.split(",");
        List<String> connectedHostName = null;
        List<PureHostGroup> hostGroups =  CLIENT.hostGroups().list();
        		 for (PureHostGroup hostgroup: hostGroups)
                 {
     	         if(hostgroup.getName().equals(hostGroupName))
     	         {
        			 connectedHostName = hostgroup.getHosts();
     	         }
                 }
       String testFlag = "purestorage Saif testing input flag";
        config.setTestFlag(testFlag);

        

        

        actionlogger.addInfo("Starting connecting host(s) to hostgroup " + hostGroupName +
                " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
        
      String connHost="";
        List<String> connectingHostName = new ArrayList<String>();
        for(int i=0; i<hostNameList.length; i++)
        {
            String hostName = hostNameList[i];
            if(connectedHostName.contains(hostName))
            {
                actionlogger.addError(hostName + " has already been connected to host group" + hostGroupName);
                continue;
            }
            connectingHostName.add(hostName);
            
            if(connHost=="")
            {
            	connHost=accountName+"@"+hostName;	
            }
            else
            {
            	connHost=connHost+","+accountName+"@"+hostName;
            }
                        
        }
        if(connectingHostName.isEmpty())
        {
        	actionlogger.addInfo(" All Host has already been connected to host group" + hostGroupName);
            
        }
        else
        {
        CLIENT.hostGroups().addHosts(hostGroupName, connectingHostName);
        }
        /*for(int i=0; i<hostNameList.length; i++)
        {
            String hostName = hostNameList[i];
            if(connectedHostName.contains(hostName))
            {
                actionlogger.addInfo(hostName + " has already been connected to host group" + hostGroupName);
                continue;
            }
            
           
            ObjStore<HostInventoryConfig> store2 = ObjStoreHelper.getStore(HostInventoryConfig.class);
            
            String query3 = "id == '" + accountName+"@"+hostName + "'";
            List<HostInventoryConfig> hconfig = store2.query(query3);
            actionlogger.addInfo("Hosts Id :"+ hconfig.get(0).getId());
            hconfig.get(0).setHostGroup(hostGroupName);
            
            store2.modifySingleObject("hostGroup == '" + hostGroupName + "' ",  hconfig.get(0));
            String query4 = "id == '" + accountName+"@"+hostName + "'";
            List<HostInventoryConfig> hconfig1 = store2.query(query4);
            actionlogger.addInfo("Hosts Id :"+ hconfig1.get(0).getId());
            actionlogger.addInfo("Connected HostGroup :"+ hconfig1.get(0).getHostGroup());
        }*/
        
        actionlogger.addInfo("Successfully Connected host(s) to hostgroup " + hostGroupName + " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
        String hostGroupIdentity =accountName+"@"+hostGroupName;
        
        context.getChangeTracker().undoableResourceModified("AssetType", "idstring", PureConstants.TASK_NAME_CONNECT_HOST_HOSTGROUP,
                "Hosts has been connected to hostGroup " + config.getAccountName(),
                new DisconnectHostHGTask().getTaskName(), new DisconnectHostHGTaskConfig(config));
        
        context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_HOST_IDENTITY, connHost);
    	actionlogger.addInfo("Host Identities as Output is saved");
    	context.saveOutputValue(PureConstants.TASK_OUTPUT_NAME_HOSTGROUP_IDENTITY, hostGroupIdentity);
    	actionlogger.addInfo("Host Group Identity as Output is saved");
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
    		
    		 PureHost host =CLIENT.hosts().get(hos);
    		 String id=accountName+"@"+hos;
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
             actionlogger.addInfo("Modifying Host "+ id);
             PureUtils.deleteHost(id);
             PureUtils.insertHost(id, accountName, hos, host.getHostGroupName(),vol,connVolumes,provSize);
    	}
    	actionlogger.addInfo("Hosts in Hostgroup :"+ hostsList);
       
    	 ObjStore<HostGroupInventoryConfig> store = ObjStoreHelper.getStore(HostGroupInventoryConfig.class);
  	   
         String query = "id == '" + hostGroupIdentity + "'";
         List<HostGroupInventoryConfig> hgconfig = store.query(query);
         actionlogger.addInfo("Hosts Id :"+ hgconfig.get(0).getId()+" "+hgconfig.size());
         hgconfig.get(0).setHostsList(hostsList);
         hgconfig.get(0).setHosts(hostgroup.getHosts().size());
         store.modifySingleObject("hostsList == '" + hostsList + "' && hosts == '" + hostgroup.getHosts().size()+"'" ,  hgconfig.get(0));
         String query1 = "id == '" + hostGroupIdentity + "'";
         List<HostGroupInventoryConfig> hgconfig1 = store.query(query1);
         actionlogger.addInfo("Hosts Id :"+ hgconfig1.get(0).getId()+" "+hgconfig1.size());
         
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
		return new ConnectHostToHGTaskConfig();
	}

	@Override
	public String getTaskName() {
		// TODO Auto-generated method stub
		return PureConstants.TASK_NAME_CONNECT_HOST_HOSTGROUP;
	}

}
