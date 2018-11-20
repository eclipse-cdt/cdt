/*******************************************************************************
 * Copyright (c) 2006, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;

/**
 * Element class used to group matches.
 *
 * @author Doug Schaefer
 */
public class CSearchElement implements IAdaptable {
	private final IIndexFileLocation location;

	public CSearchElement(IIndexFileLocation loc) {
		this.location = loc;
	}

	@Override
	public int hashCode() {
		return location.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CSearchElement))
			return false;
		CSearchElement other = (CSearchElement) obj;
		return location.equals(other.location);
	}

	final IIndexFileLocation getLocation() {
		return location;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapterType) {
		if (adapterType.isAssignableFrom(IFile.class)) {
			String fullPath = location.getFullPath();
			if (fullPath != null) {
				return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fullPath));
			}
		} else if (adapterType.isAssignableFrom(IFileStore.class)) {
			try {
				return EFS.getStore(location.getURI());
			} catch (CoreException e) {
				return null;
			}
		} else if (adapterType.isAssignableFrom(IIndexFileLocation.class)) {
			return location;
		}
		return null;
	}
}
