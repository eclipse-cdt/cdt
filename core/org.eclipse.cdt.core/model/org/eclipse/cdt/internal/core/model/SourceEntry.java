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

public class SourceEntry extends ACPathEntry implements ISourceEntry {

	IPath sourcePath;
	IPath outputLocation;

	public SourceEntry(IPath sourcePath, IPath outputLocation, boolean isRecursive, IPath[] exclusionPatterns) {
		super(ISourceEntry.CDT_SOURCE, isRecursive, exclusionPatterns, false);
		this.sourcePath = sourcePath;
		this.outputLocation = outputLocation;
	}

	/**
	 * Returns the absolute path from the worskspace root or
	 * relative path of the source folder.
	 * @return String
	 */
	public IPath getSourcePath() {
		return sourcePath;
	}

	/**
	 * Binary output location for this source folder.
	 * @return IPath, <code>null</code> means to use the
	 * default output location of the project.
	 */
	public IPath getOutputLocation() {
		return outputLocation;
	}

}
