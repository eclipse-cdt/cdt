/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public interface IMakeTargetManager {
	IMakeTarget createTarget(String targetName, String targetID);
	void addTarget(IContainer container, IMakeTarget target) throws CoreException;
	void removeTarget(IMakeTarget target) throws CoreException;
	void renameTarget(IMakeTarget target, String name) throws CoreException;

	IMakeTarget[] getTargets(IContainer container) throws CoreException;
	IMakeTarget findTarget(IContainer container, String name);

	IProject[]    getTargetBuilderProjects() throws CoreException;
	
	String getBuilderID(String targetID);
	
	String[] getTargetBuilders(IProject project);
				
	void addListener(IMakeTargetListener listener);
	void removeListener(IMakeTargetListener listener);
}
