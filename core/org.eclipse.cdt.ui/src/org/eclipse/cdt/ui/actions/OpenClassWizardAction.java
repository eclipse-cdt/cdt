package org.eclipse.cdt.ui.actions;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.ui.wizards.NewClassWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * TODO: Provide description for "OpenClassWizardAction".
 * @see IWorkbenchWindowActionDelegate
 */
public class OpenClassWizardAction extends AbstractOpenWizardAction implements IWorkbenchWindowActionDelegate {
	public OpenClassWizardAction() {
		WorkbenchHelp.setHelp(this, ICHelpContextIds.OPEN_CLASS_WIZARD_ACTION);
	}

	public OpenClassWizardAction(String label, Class[] acceptedTypes) {
		super(label, acceptedTypes, false);
		WorkbenchHelp.setHelp(this, ICHelpContextIds.OPEN_CLASS_WIZARD_ACTION);
	}

	protected Wizard createWizard() { 
		return new NewClassWizard(); 
	}	
}
