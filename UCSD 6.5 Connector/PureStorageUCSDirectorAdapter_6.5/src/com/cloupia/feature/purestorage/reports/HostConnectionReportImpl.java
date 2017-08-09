package com.cloupia.feature.purestorage.reports;

import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.HostConnectionInventoryConfig;
import com.cloupia.feature.purestorage.accounts.VolumeInventoryConfig;
import com.cloupia.model.cIM.Group;
import com.cloupia.model.cIM.ReportContext;
import com.cloupia.model.cIM.TabularReport;
import com.cloupia.service.cIM.inframgr.TabularReportGeneratorIf;
import com.cloupia.service.cIM.inframgr.reportengine.ReportRegistryEntry;
import com.cloupia.service.cIM.inframgr.reports.TabularReportInternalModel;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.host.PureHost;
import com.purestorage.rest.port.PureArrayInitiatorPort;
import com.purestorage.rest.port.PureArrayPort;
import com.purestorage.rest.volume.PureVolume;
import com.purestorage.rest.PureRestClient;

import java.util.ArrayList;
import java.util.List;

public class HostConnectionReportImpl implements TabularReportGeneratorIf
{
    static Logger logger = Logger.getLogger(HostConnectionReportImpl.class);

    @Override
    public TabularReport getTabularReportReport(ReportRegistryEntry entry, ReportContext context) throws Exception
    {
        logger.info("Entering HCReportImpl.getTabularReportReport" );
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
        model.addTextColumn("Host Name", "Host Name");
        model.addTextColumn("Initiator WWPN", "Initiator WWPN");
		
		model.addTextColumn("Initiator IQN", "Initiator IQN");
		model.addTextColumn("Connectivity", "Connectivity");
		model.addTextColumn("Target Interface(s)", "Target Interface(s)");
		
		model.addTextColumn("Target WWPN(s)", "Target WWPN(s)",true);
		model.addTextColumn("Target IQN(s)", "Target IQN(s)",true);
		model.addTextColumn("Initiator Portal", "Initiator Portal",true);
		model.addTextColumn("Target Portal", "Target Portal",true);
		
        
        model.completedHeader();

        if (accountName != null && accountName.length() > 0)
        {
        	 List<HostConnectionInventoryConfig> hosts= PureUtils.getAllPureHostConnections();
             for (HostConnectionInventoryConfig host: hosts)
             {
             	 if (accountName.equalsIgnoreCase(host.getAccountName()))
                  {
             		
             		model.addTextValue(host.getId());
            		model.addTextValue(accountName);
                    model.addTextValue(host.getHostName());
                    model.addTextValue(host.getInitiatorWwpn());	
                    model.addTextValue(host.getInitiatorIqn());
                    model.addTextValue(host.getConnectivity());
                    
                    model.addTextValue(host.getTargetInterfaces());
                    model.addTextValue(host.getTargetWwpn());
                    model.addTextValue(host.getTargetIqn());
                    model.addTextValue(host.getIniatorPortal());
        			
        			model.addTextValue(host.getTargetPortal());
        			model.completedRow();
            		
            	}
            	            	
            }
                    
    }

        model.updateReport(report);
        return report;
    }
    
}