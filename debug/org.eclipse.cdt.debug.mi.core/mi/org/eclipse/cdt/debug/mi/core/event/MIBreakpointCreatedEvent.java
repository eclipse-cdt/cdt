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
 *
 */
public class MIBreakpointCreatedEvent extends MICreatedEvent {

	int no;

	public MIBreakpointCreatedEvent(MISession source, int number) {
		this(source, 0, number);
	}

	public MIBreakpointCreatedEvent(MISession source, int id, int number) {
		super(source, id);
		no = number;
	}

	public int getNumber() {
		return no;
	}

}
