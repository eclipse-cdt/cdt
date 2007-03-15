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

import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;


public interface ICWizardHandler {
	static final String ARTIFACT = "org.eclipse.cdt.build.core.buildArtefactType";  //$NON-NLS-1$

	void handleSelection();
	void handleUnSelection();
	String getName();
	Image getIcon();
	String getHeader();
	void createProject(IProject proj, CfgHolder[] cfgs) throws CoreException;
	boolean isDummy();
	public IToolChain[] getSelectedToolChains();
	public int getToolChainsCount();
	public IProjectType getProjectType();
	public String getPropertyId();
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
	 * @return true if project can be created with zero toolchains selected
	 */
	public boolean canCreateWithoutToolchain();
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
	
}
