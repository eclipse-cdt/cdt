/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public interface IMakeTargetManager {
	/**
	 * @param project
	 * @param targetName
	 * @param targetBuilderID
	 * @return
	 * @throws CoreException
	 * 
	 */
	IMakeTarget createTarget(IProject project, String targetName, String targetBuilderID) throws CoreException;
	
	IMakeTarget createTarget(IConfiguration cfg, String builderId, String targetBuilderID, String name) throws CoreException;
		
	/**
	 * Adds target to manager.
	 * @param target
	 * @throws CoreException
	 */
	void addTarget(IMakeTarget target) throws CoreException;
	
	/**
	 * Adds target to manager on a specific projects folder. 
	 * @param container
	 * @param target
	 * @throws CoreException
	 */
	void addTarget(IContainer container, IMakeTarget target) throws CoreException;
	void removeTarget(IMakeTarget target) throws CoreException;
	void renameTarget(IMakeTarget target, String name) throws CoreException;
	
	boolean targetExists(IMakeTarget target);
	/**
	 * @param container
	 * @return
	 * @throws CoreException
	 */
	IMakeTarget[] getTargets(IContainer container) throws CoreException;

	IMakeTarget[] getTargets(IConfiguration cfg, IContainer container) throws CoreException;

	/**
	 * @param container
	 * @param name
	 * @return
	 * @throws CoreException
	 */
	IMakeTarget findTarget(IContainer container, String name) throws CoreException;

	IMakeTarget findTarget(IConfiguration cfg, IContainer container, String name) throws CoreException;

	IProject[]    getTargetBuilderProjects() throws CoreException;
	
	String getBuilderID(String targetBuilderID);
	
	boolean hasTargetBuilder(IProject project);
	String[] getTargetBuilders(IProject project);
				
	void addListener(IMakeTargetListener listener);
	void removeListener(IMakeTargetListener listener);
}
