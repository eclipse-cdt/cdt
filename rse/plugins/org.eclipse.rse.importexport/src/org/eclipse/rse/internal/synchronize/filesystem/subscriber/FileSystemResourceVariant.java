/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Takuya Miyamoto - Adapted from org.eclipse.team.examples.filesystem / FileSystemResourceVariant
 *******************************************************************************/

package org.eclipse.rse.internal.synchronize.filesystem.subscriber;

import java.io.InputStream;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.internal.importexport.files.UniFilePlus;
import org.eclipse.rse.internal.synchronize.RSESyncUtils;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.CachedResourceVariant;

public class FileSystemResourceVariant extends CachedResourceVariant {

	private UniFilePlus ioFile;
	private byte[] bytes;

	/**
	 * Create a resource variant for the given file. The bytes will be
	 * calculated when they are accessed.
	 * 
	 * @param file
	 * 		the file
	 */
	public FileSystemResourceVariant(UniFilePlus file) {
		this.ioFile = file;
	}

	/**
	 * Create a resource variant for the given file and sync bytes.
	 * 
	 * @param file
	 * 		the file
	 * @param bytes
	 * 		the timestamp bytes
	 */
	public FileSystemResourceVariant(UniFilePlus file, byte[] bytes) {
		this.ioFile = file;
		this.bytes = bytes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.core.variants.CachedResourceVariant#fetchContents(org
	 * .eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void fetchContents(IProgressMonitor monitor) throws TeamException {
		setContents(getContents(), monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.variants.CachedResourceVariant#getCachePath()
	 */
	@Override
	protected String getCachePath() {
		// append the timestamp to the file path to give each variant a unique
		// path
		return getFilePath() + " " + ioFile.lastModified(); //$NON-NLS-1$
	}

	private String getFilePath() {
		return ioFile.getCanonicalPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.variants.CachedResourceVariant#getCacheId()
	 */
	@Override
	protected String getCacheId() {
		// return FileSystemPlugin.ID;
		return RSESyncUtils.PLUGIN_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.variants.IResourceVariant#getName()
	 */
	public String getName() {
		return ioFile.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.variants.IResourceVariant#isContainer()
	 */
	public boolean isContainer() {
		return ioFile.isDirectory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.core.variants.IResourceVariant#getContentIdentifier()
	 */
	public String getContentIdentifier() {
		// Use the modification timestamp as the content identifier
		return new Date(ioFile.lastModified()).toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.variants.IResourceVariant#asBytes()
	 */
	// TODO By using byte[] which this method return, ResourceComparator run
	// comparison.
	// so, this method provide how to compare with which value.
	public byte[] asBytes() {
		if (bytes == null) {
			// For simplicity, convert the timestamp to it's string
			// representation.
			// A more optimal storage format would be the 8 bytes that make up
			// the long.
			bytes = Long.toString(ioFile.lastModified()).getBytes();
		}
		return bytes;
	}

	/**
	 * Return the files contained by the file of this resource variant.
	 * 
	 * @return the files contained by the file of this resource variant.
	 */
	public FileSystemResourceVariant[] members() {
		if (isContainer()) {
			UniFilePlus[] members = (UniFilePlus[]) ioFile.listFiles();
			FileSystemResourceVariant[] result = new FileSystemResourceVariant[members.length];
			for (int i = 0; i < members.length; i++) {
				result[i] = new FileSystemResourceVariant(members[i]);
			}
			return result;
		}
		return new FileSystemResourceVariant[0];
	}

	/**
	 * @return
	 */
	public InputStream getContents() throws TeamException {
		// Takuya: modified for managing remote resource
		try {
			return ioFile.getInputStream();
		} catch (SystemMessageException e) {
			throw new TeamException("Failed to fetch contents for " + getFilePath(), e); //$NON-NLS-1$
		}
	}

	public UniFilePlus getFile() {
		return ioFile;
	}

}
