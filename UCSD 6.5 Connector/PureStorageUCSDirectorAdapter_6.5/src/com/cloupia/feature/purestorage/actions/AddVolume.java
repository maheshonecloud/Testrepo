package com.cloupia.feature.purestorage.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.UcsdCmdbUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.VolumeInventoryConfig;
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


public class AddVolume extends CloupiaPageAction {

	private static Logger logger = Logger.getLogger(AddVolume.class);
	
	private static final String formId = "psucs.add.volume.form";
	private static final String ACTION_ID = "psucs.add.volume.action";
	private static final String label = "Add Volume";
	
	@Override
	public void definePage(Page page, ReportContext context) {
		// TODO Auto-generated method stub
		page.bind(formId, AddVolumeForm.class);
	}

	@Override
	public void loadDataToPage(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		AddVolumeForm form = (AddVolumeForm) page.unmarshallToSession(formId);
		
		
		
		
		page.marshallFromSession(formId);
	}

	@Override
	public int validatePageData(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		AddVolumeForm config = (AddVolumeForm) page.unmarshallToSession(formId);
		
		


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

		       String volumePreName = config.getVolumePreName();
		       String startNumber = config.getStartNumber();
		       String endNumber = config.getEndNumber();
		       String volumeSizeUnit = config.getVolumeSizeUnit();
		       String volumeSizeNumber = config.getVolumeSizeNumber();
		       List<String> volumeNameList = new ArrayList<String>();
		       List<PureVolume> allVolume = CLIENT.volumes().list();
		       List<String> allVolumeName = new ArrayList<String>();
		       List<String> noRollBackVolumeName = new ArrayList<String>();

		       //logger.info("Parameters are initialized");
		       if(startNumber == null )
		       {
		       	startNumber="";
		       	
		       }
		       if( endNumber == null)
		       {
		       	
		       	endNumber="";
		       }
		       for(PureVolume oneVolume : allVolume)
		       {
		           allVolumeName.add(oneVolume.getName());
		       }
		       //logger.info("Parameters are initialized1 "+startNumber +" e "+endNumber);
		       if((startNumber.equals("") && endNumber.equals("")) || (startNumber == null && endNumber == null))
		       {
		           String volumeName = volumePreName;
		           volumeNameList.add(volumeName);
		           if(allVolumeName != null && allVolumeName.contains(volumeName))
		           {
		           	                noRollBackVolumeName.add(volumeName);
		           }
		       }

		       else
		       {
		       	logger.info("Parameters are initialized5");
		           if(startNumber.equals("")||startNumber == null) startNumber = endNumber;
		           if(endNumber.equals("")||endNumber == null) endNumber = startNumber;
		          // logger.info("Parameters are initialized6");
		           for(int i = Integer.parseInt(startNumber);i <= Integer.parseInt(endNumber);i++)
		           {
		               String volumeName = volumePreName + Integer.toString(i);
		               volumeNameList.add(volumeName);
		               if(allVolumeName != null && allVolumeName.contains(volumeName))
		               {
		               	//logger.info("Parameters are initialized7");
		                   noRollBackVolumeName.add(volumeName);
		               }
		           }
		       }
		       logger.info("Checked volume name in the volume list ");
		       
		       logger.info("Set volume task flag ");
		       
		       
		      
		      
		       
		       	//logger.info("Starting creating volume(s)### "+volumeNameList);
		       	String outVolName="";
		       	String outSer="";
		       	String connVol="";
		           for(String volumeName : volumeNameList)
		           {
		           	logger.info("Volume Name is : "+volumeName+" , Size is : "+volumeSizeNumber+volumeSizeUnit);
		           	//logger.info("Volume Name is : "+volumeName+" , Size is : "+volumeSizeNumber+volumeSizeUnit);
		           	CLIENT.volumes().create(volumeName, Long.parseLong(volumeSizeNumber), volumeSizeUnit);
		           	
		               PureVolume volume =  CLIENT.volumes().get(volumeName);
		               ObjStore<VolumeInventoryConfig> store = ObjStoreHelper.getStore(VolumeInventoryConfig.class);
		          	   
		               
		            	VolumeInventoryConfig volumeConfig = new VolumeInventoryConfig();
		            	volumeConfig.setId(accountName+"@"+volume.getName());
		            	volumeConfig.setAccountName(accountName);
		            	volumeConfig.setVolumeName(volume.getName());
		            	volumeConfig.setSize(volume.getSize()/(1024*1024*1024)); // Name
		                volumeConfig.setSource(volume.getSource()); //HostGroup
		                volumeConfig.setCreation(volume.getCreatedTimestamp().getTime());
		                     // ObjStore<NewHostTaskConfig> store = ObjStoreHelper.getStore(NewHostTaskConfig.class);
		                store.insert(volumeConfig);
		               if(volumeNameList.size()==1)
		               {
		               	String outVolName1=volume.getName();
		               
		               	
		               }
		               if(connVol=="")
		               {
		               	connVol=accountName+"@"+volumeName;	
		               }
		               else
		               {
		               	connVol=connVol+","+accountName+"@"+volumeName;
		               }
		               if(outVolName == "")
		               {
		               	outVolName=volume.getName();
		               }else
		               {
		               	outVolName=outVolName+","+volume.getName();
		               }
		               if(outSer == "")
		               {
		               	outSer=volume.getSerial();
		               }else
		               {
		               	outSer=outSer+","+volume.getSerial();
		               }
		               
		           }
		           logger.info("Successfully created volume(s) " + volumePreName + " with range " + startNumber + "-" + endNumber +
		                   " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
		           String description="FlashArray Volume is created. Details are : Account Name = "+accountName+" , Volume Name = "+ volumePreName+" , Start Number = "+startNumber+" , End Number = "+endNumber+" , Volume Size = "+volumeSizeNumber+volumeSizeUnit;
	                
	                UcsdCmdbUtils.updateRecord("FlashArray Volume", description, 1, "admin", volumePreName,description);
	                
	                //context.getChangeTracker().resourceAdded("FlashArray Volume", connVol, volumePreName, description);

	                page.setPageMessage("Successfully created volume(s) " + volumePreName + " with range " + startNumber + "-" + endNumber +
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
		return false;
	}

}
