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
package org.eclipse.cdt.testsrunner.internal.ui.view.actions;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.model.TestingSessionsManager;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

/**
 * Shows the testing sessions history in drop down list.
 */
public class HistoryDropDownAction extends Action {
	
	/**
	 * The dialog for testing sessions history management. Allows to browse,
	 * activate and remove the selected testing sessions and to set testing
	 * sessions history size limit.
	 */
	private class HistoryListDialog extends StatusDialog {
		
		/** Max value for the history size limit that may be set via the dialog. */
		private static final int MAX_HISTORY_SIZE_LIMIT = 100;

		/** Testing sessions history list. */
		private ListDialogField<ITestingSession> historyList;

		/** String field that allows to set testing sessions history size limit. */
		private StringDialogField historySizeLimitField;
		
		/** Currently set testing sessions history size limit. */
		private int historySizeLimit;

		/** The currently selected active testing session. */
		private ITestingSession resultActiveSession;


		/**
		 * Label provider for the dialog testing sessions list.
		 */
		private final class TestRunLabelProvider extends LabelProvider {

			@Override
			public String getText(Object element) {
				return ((ITestingSession)element).getName();
			}

		}

		
		private HistoryListDialog(Shell shell) {
			super(shell);
			setHelpAvailable(false);
			setTitle(ActionsMessages.HistoryAction_dialog_title);

			createHistoryList();
			createHistorySizeLimitField();
		}

		@Override
		protected boolean isResizable() {
			return true;
		}

		/**
		 * Fills the testing sessions history list.
		 */
		private void createHistoryList() {
			IListAdapter<ITestingSession> adapter = new IListAdapter<ITestingSession>() {
				@Override
				public void customButtonPressed(ListDialogField<ITestingSession> field, int index) {
					doCustomButtonPressed(index);
				}
				@Override
				public void selectionChanged(ListDialogField<ITestingSession> field) {
					doSelectionChanged();
				}

				@Override
				public void doubleClicked(ListDialogField<ITestingSession> field) {
					doDoubleClicked();
				}
			};
			String[] buttonLabels = new String[] { ActionsMessages.HistoryAction_dialog_button_remove, ActionsMessages.HistoryAction_dialog_button_remove_all };
			LabelProvider labelProvider = new TestRunLabelProvider();
			historyList = new ListDialogField<ITestingSession>(adapter, buttonLabels, labelProvider);
			historyList.setLabelText(ActionsMessages.HistoryAction_dialog_list_title);

			historyList.setElements(testingSessionsManager.getSessions());
			Object currentEntry = testingSessionsManager.getActiveSession();
			ISelection sel = (currentEntry != null) ? new StructuredSelection(currentEntry) : new StructuredSelection();
			historyList.selectElements(sel);
		}

		/**
		 * Initializes history size limit field of the dialog.
		 */
		private void createHistorySizeLimitField() {
			historySizeLimitField = new StringDialogField();
			historySizeLimitField.setLabelText(ActionsMessages.HistoryAction_dialog_limit_label);
			historySizeLimitField.setDialogFieldListener(new IDialogFieldListener() {
				@Override
				public void dialogFieldChanged(DialogField field) {
					String maxString = historySizeLimitField.getText();
					boolean valid;
					try {
						historySizeLimit = Integer.parseInt(maxString);
						valid = historySizeLimit > 0 && historySizeLimit < MAX_HISTORY_SIZE_LIMIT;
					} catch (NumberFormatException e) {
						valid = false;
					}
					IStatus status = valid ? StatusInfo.OK_STATUS : new StatusInfo(IStatus.ERROR, 
							MessageFormat.format(ActionsMessages.HistoryAction_dialog_limit_label_error, 
								Integer.toString(MAX_HISTORY_SIZE_LIMIT)
							)
						);
					updateStatus(status);
				}
			});
			historySizeLimitField.setText(Integer.toString(testingSessionsManager.getHistorySizeLimit()));
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			initializeDialogUnits(parent);

			Composite composite = (Composite) super.createDialogArea(parent);

			Composite inner = new Composite(composite, SWT.NONE);
			inner.setLayoutData(new GridData(GridData.FILL_BOTH));
			inner.setFont(composite.getFont());

			LayoutUtil.doDefaultLayout(inner, new DialogField[] { historyList, new org.eclipse.cdt.internal.ui.wizards.dialogfields.Separator() }, true);
			LayoutUtil.setHeightHint(historyList.getListControl(null), convertHeightInCharsToPixels(12));
			LayoutUtil.setHorizontalGrabbing(historyList.getListControl(null));

			Composite additionalControls = new Composite(inner, SWT.NONE);
			additionalControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			LayoutUtil.doDefaultLayout(additionalControls, new DialogField[] { historySizeLimitField }, false);
			LayoutUtil.setHorizontalGrabbing(historySizeLimitField.getTextControl(null));

			applyDialogFont(composite);
			return composite;
		}

