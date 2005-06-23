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
package org.eclipse.cdt.make.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IMakeTarget extends IAdaptable, IMakeCommonBuildInfo {
	
	public final static String BUILD_TARGET = ARGS_PREFIX + ".build.target"; //$NON-NLS-1$

	String getName();
	String getTargetBuilderID();
	
	/**
	 * @deprecated
	 */
	void setBuildTarget(String target) throws CoreException;

	
	/**
	 * @deprecated
	 */
	String getBuildTarget() ;
	
	void setRunAllBuilders(boolean runAllBuilders) throws CoreException;
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
