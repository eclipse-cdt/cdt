/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.core;

import java.util.EventObject;

public class MakeTargetEvent extends EventObject {
	public final int TARGET_ADD = 1;
	public final int TARGET_CHANGED = 2;
	public final int TARGET_REMOVED = 3;

	IMakeTarget target;
	int type;

	/**
	 * @param source
	 */
	public MakeTargetEvent(Object source, int type, IMakeTarget target) {
		super(source);
		this.type = type;
		this.target = target;
	}

	public int getType() {
		return type;
	}

	public IMakeTarget getTarget() {
		return target;
	}
}