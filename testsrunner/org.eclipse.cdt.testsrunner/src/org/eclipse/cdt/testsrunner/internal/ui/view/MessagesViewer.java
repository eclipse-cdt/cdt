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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.testsrunner.internal.model.TestingSessionsManager;
import org.eclipse.cdt.testsrunner.internal.ui.view.actions.CopySelectedMessagesAction;
import org.eclipse.cdt.testsrunner.internal.ui.view.actions.OpenInEditorAction;
import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestLocation;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestMessage.Level;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Shows the messages for the currently selected items in tests hierarchy (test
 * suites or test cases).
 */
public class MessagesViewer {

	/**
	 * Enumeration of all possible message filter actions by level.
	 */
	public enum LevelFilter {
		Info(ISharedImages.IMG_OBJS_INFO_TSK, ITestMessage.Level.Info, ITestMessage.Level.Message),
		Warning(ISharedImages.IMG_OBJS_WARN_TSK, ITestMessage.Level.Warning), Error(ISharedImages.IMG_OBJS_ERROR_TSK,
				ITestMessage.Level.Error, ITestMessage.Level.FatalError, ITestMessage.Level.Exception);

		private String imageId;
		private ITestMessage.Level[] includedLevels;

		LevelFilter(String imageId, ITestMessage.Level... includedLevels) {
			this.imageId = imageId;
			this.includedLevels = includedLevels;
		}

		/**
		 * The shared image ID corresponding to the message level filter action.
		 *
		 * @return shared image ID
		 */
		public String getImageId() {
			return imageId;
		}

		/**
		 * The message levels that should be shown if current message level
		 * filter action is set.
		 *
		 * @return array of message levels
		 */
		public ITestMessage.Level[] getLevels() {
			return includedLevels;
		}

		/**
		 * Checks whether the specified message level should be shown if current
		 * message level filter action is set.
		 *
		 * @param messageLevel message level to search
		 * @return <code>true</code> if found
		 */
		public boolean isIncluded(ITestMessage.Level messageLevel) {
			for (ITestMessage.Level currLevel : includedLevels) {
				if (currLevel.equals(messageLevel)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * The content provider for the test messages viewer.
	 */
	private class MessagesContentProvider implements IStructuredContentProvider {

		/**
		 * Utility class: recursively collects all the messages of the specified
		 * test item.
		 */
		class MessagesCollector implements IModelVisitor {

			/** Collected test messages. */
			Collection<ITestMessage> collectedTestMessages;

			/**
			 * Specifies whether gathering should be done. It is used to skip
			 * the messages of the passed tests if they should not be shown.
			 */
			boolean collect = true;

			MessagesCollector(Collection<ITestMessage> testMessages) {
				this.collectedTestMessages = testMessages;
			}

			@Override
			public void visit(ITestMessage testMessage) {
				if (collect) {
					collectedTestMessages.add(testMessage);
				}
			}

			@Override
			public void visit(ITestCase testCase) {
				collect = !showFailedOnly || testCase.getStatus().isError();
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

		/** Test messages to show in the viewer. */
		ITestMessage[] testMessages;

		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			if (newInput != null) {
				collectMessages((ITestItem[]) newInput);
			} else {
				testMessages = new ITestMessage[0];
			}
		}

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object parent) {
			return testMessages;
		}

		/**
		 * Creates a messages set with a custom comparator. It is used for the
		 * ordered messages showing.
		 *
		 * @return set to store the test messages
		 */
		private TreeSet<ITestMessage> createMessagesSet() {
			return new TreeSet<>(new Comparator<ITestMessage>() {

				@Override
				public int compare(ITestMessage message1, ITestMessage message2) {
					// Compare messages by location
					ITestLocation location1 = message1.getLocation();
					ITestLocation location2 = message2.getLocation();

					if (location1 != null && location2 != null) {
						// Compare by file name
						String file1 = location1.getFile();
						String file2 = location2.getFile();
						int fileResult = file1.compareTo(file2);
						if (fileResult != 0) {
							return fileResult;
						} else {
							// Compare by line number
							int line1 = location1.getLine();
							int line2 = location2.getLine();
							if (line1 < line2) {
								return -1;

							} else if (line1 > line2) {
								return 1;
							}
						}

					} else if (location1 == null && location2 != null) {
						return -1;

					} else if (location1 != null && location2 == null) {
						return 1;
					}

					// Compare by message text
					String text1 = message1.getText();
					String text2 = message2.getText();
					return text1.compareTo(text2);
				}
			});
		}

		/**
		 * Creates a list to store the test messages. It is used for the
		 * unordered messages showing.
		 *
		 * @return list to store the test messages
		 */
		private ArrayList<ITestMessage> createMessagesList() {
			return new ArrayList<>();
		}

		/**
		 * Creates a collection to store the test messages depending on whether
		 * ordering is required.
		 *
		 * @return collection to store the test messages
		 */
		private Collection<ITestMessage> createMessagesCollection() {
			return orderingMode ? createMessagesSet() : createMessagesList();
		}

		/**
		 * Run messages collecting for the specified test items.
		 *
		 * @param testItems test items array
		 */
		private void collectMessages(ITestItem[] testItems) {
			Collection<ITestMessage> testMessagesCollection = createMessagesCollection();
			for (ITestItem testItem : testItems) {
				testItem.visit(new MessagesCollector(testMessagesCollection));
			}
			testMessages = testMessagesCollection.toArray(new ITestMessage[testMessagesCollection.size()]);
		}
	}

	/**
	 * The label provider for the test messages viewer.
	 */
	private class MessagesLabelProvider extends LabelProvider implements ITableLabelProvider {

		/**
		 * Returns the full (file path) or short (file name only) file path view
		 * depending on the filter set.
		 *
		 * @param location test object location
		 * @return file path
		 */
		private String getLocationFile(ITestLocation location) {
			String filePath = location.getFile();
			if (showFileNameOnly) {
				return new File(filePath).getName();
			} else {
				return filePath;
			}
		}

		@Override
		public String getColumnText(Object obj, int index) {
			ITestMessage message = (ITestMessage) obj;
			ITestLocation location = message.getLocation();
			String locationString = ""; //$NON-NLS-1$
			if (location != null) {
				locationString = MessageFormat.format(UIViewMessages.MessagesViewer_location_format,
						new Object[] { getLocationFile(location), location.getLine() });
			}
			return MessageFormat.format(UIViewMessages.MessagesViewer_message_format, locationString,
					message.getLevel(), message.getText());
		}

		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		@Override
		public Image getImage(Object obj) {
			Level level = ((ITestMessage) obj).getLevel();
			String imageId = ISharedImages.IMG_OBJ_ELEMENT;
			for (LevelFilter levelFilter : LevelFilter.values()) {
				if (levelFilter.isIncluded(level)) {
					imageId = levelFilter.getImageId();
					break;
				}
			}
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageId);
		}
	}

	/**
	 * Filters the required test messages by level.
	 */
	private class MessageLevelFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return acceptedMessageLevels.contains(((ITestMessage) element).getLevel());
		}
	}

