/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfoSharedLibraryInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *    sharedlibrary regex
 *
 */
public class MISharedLibrary extends CLICommand
{
	public MISharedLibrary(String lib) {
		super("sharedlibrary " + lib);
	}

}
