/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Johnson Ma (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.services.clientserver.archiveutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.eclipse.rse.services.clientserver.archiveutils.TarFile;

/**
 * This class is used to read entries from tar.gz file
 * It read compressed data from GZIPInputStream
 */
public class TgzFile extends TarFile {

	/**
	 * Opens a tar.gz file for reading given the specified File object.
	 * @param file the tar.gz file to be opened for reading.
	 * @throws FileNotFoundException if the file does not exist.
	 * @throws IOException if an I/O error occurs.
	 */
	public TgzFile(File file) throws FileNotFoundException, IOException {
		super(file);
	}
	
	/**
	 * Opens a tar.gz file for reading given the file name.
	 * @param name the name of the tar file to be opened for reading.
	 * @throws FileNotFoundException if the file with the given name does not exist.
	 * @throws IOException if an I/O error occurs.
	 */
	public TgzFile(String name) throws FileNotFoundException, IOException {
		super(name);
	}
	
	/**
	 * Gets the input stream for the tar.gz file.
	 * Get file input steam from superclass, wrap it using GZipInputSteam
	 * @return the input stream for the tar file.
	 * @throws FileNotFoundException if the file does not exist.
	 */
	protected InputStream getInputStream() throws FileNotFoundException {
		InputStream fileInputStream = super.getInputStream();
		GZIPInputStream zipInputStream = null;
		try{
		    zipInputStream = new GZIPInputStream(fileInputStream);
		} catch (IOException ioe) {
			//in that case, the file doesn't exists yet. return the file input stream from base class
            return fileInputStream;
		}
		return zipInputStream;
	}

}
