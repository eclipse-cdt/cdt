/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -file-exec-file FILE
 *
 *  Specify the executable file to be debugged.  Unlike
 * `-file-exec-and-symbols', the symbol table is _not_ read from this
 * file.  If used without argument, GDB clears the information about the
 * executable file.  No output is produced, except a completion
 * notification.
 * 
 */
public class MIFileExecFile extends MICommand 
{
	public MIFileExecFile(String miVersion, String file) {
		super(miVersion, "-file-exec-file", new String[]{file}); //$NON-NLS-1$
	}
}
