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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IMakeTarget extends IAdaptable {
	String getName();
	String getTargetBuilderID();
	
	boolean isStopOnError();
	void setStopOnError(boolean stopOnError) throws CoreException;

	boolean isDefaultBuildCmd();
	void setUseDefaultBuildCmd(boolean useDefault) throws CoreException;

	void setBuildTarget(String target) throws CoreException;
	String getBuildTarget() ;
	
	IPath getBuildCommand();
	void setBuildCommand(IPath command) throws CoreException;
	String getBuildArguments();
	void setBuildArguments(String arguments) throws CoreException;

	void setRunAllBuilders(boolean runAllBuilders);
	boolean runAllBuilders();
	
	/**
	 * Get the target build container.
	 * 
	 * @return IContainer of where target build will be invoked. 
	 */
	IContainer getContainer();
	
	/**
	 * Make this target temporary on the container, this target will not be persisted, 
	 * and may not be added to the IMakeTargetManager. 
	 * @param container
	 */
	void setContainer(IContainer container);
	
	void build(IProgressMonitor monitor) throws CoreException;
}
