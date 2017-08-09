package com.cloupia.feature.purestorage.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.UcsdCmdbUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.VolumeInventoryConfig;
import com.cloupia.feature.purestorage.actions.forms.AddVolumeForm;
import com.cloupia.feature.purestorage.actions.forms.DestroyVolumeForm;
import com.cloupia.feature.purestorage.actions.forms.ResizeVolumeForm;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.feature.purestorage.tasks.RollbackResizeVolumeTask;
import com.cloupia.feature.purestorage.tasks.RollbackResizeVolumeTaskConfig;
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
import com.purestorage.rest.volume.PureVolumeConnection;

public class ResizeVolume extends CloupiaPageAction {
	private static Logger logger = Logger.getLogger(ResizeVolume.class);
	
	private static final String formId = "psucs.resize.volume.form";
	private static final String ACTION_ID = "psucs.resize.volume.action";
	private static final String label = "Resize Volume";

	@Override
	public void definePage(Page page, ReportContext context) {
		// TODO Auto-generated method stub
		
		page.bind(formId, ResizeVolumeForm.class);
		page.addLabel("Confirm to resize the Volume '"+ context.getId());
	}

	@Override
	public void loadDataToPage(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		ResizeVolumeForm form = (ResizeVolumeForm) page.unmarshallToSession(formId);
		
		
		
		
		page.marshallFromSession(formId);
	}

	@Override
	public int validatePageData(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		 String accountName="",volumeName="";
		 
		 ResizeVolumeForm config = (ResizeVolumeForm) page.unmarshallToSession(formId);
			
	        if(context.getId().contains("@"))   //Checking the Context 
	        {
	        	 String[] parts = context.getId().split("@");
	             accountName = parts[0];
	             volumeName = parts[1];
	           	
	        }
	        else
	        {
	        	logger.error("Error in Report Generation ..Wrong Conext" + context);
	            
	        }
	        
	      
        
		if (page.isPageSubmitted())
        {
			try{
				PureRestClient CLIENT = null;
				FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
			       CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);
			       
			        String volumeSizeNumber = config.getVolumeSizeNumber();
			        String vSU=config.getVolumeSizeUnit();
			        int count = 0;
			        if(vSU.equals("KB")) count = 1;
			        if(vSU.equals("MB")) count = 2;
			        if(vSU.equals("GB")) count = 3;
			        if(vSU.equals("TB")) count = 4;
			        if(vSU.equals("PB")) count = 5;
			        
			        double volumeSizeUnit = Math.pow(1024, count);
			        long resetSize = (new Double(volumeSizeUnit)).longValue() * Long.parseLong(volumeSizeNumber);
			        Boolean truncate = config.isTruncate();
			        long originSize = CLIENT.volumes().get(volumeName).getSize();

			       
			        logger.info("Resizing volume " + volumeName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
			        CLIENT.volumes().resize(volumeName, resetSize, truncate);
			       
			        
			    	
			    	
			        String description="FlashArray Volume is Modified. Details are : Account Name = "+accountName+" , Volume Name = "+ volumeName+" , Volume Size = "+volumeSizeNumber+volumeSizeUnit;
			        
			        UcsdCmdbUtils.updateRecord("FlashArray Volume", description, 0, "admin", volumeName,description);
			        
			      
			        
			        ObjStore<VolumeInventoryConfig> store2 = ObjStoreHelper.getStore(VolumeInventoryConfig.class);
			  	   
			        String query3 = "id == '" + accountName+"@"+volumeName + "'";
			        List<VolumeInventoryConfig> hconfig = store2.query(query3);
			        logger.info("Volume Id :"+ hconfig.get(0).getId());
			        hconfig.get(0).setSize(resetSize);
			        store2.modifySingleObject("size == " + resetSize ,  hconfig.get(0));
			        String query4 = "id == '" + accountName+"@"+volumeName + "'";
			        List<VolumeInventoryConfig> hconfig1 = store2.query(query4);
			        logger.info("Volume Size :"+ hconfig1.get(0).getSize());
                
			        page.setPageMessage("Successfully resized volume " + volumeName+
		                   " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
				
				
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
