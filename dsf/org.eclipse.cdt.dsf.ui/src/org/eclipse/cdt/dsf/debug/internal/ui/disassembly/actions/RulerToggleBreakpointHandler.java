/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.ui.actions.ToggleBreakpointAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Default handler for the toggle breakpoint command in the disassembly ruler.
 * Invoked on double click in the ruler.
 * 
 * @since 2.1
 */
public class RulerToggleBreakpointHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		if (part instanceof IDisassemblyPart) {
			IDisassemblyPart disassemblyPart = (IDisassemblyPart) part;
			IDocument document = disassemblyPart.getTextViewer().getDocument();
			final IVerticalRulerInfo rulerInfo= (IVerticalRulerInfo) part.getAdapter(IVerticalRulerInfo.class);
			if (rulerInfo != null) {
				final ToggleBreakpointAction toggleBpAction= new ToggleBreakpointAction(part, document, rulerInfo);
				toggleBpAction.update();
				if (toggleBpAction.isEnabled()) {
				    if (event.getTrigger() instanceof Event) {
				        // Pass through the event that triggered the action.  
				        // This will give toggle action access to key modifiers 
				        // (shift, ctrl, etc.)
				        toggleBpAction.runWithEvent((Event)event.getTrigger());
				    } else {
				        toggleBpAction.run();
				    }
				}
			}
		}
		return null;
	}

}
