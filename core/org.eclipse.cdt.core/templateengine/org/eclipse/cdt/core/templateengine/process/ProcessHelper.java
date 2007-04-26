/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.templateengine.TemplateEngineMessages;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Acts as Helper class for process the processes i.e., copy, replace and append
 * files.
 */
public class ProcessHelper {
	public static final String CONDITION = "condition"; //$NON-NLS-1$
	public static final String START_PATTERN = "$("; //$NON-NLS-1$
	public static final String END_PATTERN = ")"; //$NON-NLS-1$
	public static final String EQUALS = "=="; //$NON-NLS-1$
	public static final String NOT_EQUALS = "!="; //$NON-NLS-1$

	/**
	 * This method is to append the given contents into a file.
	 * 
	 * @param fileContents,
	 *            contents which are appended to the file.
	 * @param toFile,
	 *            a file to append contents.
	 * @throws IOException,
	 *             exception while writing contents into a file
     * 
     * @since 4.0
	 */
	public static void appendFile(String fileContents, File toFile) throws IOException {
		RandomAccessFile raf = null;
		if (!toFile.exists()) {
			throw new FileNotFoundException(" The specified destination file does not exists "); //$NON-NLS-1$
		} else {
			try {
				raf = new RandomAccessFile(toFile, "rw"); //$NON-NLS-1$
				raf.skipBytes((int) raf.length());
				raf.writeBytes(fileContents);
			} finally {
				raf.close();
			}
		}
	}

	/**
	 * This method returns a vector of all replace marker strings. (e.g.,
	 * $(item), vector contains 'item' as one item. , ) is the end pattern.
	 * 
	 * @param str,
	 *            A given string may contains replace markers.
	 * @param pattern
	 *            start pattern (e.g., $( is the start pattern)
	 * @param endPat
	 *            end pattern (e.g., ) is the end pattern)
	 * @return a set of all replace marker strings.
     * 
     * @since 4.0
	 */
	public static Set/*<String>*/ getReplaceKeys(String str) {
		int start = 0;
		int end = 0;
		Set/*<String>*/ replaceStrings = new HashSet/*<String>*/();
		while ((start = str.indexOf(START_PATTERN, start)) >= 0) {
			end = str.indexOf(END_PATTERN, start);
			if (end != -1) {
				replaceStrings.add(str.substring(start + START_PATTERN.length(), end));
				start = end + START_PATTERN.length();
			} else
				start++;
		}
		return replaceStrings;
	}

	/**
	 * This method takes a URL as parameter to read the contents, and to add
	 * into a string buffer.
	 * 
	 * @param source
	 *            URL to read the contents.
	 * @return string, contents of a file specified in the URL source path.
	 * @throws IOException 
     * 
     * @since 4.0
	 */
	public static String readFromFile(URL source) throws IOException {
		char[] chars = new char[4092];
		InputStreamReader contentsReader = null;
		StringBuffer buffer = new StringBuffer();
		if (!new java.io.File(source.getFile()).exists()) {
			throw new FileNotFoundException(TemplateEngineMessages.getString("ProcessHelper.fileNotFound") + source.getFile()); //$NON-NLS-1$
		} else {
			contentsReader = new InputStreamReader(source.openStream());
			int c;
			do {
				c = contentsReader.read(chars);
				if (c == -1)
					break;
				buffer.append(chars, 0, c);
			} while (c != -1);
			contentsReader.close();
		}
		return buffer.toString();
	}

	/**
	 * This method reads contents from source, and writes the contents into
	 * destination file.
	 * 
	 * @param source
	 *            URL to read the contents.
	 * @param dest
	 *            destination file to write the contents.
	 * @throws IOException
     * 
     * @since 4.0
	 */
	public static void copyBinaryFile(URL source, File dest) throws IOException {
		byte[] bytes = new byte[4092];
		if (source != null && dest != null) {
			File file = new File(source.getFile());
			if (file.isFile()) {
				FileInputStream fis = new FileInputStream(file);
				FileOutputStream fos = new FileOutputStream(dest);
				int ch;
				while (true) {
					ch = fis.read(bytes);
					if (ch == -1) {
						break;
					}
					fos.write(bytes, 0, ch);
				}
			}
		}
	}

	/**
	 * This method Creates the Directories in the parent Folder.
	 * @param projectHandle
	 * @param parentFolder
	 * @throws CoreException
     * 
     * @since 4.0
	 */
	public static void mkdirs(IProject projectHandle, IFolder parentFolder) throws CoreException {
		if (parentFolder.getProjectRelativePath().equals(projectHandle.getProjectRelativePath())) {
			return;
		}
		if (!parentFolder.getParent().exists()) {
			mkdirs(projectHandle, projectHandle.getFolder(parentFolder.getParent().getProjectRelativePath()));
		}
		parentFolder.create(true, true, null);
	}


	/**
	 * Returns the Macro Value after Exanding the Macros.
	 * @param string
	 * @param macros
	 * @param valueStore
	 * @return
     * 
     * @since 4.0
	 */
	public static String getValueAfterExpandingMacros(String string, Set/*<String>*/ macros, Map/*<String, String>*/ valueStore) {
		for (Iterator i = macros.iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = (String) valueStore.get(key);
			if (value != null) {
				string = replace(START_PATTERN + key + END_PATTERN, value, string);
			}
		}
		return string;
	}
	
	/**
	 * This is equivalent to Java 5.0 version of 
	 * String.replace(CharSequence target, CharSequence replacement) method.
	 * @since 4.0
	 */
	private static String replace(String target, String replacement, String string) {
		try {
			StringBuffer stringBuffer = new StringBuffer(string);

			int index = string.length();
			int offset = target.length();

			while ((index=string.lastIndexOf(target, index-1)) != -1) {
				stringBuffer.replace(index, index+offset, replacement);
			}

			return stringBuffer.toString();
		} catch (StringIndexOutOfBoundsException e) {
			return string;
		}
	}

	/**
	 * Consturct and Return the Replacment Markers 
	 * after adding the patterns to the macro.
	 * @param macro
	 * @return
     * 
     * @since 4.0
	 */
	public static String getReplaceMarker(String macro) {
		return START_PATTERN + macro + END_PATTERN;
	}
}
