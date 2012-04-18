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
package org.eclipse.cdt.testsrunner.internal.ui.view;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.testsrunner.internal.model.ITestingSessionsManagerListener;
import org.eclipse.cdt.testsrunner.internal.model.TestingSessionsManager;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.cdt.testsrunner.model.ITestingSessionListener;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

/**
 * Tracks and collects the changes in active testing session and updates the UI
 * periodically. It allows to significantly improve the UI performance.
 */
public class UIUpdater {
	
	/** Access to the results showing view. */
	private ResultsView resultsView;
	
	/** Access to the tests hierarchy showing widget. */
	private TestsHierarchyViewer testsHierarchyViewer;

	/** Access to the statistics showing widget. */
	private ProgressCountPanel progressCountPanel;
	
	/** Listener for the changes in active testing session. */
	private ITestingSessionListener sessionListener;
	
	/**
	 * Specifies whether tests hierarchy scrolling should be done during the
	 * testing process.
	 */
	private boolean autoScroll = true;
	
	/** Access to the testing sessions manager. */
	private TestingSessionsManager sessionsManager;
	
	/** Listener to handle active testing session change. */
	private TestingSessionsManagerListener sessionsManagerListener;
	
	/** Reference to the active testing session. */
	ITestingSession testingSession;
	
	/** Storage for the UI changes that should be done on update. */
	UIChangesCache uiChangesCache = new UIChangesCache();
	
	/** A job that makes an UI update periodically. */
	UpdateUIJob updateUIJob = null;
	
	/** Time interval over which the UI should be updated. */
	private static final int REFRESH_INTERVAL = 200;
	
	
	/** 
	 * Storage for the UI changes that should be done on update.
	 */
	private class UIChangesCache {
		
		/**
		 * Specifies whether Progress Counter Panel should be updated during the
		 * next UI update.
		 */
		private boolean needProgressCountPanelUpdate;

		/**
		 * Specifies whether view actions should be updated during the next UI
		 * update.
		 */
		private boolean needActionsUpdate;

		/**
		 * A test item which path should be shown as a view caption during the
		 * next UI update.
		 */
		private ITestItem testItemForNewViewCaption;
		
		/**
		 * Set of tree objects on which <code>refresh()</code> should be called
		 * during the next UI update.
		 */
 		private Set<Object> treeItemsToRefresh = new HashSet<Object>();

		/**
		 * Set of tree objects on which <code>update()</code> should be called
		 * during the next UI update.
		 */
 		private Set<Object> treeItemsToUpdate = new HashSet<Object>();

 		/** Tree object that should be revealed during the next UI update. */
 		private Object treeItemToReveal;
 		
 		/** Map of tree objects that should be expanded or collapsed to their new states. */
		private Map<Object, Boolean> treeItemsToExpand = new LinkedHashMap<Object, Boolean>();
		
		
		UIChangesCache() {
			resetChanges();
		}

		/**
		 * Schedules the Progress Counter Panel update during the next UI update.
		 */
		public void scheduleProgressCountPanelUpdate() {
			synchronized (this) {
				needProgressCountPanelUpdate = true;
			}
		}
		
		/**
		 * Schedules the view actions update during the next UI update.
		 */
		public void scheduleActionsUpdate() {
			synchronized (this) {
				needActionsUpdate = true;
			}
		}
		
		/**
		 * Schedules the view caption update to the path to specified test item
		 * during the next UI update.
		 * 
		 * @param testItem specified test item
		 */
		public void scheduleViewCaptionChange(ITestItem testItem) {
			synchronized (this) {
				testItemForNewViewCaption = testItem;
			}
		}
		
		/**
		 * Schedules the <code>update()</code> call for the specified tree
		 * object during the next UI update.
		 * 
		 * @param item tree object to update
		 */
		public void scheduleTreeItemUpdate(Object item) {
			synchronized (this) {
				treeItemsToUpdate.add(item);
			}
		}
		
		/**
		 * Schedules the revealing of the specified tree object. Overrides
		 * the previously specified tree object to reveal (if any).
		 * 
		 * @param item tree object to reveal
		 */
		public void scheduleTreeItemReveal(Object item) {
			synchronized (this) {
				treeItemToReveal = item;
			}
		}
		
		/**
		 * Schedules the expanding or collapsing of the specified tree object.
		 * Overrides the previous state for the same tree object (if any).
		 * 
		 * @param item tree object to expand or collapse
		 * @param expandedState true if the node is expanded, and false if
		 * collapsed
		 */
		public void scheduleTreeItemExpand(Object item, boolean expandedState) {
			synchronized (this) {
				treeItemsToExpand.put(item, expandedState);
			}
		}
		
