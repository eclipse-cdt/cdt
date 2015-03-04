/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mohamed Azab (Mentor Graphics) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.wizards;

import org.eclipse.cdt.autotools.core.AutotoolsNewProjectNature;
import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.autotools.core.AutotoolsPropertyConstants;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.ui.wizards.NewMakeProjFromExistingPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

@SuppressWarnings("restriction")
public class AutotoolsProjectImportWizardPage extends
		NewMakeProjFromExistingPage {
	private Button langc;
	private Button langcpp;

	protected AutotoolsProjectImportWizardPage() {
		setTitle(AutotoolsWizardMessages
				.getResourceString("ImportWizardPage.title"));
		setDescription(AutotoolsWizardMessages
				.getResourceString("ImportWizardPage.description"));
	}

	protected IProjectType getProjectType() {
		return ((AutotoolsProjectImportWizard) getWizard()).getProjectType();
	}

	protected IConfiguration[] getSelectedConfigurations() {
		return ((AutotoolsProjectImportWizard) getWizard())
				.getSelectedConfigurations();
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		addProjectNameSelector(comp);
		addSourceSelector(comp);
		addLanguageSelector(comp);
		setControl(comp);
	}

	@Override
	public void addLanguageSelector(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText("Select project language");

		SelectionListener cListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				validatePage();
			}
		};

		langc = ControlFactory.createRadioButton(group,
				CUIMessages.ConvertProjectWizardPage_CProject, "C ", //$NON-NLS-1$
				cListener);
		langc.setSelection(true);

		langcpp = ControlFactory.createRadioButton(group,
				CUIMessages.ConvertProjectWizardPage_CppProject, "C++ ", //$NON-NLS-1$
				cListener);
		langcpp.setSelection(false);
	}

	@Override
	public IToolChain getToolChain() {
		return getSelectedConfigurations()[0].getToolChain();
	}

	public void convertProject(IProject project, IProgressMonitor monitor,
			String projectID) throws CoreException {
		monitor.beginTask(
				AutotoolsUIPlugin
						.getResourceString("WizardMakeProjectConversion.monitor.convertingToMakeProject"), 7); //$NON-NLS-1$
		IConfiguration defaultCfg = null;
		try {
			monitor.subTask(AutotoolsUIPlugin
					.getResourceString("adding project nature"));
			ManagedCProjectNature.addManagedNature(project,
					new SubProgressMonitor(monitor, 1));
			AutotoolsNewProjectNature.addAutotoolsNature(project,
					new SubProgressMonitor(monitor, 1));
			monitor.subTask(AutotoolsUIPlugin
					.getResourceString("adding builder"));
			AutotoolsNewProjectNature.addAutotoolsBuilder(project,
					new SubProgressMonitor(monitor, 1));
			project.setPersistentProperty(
					AutotoolsPropertyConstants.SCANNER_USE_MAKE_W,
					AutotoolsPropertyConstants.TRUE);
			// Specify false for override in next call as override can cause the
			// method to throw an
			// exception.
			CCorePlugin.getDefault()
					.mapCProjectOwner(project, projectID, false);
			// Add the ManagedProject to the project
			IManagedProject newManagedProject = null;
			IManagedBuildInfo info = null;
			try {
				info = ManagedBuildManager.createBuildInfo(project);
				newManagedProject = ManagedBuildManager.createManagedProject(
						project, getProjectType());
				if (newManagedProject != null) {
					for (int i = 0; i < getSelectedConfigurations().length; i++) {
						IConfiguration config = getSelectedConfigurations()[i];
						int id = ManagedBuildManager.getRandomNumber();
						IConfiguration newConfig = newManagedProject
								.createConfiguration(config, config.getId()
										+ "." + id); //$NON-NLS-1$
						newConfig.setArtifactName(newManagedProject
								.getDefaultArtifactName());
					}
					// Now add the first supported config in the list as the
					// default
					IConfiguration[] newConfigs = newManagedProject
							.getConfigurations();
					for (int i = 0; i < newConfigs.length; i++) {
						if (newConfigs[i].isSupported()) {
							defaultCfg = newConfigs[i];
							break;
						}
					}

					if (defaultCfg == null && newConfigs.length > 0)
						defaultCfg = newConfigs[0];

					if (defaultCfg != null) {
						ManagedBuildManager.setDefaultConfiguration(project,
								defaultCfg);
						ManagedBuildManager.setSelectedConfiguration(project,
								defaultCfg);
					}
					ManagedBuildManager.setNewProjectVersion(project);
				}
			} catch (BuildException e) {
				AutotoolsUIPlugin.log(e);
			}

			// Save the build options
			monitor.subTask(AutotoolsUIPlugin
					.getResourceString("saving project"));
			if (info != null) {
				info.setValid(true);
				ManagedBuildManager.saveBuildInfo(project, true);
			}
		} finally {
			monitor.done();
		}
	}

	public boolean isC() {
		return langc.getSelection();
	}

	public boolean isCPP() {
		return langcpp.getSelection();
	}
}
