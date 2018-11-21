/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems Ltd and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.errorparsers.tests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

import junit.framework.TestCase;

/**
 * This test is designed to exercise the error parser capabilities.
 */
public abstract class GenericErrorParserTests extends TestCase {
	public static final String GCC_ERROR_PARSER_ID = "org.eclipse.cdt.core.GCCErrorParser";
	public static final String GLD_ERROR_PARSER_ID = "org.eclipse.cdt.core.GLDErrorParser";
	public static final String GMAKE_ERROR_PARSER_ID = "org.eclipse.cdt.core.GmakeErrorParser";

	protected IProject fTempProject;

	/**
	 * Constructor for IndexManagerTest.
	 *
	 * @param name
	 */
	public GenericErrorParserTests(String name) {
		super(name);
	}

	public GenericErrorParserTests() {
		super();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fTempProject = ResourcesPlugin.getWorkspace().getRoot().getProject("temp-" + System.currentTimeMillis());
		if (!fTempProject.exists()) {
			fTempProject.create(new NullProgressMonitor());
		}
	}

	@Override
	protected void tearDown() {
		try {
			super.tearDown();
		} catch (Exception ex) {
		}
		try {
			fTempProject.delete(true, true, new NullProgressMonitor());
		} catch (Exception ex) {
		}
	}

	protected IProject getTempProject() {
		return fTempProject;
	}

