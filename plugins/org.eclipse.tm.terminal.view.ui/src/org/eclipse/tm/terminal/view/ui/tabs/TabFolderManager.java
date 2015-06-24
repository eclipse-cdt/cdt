/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.tabs;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tm.internal.terminal.control.ITerminalListener;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.TerminalViewControlFactory;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView;
import org.eclipse.tm.terminal.view.ui.interfaces.ImageConsts;
import org.eclipse.tm.terminal.view.ui.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * Terminal tab folder manager.
 */
@SuppressWarnings({ "restriction" })
public class TabFolderManager extends PlatformObject implements ISelectionProvider {
	// Reference to the parent terminal consoles view
	private final ITerminalsView parentView;
	// Reference to the selection listener instance
	private final SelectionListener selectionListener;

	/**
	 * List of selection changed listeners.
	 */
	private final List<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();

	/**
	 * Map of tab command input field handler per tab item
	 */
	private final Map<CTabItem, TabCommandFieldHandler> commandFieldHandler = new HashMap<CTabItem, TabCommandFieldHandler>();

	/**
	 * The terminal control selection listener implementation.
	 */
	private class TerminalControlSelectionListener implements DisposeListener, MouseListener {
		private final ITerminalViewControl terminal;
		private boolean selectMode;

		/**
		 * Constructor.
		 *
		 * @param terminal The terminal control. Must not be <code>null</code>.
		 */
		public TerminalControlSelectionListener(ITerminalViewControl terminal) {
			Assert.isNotNull(terminal);
			this.terminal = terminal;

			// Register ourself as the required listener
			terminal.getControl().addDisposeListener(this);
			terminal.getControl().addMouseListener(this);
		}

