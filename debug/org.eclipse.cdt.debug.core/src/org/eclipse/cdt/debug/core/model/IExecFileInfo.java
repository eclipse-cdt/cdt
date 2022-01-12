/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
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
