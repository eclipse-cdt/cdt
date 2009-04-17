/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

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
	 */
	void runToLine(IExecutionDMContext context, String fileName, String lineNo, 
			       boolean skipBreakpoints, DataRequestMonitor<MIInfo> rm);
}

