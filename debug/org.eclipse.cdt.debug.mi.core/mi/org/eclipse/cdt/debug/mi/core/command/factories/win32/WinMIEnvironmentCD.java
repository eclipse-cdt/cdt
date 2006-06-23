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

import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentCD;
 
/**
 * Comment for .
 */
public class WinMIEnvironmentCD extends MIEnvironmentCD {

	public WinMIEnvironmentCD( String miVersion, String path ) {
		super( miVersion, path );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#parametersToString()
	 */
	protected String parametersToString() {
		String[] params = getParameters();
		if ( params != null && params.length == 1 ) {
			StringBuffer sb = new StringBuffer();
			// We need to escape the double quotes and the backslash.
			String param = params[0];
			for( int j = 0; j < param.length(); j++ ) {
				char c = param.charAt( j );
				if ( c == '"' || c == '\\' ) {
					sb.append( '\\' );
				}
				sb.append( c );
			}
			// If the string contains spaces instead of escaping
			// surround the parameter with double quotes.
			if ( containsWhitespace( param ) ) {
				sb.insert( 0, '"' );
				sb.append( '"' );
			}
			return sb.toString().trim();
		}
		return super.parametersToString();
	}
}
