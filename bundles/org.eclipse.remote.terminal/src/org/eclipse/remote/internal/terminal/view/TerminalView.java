/*******************************************************************************
 * Copyright (c) 2003, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [206892] State handling: Only allow connect when CLOSED
 * Michael Scharf (Wind River) - [209656] ClassCastException in TerminalView under Eclipse-3.4M3
 * Michael Scharf (Wind River) - [189774] Ctrl+V does not work in the command input field.
 * Michael Scharf (Wind River) - [217999] Duplicate context menu entries in Terminal
 * Anna Dushistova (MontaVista) - [227537] moved actions from terminal.view to terminal plugin
 * Martin Oberhuber (Wind River) - [168186] Add Terminal User Docs
 * Michael Scharf (Wind River) - [172483] switch between connections
 * Michael Scharf (Wind River) - [240023] Get rid of the terminal's "Pin" button
 * Michael Scharf (Wind River) - [196454] Initial connection settings dialog should not be blank
 * Michael Scharf (Wind River) - [241096] Secondary terminals in same view do not observe the "invert colors" Preference
 * Michael Scharf (Wind River) - [262996] get rid of TerminalState.OPENED
 * Martin Oberhuber (Wind River) - [205486] Enable ScrollLock
 * Ahmet Alptekin (Tubitak) - [244405] Add a UI Control for setting the Terminal's encoding
 * Martin Oberhuber (Wind River) - [378691][api] push Preferences into the Widget
 * Kris De Volder (VMWare) - [392092] Extend ITerminalView API to allow programmatically opening a UI-less connector
 *******************************************************************************/
