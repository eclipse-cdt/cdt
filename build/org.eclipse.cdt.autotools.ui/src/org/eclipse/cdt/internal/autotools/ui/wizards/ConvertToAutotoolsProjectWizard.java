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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.wizards.NewCProjectWizardPage;
import org.eclipse.cdt.ui.wizards.conversion.ConversionWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;

/**
 * This wizard provides a method by which the user can
 * add a C nature to a project that previously had no nature associated with it.
 */
public class ConvertToAutotoolsProjectWizard extends ConversionWizard {

	private static final String WZ_TITLE = "WizardAutotoolsProjectConversion.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "WizardAutotoolsProjectConversion.description"; //$NON-NLS-1$
	private static final String PREFIX = "WizardAutotoolsConversion"; //$NON-NLS-1$
	private static final String WINDOW_TITLE = "WizardAutotoolsConversion.windowTitle"; //$NON-NLS-1$
	protected static final String CONF_TITLE = PREFIX + ".config.title"; //$NON-NLS-1$
	protected static final String CONF_DESC = PREFIX + ".config.desc"; //$NON-NLS-1$
	protected static final String MSG_SAVE = PREFIX + ".message.save"; //$NON-NLS-1$
	protected static final String OPTIONS_TITLE = PREFIX + ".options.title"; //$NON-NLS-1$
	protected static final String OPTIONS_DESC = PREFIX + ".options.desc"; //$NON-NLS-1$

	public static final String NULL_INDEXER_ID = "org.eclipse.cdt.core.nullindexer"; //$NON-NLS-1$

	protected CProjectPlatformPage projectConfigurationPage;
	protected NewAutotoolsProjectOptionPage optionPage;

	protected IProject curProject;

	/**
	 * ConvertToAutotoolsConversionWizard Wizard constructor
	 */
	public ConvertToAutotoolsProjectWizard() {
		this(getWindowTitleResource(), getWzDescriptionResource());
	}

	/**
	 * ConvertToAutotoolsConversionWizard Wizard constructor
	 *
	 * @param title
	 * @param desc
	 */
	public ConvertToAutotoolsProjectWizard(String title, String desc) {
		super(title, desc);
	}

	/**
	 * Method getWzDescriptionResource,  allows Wizard description label value
	 * to be changed by subclasses
	 *
	 * @return String
	 */
	protected static String getWzDescriptionResource() {
		return AutotoolsUIPlugin.getResourceString(WZ_DESC);
	}

	/**
	 * Method getWzTitleResource,  allows Wizard description label value
	 * to be changed by subclasses
	 *
	 * @return String
	 */
	protected static String getWzTitleResource() {
		return AutotoolsUIPlugin.getResourceString(WZ_TITLE);
	}

	/**
	 * Method getWindowTitleResource, allows Wizard Title label value to be
	 * changed by subclasses
	 *
	 * @return String
	 */
	protected static String getWindowTitleResource() {
		return AutotoolsUIPlugin.getResourceString(WINDOW_TITLE);
	}

	/**
	  * Method getPrefix,  allows prefix value to be changed by subclasses
	  *
	  * @return String
	  */
	protected static String getPrefix() {
		return PREFIX;
	}

	/**
	 * Method addPages adds our Simple to C conversion Wizard page.
	 *
	 * @see Wizard#createPages
	 */
	@Override
	public void addPages() {
		addPage(mainPage = new ConvertToAutotoolsProjectWizardPage(getPrefix(), this));

		// Add the configuration selection page
		projectConfigurationPage = new CProjectPlatformPage(PREFIX);
		projectConfigurationPage.setTitle(AutotoolsUIPlugin.getResourceString(CONF_TITLE));
		projectConfigurationPage.setDescription(AutotoolsUIPlugin.getResourceString(CONF_DESC));
		addPage(projectConfigurationPage);

		// Add the options (tabbed) page
		optionPage = new NewAutotoolsProjectOptionPage(PREFIX);
		optionPage.setTitle(AutotoolsUIPlugin.getResourceString(OPTIONS_TITLE));
		optionPage.setDescription(AutotoolsUIPlugin.getResourceString(OPTIONS_DESC));
		addPage(optionPage);

		// add custom pages
		MBSCustomPageManager.init();

		// add stock pages
		MBSCustomPageManager.addStockPage(fMainPage, NewCProjectWizardPage.PAGE_ID);
		MBSCustomPageManager.addStockPage(projectConfigurationPage, CProjectPlatformPage.PAGE_ID);
		MBSCustomPageManager.addStockPage(optionPage, NewAutotoolsProjectOptionPage.PAGE_ID);
	}

	@Override
	public String getProjectID() {
		return ManagedBuilderCorePlugin.MANAGED_MAKE_PROJECT_ID;
	}

	public IProjectType getProjectType() {
		return projectConfigurationPage.getProjectType();
	}

	public IConfiguration[] getSelectedConfigurations() {
		return projectConfigurationPage.getSelectedConfigurations();
	}

	protected void setCurrentProject(IProject project) {
		curProject = project;
	}

	public IProject getProject() {
		return curProject;
	}

	@SuppressWarnings("deprecation")
	public void applyOptions(IProject project, IProgressMonitor monitor) {
		// When applying the project options, we need to specify which
		// project because the conversion wizard allows us to convert
		// more than one project at once.  We accomplish this by setting
		// the current project we are working on.  The optionPage when
		// applying options will ask the wizard (us) to get the project which
		// we will report is the current project being converted.
		setCurrentProject(project);
		optionPage.performApply(monitor);
	}

	@Override
	protected void doRun(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(
				AutotoolsUIPlugin.getResourceString("WizardAutotoolsProjectConversion.monitor.convertingToMakeProject"), //$NON-NLS-1$
				2);
		try {
			super.doRun(SubMonitor.convert(monitor, 5));
		} finally {
			monitor.done();
		}

	}

	@Override
	protected void doRunPrologue(IProgressMonitor monitor) {
		monitor.done();
	}

	@Override
	protected void doRunEpilogue(IProgressMonitor monitor) {
		// Get my initializer to run
		//		if (project == null)
		//			return;
		//
		//		IStatus initResult = ManagedBuildManager.initBuildInfoContainer(project);
		//		if (initResult.getCode() != IStatus.OK) {
		//			// At this point, I can live with a failure
		//			ManagedBuilderUIPlugin.log(initResult);
		//		}

		// execute any operations specified by custom pages
		IRunnableWithProgress operations[] = MBSCustomPageManager.getOperations();

		if (operations != null) {
			SubMonitor submonitor = SubMonitor.convert(monitor, operations.length);
			for (int k = 0; k < operations.length; k++) {
				try {
					operations[k].run(submonitor.split(1));
				} catch (InvocationTargetException | InterruptedException e) {
					//TODO: what should we do?
				}
			}
		}
	}
}