	protected String makeStringFromArray(String[] pieces, String sep) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < pieces.length; i++) {
			if (i != 0) {
				buf.append(sep);
			}
			buf.append(pieces[i]);
		}
		return buf.toString();
	}

	protected void transferInputStreamToOutputStream(InputStream input, OutputStream output, int byteBlockSize)
			throws IOException {
		byte[] buffer = new byte[byteBlockSize];
		int bytesRead;

		while ((bytesRead = input.read(buffer)) >= 0) {
			output.write(buffer, 0, bytesRead);
		}

		buffer = null;
	}

	protected void runParserTest(InputStream inputStream, int expectedErrorCount, int expectedWarningCount,
			String[] expectedFileNames, String[] expectedDescriptions, String[] parserID) throws IOException {
		runParserTest(inputStream, expectedErrorCount, expectedWarningCount, 0, expectedFileNames, expectedDescriptions,
				parserID);
	}

	protected void runParserTest(InputStream inputStream, int expectedErrorCount, int expectedWarningCount,
			int expectedInfoCount, String[] expectedFileNames, String[] expectedDescriptions, String[] parserID)
			throws IOException {
		assertNotNull(inputStream);

		CountingMarkerGenerator markerGenerator = new CountingMarkerGenerator();

		IProject project = getTempProject();
		assertNotNull(project);

		ErrorParserManager manager;
		manager = new ImaginaryFilesErrorParserManager(project, markerGenerator, parserID);

		transferInputStreamToOutputStream(inputStream, manager.getOutputStream(), 1024);
		manager.close();
		manager.getOutputStream().close();

		if (expectedErrorCount >= 0) {
			assertEquals(expectedErrorCount, markerGenerator.numErrors);
		}
		if (expectedWarningCount >= 0) {
			assertEquals(expectedWarningCount, markerGenerator.numWarnings);
		}
		if (expectedInfoCount >= 0) {
			assertEquals(expectedInfoCount, markerGenerator.numInfos);
		}
		if (expectedFileNames != null) {
			assertEquals(expectedFileNames.length, markerGenerator.uniqFiles.size());
			for (int i = 0; i < expectedFileNames.length; i++) {
				// Keep in mind that uniqFiles get alphabetically sorted
				IPath path = ((IFile) markerGenerator.uniqFiles.get(i)).getLocation();
				assertEquals(expectedFileNames[i], path.lastSegment());
			}
		}

		if (expectedDescriptions != null) {
			assertNotNull(markerGenerator.descriptions);
			for (int i = 0; i < expectedDescriptions.length; i++) {
				assertEquals(expectedDescriptions[i], markerGenerator.descriptions.get(i));
			}
		}
	}

	protected void runParserTest(String[] dataStream, int expectedErrorCount, int expectedWarningCount,
			String[] expectedFileNames, String[] expectedDescriptions, String[] parserID) throws IOException {
		runParserTest(dataStream, expectedErrorCount, expectedWarningCount, 0, expectedFileNames, expectedDescriptions,
				parserID);
	}

	protected void runParserTest(String[] dataStream, int expectedErrorCount, int expectedWarningCount,
			int expectedInfoCount, String[] expectedFileNames, String[] expectedDescriptions, String[] parserID)
			throws IOException {
		String errorStream = makeStringFromArray(dataStream, "\n");

		ByteArrayInputStream inputStream = new ByteArrayInputStream(errorStream.getBytes());

		runParserTest(inputStream, expectedErrorCount, expectedWarningCount, expectedInfoCount, expectedFileNames,
				expectedDescriptions, parserID);
	}

	private static class FileNameComparator implements Comparator<IResource> {
		@Override
		public int compare(IResource f0, IResource f1) {
			return f0.getName().compareToIgnoreCase(f1.getName());
		}
	}

	/**
	 * Expand and grow this class to make it more useful.
	 */
	class CountingMarkerGenerator implements IMarkerGenerator {
		public int numErrors;
		public int numWarnings;
		public int numInfos;
		public int numMarkers;
		public ArrayList<IResource> uniqFiles;
		public List<String> descriptions;
		private FileNameComparator fFileNameComparator;

		@Override
		public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
			ProblemMarkerInfo problemMarkerInfo = new ProblemMarkerInfo(file, lineNumber, errorDesc, severity, errorVar,
					null);
			addMarker(problemMarkerInfo);
		}

		@Override
		public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
			int index = Collections.binarySearch(uniqFiles, problemMarkerInfo.file, fFileNameComparator);
			if (index < 0) {
				uniqFiles.add(-1 * (index + 1), problemMarkerInfo.file);
			}

			if (problemMarkerInfo.severity == SEVERITY_ERROR_BUILD
					|| problemMarkerInfo.severity == SEVERITY_ERROR_RESOURCE) {
				numErrors++;
			} else if (problemMarkerInfo.severity == SEVERITY_WARNING) {
				numWarnings++;
			} else if (problemMarkerInfo.severity == SEVERITY_INFO) {
				numInfos++;
			}

			descriptions.add(problemMarkerInfo.description);
			numMarkers++;
		}

		public CountingMarkerGenerator() {
			numErrors = 0;
			numWarnings = 0;
			numInfos = 0;
			uniqFiles = new ArrayList(0);
			descriptions = new ArrayList<>(0);
			fFileNameComparator = new FileNameComparator();
		}
	}

	/**
	 * This class allows us to run error parsers for files which don't really
	 * exist by just using the strings that come out as error codes.
	 */
	class ImaginaryFilesErrorParserManager extends ErrorParserManager {
		IProject fProject;

		public ImaginaryFilesErrorParserManager(IProject project, IMarkerGenerator generator, String[] ids) {
			super(project, generator, ids);
			fProject = project;
		}

		@Override
		public IFile findFileName(String fileName) {
			if (fileName == null || fileName.trim().length() == 0) {
				return null;
			}
			if (fileName.lastIndexOf('/') != -1) {
				fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
			}
			IFile file = fProject.getFile(fileName);
			if (file != null && !file.exists()) {
				try {
					InputStream stream = new ByteArrayInputStream("TestFile".getBytes());
					file.create(stream, true, new NullProgressMonitor());
					stream.close();
				} catch (Exception ex) {
					/* Ignore */
				}
			}
			return file;
		}

		@Override
		protected IFile findFileInWorkspace(IPath path) {
			IFile file = fProject.getFile(path.lastSegment());
			if (!file.exists()) {
				try {
					InputStream stream = new ByteArrayInputStream("TestFile".getBytes());
					file.create(stream, true, new NullProgressMonitor());
					stream.close();
				} catch (Exception e) {
					/* Ignore */
				}
			}
			return file;
		}
	}
}
