/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.actions;

import org.eclipse.cdt.dsf.commands.IStepIntoSelectionHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.ui.actions.DebugCommandHandler;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @since 2.4
 */
public class DsfStepIntoEditorSelectionHandler extends DebugCommandHandler {
	private IWorkbenchWindow fWindow = null;
	
	@Override
	protected Class<?> getCommandType() {
		return IStepIntoSelectionHandler.class;
	}
	
    @Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
    	fWindow = HandlerUtil.getActiveWorkbenchWindow(event);
    	return super.execute(event);
    }
}
