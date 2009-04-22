/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Takuya Miyamoto - Adapted from org.eclipse.team.examples.filesystem / FileSystemSubscriber
 * David McKnight   (IBM)        - [272708] [import/export] fix various bugs with the synchronization support
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.filesystem.subscriber;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.rse.internal.synchronize.RSEResourceVariantComparator;
import org.eclipse.rse.internal.synchronize.RSESyncUtils;
import org.eclipse.rse.internal.synchronize.filesystem.FileSystemProvider;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.ThreeWayRemoteTree;
import org.eclipse.team.core.variants.ThreeWaySubscriber;
import org.eclipse.team.core.variants.ThreeWaySynchronizer;
import org.eclipse.team.internal.core.mapping.LocalResourceVariant;

/**
 * This is an example file system subscriber that overrides ThreeWaySubscriber.
 * It uses a repository provider (<code>FileSystemProvider</code>) to determine
 * and manage the roots and to create resource variants. It also makes use of a
 * file system specific remote tree (<code>FileSystemRemoteTree</code>) for
 * provided the remote tree access and refresh.
 * 
 * @see ThreeWaySubscriber
 * @see ThreeWaySynchronizer
 * @see FileSystemProvider
 * @see FileSystemRemoteTree
 */
public class FileSystemSubscriber extends ThreeWaySubscriber {

	private static FileSystemSubscriber instance;

	/**
	 * Return the file system subscriber singleton.
	 * 
	 * @return the file system subscriber singleton.
	 */
	public static synchronized FileSystemSubscriber getInstance() {
		if (instance == null) {
			instance = new FileSystemSubscriber();
		}
		return instance;
	}

	/**
	 * Create the file system subscriber.
	 */
	private FileSystemSubscriber() {
		super(new ThreeWaySynchronizer(new QualifiedName(RSESyncUtils.PLUGIN_ID, "workpsace-sync"))); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.core.variants.ThreeWaySubscriber#getResourceVariant(
	 * org.eclipse.core.resources.IResource, byte[])
	 */
	@Override
	public IResourceVariant getResourceVariant(IResource resource, byte[] bytes) throws TeamException {
		RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), RSESyncUtils.PROVIDER_ID);
		if (provider != null) {
			return ((FileSystemProvider) provider).getResourceVariant(resource, bytes);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.variants.ThreeWaySubscriber#createRemoteTree()
	 */
	@Override
	protected ThreeWayRemoteTree createRemoteTree() {
		return new FileSystemRemoteTree(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.subscribers.Subscriber#getName()
	 */
	@Override
	public String getName() {
		return "Synchronize"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.subscribers.Subscriber#roots()
	 */
	@Override
	public IResource[] roots() {
		List result = new ArrayList();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if (project.isAccessible()) {
				RepositoryProvider provider = RepositoryProvider.getProvider(project, RSESyncUtils.PROVIDER_ID);
				if (provider != null) {
					result.add(project);
				}
			}
		}
		return (IProject[]) result.toArray(new IProject[result.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.core.variants.ThreeWaySubscriber#handleRootChanged(org
	 * .eclipse.core.resources.IResource, boolean)
	 */
	@Override
	public void handleRootChanged(IResource resource, boolean added) {
		// Override to allow FileSystemProvider to signal the addition and
		// removal of roots
		super.handleRootChanged(resource, added);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.core.variants.ResourceVariantTreeSubscriber#getSyncInfo
	 * (org.eclipse.core.resources.IResource,
	 * org.eclipse.team.core.variants.IResourceVariant,
	 * org.eclipse.team.core.variants.IResourceVariant)
	 */
	@Override
	protected SyncInfo getSyncInfo(IResource local, IResourceVariant base, IResourceVariant remote) throws TeamException {

		FileSystemResourceVariant rv = null;

		if (remote instanceof FileSystemResourceVariant){
			rv = (FileSystemResourceVariant)remote;
			rv.synchRemoteFile();
		}
		
		if (base == null && local.exists()){
			base = remote;			
		}
		if (base != null) {
			boolean exists = rv.getFile().remoteFile.exists();
			if (!exists){
				base = null;
			}
			else {

				if (rv != null){
					long remoteModificationTime = rv.lastModified();					
					SystemIFileProperties properties = new SystemIFileProperties(local);
					long storedModificationTime = properties.getRemoteFileTimeStamp();
				
					if (remoteModificationTime > storedModificationTime){
						// what if it's changed locally too?
						long localDownloadTimeStamp = properties.getDownloadFileTimeStamp();
						long localTimeStamp = local.getLocalTimeStamp();
						if (localTimeStamp > localDownloadTimeStamp){
							base = null; // conflict for both
						}
						else {
							base = new LocalResourceVariant(local);
						}
					}		
				}
			}
		}
		
		FileSystemSyncInfo info = new FileSystemSyncInfo(local, base, remote, new RSEResourceVariantComparator(getSynchronizer()));
		info.init();
		return info;
	}

	/**
	 * Make the resource in-sync.
	 * 
	 * @param resource
	 * 		the resource
	 * @throws TeamException
	 */
	public void makeInSync(IResource resource) throws TeamException {
		ThreeWaySynchronizer synchronizer = getSynchronizer();
		byte[] remoteBytes = synchronizer.getRemoteBytes(resource);
		if (remoteBytes == null) {
			if (!resource.exists())
				synchronizer.flush(resource, IResource.DEPTH_ZERO);
		} else {
			synchronizer.setBaseBytes(resource, remoteBytes);
		}
	}

	/**
	 * Make the change an outgoing change
	 * 
	 * @param resource
	 * @throws TeamException
	 */
	public void markAsMerged(IResource resource, IProgressMonitor monitor) throws TeamException {
		makeInSync(resource);
		try {
			resource.touch(monitor);
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		}
	}
	
	

}
