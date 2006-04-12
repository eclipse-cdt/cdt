/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.event;

import java.util.EventObject;

import org.eclipse.cdt.debug.mi.core.MISession;

/**
 */
public abstract class MIEvent extends EventObject {

	int token;
	boolean propagate = true;

	public MIEvent(MISession session, int token) {
		super(session);
		this.token = token;
	}

	public int getToken() {
		return token;
	}

	public MISession getMISession() {
		return (MISession)getSource();
	}

	public boolean propagate() {
		return propagate;
	}

	public void setPropagate( boolean propagate ) {
		this.propagate = propagate;
	}
}
