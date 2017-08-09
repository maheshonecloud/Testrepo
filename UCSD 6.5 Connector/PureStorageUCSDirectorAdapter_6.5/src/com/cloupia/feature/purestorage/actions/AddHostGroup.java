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
import com.cloupia.feature.purestorage.actions.forms.AddHostGroupForm;
import com.cloupia.feature.purestorage.actions.forms.AddVolumeForm;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.feature.purestorage.tasks.DeleteHGSuffixRangeTask;
import com.cloupia.feature.purestorage.tasks.DeleteHGSuffixRangeTaskConfig;
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
import com.purestorage.rest.hostgroup.PureHostGroup;
import com.purestorage.rest.volume.PureVolume;


public class AddHostGroup extends CloupiaPageAction {

	private static Logger logger = Logger.getLogger(AddHostGroup.class);
	
	private static final String formId = "psucs.add.hostgroup.form";
	private static final String ACTION_ID = "psucs.add.hostgroup.action";
	private static final String label = "Add HostGroup";
	
	@Override
	public void definePage(Page page, ReportContext context) {
		// TODO Auto-generated method stub
		page.bind(formId, AddHostGroupForm.class);
	}

	@Override
	public void loadDataToPage(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		AddHostGroupForm form = (AddHostGroupForm) page.unmarshallToSession(formId);
		
		
		
		
		page.marshallFromSession(formId);
	}

	@Override
	public int validatePageData(Page page, ReportContext context, WizardSession session) throws Exception {
		// TODO Auto-generated method stub
		AddHostGroupForm config = (AddHostGroupForm) page.unmarshallToSession(formId);
		
		


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
		       String hostGroupPreName = config.getHostGroupPreName();
		        String startNumber = config.getStartNumber();
		        String endNumber = config.getEndNumber();
		       List<PureHostGroup> allHostGroup = CLIENT.hostGroups().list();
		        List<String> allHostGroupName = new ArrayList<String>();
		        List<String> noRollBackHostGroupName = new ArrayList<String>();
		        logger.info("finished "+startNumber+endNumber);
		        if(startNumber == null )
		        {
		        	startNumber="";
		        	
		        }
		        if( endNumber == null)
		        {
		        	
		        	endNumber="";
		        }

		        for(PureHostGroup oneHostGroup : allHostGroup)
		        {
		            allHostGroupName.add(oneHostGroup.getName());
		        }

		        List<String> hostGroupNameList = new ArrayList<String>();

		        if(startNumber.equals("") && endNumber.equals(""))
		        {
		            String hostGroupName = hostGroupPreName;
		            hostGroupNameList.add(hostGroupName);
		            if(allHostGroupName != null && allHostGroupName.contains(hostGroupName))
		            {
		                noRollBackHostGroupName.add(hostGroupName);
		            }
		        }

		        else
		        {
		            if(startNumber.equals("")) startNumber = endNumber;
		            if(endNumber.equals("")) endNumber = startNumber;

		            for(int i = Integer.parseInt(startNumber);i <= Integer.parseInt(endNumber);i++)
		            {
		                String hostGroupName = hostGroupPreName + Integer.toString(i);
		                hostGroupNameList.add(hostGroupName);
		                if(allHostGroupName != null && allHostGroupName.contains(hostGroupName))
		                {
		                    noRollBackHostGroupName.add(hostGroupName);
		                }
		            }
		        }

		         String hostGroupIdentity ="",hostGroups="",hostGroup="";
		        ObjStore<HostGroupInventoryConfig> store = ObjStoreHelper.getStore(HostGroupInventoryConfig.class);
		       
		          
		            for(String hostGroupName : hostGroupNameList)
		            {
		                CLIENT.hostGroups().create(hostGroupName);
		                logger.info("Creating Hostgroup : " + hostGroupName);
		                PureHostGroup hostgroup =CLIENT.hostGroups().get(hostGroupName);
		                HostGroupInventoryConfig hostGroupConfig = new HostGroupInventoryConfig();
		            	hostGroupConfig.setId(accountName+"@"+hostgroup.getName());
		            	hostGroupConfig.setAccountName(accountName);
		            	hostGroupConfig.setHostGroupName(hostgroup.getName()); // Name
		            	hostGroupConfig.setHosts(hostgroup.getHosts().size()); //No. of Hosts
		            	 hostGroupConfig.setConnectedVolumes("");
		                 hostGroupConfig.setProvisionedSize(0.0); // Provisioned size of attached volumes
		                 hostGroupConfig.setVolumeCapacity(CLIENT.hostGroups().getSpaceMetrics(hostgroup.getName()).getVolumes()/(1024*1024*1024)); // Provisioned size of attached volumes
		                 hostGroupConfig.setReduction(CLIENT.hostGroups().getSpaceMetrics(hostgroup.getName()).getDataReduction());
		                 hostGroupConfig.setHostsList("");
		                 
		                 store.insert(hostGroupConfig);
		                if(hostGroupNameList.size()==1)
		                {
		                	hostGroup=hostGroupName;
		                	 	
		                }
		                if(hostGroups=="")
		                {
		                	hostGroups=hostGroupName;	
		                }
		                else
		                {
		                	hostGroups=hostGroups+","+hostGroupName;
		                }
		                
		                if(hostGroupIdentity=="")
		                {
		                	hostGroupIdentity=accountName+"@"+hostGroupName;	
		                }
		                else
		                {
		                	hostGroupIdentity=hostGroupIdentity+","+accountName+"@"+hostGroupName;
		                }
		            }
		            logger.info("Successfully created hostgroup(s) " + hostGroupPreName + " with range " + startNumber + "-" + endNumber +
		                    " on Pure FlashArray [" + flashArrayAccount.getManagementAddress() + "]");
		           
		            
		           
		        	logger.info("Host Group Identity as Output is saved");
		        	
		        	//UcsdCmdbUtils cmdb=new UcsdCmdbUtils();
		            String description="FlashArray HostGroup is created. Details are : Account Name = "+accountName+" , HostGroup Name = "+ hostGroupPreName+" , Start Number = "+startNumber+" , End Number = "+endNumber;
		            
		            UcsdCmdbUtils.updateRecord("FlashArray HostGroup", description, 1, "", hostGroupPreName,description);
		            
		            //context.getChangeTracker().resourceAdded("FlashArray HostGroup", hostGroupIdentity, hostGroupPreName, description);
		           
	                page.setPageMessage("Successfully created hostgroup(s) " + hostGroupPreName + " with range " + startNumber + "-" + endNumber +
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
