/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core;

/**
 * 
 * A checked exception representing a failure.
 *
 */
public class MIException extends Exception {
	String log = ""; //$NON-NLS-1$

	public MIException(String s) {
		super(s);
	}

	public MIException(String s, String l) {
		super(s);
		log = l;
	}

	public String getLogMessage() {
		return log;
	}
}
