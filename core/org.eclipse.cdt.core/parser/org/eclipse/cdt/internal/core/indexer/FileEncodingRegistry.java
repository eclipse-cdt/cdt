/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.indexer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * this class is a registry which maps file name and file's encoding, the class
 * is used by standalone indexer
 * 
 * @author johnliu
 * 
 */
public class FileEncodingRegistry implements Serializable {

	private Map<String, String> fFilePathToEncodingMap = null;
	private String defaultEncoding;

	public FileEncodingRegistry(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
		fFilePathToEncodingMap = new TreeMap<String, String>();
	}

	
	public void setDefaultEncoding(String newDefaultEncoding) {
		defaultEncoding = newDefaultEncoding;

	}

	public void registerFileEncoding(String filename, String encoding) {
		if(defaultEncoding.equals(encoding)){
			return;
		}
		if (fFilePathToEncodingMap != null) {
			fFilePathToEncodingMap.put(filename, encoding);
		}

	}

	public void unregisterFile(String filename) {
		if (fFilePathToEncodingMap != null) {
			fFilePathToEncodingMap.remove(filename);
		}

	}

	public String getFileEncoding(String filename) {
		String fileEncoding = null;
		if (fFilePathToEncodingMap != null) {
			fileEncoding = fFilePathToEncodingMap.get(filename);
		}
		if (fileEncoding != null) {
			return fileEncoding;

		} else {
			return defaultEncoding;
		}
	}

	public void clear() {
		if (fFilePathToEncodingMap != null) {
			fFilePathToEncodingMap.clear();
		}
		fFilePathToEncodingMap = null;
		defaultEncoding = null;

	}

	// send as little over the wire as possible
	private void writeObject(ObjectOutputStream out) throws IOException {
		if (fFilePathToEncodingMap != null && fFilePathToEncodingMap.isEmpty()) {
			fFilePathToEncodingMap = null;
		}
		out.defaultWriteObject();
	}

}