	/** Main widget. */
	private TableViewer tableViewer;

	private IViewSite viewSite;

	// Context menu actions
	private OpenInEditorAction openInEditorAction;
	private Action copyAction;

	/** Specifies whether only messages for failed tests should be shown. */
	private boolean showFailedOnly = false;

	/**
	 * Specifies whether only file names should be shown (instead of full file
	 * paths).
	 */
	private boolean showFileNameOnly = false;

	/** The set of message level to show the messages with. */
	private Set<ITestMessage.Level> acceptedMessageLevels = new HashSet<>();

	/** Specifies whether test messages ordering is on or off. */
	private boolean orderingMode = false;

	public MessagesViewer(Composite parent, TestingSessionsManager sessionsManager, IWorkbench workbench,
			IViewSite viewSite, Clipboard clipboard) {
		this.viewSite = viewSite;
		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		tableViewer.setLabelProvider(new MessagesLabelProvider());
		tableViewer.setContentProvider(new MessagesContentProvider());
		tableViewer.addFilter(new MessageLevelFilter());
		initContextMenu(viewSite, sessionsManager, workbench, clipboard);
		tableViewer.addOpenListener(new IOpenListener() {
			@Override
			public void open(OpenEvent event) {
				openInEditorAction.run();
			}
		});
	}

