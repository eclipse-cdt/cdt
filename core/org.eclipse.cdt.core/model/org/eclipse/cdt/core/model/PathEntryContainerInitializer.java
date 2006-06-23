/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 */
public abstract class PathEntryContainerInitializer {

	/**
	 * Creates a new cpath container initializer.
	 */
	public PathEntryContainerInitializer() {
	}

	public abstract void initialize(IPath containerPath, ICProject project) throws CoreException;

	 /**
     * Returns <code>true</code> if this container initializer can be requested to perform updates 
     * on its own container values. If so, then an update request will be performed using
     * <code>PathEntryContainerInitializer#requestPathEntryContainerUpdate</code>/
     * <p>
     * @param containerPath the path of the container which requires to be updated
     * @param project the project for which the container is to be updated
     * @return returns <code>true</code> if the container can be updated
     */
    public boolean canUpdatePathEntryContainer(IPath containerPath, ICProject project) {
    	
		// By default, path container initializers do not accept updating containers
    	return false; 
    }

	/**
	 * Request a registered container definition to be updated according to a container suggestion. The container suggestion 
	 * only acts as a place-holder to pass along the information to update the matching container definition(s) held by the 
	 * container initializer. In particular, it is not expected to store the container suggestion as is, but rather adjust 
	 * the actual container definition based on suggested changes.
	 * <p>
	 * IMPORTANT: In reaction to receiving an update request, a container initializer will update the corresponding
	 * container definition (after reconciling changes) at its earliest convenience, using 
	 * <code>CoreModel#setPathContainer(IPath, ICProject[], IPathEntryContainer[], IProgressMonitor)</code>. 
	 * Until it does so, the update will not be reflected in the Java Model.
	 * <p>
	 * In order to anticipate whether the container initializer allows to update its containers, the predicate
	 * <code>PathEntryContainerInitializer#canUpdatePathEntryContainer</code> should be used.
	 * <p>
	 * @param containerPath the path of the container which requires to be updated
     * @param project the project for which the container is to be updated
	 * @param containerSuggestion a suggestion to update the corresponding container definition
	 * @throws CoreException when <code>CoreModel#setPathEntryContainer</code> would throw any.
	 * @see CoreModel#setPathEntryContainer(IPath, ICProject[], IPathEntryContainer[], org.eclipse.core.runtime.IProgressMonitor)
	 * @see PathContainerInitializer#canUpdatePathContainer(IPath, ICProject)
	 */
    public void requestPathEntryContainerUpdate(IPath containerPath, ICProject project, IPathEntryContainer containerSuggestion) throws CoreException {

		// By default, path container initializers do not accept updating containers
    }
	
	
	public String getDescription(IPath containerPath, ICProject project) {
		// By default, a container path is the only available description
		return containerPath.makeRelative().toString();
	}

}
