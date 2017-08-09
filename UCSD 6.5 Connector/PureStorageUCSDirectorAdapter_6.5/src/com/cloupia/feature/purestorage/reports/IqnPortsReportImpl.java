package com.cloupia.feature.purestorage.reports;

import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.accounts.FcPortInventoryConfig;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.IqnPortInventoryConfig;
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

public class IqnPortsReportImpl implements TabularReportGeneratorIf
{
    static Logger logger = Logger.getLogger(IqnPortsReportImpl.class);

    @Override
    public TabularReport getTabularReportReport(ReportRegistryEntry entry, ReportContext context) throws Exception
    {
        logger.info("Entering IQNPortsReportImpl.getTabularReportReport" );
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
        model.addTextColumn("Name", "Name");
		model.addTextColumn("IQNs", "IQNs");
		
        model.addTextColumn("Speed(GB/S)","Speed(GB/S)");
        model.completedHeader();

        if (accountName != null && accountName.length() > 0)
        {
        	List<IqnPortInventoryConfig> ports= PureUtils.getAllPureIqnPorts();
            for (IqnPortInventoryConfig port: ports)
            {
           	 if (accountName.equalsIgnoreCase(port.getAccountName()))
                {
            	
           		 	model.addTextValue(port.getId());
		            model.addTextValue(accountName);
		            model.addTextValue(port.getName());
		            model.addTextValue(port.getIqn());	
					
					model.addTextValue(port.getSpeed());
					model.completedRow();
            	}
			
			
			
		}

        
    }

        model.updateReport(report);
        return report;
    }
}