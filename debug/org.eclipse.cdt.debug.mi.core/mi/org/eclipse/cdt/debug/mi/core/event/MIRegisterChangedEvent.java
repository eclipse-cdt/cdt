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
package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.MISession;



/**
 * This can not be detected yet by gdb/mi.
 *
 */
public class MIRegisterChangedEvent extends MIChangedEvent {

	String regName;
	int regno;

	public MIRegisterChangedEvent(MISession source, int token, String name, int no) {
		super(source, token);
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