		/**
		 * Processes dialog custom button pressing. 
		 * 
		 * @param index index of the button
		 */
		private void doCustomButtonPressed(int index) {
			switch (index) {
				case 0: // remove
					historyList.removeElements(historyList.getSelectedElements());
					historyList.selectFirstElement();
					break;

				case 1: // remove all
					historyList.removeAllElements();
					break;
				default:
					break;
			}
		}

		/**
		 * Processes double click on the item in dialog.
		 */
		private void doDoubleClicked() {
			okPressed();
		}

		/**
		 * Processes the selection change in the dialog
		 */
		private void doSelectionChanged() {
			List<ITestingSession> selected = historyList.getSelectedElements();
			if (selected.size() >= 1) {
				resultActiveSession = selected.get(0);
			} else {
				resultActiveSession = null;
			}
			historyList.enableButton(0, selected.size() != 0);
		}

		/**
		 * Provides access to the active session currently selected by user.
		 * 
		 * @return testing session
		 */
		public ITestingSession getResultActiveSession() {
			return resultActiveSession;
		}

		/**
		 * Provides access to the testing sessions history list edited by user.
		 * 
		 * @return list of testing sessions
		 */
		public List<ITestingSession> getResultSessions() {
			return historyList.getElements();
		}

		/**
		 * Provides access to value of history size limit specified by user.
		 * 
		 * @return history size limit
		 */
		public int getResultHistorySizeLimit() {
			return historySizeLimit;
		}
	}

	
	/**
	 * Represents a testing sessions history item. 
	 */
	private class HistoryAction extends Action {
		
		/** Testing session of the history item. */
		private final ITestingSession testingSession;

		public HistoryAction(int testingSessionIndex, ITestingSession testingSession) {
			super("", AS_RADIO_BUTTON); //$NON-NLS-1$
			this.testingSession = testingSession;

			// Generate action text
			String label = testingSession.getName();
			if (testingSessionIndex < 10) {
				// Add the numerical accelerator
				label = new StringBuilder().append('&').append(testingSessionIndex).append(' ').append(label).toString();
			}
			setText(label);
		}

		@Override
		public void run() {
			if (isChecked()) {
				testingSessionsManager.setActiveSession(testingSession);
			}
		}
	}
	
	
	/**
	 * Provides access to the history list management dialog.
	 */
	private class HistoryListAction extends Action {

		public HistoryListAction() {
			super(ActionsMessages.HistoryAction_history_item_show_text, AS_RADIO_BUTTON);
		}

		@Override
		public void run() {
			if (isChecked()) {
				runHistoryDialog();
			}
		}
	}
	
	
	/**
	 * Removes the terminated testing sessions from the history list.
	 */
	private class ClearAction extends Action {
		
		public ClearAction() {
			setText(ActionsMessages.HistoryAction_history_item_clear_text);

			boolean enabled = false;
			for (ITestingSession testingSession : testingSessionsManager.getSessions()) {
				if (testingSession.isFinished()) {
					enabled = true;
					break;
				}
			}
			setEnabled(enabled);
		}

