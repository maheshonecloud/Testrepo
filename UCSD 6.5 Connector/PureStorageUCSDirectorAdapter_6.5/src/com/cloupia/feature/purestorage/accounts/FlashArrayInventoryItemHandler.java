package com.cloupia.feature.purestorage.accounts;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.tasks.NewHostTaskConfig;
import com.cloupia.fw.objstore.ObjStore;
import com.cloupia.fw.objstore.ObjStoreHelper;
import com.cloupia.lib.connector.AbstractInventoryItemHandler;
import com.cloupia.lib.connector.InventoryContext;
import com.cloupia.model.cIM.ReportNameValuePair;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.array.PureArrayController;
import com.purestorage.rest.array.PureArraySpaceMetrics;
import com.purestorage.rest.host.PureHost;
import com.purestorage.rest.host.PureHostConnection;
import com.purestorage.rest.hostgroup.PureHostGroup;
import com.purestorage.rest.hostgroup.PureHostGroupConnection;
import com.purestorage.rest.networking.PureNetworkInterfaceAttributes;
import com.purestorage.rest.port.PureArrayInitiatorPort;
import com.purestorage.rest.port.PureArrayPort;
import com.purestorage.rest.protectiongroup.PureVolumeSnapshot;
import com.purestorage.rest.volume.PureVolume;

public class FlashArrayInventoryItemHandler extends AbstractInventoryItemHandler
{
    private static Logger logger = Logger.getLogger(FlashArrayInventoryItemHandler.class);
    
    @Override
    public void cleanup(String arg0) throws Exception
    {
        logger.info("FlashArrayInventoryItemHandler.cleanup: " + arg0);
        doCleanupHost(arg0);
        doCleanupVolume(arg0);
        doCleanupHostGroup(arg0);
        //doCleanupChildHost(arg0);
        doCleanupFcPort(arg0);
        doCleanupIqnPort(arg0);
        doCleanupArray(arg0);
        doCleanupSpace(arg0);
        doCleanupHostConnection(arg0);
        doCleanupSnapshot(arg0);
        doCleanupNetwork(arg0);
    }

    @Override
    public void doInventory(String arg0, InventoryContext arg1) throws Exception
    {
    	cleanup(arg0);
    	logger.info("FlashArrayInventoryItemHandler.doInventory: " + arg0);
        doInventoryHost(arg0);
    	doInventoryVolume(arg0);
    	doInventoryHostGroup(arg0);
    	//doInventoryChildHost(arg0);
    	doInventoryFcPort(arg0);
    	doInventoryIqnPort(arg0);
    	doInventoryArray(arg0);
    	doInventorySpace(arg0);
    	doInventoryHostConnection(arg0);
    	doInventorySnapshot(arg0);
    	doInventoryNetwork(arg0);
    }

    @Override
    public void doInventory(String arg0, Object arg1) throws Exception
    {
    	

    }
    public void doInventoryHost(String accountName) throws Exception
    {
    	 logger.info("HostInventoryItemHandler.doInventory: " + accountName);
         
         
            
            if (accountName != null && accountName.length() > 0)
            {
                FlashArrayAccount acc;
				
					acc = FlashArrayAccount.getFlashArrayCredential(accountName);
				
                PureRestClient CLIENT = PureUtils.ConstructPureRestClient(acc);
                List<PureHost> hosts =  CLIENT.hosts().list();
                
                ObjStore<HostInventoryConfig> store = ObjStoreHelper.getStore(HostInventoryConfig.class);
                	   
                for (PureHost host: hosts)
                {
                	HostInventoryConfig hostConfig = new HostInventoryConfig();
                	hostConfig.setId(accountName+"@"+host.getName());
                	   hostConfig.setAccountName(accountName);
                    hostConfig.setHost(host.getName()); // Name
                    hostConfig.setHostGroup(host.getHostGroupName()); //HostGroup
                    List<PureHostConnection> privateConnections = CLIENT.hosts().getPrivateConnections(host.getName());
                    List<PureHostConnection> sharedConnections = CLIENT.hosts().getSharedConnections(host.getName());
                    // private and shared connections cannot overlap (i.e. same vol cannot be part of both shared and private connections)
                    hostConfig.setVolumes(privateConnections.size() + sharedConnections.size()); // Number of volumes
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
                    hostConfig.setConnectedVolumes(connVolumes);
                    hostConfig.setProvisionedSize(totalSize/(1024*1024*1024)); // Provisioned size of attached volumes
                    
                   // ObjStore<NewHostTaskConfig> store = ObjStoreHelper.getStore(NewHostTaskConfig.class);
                    store.insert(hostConfig);
                
                }
            }
            
    }
    
