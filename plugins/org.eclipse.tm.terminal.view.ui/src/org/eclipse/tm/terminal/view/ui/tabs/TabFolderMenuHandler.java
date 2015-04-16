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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionClearAll;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionCopy;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionPaste;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionSelectAll;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.actions.SelectEncodingAction;
import org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Terminals tab folder menu handler.
 */
@SuppressWarnings("restriction")
public class TabFolderMenuHandler extends PlatformObject {
	// Reference to the parent terminals console view
	private final ITerminalsView parentView;
	// Reference to the tab folder context menu manager
	private MenuManager contextMenuManager;
	// Reference to the tab folder context menu
	private Menu contextMenu;
	// The list of actions available within the context menu
	private final List<AbstractTerminalAction> contextMenuActions = new ArrayList<AbstractTerminalAction>();

	// The list of invalid context menu contributions "startsWith" expressions
	/* default */ static final String[] INVALID_CONTRIBUTIONS_STARTS_WITH = {
		"org.eclipse.cdt", "org.eclipse.ui.edit" //$NON-NLS-1$ //$NON-NLS-2$
	};

	/**
	 * Default menu listener implementation.
	 */
	protected class MenuListener implements IMenuListener2 {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.IMenuListener2#menuAboutToHide(org.eclipse.jface.action.IMenuManager)
		 */
		@Override
		public void menuAboutToHide(IMenuManager manager) {
			// CQ:WIND00192293 and CQ:WIND194204 - don't update actions on menuAboutToHide
			// See also http://bugs.eclipse.org/296212
			//			updateMenuItems(false);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
		 */
		@Override
		public void menuAboutToShow(IMenuManager manager) {
			removeInvalidContributions(manager);
			updateMenuItems(true);
		}

		/**
		 * Bug 392249: Remove contributions that appear in the context in Eclipse 4.x which are
		 * not visible in Eclipse 3.8.x. Re-evaluate from time to time!
		 *
		 * @param manager The menu manager or <code>null</code>
		 */
		private void removeInvalidContributions(IMenuManager manager) {
			if (manager == null) return;

			IContributionItem[] items = manager.getItems();
			for (IContributionItem item : items) {
				String id = item.getId();
				if (id != null) {
					for (String prefix : INVALID_CONTRIBUTIONS_STARTS_WITH) {
						if (id.startsWith(prefix)) {
							manager.remove(item);
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Constructor.
	 *
	 * @param parentView The parent terminals console view. Must not be <code>null</code>.
	 */
	public TabFolderMenuHandler(ITerminalsView parentView) {
		super();
		Assert.isNotNull(parentView);
		this.parentView = parentView;
	}

	/**
	 * Returns the parent terminals console view.
	 *
	 * @return The parent terminals console view instance.
	 */
	protected final ITerminalsView getParentView() {
		return parentView;
	}

	/**
	 * Returns the tab folder associated with the parent view.
	 *
	 * @return The tab folder or <code>null</code>.
	 */
	protected final CTabFolder getTabFolder() {
		return (CTabFolder)getParentView().getAdapter(CTabFolder.class);
	}

	/**
	 * Dispose the tab folder menu handler instance.
	 */
	public void dispose() {
		// Dispose the context menu
		if (contextMenu != null) { contextMenu.dispose(); contextMenu = null; }
		// Dispose the context menu manager
		if (contextMenuManager != null) { contextMenuManager.dispose(); contextMenuManager = null; }
		// Clear all actions
		contextMenuActions.clear();
	}

	/**
	 * Setup the context menu for the tab folder. The method will return
	 * immediately if the menu handler had been initialized before.
	 *
	 * @param tabFolder The tab folder control. Must not be <code>null</code>.
	 */
	public void initialize() {
		// Return immediately if the menu manager and menu got initialized already
		if (contextMenuManager != null && contextMenu != null) {
			return;
		}

		// Get the tab folder
		CTabFolder tabFolder = getTabFolder();
		if (tabFolder == null) {
			return;
		}

		// Create the menu manager if not done before
		contextMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$

		// Bug 392249: Register our menu listener after registering the context menu
		//             for contributions. That way we can use our menu listener to get
		//             rid of unwanted/misguided contributions. At least until this is
		//             fixed in the Eclipse 4.x platform.

		// Create the context menu
		contextMenu = contextMenuManager.createContextMenu(tabFolder);

		// Create the context menu action instances
		doCreateContextMenuActions();

		// Fill the context menu
		doFillContextMenu(contextMenuManager);

		// Register to the view site to open the menu for contributions
		getParentView().getSite().registerContextMenu(contextMenuManager, getParentView().getSite().getSelectionProvider());

		// Create and associated the menu listener
		contextMenuManager.addMenuListener(new MenuListener());
	}

	/**
	 * Adds the given action to the context menu actions list.
	 *
	 * @param action The action instance. Must not be <code>null</code>.
	 */
	protected final void add(AbstractTerminalAction action) {
		Assert.isNotNull(action);
		contextMenuActions.add(action);
	}

	/**
	 * Create the context menu actions.
	 */
	protected void doCreateContextMenuActions() {
		// Create and add the copy action
		add(new TerminalActionCopy() {
			/* (non-Javadoc)
			 * @see org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction#getTarget()
			 */
			@Override
			protected ITerminalViewControl getTarget() {
				return getActiveTerminalViewControl();
			}
		});

		// Create and add the paste action
		add(new TerminalActionPaste() {
			@SuppressWarnings("unchecked")
            @Override
			public void run() {
				// Determine if pasting to the active tab require backslash translation
				boolean needsTranslation = false;

				TabFolderManager manager = (TabFolderManager)getParentView().getAdapter(TabFolderManager.class);
				if (manager != null) {
					// If we have the active tab item, we can get the active terminal control
					CTabItem activeTabItem = manager.getActiveTabItem();
					if (activeTabItem != null) {
						Map<String, Object> properties = (Map<String, Object>)activeTabItem.getData("properties"); //$NON-NLS-1$
						if (properties != null && properties.containsKey(ITerminalsConnectorConstants.PROP_TRANSLATE_BACKSLASHES_ON_PASTE)) {
							Object value = properties.get(ITerminalsConnectorConstants.PROP_TRANSLATE_BACKSLASHES_ON_PASTE);
							needsTranslation = value instanceof Boolean ? ((Boolean)value).booleanValue() : false;
						}
					}
				}

				if (needsTranslation) {
					ITerminalViewControl target = getTarget();
					if (target != null && target.getClipboard() != null && !target.getClipboard().isDisposed()) {
						String text = (String) target.getClipboard().getContents(TextTransfer.getInstance());
						if (text != null) {
							text = text.replace('\\', '/');

							Object[] data = new Object[] { text };
							Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
							target.getClipboard().setContents(data, types, DND.CLIPBOARD);
						}
					}
				}

			    super.run();
			}
			/* (non-Javadoc)
			 * @see org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction#getTarget()
			 */
			@Override
			protected ITerminalViewControl getTarget() {
				return getActiveTerminalViewControl();
			}
		});

		// Create and add the clear all action
		add(new TerminalActionClearAll() {
			/* (non-Javadoc)
			 * @see org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction#getTarget()
			 */
			@Override
			protected ITerminalViewControl getTarget() {
				return getActiveTerminalViewControl();
			}

			/* (non-Javadoc)
			 * @see org.eclipse.tm.internal.terminal.control.actions.TerminalActionPaste#updateAction(boolean)
			 */
			@Override
			public void updateAction(boolean aboutToShow) {
			    super.updateAction(aboutToShow);
			    if (getTarget() != null && getTarget().getState() != TerminalState.CONNECTED) {
			    	setEnabled(false);
			    }
			}
		});

		// Create and add the select all action
		add(new TerminalActionSelectAll() {
			/* (non-Javadoc)
			 * @see org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction#getTarget()
			 */
			@Override
			protected ITerminalViewControl getTarget() {
				return getActiveTerminalViewControl();
			}
		});

		// Create and add the select encoding action
		add (new SelectEncodingAction((TabFolderManager)getParentView().getAdapter(TabFolderManager.class)) {
			/* (non-Javadoc)
			 * @see org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction#getTarget()
			 */
			@Override
			protected ITerminalViewControl getTarget() {
				return getActiveTerminalViewControl();
			}
		});
	}

	/**
	 * Returns the currently active terminal control.
	 *
	 * @return The currently active terminal control or <code>null</code>.
	 */
	protected ITerminalViewControl getActiveTerminalViewControl() {
		ITerminalViewControl terminal = null;

		// Get the active tab item from the tab folder manager
		TabFolderManager manager = (TabFolderManager)getParentView().getAdapter(TabFolderManager.class);
		if (manager != null) {
			// If we have the active tab item, we can get the active terminal control
			CTabItem activeTabItem = manager.getActiveTabItem();
			if (activeTabItem != null) {
				terminal = (ITerminalViewControl)activeTabItem.getData();
			}
		}

		return terminal;
	}

	/**
	 * Fill in the context menu content within the given manager.
	 *
	 * @param manager The menu manager. Must not be <code>null</code>.
	 */
	protected void doFillContextMenu(MenuManager manager) {
		Assert.isNotNull(manager);

		// Loop all actions and add them to the menu manager
		for (AbstractTerminalAction action : contextMenuActions) {
			manager.add(action);
			// Add a separator after the paste action
			if (action instanceof TerminalActionPaste) {
				manager.add(new Separator());
			}
			// Add a separator after the select all action
			if (action instanceof TerminalActionSelectAll) {
				manager.add(new Separator());
			}
		}

		// Menu contributions will end up here
		manager.add(new Separator());
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Update the context menu items on showing or hiding the context menu.
	 *
	 * @param aboutToShow <code>True</code> if the menu is about to show, <code>false</code> otherwise.
	 */
	protected void updateMenuItems(boolean aboutToShow) {
		// Loop all actions and update the status
		for (AbstractTerminalAction action : contextMenuActions) {
			action.updateAction(aboutToShow);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (MenuManager.class.isAssignableFrom(adapter)) {
			return contextMenuManager;
		} else if (Menu.class.isAssignableFrom(adapter)) {
			if (contextMenu != null && contextMenu.isDisposed()) {
				// menu got disposed (should not happen)
				contextMenu = contextMenuManager.createContextMenu(getTabFolder());
			}
			return contextMenu;
		}

		// Try the parent view
		Object adapted = getParentView().getAdapter(adapter);
		if (adapted != null) {
			return adapted;
		}

		return super.getAdapter(adapter);
	}
}
