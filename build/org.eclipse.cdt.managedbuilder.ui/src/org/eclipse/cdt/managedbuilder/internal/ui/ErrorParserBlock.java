/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Move to Make plugin
 * Intel Corp - Use in Managed Make system
***********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.ui.wizards.NewManagedProjectOptionPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.NewManagedProjectWizard;
import org.eclipse.cdt.ui.dialogs.AbstractErrorParserBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;

public class ErrorParserBlock extends AbstractErrorParserBlock {

	public ErrorParserBlock() {
		super(null);
	}

	protected String[] getErrorParserIDs(ITarget target) {
		// Get the list of error parsers specified with this Target
		String[] errorParsers = target.getErrorParserList();
		if (errorParsers != null) {
			return errorParsers;
		}
		else {
			// If no error parsers are specified by the target, the default is 
			// all error parsers
			return CCorePlugin.getDefault().getAllErrorParsersIDs();
		}
	}
	
	protected String[] getErrorParserIDs(IProject project) {
		ITarget target = ManagedBuildManager.getSelectedTarget(project);
		if (target == null) {
			//  This case occurs when modifying the properties of an existing
			//  managed build project, and the user selects the error parsers
			//  page before the "C/C++ Build" page.

			// Get the build information
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			target = info.getDefaultTarget();
		}
		if (target != null) {
			return getErrorParserIDs(target);
		} else {
			return CCorePlugin.getDefault().getAllErrorParsersIDs();
		}
	}

	protected String[] getErrorParserIDs() {
		//  Get the currently selected target from the page's container
		//  This is invoked by the managed builder new project wizard before the
		//  project is created.
		ICOptionContainer container = getContainer();
		if (container instanceof NewManagedProjectOptionPage) {
			NewManagedProjectOptionPage parent = (NewManagedProjectOptionPage)getContainer();
			NewManagedProjectWizard wizard = (NewManagedProjectWizard)parent.getWizard();
			ITarget target = wizard.getSelectedTarget();
			return getErrorParserIDs(target);
		}
		return CCorePlugin.getDefault().getAllErrorParsersIDs();
	}

	public void saveErrorParsers(IProject project, String[] parsers) {
		ITarget target = ManagedBuildManager.getSelectedTarget(project);
		if (target != null) {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < parsers.length; i++) {
				if (i > 0) buf.append(';');
				buf.append(parsers[i]);
			}
			target.setErrorParserIds(buf.toString());
		}
	}
}
