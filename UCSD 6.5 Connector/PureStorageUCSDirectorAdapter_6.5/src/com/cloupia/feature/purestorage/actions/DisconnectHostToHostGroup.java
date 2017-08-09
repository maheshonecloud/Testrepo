package com.cloupia.feature.purestorage.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.UcsdCmdbUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.HostGroupInventoryConfig;
import com.cloupia.feature.purestorage.accounts.HostInventoryConfig;
import com.cloupia.feature.purestorage.accounts.VolumeInventoryConfig;
import com.cloupia.feature.purestorage.actions.forms.AddHostForm;
import com.cloupia.feature.purestorage.actions.forms.AddVolumeForm;
import com.cloupia.feature.purestorage.actions.forms.ConnectHostToHostGroupForm;
import com.cloupia.feature.purestorage.actions.forms.ConnectVolumeHostForm;
import com.cloupia.feature.purestorage.actions.forms.ConnectVolumeHostGroupForm;
import com.cloupia.feature.purestorage.actions.forms.DestroyVolumeForm;
import com.cloupia.feature.purestorage.actions.forms.DisconnectHostToHostGroupForm;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.feature.purestorage.tasks.DisconnectHostHGTask;
import com.cloupia.feature.purestorage.tasks.DisconnectHostHGTaskConfig;
import com.cloupia.feature.purestorage.tasks.DisconnectVolumeHGTask;
import com.cloupia.feature.purestorage.tasks.DisconnectVolumeHGTaskConfig;
import com.cloupia.feature.purestorage.tasks.DisconnectVolumeHostTask;
import com.cloupia.feature.purestorage.tasks.DisconnectVolumeHostTaskConfig;
import com.cloupia.feature.purestorage.tasks.NewHostTask;
import com.cloupia.feature.purestorage.tasks.NewHostTaskConfig;
import com.cloupia.fw.objstore.ObjStore;
import com.cloupia.fw.objstore.ObjStoreHelper;
import com.cloupia.model.cIM.ConfigTableAction;
import com.cloupia.model.cIM.ReportContext;
import com.cloupia.service.cIM.inframgr.forms.wizard.Page;
import com.cloupia.service.cIM.inframgr.forms.wizard.PageIf;
import com.cloupia.service.cIM.inframgr.forms.wizard.WizardSession;
import com.cloupia.service.cIM.inframgr.reports.simplified.CloupiaPageAction;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.exceptions.PureException;
import com.purestorage.rest.host.PureHost;
import com.purestorage.rest.host.PureHostConnection;
import com.purestorage.rest.hostgroup.PureHostGroup;
import com.purestorage.rest.hostgroup.PureHostGroupConnection;
import com.purestorage.rest.volume.PureVolumeConnection;

public class DisconnectHostToHostGroup extends CloupiaPageAction {
	private static Logger logger = Logger.getLogger(DisconnectHostToHostGroup.class);
	
	private static final String formId = "psucs.disconnect.host.hostgroup.form";
	private static final String ACTION_ID = "psucs.disconnect.host.hostgroup.action";
	private static final String label = "Disconnect Host(s)";

	@Override
	public void definePage(Page page, ReportContext context) {
		// TODO Auto-generated method stub
		
		page.bind(formId, DisconnectHostToHostGroupForm.class);
		
	}

	@Override
	public void loadDataToPage(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		DisconnectHostToHostGroupForm form = (DisconnectHostToHostGroupForm) page.unmarshallToSession(formId);
		
		
		
		
		page.marshallFromSession(formId);

	}

	@Override
	public int validatePageData(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		 String accountName="",hostGroupName="";
		 DisconnectHostToHostGroupForm config = (DisconnectHostToHostGroupForm) page.unmarshallToSession(formId);
			
		 PureRestClient CLIENT = null;
		 if(context.getId().contains("@"))   //Checking the Context 
	        {
	        	 String[] parts = context.getId().split("@");
	             accountName = parts[0];
	             hostGroupName = parts[1];
	           	
	        }
	        else
	        {
	        	logger.error("Error in Report Generation ..Wrong Conext" + context);
	            
	        }
		 logger.info("finished checking NewHostTask accoutname");
	        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
	        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);

		 
		 	
	       
	        
	      
        
