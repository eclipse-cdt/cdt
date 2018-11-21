/*******************************************************************************
 * Copyright (c) 2006, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.junit.Assert;
import org.osgi.framework.Bundle;

/**
 * Utilities for reading test source code from plug-in .java sources
 */
public class TestSourceReader {
	private final Bundle bundle;
	private final String srcRoot;
	private final Class clazz;
	private final int numSections;

	/**
	 * @param bundle the bundle containing the source, if {@code null} can try to load using
	 *     classpath (source folder has to be in the classpath for this to work)
	 * @param srcRoot the directory inside the bundle containing the packages
	 * @param clazz the name of the class containing the test
	 */
	public TestSourceReader(Bundle bundle, String srcRoot, Class clazz) {
		this(bundle, srcRoot, clazz, 0);
	}

	/**
	 * @param bundle the bundle containing the source, if {@code null} can try to load using
	 *     classpath (source folder has to be in the classpath for this to work)
	 * @param srcRoot the directory inside the bundle containing the packages
	 * @param clazz the name of the class containing the test
	 * @param numSections the number of comment sections preceding the named test to return.
	 *     Pass zero to get all available sections.
	 */
	public TestSourceReader(Bundle bundle, String srcRoot, Class clazz, int numSections) {
		this.bundle = bundle;
		this.srcRoot = srcRoot;
		this.clazz = clazz;
		this.numSections = numSections;
	}

	public StringBuilder[] getContentsForTest(final String testName) throws IOException {
		return getContentsForTest(bundle, srcRoot, clazz, testName, numSections);
	}

	public String readTaggedComment(String tag) throws IOException {
		return readTaggedComment(bundle, tag, clazz, tag);
	}

