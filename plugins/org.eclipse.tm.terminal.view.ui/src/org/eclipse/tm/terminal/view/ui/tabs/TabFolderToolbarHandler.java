/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [361363] [TERMINALS] Implement "Pin&Clone" for the "Terminals" view
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.tabs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionClearAll;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionCopy;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionPaste;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.view.ui.actions.AbstractAction;
import org.eclipse.tm.terminal.view.ui.actions.PinTerminalAction;
import org.eclipse.tm.terminal.view.ui.actions.TabScrollLockAction;
import org.eclipse.tm.terminal.view.ui.actions.ToggleCommandFieldAction;
import org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;


/**
 * Terminals tab folder toolbar handler.
 */
@SuppressWarnings("restriction")
public class TabFolderToolbarHandler extends PlatformObject {
	// Reference to the parent terminals console view
	private final ITerminalsView parentView;
	// Reference to the toolbar manager
	private IToolBarManager toolbarManager;
	// Reference to the selection listener
	private ToolbarSelectionChangedListener selectionChangedListener;
	// The list of actions available within the toolbar
	private final List<AbstractTerminalAction> toolbarActions = new ArrayList<AbstractTerminalAction>();

	/**
	 * Default selection listener implementation.
	 */
	protected class ToolbarSelectionChangedListener implements ISelectionChangedListener {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			boolean enable = event != null;

			// The VlmConsoleTabFolderManager is listening to the selection changes of the
			// TabFolder and fires selection changed events.
			if (enable && event.getSource() instanceof TabFolderManager) {
				enable = event.getSelection() instanceof StructuredSelection
				&& !event.getSelection().isEmpty()
				&& (((StructuredSelection)event.getSelection()).getFirstElement() instanceof CTabItem
					|| ((StructuredSelection)event.getSelection()).getFirstElement() instanceof String);
			}

