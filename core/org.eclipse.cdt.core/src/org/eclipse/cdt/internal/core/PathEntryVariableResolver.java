/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.resources.IPathEntryVariableManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

public class PathEntryVariableResolver implements IDynamicVariableResolver {

	public PathEntryVariableResolver() {
		super();
	}

	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		if (argument == null) {
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID,
					CCorePlugin.getResourceString("PathEntryVariableResolver.0"), null)); //$NON-NLS-1$
		}
		IPathEntryVariableManager manager = CCorePlugin.getDefault().getPathEntryVariableManager();
		IPath path = manager.getValue(argument);
		if (path == null) {
			path = Path.EMPTY;
		}
		return path.toPortableString();
	}

}
