package com.cloupia.feature.purestorage;


import java.util.List;

import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.fw.objstore.ObjStore;
import com.cloupia.fw.objstore.ObjStoreHelper;
import com.cloupia.feature.purestorage.accounts.ArrayInventoryConfig;
import com.cloupia.feature.purestorage.accounts.ChildHostInventoryConfig;
import com.cloupia.feature.purestorage.accounts.FcPortInventoryConfig;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.HostConnectionInventoryConfig;
import com.cloupia.feature.purestorage.accounts.HostGroupInventoryConfig;
import com.cloupia.feature.purestorage.accounts.HostInventoryConfig;
import com.cloupia.feature.purestorage.accounts.IqnPortInventoryConfig;
import com.cloupia.feature.purestorage.accounts.NetworkInventoryConfig;
import com.cloupia.feature.purestorage.accounts.SnapshotInventoryConfig;
import com.cloupia.feature.purestorage.accounts.SpaceInventoryConfig;
import com.cloupia.feature.purestorage.accounts.VolumeInventoryConfig;
import com.purestorage.rest.PureApiVersion;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.PureRestClientConfig;
import com.purestorage.rest.PureRestClientImpl17;
import com.purestorage.rest.host.PureHost;
import com.purestorage.rest.host.PureHostConnection;
import com.purestorage.rest.hostgroup.PureHostGroupConnection;
import com.purestorage.rest.volume.PureVolume;


public class PureUtils 
{
    static Logger logger = Logger.getLogger(PureUtils.class);

    public static PureRestClient ConstructPureRestClient(FlashArrayAccount acc)
    {
        return ConstructPureRestClient(acc, "1.7");
    }
    public static PureRestClient ConstructPureRestClient(FlashArrayAccount acc, String apiVersion)
    {
        String apiEndPoint = "https://" + acc.getManagementAddress() + "/api";
        PureApiVersion restAPIversion = PureApiVersion.fromString(apiVersion);
        logger.info(acc.getAccount().getAccountName() + ":Connecting to Pure FlashArray at [" + apiEndPoint + "] with username ["
                + acc.getUsername() + "]");
        PureRestClientConfig CONFIG = PureRestClientConfig.newBuilder(acc.getUsername(), acc.getPassword(), apiEndPoint, restAPIversion)//10.203.128.141
                .setClientInfo(PureConstants.PURE_ADAPTER_NAME, PureConstants.PURE_ADAPTER_VERSION)
                .setIgnoreCertificateError(true)
               .build();

        PureRestClient CLIENT = PureRestClientImpl17.create(CONFIG);
        CLIENT.array().getAttributes();
        logger.info(acc.getAccount().getAccountName() + ":Successfully connected to Pure FlashArray at " + apiEndPoint);
        return CLIENT;
    }
    
