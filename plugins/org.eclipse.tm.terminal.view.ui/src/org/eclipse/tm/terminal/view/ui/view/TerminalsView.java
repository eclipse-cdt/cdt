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
package org.eclipse.tm.terminal.view.ui.view;

import java.util.Iterator;
import java.util.UUID;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView;
import org.eclipse.tm.terminal.view.ui.nls.Messages;
import org.eclipse.tm.terminal.view.ui.tabs.TabFolderManager;
import org.eclipse.tm.terminal.view.ui.tabs.TabFolderMenuHandler;
import org.eclipse.tm.terminal.view.ui.tabs.TabFolderToolbarHandler;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

/**
 * Terminals view.
 */
public class TerminalsView extends ViewPart implements ITerminalsView, IShowInTarget {

	// Reference to the main page book control
	private PageBook pageBookControl;
	// Reference to the tab folder maintaining the consoles
	/* default */ CTabFolder tabFolderControl;
	// Reference to the tab folder manager
	/* default */ TabFolderManager tabFolderManager;
	// Reference to the tab folder menu handler
	private TabFolderMenuHandler tabFolderMenuHandler;
	// Reference to the tab folder toolbar handler
	private TabFolderToolbarHandler tabFolderToolbarHandler;
	// Reference to the empty page control (to be show if no console is open)
	private Control emptyPageControl;
	// Whether this terminal is pinned.
	private boolean pinned = false;
	// The view's memento handler
	private final TerminalsViewMementoHandler mementoHandler = new TerminalsViewMementoHandler();

	/**
	 * "dummy" transfer just to store the information needed for the DnD
	 *
	 */
	private static class TerminalTransfer extends ByteArrayTransfer {
		// The static terminal transfer type name. Unique per terminals view instance.
		private static final String TYPE_NAME = "terminal-transfer-format:" + UUID.randomUUID().toString(); //$NON-NLS-1$
		// Register the type name and remember the associated unique type id.
		private static final int TYPEID = registerType(TYPE_NAME);

		private CTabItem draggedFolderItem;
		private TabFolderManager draggedTabFolderManager;

		/*
		 * Thread save singleton instance creation.
		 */
		private static class LazyInstanceHolder {
			public static TerminalTransfer instance = new TerminalTransfer();
		}

		/**
		 * Constructor.
		 */
		TerminalTransfer() {
		}

		/**
		 * Returns the singleton terminal transfer instance.
		 * @return
		 */
		public static TerminalTransfer getInstance() {
			return LazyInstanceHolder.instance;
		}

		/**
		 * Sets the dragged folder item.
		 *
		 * @param tabFolderItem The dragged folder item or <code>null</code>.
		 */
		public void setDraggedFolderItem(CTabItem tabFolderItem) {
			draggedFolderItem = tabFolderItem;
		}

		/**
		 * Returns the dragged folder item.
		 *
		 * @return The dragged folder item or <code>null</code>.
		 */
		public CTabItem getDraggedFolderItem() {
			return draggedFolderItem;
		}

		/**
		 * Sets the tab folder manager the associated folder item is dragged from.
		 *
		 * @param tabFolderManager The tab folder manager or <code>null</code>.
		 */
		public void setTabFolderManager(TabFolderManager tabFolderManager) {
			draggedTabFolderManager = tabFolderManager;
		}

