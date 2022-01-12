/*******************************************************************************
 * Copyright (c) 2006, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IStepVisitor {
	/**
	 * This constant can is returneed by the step visitor
	 * to tell the visitor mechanism to continue step visiting process
	 */
	public static final int VISIT_CONTINUE = 1;

	/**
	 * This constant can is returneed by the step visitor
	 * to tell the visitor mechanism to stop step visiting process
	 */
	public static final int VISIT_STOP = 2;

	/**
	 * this call-back method is called by the build description
	 * visitor mechanism for each step in the build description
	 * The method should return one of the IStepVisitor.VISIT_xxx constants
	 *
	 * @see IStepVisitor#VISIT_CONTINUE
	 * @see IStepVisitor#VISIT_STOP
	 * @see BuildDescriptionManager#accept(IStepVisitor, IBuildDescription, boolean)
	 */
	int visit(IBuildStep step) throws CoreException;
}
