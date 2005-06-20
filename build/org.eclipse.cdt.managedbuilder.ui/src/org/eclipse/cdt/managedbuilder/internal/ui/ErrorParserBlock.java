/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Move to Make plugin
 * Intel Corp - Use in Managed Make system
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.ui.wizards.NewManagedProjectOptionPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.NewManagedProjectWizard;
import org.eclipse.cdt.ui.dialogs.AbstractErrorParserBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

public class ErrorParserBlock extends AbstractErrorParserBlock {

	public ErrorParserBlock() {
		super();
	}

	protected String[] getErrorParserIDs(IConfiguration config) {
		// Get the list of error parsers specified with this Configuration
		String[] errorParsers = config.getErrorParserList();
		if (errorParsers != null) {
			return errorParsers;
		}
		else {
			// If no error parsers are specified by the configuration, the default is 
			// all error parsers
			return CCorePlugin.getDefault().getAllErrorParsersIDs();
		}
	}
	
	protected String[] getErrorParserIDs(IProject project) {
		IConfiguration config = ManagedBuildManager.getSelectedConfiguration(project);
		if (config == null) {
			//  This case occurs when modifying the properties of an existing
			//  managed build project, and the user selects the error parsers
			//  page before the "C/C++ Build" page.

			// Get the build information
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			config = info.getDefaultConfiguration();
		}
		if (config != null) {
			return getErrorParserIDs(config);
		} else {
			return CCorePlugin.getDefault().getAllErrorParsersIDs();
		}
	}

	protected String[] getErrorParserIDs() {
		//  Get the currently selected configuration from the page's container
		//  This is invoked by the managed builder new project wizard before the
		//  project is created.
		ICOptionContainer container = getContainer();
		if (container instanceof NewManagedProjectOptionPage) {
			NewManagedProjectOptionPage parent = (NewManagedProjectOptionPage)getContainer();
			NewManagedProjectWizard wizard = (NewManagedProjectWizard)parent.getWizard();
			// TODO:  This is wrong since the Wizard does not have a selected configuration!
			IProjectType proj = wizard.getSelectedProjectType();
			IConfiguration[] configs = proj.getConfigurations();
			if (configs.length > 0)
				return getErrorParserIDs(configs[0]);
			else
				return new String[0];
		}
		return CCorePlugin.getDefault().getAllErrorParsersIDs();
	}

	public void saveErrorParsers(IProject project, String[] parsers) {
		IConfiguration config = ManagedBuildManager.getSelectedConfiguration(project);
		if (config != null) {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < parsers.length; i++) {
				if (i > 0) buf.append(';');
				buf.append(parsers[i]);
			}
			config.setErrorParserIds(buf.toString());
		}
	}

	public IPreferenceStore getPreferenceStore() {
		return null;
	}

	protected void setValues() {
	    super.setValues();
	    
	    // TODO:  This reset belongs in AbstractErrorParserBlock.java? 
		//  Reset the "dirty" flag
	    listDirty = false;
	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
	    super.performApply(monitor);
	    
	    // TODO:  This reset belongs in AbstractErrorParserBlock.java? 
		//  Reset the "dirty" flag
	    listDirty = false;
	    
	}

	/**
	 * Sets the "dirty" state
	 */
	public void setDirty(boolean b) {
	    listDirty = b;
	}

	/**
	 * Returns the "dirty" state
	 */
	public boolean isDirty() {
	    return listDirty;
	}
}
