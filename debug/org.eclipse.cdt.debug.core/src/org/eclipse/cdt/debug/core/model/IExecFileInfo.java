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
package org.eclipse.cdt.debug.core.model;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.debug.core.DebugException;

/**
 * Provides access to executable file information.
 */
public interface IExecFileInfo {

	public IBinaryObject getExecFile();

	public boolean isLittleEndian();

	public IGlobalVariableDescriptor[] getGlobals() throws DebugException;
}