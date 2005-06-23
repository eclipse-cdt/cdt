/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
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
 *      -environment-directory PATHDIR
 *
 *   Add directory PATHDIR to beginning of search path for source files.
 * 
 */
public class MIEnvironmentDirectory extends MICommand 
{
	public MIEnvironmentDirectory(boolean reset, String[] paths) {
		super("-environment-directory", paths); //$NON-NLS-1$
		if (reset) {
			setOptions(new String[] {"-r"}); //$NON-NLS-1$
		}
	}

}