    public void doInventoryVolume(String accountName) throws Exception
    {
    	 logger.info("VolumeInventoryItemHandler.doInventory: " + accountName);
         
         
            
            if (accountName != null && accountName.length() > 0)
            {
            	 FlashArrayAccount acc = FlashArrayAccount.getFlashArrayCredential(accountName);
                 PureRestClient CLIENT = PureUtils.ConstructPureRestClient(acc);
                 List<PureVolume> volumes =  CLIENT.volumes().list();
                
                ObjStore<VolumeInventoryConfig> store = ObjStoreHelper.getStore(VolumeInventoryConfig.class);
                	   
                for (PureVolume volume: volumes)
                {
                	VolumeInventoryConfig volumeConfig = new VolumeInventoryConfig();
                	volumeConfig.setId(accountName+"@"+volume.getName());
                	volumeConfig.setAccountName(accountName);
                	volumeConfig.setVolumeName(volume.getName());
                	volumeConfig.setSize(volume.getSize()/(1024*1024*1024)); // Name
                    volumeConfig.setSource(volume.getSource()); //HostGroup
                    volumeConfig.setCreation(volume.getCreatedTimestamp().getTime());
                         // ObjStore<NewHostTaskConfig> store = ObjStoreHelper.getStore(NewHostTaskConfig.class);
                    store.insert(volumeConfig);
                
                }
            }
   
  
        }
    
    public void doInventoryHostGroup(String accountName) throws Exception
    {
    	 logger.info("HostGroupInventoryItemHandler.doInventory: " + accountName);
         
         
            
            if (accountName != null && accountName.length() > 0)
            {
            	FlashArrayAccount acc = FlashArrayAccount.getFlashArrayCredential(accountName);
                PureRestClient CLIENT = PureUtils.ConstructPureRestClient(acc);
                List<PureHostGroup> hostgroups =  CLIENT.hostGroups().list();
                
                ObjStore<HostGroupInventoryConfig> store = ObjStoreHelper.getStore(HostGroupInventoryConfig.class);
                	   
              
                for (PureHostGroup hostgroup: hostgroups)
                {
                	
               
                	HostGroupInventoryConfig hostGroupConfig = new HostGroupInventoryConfig();
                	hostGroupConfig.setId(accountName+"@"+hostgroup.getName());
                	hostGroupConfig.setAccountName(accountName);
                	hostGroupConfig.setHostGroupName(hostgroup.getName()); // Name
                	hostGroupConfig.setHosts(hostgroup.getHosts().size()); //No. of Hosts
                     List<PureHostGroupConnection> connections = CLIENT.hostGroups().getConnections(hostgroup.getName());
                     
                     // private and shared connections cannot overlap (i.e. same vol cannot be part of both shared and private connections)
                     hostGroupConfig.setVolumes(connections.size()); // Number of volumes
                     long totalSize = 0;
                     String connVolumes="";
                     for (PureHostGroupConnection connection: connections)
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
                     hostGroupConfig.setConnectedVolumes(connVolumes);
                     hostGroupConfig.setProvisionedSize(totalSize/(1024*1024*1024)); // Provisioned size of attached volumes
                     hostGroupConfig.setVolumeCapacity(CLIENT.hostGroups().getSpaceMetrics(hostgroup.getName()).getVolumes()/(1024*1024*1024)); // Provisioned size of attached volumes
                     hostGroupConfig.setReduction(CLIENT.hostGroups().getSpaceMetrics(hostgroup.getName()).getDataReduction()); // Provisioned size of attached volumes
                     List<String> hostsList1 =hostgroup.getHosts();
                     String hostsList="";
 	            	logger.info("Hosts in Hostgroup :"+ hostsList1);
 	            	for(String hos : hostsList1)
 	            	{
 	            		 if(hostsList == "")
 	                    {
 	            			hostsList=hos;
 	                    }else
 	                    {
 	            		hostsList=hostsList+","+hos;
 	                    }
 	            	}
 	            	hostGroupConfig.setHostsList(hostsList);
                         // ObjStore<NewHostTaskConfig> store = ObjStoreHelper.getStore(NewHostTaskConfig.class);
                    store.insert(hostGroupConfig);
                
                }
            }
   
  
        }
    
    
    public void doInventoryChildHost(String accountName) throws Exception
    {
    	 logger.info("ChildHostInventoryItemHandler.doInventory: " + accountName);
         
         
            
            if (accountName != null && accountName.length() > 0)
            {
                FlashArrayAccount acc;
				
					acc = FlashArrayAccount.getFlashArrayCredential(accountName);
				
                PureRestClient CLIENT = PureUtils.ConstructPureRestClient(acc);
                List<PureHost> hosts =  CLIENT.hosts().list();
                
                ObjStore<ChildHostInventoryConfig> store = ObjStoreHelper.getStore(ChildHostInventoryConfig.class);
                	   
                for (PureHost host: hosts)
                {
                	ChildHostInventoryConfig hostConfig = new ChildHostInventoryConfig();
                	hostConfig.setId(accountName+"@"+host.getName());
                	   hostConfig.setAccountName(accountName);
                    hostConfig.setHost(host.getName()); // Name
                    hostConfig.setHostGroup(host.getHostGroupName()); //HostGroup
                    List<PureHostConnection> privateConnections = CLIENT.hosts().getPrivateConnections(host.getName());
                    List<PureHostConnection> sharedConnections = CLIENT.hosts().getSharedConnections(host.getName());
                    // private and shared connections cannot overlap (i.e. same vol cannot be part of both shared and private connections)
                    hostConfig.setVolumes(privateConnections.size() + sharedConnections.size()); // Number of volumes
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
                    hostConfig.setConnectedVolumes(connVolumes);
                    hostConfig.setProvisionedSize(totalSize/(1024*1024*1024)); // Provisioned size of attached volumes
                    
                   // ObjStore<NewHostTaskConfig> store = ObjStoreHelper.getStore(NewHostTaskConfig.class);
                    store.insert(hostConfig);
                
                }
            }
            
    }
    
