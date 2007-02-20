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
import org.eclipse.swt.graphics.Image;


public interface ICWizardHandler {
	static final String ARTIFACT = "org.eclipse.cdt.build.core.buildArtefactType";  //$NON-NLS-1$

	void handleSelection();
	void handleUnSelection();
	String getName();
	Image getIcon();
	String getHeader();
	void createProject(IProject proj, IConfiguration[] cfgs, String[] names) throws CoreException;
	boolean isDummy();
	boolean needsConfig();
	
	public IToolChain[] getSelectedToolChains();
	public int getToolChainsCount();
	public IProjectType getProjectType();
	public void setShowProperties(boolean show);
	public boolean showProperties();
	public String getPropertyId();
	public boolean supportedOnly();
	public void setSupportedOnly(boolean supp);
	/**
	 * @return true if project can be created with zero toolchains selected
	 */
	public boolean canCreateWithoutToolchain();

}