		if (page.isPageSubmitted())
        {
			try{
				String allHostName = config.getHostName();
		        
		        String[] hostNameList = allHostName.split(",");
		        //String testFlag = config.getTestFlag();
		        List<String> hostNameList1= new ArrayList<String>();

		        for(int i=0; i<hostNameList.length; i++)
		        {
		            String hostName = hostNameList[i];
		            hostNameList1.add(hostName);
		        }
		        try
		        {
		            CLIENT.hostGroups().removeHosts(hostGroupName, hostNameList1);
		        }
		        catch (PureException e)
		        {
		            logger.info("Error happens when disconnecting " + allHostName + " with host group " + hostGroupName );
		        }
		        logger.info("Successfully Disconnected host(s) to hostgroup " + hostGroupName + " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
				
		        String connHost="";
		        for(int i=0; i<hostNameList.length; i++)
		        {
		            String hostName = hostNameList[i];
		            logger.info("Host Name is : " +hostName);
		            
		            PureHost host =CLIENT.hosts().get(hostName);
		   		 String id=accountName+"@"+hostName;
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
		            PureUtils.deleteHost(id);
		            PureUtils.insertHost(id, accountName, hostName, host.getHostGroupName(),vol,connVolumes,provSize);
		            
		            if(connHost=="")
		            {
		            	
		            	connHost=accountName+"@"+hostName;	
		            }
		            else
		            {
		            	connHost=connHost+","+accountName+"@"+hostName;
		            }  
		        }

		        
		        

		       String hostGroupIdentity =accountName+"@"+hostGroupName;
				PureHostGroup hostgroup =CLIENT.hostGroups().get(hostGroupName);
				List<String> hostsList1 =hostgroup.getHosts();
		        String hostsList="";
		    	logger.info("Hosts in Hostgroup :"+ hostsList1);
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
		    	}
		    	logger.info("Hosts in Hostgroup :"+ hostsList);
		        ObjStore<HostGroupInventoryConfig> store = ObjStoreHelper.getStore(HostGroupInventoryConfig.class);
		  	   
		         String query = "id == '" + hostGroupIdentity + "'";
		         List<HostGroupInventoryConfig> hgconfig = store.query(query);
		         logger.info("Hosts Id :"+ hgconfig.get(0).getId());
		         hgconfig.get(0).setHostsList(hostsList);
		         hgconfig.get(0).setHosts(hostgroup.getHosts().size());
		         store.modifySingleObject("hostsList == '" + hostsList + "' && hosts == '" + hostgroup.getHosts().size() +"'",  hgconfig.get(0));
		         String query1 = "id == '" + hostGroupIdentity + "'";
		         List<HostGroupInventoryConfig> hgconfig1 = store.query(query1);
		         logger.info("Hosts List :"+ hgconfig1.get(0).getHostsList()+ "  "+hgconfig1.get(0).getHosts() );
		         
                page.setPageMessage("Successfully connected hosts to hostgroup " + hostGroupName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
	        
	          
				
	        	return PageIf.STATUS_OK;
	        }catch(Exception ex){
	        	page.setPageMessage("Error: "+ex.getMessage());
	        	return PageIf.STATUS_ERROR;
	        }
        }
		return PageIf.STATUS_OK;
	}
	
	 

	@Override
	public String getActionId() {
		// TODO Auto-generated method stub
		return ACTION_ID;
	}

	@Override
	public int getActionType() {
		// TODO Auto-generated method stub
		return ConfigTableAction.ACTION_TYPE_POPUP_FORM;
	}

	@Override
	public String getFormId() {
		// TODO Auto-generated method stub
		return formId;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return label;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return label;
	}

	@Override
	public boolean isDoubleClickAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDrilldownAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSelectionRequired() {
		// TODO Auto-generated method stub
		return true;
	}

	
}
