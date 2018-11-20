/*******************************************************************************
 * Copyright (c) 2006-2009 Wind River Systems, Inc. and others.
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
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

import java.math.BigInteger;

import org.eclipse.debug.core.DebugException;

public interface IViewportCache {

	public void dispose();

	public void refresh();

	public TraditionalMemoryByte[] getBytes(BigInteger address, int bytesRequested) throws DebugException;

	public void archiveDeltas();

	public void setEditedValue(BigInteger address, TraditionalMemoryByte[] bytes);

	public void clearEditBuffer();

	public void writeEditBuffer();

	public boolean containsEditedCell(BigInteger address);
	//    private void queueRequest(BigInteger startAddress, BigInteger endAddress);
}
