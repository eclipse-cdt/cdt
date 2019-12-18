package org.eclipse.tools.templates.ui;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard to be used to launch the template selection page.
 */
public class NewWizard extends Wizard implements INewWizard {

	private final String[] tags;

	private String templateSelectionPageTitle;
	private String templateSelectionPageDescription;
	private IWorkbench workbench;
	private IStructuredSelection selection;
	private TemplateSelectionPage templateSelectionPage;

	protected NewWizard(String... tags) {
		this.tags = tags;
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);
	}

	protected void setTemplateSelectionPageTitle(String title) {
		this.templateSelectionPageTitle = title;
		if (templateSelectionPage != null) {
			templateSelectionPage.setTitle(title);
		}
	}

	protected void setTemplateSelectionPageDescription(String description) {
		this.templateSelectionPageDescription = description;
		if (templateSelectionPage != null) {
			templateSelectionPage.setDescription(description);
		}
	}

	@Override
	public void addPages() {
		templateSelectionPage = new TemplateSelectionPage("templateSelection", tags); //$NON-NLS-1$
		templateSelectionPage.setTitle(templateSelectionPageTitle);
		templateSelectionPage.setDescription(templateSelectionPageDescription);
		this.addPage(templateSelectionPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}

	/**
	 * Initialize the template wizard that comes next.
	 *
	 * @param nextWizard
	 *            the next wizard to show
	 */
	public void initialize(INewWizard nextWizard) {
		nextWizard.init(workbench, selection);
	}

	public void selectTemplate(String id) {
		if (templateSelectionPage != null) {
			templateSelectionPage.selectTemplate(id);
		}
	}

	@Override
	public boolean canFinish() {
		// Need to check with the template wizard
		return false;
	}

	@Override
	public boolean performFinish() {
		// The template wizard will do the real finish.
		return true;
	}

}