		/**
		 * Schedules the <code>refresh()</code> call for the specified tree
		 * object during the next UI update.
		 * 
		 * @param item tree object to refresh
		 */
		public void scheduleTreeItemRefresh(Object item) {
			synchronized (this) {
				treeItemsToRefresh.add(item);
			}
		}
		
		
		/**
		 * Apply any scheduled changes to UI.
		 */
		public void applyChanges() {
			synchronized (this) {
				TreeViewer treeViewer = testsHierarchyViewer.getTreeViewer();
				// View statistics widgets update
				if (needProgressCountPanelUpdate) {
					progressCountPanel.updateInfoFromSession();
				}
				// View actions update
				if (needActionsUpdate) {
					resultsView.updateActionsFromSession();
				}
				// View caption update
				if (testItemForNewViewCaption != null) {
					resultsView.setCaption(
							MessageFormat.format(
								UIViewMessages.UIUpdater_view_caption_format, 
									testItemForNewViewCaption.getName(),
									TestPathUtils.getTestItemPath(testItemForNewViewCaption.getParent())
							)
						);
				}
				// Tree view update
				if (!treeItemsToRefresh.isEmpty()) {
					for (Object item : treeItemsToRefresh) {
						treeViewer.refresh(item, false);
					}
				}
				if (!treeItemsToUpdate.isEmpty()) {
					treeViewer.update(treeItemsToUpdate.toArray(), null);
				}
				if (treeItemToReveal != null) {
					treeViewer.reveal(treeItemToReveal);
				}
				if (!treeItemsToExpand.isEmpty()) {
					for (Map.Entry<Object, Boolean> entry : treeItemsToExpand.entrySet()) {
						treeViewer.setExpandedState(entry.getKey(), entry.getValue());
					}
				}
				// All changes are applied, remove them 
				resetChangesImpl();
			}
		}

		/**
		 * Reset all the scheduled changes to UI.
		 */
		public void resetChanges() {
			synchronized (this) {
				resetChangesImpl();
			}
		}
		
		/**
		 * Reset all the scheduled changes to UI. Note, this method is not
		 * synchronized so it should be used carefully
		 */
		private void resetChangesImpl() {
			needProgressCountPanelUpdate = false;
			needActionsUpdate = false;
			testItemForNewViewCaption = null;
			treeItemsToUpdate.clear();
			treeItemToReveal = null;
			treeItemsToExpand.clear();
		}
	}


	/**
	 * A job that makes an UI update periodically.
	 */
	private class UpdateUIJob extends UIJob {

		/** Controls whether the job should be scheduled again. */
		private boolean isRunning = true;

		public UpdateUIJob() {
			super(UIViewMessages.UIUpdater_update_ui_job);
			setSystem(true);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (!resultsView.isDisposed()) {
				uiChangesCache.applyChanges();
				scheduleSelf();
			}
			return Status.OK_STATUS;
		}
		
		/**
		 * Schedule self for running after time interval.
		 */
		public void scheduleSelf() {
			schedule(REFRESH_INTERVAL);
		}
		
		/**
		 * Sets the flag that prevents planning this job again.
		 */
		public void stop() {
			isRunning = false;
		}

		@Override
		public boolean shouldSchedule() {
			return isRunning;
		}

	}

	
	/**
	 * Listener for the changes in active testing session.
	 */
	private class SessionListener implements ITestingSessionListener {
		
		/**
		 * Common implementation for test case and test suite entering.
		 * 
		 * @param testItem test case or test suite
		 */
		private void enterTestItem(ITestItem testItem) {
			uiChangesCache.scheduleViewCaptionChange(testItem);
			uiChangesCache.scheduleTreeItemUpdate(testItem);
			if (autoScroll) {
				uiChangesCache.scheduleTreeItemReveal(testItem);
			}
		}
		
		@Override
		public void enterTestSuite(ITestSuite testSuite) {
			enterTestItem(testSuite);
		}
	
		@Override
		public void exitTestSuite(ITestSuite testSuite) {
			uiChangesCache.scheduleTreeItemUpdate(testSuite);
			if (autoScroll) {
				uiChangesCache.scheduleTreeItemExpand(testSuite, false);
			}
		}
	
		@Override
		public void enterTestCase(ITestCase testCase) {
			enterTestItem(testCase);
		}
	
		@Override
		public void exitTestCase(ITestCase testCase) {
			uiChangesCache.scheduleActionsUpdate();
			uiChangesCache.scheduleProgressCountPanelUpdate();
			uiChangesCache.scheduleTreeItemUpdate(testCase);
		}
	