    public static List<VolumeInventoryConfig> getAllPureVolumes() throws Exception{
    	
    	ObjStore<VolumeInventoryConfig> store = ObjStoreHelper.getStore(VolumeInventoryConfig.class);
        
    	
    	List<VolumeInventoryConfig> objs = store.queryAll();
    	
    	return objs;

    	
    }
    
public static List<HostInventoryConfig> getAllPureHost() throws Exception{
    	
    	ObjStore<HostInventoryConfig> store = ObjStoreHelper.getStore(HostInventoryConfig.class);
        
    	
    	List<HostInventoryConfig> objs = store.queryAll();
    	
    	return objs;

    	
    }

public static List<ChildHostInventoryConfig> getAllPureChildHost() throws Exception{
	
	ObjStore<ChildHostInventoryConfig> store = ObjStoreHelper.getStore(ChildHostInventoryConfig.class);
    
	
	List<ChildHostInventoryConfig> objs = store.queryAll();
	
	return objs;

	
}

public static List<HostGroupInventoryConfig> getAllPureHostGroup() throws Exception{
	
	ObjStore<HostGroupInventoryConfig> store = ObjStoreHelper.getStore(HostGroupInventoryConfig.class);
    
	
	List<HostGroupInventoryConfig> objs = store.queryAll();
	
	
	
	return objs;

	
}
public static List<FcPortInventoryConfig> getAllPureFcPorts() throws Exception{
	
	ObjStore<FcPortInventoryConfig> store = ObjStoreHelper.getStore(FcPortInventoryConfig.class);
    
	
	List<FcPortInventoryConfig> objs = store.queryAll();
	
	return objs;

	
}

public static List<IqnPortInventoryConfig> getAllPureIqnPorts() throws Exception{
	
	ObjStore<IqnPortInventoryConfig> store = ObjStoreHelper.getStore(IqnPortInventoryConfig.class);
    
	
	List<IqnPortInventoryConfig> objs = store.queryAll();
	
	return objs;

	
}

public static List<ArrayInventoryConfig> getAllPureArray() throws Exception{
	
	ObjStore<ArrayInventoryConfig> store = ObjStoreHelper.getStore(ArrayInventoryConfig.class);
    
	
	List<ArrayInventoryConfig> objs = store.queryAll();
	
	return objs;

	
}

public static List<SpaceInventoryConfig> getAllPureSpace() throws Exception{
	
	ObjStore<SpaceInventoryConfig> store = ObjStoreHelper.getStore(SpaceInventoryConfig.class);
    
	
	List<SpaceInventoryConfig> objs = store.queryAll();
	
	return objs;

	
}

public static List<HostConnectionInventoryConfig> getAllPureHostConnections() throws Exception{
	
	ObjStore<HostConnectionInventoryConfig> store = ObjStoreHelper.getStore(HostConnectionInventoryConfig.class);
    
	
	List<HostConnectionInventoryConfig> objs = store.queryAll();
	
	return objs;

	
}

public static List<NetworkInventoryConfig> getAllPureNetworks() throws Exception{
	
	ObjStore<NetworkInventoryConfig> store = ObjStoreHelper.getStore(NetworkInventoryConfig.class);
    
	
	List<NetworkInventoryConfig> objs = store.queryAll();
	
	return objs;

	
}

public static List<SnapshotInventoryConfig> getAllPureSnapshots() throws Exception{
	
	ObjStore<SnapshotInventoryConfig> store = ObjStoreHelper.getStore(SnapshotInventoryConfig.class);
    
	
	List<SnapshotInventoryConfig> objs = store.queryAll();
	
	return objs;

	
}

public static void insertVolume(String id, String accountName, String volumeName,double size,String source,long creation) throws Exception
{
	ObjStore<VolumeInventoryConfig> store = ObjStoreHelper.getStore(VolumeInventoryConfig.class);
	   
    
    	VolumeInventoryConfig volumeConfig = new VolumeInventoryConfig();
    	volumeConfig.setId(id);
    	volumeConfig.setAccountName(accountName);
    	volumeConfig.setVolumeName(volumeName);
    	volumeConfig.setSize(size); // Name
        volumeConfig.setSource(source); //HostGroup
        volumeConfig.setCreation(creation);
             // ObjStore<NewHostTaskConfig> store = ObjStoreHelper.getStore(NewHostTaskConfig.class);
        store.insert(volumeConfig);
    
    
}
public static void insertHost(String id, String accountName, String hostName,String hostGroupName,int volumes,String connVolumes,double provisionedSize) throws Exception
{
	ObjStore<HostInventoryConfig> store = ObjStoreHelper.getStore(HostInventoryConfig.class);
	 
	logger.info("Adding Host "+ id);
    
	HostInventoryConfig hostConfig = new HostInventoryConfig();
	hostConfig.setId(id);
	hostConfig.setAccountName(accountName);
    hostConfig.setHost(hostName); // Name
    hostConfig.setHostGroup(hostGroupName); //HostGroup
    hostConfig.setVolumes(volumes); // Number of volumes
    hostConfig.setConnectedVolumes(connVolumes);
    hostConfig.setProvisionedSize(provisionedSize); // Provisioned size of attached volumes
    
   // ObjStore<NewHostTaskConfig> store = ObjStoreHelper.getStore(NewHostTaskConfig.class);
    store.insert(hostConfig);

    
    
}

public static void insertHostGroup(String id, String accountName, String hostName,String hostGroupName,int hosts,int volumes,String connVolumes,double provisionedSize,double volCapacity, double reduction,String hostsList) throws Exception
{
	ObjStore<HostGroupInventoryConfig> store = ObjStoreHelper.getStore(HostGroupInventoryConfig.class);
	   
    
	
	HostGroupInventoryConfig hostGroupConfig = new HostGroupInventoryConfig();
	hostGroupConfig.setId(id);
	hostGroupConfig.setAccountName(accountName);
	hostGroupConfig.setHostGroupName(hostGroupName); // Name
	hostGroupConfig.setHosts(hosts); //No. of Hosts
     hostGroupConfig.setVolumes(volumes); // Number of volumes
     
     hostGroupConfig.setConnectedVolumes(connVolumes);
     hostGroupConfig.setProvisionedSize(provisionedSize); // Provisioned size of attached volumes
     hostGroupConfig.setVolumeCapacity(volCapacity); // Provisioned size of attached volumes
     hostGroupConfig.setReduction(reduction); // Provisioned size of attached volumes
 	hostGroupConfig.setHostsList(hostsList);
         // ObjStore<NewHostTaskConfig> store = ObjStoreHelper.getStore(NewHostTaskConfig.class);
    store.insert(hostGroupConfig);
    
    
}

public static void modifyHost(String id, String accountName, String hostName,String hostGroupName) throws Exception
{
	 ObjStore<HostInventoryConfig> store2 = ObjStoreHelper.getStore(HostInventoryConfig.class);
     
	 
     String query3 = "id == '" + id + "'";
     List<HostInventoryConfig> hconfig = store2.query(query3);
     HostInventoryConfig hostcon = hconfig.get(0);
	 
     logger.info("Hosts Id :"+ hostcon.getId()+hconfig.size());
     hostcon.setHostGroup(hostGroupName);
     
     store2.modifySingleObject("hostGroup == '" + hostGroupName + "' ",  hostcon);
     String query4 = "id == '" + id + "'";
     List<HostInventoryConfig> hconfig1 = store2.query(query4);
     logger.info("Hosts Id :"+ hconfig1.get(0).getId()+hconfig1.size());
     logger.info("Connected HostGroup :"+ hconfig1.get(0).getHostGroup());

    
    
}

public static void modifyHostGroup(String id, String accountName, String hostName,String hostGroupName,int hosts,int volumes,String connVolumes,double provisionedSize,double volCapacity, double reduction,String hostsList) throws Exception
{
	ObjStore<HostGroupInventoryConfig> store = ObjStoreHelper.getStore(HostGroupInventoryConfig.class);
	   
    
	
	HostGroupInventoryConfig hostGroupConfig = new HostGroupInventoryConfig();
	hostGroupConfig.setId(id);
	hostGroupConfig.setAccountName(accountName);
	hostGroupConfig.setHostGroupName(hostGroupName); // Name
	hostGroupConfig.setHosts(hosts); //No. of Hosts
     hostGroupConfig.setVolumes(volumes); // Number of volumes
     
     hostGroupConfig.setConnectedVolumes(connVolumes);
     hostGroupConfig.setProvisionedSize(provisionedSize); // Provisioned size of attached volumes
     hostGroupConfig.setVolumeCapacity(volCapacity); // Provisioned size of attached volumes
     hostGroupConfig.setReduction(reduction); // Provisioned size of attached volumes
 	hostGroupConfig.setHostsList(hostsList);
         // ObjStore<NewHostTaskConfig> store = ObjStoreHelper.getStore(NewHostTaskConfig.class);
    store.insert(hostGroupConfig);
    
    
}

public static void modifyVolume(String id, String accountName, String volumeName,double size,String source,long creation) throws Exception
{
	ObjStore<VolumeInventoryConfig> store = ObjStoreHelper.getStore(VolumeInventoryConfig.class);
	   
    
    	VolumeInventoryConfig volumeConfig = new VolumeInventoryConfig();
    	volumeConfig.setId(id);
    	volumeConfig.setAccountName(accountName);
    	volumeConfig.setVolumeName(volumeName);
    	volumeConfig.setSize(size); // Name
        volumeConfig.setSource(source); //HostGroup
        volumeConfig.setCreation(creation);
             // ObjStore<NewHostTaskConfig> store = ObjStoreHelper.getStore(NewHostTaskConfig.class);
        store.insert(volumeConfig);
    
    
}

public static void deleteHost(String id) throws Exception
{
	  ObjStore<HostInventoryConfig> store2 = ObjStoreHelper.getStore(HostInventoryConfig.class);
	   
      String query3 = "id == '" + id + "'";
      List<HostInventoryConfig> hconfig = store2.query(query3);
      logger.info("Host Id :"+ hconfig.get(0).getId());
  
      long s = store2.delete(query3);
      logger.info("Deleted in Inventory :" + s);

       
}

}
