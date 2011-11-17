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
package org.eclipse.cdt.internal.core.settings.model;

import java.util.List;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class ConfigBasedPathEntryContainer implements IPathEntryContainer {
	public static final IPath CONTAINER_PATH = new Path("org.eclipse.cdt.core.CFG_BASED_CONTAINER");	//$NON-NLS-1$
	private IPathEntry[] fEntries;

	public ConfigBasedPathEntryContainer(List<IPathEntry> list){
		this.fEntries = list.toArray(new IPathEntry[list.size()]);
	}

	public ConfigBasedPathEntryContainer(IPathEntry entries[]){
		this.fEntries = entries.clone();
	}

	@Override
	public String getDescription() {
		return "Configuration Description info container";	//$NON-NLS-1$
	}

	@Override
	public IPath getPath() {
		return CONTAINER_PATH;
	}

	@Override
	public IPathEntry[] getPathEntries() {
		return fEntries.clone();
	}

}
