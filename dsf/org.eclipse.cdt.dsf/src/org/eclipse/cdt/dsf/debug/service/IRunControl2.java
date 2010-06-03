/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;

/**
 * This interface extends IRunControl to let a service support the
 * "Run to Line," "Move to Line," and "Resume at Line" commands. 
 * @since 2.1
 */
public interface IRunControl2 extends IRunControl {

	/**
	 * Returns whether the service can run the specified context to
	 * a source file and line number.
	 * 
	 * @param context the execution DM context
	 * @param sourceFile the source file path, mapped to a debugger path if possible.
	 * @param lineNumber the line number offset (one-based) into the source file
	 * @param rm the DataRequestMonitor that will return the result
	 */
	void canRunToLine(IExecutionDMContext context, String sourceFile, int lineNumber, DataRequestMonitor<Boolean> rm );

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
	 */
	void runToLine(IExecutionDMContext context, String sourceFile, int lineNumber, boolean skipBreakpoints, RequestMonitor rm);

	/**
	 * Returns whether the service can run the specified context to
	 * a specified address.
	 * 
	 * @param context the execution DM context
	 * @param address the address specifier
	 * @param rm the DataRequestMonitor that will return the result
	 */
	void canRunToAddress(IExecutionDMContext context, IAddress address, DataRequestMonitor<Boolean> rm );

	/**
	 * Request to run the program up to the specified address.
	 * If skipBreakpoints is false, any other breakpoint will stop this
	 * command; while if skipBreakpoints is true, the operation will ignore
	 * other breakpoints and continue until the specified location.
	 * 
	 * @param context the execution DM context
	 * @param address the address specifier
	 * @param skipBreakpoints the skip breakpoints
	 * @param rm the Request Monitor
	 */
	void runToAddress(IExecutionDMContext context, IAddress address, boolean skipBreakpoints, RequestMonitor rm);

	/**
	 * Determines if the service can move the program counter to the specified
	 * source location.
	 * 
	 * @param context the execution DM context
	 * @param sourceFile the source file path, mapped to a debugger path if possible.
	 * @param lineNumber the line number offset (one-based) into the source file
	 * @param resume resume execution after moving the PC
	 * @param rm the DataRequestMonitor that will return the result
	 */
	void canMoveToLine(IExecutionDMContext context, String sourceFile, int lineNumber, boolean resume, DataRequestMonitor<Boolean> rm );

	/**
	 * Moves the program counter for the specified context to the specified
	 * source location.
	 * 
	 * @param context the execution DM context
	 * @param sourceFile the source file path, mapped to a debugger path if possible.
	 * @param lineNumber the line number offset (one-based) into the source file
	 * @param resume resume execution after moving the PC
	 * @param rm the Request Monitor
	 */
	void moveToLine(IExecutionDMContext context, String sourceFile, int lineNumber, boolean resume, RequestMonitor rm );

	/**
	 * Determines if the service can move the program counter to the specified
	 * address.
	 * 
	 * @param context the execution DM context
	 * @param address the address specifier
	 * @param resume resume execution after moving the PC
	 * @param rm the DataRequestMonitor that will return the result
	 */
	void canMoveToAddress(IExecutionDMContext context, IAddress address, boolean resume, DataRequestMonitor<Boolean> rm );

	/**
	 * Moves the program counter for the specified context to the specified
	 * address.
	 * 
	 * @param context the execution DM context
	 * @param address the address specifier
	 * @param resume resume execution after moving the PC
	 * @param rm the Request Monitor
	 */
	void moveToAddress(IExecutionDMContext context, IAddress address, boolean resume, RequestMonitor rm );

}
