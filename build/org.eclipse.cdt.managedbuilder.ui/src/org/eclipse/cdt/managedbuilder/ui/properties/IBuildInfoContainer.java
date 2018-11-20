/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

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
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBuildInfoContainer {
	public IScannerConfigBuilderInfo2 getBuildInfo();

	public CfgInfoContext getContext();

	public ICConfigurationDescription getConfiguration();

	public IProject getProject();
}
