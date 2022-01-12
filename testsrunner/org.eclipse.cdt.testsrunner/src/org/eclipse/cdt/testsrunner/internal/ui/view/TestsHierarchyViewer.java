/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.view;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.ui.viewsupport.ColoringLabelProvider;
import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.ui.view.actions.CopySelectedTestsAction;
import org.eclipse.cdt.testsrunner.internal.ui.view.actions.RedebugSelectedAction;
import org.eclipse.cdt.testsrunner.internal.ui.view.actions.RelaunchSelectedAction;
import org.eclipse.cdt.testsrunner.internal.ui.view.actions.RerunSelectedAction;
import org.eclipse.cdt.testsrunner.internal.ui.view.actions.TestsHierarchyCollapseAllAction;
import org.eclipse.cdt.testsrunner.internal.ui.view.actions.TestsHierarchyExpandAllAction;
import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Shows the tests hierarchy in a flat or hierarchical view.
 */
public class TestsHierarchyViewer {

	/**
	 * The content provider for the tests hierarchy viewer.
	 */
	private class TestTreeContentProvider implements ITreeContentProvider {

		/**
		 * Utility class: recursively collects all the test cases of the
		 * specified test item.
		 *
		 * It is used for flat view of tests hierarchy.
		 */
		private class TestCasesCollector implements IModelVisitor {

			public List<ITestCase> testCases = new ArrayList<>();

			@Override
			public void visit(ITestCase testCase) {
				testCases.add(testCase);
			}

			@Override
			public void visit(ITestMessage testMessage) {
			}

			@Override
			public void visit(ITestSuite testSuite) {
			}

			@Override
			public void leave(ITestSuite testSuite) {
			}

			@Override
			public void leave(ITestCase testCase) {
			}

			@Override
			public void leave(ITestMessage testMessage) {
			}
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return ((ITestItem) parentElement).getChildren();
		}

		@Override
		public Object[] getElements(Object rootTestSuite) {
			if (showTestsHierarchy) {
				return getChildren(rootTestSuite);
			} else {
				TestCasesCollector testCasesCollector = new TestCasesCollector();
				((ITestItem) rootTestSuite).visit(testCasesCollector);
				return testCasesCollector.testCases.toArray();
			}
		}

		@Override
		public Object getParent(Object object) {
			return ((ITestItem) object).getParent();
		}

		@Override
		public boolean hasChildren(Object object) {
			return ((ITestItem) object).hasChildren();
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}
	}

	/**
	 * The label provider for the tests hierarchy viewer.
	 */
	private class TestLabelProvider extends LabelProvider implements IStyledLabelProvider {

		/** Images for the test cases with the different statuses. */
		private Map<ITestItem.Status, Image> testCaseImages = new HashMap<>();
		{
			testCaseImages.put(ITestItem.Status.NotRun, TestsRunnerPlugin.createAutoImage("obj16/test_notrun.gif")); //$NON-NLS-1$
			testCaseImages.put(ITestItem.Status.Skipped, TestsRunnerPlugin.createAutoImage("obj16/test_skipped.gif")); //$NON-NLS-1$
			testCaseImages.put(ITestItem.Status.Passed, TestsRunnerPlugin.createAutoImage("obj16/test_passed.gif")); //$NON-NLS-1$
			testCaseImages.put(ITestItem.Status.Failed, TestsRunnerPlugin.createAutoImage("obj16/test_failed.gif")); //$NON-NLS-1$
			testCaseImages.put(ITestItem.Status.Aborted, TestsRunnerPlugin.createAutoImage("obj16/test_aborted.gif")); //$NON-NLS-1$
		}

		/** Running test case image (overrides the test case status image). */
		private Image testCaseRunImage = TestsRunnerPlugin.createAutoImage("obj16/test_run.gif"); //$NON-NLS-1$

		/** Images for the test suites with the different statuses. */
		private Map<ITestItem.Status, Image> testSuiteImages = new HashMap<>();
		{
			// NOTE: There is no skipped-icon for test suite, but it seems it is not a problem
			testSuiteImages.put(ITestItem.Status.NotRun, TestsRunnerPlugin.createAutoImage("obj16/tsuite_notrun.gif")); //$NON-NLS-1$
			testSuiteImages.put(ITestItem.Status.Skipped, TestsRunnerPlugin.createAutoImage("obj16/tsuite_notrun.gif")); //$NON-NLS-1$
			testSuiteImages.put(ITestItem.Status.Passed, TestsRunnerPlugin.createAutoImage("obj16/tsuite_passed.gif")); //$NON-NLS-1$
			testSuiteImages.put(ITestItem.Status.Failed, TestsRunnerPlugin.createAutoImage("obj16/tsuite_failed.gif")); //$NON-NLS-1$
			testSuiteImages.put(ITestItem.Status.Aborted,
					TestsRunnerPlugin.createAutoImage("obj16/tsuite_aborted.gif")); //$NON-NLS-1$
		}

