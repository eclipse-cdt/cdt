/*******************************************************************************
 * Copyright (c) 2019 Kichwa Coders Ltd and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.dap;

import org.eclipse.lsp4e.debug.debugmodel.DSPDebugElement;
import org.eclipse.lsp4e.debug.debugmodel.DSPDebugTarget;

public class CDTDebugElement extends DSPDebugElement {
	public CDTDebugElement(DSPDebugTarget target) {
		super(target);
	}

	@Override
	public ICDTDebugProtocolServer getDebugProtocolServer() {
		// TODO Auto-generated method stub
		return (ICDTDebugProtocolServer) super.getDebugProtocolServer();
	}
}
