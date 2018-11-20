/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.ui.internal;

/**
 * A LLDB-specific debugger tab for attaching.
 */
public class LLDBAttachCDebuggerTab extends LLDBLocalApplicationCDebuggerTab {

	/**
	 * Constructs the {@link LLDBAttachCDebuggerTab}. This sets the tab to
	 * attach mode.
	 */
	@SuppressWarnings("restriction")
	public LLDBAttachCDebuggerTab() {
		super();
		fAttachMode = true;
	}
}
