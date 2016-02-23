package org.eclipse.cdt.internal.qt.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.internal.qt.core.project.QtProjectGenerator;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class HelloWorldWizard extends BasicNewResourceWizard {

	private WizardNewProjectCreationPage mainPage;

	@Override
	public void addPages() {
		mainPage = new WizardNewProjectCreationPage("basicNewProjectPage") { //$NON-NLS-1$
			@Override
			public void createControl(Composite parent) {
				super.createControl(parent);
				createWorkingSetGroup((Composite) getControl(), getSelection(),
						new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$
				Dialog.applyDialogFont(getControl());
			}
		};
		mainPage.setTitle("New Qt Project"); //$NON-NLS-1$
		mainPage.setDescription("Specify properties of new Qt project."); //$NON-NLS-1$
		this.addPage(mainPage);
	}

	protected String getTemplateManifestPath() {
		return "templates/project2/appProject/manifest.xml"; //$NON-NLS-1$
	}

	@Override
	public boolean performFinish() {
		QtProjectGenerator generator = new QtProjectGenerator();
		generator.setTemplateManifestPath(getTemplateManifestPath());
		generator.setProjectName(mainPage.getProjectName());
		if (!mainPage.useDefaults()) {
			generator.setLocationURI(mainPage.getLocationURI());
		}

		Map<String, Object> model = new HashMap<>();

		try {
			getContainer().run(true, true, new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor monitor)
						throws CoreException, InvocationTargetException, InterruptedException {
					monitor.beginTask("Generating project", 1); //$NON-NLS-1$
					generator.generate(model, monitor);
					monitor.done();
				}

				@Override
				public ISchedulingRule getRule() {
					return ResourcesPlugin.getWorkspace().getRoot();
				}
			});
		} catch (InterruptedException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	@Override
	public void init(IWorkbench theWorkbench, IStructuredSelection currentSelection) {
		super.init(theWorkbench, currentSelection);
	}

}
