package org.eclipse.cdt.cmake.ui.internal;

import java.io.IOException;

import org.eclipse.cdt.cmake.core.ICMakeInstallationManager;
import org.eclipse.jface.wizard.Wizard;

public class NewCMakeInstallationWizard extends Wizard {

	private final ICMakeInstallationManager manager;
	private final NewCMakeInstallationWizardPage selectionPage;

	public NewCMakeInstallationWizard(ICMakeInstallationManager manager) {
		this.manager = manager;
		selectionPage = new NewCMakeInstallationWizardPage(this.manager);
	}
	
	@Override
	public void addPages() {
		addPage(selectionPage);
		super.addPages();
	}
	
	@Override
	public boolean performFinish() {
		try {
			manager.add(selectionPage.getInstallation());
			return true;
		} catch (IOException e) {
			Activator.log(e);
		}
		
		return false;
	}

}
