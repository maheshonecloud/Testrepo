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

public class AddVolumeForm {
	

    @FormField(label = "Volume Prefix Name or Whole Name", help = "Letters, numbers, -, and _", mandatory = true )
    private String volumePreName;

    @FormField(label = "Volume suffix Start Number", help = "FlashArray Volume suffix Start Number", validate = true, mandatory = false)
    
    private String startNumber="";

    @FormField(label = "Volume suffix End Number", help = "FlashArray Volume suffix End Number", validate = true, mandatory = false)
    
    private String endNumber ="";

    @FormField(label = "Volume Size Unit", help = "FlashArray Volume Size Unit", mandatory = true, type = FormFieldDefinition.FIELD_TYPE_EMBEDDED_LOV,
            lovProvider = VolumeSizeUnitProvider.NAME)
    
    private String volumeSizeUnit;

    @FormField(label = "Volume Size Number", help = "Numbers", mandatory = true)
    
    private String volumeSizeNumber;

	public String getVolumePreName() {
		return volumePreName;
	}

	public void setVolumePreName(String volumePreName) {
		this.volumePreName = volumePreName;
	}

	public String getStartNumber() {
		return startNumber;
	}

	public void setStartNumber(String startNumber) {
		this.startNumber = startNumber;
	}

	public String getEndNumber() {
		return endNumber;
	}

	public void setEndNumber(String endNumber) {
		this.endNumber = endNumber;
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
