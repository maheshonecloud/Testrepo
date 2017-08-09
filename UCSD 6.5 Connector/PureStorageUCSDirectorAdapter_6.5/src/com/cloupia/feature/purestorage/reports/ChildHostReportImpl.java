package com.cloupia.feature.purestorage.reports;

import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.HostGroupInventoryConfig;
import com.cloupia.feature.purestorage.accounts.HostInventoryConfig;
import com.cloupia.model.cIM.Group;
import com.cloupia.model.cIM.ReportContext;
import com.cloupia.model.cIM.TabularReport;
import com.cloupia.service.cIM.inframgr.TabularReportGeneratorIf;
import com.cloupia.service.cIM.inframgr.reportengine.ReportRegistryEntry;
import com.cloupia.service.cIM.inframgr.reports.TabularReportInternalModel;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.host.PureHost;
import com.purestorage.rest.host.PureHostConnection;
import com.purestorage.rest.hostgroup.PureHostGroup;
import com.purestorage.rest.PureRestClient;

import java.util.ArrayList;
import java.util.List;

public class ChildHostReportImpl implements TabularReportGeneratorIf
{
    static Logger logger = Logger.getLogger(ChildHostReportImpl.class);

    @Override
    public TabularReport getTabularReportReport(ReportRegistryEntry entry, ReportContext context) throws Exception
    {
        logger.info("Entering HostReportImpl.getTabularReportReport" );
        logger.info("ReportContext.getId()=" + context.getId()+context);
        String accountName="",hostGroupName="";
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
        TabularReport report = new TabularReport();
        report.setGeneratedTime(System.currentTimeMillis());
        report.setReportName(entry.getReportLabel());
        report.setContext(context);

        TabularReportInternalModel model = new TabularReportInternalModel();
        model.addTextColumn("Id", "Id",true);
        model.addTextColumn("Account Name", "Account Name");
        model.addTextColumn("Hosts", "Host Name");
        model.addTextColumn("Host Group", "Host Group");
        model.addNumberColumn("Volumes", "Number of volumes", false);
        model.addTextColumn("Connected Volumes", "Connected Volumes");
        model.addDoubleColumn("Provisioned(GB)", "Provisioned size of attached volumes");
        model.completedHeader();

        if (accountName != null && accountName.length() > 0)
        {
            List<HostInventoryConfig> hosts= PureUtils.getAllPureHost();
            List<HostGroupInventoryConfig> hostgroups= PureUtils.getAllPureHostGroup();
            for (HostGroupInventoryConfig hostgroup: hostgroups)
            {
	            if (hostgroup.getHostGroupName().equals(hostGroupName) && accountName.equalsIgnoreCase(hostgroup.getAccountName()))
	            {
	            	String hosts2 =hostgroup.getHostsList();
	            	String[] hostsL=hosts2.split(",");
	            	List<String> hosts1 = new ArrayList<String>();
	            	for(int i=0;i<hostsL.length;i++)
	            	{
	            		hosts1.add(hostsL[i]);
	            	}
	            	logger.info("Hosts in Hostgroup :"+ hosts1);
	            	for (HostInventoryConfig host: hosts)
	                {
	                	if(hosts1.contains(host.getHost()))
	                	{
	                		 if (accountName.equalsIgnoreCase(host.getAccountName()))
	                         {
		                			 model.addTextValue(host.getId());
		                			 model.addTextValue(host.getAccountName());	
				                    model.addTextValue(host.getHost()); // Name
				                    model.addTextValue(host.getHostGroup()); //HostGroup
				                    model.addNumberValue(host.getVolumes());
				                    model.addTextValue(host.getConnectedVolumes());
				                    model.addDoubleValue(host.getProvisionedSize()); // Provisioned size of attached volumes
				                    
				                    model.completedRow();
	                         }
	                	}
	                }
	             
	            	
	            }
            	
            }
        }
        

        model.updateReport(report);
        return report;
    }
}