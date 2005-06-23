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

package org.eclipse.cdt.debug.mi.core;

/**
 * 
 * A checked exception representing a failure.
 *
 */
public class MIException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3257844402679724085L;
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
