package org.eclipse.cdt.make.ui.wizards;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.ui.wizards.conversion.ConversionWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * This wizard provides a method by which the user can 
 * add a C nature to a project that previously had no nature associated with it.
 */
public class ConvertToMakeProjectWizard extends ConversionWizard {

	private MakeProjectWizardOptionPage optionsPage;
	private static final String WZ_TITLE = "WizardMakeProjectConversion.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "WizardMakeProjectConversion.description"; //$NON-NLS-1$
	private static final String PREFIX = "WizardMakeConversion"; //$NON-NLS-1$
	private static final String WINDOW_TITLE = "WizardMakeConversion.windowTitle"; //$NON-NLS-1$

	private static final String WZ_SETTINGS_TITLE = "WizardMakeProjectConversionSettings.title"; //$NON-NLS-1$
	private static final String WZ_SETTINGS_DESC = "WizardMakeProjectConversionSettings.description"; //$NON-NLS-1$

	/**
	 * ConvertToStdMakeConversionWizard Wizard constructor
	 */
	public ConvertToMakeProjectWizard() {
		this(getWindowTitleResource(), getWzDescriptionResource());
	}
	/**
	 * ConvertToStdMakeConversionWizard Wizard constructor
	 * 
	 * @param title
	 * @param desc
	 */
	public ConvertToMakeProjectWizard(String title, String desc) {
		super(title, desc);
	}

	/**
	 * Method getWzDescriptionResource,  allows Wizard description label value
	 * to be changed by subclasses
	 * 
	 * @return String
	 */
	protected static String getWzDescriptionResource() {
		return MakeUIPlugin.getResourceString(WZ_DESC);
	}

	/**
	 * Method getWzTitleResource,  allows Wizard description label value
	 * to be changed by subclasses
	 * 
	 * @return String
	 */
	protected static String getWzTitleResource() {
		return MakeUIPlugin.getResourceString(WZ_TITLE);
	}

	/**
	 * Method getWindowTitleResource, allows Wizard Title label value to be
	 * changed by subclasses
	 * 
	 * @return String
	 */
	protected static String getWindowTitleResource() {
		return MakeUIPlugin.getResourceString(WINDOW_TITLE);
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
	public void addPages() {
		addPage(mainPage = new ConvertToMakeProjectWizardPage(getPrefix()));
		addPage(optionsPage = new MakeProjectWizardOptionPage(MakeUIPlugin.getResourceString(WZ_SETTINGS_TITLE), MakeUIPlugin.getResourceString(WZ_SETTINGS_DESC)));
	}

	public String getProjectID() {
		return MakeCorePlugin.getUniqueIdentifier() + ".make"; //$NON-NLS-1$
	}

	protected void doRun(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Converting to Make Project...", 2);
		super.doRun(new SubProgressMonitor(monitor, 1));
		optionsPage.performApply(new SubProgressMonitor(monitor, 1));
	}
}
