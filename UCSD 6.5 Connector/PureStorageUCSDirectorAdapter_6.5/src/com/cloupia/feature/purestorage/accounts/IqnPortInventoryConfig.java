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

@PersistenceCapable(detachable = "true", table = "iqn_port_inventory_config")
public class IqnPortInventoryConfig
{

    static Logger logger = Logger.getLogger(IqnPortInventoryConfig.class);

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
    private String name;
    
    @Persistent
    private String iqn;
    
    public String getIqn() {
		return iqn;
	}

	public void setIqn(String iqn) {
		this.iqn = iqn;
	}

	@Persistent
    private String speed;

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}
    
    
   
   
  }
