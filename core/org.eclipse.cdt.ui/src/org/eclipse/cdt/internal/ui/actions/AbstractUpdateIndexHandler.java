/*******************************************************************************
 * Copyright (c) 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.cdt.internal.ui.util.SelectionUtil;

/**
 * Abstract handler for {@link org.eclipse.cdt.internal.ui.actions.AbstractUpdateIndexAction}
 */
public abstract class AbstractUpdateIndexHandler extends AbstractHandler {
	
	abstract protected AbstractUpdateIndexAction getAction();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		getAction().setActivePart(null, part);
		getAction().selectionChanged(null, selection);
		getAction().run(null);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#setEnabled(java.lang.Object)
	 */
	@Override
	public void setEnabled(Object evaluationContext) {
    	ISelection selection = SelectionUtil.getActiveSelection();
    	setBaseEnabled(getAction().isEnabledFor(selection));
	}
}
