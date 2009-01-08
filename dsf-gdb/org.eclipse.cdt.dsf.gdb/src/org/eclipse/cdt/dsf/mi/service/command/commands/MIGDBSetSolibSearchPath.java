/*******************************************************************************
 * Copyright (c) 2008  Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.MIControlDMContext;

/**
 * 
 *     -gdb-set solib-search-path COLON-SEPARATED-PATH
 * 
 */
public class MIGDBSetSolibSearchPath extends MIGDBSet 
{
	/**
     * @since 1.1
     */
	public MIGDBSetSolibSearchPath(ICommandControlDMContext ctx, String[] paths) {
		super(ctx, null);
		// Overload the parameter
		String sep = System.getProperty("path.separator", ":"); //$NON-NLS-1$ //$NON-NLS-2$
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < paths.length; i++) {
			if (buffer.length() == 0) {
				buffer.append(paths[i]);
			} else {
				buffer.append(sep).append(paths[i]);
			}
		}
		String[] p = new String [] {"solib-search-path", buffer.toString()}; //$NON-NLS-1$
		setParameters(p);
	}
    
    @Deprecated
    public MIGDBSetSolibSearchPath(MIControlDMContext ctx, String[] paths) {
        this ((ICommandControlDMContext)ctx, paths);
    }
}