/*******************************************************************************
 * Copyright (c) 2006, 2010, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *     Randy Rohrbach (Wind River Systems, Inc.) - Copied and modified to create the floating point plugin
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.floatingpoint;

import java.math.BigInteger;

import org.eclipse.debug.core.DebugException;

public interface FPIViewportCache {

	public void dispose();

	public void refresh();

	public FPMemoryByte[] getBytes(BigInteger address, int bytesRequested) throws DebugException;

	public void archiveDeltas();

	public void setEditedValue(BigInteger address, FPMemoryByte[] bytes);

	public void clearEditBuffer();

	public void writeEditBuffer();

	public boolean containsEditedCell(BigInteger address);
	//    private void queueRequest(BigInteger startAddress, BigInteger endAddress);
}