    public void doInventoryFcPort(String accountName) throws Exception
    {
    	 logger.info("FcPortInventoryItemHandler.doInventory: " + accountName);
         
         
            
            if (accountName != null && accountName.length() > 0)
            {
            	FlashArrayAccount acc = FlashArrayAccount.getFlashArrayCredential(accountName);
                PureRestClient CLIENT = PureUtils.ConstructPureRestClient(acc);
                List<PureArrayPort> ports =  CLIENT.ports().list();
                
                ObjStore<FcPortInventoryConfig> store = ObjStoreHelper.getStore(FcPortInventoryConfig.class);
                	   
                for (PureArrayPort port: ports)
                {
                	
                	
                	if(port.getName().contains("FC") && port.getName().split("\\.").length<3)
                	{
                		FcPortInventoryConfig fcPortConfig = new FcPortInventoryConfig();
                		String speed ="";
                		long sp = CLIENT.hardware().getHardwareAttributes(port.getName()).getSpeed();
                		if(sp==0)
                		{
                		speed="0";	
                		}
                		else
                		{
                		sp=sp/1000000000;
                		speed=""+sp;
                		}
                		String wwpn =port.getWwn();
                		List<String> strings = new ArrayList<String>();
                		int index = 0;
                		while (index < wwpn.length()) {
                		    strings.add(wwpn.substring(index, Math.min(index + 2,wwpn.length())));
                		    index += 2;
                		}
                		logger.info(strings);
                		String wwn=strings.get(0);
                		for (int i1 = 1; i1 < strings.size(); i1++) {
                			//System.out.println(strings.get(i));
                			wwn=wwn+":"+strings.get(i1);
                		}
                		//wwn=wwn+":"+strings.get(strings.size()-1);
                		logger.info(wwn);
                		
                		fcPortConfig.setAccountName(accountName);
                		fcPortConfig.setId(accountName+"@"+port.getName());
                		fcPortConfig.setName(port.getName());
                		fcPortConfig.setWwpn(wwn);
                		fcPortConfig.setSpeed(speed);
                		
                		store.insert(fcPortConfig);
                
                }
            }
          }
   
  
        }
    
    public void doInventoryIqnPort(String accountName) throws Exception
    {
    	 logger.info("IqnPortInventoryItemHandler.doInventory: " + accountName);
         
         
            
            if (accountName != null && accountName.length() > 0)
            {
            	FlashArrayAccount acc = FlashArrayAccount.getFlashArrayCredential(accountName);
                PureRestClient CLIENT = PureUtils.ConstructPureRestClient(acc);
                List<PureArrayPort> ports =  CLIENT.ports().list();
                
                ObjStore<IqnPortInventoryConfig> store = ObjStoreHelper.getStore(IqnPortInventoryConfig.class);
                
                
                
                for (PureArrayPort port: ports)
                {
                	
                	if(port.getName().contains("ETH") && port.getName().split("\\.").length<3)
                	{
                		IqnPortInventoryConfig iqnPortConfig = new IqnPortInventoryConfig();
                		String speed ="";
                		long sp = CLIENT.hardware().getHardwareAttributes(port.getName()).getSpeed();
                		if(sp==0)
                		{
                		speed="0";	
                		}
                		else
                		{
                			sp=sp/1000000000;
                		speed=""+sp;
                		}
                		
                		iqnPortConfig.setAccountName(accountName);
                		iqnPortConfig.setId(accountName+"@"+port.getName());
                		iqnPortConfig.setName(port.getName());
                		iqnPortConfig.setIqn(port.getIqn());
                		iqnPortConfig.setSpeed(speed);
                		
                		store.insert(iqnPortConfig);
                
                }
            }
          }
   
  
        }
    