package org.eclipse.remote.internal.terminal.view;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.remote.internal.terminal.actions.TerminalAction;
import org.eclipse.remote.internal.terminal.actions.TerminalActionConnect;
import org.eclipse.remote.internal.terminal.actions.TerminalActionDisconnect;
import org.eclipse.remote.internal.terminal.actions.TerminalActionNewTerminal;
import org.eclipse.remote.internal.terminal.actions.TerminalActionRemove;
import org.eclipse.remote.internal.terminal.actions.TerminalActionScrollLock;
import org.eclipse.remote.internal.terminal.actions.TerminalActionSelectionDropDown;
import org.eclipse.remote.internal.terminal.actions.TerminalActionSettings;
import org.eclipse.remote.internal.terminal.actions.TerminalActionToggleCommandInputField;
import org.eclipse.remote.internal.terminal.view.ITerminalViewConnectionManager.ITerminalViewConnectionFactory;
import org.eclipse.remote.internal.terminal.view.ITerminalViewConnectionManager.ITerminalViewConnectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tm.internal.terminal.control.ITerminalListener;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.TerminalViewControlFactory;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionClearAll;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionCopy;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionCut;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionPaste;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionSelectAll;
import org.eclipse.tm.internal.terminal.preferences.ITerminalConstants;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.LayeredSettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.PreferenceSettingStore;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class TerminalView extends ViewPart implements ITerminalView, ITerminalViewConnectionListener {
	private static final String PREF_CONNECTORS = "Connectors."; //$NON-NLS-1$

	private static final String STORE_CONNECTION_TYPE = "ConnectionType"; //$NON-NLS-1$

	private static final String STORE_SETTING_SUMMARY = "SettingSummary"; //$NON-NLS-1$

	private static final String STORE_TITLE = "Title"; //$NON-NLS-1$

	public static final String FONT_DEFINITION = ITerminalConstants.FONT_DEFINITION;

	protected ITerminalViewControl fCtlTerminal;

	// TODO (scharf): this decorator is only there to deal wit the common
	// actions. Find a better solution.
	TerminalViewControlDecorator fCtlDecorator = new TerminalViewControlDecorator();

	protected TerminalAction fActionTerminalNewTerminal;

	protected TerminalAction fActionTerminalConnect;

	private TerminalAction fActionTerminalScrollLock;

	protected TerminalAction fActionTerminalDisconnect;

	protected TerminalAction fActionTerminalSettings;

	protected TerminalActionCopy fActionEditCopy;

	protected TerminalActionCut fActionEditCut;

	protected TerminalActionPaste fActionEditPaste;

	protected TerminalActionClearAll fActionEditClearAll;

	protected TerminalActionSelectAll fActionEditSelectAll;

	protected TerminalAction fActionToggleCommandInputField;

	protected TerminalPropertyChangeHandler fPropertyChangeHandler;

	protected Action fActionTerminalDropDown;
	protected Action fActionTerminalRemove;

	protected boolean fMenuAboutToShow;

	private SettingsStore fStore;

	private final ITerminalViewConnectionManager fMultiConnectionManager = new TerminalViewConnectionManager();

	private PageBook fPageBook;

	/**
	 * This listener updates both, the view and the
	 * ITerminalViewConnection.
	 *
	 */
	class TerminalListener implements ITerminalListener {
		volatile ITerminalViewConnection fConnection;

		void setConnection(ITerminalViewConnection connection) {
			fConnection = connection;
		}

		@Override
		public void setState(final TerminalState state) {
			runInDisplayThread(new Runnable() {
				@Override
				public void run() {
					fConnection.setState(state);
					// if the active connection changes, update the view
					if (fConnection == fMultiConnectionManager.getActiveConnection()) {
						updateStatus();
					}
				}
			});
		}

		@Override
		public void setTerminalTitle(final String title) {
			runInDisplayThread(new Runnable() {
				@Override
				public void run() {
					fConnection.setTerminalTitle(title);
					// if the active connection changes, update the view
					if (fConnection == fMultiConnectionManager.getActiveConnection()) {
						updateSummary();
					}
				}
			});
		}

		/**
		 * @param runnable
		 *            run in display thread
		 */
		private void runInDisplayThread(Runnable runnable) {
			if (Display.findDisplay(Thread.currentThread()) != null) {
				runnable.run();
			} else if (PlatformUI.isWorkbenchRunning()) {
				PlatformUI.getWorkbench().getDisplay().syncExec(runnable);
				// else should not happen and we ignore it...
			}
		}

	}

	public TerminalView() {
		fMultiConnectionManager.addListener(this);
	}

	/**
	 * @param title
	 * @return a unique part name
	 */
	String findUniqueTitle(String title) {
		IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
		String id = getViewSite().getId();
		Set<String> names = new HashSet<>();
		for (IWorkbenchPage page : pages) {
			IViewReference[] views = page.getViewReferences();
			for (IViewReference view : views) {
				// only look for views with the same ID
				if (id.equals(view.getId())) {
					String name = view.getTitle();
					if (name != null) {
						names.add(view.getPartName());
					}
				}
			}
		}
		// find a unique name
		int i = 1;
		String uniqueTitle = title;
		while (true) {
			if (!names.contains(uniqueTitle)) {
				return uniqueTitle;
			}
			uniqueTitle = title + " " + i++; //$NON-NLS-1$
		}
	}

	/**
	 * Display a new Terminal view. This method is called when the user clicks the New
	 * Terminal button in any Terminal view's toolbar.
	 */
	@Override
	public void onTerminalNewTerminal() {
		setupControls();
		if (newConnection(ViewMessages.NEW_TERMINAL_CONNECTION) == null) {
			fMultiConnectionManager.removeActive();
		}
	}

	/**
	 * Programmatically create a new terminal connection within the view. This method
	 * does the same thing as onTerminalNewTerminal, but instead of popping up a settings
	 * dialog to allow the user fill in connection details, a connector is provided as
	 * a parameter. The connector should have all of its details pre-configured so it can
	 * be opened without requiring user input.
	 */
	@Override
	public void newTerminal(ITerminalConnector c) {
		this.setupControls();
		if (c != null) {
			this.setConnector(c);
			this.onTerminalConnect();
		}
	}

	@Override
	public void onTerminalNewView() {
		try {
			// The second argument to showView() is a unique String identifying the
			// secondary view instance. If it ever matches a previously used secondary
			// view identifier, then this call will not create a new Terminal view,
			// which is undesirable. Therefore, we append the active time in
			// milliseconds to the secondary view identifier to ensure it is always
			// unique. This code runs only when the user clicks the New Terminal
			// button, so there is no risk that this code will run twice in a single
			// millisecond.
			IViewPart newTerminalView = getSite().getPage().showView("org.eclipse.tm.terminal.view.TerminalView", //$NON-NLS-1$
					"SecondaryTerminal" + System.currentTimeMillis(), //$NON-NLS-1$
					IWorkbenchPage.VIEW_ACTIVATE);
			if (newTerminalView instanceof ITerminalView) {
				ITerminalConnector c = ((TerminalView) newTerminalView).newConnection(ViewMessages.NEW_TERMINAL_VIEW);
				// if there is no connector selected, hide the new view
				if (c == null) {
					getSite().getPage().hideView(newTerminalView);
				}
			}
		} catch (PartInitException ex) {
			TerminalViewPlugin.log(ex);
		}
	}

	@Override
	public void onTerminalConnect() {
		// if (isConnected())
		if (fCtlTerminal.getState() != TerminalState.CLOSED) {
			return;
		}
		if (fCtlTerminal.getTerminalConnector() == null) {
			setConnector(showSettingsDialog(ViewMessages.TERMINALSETTINGS));
		}
		setEncoding(getActiveConnection().getEncoding());
		fCtlTerminal.connectTerminal();
	}

	public void updateStatus() {
		updateTerminalConnect();
		updateTerminalDisconnect();
		updateTerminalSettings();
		fActionToggleCommandInputField.setChecked(hasCommandInputField());
		fActionTerminalScrollLock.setChecked(isScrollLock());
		updateSummary();
	}

	public void updateTerminalConnect() {
		// boolean bEnabled = ((!isConnecting()) && (!fCtlTerminal.isConnected()));
		boolean bEnabled = (fCtlTerminal.getState() == TerminalState.CLOSED);

		fActionTerminalConnect.setEnabled(bEnabled);
	}

	private boolean isConnecting() {
		return fCtlTerminal.getState() == TerminalState.CONNECTING;
	}

	@Override
	public void onTerminalDisconnect() {
		fCtlTerminal.disconnectTerminal();
	}

	public void updateTerminalDisconnect() {
		boolean bEnabled = ((isConnecting()) || (fCtlTerminal.isConnected()));
		fActionTerminalDisconnect.setEnabled(bEnabled);
	}

	@Override
	public void onTerminalSettings() {
		newConnection(null);
	}

	private ITerminalConnector newConnection(String title) {
		ITerminalConnector c = showSettingsDialog(title);
		if (c != null) {
			setConnector(c);
			onTerminalConnect();
		}
		return c;
	}

	private ITerminalConnector showSettingsDialog(String title) {
		// When the settings dialog is opened, load the Terminal settings from the
		// persistent settings.

		ITerminalConnector[] connectors = fCtlTerminal.getConnectors();
		if (fCtlTerminal.getState() != TerminalState.CLOSED) {
			connectors = new ITerminalConnector[0];
		}
		// load the state from the settings
		// first load from fStore and then from the preferences.
		ITerminalConnector c = loadSettings(new LayeredSettingsStore(fStore, getPreferenceSettingsStore()), connectors);
		// if we have no connector show the one from the settings
		if (fCtlTerminal.getTerminalConnector() != null) {
			c = fCtlTerminal.getTerminalConnector();
		}
		TerminalSettingsDialog dlgTerminalSettings = new TerminalSettingsDialog(getViewSite().getShell(), connectors, c);
		dlgTerminalSettings.setTerminalTitle(getActiveConnection().getPartName());
		dlgTerminalSettings.setEncoding(getActiveConnection().getEncoding());
		if (title != null) {
			dlgTerminalSettings.setTitle(title);
		}

		if (dlgTerminalSettings.open() == Window.CANCEL) {
			return null;
		}

		// When the settings dialog is closed, we persist the Terminal settings.
		saveSettings(fStore, dlgTerminalSettings.getConnector());
		// we also save it in the preferences. This will keep the last change
		// made to this connector as default...
		saveSettings(getPreferenceSettingsStore(), dlgTerminalSettings.getConnector());

		setViewTitle(dlgTerminalSettings.getTerminalTitle());
		setEncoding(dlgTerminalSettings.getEncoding());
		return dlgTerminalSettings.getConnector();
	}

	private void setEncoding(String encoding) {
		getActiveConnection().setEncoding(encoding);
		updateSummary();
	}

	private void setConnector(ITerminalConnector connector) {
		fCtlTerminal.setConnector(connector);
	}

	public void updateTerminalSettings() {
		// fActionTerminalSettings.setEnabled((fCtlTerminal.getState()==TerminalState.CLOSED));
	}

	private void setViewTitle(String title) {
		setPartName(title);
		getActiveConnection().setPartName(title);
	}

	private void setViewSummary(String summary) {
		setContentDescription(summary);
		getViewSite().getActionBars().getStatusLineManager().setMessage(summary);
		setTitleToolTip(getPartName() + ": " + summary); //$NON-NLS-1$

	}

	public void updateSummary() {
		setViewSummary(getActiveConnection().getFullSummary());
	}

	@Override
	public void onTerminalFontChanged() {
		// set the font for all - empty hook for extenders
	}

	// ViewPart interface

	@Override
	public void createPartControl(Composite wndParent) {
		// Bind plugin.xml key bindings to this plugin. Overrides global Control-W key
		// sequence.

		fPageBook = new PageBook(wndParent, SWT.NONE);
		ISettingsStore s = new SettingStorePrefixDecorator(fStore, "connectionManager"); //$NON-NLS-1$
		fMultiConnectionManager.loadState(s, new ITerminalViewConnectionFactory() {
			@Override
			public ITerminalViewConnection create() {
				return makeViewConnection();
			}
		});
		// if there is no connection loaded, create at least one
		// needed to read old states from the old terminal
		if (fMultiConnectionManager.size() == 0) {
			ITerminalViewConnection conn = makeViewConnection();
			fMultiConnectionManager.addConnection(conn);
			fMultiConnectionManager.setActiveConnection(conn);
			fPageBook.showPage(fCtlTerminal.getRootControl());
		}
		setTerminalControl(fMultiConnectionManager.getActiveConnection().getCtlTerminal());
		setViewTitle(findUniqueTitle(ViewMessages.PROP_TITLE));
		setupActions();
		setupLocalToolBars();
		// setup all context menus
		ITerminalViewConnection[] conn = fMultiConnectionManager.getConnections();
		for (ITerminalViewConnection element : conn) {
			setupContextMenus(element.getCtlTerminal().getControl());
		}
		setupListeners(wndParent);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(wndParent, TerminalViewPlugin.getUniqueIdentifier() + ".terminal_page"); //$NON-NLS-1$

		legacyLoadState();
		legacySetTitle();

		refresh();
		onTerminalFontChanged();

	}

	@Override
	public void dispose() {
		JFaceResources.getFontRegistry().removeListener(fPropertyChangeHandler);

		// dispose all connections
		ITerminalViewConnection[] conn = fMultiConnectionManager.getConnections();
		for (ITerminalViewConnection element : conn) {
			element.getCtlTerminal().disposeTerminal();
		}
		super.dispose();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		fCtlTerminal.setFocus();
	}

	/**
	 * This method creates the top-level control for the Terminal view.
	 */
	protected void setupControls() {
		ITerminalViewConnection conn = makeViewConnection();
		fMultiConnectionManager.addConnection(conn);
		fMultiConnectionManager.setActiveConnection(conn);
		setupContextMenus(fCtlTerminal.getControl());
	}

	private ITerminalViewConnection makeViewConnection() {
		ITerminalConnector[] connectors = makeConnectors();
		TerminalListener listener = new TerminalListener();
		ITerminalViewControl ctrl = TerminalViewControlFactory.makeControl(listener, fPageBook, connectors, true);
		setTerminalControl(ctrl);
		ITerminalViewConnection conn = new TerminalViewConnection(fCtlTerminal);
		listener.setConnection(conn);
		conn.setPartName(getPartName());
		// load from settings
		ITerminalConnector connector = loadSettings(fStore, connectors);
		// set the connector....
		ctrl.setConnector(connector);

		return conn;
	}

	/**
	 * @param store
	 *            contains the data
	 * @param connectors
	 *            loads the data from store
	 * @return null or the currently selected connector
	 */
	private ITerminalConnector loadSettings(ISettingsStore store, ITerminalConnector[] connectors) {
		ITerminalConnector connector = null;
		String connectionType = store.get(STORE_CONNECTION_TYPE);
		for (ITerminalConnector connector2 : connectors) {
			connector2.load(getStore(store, connector2));
			if (connector2.getId().equals(connectionType)) {
				connector = connector2;
			}
		}
		return connector;
	}

	/**
	 * @return a list of connectors this view can use
	 */
	protected ITerminalConnector[] makeConnectors() {
		ITerminalConnector[] connectors = TerminalConnectorExtension.makeTerminalConnectors();
		return connectors;
	}

	/**
	 * The preference setting store is used to save the settings that are
	 * shared between all views.
	 * 
	 * @return the settings store for the connection based on the preferences.
	 *
	 */
	private PreferenceSettingStore getPreferenceSettingsStore() {
		return new PreferenceSettingStore(TerminalViewPlugin.getDefault().getPluginPreferences(), PREF_CONNECTORS);
	}

	/**
	 * @param store
	 *            the settings will be saved in this store
	 * @param connector
	 *            the connector that will be saved. Can be null.
	 */
	private void saveSettings(ISettingsStore store, ITerminalConnector connector) {
		if (connector != null) {
			connector.save(getStore(store, connector));
			// the last saved connector becomes the default
			store.put(STORE_CONNECTION_TYPE, connector.getId());
		}

	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		fStore = new SettingsStore(memento);
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		fStore.put(STORE_TITLE, getPartName());
		fMultiConnectionManager.saveState(new SettingStorePrefixDecorator(fStore, "connectionManager")); //$NON-NLS-1$
		fStore.saveState(memento);
	}

	private ISettingsStore getStore(ISettingsStore store, ITerminalConnector connector) {
		return new SettingStorePrefixDecorator(store, connector.getId() + "."); //$NON-NLS-1$
	}

	protected void setupActions() {
		fActionTerminalDropDown = new TerminalActionSelectionDropDown(fMultiConnectionManager);
		fActionTerminalRemove = new TerminalActionRemove(fMultiConnectionManager);
		fActionTerminalNewTerminal = new TerminalActionNewTerminal(this);
		fActionTerminalScrollLock = new TerminalActionScrollLock(this);
		fActionTerminalConnect = new TerminalActionConnect(this);
		fActionTerminalDisconnect = new TerminalActionDisconnect(this);
		fActionTerminalSettings = new TerminalActionSettings(this);
		fActionEditCopy = new TerminalActionCopy(fCtlDecorator);
		fActionEditCut = new TerminalActionCut(fCtlDecorator);
		fActionEditPaste = new TerminalActionPaste(fCtlDecorator);
		fActionEditClearAll = new TerminalActionClearAll(fCtlDecorator);
		fActionEditSelectAll = new TerminalActionSelectAll(fCtlDecorator);
		fActionToggleCommandInputField = new TerminalActionToggleCommandInputField(this);
	}

	protected void setupLocalToolBars() {
		IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();

		toolBarMgr.add(fActionTerminalConnect);
		toolBarMgr.add(fActionTerminalDisconnect);
		toolBarMgr.add(fActionTerminalSettings);
		toolBarMgr.add(fActionToggleCommandInputField);
		toolBarMgr.add(fActionTerminalScrollLock);
		toolBarMgr.add(new Separator("fixedGroup")); //$NON-NLS-1$
		toolBarMgr.add(fActionTerminalDropDown);
		toolBarMgr.add(fActionTerminalNewTerminal);
		toolBarMgr.add(fActionTerminalRemove);
	}

	protected void setupContextMenus(Control ctlText) {
		MenuManager menuMgr;
		Menu menu;
		TerminalContextMenuHandler contextMenuHandler;

		menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menu = menuMgr.createContextMenu(ctlText);
		loadContextMenus(menuMgr);
		contextMenuHandler = new TerminalContextMenuHandler();

		ctlText.setMenu(menu);
		menuMgr.addMenuListener(contextMenuHandler);
		menu.addMenuListener(contextMenuHandler);
	}

	protected void loadContextMenus(IMenuManager menuMgr) {
		menuMgr.add(fActionEditCopy);
		menuMgr.add(fActionEditPaste);
		menuMgr.add(new Separator());
		menuMgr.add(fActionEditClearAll);
		menuMgr.add(fActionEditSelectAll);
		menuMgr.add(new Separator());
		menuMgr.add(fActionToggleCommandInputField);
		menuMgr.add(fActionTerminalScrollLock);

		// Other plug-ins can contribute there actions here
		menuMgr.add(new Separator("Additions")); //$NON-NLS-1$
	}

	protected void setupListeners(Composite wndParent) {
		fPropertyChangeHandler = new TerminalPropertyChangeHandler();
		JFaceResources.getFontRegistry().addListener(fPropertyChangeHandler);
	}

	protected class TerminalContextMenuHandler implements MenuListener, IMenuListener {
		@Override
		public void menuHidden(MenuEvent event) {
			fMenuAboutToShow = false;
			fActionEditCopy.updateAction(fMenuAboutToShow);
		}

		@Override
		public void menuShown(MenuEvent e) {
			//
		}

		@Override
		public void menuAboutToShow(IMenuManager menuMgr) {
			fMenuAboutToShow = true;
			fActionEditCopy.updateAction(fMenuAboutToShow);
			fActionEditCut.updateAction(fMenuAboutToShow);
			fActionEditSelectAll.updateAction(fMenuAboutToShow);
			fActionEditPaste.updateAction(fMenuAboutToShow);
			fActionEditClearAll.updateAction(fMenuAboutToShow);
		}
	}

	protected class TerminalPropertyChangeHandler implements IPropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FONT_DEFINITION)) {
				onTerminalFontChanged();
			}
		}
	}

	@Override
	public boolean hasCommandInputField() {
		return getActiveConnection().hasCommandInputField();
	}

	@Override
	public void setCommandInputField(boolean on) {
		getActiveConnection().setCommandInputField(on);
	}

	@Override
	public boolean isScrollLock() {
		return fCtlTerminal.isScrollLock();
	}

	@Override
	public void setScrollLock(boolean on) {
		fCtlTerminal.setScrollLock(on);
	}

	private ITerminalViewConnection getActiveConnection() {
		return fMultiConnectionManager.getActiveConnection();
	}

	/**
	 * @param ctrl
	 *            this control becomes the currently used one
	 */
	private void setTerminalControl(ITerminalViewControl ctrl) {
		fCtlTerminal = ctrl;
		fCtlDecorator.setViewContoler(ctrl);
	}

	@Override
	public void connectionsChanged() {
		if (getActiveConnection() != null) {
			// update the active {@link ITerminalViewControl}
			ITerminalViewControl ctrl = getActiveConnection().getCtlTerminal();
			if (fCtlTerminal != ctrl) {
				setTerminalControl(ctrl);
				refresh();
			}
		}
	}

	/**
	 * Show the active {@link ITerminalViewControl} in the view
	 */
	private void refresh() {
		fPageBook.showPage(fCtlTerminal.getRootControl());
		updateStatus();
		setPartName(getActiveConnection().getPartName());
	}

	/**
	 * TODO REMOVE This code (added 2008-06-11)
	 * Legacy code to real the old state. Once the state of the
	 * terminal has been saved this method is not needed anymore.
	 * Remove this code with eclipse 3.5.
	 */
	private void legacyLoadState() {
		// TODO legacy: load the old title....
		String summary = fStore.get(STORE_SETTING_SUMMARY);
		if (summary != null) {
			getActiveConnection().setSummary(summary);
			fStore.put(STORE_SETTING_SUMMARY, null);
		}
	}

	/**
	 * TODO REMOVE This code (added 2008-06-11)
	 * Legacy code to real the old state. Once the state of the
	 * terminal has been saved this method is not needed anymore.
	 * Remove this code with eclipse 3.5.
	 */
	private void legacySetTitle() {
		// restore the title of this view
		String title = fStore.get(STORE_TITLE);
		if (title != null && title.length() > 0) {
			setViewTitle(title);
			fStore.put(STORE_TITLE, null);
		}
	}

}
