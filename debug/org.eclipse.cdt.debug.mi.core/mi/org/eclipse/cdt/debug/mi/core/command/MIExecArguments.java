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
 *      -exec-arguments ARGS
 *
 *   Set the inferior program arguments, to be used in the next
 *  `-exec-run'.
 * 
 */
public class MIExecArguments extends MICommand 
{
	public MIExecArguments(String miVersion, String[] args) {
		super(miVersion, "-exec-arguments", processArguments(args)); //$NON-NLS-1$
	}

	private static String[] processArguments(String[] args) {
		String[] result = new String[args.length];
		for (int i = 0; i < result.length; ++i) {
			if (args[i].isEmpty()) {
				result[i] = "''"; //$NON-NLS-1$
			}
			else {
				result[i] = args[i];
			}
		}
		return result;
	}
}
