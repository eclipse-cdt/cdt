/*******************************************************************************
 * Copyright (c) 2006, 2007 2009 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.actions;

import org.eclipse.cdt.internal.autotools.core.AutotoolsPropertyConstants;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;


/**
 * Class responsible for invoking autoconf.
 * 
 * @author klee
 * 
 */
public class InvokeAutoconfAction extends InvokeAction {

	private final static String DEFAULT_COMMAND = "autoconf"; //$NON-NLS-1$
	public void run(IAction action) {
		IContainer container = getSelectedContainer();
		if (container == null)
			return;
		
		IPath execDir = getExecDir(container);

		if (container != null) {
			IProject project = container.getProject();
			String autoconfCommand = null;
			try {
				autoconfCommand = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_TOOL);
			} catch (CoreException e) {
				// do nothing
			}
			
			// If unset for the project, default to system path
			if (autoconfCommand == null)
				autoconfCommand = DEFAULT_COMMAND;
			
			executeConsoleCommand(DEFAULT_COMMAND, autoconfCommand, new String[]{}, execDir);
		}
	}


	public void dispose() {
	}
}
