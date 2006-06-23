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
 *  ^running
 */
public class MIRunningEvent extends MIEvent {

	public static final int CONTINUE = 0;
	public static final int NEXT = 1;
	public static final int NEXTI = 2;
	public static final int STEP = 3;
	public static final int STEPI = 4;
	public static final int FINISH = 5;
	public static final int UNTIL = 6;
	public static final int RETURN = 7;

	int type;

	public MIRunningEvent(MISession source, int token, int t) {
		super(source, token);
		type = t;
	}

	public int getType() {
		return type;
	}

	public String toString() {
		return "Running"; //$NON-NLS-1$
	}
}
