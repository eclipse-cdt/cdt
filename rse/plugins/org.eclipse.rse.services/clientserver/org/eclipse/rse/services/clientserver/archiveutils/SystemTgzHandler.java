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
package org.eclipse.rse.services.clientserver.archiveutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.eclipse.rse.internal.services.clientserver.archiveutils.TgzFile;

/**
 * Handler class for .tar.gz and .tgz files.
 * 
 * @since 3.0
 */
public class SystemTgzHandler extends SystemTarHandler {

	/**
	 * constructor for the tgz handler
	 * @param file the .tar.gz or .tgz file
	 */
	public SystemTgzHandler(File file) throws IOException {
	    super(file);
	}

	/**
	 * Gets a tar.gz file from the underlying file.
	 * @return the tar file, or <code>null</code> if the tar file does not exist.
	 */
	protected TarFile getTarFile() {

		TarFile tarFile = null;

		try {
			tarFile = new TgzFile(file);
		}
		catch (IOException e) {
			// TODO: log error
		}

		return tarFile;
	}

	protected TarOutputStream getTarOutputStream(File outputFile) throws FileNotFoundException {
		GZIPOutputStream zipOutputStream = null;
		try{
		    zipOutputStream = new GZIPOutputStream(new FileOutputStream(outputFile));
		} catch (IOException ioe) {
			throw new FileNotFoundException(ioe.getMessage());
		}
		TarOutputStream outStream = new TarOutputStream(zipOutputStream);
		return outStream;
	}
}
