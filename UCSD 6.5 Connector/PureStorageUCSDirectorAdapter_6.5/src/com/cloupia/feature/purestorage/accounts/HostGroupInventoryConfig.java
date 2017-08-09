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

@PersistenceCapable(detachable = "true", table = "host_group_inventory_config")
public class HostGroupInventoryConfig
{

    static Logger logger = Logger.getLogger(HostGroupInventoryConfig.class);

    // ManagementAddress
    @Persistent
    private String id;
    
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Persistent
    private String accountName;
    
    @Persistent
    private String hostGroupName;
    
    @Persistent
    private int hosts;
    
    @Persistent
    private int volumes;
    
    @Persistent
    private String connectedVolumes;
    

	@Persistent
    private double provisionedSize;
	
	@Persistent
    private double volumeCapacity;
	
	@Persistent
    private double reduction;
	
	@Persistent
	String hostsList;

	public String getHostsList() {
		return hostsList;
	}

	public void setHostsList(String hostsList) {
		this.hostsList = hostsList;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getHostGroupName() {
		return hostGroupName;
	}

	public void setHostGroupName(String hostGroupName) {
		this.hostGroupName = hostGroupName;
	}

	public int getHosts() {
		return hosts;
	}

	public void setHosts(int hosts) {
		this.hosts = hosts;
	}

	public int getVolumes() {
		return volumes;
	}

	public void setVolumes(int volumes) {
		this.volumes = volumes;
	}

	public String getConnectedVolumes() {
		return connectedVolumes;
	}

	public void setConnectedVolumes(String connectedVolumes) {
		this.connectedVolumes = connectedVolumes;
	}

	public double getProvisionedSize() {
		return provisionedSize;
	}

	public void setProvisionedSize(double provisionedSize) {
		this.provisionedSize = provisionedSize;
	}

	public double getVolumeCapacity() {
		return volumeCapacity;
	}

	public void setVolumeCapacity(double volumeCapacity) {
		this.volumeCapacity = volumeCapacity;
	}

	public double getReduction() {
		return reduction;
	}

	public void setReduction(double reduction) {
		this.reduction = reduction;
	}
    
    
   
   
  }
