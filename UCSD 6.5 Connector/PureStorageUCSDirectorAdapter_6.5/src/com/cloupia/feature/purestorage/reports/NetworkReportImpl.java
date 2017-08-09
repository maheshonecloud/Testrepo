package com.cloupia.feature.purestorage.reports;

import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.accounts.FcPortInventoryConfig;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.HostInventoryConfig;
import com.cloupia.feature.purestorage.accounts.NetworkInventoryConfig;
import com.cloupia.model.cIM.Group;
import com.cloupia.model.cIM.ReportContext;
import com.cloupia.model.cIM.TabularReport;
import com.cloupia.service.cIM.inframgr.TabularReportGeneratorIf;
import com.cloupia.service.cIM.inframgr.reportengine.ReportRegistryEntry;
import com.cloupia.service.cIM.inframgr.reports.TabularReportInternalModel;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.port.PureArrayPort;
import com.purestorage.rest.volume.PureVolume;
import com.purestorage.rest.PureRestClient;

import java.util.ArrayList;
import java.util.List;

public class NetworkReportImpl implements TabularReportGeneratorIf
{
    static Logger logger = Logger.getLogger(NetworkReportImpl.class);

    @Override
    public TabularReport getTabularReportReport(ReportRegistryEntry entry, ReportContext context) throws Exception
    {
        logger.info("Entering NetworkReportImpl.getTabularReportReport" );
        logger.info("ReportContext.getId()=" + context.getId());
        String accountName;
        if(context.getId().contains(";"))   //Checking the Context 
        {
        	 String[] parts = context.getId().split(";");
             accountName = parts[0];
           	
        }
        else
        {
           accountName = context.getId();
        }
        TabularReport report = new TabularReport();
        report.setGeneratedTime(System.currentTimeMillis());
        report.setReportName(entry.getReportLabel());
        report.setContext(context);

        TabularReportInternalModel model = new TabularReportInternalModel();
        model.addTextColumn("Id", "Id",true);
		model.addTextColumn("Account Name", "Account Name");
		model.addTextColumn("Port Name", "Port Name");
		model.addTextColumn("IP Address", "IP Address");
		model.addTextColumn("Gateway", "Gateway");
		model.addLongNumberColumn("MTU", "MTU");
		model.addTextColumn("Services", "Services");
		
		model.completedHeader();

        if (accountName != null && accountName.length() > 0)
        {
        	List<NetworkInventoryConfig> networks= PureUtils.getAllPureNetworks();
            for (NetworkInventoryConfig network: networks)
            {
            	 if (accountName.equalsIgnoreCase(network.getAccountName()))
                 {
            		 model.addTextValue(network.getId());
	                model.addTextValue(accountName);	
	                model.addTextValue(network.getPortName());	
	    			model.addTextValue(network.getIpaddress());
	    			model.addTextValue(network.getGateway());
	    			model.addLongNumberValue(network.getMtu());
	    			
	    			model.addTextValue(network.getServices());
	    			model.completedRow();
                 }
			
			
			
		}

        
    }

        model.updateReport(report);
        return report;
    }
}