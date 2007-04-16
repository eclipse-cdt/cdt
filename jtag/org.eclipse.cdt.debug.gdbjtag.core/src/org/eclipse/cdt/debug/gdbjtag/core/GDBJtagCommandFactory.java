/**********************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.debug.gdbjtag.core;

import org.eclipse.cdt.debug.mi.core.command.CommandFactory;

/**
 * @author Doug Schaefer
 *
 */
public class GDBJtagCommandFactory extends CommandFactory {

	/**
	 * 
	 */
	public GDBJtagCommandFactory() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param miVersion
	 */
	public GDBJtagCommandFactory(String miVersion) {
		super(miVersion);
		// TODO Auto-generated constructor stub
	}

}
