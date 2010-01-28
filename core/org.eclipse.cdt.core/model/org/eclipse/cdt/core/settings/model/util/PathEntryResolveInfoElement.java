/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import java.util.List;

import org.eclipse.cdt.core.model.IPathEntry;

public class PathEntryResolveInfoElement {
	private IPathEntry fRawEntry;
	private IPathEntry[] fResolvedEntries;
	
	public PathEntryResolveInfoElement(IPathEntry rawEntry, IPathEntry resolvedEntry){
		fRawEntry = rawEntry;
		if(resolvedEntry != null)
			fResolvedEntries = new IPathEntry[]{resolvedEntry};
		else
			fResolvedEntries = new IPathEntry[0];
	}

	public PathEntryResolveInfoElement(IPathEntry rawEntry, List<IPathEntry> resolvedList){
		fRawEntry = rawEntry;
		if(resolvedList != null){
			fResolvedEntries = new IPathEntry[resolvedList.size()];
			resolvedList.toArray(fResolvedEntries);
		}
	}

	public IPathEntry getRawEntry(){
		return fRawEntry;
	}
	
	public IPathEntry[] getResolvedEntries(){
		if(fResolvedEntries == null)
			return new IPathEntry[0];
		return fResolvedEntries.clone();
	}
}
