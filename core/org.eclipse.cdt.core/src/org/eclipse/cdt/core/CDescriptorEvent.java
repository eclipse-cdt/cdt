/*******************************************************************************
 * Copyright (c) 2002, 2003, 2004 QNX Software Systems Ltd. and others. All
 * rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.core;

import java.util.EventObject;

public class CDescriptorEvent extends EventObject {
	
	public static final int CDTPROJECT_CHANGED = 1;
	public static final int CDTPROJECT_ADDED = 2;
	public static final int CDTPROJECT_REMOVED = 3;
	
	public static final int OWNER_CHANGED = 0x10;
	public static final int EXTENSION_CHANGED = 0x20;

	private static final int FLAGS_MASK = 0xf;
	
	private int fType;

	public CDescriptorEvent(ICDescriptor descriptor, int type, int flags) {
		super(descriptor);
		fType = type | flags;
	}

	public ICDescriptor getDescriptor() {
		return (ICDescriptor) getSource();
	}
	
	public int getType() {
		return fType & FLAGS_MASK;
	}
	
	public int getFlags() {
		return fType & ~FLAGS_MASK;
	}
}
