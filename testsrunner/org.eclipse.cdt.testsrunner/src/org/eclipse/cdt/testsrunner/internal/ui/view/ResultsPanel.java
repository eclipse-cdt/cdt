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

import java.util.Iterator;

import org.eclipse.cdt.testsrunner.internal.model.TestingSessionsManager;
import org.eclipse.cdt.testsrunner.internal.ui.view.MessagesViewer.LevelFilter;
import org.eclipse.cdt.testsrunner.internal.ui.view.actions.MessageLevelFilterAction;
import org.eclipse.cdt.testsrunner.internal.ui.view.actions.MessagesOrderingAction;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;

/**
 * The main widget of testing results view. It compounds tests hierarchy and
 * messages viewer. Depending on orientation it may layout them vertically or
 * horizontally.
 */
public class ResultsPanel {

	/** Parent for the child widgets (messages & tests hierarchy viewer). */
	private SashForm sashForm;
	
	/** Child widget: messages viewer. */
	private MessagesViewer messagesViewer;

	/** Child widget: tests hierarchy viewer. */
	private TestsHierarchyViewer testsHierarchyViewer;

	// Persistence tags
	static final String TAG_WEIGHT0 = "weight0"; //$NON-NLS-1$
	static final String TAG_WEIGHT1 = "weight1"; //$NON-NLS-1$
	static final String TAG_MESSAGES_ORDERING_ACTION = "messagesOrderingAction"; //$NON-NLS-1$
	static final String TAG_ERROR_FILTER_ACTION = "errorFilterAction"; //$NON-NLS-1$
	static final String TAG_WARNING_FILTER_ACTION = "warningFilterAction"; //$NON-NLS-1$
	static final String TAG_INFO_FILTER_ACTION = "infoFilterAction"; //$NON-NLS-1$

	// Messages Viewer actions
	Action messagesOrderingAction;
	Action errorFilterAction;
	Action warningFilterAction;
	Action infoFilterAction;


	public ResultsPanel(Composite parent, TestingSessionsManager sessionsManager, IWorkbench workbench, IViewSite site, Clipboard clipboard) {
		sashForm = new SashForm(parent, SWT.VERTICAL);

		// Configure tests hierarchy viewer
		ViewForm top = new ViewForm(sashForm, SWT.NONE);
		Composite empty = new Composite(top, SWT.NONE);
		empty.setLayout(new Layout() {
			@Override
			protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
				return new Point(1, 1); // (0, 0) does not work with super-intelligent ViewForm
			}
			@Override
			protected void layout(Composite composite, boolean flushCache) {}
		});
		top.setTopLeft(empty); // makes ViewForm draw the horizontal separator line ...
		testsHierarchyViewer = new TestsHierarchyViewer(top, site, clipboard);
		top.setContent(testsHierarchyViewer.getTreeViewer().getControl());

		// Configure test messages viewer
		ViewForm bottom = new ViewForm(sashForm, SWT.NONE);
		messagesViewer = new MessagesViewer(bottom, sessionsManager, workbench, site, clipboard);
		Composite topLeftPanel = new Composite(bottom, SWT.NONE);
		RowLayout topLeftPanelLayout = new RowLayout(SWT.HORIZONTAL);
		topLeftPanelLayout.spacing = 0;
		topLeftPanelLayout.center = true;
		topLeftPanelLayout.marginBottom = topLeftPanelLayout.marginLeft = topLeftPanelLayout.marginRight = topLeftPanelLayout.marginTop = 0;
		topLeftPanel.setLayout(topLeftPanelLayout);
		ToolBar leftMessagesToolBar = new ToolBar(topLeftPanel, SWT.FLAT | SWT.WRAP);
		ToolBarManager leftMessagesToolBarManager = new ToolBarManager(leftMessagesToolBar);
		messagesOrderingAction = new MessagesOrderingAction(messagesViewer);
		leftMessagesToolBarManager.add(messagesOrderingAction);
		leftMessagesToolBarManager.update(true);
		CLabel label = new CLabel(topLeftPanel, SWT.NONE);
		label.setText(UIViewMessages.MessagesPanel_label);
		bottom.setTopLeft(topLeftPanel);
		ToolBar rightMessagesToolBar = new ToolBar(bottom, SWT.FLAT | SWT.WRAP);
		ToolBarManager rightMessagesToolBarManager = new ToolBarManager(rightMessagesToolBar);
		errorFilterAction = new MessageLevelFilterAction(messagesViewer, LevelFilter.Error, true);
		warningFilterAction = new MessageLevelFilterAction(messagesViewer, LevelFilter.Warning, true);
		infoFilterAction = new MessageLevelFilterAction(messagesViewer, LevelFilter.Info, false);
		rightMessagesToolBarManager.add(errorFilterAction);
		rightMessagesToolBarManager.add(warningFilterAction);
		rightMessagesToolBarManager.add(infoFilterAction);
		rightMessagesToolBarManager.update(true);
		bottom.setTopCenter(rightMessagesToolBar);
		bottom.setContent(messagesViewer.getTableViewer().getControl());

