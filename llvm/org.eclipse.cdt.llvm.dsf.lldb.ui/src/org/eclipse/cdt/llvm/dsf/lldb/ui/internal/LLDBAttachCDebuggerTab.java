/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
