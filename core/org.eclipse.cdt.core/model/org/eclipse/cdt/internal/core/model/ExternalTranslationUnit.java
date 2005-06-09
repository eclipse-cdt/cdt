/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * ExternalTranslationUnit
 */
public class ExternalTranslationUnit extends TranslationUnit {

	IPath fPath;

	/**
	 * @param parent
	 * @param path
	 */
	public ExternalTranslationUnit(ICElement parent, IPath path, String contentTypeID) {
		super(parent, (IResource)null, path.toString(), contentTypeID);
		fPath = path;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#openBuffer(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IBuffer openBuffer(IProgressMonitor pm) throws CModelException {

		// create buffer -  translation units only use default buffer factory
		BufferManager bufManager = getBufferManager();		
		IBuffer buffer = getBufferFactory().createBuffer(this);
		if (buffer == null) 
			return null;
	
		// set the buffer source
		if (buffer.getCharacters() == null){
			IPath path = this.getPath();
			File file = path.toFile();
			if (file != null && file.isFile()) {
				try {
					InputStream stream = new FileInputStream(file);
					buffer.setContents(Util.getInputStreamAsCharArray(stream, (int)file.length(), null));
				} catch (IOException e) {
					buffer.setContents(new char[0]);
				}
			} else {
				buffer.setContents(new char[0]);
			}
		}

		// add buffer to buffer cache
		bufManager.addBuffer(buffer);
			
		// listen to buffer changes
		buffer.addBufferChangedListener(this);
	
		return buffer;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getPath()
	 */
	public IPath getPath() {
		return fPath;
	}
}
