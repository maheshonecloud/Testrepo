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

@PersistenceCapable(detachable = "true", table = "space_inventory_config")
public class SpaceInventoryConfig
{

    static Logger logger = Logger.getLogger(SpaceInventoryConfig.class);

    // ManagementAddress
    

    @Persistent
    private String sharedSpace;
    
    @Persistent
    private String accountName;
    
    public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	@Persistent
    private String freeSpace;
    
    @Persistent
    private String systemSpace;
    
    @Persistent
    private String volumeSpace;
    
    @Persistent
    private String snapshotSpace;

	public String getSharedSpace() {
		return sharedSpace;
	}

	public void setSharedSpace(String sharedSpace) {
		this.sharedSpace = sharedSpace;
	}

	public String getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(String freeSpace) {
		this.freeSpace = freeSpace;
	}

	public String getSystemSpace() {
		return systemSpace;
	}

	public void setSystemSpace(String systemSpace) {
		this.systemSpace = systemSpace;
	}

	public String getVolumeSpace() {
		return volumeSpace;
	}

	public void setVolumeSpace(String volumeSpace) {
		this.volumeSpace = volumeSpace;
	}

	public String getSnapshotSpace() {
		return snapshotSpace;
	}

	public void setSnapshotSpace(String snapshotSpace) {
		this.snapshotSpace = snapshotSpace;
	}

	
    
    
   
   
  }
