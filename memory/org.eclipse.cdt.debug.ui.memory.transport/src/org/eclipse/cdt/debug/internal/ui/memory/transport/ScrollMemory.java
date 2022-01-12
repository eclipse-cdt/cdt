/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.memory.transport;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.memory.transport.IScrollMemory;
import org.eclipse.cdt.debug.ui.memory.transport.ImportMemoryDialog;

public final class ScrollMemory implements IScrollMemory {

	private final ImportMemoryDialog dialog;

	public ScrollMemory(ImportMemoryDialog dialog) {
		this.dialog = dialog;
	}

	@Override
	public void accept(BigInteger address) {
		dialog.scrollRenderings(address);
	}

}
