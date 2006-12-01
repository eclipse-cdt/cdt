/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/

package org.eclipse.tm.terminal.internal.view;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tm.terminal.ISettingsStore;
import org.eclipse.tm.terminal.ITerminalConnector;
import org.eclipse.tm.terminal.Logger;
import org.eclipse.tm.terminal.TerminalConnectorExtension;
import org.eclipse.tm.terminal.TerminalState;
import org.eclipse.tm.terminal.internal.actions.TerminalAction;
import org.eclipse.tm.terminal.internal.actions.TerminalActionClearAll;
import org.eclipse.tm.terminal.internal.actions.TerminalActionConnect;
import org.eclipse.tm.terminal.internal.actions.TerminalActionCopy;
import org.eclipse.tm.terminal.internal.actions.TerminalActionCut;
import org.eclipse.tm.terminal.internal.actions.TerminalActionDisconnect;
import org.eclipse.tm.terminal.internal.actions.TerminalActionNewTerminal;
import org.eclipse.tm.terminal.internal.actions.TerminalActionPaste;
import org.eclipse.tm.terminal.internal.actions.TerminalActionSelectAll;
import org.eclipse.tm.terminal.internal.actions.TerminalActionSettings;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.tm.terminal.control.ITerminalListener;
import org.eclipse.tm.terminal.control.ITerminalViewControl;
import org.eclipse.tm.terminal.control.TerminalViewControlFactory;

public class TerminalView extends ViewPart implements ITerminalView, ITerminalListener {
    public static final String  FONT_DEFINITION = "terminal.views.view.font.definition"; //$NON-NLS-1$
	protected static final String fSecondaryTerminalCountMutex = ""; //$NON-NLS-1$

	protected static int fSecondaryTerminalCount = 0;

	protected ITerminalViewControl fCtlTerminal;

	protected TerminalAction fActionTerminalNewTerminal;

	protected TerminalAction fActionTerminalConnect;

	protected TerminalAction fActionTerminalDisconnect;

	protected TerminalAction fActionTerminalSettings;

	protected TerminalAction fActionEditCopy;

	protected TerminalAction fActionEditCut;

	protected TerminalAction fActionEditPaste;

	protected TerminalAction fActionEditClearAll;

	protected TerminalAction fActionEditSelectAll;

	protected TerminalMenuHandlerEdit fMenuHandlerEdit;

	protected TerminalPropertyChangeHandler fPropertyChangeHandler;

	protected boolean fMenuAboutToShow;
	
	private ISettingsStore fStore;

	/** Remember the item with which we contributed the shortcut to unregister them again! */
	private IContextActivation fRememberedContextActivation;

	public TerminalView() {
		Logger
				.log("==============================================================="); //$NON-NLS-1$
	}

	private void XXXXX() {
		Preferences preferences = TerminalViewPlugin.getDefault().getPluginPreferences();
		boolean bLimitOutput = preferences.getBoolean(TerminalPreferencePage.PREF_LIMITOUTPUT);
		int bufferLineLimit = preferences.getInt(TerminalPreferencePage.PREF_BUFFERLINES);

	}
	// TerminalTarget interface
	public void setState(final TerminalState state) {
		Runnable runnable=new Runnable() {
			public void run() {
				updateStatus();
				onTerminalStatus();
			}
		};
		if(Thread.currentThread()==Display.getDefault().getThread())
			runnable.run();
		else
			Display.getDefault().syncExec(runnable);
	}


