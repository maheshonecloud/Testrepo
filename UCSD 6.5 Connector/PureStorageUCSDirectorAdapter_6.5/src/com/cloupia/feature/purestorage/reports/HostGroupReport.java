package com.cloupia.feature.purestorage.reports;

import org.apache.log4j.Logger;

import com.cloupia.feature.purestorage.accounts.FlashArrayConvergedStackBuilder;
import com.cloupia.feature.purestorage.actions.AddHost;
import com.cloupia.feature.purestorage.actions.AddHostGroup;
import com.cloupia.feature.purestorage.actions.ConnectHostToHostGroup;
import com.cloupia.feature.purestorage.actions.ConnectVolumeHost;
import com.cloupia.feature.purestorage.actions.ConnectVolumeHostGroup;
import com.cloupia.feature.purestorage.actions.DeleteHost;
import com.cloupia.feature.purestorage.actions.DeleteHostGroup;
import com.cloupia.feature.purestorage.actions.DisconnectHostToHostGroup;
import com.cloupia.feature.purestorage.actions.DisconnectVolumeHost;
import com.cloupia.feature.purestorage.actions.DisconnectVolumeHostGroup;
import com.cloupia.feature.purestorage.actions.RenameHost;
import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.model.cIM.DynReportContext;
import com.cloupia.model.cIM.ReportContextRegistry;
import com.cloupia.service.cIM.inframgr.reportengine.ContextMapRule;
import com.cloupia.service.cIM.inframgr.reports.simplified.CloupiaReportAction;
import com.cloupia.service.cIM.inframgr.reports.simplified.CloupiaReportWithActions;
import com.cloupia.service.cIM.inframgr.reports.simplified.actions.DrillDownAction;

public class HostGroupReport extends CloupiaReportWithActions
{

    private static Logger logger = Logger.getLogger(HostGroupReport.class);

    public HostGroupReport()
    {
        super();
        this.setMgmtColumnIndex(0);
        
        
    }

    @Override
    public CloupiaReportAction[] getActions()
    {
    	CloupiaReportAction[] actions = new CloupiaReportAction[7];
		DrillDownAction act=new DrillDownAction();
		AddHostGroup createaction = new AddHostGroup();
		DeleteHostGroup deleteaction = new DeleteHostGroup();
    	ConnectVolumeHostGroup connect = new ConnectVolumeHostGroup();
    	DisconnectVolumeHostGroup disconnect = new DisconnectVolumeHostGroup();
    	ConnectHostToHostGroup connectH = new ConnectHostToHostGroup();
    	DisconnectHostToHostGroup disconnectH = new DisconnectHostToHostGroup();
    	
		
		
		actions[0] = act;
		actions[1] = createaction;
		actions[2] = deleteaction;
		actions[3] = connect;
		actions[4] = disconnect;
		actions[5] = connectH;
		actions[6] = disconnectH;
		
		return actions;
    
    	
    }

    @Override
    public Class<?> getImplementationClass()
    {
        return HostGroupReportImpl.class;
    }

    @Override
    public String getReportLabel()
    {
        return "Host Groups";
    }

    @Override
    public String getReportName()
    {
        return "com.purestorage.flasharray.hostgroup";
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