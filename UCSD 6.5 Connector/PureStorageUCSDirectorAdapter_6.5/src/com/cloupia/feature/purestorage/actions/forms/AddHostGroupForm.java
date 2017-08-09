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

public class AddHostGroupForm {
	

	 @FormField(label = "Host Group Prefix Name or Whole Name", help = "FlashArray Host Group Prefix Name or Whole Name", mandatory = true)
	    private String hostGroupPreName;

	    @FormField(label = "Host Group suffix Start Number", help = "FlashArray Host Group suffix Start Number", mandatory = false)
	    private String startNumber;

	    @FormField(label = "Host Group suffix End Number", help = "FlashArray Host Group suffix End Number", mandatory = false)
	    private String endNumber;

		public String getHostGroupPreName() {
			return hostGroupPreName;
		}

		public void setHostGroupPreName(String hostGroupPreName) {
			this.hostGroupPreName = hostGroupPreName;
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
}
