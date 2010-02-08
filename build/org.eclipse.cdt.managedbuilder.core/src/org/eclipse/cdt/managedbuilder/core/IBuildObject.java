/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.core.runtime.PluginVersionIdentifier;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBuildObject {
	// Schema element names
	public static final String ID = "id"; //$NON-NLS-1$
	public static final String NAME = "name"; //$NON-NLS-1$
	
	public String getId();
	public String getName();
	public String getBaseId();
	public PluginVersionIdentifier getVersion();
	public void setVersion(PluginVersionIdentifier version);
	public String getManagedBuildRevision();
}
