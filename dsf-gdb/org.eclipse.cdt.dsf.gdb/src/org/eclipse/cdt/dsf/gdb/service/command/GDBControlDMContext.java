/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 335324)
 *     Marc Khouzam (Ericsson) - Include IHardwareTargetDMContext for the multicore visualizer (Bug 335027)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.ISignals.ISignalsDMContext;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.IHardwareTargetDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.MIControlDMContext;

/**
 *
 */
public class GDBControlDMContext extends MIControlDMContext implements ISymbolDMContext, ISourceLookupDMContext,
		ISignalsDMContext, ITraceTargetDMContext, IHardwareTargetDMContext {

	public GDBControlDMContext(String sessionId, String commandControlId) {
		super(sessionId, commandControlId);
	}

}
