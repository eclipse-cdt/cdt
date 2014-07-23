/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.service.IDsfService;


public interface IGDBExtendedFunctions extends IDsfService {
	/**
	 * Request a printout by the debugger
	 */
	void print(String str, RequestMonitor rm);
	
	/**
	 * Get the version of the debugger
	 */
	void getVersion(ICommandControlDMContext ctx, DataRequestMonitor<String> rm);

}
