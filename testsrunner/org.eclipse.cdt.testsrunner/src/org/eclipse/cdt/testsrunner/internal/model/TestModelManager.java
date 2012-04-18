/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestModelAccessor;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.ITestingSessionListener;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
import org.eclipse.cdt.testsrunner.model.ITestMessage.Level;
import org.eclipse.cdt.testsrunner.model.ITestSuite;

/**
 * Manages the testing model (creates, fill and update it) and notifies the
 * listeners about updates.
 */
public class TestModelManager implements ITestModelUpdater, ITestModelAccessor {

	/**
	 * Name of the root test suite.
	 * 
	 * @note Root test suite is invisible (only its children are visible), so
	 * the name is not important.
	 */
	public static final String ROOT_TEST_SUITE_NAME = "<root>"; //$NON-NLS-1$
	
	/** Stack of the currently entered (and not existed) test suites. */
	private Stack<TestSuite> testSuitesStack = new Stack<TestSuite>();

	/**
	 * Currently running test case. There are no nested test cases, so the
	 * collection is not necessary.
	 */
	private TestCase currentTestCase = null;
	
	/**
	 * The mapping of test suite object to the index on which it was inserted to
	 * the parent.
	 * 
	 * @note Test suites presence in this map means that test suite was visited
	 * during the testing process (not visited test suites are removed when
	 * testing is finished cause they are considered as renamed or removed).
	 * @note Test suite insert position is important for insertion algorithm.
	 */
	private Map<TestItem, Integer> testSuitesIndex = new HashMap<TestItem, Integer>();
	
	/** Listeners collection. */
	private List<ITestingSessionListener> listeners = new ArrayList<ITestingSessionListener>();
	
	/** Flag stores whether test execution time should be measured for the session. */
	private boolean timeMeasurement = false;

	/** Stores the test case start time or 0 there is no currently running test case. */
	private long testCaseStartTime = 0;
	
	/** Instance of the insertion algorithm for test suites. */
	private TestSuiteInserter testSuiteInserter = new TestSuiteInserter();

	/** Instance of the insertion algorithm for test cases. */
	private TestCaseInserter testCaseInserter = new TestCaseInserter();

	
	/**
	 * Builds current tests hierarchy from the other one (copies only necessary
	 * information).
	 */
	private class HierarchyCopier implements IModelVisitor {

		@Override
		public void visit(ITestSuite testSuite) {
			// Do not copy root test suite
			if (testSuite.getParent() != null) {
				enterTestSuite(testSuite.getName());
			}
		}

		@Override
		public void leave(ITestSuite testSuite) {
			// Do not copy root test suite
			if (testSuite.getParent() != null) {
				exitTestSuite();
			}
		}

		@Override
		public void visit(ITestCase testCase) {
			enterTestCase(testCase.getName());
			setTestStatus(TestCase.Status.NotRun);
		}

		@Override
		public void leave(ITestCase testCase) {
			exitTestCase();
		}

		@Override
		public void visit(ITestMessage testMessage) {}
		@Override
		public void leave(ITestMessage testMessage) {}
	}
	

	/**
	 * Utility class: generalization of insertion algorithm for test suites and
	 * test cases.
	 * 
	 * <p>
	 * The algorithm tries to find the place where the new item should be
	 * inserted at. If the item with such name does not exist in the current top
	 * most test suite, it should be inserted at the current position. If it
	 * already exists (at the next or previous position) then it should be moved
	 * from there to the current one.
	 * </p>
	 * 
	 * @param <E> test item type (test suite or test case)
	 */
	private abstract class TestItemInserter<E extends TestItem> {

		/**
		 * Check whether item has the required type (test suite for suites inserter and
		 * test case for cases one).
		 * 
		 * @param item test item to check
		 * @return whether item has the required type
		 */
		protected abstract boolean isRequiredTestItemType(TestItem item);
		
		/**
		 * Creates a new item type with the specified name and parent (test
		 * suite for suites inserter and test case for cases one).
		 * 
		 * @param name name of the new test item
		 * @param parent parent for the new test item
		 * @return new test item
		 */
		protected abstract E createTestItem(String name, TestSuite parent);
		
