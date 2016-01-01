/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command.factories.win32;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * CygwinMIEnvironmentCD
 */
public class CygwinMIEnvironmentCD extends WinMIEnvironmentCD {

	CygwinMIEnvironmentCD( String miVersion, String path ) {
		super( miVersion, path );
		// Use the cygpath utility to convert the path
		ICommandLauncher launcher = new CommandLauncher();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		String newPath = null;
		try {
			launcher.execute( new Path( "cygpath" ), //$NON-NLS-1$
			new String[]{ "-u", path }, //$NON-NLS-1$
			new String[0], new Path( "." ), //$NON-NLS-1$
			null);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		if ( launcher.waitAndRead( out, err ) == ICommandLauncher.OK ) {
			newPath = out.toString();
			if ( newPath != null ) {
				newPath = newPath.trim();
				if ( newPath.length() > 0 ) {
					path = newPath;
				}
			}
		}
		try {
			out.close();
			err.close();
		}
		catch( IOException e ) {
			// ignore.
		}
		setParameters( new String[]{ path } );
	}
}