		/** Running test suite image (overrides the test suite status image). */
		private Image testSuiteRunImage = TestsRunnerPlugin.createAutoImage("obj16/tsuite_run.gif"); //$NON-NLS-1$

		/** Small optimization: the last test item cache */
		private ITestItem lastTestItemCache = null;

		/** Small optimization: test path for the last test item is cache */
		private String lastTestItemPathCache = null;

		@Override
		public Image getImage(Object element) {
			Map<ITestItem.Status, Image> imagesMap = null;
			Image runImage = null;
			if (element instanceof ITestCase) {
				imagesMap = testCaseImages;
				runImage = testCaseRunImage;

			} else if (element instanceof ITestSuite) {
				imagesMap = testSuiteImages;
				runImage = testSuiteRunImage;
			}
			if (imagesMap != null) {
				ITestItem testItem = (ITestItem) element;
				if (testingSession.getModelAccessor().isCurrentlyRunning(testItem)) {
					return runImage;
				}
				return imagesMap.get(testItem.getStatus());
			}

			return null;
		}

		@Override
		public String getText(Object element) {
			ITestItem testItem = (ITestItem) element;
			StringBuilder sb = new StringBuilder();
			sb.append(testItem.getName());
			if (!showTestsHierarchy) {
				appendTestItemPath(sb, testItem);
			}
			if (showTime) {
				sb.append(getTestingTimeString(element));
			}
			return sb.toString();
		}

		@Override
		public StyledString getStyledText(Object element) {
			ITestItem testItem = (ITestItem) element;
			StringBuilder labelBuf = new StringBuilder();
			labelBuf.append(testItem.getName());
			StyledString name = new StyledString(labelBuf.toString());
			if (!showTestsHierarchy) {
				appendTestItemPath(labelBuf, testItem);
				name = StyledCellLabelProvider.styleDecoratedString(labelBuf.toString(), StyledString.QUALIFIER_STYLER,
						name);
			}
			if (showTime) {
				String time = getTestingTimeString(element);
				labelBuf.append(time);
				name = StyledCellLabelProvider.styleDecoratedString(labelBuf.toString(), StyledString.COUNTER_STYLER,
						name);
			}
			return name;
		}

		/**
		 * Appends path to the parent of the specified test item. Also
		 * implements caching of the last path (cause the test item parent is
		 * often the same).
		 *
		 * @param sb string builder to append test item path to
		 * @param testItem specified test item
		 */
		private void appendTestItemPath(StringBuilder sb, ITestItem testItem) {
			ITestSuite testItemParent = testItem.getParent();
			if (lastTestItemCache != testItemParent) {
				lastTestItemCache = testItemParent;
				lastTestItemPathCache = TestPathUtils.getTestItemPath(lastTestItemCache);
			}
			sb.append(MessageFormat.format(UIViewMessages.TestsHierarchyViewer_test_path_format,
					new Object[] { lastTestItemPathCache }));
		}

		/**
		 * Returns the execution time suffix for the test item.
		 *
		 * @param element test item
		 * @return execution time suffix
		 */
		private String getTestingTimeString(Object element) {
			return (element instanceof ITestItem)
					? MessageFormat.format(UIViewMessages.TestsHierarchyViewer_test_time_format,
							Double.toString(((ITestItem) element).getTestingTime() / 1000.0))
					: ""; //$NON-NLS-1$
		}

	}

