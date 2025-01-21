/*******************************************************************************
 * Copyright (c) 2010, 2014 Texas Instruments and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial implementation of run()
 *     Marc Dumais (Ericsson) - Bug 437692
 *     Raghunandana Murthappa(Advantest Europe GmbH) - Issue 1048
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler that opens the new View of the view type selected. This is used by
 * the OpenNewViewCommand which is contributed to debug related views.
 */
public class OpenNewViewHandler extends AbstractHandler {
	private OpenNewViewAction fOpenNewViewAction = new OpenNewViewAction();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IViewPart viewPart = (IViewPart) HandlerUtil.getActivePart(event);
		fOpenNewViewAction.init(viewPart);
		fOpenNewViewAction.run();

		return IStatus.OK;
	}

}
