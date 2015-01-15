package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
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
public class CLISharedLibrary extends CLICommand<MIInfo> {
	
	public CLISharedLibrary(ICommandControlDMContext ctx) {
		super(ctx, "sharedlibrary"); //$NON-NLS-1$
	}

	public CLISharedLibrary(ICommandControlDMContext ctx, String regexName) {
		super(ctx, "sharedlibrary " + regexName); //$NON-NLS-1$
	}
}
