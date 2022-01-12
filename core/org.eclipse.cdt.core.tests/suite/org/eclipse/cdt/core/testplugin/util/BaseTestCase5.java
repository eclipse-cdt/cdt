/*******************************************************************************
 * Copyright (c) 2006, 2020 Wind River Systems, Inc. and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.core.pdom.CModelListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;

import junit.framework.Test;
import junit.framework.TestResult;

/**
 * BaseTestCase for JUnit5.
 */
public abstract class BaseTestCase5 {
	/**
	 * Bug 499777: Numerous tests are flaky and of little value on gerrit verification builds. This
	 * tag can be applied to JUnit5 tests with the {@link Tag} annotation to skip flaky tests in
	 * such circumstances.
	 */
	public static final String FLAKY_TEST_TAG = "flakyTest";

	/**
	 * Bug 499777: Numerous tests are very slow and of little value on gerrit verification builds. This
	 * tag can be applied to JUnit5 tests with the {@link Tag} annotation to skip slow tests in
	 * such circumstances.
	 */
	public static final String SLOW_TEST_TAG = "slowTest";

	protected static final String DEFAULT_INDEXER_TIMEOUT_SEC = "10";
	protected static final String INDEXER_TIMEOUT_PROPERTY = "indexer.timeout";
	/**
	 * Indexer timeout used by tests. To avoid this timeout expiring during debugging add
	 * -Dindexer.timeout=some_large_number to VM arguments of the test launch configuration.
	 */
	protected static final int INDEXER_TIMEOUT_SEC = Integer
			.parseInt(System.getProperty(INDEXER_TIMEOUT_PROPERTY, DEFAULT_INDEXER_TIMEOUT_SEC));
	protected static final int INDEXER_TIMEOUT_MILLISEC = INDEXER_TIMEOUT_SEC * 1000;

	/**
	 * The GCC version to emulate when running tests.
	 * We emulate the latest version whose extensions we support.
	 */
	protected static final int GCC_MAJOR_VERSION_FOR_TESTS = 10;
	protected static final int GCC_MINOR_VERSION_FOR_TESTS = 1;

	/**
	 * This provides the systems new line separator. Use this if you do String comparisons in tests
	 * instead of hard coding '\n' or '\r\n' respectively.
	 */
	protected static final String NL = System.getProperty("line.separator");

	private boolean fExpectFailure;
	private int fBugNumber;
	private int fExpectedLoggedNonOK;
	private Deque<File> filesToDeleteOnTearDown = new ArrayDeque<>();
	private TestInfo testInfo;

	LogMonitoring logMonitoring = new LogMonitoring();

	/**
	 * Backwards support for JUnit3 style test that had a getName. This is not 100% the same, but close
	 * enough for the general use case of getName.
	 */
	public String getName() {
		return testInfo.getDisplayName();
	}

	public static NullProgressMonitor npm() {
		return new NullProgressMonitor();
	}

	@BeforeEach
	protected void setupBase(TestInfo testInfo) throws Exception {
		this.testInfo = testInfo;

		logMonitoring.start();

		CPPASTNameBase.sAllowRecursionBindings = false;
		CPPASTNameBase.sAllowNameComputation = false;
		CModelListener.sSuppressUpdateOfLastRecentlyUsed = true;
	}

	@AfterEach
	protected void tearDownBase() throws Exception {
		for (File file; (file = filesToDeleteOnTearDown.pollLast()) != null;) {
			file.delete();
		}
		ResourceHelper.cleanUp(getName());
		TestScannerProvider.clear();

		logMonitoring.stop(fExpectedLoggedNonOK);
	}

	protected void deleteOnTearDown(File file) {
		filesToDeleteOnTearDown.add(file);
	}

	protected File createTempFile(String prefix, String suffix) throws IOException {
		File file = File.createTempFile(prefix, suffix);
		filesToDeleteOnTearDown.add(file);
		return file;
	}

	protected File nonExistentTempFile(String prefix, String suffix) {
		File file = new File(System.getProperty("java.io.tmpdir"), prefix + System.currentTimeMillis() + suffix);
		filesToDeleteOnTearDown.add(file);
		return file;
	}

	/**
	 * The last value passed to this method in the body of a testXXX method
	 * will be used to determine whether or not the presence of non-OK status objects
	 * in the log should fail the test. If the logged number of non-OK status objects
	 * differs from the last value passed, the test is failed. If this method is not called
	 * at all, the expected number defaults to zero.
	 * @param count the expected number of logged error and warning messages
	 */
	public void setExpectedNumberOfLoggedNonOKStatusObjects(int count) {
		fExpectedLoggedNonOK = count;
	}

