/**********************************************************************
 * Created on Mar 25, 2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.core.runtime.IPath;

public class SourceEntry extends APathEntry implements ISourceEntry {

	IPath outputLocation;

	public SourceEntry(IPath path, IPath outputLocation, boolean isRecursive, IPath[] exclusionPatterns) {
		super(ISourceEntry.CDT_SOURCE, path, isRecursive, exclusionPatterns, false);
		this.outputLocation = outputLocation;
	}

	/**
	 * Binary output location for this source folder.
	 * @return IPath, <code>null</code> means to use the
	 * default output location of the project.
	 */
	public IPath getOutputLocation() {
		return outputLocation;
	}

	public boolean equals (Object obj) {
		if (obj instanceof ISourceEntry) {
			ISourceEntry otherEntry = (ISourceEntry)obj;
			if (!super.equals(otherEntry)) {
				return false;
			}
			if (path == null) {
				if (otherEntry.getPath() != null) {
					return false;
				}
			} else {
				if (!path.toString().equals(otherEntry.getPath().toString())) {
					return false;
				}
			}
			if (outputLocation == null) {
				if (otherEntry.getOutputLocation() != null) {
					return false;
				}
			} else {
				if (!outputLocation.toString().equals(otherEntry.getOutputLocation().toString())) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}

}
