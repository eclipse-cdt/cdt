/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.ui.dialogs.AbstractErrorParserBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.widgets.Composite;

/**
 * @deprecated as of CDT 4.0. This tab was used to set preferences/properties
 * for 3.X style projects.
 */
@Deprecated
public class ErrorParserBlock extends AbstractErrorParserBlock {

	// make builder enabled
	IMakeBuilderInfo fBuildInfo;
	boolean useBuildInfo = false;
	Preferences fPrefs;

	public ErrorParserBlock(Preferences preferences) {
		super();
		fPrefs = preferences;
	}

	@Override
	public void createControl(Composite parent) {

		if (useBuildInfo == true && fBuildInfo == null) {
			Composite composite = ControlFactory.createComposite(parent, 1);
			setControl(composite);
			ControlFactory.createEmptySpace(composite);
			ControlFactory.createLabel(composite,
					MakeUIPlugin.getResourceString("ErrorParserBlock.label.missingBuilderInformation")); //$NON-NLS-1$
			return;
		}
		super.createControl(parent);
	}

	@Override
	protected String[] getErrorParserIDs(IProject project) {
		if (getContainer().getProject() != null && fBuildInfo == null) {
			try {
				fBuildInfo = MakeCorePlugin.createBuildInfo(getContainer().getProject(), MakeBuilder.BUILDER_ID);
			} catch (CoreException e) {
			}
		}
		if (fBuildInfo != null) {
			return fBuildInfo.getErrorParsers();
		}
		return new String[0];
	}

	@Override
	public void saveErrorParsers(IProject project, String[] parserIDs) throws CoreException {
		if (getContainer().getProject() != null) {
			try {
				fBuildInfo = MakeCorePlugin.createBuildInfo(getContainer().getProject(), MakeBuilder.BUILDER_ID);
			} catch (CoreException e) {
			}
		}
		if (fBuildInfo != null) {
			fBuildInfo.setErrorParsers(parserIDs);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.AbstractErrorParserBlock#saveErrorParsers(java.lang.String[])
	 */
	@Override
	protected void saveErrorParsers(String[] parserIDs) throws CoreException {
		fBuildInfo = MakeCorePlugin.createBuildInfo(fPrefs, MakeBuilder.BUILDER_ID, false);
		fBuildInfo.setErrorParsers(parserIDs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.AbstractErrorParserBlock#getErrorParserIDs(boolean)
	 */
	@Override
	protected String[] getErrorParserIDs(boolean defaults) {
		fBuildInfo = MakeCorePlugin.createBuildInfo(fPrefs, MakeBuilder.BUILDER_ID, defaults);
		return fBuildInfo.getErrorParsers();
	}

	@Override
	public void setContainer(ICOptionContainer container) {
		super.setContainer(container);
		if (getContainer().getProject() != null) {
			try {
				fBuildInfo = MakeCorePlugin.createBuildInfo(getContainer().getProject(), MakeBuilder.BUILDER_ID);
			} catch (CoreException e) {
			}
			useBuildInfo = true;
		} else {
		}
	}

}
