/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.wizards;


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

	private static final String WZ_TITLE = "WizardMakeProjectConversion.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "WizardMakeProjectConversion.description"; //$NON-NLS-1$
	private static final String PREFIX = "WizardMakeConversion"; //$NON-NLS-1$
	private static final String WINDOW_TITLE = "WizardMakeConversion.windowTitle"; //$NON-NLS-1$

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
	}

	public String getProjectID() {
		return MakeCorePlugin.MAKE_PROJECT_ID;
	}

	protected void doRun(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(MakeUIPlugin.getResourceString("WizardMakeProjectConversion.monitor.convertingToMakeProject"), 2); //$NON-NLS-1$
		try {
			super.doRun(new SubProgressMonitor(monitor, 1));
		} finally {
			monitor.done();
		}
	}
}
