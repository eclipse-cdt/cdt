/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Move to Make plugin
***********************************************************************/
package org.eclipse.cdt.make.internal.ui;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;

public class ErrorParserBlock extends org.eclipse.cdt.ui.dialogs.ErrorParserBlock {

	public ErrorParserBlock(Preferences prefs) {
		super(prefs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ErrorParserBlock#getErrorParserIDs(org.eclipse.core.resources.IProject)
	 */
	protected String[] getErrorParserIDs(IProject project) {
		IMakeBuilderInfo info;
		try {
			info = MakeCorePlugin.createBuildInfo(project, MakeBuilder.BUILDER_ID);
		} catch (CoreException e) {
			return new String[0];
		}
		return info.getErrorParsers();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ErrorParserBlock#saveErrorParsers(org.eclipse.core.resources.IProject, java.lang.String[])
	 */
	public void saveErrorParsers(IProject project, String[] parserIDs) throws CoreException {
		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(project, MakeBuilder.BUILDER_ID);
		info.setErrorParsers(parserIDs);
	}

}
