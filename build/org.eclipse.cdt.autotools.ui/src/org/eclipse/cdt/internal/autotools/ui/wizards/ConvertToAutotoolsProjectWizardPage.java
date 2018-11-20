/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.wizards;

import org.eclipse.cdt.autotools.core.AutotoolsNewProjectNature;
import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.autotools.core.AutotoolsPropertyConstants;
import org.eclipse.cdt.internal.autotools.core.configure.AutotoolsConfigurationManager;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 *
 * ConvertToAutotoolsProjectWizardPage
 * Standard main page for a wizard that adds a Managed Make C project Nature to a project with no nature associated with it.
 * This conversion is one way in that the project cannot be converted back (i.e have the nature removed).
 *
 * @author Jeff Johnston
 * @since Feb 8, 2006
 *<p>
 * Example useage:
 * <pre>
 * mainPage = new ConvertToAutotoolsProjectWizardPage("ConvertProjectPage");
 * mainPage.setTitle("Project Conversion");
 * mainPage.setDescription("Add C or C++ Managed Make Nature to a project.");
 * </pre>
 * </p>
 */
public class ConvertToAutotoolsProjectWizardPage extends ConvertProjectWizardPage {

	private static final String WZ_TITLE = "WizardAutotoolsProjectConversion.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "WizardAutotoolsProjectConversion.description"; //$NON-NLS-1$
	private static final String PREFIX = "WizardAutotoolsProjectConversion";
	protected static final String MSG_ADD_NATURE = PREFIX + ".message.add_nature"; //$NON-NLS-1$
	protected static final String MSG_ADD_BUILDER = PREFIX + ".message.add_builder"; //$NON-NLS-1$
	protected static final String MSG_SAVE = PREFIX + ".message.save"; //$NON-NLS-1$

	/**
	 * Constructor for ConvertToStdMakeProjectWizardPage.
	 * @param pageName
	 */
	public ConvertToAutotoolsProjectWizardPage(String pageName, ConvertToAutotoolsProjectWizard wizard) {
		super(pageName);
		setWizard(wizard);
	}

	/**
	 * Method getWzTitleResource returns the correct Title Label for this class
	 * overriding the default in the superclass.
	 */
	@Override
	protected String getWzTitleResource() {
		return AutotoolsUIPlugin.getResourceString(WZ_TITLE);
	}

	/**
	 * Method getWzDescriptionResource returns the correct description
	 * Label for this class overriding the default in the superclass.
	 */
	@Override
	protected String getWzDescriptionResource() {
		return AutotoolsUIPlugin.getResourceString(WZ_DESC);
	}

	/**
	 * Method isCandidate returns true for all projects.
	 *
	 * @param project
	 * @return boolean
	 */
	@Override
	public boolean isCandidate(IProject project) {
		return true; // all
	}

	protected IProjectType getProjectType() {
		return ((ConvertToAutotoolsProjectWizard) getWizard()).getProjectType();
	}

	protected IConfiguration[] getSelectedConfigurations() {
		return ((ConvertToAutotoolsProjectWizard) getWizard()).getSelectedConfigurations();
	}

	protected void applyOptions(IProject project, IProgressMonitor monitor) {
		((ConvertToAutotoolsProjectWizard) getWizard()).applyOptions(project, monitor);
	}