	/**
	 * Display a new Terminal view.  This method is called when the user clicks the New
	 * Terminal button in any Terminal view's toolbar.
	 */
	public void onTerminalNewTerminal() {
		Logger.log("creating new Terminal instance."); //$NON-NLS-1$

		try {
			// The second argument to showView() is a unique String identifying the
			// secondary view instance.  If it ever matches a previously used secondary
			// view identifier, then this call will not create a new Terminal view,
			// which is undesireable.  Therefore, we append the current time in
			// milliseconds to the secondary view identifier to ensure it is always
			// unique.  This code runs only when the user clicks the New Terminal
			// button, so there is no risk that this code will run twice in a single
			// millisecond.

			getSite().getPage().showView(
					"org.eclipse.tm.terminal.view.TerminalView",//$NON-NLS-1$
					"SecondaryTerminal" + System.currentTimeMillis(), //$NON-NLS-1$
					IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException ex) {
			Logger.logException(ex);
		}
	}

	public void onTerminalConnect() {
		if (isConnected())
			return;
		if(fCtlTerminal.getTerminalConnection()==null)
			setConnector(showSettingsDialog());
		fCtlTerminal.connectTerminal();
	}

	public void updateStatus() {
		updateTerminalConnect();
		updateTerminalDisconnect();
		updateTerminalSettings();
	}

	public void updateTerminalConnect() {
		boolean bEnabled = ((!isConnecting()) && (!fCtlTerminal.isConnected()));

		fActionTerminalConnect.setEnabled(bEnabled);
	}

	private boolean isConnecting() {
		return fCtlTerminal.getState()==TerminalState.CONNECTING;
	}
	private boolean isConnected() {
		return fCtlTerminal.getState()==TerminalState.CONNECTED;
	}
	public void onTerminalDisconnect() {
		fCtlTerminal.disconnectTerminal();
	}

	public void updateTerminalDisconnect() {
		boolean bEnabled = ((isConnecting()) || (fCtlTerminal.isConnected()));
		fActionTerminalDisconnect.setEnabled(bEnabled);
	}

	public void onTerminalSettings() {
		setConnector(showSettingsDialog());

		onTerminalConnect();
	}

	private ITerminalConnector showSettingsDialog() {
		// When the settings dialog is opened, load the Terminal settings from the
		// persistent settings.

		TerminalSettingsDlg dlgTerminalSettings = new TerminalSettingsDlg(getViewSite().getShell(),fCtlTerminal.getConnectors(),fCtlTerminal.getTerminalConnection());

		Logger.log("opening Settings dialog."); //$NON-NLS-1$

		if (dlgTerminalSettings.open() == Window.CANCEL) {
			Logger.log("Settings dialog cancelled."); //$NON-NLS-1$
			return null;
		}

		Logger.log("Settings dialog OK'ed."); //$NON-NLS-1$

		// When the settings dialog is closed, we persist the Terminal settings.

		saveSettings();
		return dlgTerminalSettings.getConnector();
	}

	private void setConnector(ITerminalConnector connector) {
		fCtlTerminal.setConnector(connector);
	}

	public void updateTerminalSettings() {
		boolean bEnabled;

		bEnabled = ((!isConnecting()) && (!fCtlTerminal
				.isConnected()));

		fActionTerminalSettings.setEnabled(bEnabled);
	}

	public void setTerminalTitle(String strTitle) {
		if (fCtlTerminal.isDisposed())
			return;

		if (strTitle != null) {
			// When parameter 'data' is not null, it is a String containing text to
			// display in the view's content description line.  This is used by class
			// TerminalText when it processes an ANSI OSC escape sequence that commands
			// the terminal to display text in its title bar.
		} else {
			// When parameter 'data' is null, we construct a descriptive string to
			// display in the content description line.
			String strConnected = getStateDisplayName(fCtlTerminal.getState());
			String status=""; //$NON-NLS-1$
			status=fCtlTerminal.getStatusString(strConnected);
			strTitle = ViewMessages.PROP_TITLE + status;
		}

		setContentDescription(strTitle);
		getViewSite().getActionBars().getStatusLineManager().setMessage(
				strTitle);
	}
	public void onTerminalStatus() {
		setTerminalTitle(null);
	}

	private String getStateDisplayName(TerminalState state) {
		if(state==TerminalState.CONNECTED) {
			return ViewMessages.STATE_CONNECTED;
		} else if(state==TerminalState.CONNECTING) {
			return ViewMessages.STATE_CONNECTING;
		} else if(state==TerminalState.OPENED) {			
			return ViewMessages.STATE_OPENED;
		} else if(state==TerminalState.CLOSED) {			
			return ViewMessages.STATE_CLOSED;
		} else {
			throw new IllegalStateException(state.toString());
		}
	}

	public void onTerminalFontChanged() {
		fCtlTerminal.getCtlText().setFont(JFaceResources.getFont(FONT_DEFINITION));

		// Tell the TerminalControl singleton that the font has changed.

		fCtlTerminal.onFontChanged();
	}

	public void onEditCopy() {
		String selection=fCtlTerminal.getSelection();

		if (!selection.equals("")) {//$NON-NLS-1$
			fCtlTerminal.copy();
		} else {
			fCtlTerminal.sendKey('\u0003');
		}
	}

	public void updateEditCopy() {
		boolean bEnabled=true;

		if (fMenuAboutToShow) {
			bEnabled = fCtlTerminal.getSelection().length()>0;
		}

		fActionEditCopy.setEnabled(bEnabled);
	}

	public void onEditCut() {
		fCtlTerminal.sendKey('\u0018');
	}

	public void updateEditCut() {
		boolean bEnabled;

		bEnabled = !fMenuAboutToShow;
		fActionEditCut.setEnabled(bEnabled);
	}

	public void onEditPaste() {
		fCtlTerminal.paste();
	}

	public void updateEditPaste() {
		String strText = (String) fCtlTerminal.getClipboard().getContents(TextTransfer.getInstance());

		boolean bEnabled = ((strText != null) && (!strText.equals("")) && (isConnected()));//$NON-NLS-1$

		fActionEditPaste.setEnabled(bEnabled);
	}

	public void onEditClearAll() {
		fCtlTerminal.clearTerminal();
	}

	public void updateEditClearAll() {
		fActionEditClearAll.setEnabled(!fCtlTerminal.isEmpty());
	}

	public void onEditSelectAll() {
		fCtlTerminal.selectAll();
	}

	public void updateEditSelectAll() {
		fActionEditSelectAll.setEnabled(!fCtlTerminal.isEmpty());
	}

	// ViewPart interface

	public void createPartControl(Composite wndParent) {
		// Bind plugin.xml key bindings to this plugin.  Overrides global Control-W key
		// sequence.

		/** Activate the sy context allowing shortcuts like F3(open declaration) in the view */
		IContextService ctxtService = (IContextService) getSite().getService(IContextService.class);
		fRememberedContextActivation = ctxtService.activateContext("org.eclipse.tm.terminal.TerminalPreferencePage"); //$NON-NLS-1$

		synchronized (fSecondaryTerminalCountMutex) {
			setPartName(ViewMessages.PROP_TITLE + " " + fSecondaryTerminalCount++); //$NON-NLS-1$
		}

		setupControls(wndParent);
		setupActions();
		setupMenus();
		setupLocalToolBars();
		setupContextMenus();
		setupListeners(wndParent);

		onTerminalStatus();
	}

	public void dispose() {
		Logger.log("entered."); //$NON-NLS-1$

		setPartName("Terminal"); //$NON-NLS-1$

		TerminalViewPlugin plugin;
		IWorkbench workbench;
		WorkbenchWindow workbenchWindow;
		MenuManager menuMgr;
		Menu menu;

		/** The context (for short cuts) was set above, now unset it again */
		if (fRememberedContextActivation != null) {
			IContextService ctxService = (IContextService) getSite()
					.getService(IContextService.class);
			ctxService.deactivateContext(fRememberedContextActivation);
			fRememberedContextActivation = null;
		}

		JFaceResources.getFontRegistry().removeListener(fPropertyChangeHandler);
		plugin = TerminalViewPlugin.getDefault();
		workbench = plugin.getWorkbench();
		workbenchWindow = (WorkbenchWindow) workbench
				.getActiveWorkbenchWindow();
		menuMgr = workbenchWindow.getMenuManager();
		menuMgr = (MenuManager) menuMgr
				.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		menu = menuMgr.getMenu();

		menuMgr.removeMenuListener(fMenuHandlerEdit);

		if (menu != null)
			menu.removeMenuListener(fMenuHandlerEdit);

		fCtlTerminal.disposeTerminal();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		fCtlTerminal.setFocus();
	}

	/**
	 * This method creates the top-level control for the Terminal view.
	 */
	protected void setupControls(Composite wndParent) {
		ITerminalConnector[] connectors=TerminalConnectorExtension.getTerminalConnectors();
		fCtlTerminal = TerminalViewControlFactory.makeControl(this, wndParent, connectors);
		String connectionType=getStore().get("ConnectionType"); //$NON-NLS-1$
		for (int i = 0; i < connectors.length; i++) {
			connectors[i].load(getStore(connectors[i]));
			if(connectors[i].getId().equals(connectionType))
				fCtlTerminal.setConnector(connectors[i]);
		}
	}
	private void saveSettings() {
		ITerminalConnector[] connectors=fCtlTerminal.getConnectors();
		for (int i = 0; i < connectors.length; i++) {
			connectors[i].save(getStore(connectors[i]));
		}
		if(fCtlTerminal.getTerminalConnection()!=null) {
			getStore().put("ConnectionType",fCtlTerminal.getTerminalConnection().getId()); //$NON-NLS-1$
		}
	}
	
	private ISettingsStore getStore() {
		if(fStore==null)
			fStore=new SettingsStore(getPartName());
		return fStore;
	}
	
	private ISettingsStore getStore(ITerminalConnector connector) {
		return new SettingStorePrefixDecorator(getStore(),connector.getClass().getName()+"."); //$NON-NLS-1$
	}

	protected void setupActions() {
		fActionTerminalNewTerminal = new TerminalActionNewTerminal(this);
		fActionTerminalConnect = new TerminalActionConnect(this);
		fActionTerminalDisconnect = new TerminalActionDisconnect(this);
		fActionTerminalSettings = new TerminalActionSettings(this);
		fActionEditCopy = new TerminalActionCopy(this);
		fActionEditCut = new TerminalActionCut(this);
		fActionEditPaste = new TerminalActionPaste(this);
		fActionEditClearAll = new TerminalActionClearAll(this);
		fActionEditSelectAll = new TerminalActionSelectAll(this);

		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), fActionEditCopy);

		actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), fActionEditCut);

		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), fActionEditPaste);

		actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), fActionEditSelectAll);
	}

	protected void setupMenus() {
		TerminalViewPlugin plugin;
		IWorkbench workbench;
		WorkbenchWindow workbenchWindow;
		MenuManager menuMgr;
		Menu menu;

		fMenuHandlerEdit = new TerminalMenuHandlerEdit();
		plugin = TerminalViewPlugin.getDefault();
		workbench = plugin.getWorkbench();
		workbenchWindow = (WorkbenchWindow) workbench
				.getActiveWorkbenchWindow();
		menuMgr = workbenchWindow.getMenuManager();
		menuMgr = (MenuManager) menuMgr
				.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		menu = menuMgr.getMenu();

		menuMgr.addMenuListener(fMenuHandlerEdit);
		menu.addMenuListener(fMenuHandlerEdit);
	}

	protected void setupLocalToolBars() {
		IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();

		toolBarMgr.add(fActionTerminalNewTerminal);
		toolBarMgr.add(fActionTerminalConnect);
		toolBarMgr.add(fActionTerminalDisconnect);
		toolBarMgr.add(fActionTerminalSettings);
	}

	protected void setupContextMenus() {
		StyledText ctlText;
		MenuManager menuMgr;
		Menu menu;
		TerminalContextMenuHandler contextMenuHandler;

		ctlText = fCtlTerminal.getCtlText();
		menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menu = menuMgr.createContextMenu(ctlText);
		contextMenuHandler = new TerminalContextMenuHandler();

		ctlText.setMenu(menu);
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(contextMenuHandler);
		menu.addMenuListener(contextMenuHandler);
	}

	protected void loadContextMenus(IMenuManager menuMgr) {
		menuMgr.add(fActionEditCopy);
		menuMgr.add(fActionEditPaste);
		menuMgr.add(new Separator());
		menuMgr.add(fActionEditClearAll);
		menuMgr.add(fActionEditSelectAll);

		// Other plug-ins can contribute there actions here
		menuMgr.add(new Separator("Additions")); //$NON-NLS-1$
	}

	protected void setupListeners(Composite wndParent) {
		fPropertyChangeHandler = new TerminalPropertyChangeHandler();
		JFaceResources.getFontRegistry().addListener(fPropertyChangeHandler);
	}

	// Inner classes

	protected class TerminalMenuHandlerEdit implements MenuListener, IMenuListener {
		protected String fActionDefinitionIdCopy;

		protected String fActionDefinitionIdPaste;

		protected String fActionDefinitionIdSelectAll;

		protected int fAcceleratorCopy;

		protected int fAcceleratorPaste;

		protected int fAcceleratorSelectAll;

		protected TerminalMenuHandlerEdit() {
			super();

			fActionDefinitionIdCopy = ""; //$NON-NLS-1$
			fActionDefinitionIdPaste = ""; //$NON-NLS-1$
			fActionDefinitionIdSelectAll = ""; //$NON-NLS-1$

			fAcceleratorCopy = 0;
			fAcceleratorPaste = 0;
			fAcceleratorSelectAll = 0;
		}
		public void menuAboutToShow(IMenuManager menuMgr) {

			fMenuAboutToShow = true;
			updateEditCopy();
			updateEditCut();
			updateEditPaste();
			updateEditSelectAll();

			ActionContributionItem item = (ActionContributionItem) menuMgr.find(ActionFactory.COPY.getId());
			RetargetAction action = (RetargetAction) item.getAction();
			fActionDefinitionIdCopy = action.getActionDefinitionId();
			fAcceleratorCopy = action.getAccelerator();
			action.setActionDefinitionId(null);
			action.enableAccelerator(false);
			item.update();

			item = (ActionContributionItem) menuMgr.find(ActionFactory.PASTE.getId());
			action = (RetargetAction) item.getAction();
			fActionDefinitionIdPaste = action.getActionDefinitionId();
			fAcceleratorPaste = action.getAccelerator();
			action.setActionDefinitionId(null);
			action.enableAccelerator(false);
			item.update();

			item = (ActionContributionItem) menuMgr.find(ActionFactory.SELECT_ALL.getId());
			action = (RetargetAction) item.getAction();
			fActionDefinitionIdSelectAll = action.getActionDefinitionId();
			fAcceleratorSelectAll = action.getAccelerator();
			action.setActionDefinitionId(null);
			action.enableAccelerator(false);
			item.update();
		}
		public void menuShown(MenuEvent event) {
			// do nothing
		}
		public void menuHidden(MenuEvent event) {
			TerminalViewPlugin plugin;
			IWorkbench workbench;
			WorkbenchWindow workbenchWindow;
			MenuManager menuMgr;
			ActionContributionItem item;
			RetargetAction action;

			fMenuAboutToShow = false;
			updateEditCopy();
			updateEditCut();

			plugin = TerminalViewPlugin.getDefault();
			workbench = plugin.getWorkbench();
			workbenchWindow = (WorkbenchWindow) workbench
					.getActiveWorkbenchWindow();
			menuMgr = workbenchWindow.getMenuManager();
			menuMgr = (MenuManager) menuMgr
					.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);

			item = (ActionContributionItem) menuMgr.find(ActionFactory.COPY
					.getId());
			action = (RetargetAction) item.getAction();
			action.setActionDefinitionId(fActionDefinitionIdCopy);
			action.setAccelerator(fAcceleratorCopy);
			action.enableAccelerator(true);
			item.update();

			item = (ActionContributionItem) menuMgr.find(ActionFactory.PASTE
					.getId());
			action = (RetargetAction) item.getAction();
			action.setActionDefinitionId(fActionDefinitionIdPaste);
			action.setAccelerator(fAcceleratorPaste);
			action.enableAccelerator(true);
			item.update();

			item = (ActionContributionItem) menuMgr
					.find(ActionFactory.SELECT_ALL.getId());
			action = (RetargetAction) item.getAction();
			action.setActionDefinitionId(fActionDefinitionIdSelectAll);
			action.setAccelerator(fAcceleratorSelectAll);
			action.enableAccelerator(true);
			item.update();
		}
	}

	protected class TerminalContextMenuHandler implements MenuListener, IMenuListener {
		public void menuHidden(MenuEvent event) {
			fMenuAboutToShow = false;
			updateEditCopy();
		}

		public void menuShown(MenuEvent e) {
			//
		}
		public void menuAboutToShow(IMenuManager menuMgr) {
			fMenuAboutToShow = true;
			updateEditCopy();
			updateEditCut();
			updateEditPaste();
			updateEditClearAll();

			loadContextMenus(menuMgr);
		}
	}

	protected class TerminalPropertyChangeHandler implements IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FONT_DEFINITION)) {
				onTerminalFontChanged();
			}
		}
	}
}
