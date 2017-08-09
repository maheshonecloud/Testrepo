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
import com.cloupia.feature.purestorage.actions.forms.AddHostForm;
import com.cloupia.feature.purestorage.actions.forms.AddVolumeForm;
import com.cloupia.feature.purestorage.actions.forms.ConnectVolumeHostForm;
import com.cloupia.feature.purestorage.actions.forms.DestroyVolumeForm;
import com.cloupia.feature.purestorage.actions.forms.DisconnectVolumeHostForm;
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
import com.purestorage.rest.volume.PureVolumeConnection;

public class DisconnectVolumeHost extends CloupiaPageAction {
	private static Logger logger = Logger.getLogger(DisconnectVolumeHost.class);
	
	private static final String formId = "psucs.disconnect.volume.host.form";
	private static final String ACTION_ID = "psucs.disconnect.volume.host.action";
	private static final String label = "Disconnect Volume(s)";

	@Override
	public void definePage(Page page, ReportContext context) {
		// TODO Auto-generated method stub
		
		page.bind(formId, DisconnectVolumeHostForm.class);
		
	}

	@Override
	public void loadDataToPage(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		DisconnectVolumeHostForm form = (DisconnectVolumeHostForm) page.unmarshallToSession(formId);
		
		
		
		
		page.marshallFromSession(formId);

	}

	@Override
	public int validatePageData(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		 String accountName="",hostName="";
		 DisconnectVolumeHostForm config = (DisconnectVolumeHostForm) page.unmarshallToSession(formId);
		 
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
				 String allVolumeName = config.getVolumeName();
			        
			        String[] volumeNameList = allVolumeName.split(",");
			        logger.info("Starting disconnecting volume(s) from host " + hostName +
			                " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
			        String connVol="";
			        try
			        {
			            for(int i=0; i<volumeNameList.length; i++)
			            {
			                String volumeName = volumeNameList[i];
			                CLIENT.hosts().disconnectVolume(hostName, volumeName);
			                logger.info("Host Name is : " +hostName);
			                
			                
			                if(connVol=="")
			                {
			                	connVol=accountName+"@"+volumeName;	
			                }
			                else
			                {
			                	connVol=connVol+","+accountName+"@"+volumeName;
			                }
			                
			            }
			        }
			        catch (PureException e)
			        {
			            logger.info("Error happened when disconnect volume with volumes" + "Exception: " + e.getMessage());
			        }

			        logger.info("Successfully Disconnected volumes with host " + hostName + " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
					String hostIdentity =accountName+"@"+hostName;
					PureHost host = CLIENT.hosts().get(hostName);
			       
			        ObjStore<HostInventoryConfig> store2 = ObjStoreHelper.getStore(HostInventoryConfig.class);
			 	   
			        String query3 = "id == '" + accountName+"@"+hostName + "'";
			        List<HostInventoryConfig> hostConfig = store2.query(query3);
			        logger.info("Hosts Id :"+ hostConfig.get(0).getId());
			        List<PureHostConnection> privateConnections = CLIENT.hosts().getPrivateConnections(host.getName());
			        List<PureHostConnection> sharedConnections = CLIENT.hosts().getSharedConnections(host.getName());
			        // private and shared connections cannot overlap (i.e. same vol cannot be part of both shared and private connections)
			        hostConfig.get(0).setVolumes(privateConnections.size() + sharedConnections.size()); // Number of volumes
			        int noOfVol = privateConnections.size() + sharedConnections.size();
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
			        hostConfig.get(0).setConnectedVolumes(connVolumes);
			        hostConfig.get(0).setProvisionedSize(totalSize/(1024*1024*1024)); 
			        double size = totalSize/(1024*1024*1024);
			        
			        if(noOfVol == 0)
			        {
			        	store2.modifySingleObject("volumes == '" + noOfVol + "' && connectedVolumes == '" + connVolumes + "'"+" && provisionedSize == " + size ,  hostConfig.get(0));
			            	
			        }
			        else
			        {
			        store2.modifySingleObject("volumes == " + noOfVol + " && connectedVolumes == '" + connVolumes + "'"+" && provisionedSize == " + size ,  hostConfig.get(0));
			        }
			        
			        String query4 = "id == '" + accountName+"@"+hostName + "'";
			        List<HostInventoryConfig> hconfig1 = store2.query(query4);
			        logger.info("Connected Volumes are :"+ hconfig1.get(0).getConnectedVolumes());
			    	
                page.setPageMessage("Successfully disconnected volume to host " + hostName + "on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
	        
	          
				
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
