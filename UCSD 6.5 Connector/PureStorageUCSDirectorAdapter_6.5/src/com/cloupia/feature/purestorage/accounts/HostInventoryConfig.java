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

@PersistenceCapable(detachable = "true", table = "host_inventory_config")
public class HostInventoryConfig
{

    static Logger logger = Logger.getLogger(HostInventoryConfig.class);

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
    private String host;
    
    @Persistent
    private String hostGroup;
    
    @Persistent
    private int volumes;
    
    @Persistent
    private String connectedVolumes;
    
    @Persistent
    private double provisionedSize;
    
    
    public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHostGroup() {
		return hostGroup;
	}

	public void setHostGroup(String hostGroup) {
		this.hostGroup = hostGroup;
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

	
    
   
   
  }
