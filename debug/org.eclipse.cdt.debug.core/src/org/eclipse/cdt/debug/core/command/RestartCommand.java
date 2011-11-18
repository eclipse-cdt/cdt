/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - adopted to use for restart command
 *******************************************************************************/
package org.eclipse.cdt.debug.core.command;

import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IRestartHandler;

/**
 * Default restart command for CDI
 * 
 * @since 7.0
 */
public class RestartCommand extends CForEachCommand implements IRestartHandler {

	protected Object getTarget(Object element) {
		return getAdapter(element, IRestart.class);
	}

	protected void execute(Object target) throws CoreException {
		((IRestart)target).restart();
	}
	
	protected boolean isExecutable(Object target) {
		return ((IRestart)target).canRestart();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.commands.AbstractDebugCommand#getEnabledStateJobFamily(org.eclipse.debug.core.commands.IDebugCommandRequest)
	 */
	protected Object getEnabledStateJobFamily(IDebugCommandRequest request) {
		return IRestart.class;
	}
}
