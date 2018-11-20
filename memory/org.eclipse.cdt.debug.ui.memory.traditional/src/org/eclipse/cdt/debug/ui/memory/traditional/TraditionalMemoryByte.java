/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.memory.traditional;

import org.eclipse.debug.core.model.MemoryByte;

public class TraditionalMemoryByte extends MemoryByte implements IMemoryByte {
	private boolean isEdited = false;

	private boolean[] changeHistory = new boolean[0];

	public TraditionalMemoryByte() {
		super();
	}

	public TraditionalMemoryByte(byte byteValue) {
		super(byteValue);
	}

	public TraditionalMemoryByte(byte byteValue, byte byteFlags) {
		super(byteValue, byteFlags);
	}

	@Override
	public boolean isEdited() {
		return isEdited;
	}

	@Override
	public void setEdited(boolean edited) {
		isEdited = edited;
	}

	public boolean isChanged(int historyDepth) {
		return changeHistory.length > historyDepth && changeHistory[historyDepth];
	}

	public void setChanged(int historyDepth, boolean changed) {
		if (historyDepth >= changeHistory.length) {
			boolean newChangeHistory[] = new boolean[historyDepth + 1];
			System.arraycopy(changeHistory, 0, newChangeHistory, 0, changeHistory.length);
			changeHistory = newChangeHistory;
		}

		changeHistory[historyDepth] = changed;

		if (historyDepth == 0)
			this.setChanged(changed);
	}
}