    public void doInventoryArray(String accountName) throws Exception
    {
    	 logger.info("ArrayInventoryItemHandler.doInventory: " + accountName);
         
         
            
            if (accountName != null && accountName.length() > 0)
            {
            	
                
                ObjStore<ArrayInventoryConfig> store = ObjStoreHelper.getStore(ArrayInventoryConfig.class);
                
                
                
                FlashArrayAccount acc = FlashArrayAccount.getFlashArrayCredential(accountName);
                PureRestClient CLIENT = PureUtils.ConstructPureRestClient(acc);
                logger.info("Getting Array Detail......");
                PureArraySpaceMetrics spaceMetrics = CLIENT.array().getSpaceMetrics();
                long provisionedCapacity = (long) 0.0;
                int noOfVolume=0,noOfSnapshot=0,noOfHost=0,noOfHostGroup=0;
                String version="",ipAddress="";
                StringBuilder model1 = new StringBuilder();
                List<PureArrayController> controllers = CLIENT.array().getControllers();
                // For multiple controllers, show Model as:"FA-250|FA-250"
                String delim = "";
                for (PureArrayController controller: controllers)
                {
                    model1.append(delim).append(controller.getModel());
                    delim = "|";
                }
                ArrayInventoryConfig arrayConfig = new ArrayInventoryConfig();
                List<PureVolume> volumes =  CLIENT.volumes().list();
                for (PureVolume volume: volumes)
                {
                	provisionedCapacity +=volume.getSize();
               	           	          	
                }
                version = CLIENT.array().getAttributes().getVersion();
                ipAddress =acc.getManagementAddress();
                noOfVolume =  CLIENT.volumes().list().size();
                noOfSnapshot = CLIENT.volumes().getSnapshots().size();
                noOfHost = CLIENT.hosts().list().size();
                noOfHostGroup =  CLIENT.hostGroups().list().size();
               
                long freeSpace = spaceMetrics.getCapacity() - spaceMetrics.getTotal();
                logger.info("Details are getting stored....");
                //logger.info(accountName+" "+);
                	arrayConfig.setAccountName(accountName);
                	arrayConfig.setModel(model1.toString());
                	arrayConfig.setTotalRawSpace(spaceMetrics.getCapacity()/(1024*1024*1024)); // Name
                	arrayConfig.setTotalProvisionedSpace(provisionedCapacity/(1024*1024*1024)); //No. of Hosts
                	arrayConfig.setFreeSpace(""+(freeSpace/(1024*1024*1024))); // Provisioned size of attached volumes
                	arrayConfig.setSharedSpace(""+(spaceMetrics.getSharedSpace()/(1024*1024*1024))); // Provisioned size of attached volumes
                	arrayConfig.setSystemSpace(""+(spaceMetrics.getSystem()/(1024*1024*1024))); // Provisioned size of attached volumes
                	arrayConfig.setVolumesSpace(""+(spaceMetrics.getVolumes()/(1024*1024*1024))); // Provisioned size of attached volumes
                	arrayConfig.setSnapshotSpace(""+(spaceMetrics.getSnapshots()/(1024*1024*1024))); // Provisioned size of attached volumes
                	arrayConfig.setReduction(spaceMetrics.getDataReduction()); // Provisioned size of attached volumes
                	arrayConfig.setNoOfVolume(noOfVolume); // Provisioned size of attached volumes
                	arrayConfig.setNoOfSnapshot(noOfSnapshot); // Provisioned size of attached volumes
                	arrayConfig.setNoOfHost(noOfHost); // Provisioned size of attached volumes
                	arrayConfig.setNoOfHostGroup(noOfHostGroup); // Provisioned size of attached volumes
                	arrayConfig.setIpaddress(ipAddress);
                	arrayConfig.setVersion(version);
                    logger.info("Values is added");
                    
                    store.insert(arrayConfig);
          }
   
  
        }
    
    
    public void doInventorySpace(String accountName) throws Exception
    {
    	 logger.info("SpaceInventoryItemHandler.doInventory: " + accountName);
         
         
            
            if (accountName != null && accountName.length() > 0)
            {
            	
                
                ObjStore<SpaceInventoryConfig> store = ObjStoreHelper.getStore(SpaceInventoryConfig.class);
                
                
                
                FlashArrayAccount acc = FlashArrayAccount.getFlashArrayCredential(accountName);
                PureRestClient CLIENT = PureUtils.ConstructPureRestClient(acc);
                PureArraySpaceMetrics spaceMetrics = CLIENT.array().getSpaceMetrics();
                long provisionedCapacity = (long) 0.0;
                int noOfVolume=0,noOfSnapshot=0;
                List<PureVolume> volumes =  CLIENT.volumes().list();
                for (PureVolume volume: volumes)
                {
                	provisionedCapacity +=volume.getSize();
                	
                	           	          	
                	noOfVolume++;
                }
                
                List<PureVolumeSnapshot> snapshots =  CLIENT.volumes().getSnapshots();
                for (PureVolumeSnapshot snapshot: snapshots)
                {
                	
                	            	           	          	
                	noOfSnapshot++;
                }
                
                
                SpaceInventoryConfig spaceConfig = new SpaceInventoryConfig();
                
               double freeSpace = spaceMetrics.getCapacity() - spaceMetrics.getTotal();
               
               
               

                	spaceConfig.setAccountName(accountName);
					spaceConfig.setFreeSpace(""+(freeSpace/(1024*1024*1024))); // Provisioned size of attached volumes
                	spaceConfig.setSharedSpace(""+(spaceMetrics.getSharedSpace()/(1024*1024*1024))); // Provisioned size of attached volumes
                	spaceConfig.setSystemSpace(""+(spaceMetrics.getSystem()/(1024*1024*1024))); // Provisioned size of attached volumes
                	spaceConfig.setVolumeSpace(""+(spaceMetrics.getVolumes()/(1024*1024*1024))); // Provisioned size of attached volumes
                	spaceConfig.setSnapshotSpace(""+(spaceMetrics.getSnapshots()/(1024*1024*1024))); // Provisioned size of attached volumes
                	
                    logger.info("Values is added");
                    
                    store.insert(spaceConfig);
          }
   
  
        }
    
