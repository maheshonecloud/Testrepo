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

public class DestroyVolumeForm {
	

    
    @FormField(label = "Eradicate", help = "Check box to eradicate volumes", mandatory = false, type = FormFieldDefinition.FIELD_TYPE_BOOLEAN)
    private boolean eradicate;

	

	public boolean isEradicate() {
		return eradicate;
	}

	public void setEradicate(boolean eradicate) {
		this.eradicate = eradicate;
	}

	
}
