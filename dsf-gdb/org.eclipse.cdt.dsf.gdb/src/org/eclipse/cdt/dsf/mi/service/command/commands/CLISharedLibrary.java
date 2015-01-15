/*******************************************************************************
 * Copyright (c) 2015 QNX Software System and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Elena Laskavaia (QNX Software System) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * CLI command implement gdb sharedlibary command<br>
 * sharedlibrary regex<br>
 * Load shared object library symbols for files matching a Unix regular
 * expression. As with files loaded automatically, it only loads shared
 * libraries required by your program for a core file or after typing run. If
 * regex is omitted all shared libraries required by your program are loaded.
 * 
 * @since 4.6
 */
public class CLISharedLibrary extends MIInterpreterExecConsole<MIInfo> {
	private static final String SHARED_LIBRARY = "sharedlibrary";  //$NON-NLS-1$

	public CLISharedLibrary(ISymbolDMContext ctx) {
		super(ctx, SHARED_LIBRARY);
	}

	public CLISharedLibrary(ISymbolDMContext ctx, String name) {
		super(ctx, SHARED_LIBRARY + " " + name); //$NON-NLS-1$
	}
}