	/**
	 * Returns an array of StringBuilder objects for each comment section found preceding the named
	 * test in the source code.
	 *
	 * @param bundle the bundle containing the source, if {@code null} can try to load using
	 *      classpath (source folder has to be in the classpath for this to work)
	 * @param srcRoot the directory inside the bundle containing the packages
	 * @param clazz the name of the class containing the test
	 * @param testName the name of the test
	 * @param numSections the number of comment sections preceding the named test to return.
	 *     Pass zero to get all available sections.
	 * @return an array of StringBuilder objects for each comment section found preceding the named
	 *     test in the source code.
	 * @throws IOException
	 */
	public static StringBuilder[] getContentsForTest(Bundle bundle, String srcRoot, Class clazz, final String testName,
			int numSections) throws IOException {
		// Walk up the class inheritance chain until we find the test method.
		try {
			while (clazz.getMethod(testName).getDeclaringClass() != clazz) {
				clazz = clazz.getSuperclass();
			}
		} catch (SecurityException e) {
			Assert.fail(e.getMessage());
		} catch (NoSuchMethodException e) {
			Assert.fail(e.getMessage());
		}

		while (true) {
			// Find and open the .java file for the class clazz.
			String fqn = clazz.getName().replace('.', '/');
			fqn = fqn.indexOf("$") == -1 ? fqn : fqn.substring(0, fqn.indexOf("$"));
			String classFile = fqn + ".java";
			IPath filePath = new Path(srcRoot + '/' + classFile);

			InputStream in;
			Class superclass = clazz.getSuperclass();
			try {
				if (bundle != null) {
					in = FileLocator.openStream(bundle, filePath, false);
				} else {
					in = clazz.getResourceAsStream('/' + classFile);
				}
			} catch (IOException e) {
				if (superclass == null || !superclass.getPackage().equals(clazz.getPackage())) {
					throw e;
				}
				clazz = superclass;
				continue;
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			try {
				// Read the java file collecting comments until we encounter the test method.
				List<StringBuilder> contents = new ArrayList<>();
				StringBuilder content = new StringBuilder();
				for (String line = br.readLine(); line != null; line = br.readLine()) {
					line = line.replaceFirst("^\\s*", ""); // Replace leading whitespace, preserve trailing
					// Trailing whitespace can be removed by editor/clean-up actions. To enforce whitespace
					// at end of line, use ${whitspace_eol}, which will be removed, but cause the
					// whitespace to the left of it to be preserved.
					line = line.replace("${whitespace_eol}", "");
					if (line.startsWith("//")) {
						content.append(line.substring(2)).append('\n');
					} else {
						if (!line.startsWith("@") && content.length() > 0) {
							contents.add(content);
							if (numSections > 0 && contents.size() == numSections + 1)
								contents.remove(0);
							content = new StringBuilder();
						}
						if (line.length() > 0 && !contents.isEmpty()) {
							int idx = line.indexOf(testName);
							if (idx != -1 && !Character.isJavaIdentifierPart(line.charAt(idx + testName.length()))) {
								return contents.toArray(new StringBuilder[contents.size()]);
							}
							if (!line.startsWith("@")) {
								contents.clear();
							}
						}
					}
				}
			} finally {
				br.close();
			}

			if (superclass == null || !superclass.getPackage().equals(clazz.getPackage())) {
				throw new IOException("Test data not found for " + clazz.getName() + "." + testName);
			}
			clazz = superclass;
		}
	}

	/**
	 * Searches for the offset of the first occurrence of a string in a workspace file.
	 * @param lookfor string to be searched for
	 * @param fullPath full path of the workspace file
	 * @return the offset or -1
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 * @since 4.0
	 */
	public static int indexOfInFile(String lookfor, Path fullPath) throws Exception {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(fullPath);
		Reader reader = new BufferedReader(new InputStreamReader(file.getContents(), file.getCharset()));
		Assert.assertTrue(lookfor.indexOf('\n') == -1);
		try {
			int c = 0;
			int offset = 0;
			StringBuilder buf = new StringBuilder();
			while ((c = reader.read()) >= 0) {
				buf.append((char) c);
				if (c == '\n') {
					int idx = buf.indexOf(lookfor);
					if (idx >= 0) {
						return idx + offset;
					}
					offset += buf.length();
					buf.setLength(0);
				}
			}
			int idx = buf.indexOf(lookfor);
			if (idx >= 0) {
				return idx + offset;
			}
			return -1;
		} finally {
			reader.close();
		}
	}

	public static int getLineNumber(int offset, Path fullPath) throws Exception {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(fullPath);
		Reader reader = new BufferedReader(new InputStreamReader(file.getContents(), file.getCharset()));
		try {
			int line = 1;
			for (int i = 0; i < offset; i++) {
				int c = reader.read();
				Assert.assertTrue(c >= 0);
				if (c == '\n')
					line++;
			}
			return line;
		} finally {
			reader.close();
		}
	}

	/**
	 * Reads a section in comments form the source of the given class. The section
	 * is started with '// {tag}' and ends with the first line not started by '//'
	 * @since 4.0
	 */
	public static String readTaggedComment(Bundle bundle, String srcRoot, Class clazz, final String tag)
			throws IOException {
		IPath filePath = new Path(srcRoot + '/' + clazz.getName().replace('.', '/') + ".java");

		InputStream in = FileLocator.openStream(bundle, filePath, false);
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
		boolean found = false;
		final StringBuilder content = new StringBuilder();
		try {
			String line = reader.readLine();
			while (line != null) {
				line = line.trim();
				if (line.startsWith("//")) {
					line = line.substring(2);
					if (found) {
						content.append(line);
						content.append('\n');
					} else {
						line = line.trim();
						if (line.startsWith("{" + tag)) {
							if (line.length() == tag.length() + 1
									|| !Character.isJavaIdentifierPart(line.charAt(tag.length() + 1))) {
								found = true;
							}
						}
					}
				} else if (found) {
					break;
				}
				line = reader.readLine();
			}
		} finally {
			reader.close();
		}
		Assert.assertTrue("Tag '" + tag + "' is not defined inside of '" + filePath + "'.", found);
		return content.toString();
	}

	/**
	 * Creates a file with content at the given path inside the given container.
	 * If the file exists its content is replaced.
	 * @param container a container to create the file in
	 * @param filePath the path relative to the container to create the file at
	 * @param contents the content for the file
	 * @return a file object.
	 * @throws CoreException
	 * @since 4.0
	 */
	public static IFile createFile(final IContainer container, final IPath filePath, final CharSequence contents)
			throws CoreException {
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		final IFile result[] = new IFile[1];
		ws.run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				// Obtain file handle
				IFile file = container.getFile(filePath);

				InputStream stream;
				try {
					stream = new ByteArrayInputStream(contents.toString().getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new CoreException(new Status(IStatus.ERROR, CTestPlugin.PLUGIN_ID, null, e));
				}
				// Create file input stream
				if (file.exists()) {
					long timestamp = file.getLocalTimeStamp();
					file.setContents(stream, false, false, new NullProgressMonitor());
					if (file.getLocalTimeStamp() == timestamp) {
						file.setLocalTimeStamp(timestamp + 1000);
					}
				} else {
					createFolders(file);
					file.create(stream, true, new NullProgressMonitor());
				}
				result[0] = file;
			}

			private void createFolders(IResource res) throws CoreException {
				IContainer container = res.getParent();
				if (!container.exists() && container instanceof IFolder) {
					createFolders(container);
					((IFolder) container).create(true, true, new NullProgressMonitor());
				}
			}
		}, null);
		return result[0];
	}

	/**
	 * Creates a file with content at the given path inside the given container.
	 * If the file exists its content is replaced.
	 * @param container a container to create the file in
	 * @param filePath the path relative to the container to create the file at
	 * @param contents the content for the file
	 * @return a file object.
	 * @since 4.0
	 */
	public static IFile createFile(IContainer container, String filePath, String contents) throws CoreException {
		return createFile(container, new Path(filePath), contents);
	}

	/**
	 * Waits until the given file is indexed. Fails if this does not happen within the
	 * given time.
	 * @param file
	 * @param maxmillis
	 * @throws Exception
	 * @since 4.0
	 */
	public static void waitUntilFileIsIndexed(IIndex index, IFile file, int maxmillis) throws Exception {
		long fileTimestamp = file.getLocalTimeStamp();
		IIndexFileLocation indexFileLocation = IndexLocationFactory.getWorkspaceIFL(file);

		long endTime = System.currentTimeMillis() + maxmillis;
		int timeLeft = maxmillis;
		while (timeLeft >= 0) {
			Assert.assertTrue(CCorePlugin.getIndexManager().joinIndexer(timeLeft, new NullProgressMonitor()));
			index.acquireReadLock();
			try {
				IIndexFile[] files = index.getFiles(ILinkage.CPP_LINKAGE_ID, indexFileLocation);
				if (files.length > 0 && areAllFilesNotOlderThan(files, fileTimestamp)) {
					Assert.assertTrue(CCorePlugin.getIndexManager().joinIndexer(timeLeft, new NullProgressMonitor()));
					return;
				}
				files = index.getFiles(ILinkage.C_LINKAGE_ID, indexFileLocation);
				if (files.length > 0 && areAllFilesNotOlderThan(files, fileTimestamp)) {
					Assert.assertTrue(CCorePlugin.getIndexManager().joinIndexer(timeLeft, new NullProgressMonitor()));
					return;
				}
			} finally {
				index.releaseReadLock();
			}

			Thread.sleep(50);
			timeLeft = (int) (endTime - System.currentTimeMillis());
		}
		Assert.fail("Indexing of " + file.getFullPath() + " did not complete in " + maxmillis / 1000. + " sec");
	}

	private static boolean areAllFilesNotOlderThan(IIndexFile[] files, long timestamp) throws CoreException {
		for (IIndexFile file : files) {
			if (file.getTimestamp() < timestamp) {
				return false;
			}
		}
		return true;
	}

	public static IASTTranslationUnit createIndexBasedAST(IIndex index, ICProject project, IFile file)
			throws CModelException, CoreException {
		ICElement elem = project.findElement(file.getFullPath());
		if (elem instanceof ITranslationUnit) {
			ITranslationUnit tu = (ITranslationUnit) elem;
			return tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
		}
		Assert.fail("Could not create AST for " + file.getFullPath());
		return null;
	}
}
