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

@PersistenceCapable(detachable = "true", table = "volume_inventory_config")
public class VolumeInventoryConfig
{

    static Logger logger = Logger.getLogger(VolumeInventoryConfig.class);

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

   
    public double getSize() {
		return size;
	}


	@Persistent
     private String accountName;

    public void setAccountName(String accountName)
    {
        this.accountName = accountName;
    }

    
    public String getAccountName()
    {
        return this.accountName;
    }

    @Persistent
    private String volumeName;

    
    public String getVolumeName()
    {
        return volumeName;
    }

    
    public void setVolumeName(String volumeName)
    {
        this.volumeName = volumeName;
    }
    
    @Persistent
    private double  size;

    
   

    
    public void setSize(double size)
    {
        this.size = size;
    }
    
    @Persistent
    private String source;

    
    public String getSource()
    {
        return source;
    }

    
    public void setSource(String source)
    {
        this.source = source;
    }
    
    @Persistent
    private long creation;

	public long getCreation() {
		return creation;
	}

	public void setCreation(long creation) {
		this.creation = creation;
	}
  }
