package com.cloupia.feature.purestorage.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.PureUtils;

import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.SnapshotInventoryConfig;
import com.cloupia.feature.purestorage.actions.forms.ScheduleVolumeSnapshotForm;
import com.cloupia.fw.objstore.ObjStore;
import com.cloupia.fw.objstore.ObjStoreHelper;
import com.cloupia.model.cIM.ConfigTableAction;
import com.cloupia.model.cIM.ReportContext;
import com.cloupia.service.cIM.inframgr.forms.wizard.Page;
import com.cloupia.service.cIM.inframgr.forms.wizard.PageIf;
import com.cloupia.service.cIM.inframgr.forms.wizard.WizardSession;
import com.cloupia.service.cIM.inframgr.reports.simplified.CloupiaPageAction;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.protectiongroup.PureVolumeSnapshot;
import com.purestorage.rest.volume.PureVolumeConnection;

public class ScheduleVolumeSnapshot extends CloupiaPageAction {
	private static Logger logger = Logger.getLogger(ScheduleVolumeSnapshot.class);
	
	private static final String formId = "psucs.create.volume.snapshot.form";
	private static final String ACTION_ID = "psucs.create.volume.snapshot.action";
	private static final String label = "Create Volume Snapshot";

	@Override
	public void definePage(Page page, ReportContext context) {
		// TODO Auto-generated method stub
		
		page.bind(formId, ScheduleVolumeSnapshotForm.class);
		page.addLabel("Confirm to create the snapshot for Volume '"+ context.getId());
	}

	@Override
	public void loadDataToPage(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		ScheduleVolumeSnapshotForm form = (ScheduleVolumeSnapshotForm) page.unmarshallToSession(formId);
		
		
		
		
		page.marshallFromSession(formId);
	}

	@Override
	public int validatePageData(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		 String accountName="",volumeName="";
		 
		 ScheduleVolumeSnapshotForm config = (ScheduleVolumeSnapshotForm) page.unmarshallToSession(formId);
			
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
				 
		        logger.info("finished checking NewHostTask accoutname");
		        FlashArrayAccount flashArrayAccount = FlashArrayAccount.getFlashArrayCredential(accountName);
		        CLIENT = PureUtils.ConstructPureRestClient(flashArrayAccount);
		        
		        String suffix = config.getSuffix();
			
		        List<PureVolumeSnapshot> snapshots =  CLIENT.volumes().createSnapshots(Arrays.asList(volumeName), suffix);
                
                page.setPageMessage("Successfully scheduled volume snapshot of volume  " + volumeName+
		                   " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
                
                ObjStore<SnapshotInventoryConfig> store = ObjStoreHelper.getStore(SnapshotInventoryConfig.class);
                
                for (PureVolumeSnapshot snapshot: snapshots)
                {
                	SnapshotInventoryConfig snapConfig = new SnapshotInventoryConfig();
                	snapConfig.setId(accountName+"@"+snapshot.getName());
                	snapConfig.setAccountName(accountName);
                	snapConfig.setSnapshotName(snapshot.getName());
                	snapConfig.setSize(snapshot.getSize()/(1024*1024*1024));
                	snapConfig.setSource(snapshot.getSource());
                	snapConfig.setCreation(snapshot.getCreatedTimestamp().getTime());
                    store.insert(snapConfig);

                }
				
				
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
