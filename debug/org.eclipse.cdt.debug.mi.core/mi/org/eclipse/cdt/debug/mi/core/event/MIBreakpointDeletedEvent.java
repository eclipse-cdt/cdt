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
public class MIBreakpointDeletedEvent extends MIDestroyedEvent {

	int no;

	public MIBreakpointDeletedEvent(int number) {
		this(0, number);
	}

	public MIBreakpointDeletedEvent(int id, int number) {
		super(id);
		no = number;
	}

	public int getNumber() {
		return no;
	}

}
