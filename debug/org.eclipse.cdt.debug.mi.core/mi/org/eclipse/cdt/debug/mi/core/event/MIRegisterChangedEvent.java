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
public class MIRegisterChangedEvent extends MIChangedEvent {

	String regName;
	int regno;

	public MIRegisterChangedEvent(int token, String name, int no) {
		super(token);
		regName = name;
		regno = no;
	}

	public String getName() {
		return regName;
	}

	public int getNumber() {
		return regno;
	}
}
