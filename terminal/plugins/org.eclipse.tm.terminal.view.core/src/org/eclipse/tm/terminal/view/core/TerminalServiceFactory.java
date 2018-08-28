/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.terminal.view.core.activator.CoreBundleActivator;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.nls.Messages;
import org.osgi.framework.Bundle;

/**
 * Terminal service factory implementation.
 * <p>
 * Provides access to the terminal service instance.
 */
public final class TerminalServiceFactory {
	private static ITerminalService instance = null;

	static {
		// Tries to instantiate the terminal service implementation
		// from the o.e.tm.terminal.view.ui bundle
		Bundle bundle = Platform.getBundle("org.eclipse.tm.terminal.view.ui"); //$NON-NLS-1$
		if (bundle != null && bundle.getState() != Bundle.UNINSTALLED && bundle.getState() != Bundle.STOPPING) {
			try {
	            Class<?> clazz = bundle.loadClass("org.eclipse.tm.terminal.view.ui.services.TerminalService"); //$NON-NLS-1$
	            instance = (ITerminalService) clazz.newInstance();
            }
            catch (Exception e) {
            	if (Platform.inDebugMode()) {
            		Platform.getLog(bundle).log(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), Messages.TerminalServiceFactory_error_serviceImplLoadFailed, e));
            	}
            }
		}
	}

	/**
	 * Returns the terminal service instance.
	 */
	public static ITerminalService getService() {
		return instance;
	}
}
