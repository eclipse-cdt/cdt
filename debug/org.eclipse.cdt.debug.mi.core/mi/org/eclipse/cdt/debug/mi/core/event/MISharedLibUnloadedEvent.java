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
 *
 */
public class MISharedLibUnloadedEvent extends MIDestroyedEvent {

	String filename;

	public MISharedLibUnloadedEvent(MISession source, String name) {
		this(source, 0, name);
	}

	public MISharedLibUnloadedEvent(MISession source, int id, String name) {
		super(source, id);
		filename = name;
	}

	public String getName() {
		return filename;
	}

}