	/**
	 * Initializes the viewer context menu.
	 *
	 * @param viewSite view
	 * @param sessionsManager testing sessions manager
	 * @param workbench workbench
	 * @param clipboard clipboard
	 */
	private void initContextMenu(IViewSite viewSite, TestingSessionsManager sessionsManager, IWorkbench workbench,
			Clipboard clipboard) {
		openInEditorAction = new OpenInEditorAction(tableViewer, sessionsManager, workbench);
		copyAction = new CopySelectedMessagesAction(tableViewer, clipboard);

		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				handleMenuAboutToShow(manager);
			}
		});
		viewSite.registerContextMenu(menuMgr, tableViewer);
		Menu menu = menuMgr.createContextMenu(tableViewer.getTable());
		tableViewer.getTable().setMenu(menu);

		menuMgr.add(openInEditorAction);
		menuMgr.add(copyAction);
		configureCopy();
	}

	/**
	 * Configures the view copy action which should be run on CTRL+C. We have to
	 * track widget focus to select the actual action because we have a few
	 * widgets that should provide copy action (at least tests hierarchy viewer
	 * and messages viewer).
	 */
	private void configureCopy() {
		getTableViewer().getTable().addFocusListener(new FocusListener() {
			IAction viewCopyHandler;

			@Override
			public void focusLost(FocusEvent e) {
				if (viewCopyHandler != null) {
					switchTo(viewCopyHandler);
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				switchTo(copyAction);
			}

			private void switchTo(IAction copyAction) {
				IActionBars actionBars = viewSite.getActionBars();
				viewCopyHandler = actionBars.getGlobalActionHandler(ActionFactory.COPY.getId());
				actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
				actionBars.updateActionBars();
			}
		});
	}

	/**
	 * Handles the context menu showing.
	 *
	 * @param manager context menu manager
	 */
	private void handleMenuAboutToShow(IMenuManager manager) {
		ISelection selection = tableViewer.getSelection();
		openInEditorAction.setEnabled(!selection.isEmpty());
		copyAction.setEnabled(!selection.isEmpty());
	}

	/**
	 * Provides access to the main widget of the messages viewer.
	 *
	 * @return main widget of the messages viewer
	 */
	public TableViewer getTableViewer() {
		return tableViewer;
	}

	/**
	 * Sets the test items for which the messages should be shown.
	 *
	 * @param testItems test items array
	 */
	public void showItemsMessages(ITestItem[] testItems) {
		tableViewer.setInput(testItems);
	}

	/**
	 * Forces the messages recollecting. It is used after message filters
	 * change.
	 */
	private void forceRecollectMessages() {
		// NOTE: Set input again makes content provider to recollect messages (with filters applied)
		tableViewer.setInput(tableViewer.getInput());
	}

	/**
	 * Returns whether the messages only for the failed tests should be shown.
	 *
	 * @return filter state
	 */
	public boolean getShowFailedOnly() {
		return showFailedOnly;
	}

	/**
	 * Sets whether the messages only for the failed tests should be shown.
	 *
	 * @param showFailedOnly new filter state
	 */
	public void setShowFailedOnly(boolean showFailedOnly) {
		if (this.showFailedOnly != showFailedOnly) {
			this.showFailedOnly = showFailedOnly;
			forceRecollectMessages();
		}
	}

	/**
	 * Returns whether short or long view for file paths should be shown.
	 *
	 * @return filter state
	 */
	public boolean getShowFileNameOnly() {
		return showFileNameOnly;
	}

	/**
	 * Sets whether short or long view for file paths should be shown.
	 *
	 * @param showFileNameOnly new filter state
	 */
	public void setShowFileNameOnly(boolean showFileNameOnly) {
		if (this.showFileNameOnly != showFileNameOnly) {
			this.showFileNameOnly = showFileNameOnly;
			forceRecollectMessages();
		}
	}

	/**
	 * Returns whether test messages should be ordered by location.
	 *
	 * @return messages ordering state
	 */
	public boolean getOrderingMode() {
		return orderingMode;
	}

	/**
	 * Sets whether test messages should be ordered by location.
	 *
	 * @param orderingMode new messages ordering state
	 */
	public void setOrderingMode(boolean orderingMode) {
		if (this.orderingMode != orderingMode) {
			this.orderingMode = orderingMode;
			forceRecollectMessages();
		}
	}

	/**
	 * Adds the filter message level filters by the message filter action level.
	 *
	 * @param levelFilter message filter action level
	 * @param refresh specifies whether viewer should be refreshed after filter
	 * update (small optimization: avoid many updates on initialization)
	 */
	public void addLevelFilter(LevelFilter levelFilter, boolean refresh) {
		for (ITestMessage.Level level : levelFilter.getLevels()) {
			acceptedMessageLevels.add(level);
		}
		if (refresh) {
			tableViewer.refresh();
		}
	}

	/**
	 * Removed the filter message level filters by the message filter action
	 * level.
	 *
	 * @param levelFilter message filter action level
	 */
	public void removeLevelFilter(LevelFilter levelFilter) {
		for (ITestMessage.Level level : levelFilter.getLevels()) {
			acceptedMessageLevels.remove(level);
		}
		tableViewer.refresh();
	}

}
