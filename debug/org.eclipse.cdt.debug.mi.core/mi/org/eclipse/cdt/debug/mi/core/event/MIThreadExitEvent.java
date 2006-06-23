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
public class MIThreadExitEvent extends MIDestroyedEvent {

	int tid;

	public MIThreadExitEvent(MISession source, int id) {
		this(source, 0, id);
	}

	public MIThreadExitEvent(MISession source, int token, int id) {
		super(source, token);
		tid = id;
	}

	public int getId() {
		return tid;
	}
}
