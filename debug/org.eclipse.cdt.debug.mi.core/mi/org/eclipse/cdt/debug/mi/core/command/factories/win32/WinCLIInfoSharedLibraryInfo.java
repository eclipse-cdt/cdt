/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories.win32; 

import java.util.List;
import org.eclipse.cdt.debug.mi.core.output.CLIInfoSharedLibraryInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIShared;
 
/**
 * Comment for .
 */
public class WinCLIInfoSharedLibraryInfo extends CLIInfoSharedLibraryInfo {

	public WinCLIInfoSharedLibraryInfo( MIOutput out ) {
		super( out );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.output.CLIInfoSharedLibraryInfo#parseShared(java.lang.String, java.util.List)
	 */
	protected void parseShared( String str, List aList ) {
		// skip the header (DLL   Name)
		if ( !str.startsWith( "DLL" ) ) { //$NON-NLS-1$
			String from = ""; //$NON-NLS-1$
			String to = ""; //$NON-NLS-1$
			boolean syms = true;
			int index = str.lastIndexOf( ' ' );
			if ( index > 0 ) {
				String sub = str.substring( index ).trim();
				// Go figure they do not print the "0x" to indicate hexadecimal!!
				if ( !sub.startsWith( "0x" ) ) { //$NON-NLS-1$
					sub = "0x" + sub; //$NON-NLS-1$
				}
				from = sub;
				str = str.substring( 0, index ).trim();
			}
			MIShared s = new MIShared( from, to, syms, str.trim() );
			aList.add( s );
		}
	}
}
