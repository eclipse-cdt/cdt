/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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

	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		if (argument == null) {
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, IStatus.ERROR, CCorePlugin.getResourceString("PathEntryVariableResolver.0"), null)); //$NON-NLS-1$
		}
		IPathEntryVariableManager manager = CCorePlugin.getDefault().getPathEntryVariableManager();
		IPath path = manager.getValue(argument);
		if (path == null) {
			path = Path.EMPTY;
		}
		return path.toPortableString();
	}

}
