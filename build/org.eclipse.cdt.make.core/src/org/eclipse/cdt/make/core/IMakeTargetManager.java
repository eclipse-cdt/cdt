/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public interface IMakeTargetManager {
	IMakeTarget createTarget(IProject project, String targetName, String targetBuilderID) throws CoreException;
	void addTarget(IContainer container, IMakeTarget target) throws CoreException;
	void removeTarget(IMakeTarget target) throws CoreException;
	void renameTarget(IMakeTarget target, String name) throws CoreException;

	IMakeTarget[] getTargets(IContainer container) throws CoreException;
	IMakeTarget findTarget(IContainer container, String name) throws CoreException;

	IProject[]    getTargetBuilderProjects() throws CoreException;
	
	String getBuilderID(String targetBuilderID);
	
	boolean hasTargetBuilder(IProject project);
	String[] getTargetBuilders(IProject project);
				
	void addListener(IMakeTargetListener listener);
	void removeListener(IMakeTargetListener listener);
}