	/**
	 * Filters passed test cases and test suites.
	 */
	private class FailedOnlyFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return ((ITestItem) element).getStatus().isError();
		}
	}

	/** Testing session to show hierarchy of. */
	private ITestingSession testingSession;

	/** Main widget. */
	private TreeViewer treeViewer;

	/** Specifies whether test items execution time should be shown in hierarchy. */
	private boolean showTime = true;

	/** Specifies whether tests hierarchy should be shown in flat or hierarchical view. */
	private boolean showTestsHierarchy = true;

	/** Failed only tree filter instance. Created on first demand. */
	private FailedOnlyFilter failedOnlyFilter = null;

	/** System clipboard access to provide copy operations. */
	private Clipboard clipboard;

	// Context menu actions
	private Action expandAllAction;
	private Action collapseAllAction;
	private Action copyAction;
	private RelaunchSelectedAction rerunAction;
	private RelaunchSelectedAction redebugAction;

	public TestsHierarchyViewer(Composite parent, IViewSite viewSite, Clipboard clipboard) {
		this.clipboard = clipboard;
		treeViewer = new TreeViewer(parent, SWT.V_SCROLL | SWT.MULTI);
		treeViewer.setContentProvider(new TestTreeContentProvider());
		treeViewer.setLabelProvider(new ColoringLabelProvider(new TestLabelProvider()));
		initContextMenu(viewSite);
	}

	/**
	 * Initializes the viewer context menu.
	 *
	 * @param viewSite view
	 */
	private void initContextMenu(IViewSite viewSite) {
		expandAllAction = new TestsHierarchyExpandAllAction(treeViewer);
		collapseAllAction = new TestsHierarchyCollapseAllAction(treeViewer);
		copyAction = new CopySelectedTestsAction(treeViewer, clipboard);
		rerunAction = new RerunSelectedAction(testingSession, treeViewer);
		redebugAction = new RedebugSelectedAction(testingSession, treeViewer);

		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				handleMenuAboutToShow(manager);
			}
		});
		viewSite.registerContextMenu(menuMgr, treeViewer);
		Menu menu = menuMgr.createContextMenu(treeViewer.getTree());
		treeViewer.getTree().setMenu(menu);

		menuMgr.add(copyAction);
		menuMgr.add(new Separator());
		menuMgr.add(rerunAction);
		menuMgr.add(redebugAction);
		menuMgr.add(new Separator());
		menuMgr.add(expandAllAction);
		menuMgr.add(collapseAllAction);

		IActionBars actionBars = viewSite.getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
		actionBars.updateActionBars();
	}

	/**
	 * Handles the context menu showing.
	 *
	 * @param manager context menu manager
	 */
	private void handleMenuAboutToShow(IMenuManager manager) {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		boolean isRelaunchEnabledForSelection = !selection.isEmpty()
				&& (testingSession.getTestsRunnerProviderInfo().isAllowedMultipleTestFilter()
						|| (selection.size() == 1));
		rerunAction.setEnabled(isRelaunchEnabledForSelection);
		rerunAction.setTestingSession(testingSession);
		redebugAction.setEnabled(isRelaunchEnabledForSelection);
		redebugAction.setTestingSession(testingSession);
		copyAction.setEnabled(!selection.isEmpty());

		boolean hasAnything = treeViewer.getInput() != null;
		expandAllAction.setEnabled(hasAnything);
		collapseAllAction.setEnabled(hasAnything);
	}

	/**
	 * Sets the testing session to show.
	 *
	 * @param testingSession testing session or null to set default empty
	 * session
	 */
	public void setTestingSession(ITestingSession testingSession) {
		this.testingSession = testingSession;
		treeViewer.setInput(testingSession != null ? testingSession.getModelAccessor().getRootSuite() : null);
	}

	/**
	 * Provides access to the main widget of the tests hierarchy viewer.
	 *
	 * @return main widget of the tests hierarchy viewer
	 */
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	/**
	 * Move the selection to the next failed test case.
	 */
	public void showNextFailure() {
		showFailure(true);
	}

	/**
	 * Move the selection to the previous failed test case.
	 */
	public void showPreviousFailure() {
		showFailure(false);
	}

	/**
	 * Common implementation for movement the selection to the next or previous
	 * failed test case.
	 *
	 * @param next true if the next failed test case should be selected and false otherwise
	 */
	private void showFailure(boolean next) {
		IStructuredSelection selection = (IStructuredSelection) getTreeViewer().getSelection();
		ITestItem selected = (ITestItem) selection.getFirstElement();
		ITestItem failedItem;

		if (selected == null) {
			ITestItem rootSuite = (ITestItem) treeViewer.getInput();
			// For next element we should also check its children, for previous shouldn't.
			failedItem = findFailedImpl(rootSuite, null, next, next);
		} else {
			// For next element we should also check its children, for previous shouldn't.
			failedItem = findFailedImpl(selected.getParent(), selected, next, next);
		}

		if (failedItem != null)
			getTreeViewer().setSelection(new StructuredSelection(failedItem), true);
	}

	/**
	 * Returns the next or previous failed test case relatively to the
	 * <code>currItem</code> that should be a child of <code>parentItem</code>.
	 * If the such item was not found through the children, it steps up to the
	 * parent and continues search.
	 *
	 * @param parentItem parent test item to the current one
	 * @param currItem current item search should be started from or null if
	 * there is no any
	 * @param next true if the next failed test case should be looked for and
	 * false otherwise
	 * @param checkCurrentChild specifies whether the search should be also
	 * through the children for the current item
	 * @return found item or null
	 */
	private ITestItem findFailedImpl(ITestItem parentItem, ITestItem currItem, boolean next,
			boolean checkCurrentChild) {
		ITestItem result = findFailedChild(parentItem, currItem, next, checkCurrentChild);
		if (result != null) {
			return result;
		}
		// Nothing found at this level - try to step up
		ITestSuite grandParentItem = parentItem.getParent();
		if (grandParentItem != null) {
			return findFailedImpl(grandParentItem, parentItem, next, false);
		}
		return null;
	}

	/**
	 * Returns the next or previous failed test case relatively to the
	 * <code>currItem</code> that should be a child of <code>parentItem</code>.
	 * Note that unlike <code>findFailedImpl()</code> this method search only
	 * through the children items.
	 *
	 * @param parentItem parent test item to the current one
	 * @param currItem current item search should be started from or null if
	 * there is no any
	 * @param next true if the next failed test case should be looked for and
	 * false otherwise
	 * @param checkCurrentChild specifies whether the search should be also
	 * through the children for the current item
	 * @return found item or null
	 */
	private ITestItem findFailedChild(ITestItem parentItem, ITestItem currItem, boolean next,
			boolean checkCurrentChild) {
		ITestItem[] children = parentItem.getChildren();
		boolean doSearch = (currItem == null);
		int increment = next ? 1 : -1;
		int startIndex = next ? 0 : children.length - 1;
		int endIndex = next ? children.length : -1;
		for (int index = startIndex; index != endIndex; index += increment) {
			ITestItem item = children[index];
			// Check element
			if (doSearch) {
				if (item instanceof ITestCase && item.getStatus().isError()) {
					return item;
				}
			}
			// If children of current element should be checked we should enable search here (if necessary)
			if (checkCurrentChild && item == currItem) {
				doSearch = true;
			}
			// Search element's children
			if (doSearch) {
				ITestItem result = findFailedChild(item, null, next, checkCurrentChild);
				if (result != null) {
					return result;
				}
			}
			// If children of current element should NOT be checked we should enable search here
			if (!checkCurrentChild && item == currItem) {
				doSearch = true;
			}
		}
		return null;
	}

	/**
	 * Returns whether test items execution time should be shown in tests
	 * hierarchy.
	 *
	 * @return true if time should be shown and false otherwise
	 */
	public boolean showTime() {
		return showTime;
	}

	/**
	 * Sets whether test items execution time should be shown in tests
	 * hierarchy. Updates tests hierarchy viewer if the view is changed.
	 *
	 * @param showTime true if time is shown and false otherwise
	 */
	public void setShowTime(boolean showTime) {
		if (this.showTime != showTime) {
			this.showTime = showTime;
			getTreeViewer().refresh();
		}
	}

	/**
	 * Sets whether only failed tests should be shown.
	 *
	 * @param showFailedOnly new filter state
	 */
	public void setShowFailedOnly(boolean showFailedOnly) {
		// Create filter on first demand
		if (failedOnlyFilter == null) {
			failedOnlyFilter = new FailedOnlyFilter();
		}
		if (showFailedOnly) {
			getTreeViewer().addFilter(failedOnlyFilter);
		} else {
			getTreeViewer().removeFilter(failedOnlyFilter);
		}
	}

	/**
	 * Returns whether tests hierarchy should be shown in flat or hierarchical
	 * mode.
	 *
	 * @return tests hierarchy view mode
	 */
	public boolean showTestsHierarchy() {
		return showTestsHierarchy;
	}

	/**
	 * Sets whether tests hierarchy should be shown in flat or hierarchical
	 * mode. Updates tests hierarchy viewer if the view is changed.
	 *
	 * @param showTestsHierarchy true if tests hierarchy is shown in
	 * hierarchical mode and false otherwise
	 */
	public void setShowTestsHierarchy(boolean showTestsHierarchy) {
		if (this.showTestsHierarchy != showTestsHierarchy) {
			this.showTestsHierarchy = showTestsHierarchy;
			getTreeViewer().refresh();
		}
	}

}
