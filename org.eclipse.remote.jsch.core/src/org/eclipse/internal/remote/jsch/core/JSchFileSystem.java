/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * Kushal Munir (IBM) - moved to internal package
 * Martin Oberhuber (Wind River) - [181917] EFS Improvements: Avoid unclosed Streams,
 *    - Fix early startup issues by deferring FileStore evaluation and classloading,
 *    - Improve performance by RSEFileStore instance factory and caching IRemoteFile.
 *    - Also remove unnecessary class RSEFileCache and obsolete branding files.
 * Martin Oberhuber (Wind River) - [188360] renamed from plugin org.eclipse.rse.eclipse.filesystem
 * Martin Oberhuber (Wind River) - [199587] return attributes of RemoteToolsFileSystem
 ********************************************************************************/

package org.eclipse.internal.remote.jsch.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.IPath;
import org.eclipse.remote.core.exception.RemoteConnectionException;

import com.jcraft.jsch.ChannelSftp;

public class JSchFileSystem extends FileSystem {
	private final Map<String, ChannelSftp> fChannels = new HashMap<String, ChannelSftp>();

	/**
	 * Return the connection name encoded in the URI.
	 * 
	 * @param uri
	 *            URI specifying a remote tools connection
	 * @return name of the connection or null if the URI is invalid
	 * @since 4.0
	 */
	public static String getConnectionNameFor(URI uri) {
		return uri.getAuthority();
	}

	/**
	 * Return an URI uniquely naming a remote tools remote resource.
	 * 
	 * @param connectionName
	 *            remote tools connection name
	 * @param path
	 *            absolute path to resource as valid on the remote system
	 * @return an URI uniquely naming the remote resource.
	 */
	public static URI getURIFor(String connectionName, String path) {
		try {
			return new URI("ssh", connectionName, path, null, null); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Default constructor.
	 */
	public JSchFileSystem() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.IFileSystem#attributes()
	 */
	@Override
	public int attributes() {
		// Attributes supported by JSch IFileService
		return EFS.ATTRIBUTE_READ_ONLY | EFS.ATTRIBUTE_EXECUTABLE | EFS.ATTRIBUTE_SYMLINK | EFS.ATTRIBUTE_LINK_TARGET;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileSystem#canDelete()
	 */
	@Override
	public boolean canDelete() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileSystem#canWrite()
	 */
	@Override
	public boolean canWrite() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.filesystem.provider.FileSystem#getStore(java.net.URI)
	 */
	@Override
	public IFileStore getStore(URI uri) {
		try {
			return JschFileStore.getInstance(uri);
		} catch (Exception e) {
			// Could be an URI format exception
			Activator.log(e);
			return EFS.getNullFileSystem().getStore(uri);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileSystem#getStore(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IFileStore getStore(IPath path) {
		return null;
	}

	/**
	 * Get an sftp channel for the connection being used for the file stores. We only want one channel per connection so that all
	 * file stores using that connection also use the same channel.
	 * 
	 * @return sftp channel or null if monitor is cancelled
	 * @throws RemoteConnectionException
	 *             if a channel can't be obtained
	 */
	public synchronized ChannelSftp getChannel(JSchConnection connection) throws RemoteConnectionException {
		ChannelSftp channel = fChannels.get(connection.getName());
		if (channel == null) {
			channel = connection.getSftpChannel();
			fChannels.put(connection.getName(), channel);
		}
		return channel;
	}
}