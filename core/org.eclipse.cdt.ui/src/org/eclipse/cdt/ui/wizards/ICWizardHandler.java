/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizardPage;

public interface ICWizardHandler extends Cloneable {
	/**
	 * Called when user selects corresponding item in wizard tree
	 * 
	 * @parame pane - parent for handler-specific data    
	 */
	public void handleSelection();
	/**
	 * Called when user leaves corresponding item in wizard tree 
	 */
	public void handleUnSelection();
	/**
	 * @return text for label above handler-specific pane
	 */
	public String getHeader();
	/**
	 * @return text for label in left tree
	 */
	public String getName();
	/**
	 * @return 1st handler-specific page
	 */
	public IWizardPage getSpecificPage();
	/**
	 * Creates project
	 * 
	 * @param proj - simple project to be used as base
	 * @param defaults - true if called from 1st Wizard page
	 * @throws CoreException
	 */
	public void createProject(IProject proj, boolean defaults) throws CoreException;
	/**
	 * Called when Finish button pressed, 
	 * even if project was created before.
	 * @param proj
	 */
	public void postProcess(IProject proj);
	/**
	 * @return true if only supported project types and toolchains are displayed
	 */
	public boolean supportedOnly();
	/**
	 * Defines whether only supported project types and toolchains are displayed
	 * @param supp 
	 */
	public void setSupportedOnly(boolean supp);
	/**
	 * @return true if handler is able to process preferred toolchains
	 */
	public boolean supportsPreferred();
	/**
	 * Asks handler to update its data according to preferred list.
	 * Usually, marks preferred toolchains somehow (icon, font etc)
	 * @param prefs - list of strings (preferred Toolchain IDs)
	 */
	public void updatePreferred(List prefs);
	/**
	 * @return null if data is consistent
	 *         else returns error message 
	 */
	public String getErrorMessage();
	/**
	 * Stores current internal settings 
	 */
	public void saveState();
	/**
	 * 
	 * @return true if settings were changed 
	 *         since last call to saveState()
	 */
	public boolean isChanged();
	/**
	 * Checks whether this item can be added to Wizard tree
	 * 
	 * @param data - Wizard Item data to be added 
	 *               as child to current Wizard item
	 * @return - true if item can be added.
	 */
	public boolean isApplicable(EntryDescriptor data);
	/**
	 * Initializes the handler to be used for the specified entry
	 * 
	 * @param data - Wizard Item data to be handled 
	 * @throws CoreException
	 */
	public void initialize(EntryDescriptor data) throws CoreException;
	
	public boolean canFinich();
	
	public Object clone();
}
