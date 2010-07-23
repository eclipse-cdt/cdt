/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
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
 *      -gdb-set
 *
 *   Set an internal GDB variable.
 * 
 */
public class MIGDBSet extends MICommand 
{
	public MIGDBSet(String miVersion, String[] params) {
		super(miVersion, "-gdb-set", params); //$NON-NLS-1$
	}
	
	@Override
	protected String parametersToString() {
		/* gdb (at least up to 6.8) does not correctly process escaping for arguments.
		 * pass argument without escaping. Just in case only do it for simple cases only like -gdb-set variable value.
		 * For example set solib-search-path */
	    if (fParameters!=null && fParameters.length==2 && (fOptions==null || fOptions.length==0)) {
	    	return fParameters[0]+" "+fParameters[1]; //$NON-NLS-1$
	    }
	    return super.parametersToString();
	}
}
