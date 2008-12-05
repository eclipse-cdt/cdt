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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author DSchaefe
 *
 */
public class FileListReader {
	
	private final BufferedReader in;
	
	public FileListReader(File fileListfile) throws IOException {
		in = new BufferedReader(new FileReader(fileListfile));
	}
	
	public InstalledFile getNext() throws IOException {
		String line = in.readLine();
		return line != null ? new InstalledFile(line) : null;
	}
	
	public void close() throws IOException {
		in.close();
	}

}
