/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;

/**
 * This interface extends IRunControl2 to let a service support the
 * "Step into selection" command.
 * @since 2.4
 */
public interface IRunControl3 extends IRunControl2 {
	/**
	 * Returns whether the service is in the state to execute 'Step into selection'
	 * for the specified context
	 *
	 * @param context the execution DM context
	 * @param sourceFile the source file path, mapped to a debugger path if possible, invalid if selectedFunction is Null
	 * @param lineNumber the line number of the source file where the user selected the target function, invalid if selectedFunction is Null
	 * @param selectedFunction The target function to step into <br>NOTE: a null value shall disregard linenumber and sourceFile
	 * @param rm the DataRequestMonitor that will return the result
	 */
	void canStepIntoSelection(IExecutionDMContext context, String sourceFile, int lineNumber,
			IFunctionDeclaration selectedFunction, DataRequestMonitor<Boolean> rm);

	/**
	 * Request to run the program forward into the specified function
	 * if the current stop location is not at the specified lineNumber and
	 * skipBreakpoints is false, any other breakpoint found before the specified line number will stop this
	 * command; while if skipBreakpoints is true, the operation will ignore
	 * other breakpoints and continue until the specified location.
	 *
	 * @param context the execution DM context
	 * @param sourceFile the source file path, mapped to a debugger path if possible.
	 * @param lineNumber the line number of the source file where the user selected the target function
	 * @param skipBreakpoints skip breakpoints while performing this operation
	 * @param selectedFunction the target function to step into
	 * @param rm the DataRequestMonitor that will return the result
	 */
	void stepIntoSelection(IExecutionDMContext context, String sourceFile, int lineNumber, boolean skipBreakpoints,
			IFunctionDeclaration selectedFunction, RequestMonitor rm);
}
