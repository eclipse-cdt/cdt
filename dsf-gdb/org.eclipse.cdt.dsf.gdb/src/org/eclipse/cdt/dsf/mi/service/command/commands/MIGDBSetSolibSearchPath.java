/*******************************************************************************
 * Copyright (c) 2009, 2017 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Ingenico	- solib-search-path with space fails (Bug 516227)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

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
		super(ctx,
			new String[] {"solib-search-path", concat(paths, System.getProperty("path.separator", ":"))}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			x-> new MINoChangeAdjustable(x));
	}

	private static String concat(String[] paths, String sep) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < paths.length; i++) {
			if (buffer.length() == 0) {
				buffer.append(paths[i]);
			} else {
				buffer.append(sep).append(paths[i]);
			}
		}

		return buffer.toString();
	}
}
