package com.cloupia.feature.purestorage.reports;

import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.accounts.FlashArrayConvergedStackBuilder;
import com.cloupia.feature.purestorage.actions.AddHost;
import com.cloupia.feature.purestorage.actions.AddVolume;
import com.cloupia.feature.purestorage.actions.ConnectVolumeHost;
import com.cloupia.feature.purestorage.actions.DeleteHost;
import com.cloupia.feature.purestorage.actions.DestroyVolume;
import com.cloupia.feature.purestorage.actions.DisconnectVolumeHost;
import com.cloupia.feature.purestorage.actions.RenameHost;
import com.cloupia.feature.purestorage.actions.ResizeVolume;
import com.cloupia.feature.purestorage.actions.ScheduleVolumeSnapshot;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.model.cIM.DynReportContext;
import com.cloupia.model.cIM.ReportContextRegistry;
import com.cloupia.service.cIM.inframgr.reportengine.ContextMapRule;
import com.cloupia.service.cIM.inframgr.reports.simplified.CloupiaReportAction;
import com.cloupia.service.cIM.inframgr.reports.simplified.CloupiaReportWithActions;

public class HostReport extends CloupiaReportWithActions
{

    private static Logger logger = Logger.getLogger(HostReport.class);

    public HostReport()
    {
        super();
        this.setMgmtColumnIndex(0);
    }

    @Override
    public CloupiaReportAction[] getActions()
    {
        //return null;
    	
    	AddHost createaction = new AddHost();
    	DeleteHost deleteaction = new DeleteHost();
    	RenameHost rename = new RenameHost();
    	ConnectVolumeHost connect = new ConnectVolumeHost();
    	DisconnectVolumeHost disconnect = new DisconnectVolumeHost();
    	
		CloupiaReportAction[] actions = new CloupiaReportAction[5];
		
		actions[0] = createaction;
		actions[1] = deleteaction;
		actions[2] = rename;
		actions[3] = connect;
		actions[4] = disconnect;
		
		
		return actions;
    }

    @Override
    public Class<?> getImplementationClass()
    {
        return HostReportImpl.class;
    }

    @Override
    public String getReportLabel()
    {
        return "Hosts";
    }

    @Override
    public String getReportName()
    {
        return "com.purestorage.flasharray.host";
    }

    @Override
    public boolean isEasyReport()
    {
        return false;
    }

    @Override
    public boolean isLeafReport()
    {
        return true;
    }

    @Override
    public int getMenuID()
    {
        return PureConstants.HOST_REPORT_MENU_LOCATION;
    }

    @Override
    public ContextMapRule[] getMapRules()
    {
        DynReportContext context = ReportContextRegistry.getInstance().getContextByName(PureConstants.PURE_ACCOUNT_TYPE);
        ContextMapRule rule = new ContextMapRule();
        logger.info("ContextMapRule: context Id:" + context.getId());
        logger.info("ContextMapRule: ContextType:" + context.getType());
        rule.setContextName(context.getId());
        rule.setContextType(context.getType());

        ContextMapRule[] rules = new ContextMapRule[1];
        rules[0] = rule;

        return rules;
    }
    
    @Override
	public boolean showInSummary()
	{
		return true;
	}

}