package com.cloupia.feature.purestorage.actions;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.cloupia.feature.purestorage.actions.forms.RenameHostForm;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.feature.purestorage.tasks.DestroyVolumesTask;
import com.cloupia.feature.purestorage.tasks.DestroyVolumesTaskConfig;
import com.cloupia.feature.purestorage.tasks.DisconnectHostHGTask;
import com.cloupia.feature.purestorage.tasks.DisconnectHostHGTaskConfig;
import com.cloupia.fw.objstore.ObjStore;
import com.cloupia.fw.objstore.ObjStoreHelper;
import com.cloupia.model.cIM.ConfigTableAction;
import com.cloupia.model.cIM.ReportContext;
import com.cloupia.service.cIM.inframgr.forms.wizard.Page;
import com.cloupia.service.cIM.inframgr.forms.wizard.PageIf;
import com.cloupia.service.cIM.inframgr.forms.wizard.WizardSession;
import com.cloupia.service.cIM.inframgr.reports.simplified.CloupiaPageAction;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.host.PureHost;
import com.purestorage.rest.host.PureHostConnection;
import com.purestorage.rest.hostgroup.PureHostGroup;
import com.purestorage.rest.volume.PureVolume;


public class RenameHost extends CloupiaPageAction {

	private static Logger logger = Logger.getLogger(RenameHost.class);
	
	private static final String formId = "psucs.rename.host.form";
	private static final String ACTION_ID = "psucs.rename.host.action";
	private static final String label = "Rename Host";
	
	@Override
	public void definePage(Page page, ReportContext context) {
		// TODO Auto-generated method stub
		page.bind(formId, RenameHostForm.class);
		page.addLabel("Confirm to rename the Host "+ context.getId());
	}

	@Override
	public void loadDataToPage(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		RenameHostForm form = (RenameHostForm) page.unmarshallToSession(formId);
		
		
		
		
		page.marshallFromSession(formId);
	}

	@Override
	public int validatePageData(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		RenameHostForm config = (RenameHostForm) page.unmarshallToSession(formId);
		
		
		String accountName="",hostName="";
		String newHostName=config.getHostName();
		 PureRestClient CLIENT = null;
		 if(context.getId().contains("@"))   //Checking the Context 
	        {
	        	 String[] parts = context.getId().split("@");
	             accountName = parts[0];
	             hostName = parts[1];
	           	
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
				
				String hostGroupName=CLIENT.hosts().get(hostName).getHostGroupName();
	            CLIENT.hosts().rename(hostName, newHostName);
	            logger.info("Successfully deleted host " + hostName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
	        
	           
	            String hostIdentity =accountName+"@"+newHostName;
	            
	            
	        	
	        	String description="FlashArray Host is Deleted. Details are : Account Name = "+accountName+" , Host Name = "+ hostName;
	            
	            //UcsdCmdbUtils.updateRecord("FlashArray Host", description, 1, "", hostName,description);
	            
	            ObjStore<HostInventoryConfig> store2 = ObjStoreHelper.getStore(HostInventoryConfig.class);
	            
	            String query3 = "id == '" + accountName+"@"+hostName + "'";
	            List<HostInventoryConfig> hconfig = store2.query(query3);
	            logger.info("Hosts Id :"+ hconfig.get(0).getId());
	            hconfig.get(0).setHost(newHostName);
	            hconfig.get(0).setId(hostIdentity);
	            
	            store2.modifySingleObject("id == '" + hostIdentity + "'  && host == '" + newHostName+"'",  hconfig.get(0));
	            String query4 = "id == '" + accountName+"@"+newHostName + "'";
	            List<HostInventoryConfig> hconfig1 = store2.query(query4);
	            logger.info("Hosts Id :"+ hconfig1.get(0).getId());
	            logger.info("New Host Name :"+ hconfig1.get(0).getHost());
               
               page.setPageMessage("Successfully renamed host " + hostName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
	         if(hostGroupName != null && !hostGroupName.isEmpty())
	         {
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
	              logger.info("Hosts Id :"+ hgconfig.get(0).getId()+" "+hgconfig.size());
	              hgconfig.get(0).setHostsList(hostsList);
	              hgconfig.get(0).setHosts(hostgroup.getHosts().size());
	              store.modifySingleObject("hostsList == '" + hostsList + "' && hosts == '" + hostgroup.getHosts().size()+"'" ,  hgconfig.get(0));
	              String query1 = "id == '" + hostGroupIdentity + "'";
	              List<HostGroupInventoryConfig> hgconfig1 = store.query(query1);
	              logger.info("Hosts Id :"+ hgconfig1.get(0).getId()+" "+hgconfig1.size());
	              
	              logger.info("Hosts List :"+ hgconfig1.get(0).getHostsList()+ "  "+hgconfig1.get(0).getHosts() );
	              
	         }
	          
				
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