		/**
		 * Returns the associated terminal view control.
		 *
		 * @return The terminal view control.
		 */
		protected final ITerminalViewControl getTerminal() {
			return terminal;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
		 */
		@Override
		public void widgetDisposed(DisposeEvent e) {
			// Widget got disposed, check if it is ours
			// If a tab item gets disposed, we have to dispose the terminal as well
			if (e.getSource().equals(terminal.getControl())) {
				// Remove as listener
				getTerminal().getControl().removeDisposeListener(this);
				getTerminal().getControl().removeMouseListener(this);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseDown(MouseEvent e) {
			// Left button down -> select mode starts
			if (e.button == 1) selectMode = true;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseUp(MouseEvent e) {
			if (e.button == 1 && selectMode) {
				selectMode = false;
				// Fire a selection changed event with the terminal controls selection
		        try {
		            Display display = PlatformUI.getWorkbench().getDisplay();
		            display.asyncExec(new Runnable() {
						@Override
						public void run() {
							fireSelectionChanged(new StructuredSelection(getTerminal().getSelection()));
						}
					});
		        }
		        catch (Exception ex) {
		            // if display is disposed, silently ignore.
		        }
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseDoubleClick(MouseEvent e) {
		}
	}

	/**
	 * Constructor.
	 *
	 * @param parentView The parent terminals console view. Must not be <code>null</code>.
	 */
	public TabFolderManager(ITerminalsView parentView) {
		super();
		Assert.isNotNull(parentView);
		this.parentView = parentView;

		// Attach a selection listener to the tab folder
		selectionListener = doCreateTabFolderSelectionListener(this);
		if (getTabFolder() != null) getTabFolder().addSelectionListener(selectionListener);
	}

	/**
	 * Creates the terminal console tab folder selection listener instance.
	 *
	 * @param parent The parent terminal console tab folder manager. Must not be <code>null</code>.
	 * @return The selection listener instance.
	 */
	protected TabFolderSelectionListener doCreateTabFolderSelectionListener(TabFolderManager parent) {
		Assert.isNotNull(parent);
		return new TabFolderSelectionListener(parent);
	}

	/**
	 * Returns the parent terminal consoles view.
	 *
	 * @return The terminal consoles view instance.
	 */
	protected final ITerminalsView getParentView() {
		return parentView;
	}

	/**
	 * Returns the tab folder associated with the parent view.
	 *
	 * @return The tab folder or <code>null</code>.
	 */
	@SuppressWarnings("cast")
    protected final CTabFolder getTabFolder() {
		return (CTabFolder) getParentView().getAdapter(CTabFolder.class);
	}

	/**
	 * Returns the selection changed listeners currently registered.
	 *
	 * @return The registered selection changed listeners or an empty array.
	 */
	protected final ISelectionChangedListener[] getSelectionChangedListeners() {
		return selectionChangedListeners.toArray(new ISelectionChangedListener[selectionChangedListeners.size()]);
	}

	/**
	 * Dispose the tab folder manager instance.
	 */
	public void dispose() {
		// Dispose the selection listener
		if (getTabFolder() != null && !getTabFolder().isDisposed()) getTabFolder().removeSelectionListener(selectionListener);
		// Dispose the tab command field handler
		for (TabCommandFieldHandler handler : commandFieldHandler.values()) {
			handler.dispose();
		}
		commandFieldHandler.clear();
	}

	/**
	 * Creates a new tab item with the given title and connector.
	 *
	 * @param title The tab title. Must not be <code>null</code>.
	 * @param encoding The terminal encoding or <code>null</code>.
	 * @param connector The terminal connector. Must not be <code>null</code>.
	 * @param data The custom terminal data node or <code>null</code>.
	 * @param flags The flags controlling how the console is opened or <code>null</code> to use defaults.
	 *
	 * @return The created tab item or <code>null</code> if failed.
	 */
	@SuppressWarnings({ "unused", "cast" })
	public CTabItem createTabItem(String title, String encoding, ITerminalConnector connector, Object data, Map<String, Boolean> flags) {
		Assert.isNotNull(title);
		Assert.isNotNull(connector);

		// The result tab item
		CTabItem item = null;

		// Get the tab folder from the parent viewer
		CTabFolder tabFolder = getTabFolder();
		if (tabFolder != null) {
			// Generate a unique title string for the new tab item (must be called before creating the item itself)
			title = makeUniqueTitle(title, tabFolder);
			// Create the tab item
			item = new CTabItem(tabFolder, SWT.CLOSE);
			// Set the tab item title
			item.setText(title);
			// Set the tab icon
			Image image = getTabItemImage(connector, data);
			if (image != null) item.setImage(image);

			// Setup the tab item listeners
			setupTerminalTabListeners(item);

			// Create the composite to create the terminal control within
			Composite composite = new Composite(tabFolder, SWT.NONE);
			composite.setLayout(new FillLayout());
			// Associate the composite with the tab item
			item.setControl(composite);

			// Refresh the layout
			tabFolder.getParent().layout(true);

			// Create the terminal control
			ITerminalViewControl terminal = TerminalViewControlFactory.makeControl(doCreateTerminalTabTerminalListener(this, item), composite, new ITerminalConnector[] { connector }, true);
			if (terminal instanceof ITerminalControl) {
				Object value = flags != null ? flags.get(ITerminalsConnectorConstants.PROP_DATA_NO_RECONNECT) : null;
				boolean noReconnect = value instanceof Boolean ? ((Boolean)value).booleanValue() : false;
				((ITerminalControl)terminal).setConnectOnEnterIfClosed(!noReconnect);
			}

			// Add middle mouse button paste support
			addMiddleMouseButtonPasteSupport(terminal);
			// Add the "selection" listener to the terminal control
			new TerminalControlSelectionListener(terminal);
			// Configure the terminal encoding
			try { terminal.setEncoding(encoding); } catch (UnsupportedEncodingException e) { /* ignored on purpose */ }
			// Associated the terminal with the tab item
			item.setData(terminal);
			// Associated the custom data node with the tab item (if any)
			if (data != null) item.setData("customData", data); //$NON-NLS-1$

			// Overwrite the text canvas help id
			String contextHelpId = getParentView().getContextHelpId();
			if (contextHelpId != null) {
				PlatformUI.getWorkbench().getHelpSystem().setHelp(terminal.getControl(), contextHelpId);
			}

			// Set the context menu
			TabFolderMenuHandler menuHandler = (TabFolderMenuHandler) getParentView().getAdapter(TabFolderMenuHandler.class);
			if (menuHandler != null) {
				Menu menu = (Menu)menuHandler.getAdapter(Menu.class);
				if (menu != null) {
					// One weird occurrence of IllegalArgumentException: Widget has wrong parent.
					// Inspecting the code, this seem extremely unlikely. The terminal is created
					// from a composite parent, the composite parent from the tab folder and the menu
					// from the tab folder. Means, at the end all should have the same menu shell, shouldn't they?
					try {
						terminal.getControl().setMenu(menu);
					} catch (IllegalArgumentException e) {
						// Log exception only if debug mode is set to 1.
						if (UIPlugin.getTraceHandler().isSlotEnabled(1, null)) {
							e.printStackTrace();
						}
					}
				}
			}

			// Select the created item within the tab folder
			tabFolder.setSelection(item);

			// Set the connector
			terminal.setConnector(connector);

			// And connect the terminal
			terminal.connectTerminal();

			// Fire selection changed event
			fireSelectionChanged();
		}

		// Return the create tab item finally.
		return item;
	}

	/**
 	 * Used for DnD of terminal tab items between terminal views
 	 * <p>
	 * Create a new tab item in the "dropped" terminal view using the
	 * information stored in the given item.
	 *
	 * @param oldItem The old dragged tab item. Must not be <code>null</code>.
	 * @return The new dropped tab item.
	 */
	@SuppressWarnings({ "unchecked", "cast" })
    public CTabItem cloneTabItemAfterDrop(CTabItem oldItem) {
		Assert.isNotNull(oldItem);

		ITerminalViewControl terminal = (ITerminalViewControl)oldItem.getData();
		ITerminalConnector connector = terminal.getTerminalConnector();
		Object data = oldItem.getData("customData"); //$NON-NLS-1$
		Map<String, Object> properties = (Map<String, Object>)oldItem.getData("properties"); //$NON-NLS-1$
		String title = oldItem.getText();

		// The result tab item
		CTabItem item = null;

		// Get the tab folder from the parent viewer
		CTabFolder tabFolder = getTabFolder();
		if (tabFolder != null) {
			// Generate a unique title string for the new tab item (must be called before creating the item itself)
			title = makeUniqueTitle(title, tabFolder);
			// Create the tab item
			item = new CTabItem(tabFolder, SWT.CLOSE);
			// Set the tab item title
			item.setText(title);
			// Set the tab icon
			Image image = getTabItemImage(connector, data);
			if (image != null) item.setImage(image);

			// Setup the tab item listeners
			setupTerminalTabListeners(item);
			// Move the terminal listener to the new item
			TabTerminalListener.move(oldItem, item);

			// Create the composite to create the terminal control within
			Composite composite = new Composite(tabFolder, SWT.NONE);
			composite.setLayout(new FillLayout());
			// Associate the composite with the tab item
			item.setControl(composite);

			// Refresh the layout
			tabFolder.getParent().layout(true);

			// Remember terminal state
			TerminalState oldState = terminal.getState();

			// Keep the context menu from being disposed
			terminal.getControl().setMenu(null);

			// change the "parent".
			Assert.isTrue(terminal instanceof ITerminalControl);
			((ITerminalControl)terminal).setupTerminal(composite);

			// Add middle mouse button paste support
			addMiddleMouseButtonPasteSupport(terminal);

			item.setData(terminal);

			// Associate the custom data node with the tab item (if any)
			if (data != null) item.setData("customData", data); //$NON-NLS-1$
			// Associate the properties with the tab item (if any)
			if (properties != null) item.setData("properties", properties); //$NON-NLS-1$

			// Overwrite the text canvas help id
			String contextHelpId = getParentView().getContextHelpId();
			if (contextHelpId != null) {
				PlatformUI.getWorkbench().getHelpSystem().setHelp(terminal.getControl(), contextHelpId);
			}

			// Set the context menu
			TabFolderMenuHandler menuHandler = (TabFolderMenuHandler) getParentView().getAdapter(TabFolderMenuHandler.class);
			if (menuHandler != null) {
				Menu menu = (Menu)menuHandler.getAdapter(Menu.class);
				if (menu != null) {
					// One weird occurrence of IllegalArgumentException: Widget has wrong parent.
					// Inspecting the code, this seem extremely unlikely. The terminal is created
					// from a composite parent, the composite parent from the tab folder and the menu
					// from the tab folder. Means, at the end all should have the same menu shell, shouldn't they?
					try {
						terminal.getControl().setMenu(menu);
					} catch (IllegalArgumentException e) {
						// Log exception only if debug mode is set to 1.
						if (UIPlugin.getTraceHandler().isSlotEnabled(1, null)) {
							e.printStackTrace();
						}
					}
				}
			}

			// Select the created item within the tab folder
			tabFolder.setSelection(item);

			// Set the connector
			terminal.setConnector(connector);

			// needed to get the focus and cursor
			Assert.isTrue(terminal instanceof ITerminalControl);
			((ITerminalControl)terminal).setState(oldState);

			// Fire selection changed event
			fireSelectionChanged();
		}

		// Return the create tab item finally.
		return item;
	}


	protected void addMiddleMouseButtonPasteSupport(final ITerminalViewControl terminal) {
		terminal.getControl().addMouseListener(new MouseAdapter(){
			@Override
            public void mouseDown(MouseEvent e) {
				// paste when the middle button is clicked
				if (e.button == 2) {
					Clipboard clipboard = terminal.getClipboard();
					if (clipboard.isDisposed()) return;
					int clipboardType = DND.SELECTION_CLIPBOARD;
					if (clipboard.getAvailableTypes(clipboardType).length == 0)
						// use normal clipboard if selection clipboard is not available
						clipboardType = DND.CLIPBOARD;
					String text = (String) clipboard.getContents(TextTransfer.getInstance(), clipboardType);
					if (text != null && text.length() > 0)
						terminal.pasteString(text);
				}
			}
		});
    }

	/**
	 * Generate a unique title string based on the given proposal.
	 *
	 * @param proposal The proposal. Must not be <code>null</code>.
	 * @return The unique title string.
	 */
	protected String makeUniqueTitle(String proposal, CTabFolder tabFolder) {
		Assert.isNotNull(proposal);
		Assert.isNotNull(tabFolder);

		String title = proposal;
		int index = 0;

		// Loop all existing tab items and check the titles. We have to remember
		// all found titles as modifying the proposal might in turn conflict again
		// with the title of a tab already checked.
		List<String> titles = new ArrayList<String>();
		for (CTabItem item : tabFolder.getItems()) {
			// Get the tab item title
			titles.add(item.getText());
		}
		// Make the proposal unique be appending (<n>) against all known titles.
		while (titles.contains(title)) title = proposal + " (" + ++index + ")"; //$NON-NLS-1$ //$NON-NLS-2$

		return title;
	}

	/**
	 * Setup the terminal console tab item listeners.
	 *
	 * @param item The tab item. Must not be <code>null</code>.
	 */
	protected void setupTerminalTabListeners(final CTabItem item) {
		Assert.isNotNull(item);

		// Create and associate the disposal listener
		DisposeListener disposeListener = doCreateTerminalTabDisposeListener(this);

		// store the listener to make access easier e.g. needed in DnD
		item.setData("disposeListener", disposeListener); //$NON-NLS-1$
		item.addDisposeListener(disposeListener);
	}

	/**
	 * Creates a new terminal console tab terminal listener instance.
	 *
	 * @param tabFolderManager The tab folder manager. Must not be <code>null</code>.
	 * @param item The tab item. Must not be <code>null</code>.
	 *
	 * @return The terminal listener instance.
	 */
	protected ITerminalListener doCreateTerminalTabTerminalListener(TabFolderManager tabFolderManager, CTabItem item) {
		Assert.isNotNull(item);
		return new TabTerminalListener(tabFolderManager, item);
	}

	/**
	 * Creates a new terminal console tab dispose listener instance.
	 *
	 * @param parent The parent terminal console tab folder manager. Must not be <code>null</code>.
	 * @return The dispose listener instance.
	 */
	protected DisposeListener doCreateTerminalTabDisposeListener(TabFolderManager parent) {
		Assert.isNotNull(parent);
		return new TabDisposeListener(parent);
	}

	/**
	 * Returns the tab item image.
	 *
	 * @param connector The terminal connector. Must not be <code>null</code>.
	 * @param data The custom terminal data node or <code>null</code>.
	 *
	 * @return The tab item image or <code>null</code>.
	 */
	protected Image getTabItemImage(ITerminalConnector connector, Object data) {
		Assert.isNotNull(connector);
		return UIPlugin.getImage(ImageConsts.VIEW_Terminals);
	}

	/**
	 * Lookup a tab item with the given title and the given terminal connector.
	 * <p>
	 * <b>Note:</b> The method will handle unified tab item titles itself.
	 *
	 * @param title The tab item title. Must not be <code>null</code>.
	 * @param connector The terminal connector. Must not be <code>null</code>.
	 * @param data The custom terminal data node or <code>null</code>.
	 *
	 * @return The corresponding tab item or <code>null</code>.
	 */
	public CTabItem findTabItem(String title, ITerminalConnector connector, Object data) {
		Assert.isNotNull(title);
		Assert.isNotNull(connector);

		// Get the tab folder
		CTabFolder tabFolder = getTabFolder();
		if (tabFolder == null) return null;

		// Loop all existing tab items and try to find a matching title
		for (CTabItem item : tabFolder.getItems()) {
			// Disposed items cannot be matched
			if (item.isDisposed()) continue;
			// Get the title from the current tab item
			String itemTitle = item.getText();
			// The terminal console state might be signaled to the user via the
			// terminal console tab title. Filter out any prefix "<.*>\s*".
			itemTitle = itemTitle.replaceFirst("^<.*>\\s*", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (itemTitle.startsWith(title)) {
				// The title string matches -> double check with the terminal connector
				ITerminalViewControl terminal = (ITerminalViewControl)item.getData();
				ITerminalConnector connector2 = terminal.getTerminalConnector();
				// If the connector id and name matches -> check on the settings
				if (connector.getId().equals(connector2.getId()) && connector.getName().equals(connector2.getName())) {
					if (!connector.isInitialized()) {
						// an uninitialized connector does not yield a sensible summary
						return item;
					}
					String summary = connector.getSettingsSummary();
					String summary2 = connector2.getSettingsSummary();
					// If we have matching settings -> we've found the matching item
					if (summary.equals(summary2)) return item;
				}
			}
		}

		return null;
	}

	/**
	 * Make the given tab item the active tab and bring the tab to the top.
	 *
	 * @param item The tab item. Must not be <code>null</code>.
	 */
	public void bringToTop(CTabItem item) {
		Assert.isNotNull(item);

		// Get the tab folder
		CTabFolder tabFolder = getTabFolder();
		if (tabFolder == null) return;

		// Set the given tab item as selection to the tab folder
		tabFolder.setSelection(item);
		// Fire selection changed event
		fireSelectionChanged();
	}

	/**
	 * Returns the currently active tab.
	 *
	 * @return The active tab item or <code>null</code> if none.
	 */
	public CTabItem getActiveTabItem() {
		// Get the tab folder
		CTabFolder tabFolder = getTabFolder();
		if (tabFolder == null) return null;

		return tabFolder.getSelection();
	}

	/**
	 * Remove all terminated tab items.
	 */
	public void removeTerminatedItems() {
		// Get the tab folder
		CTabFolder tabFolder = getTabFolder();
		if (tabFolder == null) return;

		// Loop the items and check for terminated status
		for (CTabItem item: tabFolder.getItems()) {
			// Disposed items cannot be matched
			if (item.isDisposed()) continue;
			// Check if the item is terminated
			if (isTerminatedTabItem(item)) {
				// item is terminated -> dispose
				item.dispose();
			}
		}
	}

	/**
	 * Checks if the given tab item represents a terminated console. Subclasses may
	 * overwrite this method to extend the definition of terminated.
	 *
	 * @param item The tab item or <code>null</code>.
	 * @return <code>True</code> if the tab item represents a terminated console, <code>false</code> otherwise.
	 */
	protected boolean isTerminatedTabItem(CTabItem item) {
		// Null items or disposed items cannot be matched
		if (item == null || item.isDisposed()) return false;

		// First, match the item title. If it contains "<terminated>", the item can be removed
		String itemTitle = item.getText();
		if (itemTitle != null && itemTitle.contains("<terminated>")) { //$NON-NLS-1$
			return true;
		}
		// Second, check if the associated terminal control is closed
		// The title string matches -> double check with the terminal connector
		ITerminalViewControl terminal = (ITerminalViewControl)item.getData();
		if (terminal != null && terminal.getState() == TerminalState.CLOSED) {
			return true;
		}

		return false;
	}

	/**
	 * Returns the command input field handler for the given tab item.
	 *
	 * @param item The tab item or <code>null</code>.
	 * @return The command input field handler or <code>null</code>.
	 */
	public final TabCommandFieldHandler getTabCommandFieldHandler(CTabItem item) {
		// Null items or disposed items cannot be matched
		if (item == null || item.isDisposed()) return null;

		TabCommandFieldHandler handler = commandFieldHandler.get(item);
		if (handler == null) {
			handler = createTabCommandFieldHandler(this, item);
			Assert.isNotNull(handler);
			commandFieldHandler.put(item, handler);
		}
		return handler;
	}

	/**
	 * Create the command input field handler for the given tab item.
	 *
	 * @param tabFolderManager The parent tab folder manager. Must not be <code>null</code>
	 * @param item The associated tab item. Must not be <code>null</code>.
	 *
	 * @return The command input field handler. Must not be <code>null</code>.
	 */
	protected TabCommandFieldHandler createTabCommandFieldHandler(TabFolderManager tabFolderManager, CTabItem item) {
		return new TabCommandFieldHandler(tabFolderManager, item);
	}

	/**
	 * Dispose the command input field handler for the given tab item.
	 *
	 * @param item The tab item or <code>null</code>.
	 */
	protected void disposeTabCommandFieldHandler(CTabItem item) {
		// Null items or disposed items cannot be matched
		if (item == null || item.isDisposed()) return;

		TabCommandFieldHandler handler = commandFieldHandler.remove(item);
		if (handler != null) handler.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (listener != null && !selectionChangedListeners.contains(listener)) selectionChangedListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		if (listener != null) selectionChangedListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	@Override
	public ISelection getSelection() {
		CTabItem activeTabItem = getActiveTabItem();
		return activeTabItem != null ? new StructuredSelection(activeTabItem) : new StructuredSelection();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			// The first selection element which is a CTabItem will become the active item
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				Object candidate = iterator.next();
				if (candidate instanceof CTabItem) { bringToTop((CTabItem)candidate); return; }
			}
		}
		// fire a changed event in any case
		fireSelectionChanged(selection);
	}

	/**
	 * Fire the selection changed event to the registered listeners.
	 */
	protected void fireSelectionChanged() {
		updateStatusLine();
		fireSelectionChanged(getSelection());
	}

	/**
	 * Fire the selection changed event to the registered listeners.
	 */
	protected final void fireSelectionChanged(ISelection selection) {
		// Create the selection changed event
		SelectionChangedEvent event = new SelectionChangedEvent(TabFolderManager.this, selection);

		// First, invoke the registered listeners and let them do their job
		for (ISelectionChangedListener listener : selectionChangedListeners) {
			listener.selectionChanged(event);
		}
	}

	/**
	 * Update the parent view status line.
	 */
	public final void updateStatusLine() {
		String message = null;
		IStatusLineManager manager = parentView.getViewSite().getActionBars().getStatusLineManager();

		CTabItem item = getActiveTabItem();
		if (item != null && !item.isDisposed()) {
			ITerminalViewControl terminal = (ITerminalViewControl)item.getData();
			if (terminal != null && !terminal.isDisposed()) {
				StringBuilder buffer = new StringBuilder();

				buffer.append(state2msg(item, terminal.getState()));
				buffer.append(" - "); //$NON-NLS-1$

				String encoding = terminal.getEncoding();
				if (encoding == null || "ISO-8859-1".equals(encoding)) { //$NON-NLS-1$
					encoding = "Default (ISO-8859-1)"; //$NON-NLS-1$
				}
				buffer.append(NLS.bind(Messages.TabFolderManager_encoding, encoding));

				message = buffer.toString();
			}
		}

		manager.setMessage(message);
	}

	/**
	 * Returns the string representation of the given terminal state.
	 *
	 * @param item The tab folder item. Must not be <code>null</code>.
	 * @param state The terminal state. Must not be <code>null</code>.
	 *
	 * @return The string representation.
	 */
	@SuppressWarnings("unchecked")
    protected String state2msg(CTabItem item, TerminalState state) {
		Assert.isNotNull(item);
		Assert.isNotNull(state);

		// Determine the terminal properties of the tab folder
		Map<String, Object> properties = (Map<String, Object>)item.getData("properties"); //$NON-NLS-1$

		// Get he current terminal state as string
		String stateStr = state.toString();
		// Lookup a matching text representation of the state
		String key = "TabFolderManager_state_" + stateStr.replaceAll("\\.", " ").trim().toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String stateMsg = null;
		if (properties != null) stateMsg = properties.get(key) instanceof String ? (String) properties.get(key) : null;
		if (stateMsg == null) stateMsg = Messages.getString(key);
		if (stateMsg == null) stateMsg = stateStr;

		return stateMsg;
	}
}
