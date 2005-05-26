/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.core.runtime.PluginVersionIdentifier;

public interface IBuildObject {
	// Schema element names
	public static final String ID = "id"; //$NON-NLS-1$
	public static final String NAME = "name"; //$NON-NLS-1$
	
	public String getId();
	public String getName();
	public PluginVersionIdentifier getVersion();
	public void setVersion(PluginVersionIdentifier version);
	public String getManagedBuildRevision();
}
