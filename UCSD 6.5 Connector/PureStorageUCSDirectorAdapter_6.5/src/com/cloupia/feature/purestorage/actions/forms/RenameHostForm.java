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

public class RenameHostForm {
	

	@FormField(label = "New Host Name", help = "FlashArray Host Name", mandatory = true)
    private String hostName;

   
	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	
}
