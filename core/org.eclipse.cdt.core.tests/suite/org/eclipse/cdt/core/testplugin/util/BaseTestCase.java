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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.core.pdom.CModelListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class BaseTestCase extends TestCase {
	private static final String DEFAULT_INDEXER_TIMEOUT_SEC = "10";
	private static final String INDEXER_TIMEOUT_PROPERTY = "indexer.timeout";
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
	protected static final int GCC_MAJOR_VERSION_FOR_TESTS = 8;
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
		final List<IStatus> statusLog = Collections.synchronizedList(new ArrayList());
		ILogListener logListener = new ILogListener() {
			@Override
			public void logging(IStatus status, String plugin) {
				if (!status.isOK() && status.getSeverity() != IStatus.INFO) {
					switch (status.getCode()) {
					case IResourceStatus.NOT_FOUND_LOCAL:
					case IResourceStatus.NO_LOCATION_LOCAL:
					case IResourceStatus.FAILED_READ_LOCAL:
					case IResourceStatus.RESOURCE_NOT_LOCAL:
						// Logged by the resources plugin.
						return;
					}
					statusLog.add(status);
				}
			}
		};
		final CCorePlugin corePlugin = CCorePlugin.getDefault();
		if (corePlugin != null) { // Iff we don't run as a JUnit Plugin Test.
			corePlugin.getLog().addLogListener(logListener);
		}

		Throwable testThrowable = null;
		try {
			try {
				super.runBare();
			} catch (Throwable e) {
				testThrowable = e;
			}

			if (statusLog.size() != fExpectedLoggedNonOK) {
				StringBuilder msg = new StringBuilder("Expected number (").append(fExpectedLoggedNonOK).append(") of ");
				msg.append("Non-OK status objects in log differs from actual (").append(statusLog.size())
						.append(").\n");
				Throwable cause = null;
				if (!statusLog.isEmpty()) {
					synchronized (statusLog) {
						for (IStatus status : statusLog) {
							IStatus[] ss = { status };
							ss = status instanceof MultiStatus ? ((MultiStatus) status).getChildren() : ss;
							for (IStatus s : ss) {
								msg.append('\t').append(s.getMessage()).append(' ');

								Throwable t = s.getException();
								cause = cause != null ? cause : t;
								if (t != null) {
									msg.append(
											t.getMessage() != null ? t.getMessage() : t.getClass().getCanonicalName());
								}

								msg.append("\n");
							}
						}
					}
				}
				cause = cause != null ? cause : testThrowable;
				AssertionFailedError afe = new AssertionFailedError(msg.toString());
				afe.initCause(cause);
				throw afe;
			}
		} finally {
			if (corePlugin != null) {
				corePlugin.getLog().removeLogListener(logListener);
			}
		}

		if (testThrowable != null)
			throw testThrowable;
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

	/**
	 * Some test steps need synchronizing against a CModel event. This class
	 * is a very basic means of doing that.
	 */
	static protected class ModelJoiner implements IElementChangedListener {
		private final boolean[] changed = new boolean[1];

		public ModelJoiner() {
			CoreModel.getDefault().addElementChangedListener(this);
		}

		public void clear() {
			synchronized (changed) {
				changed[0] = false;
				changed.notifyAll();
			}
		}

		public void join() throws CoreException {
			try {
				synchronized (changed) {
					while (!changed[0]) {
						changed.wait();
					}
				}
			} catch (InterruptedException e) {
				throw new CoreException(CCorePlugin.createStatus("Interrupted", e));
			}
		}

		public void dispose() {
			CoreModel.getDefault().removeElementChangedListener(this);
		}

		@Override
		public void elementChanged(ElementChangedEvent event) {
			// Only respond to post change events
			if (event.getType() != ElementChangedEvent.POST_CHANGE)
				return;

			synchronized (changed) {
				changed[0] = true;
				changed.notifyAll();
			}
		}
	}

	public static void waitForIndexer(ICProject project) throws InterruptedException {
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);
		assertTrue(CCoreInternals.getPDOMManager().joinIndexer(INDEXER_TIMEOUT_SEC * 1000, npm()));
	}

	public static void waitUntilFileIsIndexed(IIndex index, IFile file) throws Exception {
		TestSourceReader.waitUntilFileIsIndexed(index, file, INDEXER_TIMEOUT_SEC * 1000);
	}

	// Assertion helpers

	protected static <T> T assertInstance(Object o, Class<T> clazz, Class... cs) {
		assertNotNull("Expected object of " + clazz.getName() + " but got a null value", o);
		assertTrue("Expected " + clazz.getName() + " but got " + o.getClass().getName(), clazz.isInstance(o));
		for (Class c : cs) {
			assertNotNull("Expected object of " + c.getName() + " but got a null value", o);
			assertTrue("Expected " + c.getName() + " but got " + o.getClass().getName(), c.isInstance(o));
		}
		return clazz.cast(o);
	}

	protected static void assertValue(IValue value, long expectedValue) {
		assertNotNull(value);
		assertTrue(value.numberValue() instanceof Long);
		assertEquals(expectedValue, value.numberValue().longValue());
	}

	protected static void assertVariableValue(IVariable var, long expectedValue) {
		assertValue(var.getInitialValue(), expectedValue);
	}

	protected static String formatForPrinting(IASTName name) {
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
}
