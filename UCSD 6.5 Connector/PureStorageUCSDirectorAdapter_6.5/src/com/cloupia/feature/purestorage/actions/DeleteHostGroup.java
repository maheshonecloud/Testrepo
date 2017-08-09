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
import com.purestorage.rest.hostgroup.PureHostGroup;
import com.purestorage.rest.hostgroup.PureHostGroupConnection;
import com.purestorage.rest.volume.PureVolumeConnection;

public class DeleteHostGroup extends CloupiaPageAction {
	private static Logger logger = Logger.getLogger(DeleteHostGroup.class);
	
	private static final String formId = "psucs.delete.hostgroup.form";
	private static final String ACTION_ID = "psucs.delete.hostgroup.action";
	private static final String label = "Delete HostGroup";

	@Override
	public void definePage(Page page, ReportContext context) {
		// TODO Auto-generated method stub
		
		
		page.addLabel("Confirm to delete the HostGroup "+ context.getId());
	}

	@Override
	public void loadDataToPage(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int validatePageData(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		 String accountName="",hostGroupName="";
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
		 logger.info("finished checking New Task accoutname");
	        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
	        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);
    
        
		if (page.isPageSubmitted())
        {
			try{
				try
	            {
	                
	                    deleteHostGroup(hostGroupName, CLIENT);
	                    logger.info("deleting hostgroup" + hostGroupName);
	                    ObjStore<HostGroupInventoryConfig> store2 = ObjStoreHelper.getStore(HostGroupInventoryConfig.class);
	             	   
	                    String query3 = "id == '" + accountName+"@"+hostGroupName + "'";
	                    List<HostGroupInventoryConfig> hconfig = store2.query(query3);
	                    logger.info("Host Id :"+ hconfig.get(0).getId());
	                
	                    long s = store2.delete(query3);
	                    logger.info("Deleted in Inventory :" + s);
	                    String description="FlashArray HostGroup is Deleted. Details are : Account Name = "+accountName+" , HostGroup Name = "+ hostGroupName;
	                    
	                    UcsdCmdbUtils.updateRecord("FlashArray HostGroup", description, 2, "", hostGroupName,description);
	                    
	                
	            }
	            catch (PureException e)
	            {
	                logger.info("Error happens while deleting host group " + hostGroupName + "Exception: " + e.getMessage());
	            }
                
                page.setPageMessage("Successfully deleted hostgroup " + hostGroupName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
	        
	          
				
	        	return PageIf.STATUS_OK;
	        }catch(Exception ex){
	        	page.setPageMessage("Error: "+ex.getMessage());
	        	return PageIf.STATUS_ERROR;
	        }
        }
		return PageIf.STATUS_OK;
	}
	
	 public void deleteHostGroup(String hostGroupName, PureRestClient CLIENT)
	    {
	        PureHostGroup tempHG = CLIENT.hostGroups().get(hostGroupName);
	        List<PureHostGroupConnection> connectedVolumes = CLIENT.hostGroups().getConnections(hostGroupName);
	        List<String> connectedHosts = tempHG.getHosts();

	        CLIENT.hostGroups().removeHosts(hostGroupName, connectedHosts);

	        for(PureHostGroupConnection hgVolumeConnection : connectedVolumes)
	        {
	            String tempVolume = hgVolumeConnection.getVolumeName();
	            CLIENT.hostGroups().disconnectVolume(hostGroupName,tempVolume);
	        }

	        CLIENT.hostGroups().delete(hostGroupName);
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
