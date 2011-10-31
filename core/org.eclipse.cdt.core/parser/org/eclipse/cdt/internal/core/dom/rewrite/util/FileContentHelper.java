/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Emanuel Graf IFS
 */
public class FileContentHelper {
	private static final int BUFFER_SIZE = 2048;

	public static String getContent(IFile file, int start) throws CoreException, IOException {
		InputStreamReader reader = getReaderForFile(file);	
		skip(start, reader);
		
		final String rest = readRest(reader);
		reader.close();
		return rest;
	}
	
	public static String getContent(IFile file, int start, int length) {
		try {
			InputStreamReader r = getReaderForFile(file);
			char[] chars = new char[length];
			
			skip(start, r);
			
			read(length, r, chars);
			r.close();
			return new String(chars);
		} catch (IOException e) {
			CCorePlugin.log(e);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return ""; //$NON-NLS-1$
	}

	private static InputStreamReader getReaderForFile(IFile file)
			throws CoreException, UnsupportedEncodingException {
		InputStream contents = file.getContents();
		return new InputStreamReader(contents, file.getCharset());
	}

	private static String readRest(InputStreamReader reader) throws IOException {
		StringBuilder content = new StringBuilder();
		char[] buffer = new char[BUFFER_SIZE];
		int bytesRead = 0;
		while ((bytesRead = reader.read(buffer)) >= 0) {
			content.append(buffer, 0, bytesRead);
		}
		
		return content.toString();
	}
	
	private static void read(int length, InputStreamReader r, char[] bytes) throws IOException {
		int bufferOffset = 0;
		int charactersRead = 0;
		while (charactersRead >= 0 && length > 0) {
			charactersRead = r.read(bytes, bufferOffset, length);
			if (charactersRead > 0) {
				bufferOffset += charactersRead;
				length -= charactersRead;
			}
		}
	}

	private static void skip(int start, InputStreamReader r) throws IOException {
		long skipped = 0;
		while (skipped >= 0 && start > 0 && r.ready()) {
			skipped = r.skip(start);
			if (skipped > 0) {
				start -= skipped;
			}
		}
	}
}
