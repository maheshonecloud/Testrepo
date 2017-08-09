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

public class DestroyVolume extends CloupiaPageAction {
	private static Logger logger = Logger.getLogger(DestroyVolume.class);
	
	private static final String formId = "psucs.destroy.volume.form";
	private static final String ACTION_ID = "psucs.destroy.volume.action";
	private static final String label = "Destroy Volume";

	@Override
	public void definePage(Page page, ReportContext context) {
		// TODO Auto-generated method stub
		
		page.bind(formId, DestroyVolumeForm.class);
		page.addLabel("Confirm to delete the Volume '"+ context.getId());
	}

	@Override
	public void loadDataToPage(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
DestroyVolumeForm form = (DestroyVolumeForm) page.unmarshallToSession(formId);
		
		
		
		
		page.marshallFromSession(formId);
	}

	@Override
	public int validatePageData(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		 String accountName="",volumeName="";
		 
		 DestroyVolumeForm config = (DestroyVolumeForm) page.unmarshallToSession(formId);
			
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
				 Boolean eradicate = config.isEradicate();
			        
			        List<String> volumeNameList = new ArrayList<String>();
			        StringBuilder hostConnection = new StringBuilder();
			        StringBuilder hostGroupConnection = new StringBuilder();
			        PureRestClient CLIENT = null;
			        logger.info("finished checking NewHostTask accoutname");
				       FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
				       CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);
				try
	            {
					
	                List<PureVolumeConnection> connectedHost = CLIENT.volumes().getHostConnections(volumeName);
	                List<PureVolumeConnection> connectedHostGroup = CLIENT.volumes().getHostGroupConnections(volumeName);
	                if(connectedHost.size()>0)
	                {
	                    hostConnection.append(volumeName + ":");
	                    for(PureVolumeConnection host : connectedHost)
	                    {
	                        hostConnection.append(host.getHost() + ",");
	                    }
	                    hostConnection.append("!");
	                }
	                if(connectedHostGroup.size()>0)
	                {
	                    hostGroupConnection.append(volumeName + ":");
	                    for(PureVolumeConnection hostGroup : connectedHostGroup)
	                    {
	                        hostGroupConnection.append(hostGroup.getHostGroupName() + ",");
	                    }
	                    hostGroupConnection.append("!");
	                }
	            }
	            catch (PureException e)
	            {
	                logger.info("Error happens when trying to get host connection and host group connection with volume "
	                + volumeName + e.getMessage());
	            }
				
				destroyVolume(volumeName, CLIENT, eradicate);
                logger.info("Destroying Volume : " + volumeName);
                
                ObjStore<VolumeInventoryConfig> store2 = ObjStoreHelper.getStore(VolumeInventoryConfig.class);
           	   
                String query3 = "id == '" + accountName+"@"+volumeName + "'";
                List<VolumeInventoryConfig> hconfig = store2.query(query3);
                logger.info("Volume Id :"+ hconfig.get(0).getId());
            
                long s = store2.delete(query3);
                logger.info("Deleted in Inventory :" + s);
                String description="FlashArray Volume is Deleted. Details are : Account Name = "+accountName+" , Volume Name = "+ volumeName;
                
                UcsdCmdbUtils.updateRecord("FlashArray Volume", description, 2, "admin", volumeName,description);
                
                
                page.setPageMessage("Successfully destroyed volume " + volumeName+
		                   " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
				
				
	        	return PageIf.STATUS_OK;
	        }catch(Exception ex){
	        	page.setPageMessage("Error: "+ex.getMessage());
	        	return PageIf.STATUS_ERROR;
	        }
        }
		return PageIf.STATUS_OK;
	}
	
	 public void destroyVolume(String volumeName, PureRestClient CLIENT, Boolean eradicate)
	    {
	        List<PureVolumeConnection> connectedHostGroup = CLIENT.volumes().getHostGroupConnections(volumeName);
	        List<PureVolumeConnection> connectedHost = CLIENT.volumes().getHostConnections(volumeName);
	        List<String> hostGroupList = new ArrayList<String>();

	        for(PureVolumeConnection hostGroupVolumeConnection : connectedHostGroup)
	        {
	            String hostGroupName = hostGroupVolumeConnection.getHostGroupName();
	            if(!hostGroupList.contains(hostGroupName))
	            {
	                hostGroupList.add(hostGroupName);
	            }
	        }

	        for(String hgName : hostGroupList)
	        {
	            CLIENT.hostGroups().disconnectVolume(hgName,volumeName);
	        }

	        for(PureVolumeConnection hostVolumeConnection : connectedHost)
	        {
	            String hostName = hostVolumeConnection.getHost();
	            CLIENT.hosts().disconnectVolume(hostName,volumeName);
	        }

	        CLIENT.volumes().destroy(volumeName);
	        if(eradicate==true)
	        {
	            CLIENT.volumes().destroy(volumeName, eradicate);
	        }
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
