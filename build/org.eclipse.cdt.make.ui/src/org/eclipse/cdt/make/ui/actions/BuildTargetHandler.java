/*******************************************************************************
 * Copyright (c) 2011 Axel Mueller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Axel Mueller - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for {@link org.eclipse.cdt.make.ui.actions.BuildTargetAction}
 *
 * @since 7.1
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class BuildTargetHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final BuildTargetAction buildAction= new BuildTargetAction();
		ISelection selection = HandlerUtil.getCurrentSelection( event );
		IWorkbenchPart part = HandlerUtil.getActivePart( event );
		buildAction.setActivePart(null, part);
		buildAction.selectionChanged(null, selection);

		if (buildAction.isEnabled()) {
			buildAction.run(null);
		}
		return null;
	}

}
