/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.testplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

/**
 * Copied from org.eclipse.core.filebuffers.tests.
 *
 * @since 4.0
 */
public class FileTool {

	private final static int MAX_RETRY = 5;

	/**
	 * A buffer.
	 */
	private static byte[] buffer = new byte[8192];

	/**
	 * Unzips the given zip file to the given destination directory
	 * extracting only those entries the pass through the given
	 * filter.
	 *
	 * @param zipFile the zip file to unzip
	 * @param dstDir the destination directory
	 * @throws IOException in case of problem
	 */
	public static void unzip(ZipFile zipFile, File dstDir) throws IOException {
		unzip(zipFile, dstDir, dstDir, 0);
	}

	private static void unzip(ZipFile zipFile, File rootDstDir, File dstDir, int depth) throws IOException {

		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		try {
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					continue;
				}
				String entryName = entry.getName();
				File file = new File(dstDir, changeSeparator(entryName, '/', File.separatorChar));
				file.getParentFile().mkdirs();
				InputStream src = null;
				OutputStream dst = null;
				try {
					src = zipFile.getInputStream(entry);
					dst = new FileOutputStream(file);
					transferData(src, dst);
				} finally {
					if (dst != null) {
						try {
							dst.close();
						} catch (IOException e) {
						}
					}
					if (src != null) {
						try {
							src.close();
						} catch (IOException e) {
						}
					}
				}
			}
		} finally {
			try {
				zipFile.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Returns the given file path with its separator
	 * character changed from the given old separator to the
	 * given new separator.
	 *
	 * @param path a file path
	 * @param oldSeparator a path separator character
	 * @param newSeparator a path separator character
	 * @return the file path with its separator character
	 * changed from the given old separator to the given new
	 * separator
	 */
	public static String changeSeparator(String path, char oldSeparator, char newSeparator) {
		return path.replace(oldSeparator, newSeparator);
	}

	/**
	 * Copies all bytes in the given source file to
	 * the given destination file.
	 *
	 * @param source the given source file
	 * @param destination the given destination file
	 * @throws IOException in case of error
	 */
	public static void transferData(File source, File destination) throws IOException {
		destination.getParentFile().mkdirs();
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(destination);
			transferData(is, os);
		} finally {
			if (os != null)
				os.close();
			if (is != null)
				is.close();
		}
	}

	/**
	 * Copies all bytes in the given source stream to
	 * the given destination stream. Neither streams
	 * are closed.
	 *
	 * @param source the given source stream
	 * @param destination the given destination stream
	 * @throws IOException in case of error
	 */
	public static void transferData(InputStream source, OutputStream destination) throws IOException {
		int bytesRead = 0;
		while (bytesRead != -1) {
			bytesRead = source.read(buffer, 0, buffer.length);
			if (bytesRead != -1) {
				destination.write(buffer, 0, bytesRead);
			}
		}
	}

	/**
	 * Copies the given source file to the given destination file.
	 *
	 * @param src the given source file
	 * @param dst the given destination file
	 * @throws IOException in case of error
	 */
	public static void copy(File src, File dst) throws IOException {
		if (src.isDirectory()) {
			String[] srcChildren = src.list();
			for (int i = 0; i < srcChildren.length; ++i) {
				File srcChild = new File(src, srcChildren[i]);
				File dstChild = new File(dst, srcChildren[i]);
				copy(srcChild, dstChild);
			}
		} else
			transferData(src, dst);
	}

	public static File getFileInPlugin(Plugin plugin, IPath path) {
		try {
			URL installURL = plugin.getBundle().getEntry(path.toString());
			URL localURL = Platform.asLocalURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException e) {
			return null;
		}
	}

	public static File createTempFileInPlugin(Plugin plugin, IPath path) {
		IPath stateLocation = plugin.getStateLocation();
		stateLocation = stateLocation.append(path);
		return stateLocation.toFile();
	}

	public static StringBuffer read(String fileName) throws IOException {
		return read(new FileReader(fileName));
	}

	public static StringBuffer read(Reader reader) throws IOException {
		StringBuffer s = new StringBuffer();
		try {
			char[] charBuffer = new char[8196];
			int chars = reader.read(charBuffer);
			while (chars != -1) {
				s.append(charBuffer, 0, chars);
				chars = reader.read(charBuffer);
			}
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
		return s;
	}

	public static void write(String fileName, StringBuffer content) throws IOException {
		Writer writer = new FileWriter(fileName);
		try {
			writer.write(content.toString());
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
	}

	public static void delete(IPath path) throws CoreException {
		File file = FileBuffers.getSystemFileAtLocation(path);
		delete(file);
	}

	public static void delete(File file) throws CoreException {
		if (file.exists()) {
			for (int i = 0; i < MAX_RETRY; i++) {
				if (file.delete())
					i = MAX_RETRY;
				else {
					try {
						Thread.sleep(1000); // sleep a second
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
}