		/**
		 * Save new test item in the tracking structures (suite in stack, case
		 * in current variable). Additional operations (e.g. listeners
		 * notification about item entering) can be done too.
		 * 
		 * @param item new test item
		 */
		protected abstract void addNewTestItem(E item);

		
		/**
		 * Returns the casted test item if it matches by name and type or
		 * <code>null</code> if it doesn't.
		 * 
		 * @param item test item to check
		 * @param name test item name
		 * @return casted test item or null
		 */
		@SuppressWarnings("unchecked")
		private E checkTestItem(TestItem item, String name) {
			return (isRequiredTestItemType(item) && item.getName().equals(name)) ? (E)item : null;
		}
		
		/**
		 * Returns the last insert index for the specified test suite. Returns 0
		 * if test suite was not inserted yet.
		 * 
		 * @param testSuite test suite to look up
		 * @return insert index or 0
		 */
		private int getLastInsertIndex(TestSuite testSuite) {
			Integer intLastInsertIndex = testSuitesIndex.get(testSuite);
			return intLastInsertIndex != null ? intLastInsertIndex : 0;
		}
		
		/**
		 * Notifies the listeners about children update of the specified test
		 * suite.
		 * 
		 * @param suite updated test suite
		 */
		private void notifyAboutChildrenUpdate(ITestSuite suite) {
			for (ITestingSessionListener listener : getListenersCopy()) {
				listener.childrenUpdate(suite);
			}
		}
		
		/**
		 * Inserts the test item by the name.
		 * 
		 * @param name test item name
		 */
		public void insert(String name) {
			TestSuite currTestSuite = testSuitesStack.peek();
			int lastInsertIndex = getLastInsertIndex(currTestSuite);
			List<TestItem> children = currTestSuite.getChildrenList();
			E newTestItem = null;

			// Optimization: Check whether we already pointing to the test suite with required name
			try {
				newTestItem = checkTestItem(children.get(lastInsertIndex), name);
			} catch (IndexOutOfBoundsException e) {}
			if (newTestItem != null) {
				testSuitesIndex.put(currTestSuite, lastInsertIndex+1);
			}
			
			// Check whether the suite with required name was later in the hierarchy
			if (newTestItem == null) {
				for (int childIndex = lastInsertIndex; childIndex < children.size(); childIndex++) {
					newTestItem = checkTestItem(children.get(childIndex), name);
					if (newTestItem != null) {
						testSuitesIndex.put(currTestSuite, childIndex);
						break;
					}
				}
			}
			
			// Search in previous
			if (newTestItem == null) {
				for (int childIndex = 0; childIndex < lastInsertIndex; childIndex++) {
					newTestItem = checkTestItem(children.get(childIndex), name);
					if (newTestItem != null) {
						children.add(lastInsertIndex, children.remove(childIndex));
						notifyAboutChildrenUpdate(currTestSuite);
						break;
					}
				}
			}
			
			// Add new
			if (newTestItem == null) {
				newTestItem = createTestItem(name, currTestSuite);
				children.add(lastInsertIndex, newTestItem);
				testSuitesIndex.put(currTestSuite, lastInsertIndex+1);
				notifyAboutChildrenUpdate(currTestSuite);
			}
			if (!testSuitesIndex.containsKey(newTestItem)) {
				testSuitesIndex.put(newTestItem, 0);
			}
			addNewTestItem(newTestItem);
		}
		
	}
	

	/**
	 * Utility class: insertion algorithm specialization for test suites.
	 */
	private class TestSuiteInserter extends TestItemInserter<TestSuite> {
		
		@Override
		protected boolean isRequiredTestItemType(TestItem item) {
			return (item instanceof TestSuite);
		}
		
		@Override
		protected TestSuite createTestItem(String name, TestSuite parent) {
			return new TestSuite(name, parent);
		}
		
		@Override
		protected void addNewTestItem(TestSuite testSuite) {
			testSuitesStack.push(testSuite);

			// Notify listeners
			for (ITestingSessionListener listener : getListenersCopy()) {
				listener.enterTestSuite(testSuite);
			}
		}
	}


	/**
	 * Utility class: insertion algorithm specialization for test cases.
	 */
	private class TestCaseInserter extends TestItemInserter<TestCase> {
		
		@Override
		protected boolean isRequiredTestItemType(TestItem item) {
			return (item instanceof TestCase);
		}
		
		@Override
		protected TestCase createTestItem(String name, TestSuite parent) {
			return new TestCase(name, parent);
		}
		
