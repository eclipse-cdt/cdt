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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

/**
 * @author DSchaefe
 *
 */
public class InstalledFile {
	
	private final File file;
	private final long lastModified;
	
	public InstalledFile(File _file, long _lastModified) {
		file = _file;
		lastModified = _lastModified;
	}
	
	InstalledFile(String line) {
		String[] entries = line.split(","); //$NON-NLS-1$
		if (entries.length < 2) {
			file = null;
			lastModified = 0;
			return;
		}
		file = new File(entries[0]);
		lastModified = Long.parseLong(entries[1]);
	}
	
	public File getFile() {
		return file;
	}
	
	public long getLastModified() {
		return lastModified;
	}
	
	public void uninstall() {
		if (file != null && file.lastModified() == lastModified) {
			File f = file;
			while (f != null && f.delete())
				f = f.getParentFile();
		}
	}
	
	void write(BufferedWriter out) throws IOException {
		out.write(file.getAbsolutePath());
		out.write(',');
		out.write(String.valueOf(lastModified));
		out.newLine();
	}
	
}
