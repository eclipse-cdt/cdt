/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import org.eclipse.cdt.make.internal.core.MakeTargetManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * {@code IMakeTarget} represents a make target item in Make Targets View.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMakeTarget extends IAdaptable, IMakeCommonBuildInfo {
	public final static String BUILD_TARGET = ARGS_PREFIX + ".build.target"; //$NON-NLS-1$

	public String getName();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * Do not use this method to change target name, rather use {@link MakeTargetManager#renameTarget(IMakeTarget, String)}.
	 * This method is for internal use only.
	 * 
	 * @since 7.0
	 */
	public void setName(String name);
	
	public String getTargetBuilderID();
	
	public IProject getProject();
	
	/**
	 * Set build target
	 * 
	 * @deprecated as of CDT 3.0
	 */
	@Deprecated
	public void setBuildTarget(String target) throws CoreException;

	
	/**
	 * @deprecated as of CDT 3.0
	 * 
	 * @return build target
	 */
	@Deprecated
	public String getBuildTarget();
	
	public void setRunAllBuilders(boolean runAllBuilders) throws CoreException;

	public boolean runAllBuilders();
	
	/**
	 * Get the target build container.
	 * 
	 * @return IContainer of where target build will be invoked. 
	 */
	public IContainer getContainer();
	
	/**
	 * Make this target temporary on the container, this target will not be persisted, 
	 * and may not be added to the IMakeTargetManager. 
	 */
	public void setContainer(IContainer container);
	
	public void setAppendProjectEnvironment(boolean append);
	
	public boolean appendProjectEnvironment();
	
	public void build(IProgressMonitor monitor) throws CoreException;
}