	@Override
	public void convertProject(IProject project, IProgressMonitor monitor, String projectID) throws CoreException {
		monitor.beginTask(
				AutotoolsUIPlugin.getResourceString("WizardMakeProjectConversion.monitor.convertingToMakeProject"), 7); //$NON-NLS-1$
		IConfiguration defaultCfg = null;
		Boolean convertingNewAutotoolsProject = false;
		try {
			super.convertProject(project, SubMonitor.convert(monitor, 1), projectID);
			// Bug 289834 - Converting C Autotools project to C++ loses configurations
			if (project.hasNature(AutotoolsNewProjectNature.AUTOTOOLS_NATURE_ID)) {
				convertingNewAutotoolsProject = true; // set this for finally clause
				return; // We have converted C to C++ or vice-versa for an existing Autotools project and are done
			}
			// Otherwise, we must scrap existing configurations as they will have tool settings we cannot use
			monitor.subTask(AutotoolsUIPlugin.getResourceString(MSG_ADD_NATURE));
			ManagedCProjectNature.addManagedNature(project, SubMonitor.convert(monitor, 1));
			AutotoolsNewProjectNature.addAutotoolsNature(project, SubMonitor.convert(monitor, 1));
			// We need to remove any old Autotools nature, if one exists.
			AutotoolsNewProjectNature.removeOldAutotoolsNature(project, SubMonitor.convert(monitor, 1));
			monitor.subTask(AutotoolsUIPlugin.getResourceString(MSG_ADD_BUILDER));
			AutotoolsNewProjectNature.addAutotoolsBuilder(project, SubMonitor.convert(monitor, 1));
			// FIXME: Default scanner property: make -w - eventually we want to use Make core's build scanner
			project.setPersistentProperty(AutotoolsPropertyConstants.SCANNER_USE_MAKE_W,
					AutotoolsPropertyConstants.TRUE);
			// Specify false for override in next call as override can cause the method to throw an
			// exception.
			CCorePlugin.getDefault().mapCProjectOwner(project, projectID, false);
			// Add the ManagedProject to the project
			IManagedProject newManagedProject = null;
			IManagedBuildInfo info = null;
			try {
				info = ManagedBuildManager.createBuildInfo(project);
				IProjectType parent = getProjectType();
				newManagedProject = ManagedBuildManager.createManagedProject(project, parent);
				if (newManagedProject != null) {
					IConfiguration[] selectedConfigs = getSelectedConfigurations();
					for (int i = 0; i < selectedConfigs.length; i++) {
						IConfiguration config = selectedConfigs[i];
						int id = ManagedBuildManager.getRandomNumber();
						IConfiguration newConfig = newManagedProject.createConfiguration(config,
								config.getId() + "." + id); //$NON-NLS-1$
						newConfig.setArtifactName(newManagedProject.getDefaultArtifactName());
					}
					// Now add the first supported config in the list as the default
					IConfiguration[] newConfigs = newManagedProject.getConfigurations();
					for (int i = 0; i < newConfigs.length; i++) {
						if (newConfigs[i].isSupported()) {
							defaultCfg = newConfigs[i];
							break;
						}
					}

					if (defaultCfg == null && newConfigs.length > 0)
						defaultCfg = newConfigs[0];

					if (defaultCfg != null) {
						ManagedBuildManager.setDefaultConfiguration(project, defaultCfg);
						ManagedBuildManager.setSelectedConfiguration(project, defaultCfg);
					}
					ManagedBuildManager.setNewProjectVersion(project);
				}
			} catch (BuildException e) {
				AutotoolsUIPlugin.log(e);
			}

			// Modify the project settings
			if (project != null) {
				applyOptions(project, SubMonitor.convert(monitor, 2));
			}

			// Save the build options
			monitor.subTask(AutotoolsUIPlugin.getResourceString(MSG_SAVE));
			if (info != null) {
				info.setValid(true);
				ManagedBuildManager.saveBuildInfo(project, true);
			}
		} finally {
			if (!convertingNewAutotoolsProject) { // Bug 289834 - don't create new config if switching between C and C++
				// Create a default Autotools configuration and save it.
				// We must do this after the ManagedBuildManager does a save because
				// we might not yet have a Configuration Description set up for the
				// default configuration and we need the id to create our own form
				// of configuration.
				ICConfigurationDescription cfgd = ManagedBuildManager.getDescriptionForConfiguration(defaultCfg);
				String id = cfgd.getId();
				AutotoolsConfigurationManager.getInstance().getConfiguration(project, id, true);
				AutotoolsConfigurationManager.getInstance().saveConfigs(project);
				IStatus initResult = ManagedBuildManager.initBuildInfoContainer(project);
				if (initResult.getCode() != IStatus.OK) {
					// At this point, I can live with a failure
					AutotoolsUIPlugin.log(initResult);
				}
			}
			monitor.done();
		}
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		IStructuredSelection sel = ((BasicNewResourceWizard) getWizard()).getSelection();
		if (sel != null) {
			tableViewer.setCheckedElements(sel.toArray());
			setPageComplete(validatePage());
		}
	}

}
