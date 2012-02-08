package org.eclipse.cdt.ui.wizards;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;

import org.eclipse.cdt.internal.ui.CUIMessages;

/**
 * This is the new CDT project wizard.
 * 
 * Without subclassing, it is in it's most generic form.
 * 
 * Subclasses can filter or select the languages and add template filters.
 * 
 * @author Doug Schaefer
 * @since 5.4
 */
public class NewCDTProjectWizard extends Wizard implements INewWizard {

	private IStructuredSelection selection;
	private WizardNewProjectCreationPage mainPage;
	private TemplateSelectionPage templatePage;
	private WizardNewProjectReferencePage referencePage;
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		setNeedsProgressMonitor(true);
		setWindowTitle(CUIMessages.NewCDTProjectWizard_windowTitle);
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();

		mainPage = new WizardNewProjectCreationPage("basicNewProjectPage") { //$NON-NLS-1$
			@Override
			public void createControl(Composite parent) {
				super.createControl(parent);
				createWorkingSetGroup(
						(Composite) getControl(),
						selection,
						new String[] { "org.eclipse.ui.resourceWorkingSetPage", //$NON-NLS-1$
								"org.eclipse.cdt.ui.CElementWorkingSetPage" }); //$NON-NLS-1$
				Dialog.applyDialogFont(getControl());
			}
		}; 
		mainPage.setTitle(CUIMessages.NewCDTProjectWizard_mainPageTitle);
		mainPage.setDescription(CUIMessages.NewCDTProjectWizard_mainPageDesc);
		addPage(mainPage);

		templatePage = new TemplateSelectionPage();
		templatePage.setTitle(CUIMessages.NewCDTProjectWizard_templatePageTitle);
		templatePage.setDescription(CUIMessages.NewCDTProjectWizard_templatePageDesc);
		addPage(templatePage);
		
		// only add page if there are already projects in the workspace
		if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
			referencePage = new WizardNewProjectReferencePage(
					"basicReferenceProjectPage");//$NON-NLS-1$
			referencePage.setTitle(CUIMessages.NewCDTProjectWizard_refPageTitle);
			referencePage
					.setDescription(CUIMessages.NewCDTProjectWizard_refPageDesc);
			this.addPage(referencePage);
		}
	}
	
	@Override
	public boolean canFinish() {
		IWizardPage page = mainPage;
		while (page != null) {
			if (!page.isPageComplete())
				return false;
			page = page.getNextPage();
		}
		return true;
	}
	
}
