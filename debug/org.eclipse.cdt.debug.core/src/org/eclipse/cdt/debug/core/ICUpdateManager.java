/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.core;

import org.eclipse.debug.core.DebugException;

/**
 * Indicates the support of update and auto-update functions.
 */
public interface ICUpdateManager {

	void setAutoModeEnabled( boolean enable );

	boolean getAutoModeEnabled();

	void update() throws DebugException;

	boolean canUpdate();
}