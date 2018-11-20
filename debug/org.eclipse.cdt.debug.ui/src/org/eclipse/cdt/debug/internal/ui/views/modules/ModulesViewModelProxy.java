/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.modules;

import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler;
import org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy;

/**
 * Default update for modules view.
 */
public class ModulesViewModelProxy extends EventHandlerModelProxy {

	private IModuleRetrieval fModuleRetrieval;

	/**
	 * Constructor for ModulesViewModelProxy.
	 */
	public ModulesViewModelProxy(IModuleRetrieval moduleRetrieval) {
		super();
		fModuleRetrieval = moduleRetrieval;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#createEventHandlers()
	 */
	@Override
	protected DebugEventHandler[] createEventHandlers() {
		return new DebugEventHandler[] { new ModulesViewEventHandler(this, fModuleRetrieval) };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#dispose()
	 */
	@Override
	public synchronized void dispose() {
		super.dispose();
		fModuleRetrieval = null;
	}
}
