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
package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.MISession;



/**
 * This can not be detected yet by gdb/mi.
 *
 */
public class MIVarChangedEvent extends MIChangedEvent {

	String varName;

	public MIVarChangedEvent(MISession source, String var) {
		this(source, 0, var);
	}

	public MIVarChangedEvent(MISession source, int token, String var) {
		super(source, token);
		varName = var;
	}

	public String getVarName() {
		return varName;
	}

}
