/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.missingapi;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.internal.core.pdom.dom.PDOMInclude;

/**
 * Represents an include relation found in the index. 
 * @since 4.0
 */
public class CIndexIncludeRelation {
	private IPath fIncludedBy;
	private IPath fIncludes;
	
	CIndexIncludeRelation(PDOMInclude include) throws CoreException {
		fIncludedBy= Path.fromOSString(include.getIncludedBy().getFileName().getString());
		fIncludes= Path.fromOSString(include.getIncludes().getFileName().getString());
	}
	public boolean isSystemInclude() {
		return false;
	}
	public boolean isActiveCode() {
		return true;
	}
	public IPath getIncludedBy() {
		return fIncludedBy;
	}
	public IPath getIncludes() {
		return fIncludes;
	}
	public String getName() {
		return fIncludes.lastSegment();
	}
	public int getOffset() {
		return 9;
	}
	public long getTimestamp() {
		return 0;
	}
}