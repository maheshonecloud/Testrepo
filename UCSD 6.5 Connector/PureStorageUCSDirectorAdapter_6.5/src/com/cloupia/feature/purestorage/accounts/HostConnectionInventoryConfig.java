package com.cloupia.feature.purestorage.accounts;



import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.apache.log4j.Logger;

@PersistenceCapable(detachable = "true", table = "host_connection_inventory_config")
public class HostConnectionInventoryConfig
{

    static Logger logger = Logger.getLogger(HostConnectionInventoryConfig.class);

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
    private String hostName;
    
    @Persistent
    private String initiatorIqn;
    
    @Persistent
    private String initiatorWwpn;
    
    @Persistent
    private String targetIqn;
    
    @Persistent
    private String targetWwpn;
    
    @Persistent
    private String connectivity;
    
    @Persistent
    private String targetInterfaces;
    
    @Persistent
    private String targetPortal;
    
    @Persistent
    private String iniatorPortal;

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getInitiatorIqn() {
		return initiatorIqn;
	}

	public void setInitiatorIqn(String initiatorIqn) {
		this.initiatorIqn = initiatorIqn;
	}

	public String getInitiatorWwpn() {
		return initiatorWwpn;
	}

	public void setInitiatorWwpn(String initiatorWwpn) {
		this.initiatorWwpn = initiatorWwpn;
	}

	public String getTargetIqn() {
		return targetIqn;
	}

	public void setTargetIqn(String targetIqn) {
		this.targetIqn = targetIqn;
	}

	public String getTargetWwpn() {
		return targetWwpn;
	}

	public void setTargetWwpn(String targetWwpn) {
		this.targetWwpn = targetWwpn;
	}

	public String getConnectivity() {
		return connectivity;
	}

	public void setConnectivity(String connectivity) {
		this.connectivity = connectivity;
	}

	public String getTargetInterfaces() {
		return targetInterfaces;
	}

	public void setTargetInterfaces(String targetInterfaces) {
		this.targetInterfaces = targetInterfaces;
	}

	public String getTargetPortal() {
		return targetPortal;
	}

	public void setTargetPortal(String targetPortal) {
		this.targetPortal = targetPortal;
	}

	public String getIniatorPortal() {
		return iniatorPortal;
	}

	public void setIniatorPortal(String iniatorPortal) {
		this.iniatorPortal = iniatorPortal;
	}
    
    
   
   
  }
