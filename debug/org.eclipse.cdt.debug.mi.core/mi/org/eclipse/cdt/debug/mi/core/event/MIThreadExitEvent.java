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
public class MIThreadExitEvent extends MIDestroyedEvent {

	int tid;

	public MIThreadExitEvent(int id) {
		this(0, id);
	}

	public MIThreadExitEvent(int token, int id) {
		super(token);
		tid = id;
	}

	public int getId() {
		return tid;
	}
}
