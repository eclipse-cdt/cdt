/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.buildmodel;

import org.eclipse.core.runtime.CoreException;

/**
 *
 * This interface is used for the build description visitor mechanism
 * to represent the visitor
 * 
 * @see BuildDescriptionManager#accept(IStepVisitor, IBuildDescription, boolean)
 *
 */
public interface IStepVisitor {
	public static final int VISIT_CONTINUE = 1;
	public static final int VISIT_STOP = 2;
	
	/**
	 * this call-back method is called by the build description
	 * visitor mechanism for each step in the build description
	 * 
	 * @see BuildDescriptionManager#accept(IStepVisitor, IBuildDescription, boolean)
	 * 
	 * @param step
	 * @return
	 * @throws CoreException
	 */
	int visit(IBuildStep step) throws CoreException;
}
