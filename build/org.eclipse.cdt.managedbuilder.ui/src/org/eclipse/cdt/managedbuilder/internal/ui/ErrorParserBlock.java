/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
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
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildPropertyPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.NewManagedProjectOptionPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.NewManagedProjectWizard;
import org.eclipse.cdt.ui.dialogs.AbstractErrorParserBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

public class ErrorParserBlock extends AbstractErrorParserBlock {
	private BuildPropertyPage parent;
	private String errorParsers[];
	
	public ErrorParserBlock(BuildPropertyPage parent) {
		super();
		this.parent = parent;
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
		
		IConfiguration config = null;
		if(parent != null)
			config = parent.getSelectedConfigurationClone();
		else if ((config = ManagedBuildManager.getSelectedConfiguration(project)) == null) {
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
		if(parent != null){
			return getErrorParserIDs(parent.getSelectedConfigurationClone());
		}
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
		IConfiguration config = null;
		if(parent != null)
			config = parent.getSelectedConfigurationClone();
		else
			config = ManagedBuildManager.getSelectedConfiguration(project); 
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
	
	protected boolean checkIds(String ids1[], String ids2[]){
		if(ids1.length != ids2.length)
			return true;

		for(int i = 0; i < ids1.length; i++){
			String id = ids1[i];
			int j;
			for(j = 0; j < ids2.length; j++){
				if(id.equals(ids2[j]))
					break;
			}
			
			if(j == ids2.length)
				return true;
		}
		
		return false;
	}

	protected void setValues() {
	    super.setValues();
	    
	    if(parent != null && parent.getSelectedConfigurationClone() != null)
	    	errorParsers = getErrorParserIDs(parent.getSelectedConfigurationClone());
	    
	    // TODO:  This reset belongs in AbstractErrorParserBlock.java? 
		//  Reset the "dirty" flag
	    listDirty = false;
	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
	    super.performApply(monitor);
	    
	    if(parent != null){
	    	IConfiguration realConfig = ManagedBuildManager.getSelectedConfiguration(parent.getProject());
	    	realConfig.setErrorParserIds(parent.getSelectedConfigurationClone().getErrorParserIds());
	    	errorParsers = getErrorParserIDs(parent.getSelectedConfigurationClone());
	    }
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
	
	public void setVisible(boolean visible){
		if(parent != null){
			if(visible){
				boolean dirtyState = listDirty;
				updateListControl(parent.getSelectedConfigurationClone().getErrorParserList());
				if(dirtyState != listDirty)
					listDirty = checkIds(parent.getSelectedConfigurationClone().getErrorParserList(),errorParsers);
			} else {
				try {
					super.performApply(null);
				} catch (CoreException e) {
				}
			}
		}
		super.setVisible(visible);
	}
	
	protected void setDefaults() {
		if(parent != null){
			IConfiguration cfg = parent.getSelectedConfigurationClone(); 
			cfg.setErrorParserIds(null);
			updateListControl(cfg.getErrorParserList());
			listDirty = true;
		} else
			super.setDefaults();
	}

}
