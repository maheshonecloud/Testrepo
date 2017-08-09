package com.cloupia.feature.purestorage.reports;

import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.PureUtils;
import com.cloupia.feature.purestorage.accounts.FlashArrayAccount;
import com.cloupia.feature.purestorage.accounts.SnapshotInventoryConfig;
import com.cloupia.feature.purestorage.accounts.VolumeInventoryConfig;
import com.cloupia.model.cIM.Group;
import com.cloupia.model.cIM.ReportContext;
import com.cloupia.model.cIM.TabularReport;
import com.cloupia.service.cIM.inframgr.TabularReportGeneratorIf;
import com.cloupia.service.cIM.inframgr.reportengine.ReportRegistryEntry;
import com.cloupia.service.cIM.inframgr.reports.TabularReportInternalModel;
import com.purestorage.rest.PureRestClient;
import com.purestorage.rest.volume.PureVolume;
import com.purestorage.rest.PureRestClient;

import java.util.List;

public class SnapshotReportImpl implements TabularReportGeneratorIf
{
    static Logger logger = Logger.getLogger(SnapshotReportImpl.class);

    @Override
    public TabularReport getTabularReportReport(ReportRegistryEntry entry, ReportContext context) throws Exception
    {
        logger.info("Entering SnapshotReportImpl.getTabularReportReport" );
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
        model.addTextColumn("Snapshot Name", "Snapshot Name");
        model.addDoubleColumn("Size(GB)", "Size");
        model.addTextColumn("Source", "Source");
        model.addTimeColumn("Creation","Creation");
        model.completedHeader();

        if (accountName != null && accountName.length() > 0)
        {
//            FlashArrayAccount acc = FlashArrayAccount.getFlashArrayCredential(accountName);
//            PureRestClient CLIENT = PureUtils.ConstructPureRestClient(acc);
//            List<PureVolume> volumes =  CLIENT.volumes().list();
        	List<SnapshotInventoryConfig> snapshots= PureUtils.getAllPureSnapshots();
            for (SnapshotInventoryConfig snapshot: snapshots)
            {
            	 if (accountName.equalsIgnoreCase(snapshot.getAccountName()))
                 {
            		 model.addTextValue(snapshot.getId());
                	model.addTextValue(accountName);
                    model.addTextValue(snapshot.getSnapshotName());
                    model.addDoubleValue(snapshot.getSize());
                    model.addTextValue(snapshot.getSource());
                    model.addTimeValue(snapshot.getCreation());
                    model.completedRow();
                 }
            }
        }

        model.updateReport(report);
        return report;
    }
}