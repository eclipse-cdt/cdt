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
public class MISignalChangedEvent extends MIChangedEvent {

	String name;

	public MISignalChangedEvent(MISession source, String n) {
		this(source, 0, n);
	}

	public MISignalChangedEvent(MISession source, int id, String n) {
		super(source, id);
		name = n;
	}

	public String getName() {
		return name;
	}

}