    public void doInventoryHostConnection(String accountName) throws Exception
    {
    	 logger.info("HostConnectionInventoryItemHandler.doInventory: " + accountName);
         
         
            
            if (accountName != null && accountName.length() > 0)
            {
            	ObjStore<HostConnectionInventoryConfig> store = ObjStoreHelper.getStore(HostConnectionInventoryConfig.class);
            	FlashArrayAccount acc = FlashArrayAccount.getFlashArrayCredential(accountName);
                PureRestClient CLIENT = PureUtils.ConstructPureRestClient(acc);
                List<PureArrayInitiatorPort> ports =  CLIENT.ports().listInitiatorPorts();
                List<PureHost> hosts =  CLIENT.hosts().list();
                for (PureHost host: hosts)
                {
                	logger.info("## The Current host is "+host.getName());
                	if(host.getWwnList().isEmpty()&&host.getIqnList().isEmpty())
                	{
                		logger.info("No WWPN or IQN ");
                		HostConnectionInventoryConfig hostConnConfig = new HostConnectionInventoryConfig();
                		hostConnConfig.setId(accountName+"@"+host.getName());
                		hostConnConfig.setAccountName(accountName);
                        hostConnConfig.setHostName(host.getName());
                        hostConnConfig.setInitiatorWwpn("");	
                        hostConnConfig.setInitiatorIqn("");
                        hostConnConfig.setConnectivity("NONE");
                        
                        hostConnConfig.setTargetInterfaces("");
                        hostConnConfig.setTargetWwpn("");
                        hostConnConfig.setTargetIqn("");
                        hostConnConfig.setIniatorPortal("");
            			
            			hostConnConfig.setTargetPortal("");
            			
            			store.insert(hostConnConfig);
            			
                		
                	}
                	else
                	{
                		String ini_wwpn="",ini_iqn="",conn="NONE",intf="",tar_wwpn="",tar_iqn="",ini_portal="",tar_portal="";
                	List<String>  lsWWN=host.getWwnList();
                	
                	if(lsWWN==null||lsWWN.isEmpty())
            		{
            			
            			logger.info("## WWN list is empty "+host.getName());
            		}
                	else
                	{
                		for(int i=0;i<lsWWN.size();i++)
                		{
                			ini_wwpn="";
                			
        					ini_iqn="";
        					conn="NONE";
        					intf="";tar_wwpn="";tar_iqn="";ini_portal="";tar_portal="";
                            
                			
                			for (PureArrayInitiatorPort port: ports){
                    		
                    		
                    		 if(lsWWN.get(i).equalsIgnoreCase(port.getWwn()))
                    		{
                    			logger.info("## "+host.getName());
                        		logger.info("## "+host.getWwnList());
                        		//logger.info("## "+host.getIqnList());
                    			ini_wwpn=getPorts(port.getWwn());
                    			logger.info("## WWN port is "+ini_wwpn);
                    			if(port.getTarget()==null||port.getTarget().isEmpty())
                        		{
                        			intf="";	
                        			conn="NONE";
                        			logger.info("## the target for WWN is empty ");
                        		}
                        		else
                        		{
                        			if(intf=="")
                        			{
                        				intf=port.getTarget();
                            			conn="Redundant";
                            			logger.info("## target is "+intf);
                        				
                        			}else
                        			{
                        			intf=intf+","+port.getTarget();
                        			conn="Redundant";
                        			logger.info("## target2 is "+intf);
                        			}
                        		}
                        		
                        		
                        		if(port.getTargetWwn()==null||port.getTargetWwn().isEmpty())
                        		{
                        			tar_wwpn="";
                        			logger.info("## the target WWN is empty ");
                        			
                        		}
                        		else
                        		{
                        			if(tar_wwpn=="")
                        			{
                        			tar_wwpn=getPorts(port.getTargetWwn());	
                        			logger.info("## the target WWN is  ");
                        			logger.info("## "+tar_wwpn);
                        			}
                        			else
                        			{
                        				tar_wwpn=tar_wwpn+","+getPorts(port.getTargetWwn());	
                            			logger.info("## the target WWN 2 is  ");
                            			logger.info("## "+tar_wwpn);	
                        			}
                        		}
                        		
                    		}
                    		 
                    		}
                			if(i<lsWWN.size())
    	            		{
    	            			
                				HostConnectionInventoryConfig hostConnConfig = new HostConnectionInventoryConfig();
              				
                    			hostConnConfig.setId(accountName+"@"+host.getName());
                        		hostConnConfig.setAccountName(accountName);
                        		hostConnConfig.setHostName(host.getName());
                                hostConnConfig.setInitiatorWwpn(ini_wwpn);	
                                hostConnConfig.setInitiatorIqn(ini_iqn);
                                hostConnConfig.setConnectivity(conn);
    	                        
                                hostConnConfig.setTargetInterfaces(intf);
                                hostConnConfig.setTargetWwpn(tar_wwpn);
                                hostConnConfig.setTargetIqn(tar_iqn);
                                hostConnConfig.setIniatorPortal(ini_portal);
    	            			
                                hostConnConfig.setTargetPortal(tar_portal);
    	            			store.insert(hostConnConfig);
    	                		
    	            		}
                		
                			
                		}	
                	}
                            	
                	List<String> lsIQN=host.getIqnList();
                	if(lsIQN==null||lsIQN.isEmpty())
            		{
            			
            			logger.info("## IQN list is empty "+host.getName());
            		}
                	else
                	{
                		logger.info("## "+host.getName());
                		logger.info("## "+lsIQN.size());
                		
                		for(int i=0;i<lsIQN.size();i++)
                		{
                			ini_wwpn="";
                			
        					ini_iqn="";
        					conn="NONE";
        					intf="";tar_wwpn="";tar_iqn="";ini_portal="";tar_portal="";
                			logger.info("## inside "+lsIQN.size()); 
    	            		for (PureArrayInitiatorPort port: ports)
    	                    {
    	            			logger.info("## inside port loop "+lsIQN.get(i)+" "+port.getIqn()); 
    	            		if(lsIQN.get(i).equalsIgnoreCase(port.getIqn()))
    	            		{
    	            			logger.info("## "+host.getName());
    	                		//logger.info("## "+host.getWwnList());
    	                		logger.info("## "+host.getIqnList());
    	            			ini_iqn=port.getIqn();
    	            			logger.info("## Initiator IQN is "+ini_iqn);
    	            			
    	            			if(port.getTargetIqn()==null||port.getTargetIqn().isEmpty())
    	                		{
    	                			tar_iqn="";	
    	                			logger.info("## Target IQN is empty");
    	                		}
    	                		else
    	                		{
    	                			//tar_iqn=port.getTargetIqn();
    	                			//logger.info("## Target IQN is "+tar_iqn);
    	                			if(tar_iqn=="")
                        			{
    	                				tar_iqn=port.getTargetIqn();
    	                				logger.info("## Target IQN is "+tar_iqn);                    			}
                        			else
                        			{
                        				tar_iqn=tar_iqn+","+port.getTargetIqn();	
                        				logger.info("## Target IQN is "+tar_iqn);   	
                        			}
                        	
    	                			
    	                		}
    	                		
    	                		
    	                		if(port.getTarget()==null||port.getTarget().isEmpty())
    	                		{
    	                			intf="";	
    	                			conn="NONE";
    	                			logger.info("## the target for IQN is empty ");
    	                		}
    	                		else
    	                		{
    	                			//intf=port.getTarget();
    	                			if(intf=="")
                        			{
                        				intf=port.getTarget();
                            			conn="Redundant";
                            			logger.info("## target is "+intf);
                        				
                        			}else
                        			{
                        			intf=intf+","+port.getTarget();
                        			conn="Redundant";
                        			logger.info("## target2 is "+intf);
                        			}
    	                		}
    	                		
    	                		if(port.getPortal()==null||port.getPortal().isEmpty())
    	                		{
    	                			ini_portal="";
    	                			
    	                			logger.info("## Initiator portal is empty");
    	                		}
    	                		else
    	                		{
    	                			//ini_portal=port.getPortal();
    	                			//logger.info("## Initiator portal is "+ini_portal);
    	                			if(ini_portal=="")
                        			{
    	                				ini_portal=port.getPortal();
    	                				logger.info("## initial portal  is "+ini_portal);                    			}
                        			else
                        			{
                        				ini_portal=ini_portal+","+port.getPortal();	
                        				logger.info("## initial portal  is "+ini_portal);    	
                        			}
    	                			
    	                		}
    	                		
    	                		if(port.getTargetPortal()==null||port.getTargetPortal().isEmpty())
    	                		{
    	                			tar_portal="";	
    	                			logger.info("## target portal is empty ");
    	                		}
    	                		else
    	                		{
    	                			if(tar_portal=="")
                        			{
    	                				tar_portal=port.getTargetPortal();
    	                				logger.info("## initial portal  is "+tar_portal);                    			}
                        			else
                        			{
                        				tar_portal=ini_portal+","+port.getTargetPortal();	
                        				logger.info("## initial portal  is "+tar_portal);    	
                        			}
    	                		}
    	                		
    	            		  }
    	                    }
    	            		if(i<lsIQN.size())
    	            		{
    	            			
    	            			HostConnectionInventoryConfig hostConnConfig = new HostConnectionInventoryConfig();
                  				
                    			hostConnConfig.setId(accountName+"@"+host.getName());
                        		hostConnConfig.setAccountName(accountName);
                        		hostConnConfig.setHostName(host.getName());
                                hostConnConfig.setInitiatorWwpn(ini_wwpn);	
                                hostConnConfig.setInitiatorIqn(ini_iqn);
                                hostConnConfig.setConnectivity(conn);
    	                        
                                hostConnConfig.setTargetInterfaces(intf);
                                hostConnConfig.setTargetWwpn(tar_wwpn);
                                hostConnConfig.setTargetIqn(tar_iqn);
                                hostConnConfig.setIniatorPortal(ini_portal);
    	            			
                                hostConnConfig.setTargetPortal(tar_portal);
    	            			store.insert(hostConnConfig);
    	                		
    	            		}
                		}
                		
                    		
                	}
                	
                	
                	
        			
        		}

    	
                	
                }

          }
   
  
        }
    
