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



/**
 * This can not be detected yet by gdb/mi.
 *
 */
public class MIVarCreatedEvent extends MICreatedEvent {

	String varName;

	public MIVarCreatedEvent(String var) {
		super(0);
		varName = var;
	}

	public MIVarCreatedEvent(int token, String var) {
		super(token);
		varName = var;
	}

	public String getVarName() {
		return varName;
	}

}
