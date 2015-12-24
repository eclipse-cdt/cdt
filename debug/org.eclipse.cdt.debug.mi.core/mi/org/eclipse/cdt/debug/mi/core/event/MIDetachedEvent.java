/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
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
public class MIDetachedEvent extends MIDestroyedEvent {

	public MIDetachedEvent(MISession source, int token) {
		super(source, token);
	}

	@Override
	public String toString() {
		return "Detached"; //$NON-NLS-1$
	}
}