    String getPorts(String pot)
    {
    	String wwpn =pot;
		List<String> strings = new ArrayList<String>();
		int index = 0;
		while (index < wwpn.length()) {
		    strings.add(wwpn.substring(index, Math.min(index + 2,wwpn.length())));
		    index += 2;
		}
		logger.info(strings);
		String wwn=strings.get(0);
		for (int i1 = 1; i1 < strings.size(); i1++) {
			//System.out.println(strings.get(i));
			wwn=wwn+":"+strings.get(i1);
		}
		//wwn=wwn+":"+strings.get(strings.size()-1);
		logger.info(wwn);
		return wwn;
    }
    
    
    public void doInventorySnapshot(String accountName) throws Exception
    {
    	 logger.info("SnapshotInventoryItemHandler.doInventory: " + accountName);
         
         
            
            if (accountName != null && accountName.length() > 0)
            {
            	 FlashArrayAccount acc = FlashArrayAccount.getFlashArrayCredential(accountName);
                 PureRestClient CLIENT = PureUtils.ConstructPureRestClient(acc);
                
                
                ObjStore<SnapshotInventoryConfig> store = ObjStoreHelper.getStore(SnapshotInventoryConfig.class);
                	   
                List<PureVolumeSnapshot> snapshots =  CLIENT.volumes().getSnapshots();
                for (PureVolumeSnapshot snapshot: snapshots)
                {
                	SnapshotInventoryConfig snapConfig = new SnapshotInventoryConfig();
                	snapConfig.setId(accountName+"@"+snapshot.getName());
                	snapConfig.setAccountName(accountName);
                	snapConfig.setSnapshotName(snapshot.getName());
                	snapConfig.setSize(snapshot.getSize()/(1024*1024*1024));
                	snapConfig.setSource(snapshot.getSource());
                	snapConfig.setCreation(snapshot.getCreatedTimestamp().getTime());
                    store.insert(snapConfig);

                }
            }
   
  
        }
    
