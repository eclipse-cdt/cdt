/*******************************************************************************
 * Copyright (c) 2002, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.custom.BusyIndicator;
import org.osgi.framework.Bundle;

public class CoreUtility {
	/**
	 * Creates a folder and all parent folders if not existing.
	 * Project must exist.
	 * {@link org.eclipse.ui.dialogs.ContainerGenerator} is too heavy
	 * (creates a runnable)
	 */
	public static void createFolder(IFolder folder, boolean force, boolean local, IProgressMonitor monitor)
			throws CoreException {
		if (!folder.exists()) {
			IContainer parent = folder.getParent();
			if (parent instanceof IFolder) {
				createFolder((IFolder) parent, force, local, null);
			}
			folder.create(force, local, monitor);
		}
	}

	/**
	 * Creates an extension.  If the extension plugin has not been loaded,
	 * a busy cursor will be activated during the duration of the load.
	 *
	 * @param element the config element defining the extension
	 * @param classAttribute the name of the attribute carrying the class
	 * @return the extension object
	 */
	public static Object createExtension(final IConfigurationElement element, final String classAttribute)
			throws CoreException {
		// If plugin has been loaded, create extension.
		// Otherwise, show busy cursor then create extension.
		String id = element.getContributor().getName();
		Bundle bundle = Platform.getBundle(id);
		if (bundle.getState() == org.osgi.framework.Bundle.ACTIVE) {
			return element.createExecutableExtension(classAttribute);
		}
		final Object[] ret = new Object[1];
		final CoreException[] exc = new CoreException[1];
		BusyIndicator.showWhile(null, () -> {
			try {
				ret[0] = element.createExecutableExtension(classAttribute);
			} catch (CoreException e) {
				exc[0] = e;
			}
		});
		if (exc[0] != null)
			throw exc[0];
		return ret[0];
	}
}
