/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.p2.internal.repo.artifact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

/**
 * @author DSchaefe
 *
 */
public class TarExtractor extends Thread {

	private final InputStream in;
	private final File installDir;
	private final FileListWriter fileListWriter;
	private final String compression;
	
	public TarExtractor(InputStream in, File installDir, FileListWriter fileListWriter, String compression) {
		this.in = in;
		this.installDir = installDir;
		this.fileListWriter = fileListWriter;
		this.compression = compression;
	}

	@Override
	public void run() {
		try {
			InputStream compIn;
			if (InstallArtifactRepository.GZIP_COMPRESSON.equals(compression))
				compIn = new GZIPInputStream(in);
			else if (InstallArtifactRepository.BZIP2_COMPRESSION.equals(compression)) {
				// Skip the magic bytes first		
				in.read(new byte[2]);
				compIn = new CBZip2InputStream(in);
			} else {
				// No idea
				return;
			}
			
			TarInputStream tarIn = new TarInputStream(compIn);
			for (TarEntry tarEntry = tarIn.getNextEntry(); tarEntry != null; tarEntry = tarIn.getNextEntry()) {
				File outFile = new File(installDir, tarEntry.getName());
				if (tarEntry.isDirectory()) {
					outFile.mkdirs();
				} else {
					if (outFile.exists())
						outFile.delete();
					else
						outFile.getParentFile().mkdirs();
					FileOutputStream outStream = new FileOutputStream(outFile);
					tarIn.copyEntryContents(outStream);
					outStream.close();
					long lastModified = tarEntry.getModTime().getTime();
					outFile.setLastModified(lastModified);
					fileListWriter.addFile(new InstalledFile(outFile, lastModified));
				}
			}
			tarIn.close();
			fileListWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