    public void doInventoryNetwork(String accountName) throws Exception
    {
    	 logger.info("NetworkInventoryItemHandler.doInventory: " + accountName);
         
         
            
            if (accountName != null && accountName.length() > 0)
            {
            	 FlashArrayAccount acc = FlashArrayAccount.getFlashArrayCredential(accountName);
                 PureRestClient CLIENT = PureUtils.ConstructPureRestClient(acc);
                
                
                ObjStore<NetworkInventoryConfig> store = ObjStoreHelper.getStore(NetworkInventoryConfig.class);
                int i = 0;	   
                List<PureNetworkInterfaceAttributes> intfs =  CLIENT.networking().listInterfaces();
                for (PureNetworkInterfaceAttributes intf: intfs)
                {
                	NetworkInventoryConfig netConfig = new NetworkInventoryConfig();
                	netConfig.setId(accountName+"@"+intf.getName());
                	netConfig.setAccountName(accountName);	
                	netConfig.setPortName(intf.getName());	
                	netConfig.setIpaddress(intf.getAddress());
                	netConfig.setGateway(intf.getGateway());
                	netConfig.setMtu(intf.getMtu());
    			String serv=intf.getServices().get(0);
    			logger.info(serv);
    			if(intf.getServices().size()>1)
    			{
    			for(int l=1;i<intf.getServices().size();i++)
    			{
    				serv = serv+","+intf.getServices().get(l);
    				logger.info(serv);
    			
    			}
    			}
    			netConfig.setServices(serv);
    			store.insert(netConfig);
                	
    			
    			
    			
    		}
            }
   
  
        }
    
    
    public void doCleanupHost(String accountName) throws Exception
    {
    	 	logger.info("HostCleanupItemHandler.doCleanup: " + accountName);
         
                ObjStore<HostInventoryConfig> store = ObjStoreHelper.getStore(HostInventoryConfig.class);
                	   
                String query = "accountName == '" + accountName + "'";
                store.delete(query);
                           
            
    }
    
