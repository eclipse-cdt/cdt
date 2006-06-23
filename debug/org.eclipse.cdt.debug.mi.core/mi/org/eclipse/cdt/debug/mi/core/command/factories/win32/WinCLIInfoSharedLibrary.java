/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories.win32; 

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.command.CLIInfoSharedLibrary;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
 
/**
 * Windows version of "info shared library".
 */
public class WinCLIInfoSharedLibrary extends CLIInfoSharedLibrary {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.CLIInfoSharedLibrary#getMIInfo()
	 */
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if ( out != null ) {
			info = new WinCLIInfoSharedLibraryInfo( out );
			if ( info.isError() ) {
				throwMIException( info, out );
			}
		}
		return info;
	}
}
