/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.newui;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.core.resources.IProject;

/**
 * This interface is used by dynamic discovery pages
 * to refer to underlying tab (DiscoveryTab)
 * and get all object-specific data.
 * 
 * In previous code, reference to specific 
 * property page was used instead of interface.
 */
public interface IBuildInfoContainer {
	public IScannerConfigBuilderInfo2 getBuildInfo();
	public CfgInfoContext getContext();
	public ICConfigurationDescription getConfiguration();
	public IProject getProject();
}