		/**
		 * Returns the tab folder manager the associated folder item is dragged from.
		 *
		 * @return The tab folder manager or <code>null</code>.
		 */
		public TabFolderManager getTabFolderManager() {
			return draggedTabFolderManager;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
		 */
		@Override
		protected int[] getTypeIds() {
			return new int[] { TYPEID };
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
		 */
		@Override
		protected String[] getTypeNames() {
			return new String[] { TYPE_NAME };
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object, org.eclipse.swt.dnd.TransferData)
		 */
		@Override
		public void javaToNative(Object data, TransferData transferData) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(org.eclipse.swt.dnd.TransferData)
		 */
		@Override
		public Object nativeToJava(TransferData transferData) {
			return null;
		}
	}

	/**
	 * Constructor.
	 */
	public TerminalsView() {
		super();
	}

	/**
	 * Initialize the drag support.
	 */
	private void addDragSupport() {
		// The event listener is registered as filter. It will receive events from all widgets.
		PlatformUI.getWorkbench().getDisplay().addFilter(SWT.DragDetect, new Listener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			@Override
			public void handleEvent(Event event) {
				// Only handle events where a CTabFolder is the source
				if (!(event.widget instanceof CTabFolder)) return;
				// TabFolderManager must be created
				if (tabFolderManager == null) return;

				// only for own tab folders
				if (event.widget != tabFolderControl) return;

				// Skip drag if DnD is still ongoing (bug 443787)
				if (tabFolderControl.getData(DND.DRAG_SOURCE_KEY) != null) return;

				final CTabFolder draggedFolder = (CTabFolder) event.widget;

				int operations = DND.DROP_MOVE | DND.DROP_DEFAULT;
				final DragSource dragSource = new DragSource(draggedFolder, operations);

				// Initialize the terminal transfer type data
				TerminalTransfer.getInstance().setDraggedFolderItem(tabFolderManager.getActiveTabItem());
				TerminalTransfer.getInstance().setTabFolderManager(tabFolderManager);

				Transfer[] transferTypes = new Transfer[] { TerminalTransfer.getInstance() };
				dragSource.setTransfer(transferTypes);

				// Add a drag source listener to cleanup after the drag operation finished
				dragSource.addDragListener(new DragSourceListener() {
					@Override
					public void dragStart(DragSourceEvent event) {
					}

					@Override
					public void dragSetData(DragSourceEvent event) {
					}

					@Override
					public void dragFinished(DragSourceEvent event) {
						// dispose this drag-source-listener by disposing its drag-source
						dragSource.dispose();

						// Inhibit the action of CTabFolder's default DragDetect-listeners,
						// fire a mouse-click event on the widget that was dragged.
						draggedFolder.notifyListeners(SWT.MouseUp, null);
					}
				});
			}
		});
	}

