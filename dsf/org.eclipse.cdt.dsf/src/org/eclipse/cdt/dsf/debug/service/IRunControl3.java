/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson AB - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;

/**
 * This interface extends IRunControl2 to let a service support the
 * "Step into selection" command. 
 * @author Alvaro Sanchez-Leon
 * @since 2.4
 */
public interface IRunControl3 extends IRunControl2 {

	/**
	 * Returns whether the service can run the specified context to
	 * a source file and line number.
	 * 
	 * @param context the execution DM context
	 * @param rm the DataRequestMonitor that will return the result
	 */
	void canStepIntoSelection(IExecutionDMContext context, DataRequestMonitor<Boolean> rm);
	
	/**
	 * Request to run the program up to the specified location.
	 * If skipBreakpoints is false, any other breakpoint will stop this
	 * command; while if skipBreakpoints is true, the operation will ignore
	 * other breakpoints and continue until the specified location.
	 * 
	 * @param context the execution DM context
	 * @param sourceFile the source file path, mapped to a debugger path if possible.
	 * @param lineNumber the line number offset into (one-based) the source file
	 * @param skipBreakpoints skip breakpoints while performing this operation
	 * @param rm the Request Monitor
	 * @param selectedFunction
	 */
	void stepIntoSelection(IExecutionDMContext context, String sourceFile, int lineNumber, boolean skipBreakpoints, RequestMonitor rm, IFunctionDeclaration selectedFunction);

}
