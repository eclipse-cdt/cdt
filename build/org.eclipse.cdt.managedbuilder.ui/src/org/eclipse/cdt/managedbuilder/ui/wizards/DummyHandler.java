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
package org.eclipse.cdt.managedbuilder.ui.wizards;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * This object is created per each Project type
 *  
 * It is responsible for:
 * - corresponding line in left pane of 1st wizard page
 * - whole view of right pane, including 
 *
 */
public class DummyHandler implements ICWizardHandler {
	private static final String DUMMY = ""; //$NON-NLS-1$
	Composite parent;

	public DummyHandler(Composite p) {
		parent = p;
	}

// interface methods
	public String getHeader() { return DUMMY; }
	public String getName() { return DUMMY; }
	public Image getIcon() { return null; }	
	public void handleSelection() {}
	public void handleUnSelection() {}
	public IWizardPage getNextPage() { return null; }
	public void createProject(IProject project, IConfiguration[] cfgs, String[] names) throws CoreException {}
	public boolean isDummy() { return true; }
	public boolean needsConfig() { return false; }
	public IToolChain[] getSelectedToolChains() {return null;}
	public int getToolChainsCount() { return 0; }
	public void setShowProperties(boolean show) {}
	public boolean showProperties() { return false; }
	public IProjectType getProjectType() { return null; }
	public String getPropertyId() {	return null; }	
	public boolean canCreateWithoutToolchain() { return false; }
	public void setSupportedOnly(boolean supp) {}
	public boolean supportedOnly() { return true; }
	public boolean isFirstPageSelected() { return false; } 
}
