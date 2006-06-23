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
package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.MISession;



/**
 * This can not be detected yet by gdb/mi.
 *
 */
public class MIRegisterCreatedEvent extends MICreatedEvent {

	String regName;
	int regno;

	public MIRegisterCreatedEvent(MISession source, String name, int number) {
		this(source, 0, name, number);
	}

	public MIRegisterCreatedEvent(MISession source, int token, String name, int number) {
		super(source, token);
		regName = name;
		regno = number;
	}

	public String getName() {
		return regName;
	}

	public int getNumber() {
		return regno;
	}

}
