/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.core.index.IIndexFileLocation;

/**
 * Element class used to group matches.
 *  
 * @author Doug Schaefer
 */
public class CSearchElement implements IAdaptable {

	private final IIndexFileLocation location;
	
	public CSearchElement(IIndexFileLocation loc) {
		this.location= loc;
	}
	
	@Override
	public int hashCode() {
		return location.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CSearchElement))
			return false;
		CSearchElement other = (CSearchElement)obj;
		return location.equals(other.location);
	}

	final IIndexFileLocation getLocation() {
		return location;
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapterType) {
		if (adapterType.isAssignableFrom(IFile.class)) {
			String fullPath= location.getFullPath();
			if (fullPath != null) {
				return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fullPath));
			}
		}
		return null;
	}
}
