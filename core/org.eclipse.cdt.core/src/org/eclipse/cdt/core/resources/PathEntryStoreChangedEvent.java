/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.core.resources;

import java.util.EventObject;

import org.eclipse.core.resources.IProject;

/**
 * PathEntryChangedEvent
 */
public class PathEntryStoreChangedEvent extends EventObject {
	/**
	 * 
	 */
	public PathEntryStoreChangedEvent(Object object) {
		super(object);
	}

	public IProject getProject() {
		return (IProject)getSource();
	}

}