	public static void waitForIndexer(ICProject project) throws InterruptedException {
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);
		assertTrue(CCoreInternals.getPDOMManager().joinIndexer(INDEXER_TIMEOUT_SEC * 1000, npm()));
	}

	public static void waitUntilFileIsIndexed(IIndex index, IFile file) throws Exception {
		TestSourceReader.waitUntilFileIsIndexed(index, file, INDEXER_TIMEOUT_SEC * 1000);
	}

	// Assertion helpers

	/**
	 * Asserts that the file does exist and prints a niceish error message if not.
	 */
	public static void assertExists(File f) {
		assertTrue(f.exists(), "File " + f + " does not exist");
	}

	/**
	 * Asserts that the resource does exist and prints a niceish error message if not.
	 */
	public static void assertExists(IResource f) {
		assertTrue(f.exists(), "Resource " + f + " does not exist");
	}

	/**
	 * Asserts that the file does exist and prints a niceish error message if not.
	 */
	public static void assertNotExists(File f) {
		assertFalse(f.exists(), "File " + f + " should not exist");
	}

	/**
	 * Asserts that the Resource does exist and prints a niceish error message if not.
	 */
	public static void assertNotExists(IResource f) {
		assertFalse(f.exists(), "Resource " + f + " should not exist");
	}

	public static <T> T assertInstance(Object o, Class<T> clazz, Class... cs) {
		assertNotNull(o, "Expected object of " + clazz.getName() + " but got a null value");
		assertTrue(clazz.isInstance(o), "Expected " + clazz.getName() + " but got " + o.getClass().getName());
		for (Class c : cs) {
			assertNotNull(o, "Expected object of " + c.getName() + " but got a null value");
			assertTrue(c.isInstance(o), "Expected " + c.getName() + " but got " + o.getClass().getName());
		}
		return clazz.cast(o);
	}

	public static void assertValue(IValue value, long expectedValue) {
		assertNotNull(value);
		assertTrue(value.numberValue() instanceof Long);
		assertEquals(expectedValue, value.numberValue().longValue());
	}

	public static void assertVariableValue(IVariable var, long expectedValue) {
		assertValue(var.getInitialValue(), expectedValue);
	}

	public static String formatForPrinting(IASTName name) {
		String signature = name.getRawSignature();
		boolean saved = CPPASTNameBase.sAllowNameComputation;
		CPPASTNameBase.sAllowNameComputation = true;
		try {
			String nameStr = name.toString();
			if (signature.replace(" ", "").equals(nameStr.replace(" ", "")))
				return signature;
			return nameStr + " in " + signature;
		} catch (Throwable e) {
			return signature;
		} finally {
			CPPASTNameBase.sAllowNameComputation = saved;
		}
	}

	// These methods help migrate from JUnit3 to JUnit5 version by providing errors as early as possible
	// in the migration cycle

	public BaseTestCase5() {
		// This constructor is expected to be called
	}

	/**
	 * This JUnit3 style constructor is not supported.
	 */
	private BaseTestCase5(String name) {
		fail("Test not migrated properly to JUnit5 yet.");
	}

	/**
	 * This method is declared as final to help transition to JUnit5 to ensure that
	 * accidental override of the method is not left in subclasses when migrating.
	 */
	final protected void setUp() {
		fail("Test not migrated properly to JUnit5 yet.");
	}

	/**
	 * This method is declared as final to help transition to JUnit5 to ensure that
	 * accidental override of the method is not left in subclasses when migrating.
	 */
	final protected static Test suite() {
		fail("Test not migrated properly to JUnit5 yet.");
		return null; // unreachable
	}

	/**
	 * This method is declared as final to help transition to JUnit5 to ensure that
	 * accidental override of the method is not left in subclasses when migrating.
	 */
	final protected void tearDown() {
		fail("Test not migrated properly to JUnit5 yet.");
	}

	/**
	 * This method is declared as final to help transition to JUnit5 to ensure that
	 * accidental override of the method is not left in subclasses when migrating.
	 */
	final protected void runBare() {
		fail("Test not migrated properly to JUnit5 yet.");
	}

	/**
	 * This method is declared as final to help transition to JUnit5 to ensure that
	 * accidental override of the method is not left in subclasses when migrating.
	 */
	final protected void run(TestResult result) {
		fail("Test not migrated properly to JUnit5 yet.");
	}

	/**
	* This method is declared as final to help transition to JUnit5 to ensure that
	* accidental override of the method is not left in subclasses when migrating.
	*/
	final protected void runTest() throws Throwable {
		fail("Test not migrated properly to JUnit5 yet.");
	}

	/**
	 * Setting expected failures in this way is not support with the BaseTestCase5. If this
	 * is functionality that is needed, please find a new way to do it.
	 */
	public void setExpectFailure(int bugNumber) {
		fail("Test not migrated properly to JUnit5 yet.");
	}

}
