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
 *
 */
public class MISignalChangedEvent extends MIChangedEvent {

	String name;

	public MISignalChangedEvent(String n) {
		this(0, n);
	}

	public MISignalChangedEvent(int id, String n) {
		super(id);
		name = n;
	}

	public String getName() {
		return name;
	}

}
