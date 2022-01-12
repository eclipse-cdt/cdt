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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.core.pdom.CModelListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * @deprecated Please migrate tests away from JUnit3 style to JUnit5 style by using {@link BaseTestCase5}
 * as base class.
 */
@Deprecated
public abstract class BaseTestCase extends TestCase {
	private static final String DEFAULT_INDEXER_TIMEOUT_SEC = BaseTestCase5.DEFAULT_INDEXER_TIMEOUT_SEC;
	private static final String INDEXER_TIMEOUT_PROPERTY = BaseTestCase5.INDEXER_TIMEOUT_PROPERTY;
	/**
	 * Indexer timeout used by tests. To avoid this timeout expiring during debugging add
	 * -Dindexer.timeout=some_large_number to VM arguments of the test launch configuration.
	 */
	protected static final int INDEXER_TIMEOUT_SEC = BaseTestCase5.INDEXER_TIMEOUT_SEC;
	protected static final int INDEXER_TIMEOUT_MILLISEC = BaseTestCase5.INDEXER_TIMEOUT_MILLISEC;

	/**
	 * The GCC version to emulate when running tests.
	 * We emulate the latest version whose extensions we support.
	 */
	protected static final int GCC_MAJOR_VERSION_FOR_TESTS = BaseTestCase5.GCC_MAJOR_VERSION_FOR_TESTS;
	protected static final int GCC_MINOR_VERSION_FOR_TESTS = BaseTestCase5.GCC_MINOR_VERSION_FOR_TESTS;

	/**
	 * This provides the systems new line separator. Use this if you do String comparisons in tests
	 * instead of hard coding '\n' or '\r\n' respectively.
	 */
	protected static final String NL = BaseTestCase5.NL;

	private boolean fExpectFailure;
	private int fBugNumber;
	private int fExpectedLoggedNonOK;
	private Deque<File> filesToDeleteOnTearDown = new ArrayDeque<>();

	public BaseTestCase() {
		super();
	}

	public BaseTestCase(String name) {
		super(name);
	}

	public static NullProgressMonitor npm() {
		return new NullProgressMonitor();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		CPPASTNameBase.sAllowRecursionBindings = false;
		CPPASTNameBase.sAllowNameComputation = false;
		CModelListener.sSuppressUpdateOfLastRecentlyUsed = true;
	}

	@Override
	protected void tearDown() throws Exception {
		for (File file; (file = filesToDeleteOnTearDown.pollLast()) != null;) {
			file.delete();
		}
		ResourceHelper.cleanUp(getName());
		TestScannerProvider.clear();
		super.tearDown();
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

	protected static TestSuite suite(Class clazz) {
		return suite(clazz, null);
	}

	protected static TestSuite suite(Class clazz, String failingTestPrefix) {
		TestSuite suite = new TestSuite(clazz);
		Test failing = getFailingTests(clazz, failingTestPrefix);
		if (failing != null) {
			suite.addTest(failing);
		}
		return suite;
	}

	private static Test getFailingTests(Class clazz, String prefix) {
		TestSuite suite = new TestSuite("Failing Tests");
		HashSet names = new HashSet();
		Class superClass = clazz;
		while (Test.class.isAssignableFrom(superClass) && !TestCase.class.equals(superClass)) {
			Method[] methods = superClass.getDeclaredMethods();
			for (Method method : methods) {
				addFailingMethod(suite, method, names, clazz, prefix);
			}
			superClass = superClass.getSuperclass();
		}
		if (suite.countTestCases() == 0) {
			return null;
		}
		return suite;
	}

	private static void addFailingMethod(TestSuite suite, Method m, Set names, Class clazz, String prefix) {
		String name = m.getName();
		if (!names.add(name)) {
			return;
		}
		if (name.startsWith("test") || (prefix != null && !name.startsWith(prefix))) {
			return;
		}
		if (name.equals("tearDown") || name.equals("setUp") || name.equals("runBare")) {
			return;
		}
		if (Modifier.isPublic(m.getModifiers())) {
			Class[] parameters = m.getParameterTypes();
			Class returnType = m.getReturnType();
			if (parameters.length == 0 && returnType.equals(Void.TYPE)) {
				Test test = TestSuite.createTest(clazz, name);
				((BaseTestCase) test).setExpectFailure(0);
				suite.addTest(test);
			}
		}
	}

	@Override
	public void runBare() throws Throwable {
		LogMonitoring monitoring = new LogMonitoring();
		monitoring.start();
		try {
			super.runBare();
		} finally {
			monitoring.stop(fExpectedLoggedNonOK);
		}
	}

	@Override
	public void run(TestResult result) {
		if (!fExpectFailure || Boolean.parseBoolean(System.getProperty("SHOW_EXPECTED_FAILURES"))) {
			super.run(result);
			return;
		}

		result.startTest(this);

		TestResult r = new TestResult();
		super.run(r);
		if (r.failureCount() == 1) {
			TestFailure failure = r.failures().nextElement();
			String msg = failure.exceptionMessage();
			if (msg != null && msg.startsWith("Method \"" + getName() + "\"")) {
				result.addFailure(this, new AssertionFailedError(msg));
			}
		} else if (r.errorCount() == 0 && r.failureCount() == 0) {
			String err = "Unexpected success of " + getName();
			if (fBugNumber > 0) {
				err += ", bug #" + fBugNumber;
			}
			result.addFailure(this, new AssertionFailedError(err));
		}

		result.endTest(this);
	}

	public void setExpectFailure(int bugNumber) {
		fExpectFailure = true;
		fBugNumber = bugNumber;
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
		BaseTestCase5.waitForIndexer(project);
	}

	public static void waitUntilFileIsIndexed(IIndex index, IFile file) throws Exception {
		BaseTestCase5.waitUntilFileIsIndexed(index, file);
	}

	// Assertion helpers (redirected to the common implementation)

	protected static <T> T assertInstance(Object o, Class<T> clazz, Class... cs) {
		return BaseTestCase5.assertInstance(o, clazz, cs);
	}

	protected static void assertValue(IValue value, long expectedValue) {
		BaseTestCase5.assertValue(value, expectedValue);
	}

	protected static void assertVariableValue(IVariable var, long expectedValue) {
		BaseTestCase5.assertVariableValue(var, expectedValue);
	}

	protected static String formatForPrinting(IASTName name) {
		return BaseTestCase5.formatForPrinting(name);
	}
}
