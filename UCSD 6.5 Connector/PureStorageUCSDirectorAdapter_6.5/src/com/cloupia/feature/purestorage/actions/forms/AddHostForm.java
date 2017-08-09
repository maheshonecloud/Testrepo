package com.cloupia.feature.purestorage.actions.forms;

import javax.jdo.annotations.Persistent;

import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.feature.purestorage.lovs.FlashArrayAccountsNameProvider;
import com.cloupia.feature.purestorage.lovs.VolumeSizeUnitProvider;
import com.cloupia.model.cIM.FormFieldDefinition;
import com.cloupia.service.cIM.inframgr.customactions.UserInputField;
import com.cloupia.service.cIM.inframgr.customactions.WorkflowInputFieldTypeDeclaration;
import com.cloupia.service.cIM.inframgr.forms.wizard.FormField;
import com.cloupia.service.cIM.inframgr.forms.wizard.HideFieldOnCondition;

public class AddHostForm {
	

	@FormField(label = "Host Name", help = "FlashArray Host Name", mandatory = true)
    private String hostName;

    @FormField(label = "Configure Fiber Channel WWPNs", help = "Fiber Channel WWNs for Host. Use ',' to seperate WWN", mandatory = false)
    private String wwns;

    @FormField(label = "Configure iSCSI IQNs", help = "iSCSI IQNs for Host. Use ',' to seperate WWN", mandatory = false)
    private String iqns;

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getWwns() {
		return wwns;
	}

	public void setWwns(String wwns) {
		this.wwns = wwns;
	}

	public String getIqns() {
		return iqns;
	}

	public void setIqns(String iqns) {
		this.iqns = iqns;
	}

}
