package com.cloupia.feature.purestorage.accounts;

import java.util.List;

import com.cisco.cuic.api.client.JSON;
import com.cloupia.fw.objstore.ObjStore;
import com.cloupia.fw.objstore.ObjStoreHelper;
import com.cloupia.lib.cIaaS.network.model.DeviceCredential;
import com.cloupia.lib.connector.account.AbstractInfraAccount;
import com.cloupia.lib.connector.account.AccountUtil;
import com.cloupia.lib.connector.account.PhysicalInfraAccount;
import com.cloupia.model.cIM.FormFieldDefinition;
import com.cloupia.model.cIM.InfraAccount;
import com.cloupia.service.cIM.inframgr.collector.view2.ConnectorCredential;
import com.cloupia.service.cIM.inframgr.forms.wizard.FormField;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.apache.log4j.Logger;

@PersistenceCapable(detachable = "true", table = "array_inventory_config")
public class ArrayInventoryConfig
{

    static Logger logger = Logger.getLogger(ArrayInventoryConfig.class);

    // ManagementAddress
    

    @Persistent
    private String accountName;
    
    @Persistent
    private long totalRawSpace;
    
    @Persistent
    private long totalProvisionedSpace;
    
    @Persistent
    private String freeSpace;
    
    @Persistent
    private String sharedSpace;
    
    @Persistent
    private String model;

    public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Persistent
    private String systemSpace;

    @Persistent
    private String volumesSpace;

    @Persistent
    private String snapshotSpace;

    @Persistent
    private double reduction;

    
    @Persistent
    private int noOfVolume;
    
    @Persistent
    private int noOfSnapshot;
    
    @Persistent
    private int noOfHost;
    
    public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public long getTotalRawSpace() {
		return totalRawSpace;
	}

	public void setTotalRawSpace(long totalRawSpace) {
		this.totalRawSpace = totalRawSpace;
	}

	public long getTotalProvisionedSpace() {
		return totalProvisionedSpace;
	}

	public void setTotalProvisionedSpace(long totalProvisionedSpace) {
		this.totalProvisionedSpace = totalProvisionedSpace;
	}

	public String getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(String freeSpace) {
		this.freeSpace = freeSpace;
	}

	public String getSharedSpace() {
		return sharedSpace;
	}

	public void setSharedSpace(String sharedSpace) {
		this.sharedSpace = sharedSpace;
	}

	public String getSystemSpace() {
		return systemSpace;
	}

	public void setSystemSpace(String systemSpace) {
		this.systemSpace = systemSpace;
	}

	public String getVolumesSpace() {
		return volumesSpace;
	}

	public void setVolumesSpace(String volumesSpace) {
		this.volumesSpace = volumesSpace;
	}

	public String getSnapshotSpace() {
		return snapshotSpace;
	}

	public void setSnapshotSpace(String snapshotSpace) {
		this.snapshotSpace = snapshotSpace;
	}

	public double getReduction() {
		return reduction;
	}

	public void setReduction(double reduction) {
		this.reduction = reduction;
	}

	public int getNoOfVolume() {
		return noOfVolume;
	}

	public void setNoOfVolume(int noOfVolume) {
		this.noOfVolume = noOfVolume;
	}

	public int getNoOfSnapshot() {
		return noOfSnapshot;
	}

	public void setNoOfSnapshot(int noOfSnapshot) {
		this.noOfSnapshot = noOfSnapshot;
	}

	public int getNoOfHost() {
		return noOfHost;
	}

	public void setNoOfHost(int noOfHost) {
		this.noOfHost = noOfHost;
	}

	public int getNoOfHostGroup() {
		return noOfHostGroup;
	}

	public void setNoOfHostGroup(int noOfHostGroup) {
		this.noOfHostGroup = noOfHostGroup;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Persistent
    private int noOfHostGroup;
    
    @Persistent
    private String ipaddress;
    
    @Persistent
    private String version;

    

    
    
    
   
   
  }
