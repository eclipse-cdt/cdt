/*******************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *     IBM Corporation - EFS support
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.internal.core.util.CharArrayBuffer;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.ibm.icu.text.MessageFormat;

public class Util implements ICLogConstants {
	public static boolean VERBOSE_PARSER = false;
	public static boolean VERBOSE_SCANNER = false;
	public static boolean VERBOSE_MODEL = false;
	public static boolean VERBOSE_DELTA = false;
	public static boolean PARSER_EXCEPTIONS= false;

	public static String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$

	private Util() {
	}

	public static StringBuffer getContent(IFile file) throws IOException {
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(file.getContents(true));
		} catch (CoreException e) {
			throw new IOException(e.getMessage());
		}
		try {
			char[] b = getInputStreamAsCharArray(stream, -1, null);
			return new StringBuffer(b.length).append(b);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Returns the given input stream's contents as a character array. If a
	 * length is specified (ie. if length != -1), only length chars are
	 * returned. Otherwise all chars in the stream are returned. Closes the stream.
	 * 
	 * @throws IOException
	 *             if a problem occured reading the stream.
	 */
	public static char[] getInputStreamAsCharArray(InputStream stream,
			int length, String encoding) throws IOException {
		final InputStreamReader reader = encoding == null
				? new InputStreamReader(stream)
				: new InputStreamReader(stream, encoding);
		try {
			char[] contents;
			if (length == -1) {
				contents = new char[0];
				int contentsLength = 0;
				int charsRead = -1;
				do {
					int available = stream.available();
					// resize contents if needed
					if (contentsLength + available > contents.length) {
						System.arraycopy(contents, 0,
								contents = new char[contentsLength + available], 0,
								contentsLength);
					}
					// read as many chars as possible
					charsRead = reader.read(contents, contentsLength, available);
					if (charsRead > 0) {
						// remember length of contents
						contentsLength += charsRead;
					}
				} while (charsRead > 0);
				// resize contents if necessary
				if (contentsLength < contents.length) {
					System.arraycopy(contents, 0,
							contents = new char[contentsLength], 0, contentsLength);
				}
			} else {
				contents = new char[length];
				int len = 0;
				int readSize = 0;
				while ((readSize != -1) && (len != length)) {
					// See PR 1FMS89U
					// We record first the read size. In this case len is the
					// actual read size.
					len += readSize;
					readSize = reader.read(contents, len, length - len);
				}
				// See PR 1FMS89U
				// Now we need to resize in case the default encoding used more
				// than one byte for each
				// character
				if (len != length)
					System.arraycopy(contents, 0, (contents = new char[len]), 0,
							len);
			}
			return contents;
		} finally {
			reader.close();
		}
	}

	/**
	 * Returns the given file's contents as a character array.
	 */
	public static char[] getResourceContentsAsCharArray(IFile file)
			throws CModelException {
		try {
			return getResourceContentsAsCharArray(file, file.getCharset());
		} catch (CoreException exc) {
			throw new CModelException(exc, ICModelStatusConstants.CORE_EXCEPTION);
		}
	}

	public static char[] getResourceContentsAsCharArray(IFile file,
			String encoding) throws CModelException {
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(file.getContents(true));
		} catch (CoreException e) {
			throw new CModelException(e,
					ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST);
		}
		try {
			return Util.getInputStreamAsCharArray(stream, -1, encoding);
		} catch (IOException e) {
			throw new CModelException(e, ICModelStatusConstants.IO_EXCEPTION);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
	}

	/*
	 * Add a log entry
	 */
	public static void log(Throwable e, String message, LogConst logType) {
		IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, message,e);
		Util.log(status, logType);
	}

	public static void log(IStatus status, LogConst logType) {
		if (logType.equals(ICLogConstants.PDE)) {
			CCorePlugin.getDefault().getLog().log(status);
		} else if (logType.equals(ICLogConstants.CDT)) {
			CCorePlugin.getDefault().cdtLog.log(status);
		}
	}

	public static void log(String message, LogConst logType) {
		IStatus status = new Status(IStatus.INFO, CCorePlugin.PLUGIN_ID, IStatus.INFO, message,	null);
		Util.log(status, logType);
	}

	public static void debugLog(String message, DebugLogConstants client) {
		Util.debugLog(message, client, true);
	}

	public static void debugLog(String message, DebugLogConstants client,
			boolean addTimeStamp) {
		if (CCorePlugin.getDefault() == null)
			return;
		if (CCorePlugin.getDefault().isDebugging() && isActive(client)) {
			// Time stamp
			if (addTimeStamp)
				message = MessageFormat.format("[{0}] {1}", new Object[]{ //$NON-NLS-1$
						new Long(System.currentTimeMillis()), message}); 
			while (message.length() > 100) {
				String partial = message.substring(0, 100);
				message = message.substring(100);
				System.out.println(partial + "\\"); //$NON-NLS-1$
			}
			if (message.endsWith("\n")) { //$NON-NLS-1$
				System.err.print(message);
			} else {
				System.out.println(message);
			}
		}
	}

	public static boolean isActive(DebugLogConstants client) {
		if (client.equals(DebugLogConstants.PARSER)) {
			return VERBOSE_PARSER;
		} else if (client.equals(DebugLogConstants.SCANNER))
			return VERBOSE_SCANNER;
		else if (client.equals(DebugLogConstants.MODEL)) {
			return VERBOSE_MODEL;
		}
		return false;
	}

	public static void setDebugging(boolean value) {
		CCorePlugin.getDefault().setDebugging(value);
	}

	/**
	 * Combines two hash codes to make a new one.
	 */
	public static int combineHashCodes(int hashCode1, int hashCode2) {
		return hashCode1 * 17 + hashCode2;
	}

	/**
	 * Compares two arrays using equals() on the elements. Either or both
	 * arrays may be null. Returns true if both are null. Returns false if only
	 * one is null. If both are arrays, returns true iff they have the same
	 * length and all elements compare true with equals.
	 */
	public static boolean equalArraysOrNull(Object[] a, Object[] b) {
		if (a == b)
			return true;
		if (a == null || b == null)
			return false;
		int len = a.length;
		if (len != b.length)
			return false;
		for (int i = 0; i < len; ++i) {
			if (a[i] == null) {
				if (b[i] != null)
					return false;
			} else {
				if (!a[i].equals(b[i]))
					return false;
			}
		}
		return true;
	}

	/**
	 * Compares two arrays using equals() on the elements. Either or both
	 * arrays may be null. Returns true if both are null. Returns false if only
	 * one is null. If both are arrays, returns true iff they have the same
	 * length and all elements are equal.
	 */
	public static boolean equalArraysOrNull(int[] a, int[] b) {
		if (a == b)
			return true;
		if (a == null || b == null)
			return false;
		int len = a.length;
		if (len != b.length)
			return false;
		for (int i = 0; i < len; ++i) {
			if (a[i] != b[i])
				return false;
		}
		return true;
	}

	/**
	 * Compares two objects using equals(). Either or both array may be null.
	 * Returns true if both are null. Returns false if only one is null.
	 * Otherwise, return the result of comparing with equals().
	 */
	public static boolean equalOrNull(Object a, Object b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return a.equals(b);
	}

	/**
	 * Normalizes the cariage returns in the given text.
	 * They are all changed  to use the given buffer's line separator.
	 */
	public static char[] normalizeCRs(char[] text, char[] buffer) {
		CharArrayBuffer result = new CharArrayBuffer();
		int lineStart = 0;
		int length = text.length;
		if (length == 0) return text;
		String lineSeparator = getLineSeparator(text, buffer);
		char nextChar = text[0];
		for (int i = 0; i < length; i++) {
			char currentChar = nextChar;
			nextChar = i < length-1 ? text[i+1] : ' ';
			switch (currentChar) {
				case '\n':
					int lineLength = i-lineStart;
					char[] line = new char[lineLength];
					System.arraycopy(text, lineStart, line, 0, lineLength);
					result.append(line);
					result.append(lineSeparator);
					lineStart = i+1;
					break;
				case '\r':
					lineLength = i-lineStart;
					if (lineLength >= 0) {
						line = new char[lineLength];
						System.arraycopy(text, lineStart, line, 0, lineLength);
						result.append(line);
						result.append(lineSeparator);
						if (nextChar == '\n') {
							nextChar = ' ';
							lineStart = i+2;
						} else {
							// when line separator are mixed in the same file
							// \r might not be followed by a \n. If not, we should increment
							// lineStart by one and not by two.
							lineStart = i+1;
						}
					} else {
						// when line separator are mixed in the same file
						// we need to prevent NegativeArraySizeException
						lineStart = i+1;
					}
					break;
			}
		}
		char[] lastLine;
		if (lineStart > 0) {
			int lastLineLength = length-lineStart;
			if (lastLineLength > 0) {
				lastLine = new char[lastLineLength];
				System.arraycopy(text, lineStart, lastLine, 0, lastLineLength);
				result.append(lastLine);
			}
			return result.getContents();
		}
		return text;
	}

	/**
	 * Normalizes the cariage returns in the given text.
	 * They are all changed  to use given buffer's line sepatator.
	 */
	public static String normalizeCRs(String text, String buffer) {
		return new String(normalizeCRs(text.toCharArray(), buffer.toCharArray()));
	}

	/**
	 * Returns the line separator used by the given buffer.
	 * Uses the given text if none found.
	 *
	 * @return </code>"\n"</code> or </code>"\r"</code> or  </code>"\r\n"</code>
	 */
	private static String getLineSeparator(char[] text, char[] buffer) {
		// search in this buffer's contents first
		String lineSeparator = findLineSeparator(buffer);
		if (lineSeparator == null) {
			// search in the given text
			lineSeparator = findLineSeparator(text);
			if (lineSeparator == null) {
				// default to system line separator
				return LINE_SEPARATOR;
			}
		}
		return lineSeparator;
	}

	/**
	 * Finds the first line separator used by the given text.
	 *
	 * @return </code>"\n"</code> or </code>"\r"</code> or  </code>"\r\n"</code>,
	 *			or <code>null</code> if none found
	 */
	public static String findLineSeparator(char[] text) {
		// find the first line separator
		int length = text.length;
		if (length > 0) {
			char nextChar = text[0];
			for (int i = 0; i < length; i++) {
				char currentChar = nextChar;
				nextChar = i < length-1 ? text[i+1] : ' ';
				switch (currentChar) {
					case '\n': return "\n"; //$NON-NLS-1$
					case '\r': return nextChar == '\n' ? "\r\n" : "\r"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		// not found
		return null;
	}

	/**
	 * Returns value of line separator preference from the given preference node.
	 */
	private static String getLineSeparatorFromPreferences(Preferences node) {
		try {
			// be careful looking up for our node so not to create any nodes as side effect
			if (node.nodeExists(Platform.PI_RUNTIME))
				return node.node(Platform.PI_RUNTIME).get(Platform.PREF_LINE_SEPARATOR, null);
		} catch (BackingStoreException e) {
			// ignore
		}
		return null;
	}

	/**
	 * Returns the first line separator found in the given file.
	 * 
	 * @param fileUri - URI if the file on the local file-system.
	 * @return the first line separator in the given file or {@code null} if none was read.
	 */
	public static String getLineSeparator(URI fileUri) {
		String value = null;
		InputStream input = null;
		try {
			java.io.File file = new java.io.File(fileUri);
			if (file.exists()) {
				input = new FileInputStream(file);
				value = getLineSeparator(input);
			}
		} catch (Exception e) {
			// ignore
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException e) {
				//ignore
			}
		}
		return value;
	}

	/**
	 * Returns the line separator for the given file. If line separator is not found in the file
	 * default value for the project or workspace is returned.
	 * 
	 * @param file - the file to look for a line separator.
	 * @return the line separator for the given file. The method does not return {@code null}.
	 */
	public static String getLineSeparator(IFile file) {
		String value = null;
		InputStream input = null;
		try {
			input = file.getContents();
			value = getLineSeparator(input);
		} catch (CoreException e) {
			// ignore
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException e) {
				//ignore
			}
		}

		if (value == null)
			value = getDefaultLineSeparator(file.getProject());

		return value;
	}

	/**
	 * Returns default line separator for the given project. The returned value
	 * will be the first available value from the list below:
	 * <ol>
	 *   <li> Line separator defined in project preferences.
	 *   <li> Line separator defined in instance preferences.
	 *   <li> Line separator defined in default preferences.
	 *   <li> Operating system default line separator.
	 * </ol>
	 * 
	 * @param project - the project. If {@code null} no project preferences are inquired.
	 * @return line separator for the given project. The method does not return {@code null}.
	 */
	public static String getDefaultLineSeparator(IProject project) {
		String value = null;
		Preferences rootNode = Platform.getPreferencesService().getRootNode();
		// if the file does not exist or has no content yet, try with project preferences
		if (project != null) {
			value = getLineSeparatorFromPreferences(rootNode.node(ProjectScope.SCOPE).node(project.getName()));
			if (value != null)
				return value;
		}
		value = getDefaultLineSeparator();
		return value;
	}

	/**
	 * Returns default line separator for the workspace. The returned value
	 * will be the first available value from the list below:
	 * <ol>
	 *   <li> Line separator defined in instance preferences.
	 *   <li> Line separator defined in default preferences.
	 *   <li> Operating system default line separator.
	 * </ol>
	 * @return line separator for the workspace. The method does not return {@code null}.
	 */
	public static String getDefaultLineSeparator() {
		Preferences rootNode = Platform.getPreferencesService().getRootNode();
		String value = null;

		// try with instance preferences
		value = getLineSeparatorFromPreferences(rootNode.node(InstanceScope.SCOPE));
		if (value != null)
			return value;
		// try with default preferences
		value = getLineSeparatorFromPreferences(rootNode.node(DefaultScope.SCOPE));
		if (value != null)
			return value;
		// if there is no preference set, fall back to OS default value
		return LINE_SEPARATOR;
	}

	/**
	 * Returns the first line separator used by the input stream.
	 * 
	 * @param input - input stream to inspect.
	 * @return line separator for the given input stream or {@code null} if no line separator was read.
	 */
	private static String getLineSeparator(InputStream input) {
		try {
			int c = input.read();
			while (c != -1 && c != '\r' && c != '\n')
				c = input.read();
			if (c == '\n')
				return "\n"; //$NON-NLS-1$
			if (c == '\r') {
				int c2 = input.read();
				if (c2 == '\n')
					return "\r\n"; //$NON-NLS-1$
				return "\r"; //$NON-NLS-1$
			}
		} catch (IOException e) {
			// ignore
		}
		return null;
	}

	/**
	 * Return true if the file is not a directory and has length > 0
	 */
	public static boolean isNonZeroLengthFile(IPath path) {
		return isNonZeroLengthFile(URIUtil.toURI(path));
	}
	
	/**
	 * Return true if the file referred to by the URI is not a directory and has length > 0
	 */
	public static boolean isNonZeroLengthFile(URI uri) {
		try {
			IFileInfo file = EFS.getStore(uri).fetchInfo();
			if (file.getLength() == EFS.NONE) {
				return false;
			}
			return true;
		} catch (CoreException e) {
			// ignore
		}
		return false;
	}
}
