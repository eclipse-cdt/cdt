/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Takuya Miyamoto - Adapted from org.eclipse.team.examples.filesystem / FileSystemProviderType
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.filesystem;

import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.RepositoryProviderType;

/**
 * The file system repository provider types
 */
public class FileSystemProviderType extends RepositoryProviderType {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.core.RepositoryProviderType#getProjectSetCapability()
	 */
	@Override
	public ProjectSetCapability getProjectSetCapability() {
		// Create an empty project set capability to test backwards
		// compatibility
		return new ProjectSetCapability() {
		};
	}

}
