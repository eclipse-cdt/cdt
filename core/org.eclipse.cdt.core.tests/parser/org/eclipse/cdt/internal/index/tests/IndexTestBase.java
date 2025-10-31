/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.jupiter.api.Assertions;

public abstract class IndexTestBase extends BaseTestCase5 {

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

	protected ICProject createProject(final boolean useCpp, final String importSource)
			throws CoreException, InterruptedException {
		// Create the project
		final ICProject[] result = new ICProject[] { null };
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				String name = "IndexTest_" + getName() + "_" + System.currentTimeMillis();
				if (useCpp) {
					result[0] = CProjectHelper.createCCProject(name, null, IPDOMManager.ID_NO_INDEXER);
				} else {
					result[0] = CProjectHelper.createCProject(name, null, IPDOMManager.ID_NO_INDEXER);
				}
				CProjectHelper.importSourcesFromPlugin(result[0], CTestPlugin.getDefault().getBundle(), importSource);
			}
		}, null);
		CCorePlugin.getIndexManager().setIndexerId(result[0], IPDOMManager.ID_FAST_INDEXER);
		// wait until the indexer is done
		waitForIndexer(result[0]);
		return result[0];
	}

	protected String readTaggedComment(String tag) throws Exception {
		return TestSourceReader.readTaggedComment(CTestPlugin.getDefault().getBundle(), "parser", getClass(), tag);
	}

	protected StringBuilder[] getContentsForTest(int blocks) throws IOException {
		return TestSourceReader.getContentsForTest(CTestPlugin.getDefault().getBundle(), "parser", getClass(),
				getName(), blocks);
	}
}