    public void doCleanupVolume(String accountName) throws Exception
    {
    	 	logger.info("VolumeCleanupItemHandler.doCleanup: " + accountName);
         
                ObjStore<VolumeInventoryConfig> store = ObjStoreHelper.getStore(VolumeInventoryConfig.class);
                	   
                String query = "accountName == '" + accountName + "'";
                store.delete(query);
                           
            
    }
    public void doCleanupHostGroup(String accountName) throws Exception
    {
    	 	logger.info("HostGroupCleanupItemHandler.doCleanup: " + accountName);
         
                ObjStore<HostGroupInventoryConfig> store = ObjStoreHelper.getStore(HostGroupInventoryConfig.class);
                	   
                String query = "accountName == '" + accountName + "'";
                store.delete(query);
                           
            
    }
    
    public void doCleanupChildHost(String accountName) throws Exception
    {
    	 	logger.info("ChildHostCleanupItemHandler.doCleanup: " + accountName);
         
                ObjStore<ChildHostInventoryConfig> store = ObjStoreHelper.getStore(ChildHostInventoryConfig.class);
                	   
                String query = "accountName == '" + accountName + "'";
                store.delete(query);
                           
            
    }
    public void doCleanupFcPort(String accountName) throws Exception
    {
    	 	logger.info("FcPortCleanupItemHandler.doCleanup: " + accountName);
         
                ObjStore<FcPortInventoryConfig> store = ObjStoreHelper.getStore(FcPortInventoryConfig.class);
                	   
                String query = "accountName == '" + accountName + "'";
                store.delete(query);
                           
            
    }
    public void doCleanupIqnPort(String accountName) throws Exception
    {
    	 	logger.info("IqnPortCleanupItemHandler.doCleanup: " + accountName);
         
                ObjStore<IqnPortInventoryConfig> store = ObjStoreHelper.getStore(IqnPortInventoryConfig.class);
                	   
                String query = "accountName == '" + accountName + "'";
                store.delete(query);
                           
            
    }
    public void doCleanupArray(String accountName) throws Exception
    {
    	 	logger.info("ArrayCleanupItemHandler.doCleanup: " + accountName);
         
                ObjStore<ArrayInventoryConfig> store = ObjStoreHelper.getStore(ArrayInventoryConfig.class);
                	   
                String query = "accountName == '" + accountName + "'";
                store.delete(query);
                           
            
    }
    public void doCleanupSpace(String accountName) throws Exception
    {
    	 	logger.info("SpaceCleanupItemHandler.doCleanup: " + accountName);
         
                ObjStore<SpaceInventoryConfig> store = ObjStoreHelper.getStore(SpaceInventoryConfig.class);
                	   
                String query = "accountName == '" + accountName + "'";
                store.delete(query);
                           
            
    }
    
    public void doCleanupHostConnection(String accountName) throws Exception
    {
    	 	logger.info("HostConectionCleanupItemHandler.doCleanup: " + accountName);
         
                ObjStore<HostConnectionInventoryConfig> store = ObjStoreHelper.getStore(HostConnectionInventoryConfig.class);
                	   
                String query = "accountName == '" + accountName + "'";
                store.delete(query);
                           
            
    }
    
    public void doCleanupSnapshot(String accountName) throws Exception
    {
    	 	logger.info("SnapshotCleanupItemHandler.doCleanup: " + accountName);
         
                ObjStore<SnapshotInventoryConfig> store = ObjStoreHelper.getStore(SnapshotInventoryConfig.class);
                	   
                String query = "accountName == '" + accountName + "'";
                store.delete(query);
                           
            
    }
    
    public void doCleanupNetwork(String accountName) throws Exception
    {
    	 	logger.info("NetworkCleanupItemHandler.doCleanup: " + accountName);
         
                ObjStore<NetworkInventoryConfig> store = ObjStoreHelper.getStore(NetworkInventoryConfig.class);
                	   
                String query = "accountName == '" + accountName + "'";
                store.delete(query);
                           
            
    }
}