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

public class ResizeVolumeForm {
	

   

    @FormField(label = "Volume Size Unit", help = "FlashArray Volume Size Unit", mandatory = true, type = FormFieldDefinition.FIELD_TYPE_EMBEDDED_LOV,
            lovProvider = VolumeSizeUnitProvider.NAME)
    
    private String volumeSizeUnit;

    @FormField(label = "Volume Size Number", help = "Numbers", mandatory = true)
    
    private String volumeSizeNumber;
    
    @FormField(label = "Truncate", help = "Check box to truncate size ", mandatory = false, type = FormFieldDefinition.FIELD_TYPE_BOOLEAN)
    
    private boolean truncate;

	
	public boolean isTruncate() {
		return truncate;
	}

	public void setTruncate(boolean truncate) {
		this.truncate = truncate;
	}

	public String getVolumeSizeUnit() {
		return volumeSizeUnit;
	}

	public void setVolumeSizeUnit(String volumeSizeUnit) {
		this.volumeSizeUnit = volumeSizeUnit;
	}

	public String getVolumeSizeNumber() {
		return volumeSizeNumber;
	}

	public void setVolumeSizeNumber(String volumeSizeNumber) {
		this.volumeSizeNumber = volumeSizeNumber;
	}

}
