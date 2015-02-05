/***************************************************************************************************
 * Copyright (c) 2008, 2010 Mirko Raner.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - initial implementation for Eclipse Bug 196337
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;

/**
 * The class {@link LocalTerminalLaunchListProvider} is a {@link IStructuredContentProvider} that
 * provides a list of all {@link ILaunchConfiguration}s of the type "External Tools"/"Program".
 * Those launch configurations can be used to start a new session in the Terminal View.
 *
 * @author Mirko Raner
 * @version $Revision: 1.2 $
 */
public class LocalTerminalLaunchListProvider implements IStructuredContentProvider {

	/**
	 * Creates a new {@link LocalTerminalLaunchListProvider}.
	 */
	public LocalTerminalLaunchListProvider() {

		super();
	}

	/**
	 * Returns the matching {@link ILaunchConfiguration}s for the given input element. This content
	 * provider does not really use the concept of "input" because the input can only be obtained in
	 * one way (from the {@link ILaunchManager}.
	 *
	 * @param input the input element (not checked or used by this method)
	 * @return the matching {@link ILaunchConfiguration}s
	 *
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object input) {

		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = LocalTerminalUtilities.TERMINAL_LAUNCH_TYPE;
		ILaunchConfiguration[] configurations = null;
		try {

			configurations = launchManager.getLaunchConfigurations(type);
		}
		catch (CoreException couldNotObtainLaunchConfigurations) {

			Logger.logException(couldNotObtainLaunchConfigurations);
		}
		return configurations;
	}

	/**
	 * Disposes of this {@link LocalTerminalLaunchListProvider}. Currently, there is no additional
	 * clean-up necessary, and this method is empty.
	 *
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {

		// Does nothing...
	}

	/**
	 * Notifies the {@link LocalTerminalLaunchListProvider} that its input has changed. This method
	 * is currently empty because {@link LocalTerminalLaunchListProvider} is not aware of the
	 * concept of "input"
	 *
	 * @see #getElements(Object)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		// Does nothing...
	}
}
