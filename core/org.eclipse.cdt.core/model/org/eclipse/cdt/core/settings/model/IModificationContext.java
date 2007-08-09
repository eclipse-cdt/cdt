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
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;

public interface IModificationContext {
	IProject getProject();
	
	IProjectDescription getEclipseProjectDescription() throws CoreException;

	void setEclipseProjectDescription(IProjectDescription eDes) throws CoreException;

	void addWorkspaceRunnable(IWorkspaceRunnable runnable);
	
	/**
	 * the CConfigurationDataProvider can call this method to indicate whether the
	 * ICConfigurationDescription settings were changed 
	 * @param modified
	 */
	void setConfiguratoinDataModifiedState(boolean modified);

	boolean getBaseConfigurationDataCacheState();

}