		@Override
		public void run() {
			List<ITestingSession> remainingSessions = new ArrayList<ITestingSession>();
			for (ITestingSession testingSession : testingSessionsManager.getSessions()) {
				if (!testingSession.isFinished()) {
					remainingSessions.add(testingSession);
				}
			}

			ITestingSession newActiveSession = remainingSessions.isEmpty() ? null : remainingSessions.get(0);
			testingSessionsManager.setActiveSession(newActiveSession);
			testingSessionsManager.setSessions(remainingSessions);
		}
	}

	
	/**
	 * Represents the testing sessions history menu factory.
	 */
	private class HistoryMenuCreator implements IMenuCreator {

		@Override
		public Menu getMenu(Menu parent) {
			return null;
		}

		@Override
		public Menu getMenu(Control parent) {
			if (menu != null) {
				menu.dispose();
			}
			final MenuManager manager = new MenuManager();
			manager.setRemoveAllWhenShown(true);
			manager.addMenuListener(new IMenuListener() {
				@Override
				public void menuAboutToShow(IMenuManager manager2) {
					boolean checkOthers = addHistoryItems(manager2);

					Action others = new HistoryListAction();
					others.setChecked(checkOthers);
					manager2.add(others);
					manager2.add(new Separator());
					manager2.add(new ClearAction());
				}

				private boolean addHistoryItems(IMenuManager manager) {
					boolean checkOthers = true;
					
					int sessionsCount = testingSessionsManager.getSessionsCount();
					if (sessionsCount == 0) {
						return false;
					}
					int menuItemsCount = Math.min(sessionsCount, RESULTS_IN_DROP_DOWN_MENU);
					
					ITestingSession activeSession = testingSessionsManager.getActiveSession();
					int testingSessionIndex = 0;
					for (ITestingSession testingSession : testingSessionsManager.getSessions()) {
						if (testingSessionIndex >= menuItemsCount) {
							break;
						}
						HistoryAction action = new HistoryAction(testingSessionIndex, testingSession);
						boolean check = (testingSession == activeSession);
						action.setChecked(check);
						if (check) {
							checkOthers = false;
						}
						manager.add(action);
						testingSessionIndex++;
					}
					
					return checkOthers;
				}
			});

			menu = manager.createContextMenu(parent);
			return menu;
		}

		@Override
		public void dispose() {
			if (menu != null) {
				menu.dispose();
				menu = null;
			}
		}
	}
	
	
	/** Defines how many history items should be showed in drop down history menu. */
	public static final int RESULTS_IN_DROP_DOWN_MENU = 10;

	/** Accessor to the Testing Sessions Plug-in Manager. */
	private TestingSessionsManager testingSessionsManager;
	
	/** Required for the history dialog. */
	private Shell shell;
	
	/** Drop down menu */
	private Menu menu;

	
	public HistoryDropDownAction(TestingSessionsManager testingSessionsManager, Shell shell) {
		super(ActionsMessages.HistoryAction_history_text);
		setToolTipText(ActionsMessages.HistoryAction_history_tooltip);
		setDisabledImageDescriptor(TestsRunnerPlugin.getImageDescriptor("dlcl16/history_list.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/history_list.gif")); //$NON-NLS-1$
		setImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/history_list.gif")); //$NON-NLS-1$
		this.testingSessionsManager = testingSessionsManager;
		this.shell = shell;
		setMenuCreator(new HistoryMenuCreator());
	}

	/**
	 * Shows the testing sessions history dialog.
	 */
	private void runHistoryDialog() {
		HistoryListDialog dialog = new HistoryListDialog(shell);
		if (dialog.open() == Window.OK) {
			testingSessionsManager.setHistorySizeLimit(dialog.getResultHistorySizeLimit());
			testingSessionsManager.setActiveSession(dialog.getResultActiveSession());
			testingSessionsManager.setSessions(dialog.getResultSessions());
		}
	}
	
	@Override
	public void run() {
		runHistoryDialog();
	}
	
}

