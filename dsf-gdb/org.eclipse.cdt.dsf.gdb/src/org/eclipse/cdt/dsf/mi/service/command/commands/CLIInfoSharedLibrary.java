/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson			    - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;


import org.eclipse.cdt.dsf.debug.service.IModules.IModuleDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoSharedLibraryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * 
 *    info sharedlibrary
 *
 */
public class CLIInfoSharedLibrary extends CLICommand<CLIInfoSharedLibraryInfo> {
	
	public CLIInfoSharedLibrary(ISymbolDMContext ctx) {
		super(ctx, "info sharedlibrary"); //$NON-NLS-1$
	}
	public CLIInfoSharedLibrary(IModuleDMContext ctx) {
		super(ctx, "info sharedlibrary"); //$NON-NLS-1$
	}
	@Override
	public CLIInfoSharedLibraryInfo getResult(MIOutput output) {
		return (CLIInfoSharedLibraryInfo)getMIInfo(output);
	}
	
	public MIInfo getMIInfo(MIOutput out) {
		MIInfo info = null;
		if (out != null) {
			info = new CLIInfoSharedLibraryInfo(out);
		}
		return info;
	}
}
