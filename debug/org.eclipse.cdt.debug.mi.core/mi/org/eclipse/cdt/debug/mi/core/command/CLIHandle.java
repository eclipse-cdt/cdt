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
 *    handle SIGUSR1 nostop noignore
 *
 */
public class CLIHandle extends CLICommand {

	public CLIHandle(String arg) {
		super("handle " + arg); //$NON-NLS-1$
	}

}
