/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc - Add setTargets method
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import org.eclipse.cdt.make.internal.core.MakeTargetManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface for {@link MakeTargetManager} handling make target items in Make Targets View.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMakeTargetManager {
	IMakeTarget createTarget(IProject project, String targetName, String targetBuilderID) throws CoreException;
	/**
	 * Adds target to manager.
	 */
	void addTarget(IMakeTarget target) throws CoreException;
	
	/**
	 * Adds target to manager on a specific projects folder. It is assumed
	 * that the target and container belong to the same project.
	 */
	void addTarget(IContainer container, IMakeTarget target) throws CoreException;
	void removeTarget(IMakeTarget target) throws CoreException;
	void renameTarget(IMakeTarget target, String name) throws CoreException;
	
	/**
	 * Set targets on a specific projects folder.  It is assumed
	 * all targets and container belong to the same project which
	 * is determined from the first element of the
	 * targets array.  If no container is specified, the project is used.
	 * All previous targets for the container are replaced upon success and if
	 * failure occurs, an exception is thrown and the previous set of targets
	 * for the container are unchanged.
	 * 
	 * @param container to set targets for or null if project should be used
	 * @param targets array
	 * 
	 * @since 7.0
	 */
	public void setTargets(IContainer container, IMakeTarget[] targets) throws CoreException;
	
	boolean targetExists(IMakeTarget target);
	
	IMakeTarget[] getTargets(IContainer container) throws CoreException;
	IMakeTarget findTarget(IContainer container, String name) throws CoreException;

	IProject[]    getTargetBuilderProjects() throws CoreException;
	
	String getBuilderID(String targetBuilderID);
	
	boolean hasTargetBuilder(IProject project);
	String[] getTargetBuilders(IProject project);
				
	void addListener(IMakeTargetListener listener);
	void removeListener(IMakeTargetListener listener);
}
