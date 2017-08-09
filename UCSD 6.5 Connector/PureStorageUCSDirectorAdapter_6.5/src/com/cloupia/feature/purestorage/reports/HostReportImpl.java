package com.cloupia.feature.purestorage.reports;

import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.HostInventoryConfig;
import com.cloupia.feature.purestorage.accounts.VolumeInventoryConfig;
import com.cloupia.model.cIM.Group;
import com.cloupia.model.cIM.ReportContext;
import com.cloupia.model.cIM.TabularReport;
import com.cloupia.service.cIM.inframgr.TabularReportGeneratorIf;
import com.cloupia.service.cIM.inframgr.reportengine.ReportRegistryEntry;
import com.cloupia.service.cIM.inframgr.reports.TabularReportInternalModel;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.host.PureHost;
import com.purestorage.rest.host.PureHostConnection;
import com.purestorage.rest.PureRestClient;

import java.util.List;

public class HostReportImpl implements TabularReportGeneratorIf
{
    static Logger logger = Logger.getLogger(HostReportImpl.class);

    @Override
    public TabularReport getTabularReportReport(ReportRegistryEntry entry, ReportContext context) throws Exception
    {
        logger.info("Entering HostReportImpl.getTabularReportReport" );
        logger.info("ReportContext.getId()=" + context.getId()+context);
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
        model.addTextColumn("Name", "Host Name");
        model.addTextColumn("HostGroup", "HostGroup");
        model.addNumberColumn("Volumes", "Number of volumes", false);
        model.addTextColumn("Connected Volumes", "Connected Volumes");
        model.addDoubleColumn("Provisioned(GB)", "Provisioned size of attached volumes");
        model.completedHeader();

        
        if (accountName != null && accountName.length() > 0 )
        {
//            FlashArrayAccount acc = FlashArrayAccount.getFlashArrayCredential(accountName);
//            PureRestClient CLIENT = PureUtils.ConstructPureRestClient(acc);
//            List<PureHost> hosts =  CLIENT.hosts().list();
//            for (PureHost host: hosts)
        	List<HostInventoryConfig> hosts= PureUtils.getAllPureHost();
             for (HostInventoryConfig host: hosts)
             {
            	 if (accountName.equalsIgnoreCase(host.getAccountName()))
                 {
              
            	model.addTextValue(host.getId());
            	model.addTextValue(accountName);
                model.addTextValue(host.getHost()); // Name
                model.addTextValue(host.getHostGroup()); //HostGroup
                model.addNumberValue(host.getVolumes()); // Number of volumes
                
               /* List<PureHostConnection> privateConnections = CLIENT.hosts().getPrivateConnections(host.getName());
                List<PureHostConnection> sharedConnections = CLIENT.hosts().getSharedConnections(host.getName());
                // private and shared connections cannot overlap (i.e. same vol cannot be part of both shared and private connections)
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
                }*/
                model.addTextValue(host.getConnectedVolumes());
                model.addDoubleValue(host.getProvisionedSize()); // Provisioned size of attached volumes
                model.completedRow();
                 }
            }
        }

        model.updateReport(report);
        return report;
    }
}