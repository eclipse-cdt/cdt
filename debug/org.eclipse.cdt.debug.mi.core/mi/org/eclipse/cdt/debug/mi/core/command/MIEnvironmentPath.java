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
 *      -environment-path ( PATHDIR )+
 *
 *   Add directories PATHDIR to beginning of search path for object files.
 * 
 */
public class MIEnvironmentPath extends MICommand 
{
	public MIEnvironmentPath(String miVersion, String[] paths) {
		super(miVersion, "-environment-path", paths); //$NON-NLS-1$
	}
}
