/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.gdb.internal.service.command.output.MIMetaGetCPUInfoInfo;

/**
 * Meta MI command to fetch CPU info from the target.
 * @since 4.1
 */
public class MIMetaGetCPUInfo implements ICommand<MIMetaGetCPUInfoInfo> {

	private final ICommandControlDMContext fCtx;

	public MIMetaGetCPUInfo(ICommandControlDMContext ctx) {
		fCtx = ctx;
	}

	@Override
	public ICommand<? extends ICommandResult> coalesceWith( ICommand<? extends ICommandResult> command ) {
		return null ;
	}  

	@Override
	public IDMContext getContext(){
		return fCtx;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (!(other.getClass().equals(getClass()))) return false;

		// Since other is the same class is this, we are sure it is of type MIMetaGetCPUInfo also
		MIMetaGetCPUInfo otherCmd = (MIMetaGetCPUInfo)other;
		return fCtx == null ? otherCmd.fCtx == null : fCtx.equals(otherCmd.fCtx);	
	}

	@Override
	public int hashCode() {
		return fCtx == null ? getClass().hashCode() : getClass().hashCode() ^ fCtx.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getName() + (fCtx == null ? "null" : fCtx.toString()); //$NON-NLS-1$
	}
}
