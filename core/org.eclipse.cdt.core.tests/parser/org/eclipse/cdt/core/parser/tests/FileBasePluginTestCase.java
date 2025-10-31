/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
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
package org.eclipse.cdt.core.parser.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author dsteffle
 */
public abstract class FileBasePluginTestCase {

	public static void assertNotEquals(Object expected, Object actual) {
		Assertions.assertNotEquals(expected, actual);
	}

	public static void assertNotEquals(String msg, Object expected, Object actual) {
		Assertions.assertNotEquals(expected, actual, msg);
	}

	public static void assertNotEquals(String msg, long expected, long actual) {
		Assertions.assertNotEquals(expected, actual, msg);
	}

	public static void assertNotEquals(long expected, long actual) {
		Assertions.assertNotEquals(expected, actual);
	}

	public static void assertEquals(Object expected, Object actual) {
		Assertions.assertEquals(expected, actual);
	}

	public static void assertEquals(String msg, Object expected, Object actual) {
		Assertions.assertEquals(expected, actual, msg);
	}

	public static void assertEquals(long expected, long actual) {
		Assertions.assertEquals(expected, actual);
	}

	public static void assertEquals(String msg, long expected, long actual) {
		Assertions.assertEquals(expected, actual, msg);
	}

	public static void assertEquals(double a, double b, double c) {
		Assertions.assertEquals(a, b, c);
	}

	public static void assertEquals(String msg, double a, double b, double c) {
		Assertions.assertEquals(a, b, c, msg);
	}

	public static void assertEquals(float a, float b, float c) {
		Assertions.assertEquals(a, b, c);
	}

	public static void assertEquals(String msg, float a, float b, float c) {
		Assertions.assertEquals(a, b, c, msg);
	}

	public static void assertSame(Object expected, Object actual) {
		Assertions.assertSame(expected, actual);
	}

	public static void assertSame(String msg, Object expected, Object actual) {
		Assertions.assertSame(expected, actual, msg);
	}

	public static void assertNotSame(Object expected, Object actual) {
		Assertions.assertNotSame(expected, actual);
	}

	public static void assertNotSame(String msg, Object expected, Object actual) {
		Assertions.assertNotSame(expected, actual, msg);
	}

	public static void assertNull(Object object) {
		Assertions.assertNull(object);
	}

	public static void assertNull(String msg, Object object) {
		Assertions.assertNull(object, msg);
	}

	public static void assertNotNull(Object object) {
		Assertions.assertNotNull(object);
	}

	public static void assertNotNull(String msg, Object object) {
		Assertions.assertNotNull(object, msg);
	}

	public static void assertTrue(boolean n) {
		Assertions.assertTrue(n);
	}

	public static void assertTrue(String msg, boolean n) {
		Assertions.assertTrue(n, msg);
	}

	public static void assertFalse(boolean n) {
		Assertions.assertFalse(n);
	}

	public static void assertFalse(String msg, boolean n) {
		Assertions.assertFalse(n, msg);
	}

	public static void fail() {
		Assertions.fail();
	}

	public static void fail(String msg) {
		Assertions.fail(msg);
	}

	public static void assertArrayEquals() {
		fail("TODO");
	}

	static NullProgressMonitor monitor;
	static IWorkspace workspace;
	static IProject project;
	static int numProjects;
	static Class className;
	static ICProject cPrj;
	private Class className2;

	@BeforeEach
	protected void setUp() throws Exception {
		monitor = new NullProgressMonitor();

		workspace = ResourcesPlugin.getWorkspace();

		cPrj = CProjectHelper.createCCProject("ParserTestProject", "bin", IPDOMManager.ID_NO_INDEXER); //$NON-NLS-1$ //$NON-NLS-2$
		project = cPrj.getProject();
		assertNotNull(project);
	}

	@AfterEach
	protected void tearDown() throws Exception {
		if (project == null || !project.exists())
			return;

		project.delete(true, false, monitor);
		BaseTestCase5.assertWorkspaceIsEmpty();
	}

	protected IFolder importFolder(String folderName) throws Exception {
		IFolder folder = project.getProject().getFolder(folderName);

		// Create file input stream
		if (!folder.exists())
			folder.create(false, false, monitor);

		return folder;
	}

	public IFile importFile(String fileName, String contents) throws Exception {
		// Obtain file handle
		IFile file = project.getProject().getFile(fileName);

		InputStream stream = new ByteArrayInputStream(contents.getBytes());
		// Create file input stream
		if (file.exists()) {
			file.setContents(stream, false, false, monitor);
		} else {
			file.create(stream, false, monitor);
		}

		return file;
	}
}
