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
import com.cloupia.feature.purestorage.actions.forms.ConnectVolumeHostForm;
import com.cloupia.feature.purestorage.actions.forms.DestroyVolumeForm;
import com.cloupia.feature.purestorage.actions.forms.DisconnectVolumeHostForm;
import com.cloupia.feature.purestorage.actions.forms.DisconnectVolumeHostGroupForm;
import com.cloupia.feature.purestorage.constants.PureConstants;
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

public class DisconnectVolumeHostGroup extends CloupiaPageAction {
	private static Logger logger = Logger.getLogger(DisconnectVolumeHostGroup.class);
	
	private static final String formId = "psucs.disconnect.volume.hostgroup.form";
	private static final String ACTION_ID = "psucs.disconnect.volume.hostgroup.action";
	private static final String label = "Disconnect Volume(s)";

	@Override
	public void definePage(Page page, ReportContext context) {
		// TODO Auto-generated method stub
		
		page.bind(formId, DisconnectVolumeHostGroupForm.class);
		
	}

	@Override
	public void loadDataToPage(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		DisconnectVolumeHostGroupForm form = (DisconnectVolumeHostGroupForm) page.unmarshallToSession(formId);
		
		
		
		
		page.marshallFromSession(formId);

	}

	@Override
	public int validatePageData(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		 String accountName="",hostGroupName="";
		 DisconnectVolumeHostGroupForm config = (DisconnectVolumeHostGroupForm) page.unmarshallToSession(formId);
		 
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
				 String allVolumeName = config.getVolumeName();
			        
			        String[] volumeNameList = allVolumeName.split(",");
			        

			        String connVol="";
			        for(int i=0; i<volumeNameList.length; i++)
			        {
			            String volumeName = volumeNameList[i];
			            try
			            {
			                CLIENT.hostGroups().disconnectVolume(hostGroupName, volumeName);
			            }
			            catch (PureException e)
			            {
			                logger.info("Error happens when disconnecting " + volumeName + " with host group " + hostGroupName );
			            }
			            if(connVol=="")
			            {
			            	connVol=accountName+"@"+volumeName;	
			            }
			            else
			            {
			            	connVol=connVol+","+accountName+"@"+volumeName;
			            }
			        }


			        logger.info("Successfully Disconnected volume(s) to hostgroup " + hostGroupName + " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
			String hostGroupIdentity =accountName+"@"+hostGroupName;
			        
			       
			    	
			    	PureHostGroup hostgroup =CLIENT.hostGroups().get(hostGroupName);
			    	ObjStore<HostGroupInventoryConfig> store = ObjStoreHelper.getStore(HostGroupInventoryConfig.class);
			    	String query = "id == '" + hostGroupIdentity + "'";
			    	List<HostGroupInventoryConfig> hostGroupConfig = store.query(query);
			    	 
			    	
			    	List<PureHostGroupConnection> connections = CLIENT.hostGroups().getConnections(hostgroup.getName());
			        
			        // private and shared connections cannot overlap (i.e. same vol cannot be part of both shared and private connections)
			        hostGroupConfig.get(0).setVolumes(connections.size()); // Number of volumes
			        long totalSize = 0;
			        String connVolumes="";
			        for (PureHostGroupConnection connection: connections)
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
			        hostGroupConfig.get(0).setConnectedVolumes(connVolumes);
			        hostGroupConfig.get(0).setProvisionedSize(totalSize/(1024*1024*1024)); // Provisioned size of attached volumes
			        hostGroupConfig.get(0).setVolumeCapacity(CLIENT.hostGroups().getSpaceMetrics(hostgroup.getName()).getVolumes()/(1024*1024*1024)); // Provisioned size of attached volumes
			        hostGroupConfig.get(0).setReduction(CLIENT.hostGroups().getSpaceMetrics(hostgroup.getName()).getDataReduction()); // Provisioned size of attached volumes
			        double size = totalSize/(1024*1024*1024);
			        int noOfVol = connections.size();
			        double volCapacity = CLIENT.hostGroups().getSpaceMetrics(hostgroup.getName()).getVolumes()/(1024*1024*1024);
			        double reduction = CLIENT.hostGroups().getSpaceMetrics(hostgroup.getName()).getDataReduction();
			    	
			    	 
			        
			        
			        logger.info("Hosts Id :"+ hostGroupConfig.get(0).getId()+"  (Query) volumes == " + noOfVol + " && connectedVolumes == '" + connVolumes + "'"+" && provisionedSize == " + size + " && volumeCapacity == " + volCapacity +" && reduction == " + reduction);
			        if (noOfVol == 0)
			        {
			        	store.modifySingleObject("volumes == '" + noOfVol + "' && connectedVolumes == '" + connVolumes + "'"+" && provisionedSize == " + size + " && volumeCapacity == " + volCapacity +" && reduction == " + reduction ,  hostGroupConfig.get(0));
			            	
			        }
			        else
			        {
			        	store.modifySingleObject("volumes == " + noOfVol + " && connectedVolumes == '" + connVolumes + "'"+" && provisionedSize == " + size + " && volumeCapacity == " + volCapacity +" && reduction == " + reduction ,  hostGroupConfig.get(0));
			            
			        }
			        String query1 = "id == '" + hostGroupIdentity + "'";
			        List<HostGroupInventoryConfig> hgconfig1 = store.query(query1);
			        logger.info("Volumes List :"+ hgconfig1.get(0).getVolumes()+ "  "+hgconfig1.get(0).getConnectedVolumes() );
			        
			    	
                page.setPageMessage("Successfully disconnected volume to hostgroup " + hostGroupName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
	        
	          
				
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