	/**
	 * Initialize the drop support on the terminals page book control.
	 */
	private void addDropSupport() {
		int operations = DND.DROP_MOVE | DND.DROP_DEFAULT;
		final DropTarget target = new DropTarget(pageBookControl, operations);

		Transfer[] transferTypes = new Transfer[] { TerminalTransfer.getInstance() };
		target.setTransfer(transferTypes);

		target.addDropListener(new DropTargetListener() {
			@Override
			public void dragEnter(DropTargetEvent event) {
				// only if the drop target is different then the drag source
				if (TerminalTransfer.getInstance().getTabFolderManager() == tabFolderManager) {
					event.detail = DND.DROP_NONE;
				}
				else {
					event.detail = DND.DROP_MOVE;
				}
			}

			@Override
			public void dragOver(DropTargetEvent event) {
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
			}

			@Override
			public void dragLeave(DropTargetEvent event) {
			}

			@Override
			public void dropAccept(DropTargetEvent event) {
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (TerminalTransfer.getInstance().getDraggedFolderItem() != null && tabFolderManager != null) {
					CTabItem draggedItem = TerminalTransfer.getInstance().getDraggedFolderItem();

					CTabItem item = tabFolderManager.cloneTabItemAfterDrop(draggedItem);
					tabFolderManager.bringToTop(item);
					switchToTabFolderControl();

					// dispose tab item control
					final Control control = draggedItem.getControl();
					draggedItem.setControl(null);
					if (control != null) control.dispose();

					// need to remove the dispose listener first
					DisposeListener disposeListener = (DisposeListener) draggedItem.getData("disposeListener"); //$NON-NLS-1$
					draggedItem.removeDisposeListener(disposeListener);
					draggedItem.dispose();

					// make sure the "new" terminals view has the focus after dragging a terminal
					setFocus();
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		// Dispose the tab folder manager
		if (tabFolderManager != null) {
			tabFolderManager.dispose();
			tabFolderManager = null;
		}
		// Dispose the tab folder menu handler
		if (tabFolderMenuHandler != null) {
			tabFolderMenuHandler.dispose();
			tabFolderMenuHandler = null;
		}
		// Dispose the tab folder toolbar handler
		if (tabFolderToolbarHandler != null) {
			tabFolderToolbarHandler.dispose();
			tabFolderToolbarHandler = null;
		}

		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
	    super.init(site, memento);
	    restoreState(memento);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		// Create the page book control
		pageBookControl = doCreatePageBookControl(parent);
		Assert.isNotNull(pageBookControl);
		// Configure the page book control
		doConfigurePageBookControl(pageBookControl);

		// Create the empty page control
		emptyPageControl = doCreateEmptyPageControl(pageBookControl);
		Assert.isNotNull(emptyPageControl);
		// Configure the empty page control
		doConfigureEmptyPageControl(emptyPageControl);

		// Create the tab folder control (empty)
		tabFolderControl = doCreateTabFolderControl(pageBookControl);
		Assert.isNotNull(tabFolderControl);
		// Configure the tab folder control
		doConfigureTabFolderControl(tabFolderControl);

		// Create the tab folder manager
		tabFolderManager = doCreateTabFolderManager(this);
		Assert.isNotNull(tabFolderManager);
		// Set the tab folder manager as the selection provider
		getSite().setSelectionProvider(tabFolderManager);

		// Setup the tab folder menu handler
		tabFolderMenuHandler = doCreateTabFolderMenuHandler(this);
		Assert.isNotNull(tabFolderMenuHandler);
		doConfigureTabFolderMenuHandler(tabFolderMenuHandler);

		// Setup the tab folder toolbar handler
		tabFolderToolbarHandler = doCreateTabFolderToolbarHandler(this);
		Assert.isNotNull(tabFolderToolbarHandler);
		doConfigureTabFolderToolbarHandler(tabFolderToolbarHandler);

		// Show the empty page control by default
		switchToEmptyPageControl();

		String secondaryId = ((IViewSite) getSite()).getSecondaryId();
		if (secondaryId != null) {
			String defaultTitle = getPartName();
			// set title
			setPartName(defaultTitle + " " + secondaryId); //$NON-NLS-1$
		}

		// Initialize DnD support
		addDragSupport();
		addDropSupport();
	}

	/**
	 * Creates the {@link PageBook} instance.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @return The page book instance. Must never be <code>null</code>.
	 */
	protected PageBook doCreatePageBookControl(Composite parent) {
		return new PageBook(parent, SWT.NONE);
	}

	/**
	 * Configure the given page book control.
	 *
	 * @param pagebook The page book control. Must not be <code>null</code>.
	 */
	protected void doConfigurePageBookControl(PageBook pagebook) {
		Assert.isNotNull(pagebook);

		if (getContextHelpId() != null) PlatformUI.getWorkbench().getHelpSystem().setHelp(pagebook, getContextHelpId());
	}

	/**
	 * Returns the context help id associated with the terminals console view instance.
	 * <p>
	 * <b>Note:</b> The default implementation returns the view id as context help id.
	 *
	 * @return The context help id or <code>null</code> if none is associated.
	 */
	@Override
	public String getContextHelpId() {
		return getViewSite().getId();
	}

	/**
	 * Creates the empty page control instance.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @return The empty page control instance. Must never be <code>null</code>.
	 */
	protected Control doCreateEmptyPageControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		return composite;
	}

	/**
	 * Configures the empty page control.
	 *
	 * @param control The empty page control. Must not be <code>null</code>.
	 */
	protected void doConfigureEmptyPageControl(Control control) {
		Assert.isNotNull(control);
	}

	/**
	 * Creates the tab folder control instance.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @return The tab folder control instance. Must never be <code>null</code>.
	 */
	protected CTabFolder doCreateTabFolderControl(Composite parent) {
		return new CTabFolder(parent, SWT.NO_REDRAW_RESIZE | SWT.NO_TRIM | SWT.FLAT | SWT.BORDER);
	}

	/**
	 * Configures the tab folder control.
	 *
	 * @param tabFolder The tab folder control. Must not be <code>null</code>.
	 */
	protected void doConfigureTabFolderControl(final CTabFolder tabFolder) {
		Assert.isNotNull(tabFolder);

		// Set the layout data
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Set the tab gradient coloring from the global preferences
		if (useGradientTabBackgroundColor()) {
			tabFolder.setSelectionBackground(new Color[] {
													JFaceResources.getColorRegistry().get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_START"), //$NON-NLS-1$
													JFaceResources.getColorRegistry().get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_END") //$NON-NLS-1$
												}, new int[] { 100 }, true);
		}
		// Apply the tab folder selection foreground color
		tabFolder.setSelectionForeground(JFaceResources.getColorRegistry().get("org.eclipse.ui.workbench.ACTIVE_TAB_TEXT_COLOR")); //$NON-NLS-1$

		// Set the tab style from the global preferences
		tabFolder.setSimple(PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));

		// Attach the mouse listener
		tabFolder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 2) {
					// middle mouse button click - close tab
					CTabItem item = tabFolder.getItem(new Point(e.x, e.y));
					if (item != null) item.dispose();
				}
			}
		});
	}

	/**
	 * If <code>True</code> is returned, the inner tabs are colored with gradient coloring set in
	 * the Eclipse workbench color settings.
	 *
	 * @return <code>True</code> to use gradient tab colors, <code>false</code> otherwise.
	 */
	protected boolean useGradientTabBackgroundColor() {
		return false;
	}

	/**
	 * Creates the tab folder manager.
	 *
	 * @param parentView The parent view instance. Must not be <code>null</code>.
	 * @return The tab folder manager. Must never be <code>null</code>.
	 */
	protected TabFolderManager doCreateTabFolderManager(ITerminalsView parentView) {
		Assert.isNotNull(parentView);
		return new TabFolderManager(parentView);
	}

	/**
	 * Creates the tab folder menu handler.
	 *
	 * @param parentView The parent view instance. Must not be <code>null</code>.
	 * @return The tab folder menu handler. Must never be <code>null</code>.
	 */
	protected TabFolderMenuHandler doCreateTabFolderMenuHandler(ITerminalsView parentView) {
		Assert.isNotNull(parentView);
		return new TabFolderMenuHandler(parentView);
	}

	/**
	 * Configure the tab folder menu handler
	 *
	 * @param menuHandler The tab folder menu handler. Must not be <code>null</code>.
	 */
	protected void doConfigureTabFolderMenuHandler(TabFolderMenuHandler menuHandler) {
		Assert.isNotNull(menuHandler);
		menuHandler.initialize();
	}

	/**
	 * Creates the tab folder toolbar handler.
	 *
	 * @param parentView The parent view instance. Must not be <code>null</code>.
	 * @return The tab folder toolbar handler. Must never be <code>null</code>.
	 */
	protected TabFolderToolbarHandler doCreateTabFolderToolbarHandler(ITerminalsView parentView) {
		Assert.isNotNull(parentView);
		return new TabFolderToolbarHandler(parentView);
	}

	/**
	 * Configure the tab folder toolbar handler
	 *
	 * @param toolbarHandler The tab folder toolbar handler. Must not be <code>null</code>.
	 */
	protected void doConfigureTabFolderToolbarHandler(TabFolderToolbarHandler toolbarHandler) {
		Assert.isNotNull(toolbarHandler);
		toolbarHandler.initialize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (pageBookControl != null) pageBookControl.setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView#switchToEmptyPageControl()
	 */
	@Override
	public void switchToEmptyPageControl() {
		if (pageBookControl != null && !pageBookControl.isDisposed()
						&& emptyPageControl != null && !emptyPageControl.isDisposed()) {
			pageBookControl.showPage(emptyPageControl);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView#switchToTabFolderControl()
	 */
	@Override
	public void switchToTabFolderControl() {
		if (pageBookControl != null && !pageBookControl.isDisposed()
						&& tabFolderControl != null && !tabFolderControl.isDisposed()) {
			pageBookControl.showPage(tabFolderControl);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (CTabFolder.class.isAssignableFrom(adapter)) {
			return tabFolderControl;
		}
		if (TabFolderManager.class.isAssignableFrom(adapter)) {
			return tabFolderManager;
		}
		if (TabFolderMenuHandler.class.isAssignableFrom(adapter)) {
			return tabFolderMenuHandler;
		}
		if (TabFolderToolbarHandler.class.isAssignableFrom(adapter)) {
			return tabFolderToolbarHandler;
		}
		if (TerminalsViewMementoHandler.class.isAssignableFrom(adapter)) {
			return mementoHandler;
		}

		return super.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView#setPinned(boolean)
	 */
	@Override
	public void setPinned(boolean pin) {
		this.pinned = pin;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView#isPinned()
	 */
	@Override
	public boolean isPinned() {
		return pinned;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
	    if (memento == null) return;
	    mementoHandler.saveState(this, memento);
	}

	/**
	 * Restore the view state from the given memento.
	 *
	 * @param memento The memento or <code>null</code>.
	 */
	public void restoreState(IMemento memento) {
		if (memento == null) return;
		mementoHandler.restoreState(this, memento);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IShowInTarget#show(org.eclipse.ui.part.ShowInContext)
	 */
	@Override
	public boolean show(ShowInContext context) {
		if (context != null) {
			// Get the selection from the context
			ISelection selection = context.getSelection();
			// The selection must contain elements that can be adapted to IResource
			if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
				boolean isValid = true;

				Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
				while (iterator.hasNext() && isValid) {
					Object element = iterator.next();
					Object adapted = null;

					Bundle bundle = Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
					if (bundle != null && bundle.getState() != Bundle.UNINSTALLED && bundle.getState() != Bundle.STOPPING) {
						if (element instanceof org.eclipse.core.resources.IResource) continue;

						adapted = element instanceof IAdaptable ? ((IAdaptable)element).getAdapter(org.eclipse.core.resources.IResource.class) : null;
						if (adapted == null) adapted = Platform.getAdapterManager().getAdapter(element, org.eclipse.core.resources.IResource.class);
						if (adapted == null) adapted = Platform.getAdapterManager().loadAdapter(element, org.eclipse.core.resources.IResource.class.getName());
					}

					isValid = adapted != null;
				}

				// If the selection is valid, fire the command to open the local terminal
				if (isValid) {
					ICommandService service = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
					Command command = service != null ? service.getCommand("org.eclipse.tm.terminal.connector.local.command.launch") : null; //$NON-NLS-1$
					if (command != null && command.isDefined() && command.isEnabled()) {
						try {
							ParameterizedCommand pCmd = ParameterizedCommand.generateCommand(command, null);
							Assert.isNotNull(pCmd);
							IHandlerService handlerSvc = (IHandlerService)PlatformUI.getWorkbench().getService(IHandlerService.class);
							Assert.isNotNull(handlerSvc);
							IEvaluationContext ctx = handlerSvc.getCurrentState();
							ctx = new EvaluationContext(ctx, selection);
							ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
							handlerSvc.executeCommandInContext(pCmd, null, ctx);
						} catch (Exception e) {
							// If the platform is in debug mode, we print the exception to the log view
							if (Platform.inDebugMode()) {
								IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
															Messages.AbstractTriggerCommandHandler_error_executionFailed, e);
								UIPlugin.getDefault().getLog().log(status);
							}
						}
					}
					return true;
				}
			}
		}
	    return false;
	}
}
