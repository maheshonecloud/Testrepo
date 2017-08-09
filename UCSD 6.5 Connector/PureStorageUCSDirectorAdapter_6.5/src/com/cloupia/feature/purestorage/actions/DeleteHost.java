package com.cloupia.feature.purestorage.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.UcsdCmdbUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.HostInventoryConfig;
import com.cloupia.feature.purestorage.accounts.VolumeInventoryConfig;
import com.cloupia.feature.purestorage.actions.forms.AddVolumeForm;
import com.cloupia.feature.purestorage.actions.forms.DestroyVolumeForm;
import com.cloupia.feature.purestorage.constants.PureConstants;
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
import com.purestorage.rest.host.PureHostConnection;
import com.purestorage.rest.volume.PureVolumeConnection;

public class DeleteHost extends CloupiaPageAction {
	private static Logger logger = Logger.getLogger(DeleteHost.class);
	
	private static final String formId = "psucs.delete.host.form";
	private static final String ACTION_ID = "psucs.delete.host.action";
	private static final String label = "Delete Host";

	@Override
	public void definePage(Page page, ReportContext context) {
		// TODO Auto-generated method stub
		
		
		page.addLabel("Confirm to delete the Host "+ context.getId());
	}

	@Override
	public void loadDataToPage(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int validatePageData(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		 String accountName="",hostName="";
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
				String hostGroupName = "";
				String wwns = "";
		        String iqns = "";
				StringBuilder privateVolumes = new StringBuilder();
		        List<String> wwnsList = CLIENT.hosts().get(hostName).getWwnList();
		        List<String> iqnsList = CLIENT.hosts().get(hostName).getIqnList();
		        List<PureHostConnection> privateConnectedVolumes = CLIENT.hosts().getPrivateConnections(hostName);
		        List<PureHostConnection> sharedConnectedVolumes = CLIENT.hosts().getSharedConnections(hostName);
		        
		        wwns = StringUtils.join(wwnsList, ",");
		        iqns = StringUtils.join(iqnsList, ",");
		        for(PureHostConnection volumeConnection : privateConnectedVolumes)
		        {
		            String volumeName = volumeConnection.getVolumeName();
		            privateVolumes.append(volumeName + ",");
		        }
		        logger.info("private volume is " + privateVolumes);

		        if(sharedConnectedVolumes.size()>0)
		        {
		            hostGroupName = sharedConnectedVolumes.get(0).getHostGroupName();
		            logger.info("hostgroup name is " + hostGroupName);
		        }

				for(PureHostConnection volumeConnection : privateConnectedVolumes)
	            {
	                String volumeName = volumeConnection.getVolumeName();
	                CLIENT.hosts().disconnectVolume(hostName, volumeName);
	            }
	            if(!hostGroupName.equals(""))
	            {
	                List<String> tempHostList  = new ArrayList<String>();
	                tempHostList.add(hostName);
	                CLIENT.hostGroups().removeHosts(hostGroupName, tempHostList);
	            }
	            CLIENT.hosts().delete(hostName);
	            logger.info("Successfully deleted host " + hostName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
	        
	           
	            String hostIdentity =accountName+"@"+hostName;
	            
	            
	        	
	        	String description="FlashArray Host is Deleted. Details are : Account Name = "+accountName+" , Host Name = "+ hostName+" , WWNS = "+wwns+" , IQNS = "+iqns;
	            
	            UcsdCmdbUtils.updateRecord("FlashArray Host", description, 2, "", hostName,description);
	            
	            
	            ObjStore<HostInventoryConfig> store2 = ObjStoreHelper.getStore(HostInventoryConfig.class);
	        	   
	            String query3 = "id == '" + accountName+"@"+hostName + "'";
	            List<HostInventoryConfig> hconfig = store2.query(query3);
	            logger.info("Host Id :"+ hconfig.get(0).getId());
	        
	            long s = store2.delete(query3);
	            logger.info("Deleted in Inventory :" + s);
                
                page.setPageMessage("Successfully deleted host " + hostName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
	        
	          
				
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
