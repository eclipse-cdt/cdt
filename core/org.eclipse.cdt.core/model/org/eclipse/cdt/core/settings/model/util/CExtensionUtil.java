/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class CExtensionUtil {
	public static IConfigurationElement getFirstConfigurationElement(ICConfigExtensionReference ref, String name,
			boolean caseSensitive) throws CoreException {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(ref.getExtensionPoint());
		IExtension extension = extensionPoint.getExtension(ref.getID());
		if (extension == null) {
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
					CCorePlugin.getResourceString("CDescriptor.exception.providerNotFound"), null)); //$NON-NLS-1$
		}
		IConfigurationElement element[] = extension.getConfigurationElements();
		for (int i = 0; i < element.length; i++) {
			if (caseSensitive ? element[i].getName().equals(name) : element[i].getName().equalsIgnoreCase(name)) {
				return element[i];
			}
		}
		return null;
	}

}
