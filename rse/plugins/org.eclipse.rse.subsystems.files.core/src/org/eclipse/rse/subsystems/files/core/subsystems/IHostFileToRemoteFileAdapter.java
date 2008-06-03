/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [235363][api][breaking] IHostFileToRemoteFileAdapter methods should return AbstractRemoteFile
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.subsystems;

import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystemConfiguration;


/**
 * Provider interface for implementers of RSE IFileService instances, by which
 * they convert their internal service objects into objects suitable for the RSE
 * file subsystem.
 *
 * Must be implemented and returned by overriding the
 * {@link IFileServiceSubSystemConfiguration#getHostFileAdapter()} method in the
 * contributed concrete file subsystem configuration.
 */
public interface IHostFileToRemoteFileAdapter
{
	/**
	 * Convert a list of IHostFile objects from the file service into remote
	 * file objects. Used to return IRemoteFile[] before RSE 3.0, returns
	 * AbstractRemoteFile since RSE 3.0.
	 *
	 * @param ss The file service subsystem to which the remote files belong.
	 * @param context The context (connection, subsystem, filter) under which
	 *            the files have been queried.
	 * @param parent The parent IRemoteFile below which the new nodes should
	 *            appear. Can be <code>null</code> when converting root files.
	 * @param nodes IHostFile nodes from the file service
	 * @return an array of converted remote file objects.
	 *
	 * @since org.eclipse.rse.subsystems.files.core 3.0
	 */
	public AbstractRemoteFile[] convertToRemoteFiles(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, IHostFile[] nodes);

	/**
	 * Convert a single IHostFile object from the file service into a remote
	 * file object. Used to return IRemoteFile before RSE 3.0, returns
	 * AbstractRemoteFile since RSE 3.0.
	 * 
	 * @param ss The file service subsystem to which the remote files belong.
	 * @param context The context (connection, subsystem, filter) under which
	 *            the files have been queried.
	 * @param parent The parent IRemoteFile below which the new nodes should
	 *            appear. Can be <code>null</code> when converting root files.
	 * @param node IHostFile node from the file service
	 * @return converted remote file object.
	 * 
	 * @since org.eclipse.rse.subsystems.files.core 3.0
	 */
	public AbstractRemoteFile convertToRemoteFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, IHostFile node);
}