		@Override
		public void childrenUpdate(ITestSuite parent) {
			uiChangesCache.scheduleTreeItemRefresh(parent);
		}

		@Override
		public void testingStarted() {
			resultsView.updateActionsFromSession();
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					resultsView.setCaption(testingSession.getStatusMessage());
					progressCountPanel.updateInfoFromSession();
					testsHierarchyViewer.getTreeViewer().refresh();
				}
			});
			startUpdateUIJob();
		}

		@Override
		public void testingFinished() {
			stopUpdateUIJob();
			resultsView.updateActionsFromSession();
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					uiChangesCache.applyChanges();
					resultsView.setCaption(testingSession.getStatusMessage());
					progressCountPanel.updateInfoFromSession();
					testsHierarchyViewer.getTreeViewer().refresh();
					testsHierarchyViewer.getTreeViewer().collapseAll();
					testsHierarchyViewer.getTreeViewer().expandToLevel(2);
				}
			});
		}
	}


	/**
	 * Listener to handle active testing session change.
	 */
	private class TestingSessionsManagerListener implements ITestingSessionsManagerListener {
		
		@Override
		public void sessionActivated(ITestingSession newTestingSession) {
			if (testingSession != newTestingSession) {
				stopUpdateUIJob();
				uiChangesCache.resetChanges();
				
				unsubscribeFromSessionEvent();
				testingSession = newTestingSession;
				subscribeToSessionEvent();
				
				resultsView.updateActionsFromSession();
				Display.getDefault().syncExec(new Runnable() {
					
					@Override
					public void run() {
						progressCountPanel.setTestingSession(testingSession);
						testsHierarchyViewer.setTestingSession(testingSession);
						resultsView.setCaption(testingSession != null ? testingSession.getStatusMessage() : ""); //$NON-NLS-1$
					}
				});
				if (newTestingSession != null && !newTestingSession.isFinished()) {
					startUpdateUIJob();
				}
			}
		}
	}


	public UIUpdater(ResultsView resultsView, TestsHierarchyViewer testsHierarchyViewer, ProgressCountPanel progressCountPanel, TestingSessionsManager sessionsManager) {
		this.resultsView = resultsView;
		this.testsHierarchyViewer = testsHierarchyViewer;
		this.progressCountPanel = progressCountPanel;
		this.sessionsManager = sessionsManager;
		sessionListener = new SessionListener();
		sessionsManagerListener = new TestingSessionsManagerListener();
		sessionsManager.addListener(sessionsManagerListener);
	}

	/**
	 * Returns whether tests hierarchy scrolling should be done during the
	 * testing process.
	 * 
	 * @return auto scroll state
	 */
	public boolean getAutoScroll() {
		return autoScroll;
	}
	
	/**
	 * Sets whether whether tests hierarchy scrolling should be done during the
	 * testing process.
	 * 
	 * @param autoScroll new filter state
	 */
	public void setAutoScroll(boolean autoScroll) {
		this.autoScroll = autoScroll;
	}

	/**
	 * Disposes of the UI Updater. Make the necessary clean up.
	 */
	public void dispose() {
		unsubscribeFromSessionEvent();
		sessionsManager.removeListener(sessionsManagerListener);
	}
	
	/**
	 * Subscribes to the events of currently set testing session.
	 */
	private void subscribeToSessionEvent() {
		if (testingSession != null) {
			testingSession.getModelAccessor().addChangesListener(sessionListener);
		}
	}

	/**
	 * Unsubscribe from the events of currently set testing session.
	 */
	private void unsubscribeFromSessionEvent() {
		if (testingSession != null) {
			testingSession.getModelAccessor().removeChangesListener(sessionListener);
		}
	}
	
	/**
	 * Starts the UI updating job. Stops the previously running (if any).
	 */
	private void startUpdateUIJob() {
		stopUpdateUIJob();
		uiChangesCache.resetChanges();
		updateUIJob = new UpdateUIJob();
		updateUIJob.scheduleSelf();
	}
	
	/**
	 * Stops the UI updating job (if any).
	 */
	private void stopUpdateUIJob() {
		if (updateUIJob != null) {
			updateUIJob.stop();
			updateUIJob = null;
		}
	}

	/**
	 * Fakes the testing session activation and makes all necessary steps to
	 * handle it.
	 */
	public void reapplyActiveSession() {
		sessionsManagerListener.sessionActivated(sessionsManager.getActiveSession());
	}

}
