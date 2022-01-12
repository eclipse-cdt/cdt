/*******************************************************************************
 * Copyright (c) 2021 Intel Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Default handler for the jump to memory command in the disassembly ruler.
 *
 * @since 2.10
 */
public class RulerJumpToMemoryHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		if (part instanceof IDisassemblyPart) {
			IDisassemblyPart disassemblyPart = (IDisassemblyPart) part;
			final IVerticalRulerInfo rulerInfo = part.getAdapter(IVerticalRulerInfo.class);
			if (rulerInfo != null) {
				new RulerJumpToMemoryAction(disassemblyPart, rulerInfo).run();
			}
		}
		return null;
	}
}
