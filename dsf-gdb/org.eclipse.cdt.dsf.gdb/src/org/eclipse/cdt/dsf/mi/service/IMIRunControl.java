/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl;

/**
 * This interface provides methods for RunControl that are not
 * part of the standard DSF IRunControl
 * 
 * @since 2.0
 */
public interface IMIRunControl extends IRunControl
{
	/**
	 * Request to run the program up to the specified location.
	 * If skipBreakpoints is false, any other breakpoint will stop this
	 * command; while if skipBreakpoints is true, the operation will ignore
	 * other breakpoints and continue until the specified location.
	 * 
	 * @since 3.0
	 */
	void runToLocation(IExecutionDMContext context, String location, boolean skipBreakpoints, RequestMonitor rm);
	
	/** 
	 * Request to resume the program starting at the specified location.
	 * The specified location can be anywhere in the program, but proper
	 * program behavior is not guaranteed after this operation.
	 * 
	 * @since 3.0 
	 */
	void resumeAtLocation(IExecutionDMContext context, String location, RequestMonitor rm);

}

