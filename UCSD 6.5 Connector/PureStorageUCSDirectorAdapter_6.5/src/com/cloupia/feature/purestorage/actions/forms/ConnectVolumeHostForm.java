package com.cloupia.feature.purestorage.actions.forms;

import javax.jdo.annotations.Persistent;

import com.cloupia.feature.purestorage.constants.PureConstants;
import com.cloupia.feature.purestorage.lovs.FlashArrayAccountsNameProvider;
import com.cloupia.feature.purestorage.lovs.HostTabularProvider;
import com.cloupia.feature.purestorage.lovs.VolumeSizeUnitProvider;
import com.cloupia.feature.purestorage.lovs.VolumeTabularProvider;
import com.cloupia.model.cIM.FormFieldDefinition;
import com.cloupia.service.cIM.inframgr.customactions.UserInputField;
import com.cloupia.service.cIM.inframgr.customactions.WorkflowInputFieldTypeDeclaration;
import com.cloupia.service.cIM.inframgr.forms.wizard.FieldValidation;
import com.cloupia.service.cIM.inframgr.forms.wizard.FormField;
import com.cloupia.service.cIM.inframgr.forms.wizard.HideFieldOnCondition;

public class ConnectVolumeHostForm {
	
	    @FormField(label = "Volume Name", help = "Use ',' to seperate volumes name", mandatory = true,multiline = true,type=FormFieldDefinition.FIELD_TYPE_TABULAR_POPUP, table= VolumeTabularProvider.TABULAR_PROVIDER)
	   
	    private String volumeName;
	 

		@FormField(label = "Specify LUN", validate = true, help = "Select if you want to give LUN as Admin Input.", type = FormFieldDefinition.FIELD_TYPE_BOOLEAN)
		private boolean isStatusChange = false;
		
		@FormField(label = "LUN Id", help = "Use ',' to seperate the LUN Ids and LUN Id must be in range 10-255", mandatory = true )
		@HideFieldOnCondition(field = "isStatusChange", op = FieldValidation.OP_EQUALS, value = "false")
		private String   allLunId;

	    
		public String getVolumeName() {
		return volumeName;
	}

	public void setVolumeName(String volumeName) {
		this.volumeName = volumeName;
	}

	public boolean isStatusChange() {
		return isStatusChange;
	}

	public void setStatusChange(boolean isStatusChange) {
		this.isStatusChange = isStatusChange;
	}

	public String getAllLunId() {
		return allLunId;
	}

	public void setAllLunId(String allLunId) {
		this.allLunId = allLunId;
	}

	
}
