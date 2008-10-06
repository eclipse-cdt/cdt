/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.gdb.internal.provisional.service.command;

import org.eclipse.dd.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.dd.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.dd.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.dd.dsf.debug.service.ISignals.ISignalsDMContext;
import org.eclipse.dd.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.dd.mi.service.command.MIControlDMContext;

/**
 * 
 */
public class GDBControlDMContext extends MIControlDMContext
    implements ISymbolDMContext, IBreakpointsTargetDMContext, ISourceLookupDMContext, 
        ISignalsDMContext, IDisassemblyDMContext 
{

    public GDBControlDMContext(String sessionId, String commandControlId) {
        super(sessionId, commandControlId);
    }

}