			updateToolbarItems(enable);
		}
	}

	/**
	 * Constructor.
	 *
	 * @param parentView The parent terminals console view. Must not be <code>null</code>.
	 */
	public TabFolderToolbarHandler(ITerminalsView parentView) {
		super();
		Assert.isNotNull(parentView);
		this.parentView = parentView;
	}

	/**
	 * Returns the parent terminals console view.
	 *
	 * @return The terminals console view instance.
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
	 * Returns the currently active terminal control.
	 *
	 * @return The currently active terminal control or <code>null</code>.
	 */
	public ITerminalViewControl getActiveTerminalViewControl() {
		ITerminalViewControl terminal = null;

		// Get the active tab item from the tab folder manager
		TabFolderManager manager = (TabFolderManager)getParentView().getAdapter(TabFolderManager.class);
		if (manager != null) {
			// If we have the active tab item, we can get the active terminal control
			CTabItem activeTabItem = manager.getActiveTabItem();
			if (activeTabItem != null && !activeTabItem.isDisposed()) {
				terminal = (ITerminalViewControl)activeTabItem.getData();
			}
		}

		return terminal;
	}

	/**
	 * Dispose the tab folder menu handler instance.
	 */
	public void dispose() {
		// Dispose the selection changed listener
		if (selectionChangedListener != null) {
			getParentView().getViewSite().getSelectionProvider().removeSelectionChangedListener(selectionChangedListener);
			selectionChangedListener = null;
		}

		// Clear all actions
		toolbarActions.clear();
	}

	/**
	 * Setup the context menu for the tab folder. The method will return
	 * immediately if the toolbar handler had been initialized before.
	 *
	 * @param tabFolder The tab folder control. Must not be <code>null</code>.
	 */
	public void initialize() {
		// Return immediately if the toolbar manager got initialized already
		if (toolbarManager != null) {
			return;
		}

		// Register ourself as selection listener to the tab folder
		selectionChangedListener = doCreateSelectionChangedListener();
		Assert.isNotNull(selectionChangedListener);
		getParentView().getViewSite().getSelectionProvider().addSelectionChangedListener(selectionChangedListener);

		// Get the parent view action bars
		IActionBars bars = getParentView().getViewSite().getActionBars();

		// From the action bars, get the toolbar manager
		toolbarManager = bars.getToolBarManager();

		// Create the toolbar action instances
		doCreateToolbarActions();

		// Fill the toolbar
		doFillToolbar(toolbarManager);

		// Update actions
		updateToolbarItems(false);
	}

	/**
	 * Creates a new selection changed listener instance.
	 *
	 * @return The new selection changed listener instance.
	 */
	protected ToolbarSelectionChangedListener doCreateSelectionChangedListener() {
		return new ToolbarSelectionChangedListener();
	}

	/**
	 * Adds the given action to the toolbar actions list.
	 *
	 * @param action The action instance. Must not be <code>null</code>.
	 */
	protected final void add(AbstractTerminalAction action) {
		Assert.isNotNull(action);
		toolbarActions.add(action);
	}

	/**
	 * Removes the given action from the toolbar actions list.
	 *
	 * @param action The action instance. Must not be <code>null</code>.
	 */
	protected final void remove(AbstractTerminalAction action) {
		Assert.isNotNull(action);
		toolbarActions.remove(action);
	}

	/**
	 * Create the toolbar actions.
	 */
	protected void doCreateToolbarActions() {
		// Create and add the paste action
		add(new TerminalActionPaste() {
			/* (non-Javadoc)
			 * @see org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction#getTarget()
			 */
			@Override
			protected ITerminalViewControl getTarget() {
				return getActiveTerminalViewControl();
			}
		});

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

		// Create and add the scroll lock action
		add (new TabScrollLockAction() {
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

		// Create and add the toggle command input field action
		add (new ToggleCommandFieldAction(getParentView()) {
			/* (non-Javadoc)
			 * @see org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction#getTarget()
			 */
			@Override
			protected ITerminalViewControl getTarget() {
				return getActiveTerminalViewControl();
			}
		});

		// Create and add the pin view action
		add (new PinTerminalAction(getParentView()) {
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
	 * Fill in the context menu content within the given manager.
	 *
	 * @param manager The menu manager. Must not be <code>null</code>.
	 */
	protected void doFillToolbar(IToolBarManager manager) {
		Assert.isNotNull(manager);

		// Note: For the toolbar, the actions are added from left to right!
		//       So we start with the additions marker here which is the most
		//       left contribution item.
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator("anchor")); //$NON-NLS-1$

		// we want that at the end
		PinTerminalAction pinAction=null;

		// Loop all actions and add them to the menu manager
		for (AbstractTerminalAction action : toolbarActions) {
			// Add a separator before the clear all action or if the action is a separator
			if (action instanceof TabScrollLockAction
				|| (action instanceof AbstractAction && ((AbstractAction)action).isSeparator())) {
				manager.insertAfter("anchor", new Separator()); //$NON-NLS-1$
			}
			// skip pin action for now
			if(action instanceof PinTerminalAction){
				pinAction=(PinTerminalAction)action;
				continue;
			}
			// Add the action itself
			manager.insertAfter("anchor", action); //$NON-NLS-1$
		}
		// now add pin at the end
		if(pinAction!=null){
			manager.add(pinAction);
		}
	}

	/**
	 * Update the toolbar items.
	 *
	 * @param enabled <code>True</code> if the items shall be enabled, <code>false</code> otherwise.
	 */
	protected void updateToolbarItems(boolean enabled) {
		// Determine the currently active terminal control
		ITerminalViewControl control = getActiveTerminalViewControl();
		// Loop all actions and update the status
		for (AbstractTerminalAction action : toolbarActions) {
			// If the terminal control is not available, the updateAction
			// method of certain actions enable the action (bugzilla #260372).
			// Workaround by forcing the action to get disabled with setEnabled.
			if (control == null && !(action instanceof PinTerminalAction)) {
				action.setEnabled(false);
			}
			else {
				action.updateAction(enabled);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (IToolBarManager.class.isAssignableFrom(adapter)) {
			return toolbarManager;
		}

		// Try the toolbar actions
		for (AbstractTerminalAction action : toolbarActions) {
			if (adapter.isAssignableFrom(action.getClass())) {
				return action;
			}
		}

		// Try the parent view
		Object adapted = getParentView().getAdapter(adapter);
		if (adapted != null) {
			return adapted;
		}

		return super.getAdapter(adapter);
	}
}
