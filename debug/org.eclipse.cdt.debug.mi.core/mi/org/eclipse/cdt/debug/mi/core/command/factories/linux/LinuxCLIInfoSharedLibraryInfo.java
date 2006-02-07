/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories.linux;

import java.util.List;
import org.eclipse.cdt.debug.mi.core.output.CLIInfoSharedLibraryInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIShared;

/**
 * Linux specific parser of the "info shared" output.
 */
public class LinuxCLIInfoSharedLibraryInfo extends CLIInfoSharedLibraryInfo {

	/**
	 * Constructor for LinuxCLIInfoSharedLibraryInfo.
	 */
	public LinuxCLIInfoSharedLibraryInfo( MIOutput out ) {
		super( out );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.output.CLIInfoSharedLibraryInfo#parseShared(java.lang.String, java.util.List)
	 */
	protected void parseShared( String str, List aList ) {
		if ( str.length() > 0 && !str.startsWith( "From" ) ) { //$NON-NLS-1$
			// Pass the header
			int index = -1;
			String from = ""; //$NON-NLS-1$
			String to = ""; //$NON-NLS-1$
			boolean syms = false;
			String name = ""; //$NON-NLS-1$
			for( int i = 0; (index = str.lastIndexOf( ' ' )) != -1 || i <= 3; i++ ) {
				if ( index == -1 ) {
					index = 0;
				}
				String sub = str.substring( index ).trim();
				// move to previous column
				str = str.substring( 0, index ).trim();
				switch( i ) {
					case 0:
						name = sub;
						break;
					case 1:
						if ( sub.equalsIgnoreCase( "Yes" ) ) { //$NON-NLS-1$
							syms = true;
						}
						break;
					case 2: // second column is "To"
						to = sub;
						break;
					case 3: // first column is "From"
						from = sub;
						break;
				}
			}
			if ( name.length() > 0 ) {
				MIShared s = new MIShared( from, to, syms, name );
				aList.add( s );
			}
		}
	}
}