		@Override
		protected void addNewTestItem(TestCase testCase) {
			currentTestCase = testCase;
			testCase.setStatus(ITestItem.Status.Skipped);
			
			// Notify listeners
			for (ITestingSessionListener listener : getListenersCopy()) {
				listener.enterTestCase(testCase);
			}
		}
	}

	
	public TestModelManager(ITestSuite previousTestsHierarchy, boolean timeMeasurement) {
		testSuitesStack.push(new TestSuite(ROOT_TEST_SUITE_NAME, null));
		if (previousTestsHierarchy != null) {
			// Copy tests hierarchy
			this.timeMeasurement = false;
			previousTestsHierarchy.visit(new HierarchyCopier());
		}
		this.timeMeasurement = timeMeasurement;
		this.testSuitesIndex.clear();
	}

	/**
	 * Notifies the listeners that testing was started.
	 */
	public void testingStarted() {
		// Notify listeners
		for (ITestingSessionListener listener : getListenersCopy()) {
			listener.testingStarted();
		}
	}

	/**
	 * Removes not visited test items and notifies the listeners that testing
	 * was finished.
	 */
	public void testingFinished() {
		// Remove all NotRun-tests and not used test suites (probably they were removed from test module)
		getRootSuite().visit(new IModelVisitor() {
			
			@Override
			public void visit(ITestSuite testSuite) {
				List<TestItem> suiteChildren = ((TestSuite)testSuite).getChildrenList();
				for (Iterator<TestItem> it = suiteChildren.iterator(); it.hasNext();) {
					TestItem item = it.next();
					if ((item instanceof ITestSuite && !testSuitesIndex.containsKey(item)) ||
						(item instanceof ITestCase && item.getStatus() == ITestItem.Status.NotRun)) {
						it.remove();
					}
				}
			}

			@Override
			public void visit(ITestMessage testMessage) {}
			@Override
			public void visit(ITestCase testCase) {}
			@Override
			public void leave(ITestSuite testSuite) {}
			@Override
			public void leave(ITestCase testCase) {}
			@Override
			public void leave(ITestMessage testMessage) {}
		});
		testSuitesIndex.clear();
		
		// Notify listeners
		for (ITestingSessionListener listener : getListenersCopy()) {
			listener.testingFinished();
		}
	}
	
	@Override
	public void enterTestSuite(String name) {
		testSuiteInserter.insert(name);
	}

	@Override
	public void exitTestSuite() {
		exitTestCase();
		TestSuite testSuite = testSuitesStack.pop();
		// Notify listeners
		for (ITestingSessionListener listener : getListenersCopy()) {
			listener.exitTestSuite(testSuite);
		}
	}

	@Override
	public void enterTestCase(String name) {
		testCaseInserter.insert(name);
		if (timeMeasurement) {
			testCaseStartTime = System.currentTimeMillis();
		}
	}


	@Override
	public void setTestStatus(Status status) {
		currentTestCase.setStatus(status);
	}

	@Override
	public void setTestingTime(int testingTime) {
		currentTestCase.setTestingTime(testingTime);
	}

	@Override
	public void exitTestCase() {
		if (currentTestCase != null) {
			// Set test execution time (if time measurement is turned on)
			if (timeMeasurement) {
				int testingTime = (int)(System.currentTimeMillis()-testCaseStartTime);
				currentTestCase.setTestingTime(currentTestCase.getTestingTime()+testingTime);
				testCaseStartTime = 0;
			}
			TestCase testCase = currentTestCase;
			currentTestCase = null;
			// Notify listeners
			for (ITestingSessionListener listener : getListenersCopy()) {
				listener.exitTestCase(testCase);
			}
		}
	}

	@Override
	public void addTestMessage(String file, int line, Level level, String text) {
		TestLocation location = (file == null || file.isEmpty() || line <= 0) ? null : new TestLocation(file, line);
		currentTestCase.addTestMessage(new TestMessage(location, level, text));
	}
	
	@Override
	public ITestSuite currentTestSuite() {
		return testSuitesStack.peek();
	}


	@Override
	public ITestCase currentTestCase() {
		return currentTestCase;
	}

	@Override
	public boolean isCurrentlyRunning(ITestItem item) {
		return (item == currentTestCase && item != null) || testSuitesStack.contains(item);
	}
	
	@Override
	public TestSuite getRootSuite() {
		return testSuitesStack.firstElement();
	}

	@Override
	public void addChangesListener(ITestingSessionListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeChangesListener(ITestingSessionListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	/**
	 * Copies listeners before notifying them to avoid dead-locks.
	 * 
	 * @return listeners collection copy
	 */
	private ITestingSessionListener[] getListenersCopy() {
		synchronized (listeners) {
			return listeners.toArray(new ITestingSessionListener[listeners.size()]);
		}		
	}
	
}
