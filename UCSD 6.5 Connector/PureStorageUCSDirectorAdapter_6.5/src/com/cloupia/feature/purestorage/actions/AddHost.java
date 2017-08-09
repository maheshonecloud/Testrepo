package com.cloupia.feature.purestorage.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.UcsdCmdbUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.HostInventoryConfig;
import com.cloupia.feature.purestorage.accounts.VolumeInventoryConfig;
import com.cloupia.feature.purestorage.actions.forms.AddHostForm;
import com.cloupia.feature.purestorage.actions.forms.AddVolumeForm;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.feature.purestorage.tasks.DestroyVolumesTask;
import com.cloupia.feature.purestorage.tasks.DestroyVolumesTaskConfig;
import com.cloupia.fw.objstore.ObjStore;
import com.cloupia.fw.objstore.ObjStoreHelper;
import com.cloupia.model.cIM.ConfigTableAction;
import com.cloupia.model.cIM.ReportContext;
import com.cloupia.service.cIM.inframgr.forms.wizard.Page;
import com.cloupia.service.cIM.inframgr.forms.wizard.PageIf;
import com.cloupia.service.cIM.inframgr.forms.wizard.WizardSession;
import com.cloupia.service.cIM.inframgr.reports.simplified.CloupiaPageAction;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.volume.PureVolume;


public class AddHost extends CloupiaPageAction {

	private static Logger logger = Logger.getLogger(AddHost.class);
	
	private static final String formId = "psucs.add.host.form";
	private static final String ACTION_ID = "psucs.add.host.action";
	private static final String label = "Add Host";
	
	@Override
	public void definePage(Page page, ReportContext context) {
		// TODO Auto-generated method stub
		page.bind(formId, AddHostForm.class);
	}

	@Override
	public void loadDataToPage(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		AddHostForm form = (AddHostForm) page.unmarshallToSession(formId);
		
		
		
		
		page.marshallFromSession(formId);
	}

	@Override
	public int validatePageData(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		AddHostForm config = (AddHostForm) page.unmarshallToSession(formId);
		
		


		if (page.isPageSubmitted())
        {
			try{
				
				String accountName ;
				PureRestClient CLIENT = null;
				 if(context.getId().contains(";"))   //Checking the Context 
			        {
			        	 String[] parts = context.getId().split(";");
			             accountName = parts[0];
			           	
			        }
			        else
			        {
			           accountName = context.getId();
			        }
				 
		       logger.info("finished checking NewHostTask accoutname");
		       FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
		       CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);
		       String hostName = config.getHostName();
		        String wwns = config.getWwns();
		        String iqns = config.getIqns();
		        
		        logger.info("Start creating host " + hostName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
	            CLIENT.hosts().create(hostName);

	            if(!wwns.equals(""))
	            {
	                String[] wwnArray = wwns.split(",");
	                CLIENT.hosts().addWwnList(hostName, Arrays.asList(wwnArray));
	            }
	            
	            if(!iqns.equals("")){
	                String[] iqnArray = iqns.split(",");
	                CLIENT.hosts().addIqnList(hostName, Arrays.asList(iqnArray));
	            }
	            logger.info("Successfully created host " + hostName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
	           
	        	ObjStore<HostInventoryConfig> store = ObjStoreHelper.getStore(HostInventoryConfig.class);
	            
	            String hostIdentity =accountName+"@"+hostName;
	            HostInventoryConfig hostConfig = new HostInventoryConfig();
	        	hostConfig.setId(hostIdentity);
	        	   hostConfig.setAccountName(accountName);
	            hostConfig.setHost(hostName); // Name
	            hostConfig.setHostGroup("");
	            hostConfig.setVolumes(0);
	            hostConfig.setProvisionedSize(0.0);
	            hostConfig.setConnectedVolumes("");
	            store.insert(hostConfig);
	            
	            
	        	//UcsdCmdbUtils cmdb=new UcsdCmdbUtils();
	            String description="FlashArray Host is created. Details are : Account Name = "+accountName+" , Host Name = "+ hostName+" , WWNS = "+wwns+" , IQNS = "+iqns;
	            
	            UcsdCmdbUtils.updateRecord("FlashArray Host", description, 1, "", hostName,description);
	            
		      
	                page.setPageMessage("Successfully created host " + hostName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
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
		return false;
	}

}
