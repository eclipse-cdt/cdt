/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.modules; 

import org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler;
import org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy;
 
/**
 * Default update for modules view.
 */
public class ModulesViewModelProxy extends EventHandlerModelProxy {

	/** 
	 * Constructor for ModulesViewModelProxy. 
	 */
	public ModulesViewModelProxy() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#createEventHandlers()
	 */
	protected DebugEventHandler[] createEventHandlers() {
		return new DebugEventHandler[] { new ModulesViewEventHandler( this ) };
	}
}
