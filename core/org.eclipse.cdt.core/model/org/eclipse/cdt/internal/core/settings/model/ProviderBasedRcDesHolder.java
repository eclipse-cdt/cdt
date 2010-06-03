/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.core.runtime.IPath;

public class ProviderBasedRcDesHolder extends ResourceDescriptionHolder {
	private IProxyProvider fProvider;

	public ProviderBasedRcDesHolder(IProxyProvider provider, PathSettingsContainer pathContainer, boolean includeCurrent) {
		super(pathContainer, includeCurrent);
		fProvider = provider;
	}

	@Override
	public ICResourceDescription getResourceDescription(IPath path, boolean exactPath){
		fProvider.cacheValues();
		return super.getResourceDescription(path, exactPath);
	}

	@Override
	public void addResourceDescription(IPath path, ICResourceDescription des){
		fProvider.cacheValues();
		super.addResourceDescription(path, des);
	}
	
	@Override
	public ICResourceDescription[] getResourceDescriptions(final int kind){
		fProvider.cacheValues();
		return super.getResourceDescriptions(kind);
	}

	@Override
	public ICResourceDescription getCurrentResourceDescription() {
		fProvider.cacheValues();
		return super.getCurrentResourceDescription();
	}

	@Override
	public ICResourceDescription[] getResourceDescriptions() {
		fProvider.cacheValues();
		return super.getResourceDescriptions();
	}

	@Override
	public ICResourceDescription[] getDirectChildren() {
		fProvider.cacheValues();
		return super.getDirectChildren();
	}

//	public ICSourceEntry[] calculateSourceEntriesFromPaths(IProject project, IPath[] paths) {
//		fProvider.cacheValues();
//		return super.calculateSourceEntriesFromPaths(project, paths);
//	}
	
}
