/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Institute for Software - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Some helper methods to access part of the content of an iFile
 *
 * @author Emanuel Graf
 *
 */
public class FileContentHelper {

	private static final int bufferSize = 512;

	public static String getContent(IFile file, int start) throws CoreException, IOException {
		InputStreamReader reader = getReaderForFile(file);
		skip(start, reader);
		return readRest(reader);
	}

	public static String getContent(IFile file, int start, int length) {
		try {
			InputStreamReader r = getReaderForFile(file);
			char[] bytes = new char[length];

			skip(start, r);

			read(length, r, bytes);

			return new String(bytes);
		} catch (IOException e) {
			CUIPlugin.log(e);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return ""; //$NON-NLS-1$
	}

	private static InputStreamReader getReaderForFile(IFile file) throws CoreException, UnsupportedEncodingException {
		InputStream contents = file.getContents();
		InputStreamReader r = new InputStreamReader(contents, file.getCharset());
		return r;
	}

	private static String readRest(InputStreamReader reader) throws IOException {
		StringBuilder content = new StringBuilder();
		char[] buffer = new char[bufferSize];
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

	private static void skip(int count, InputStreamReader r) throws IOException {
		long skipped = 0;
		while (skipped >= 0 && count > 0 && r.ready()) {
			skipped = r.skip(count);
			if (skipped > 0) {
				count -= skipped;
			}
		}
	}
}
