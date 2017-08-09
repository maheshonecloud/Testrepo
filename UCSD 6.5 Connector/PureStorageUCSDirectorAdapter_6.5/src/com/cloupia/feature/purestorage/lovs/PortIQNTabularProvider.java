package com.cloupia.feature.purestorage.lovs;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.IqnPortInventoryConfig;
import com.cloupia.feature.purestorage.reports.VolumeReport;
import com.cloupia.feature.purestorage.reports.VolumeReportImpl;
import com.cloupia.lib.connector.account.AccountUtil;
import com.cloupia.lib.connector.account.PhysicalInfraAccount;
import com.cloupia.model.cIM.FormLOVPair;
import com.cloupia.model.cIM.ReportContext;
import com.cloupia.model.cIM.TabularReport;
import com.cloupia.service.cIM.inframgr.TabularReportGeneratorIf;
import com.cloupia.service.cIM.inframgr.reportengine.ReportRegistryEntry;
import com.cloupia.service.cIM.inframgr.reports.TabularReportInternalModel;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.port.PureArrayPort;
import com.purestorage.rest.volume.PureVolume;

public class PortIQNTabularProvider implements TabularReportGeneratorIf {

	public static final String TABULAR_PROVIDER = "pure_port_iqn_tabular_provider";
	 static Logger logger = Logger.getLogger(PortIQNTabularProvider.class);
	
	

	@Override
	public TabularReport getTabularReportReport(ReportRegistryEntry reportEntry, ReportContext context) throws Exception {
		
		
        TabularReport report = new TabularReport();

		report.setGeneratedTime(System.currentTimeMillis());
		report.setReportName(reportEntry.getReportLabel());
		report.setContext(context);

		TabularReportInternalModel model = new TabularReportInternalModel();
		model.addTextColumn("Account Name", "Account Name");
		model.addTextColumn("IQNs", "WWN");
		model.addTextColumn("Name", "Name");
		model.addTextColumn("Speed(GB/S)", "Speed(GB/S)");
		
		model.completedHeader();
		List<PhysicalInfraAccount> accounts = AccountUtil.getAccountsByType("FlashArray");
        
        int i = 0;
        for (PhysicalInfraAccount account:accounts)
        {
      	  
            String accountName = account.getAccountName();
            logger.info("Found account:" + accountName);
            if (accountName != null && accountName.length() > 0)
            {
            	List<IqnPortInventoryConfig> ports= PureUtils.getAllPureIqnPorts();
                for (IqnPortInventoryConfig port: ports)
                {
                	 if (accountName.equalsIgnoreCase(port.getAccountName()))
                     {
                		 if (accountName.equalsIgnoreCase(port.getAccountName()))
                         {
		                	model.addTextValue(accountName);
				            model.addTextValue(port.getName());
				            model.addTextValue(port.getIqn());	
							
							model.addTextValue(port.getSpeed());
							model.completedRow();
                         }
                     }
    			    
                }
    			
    			
    			
    		}

            
        }
    
		

                

		model.updateReport(report);

		return report;
	}

}

