/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Takuya Miyamoto - Adapted from org.eclipse.team.examples.filesystem / FileSystemRemoteTree
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.filesystem.subscriber;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.internal.synchronize.RSESyncUtils;
import org.eclipse.rse.internal.synchronize.filesystem.FileSystemProvider;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.ThreeWayRemoteTree;

/**
 * The file system three-way remote resource variant tree that provides the
 * ability to traverse the file system for the creation of resource variants.
 */
public class FileSystemRemoteTree extends ThreeWayRemoteTree {

	/**
	 * Create the file system remote resource variant tree
	 * 
	 * @param subscriber
	 * 		the file system subscriber
	 */
	public FileSystemRemoteTree(FileSystemSubscriber subscriber) {
		super(subscriber);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.core.variants.AbstractResourceVariantTree#fetchMembers
	 * (org.eclipse.team.core.variants.IResourceVariant,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IResourceVariant[] fetchMembers(IResourceVariant variant, IProgressMonitor progress) throws TeamException {
		return ((FileSystemResourceVariant) variant).members();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.core.variants.AbstractResourceVariantTree#fetchVariant
	 * (org.eclipse.core.resources.IResource, int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IResourceVariant fetchVariant(IResource resource, int depth, IProgressMonitor monitor) throws TeamException {
		try {
			RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), RSESyncUtils.PROVIDER_ID);
			if (provider != null) {
				return ((FileSystemProvider) provider).getExportResourceVariant(resource);
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
