/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
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
package org.eclipse.cdt.make.ui.wizards;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.ui.wizards.conversion.ConversionWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.wizard.Wizard;

/**
 * This wizard provides a method by which the user can
 * add a C nature to a project that previously had no nature associated with it.
 *
 * This wizard was used for 3.X style projects. It is left here for compatibility
 * reasons only. The wizard is superseded by MBS Project Conversion Wizard,
 * class {@code org.eclipse.cdt.managedbuilder.ui.wizards.ConvertToMakeWizard}.
 *
 * @deprecated as of CDT 4.0.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@Deprecated
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
	 * @see Wizard#addPages
	 */
	@Override
	public void addPages() {
		addPage(mainPage = new ConvertToMakeProjectWizardPage(getPrefix()));
	}

	@Override
	public String getProjectID() {
		return MakeCorePlugin.MAKE_PROJECT_ID;
	}

	@Override
	protected void doRun(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(MakeUIPlugin.getResourceString("WizardMakeProjectConversion.monitor.convertingToMakeProject"), //$NON-NLS-1$
				2);
		try {
			super.doRun(new SubProgressMonitor(monitor, 1));
		} finally {
			monitor.done();
		}
	}
}