		sashForm.setWeights(new int[]{50, 50});
	
		testsHierarchyViewer.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				handleTestItemSelected();
			}
		});
		
		// Initialize default value
		setShowFailedOnly(false);
		
		// Data for parent (view's) layout
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	/**
	 * Provides access to the tests hierarchy viewer.
	 * 
	 * @return tests hierarchy viewer
	 */
	public TestsHierarchyViewer getTestsHierarchyViewer() {
		return testsHierarchyViewer;
	}

	/**
	 * Provides access to the messages viewer.
	 * 
	 * @return messages viewer
	 */
	public MessagesViewer getMessagesViewer() {
		return messagesViewer;
	}

	/**
	 * Handles selection change in tests hierarchy viewer and updates the
	 * content of the messages viewer to show the messages for the selected
	 * items.
	 */
	private void handleTestItemSelected() {
		IStructuredSelection selection = (IStructuredSelection)testsHierarchyViewer.getTreeViewer().getSelection();
		ITestItem[] testItems = new ITestItem[selection.size()];
		int index = 0;
		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			testItems[index] = (ITestItem)it.next();
			++index;
		}
		messagesViewer.showItemsMessages(testItems);
	}

	/**
	 * Sets the widget orientation.
	 * 
	 * @param orientation new widget orientation (vertical or horizontal; auto
	 * is not supported)
	 */
	public void setPanelOrientation(ResultsView.Orientation orientation) {
		sashForm.setOrientation(orientation == ResultsView.Orientation.Horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
	}

	/**
	 * Returns whether only failed tests (and messages for them) should be
	 * shown.
	 * 
	 * @return filter state
	 */
	public boolean getShowFailedOnly() {
		return messagesViewer.getShowFailedOnly();
	}
	
	/**
	 * Sets whether only failed tests (and messages for them) should be shown.
	 * 
	 * @param showFailedOnly new filter state
	 */
	public void setShowFailedOnly(boolean showFailedOnly) {
		testsHierarchyViewer.setShowFailedOnly(showFailedOnly);
		messagesViewer.setShowFailedOnly(showFailedOnly);
	}
	
	/**
	 * Restores the value of the checkable action.
	 * 
	 * @param memento previously saved state to restore the action value from
	 * @param key tag name that is used to restore the value
	 * @param action action to restore
	 */
	private void restoreActionChecked(IMemento memento, String key, Action action) {
		Boolean checked = memento.getBoolean(key);
		if (checked != null) {
			action.setChecked(checked);
			action.run();
		}
	}

	/**
	 * Restores the state of the widget.
	 * 
	 * @param memento previously saved state
	 */
	public void restoreState(IMemento memento) {
		Integer weight0 = memento.getInteger(TAG_WEIGHT0);
		Integer weight1 = memento.getInteger(TAG_WEIGHT1);
		if (weight0 != null && weight1 != null) {
			sashForm.setWeights(new int[] {weight0, weight1});
		}
		restoreActionChecked(memento, TAG_MESSAGES_ORDERING_ACTION, messagesOrderingAction);
		restoreActionChecked(memento, TAG_ERROR_FILTER_ACTION, errorFilterAction);
		restoreActionChecked(memento, TAG_WARNING_FILTER_ACTION, warningFilterAction);
		restoreActionChecked(memento, TAG_INFO_FILTER_ACTION, infoFilterAction);
	}

	/**
	 * Saves the state of the widget.
	 * 
	 * @param memento where to save the state
	 */
	public void saveState(IMemento memento) {
		int[] weights = sashForm.getWeights();
		memento.putInteger(TAG_WEIGHT0, weights[0]);
		memento.putInteger(TAG_WEIGHT1, weights[1]);
		memento.putBoolean(TAG_MESSAGES_ORDERING_ACTION, messagesOrderingAction.isChecked());
		memento.putBoolean(TAG_ERROR_FILTER_ACTION, errorFilterAction.isChecked());
		memento.putBoolean(TAG_WARNING_FILTER_ACTION, warningFilterAction.isChecked());
		memento.putBoolean(TAG_INFO_FILTER_ACTION, infoFilterAction.isChecked());
	}

}
