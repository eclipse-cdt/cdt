/*******************************************************************************
 * Copyright (c) 2003, 2008 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.view;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tm.internal.terminal.actions.TerminalAction;
import org.eclipse.tm.internal.terminal.actions.TerminalActionConnect;
import org.eclipse.tm.internal.terminal.actions.TerminalActionDisconnect;
import org.eclipse.tm.internal.terminal.actions.TerminalActionNewTerminal;
import org.eclipse.tm.internal.terminal.actions.TerminalActionSettings;
import org.eclipse.tm.internal.terminal.actions.TerminalActionToggleCommandInputField;
import org.eclipse.tm.internal.terminal.control.CommandInputFieldWithHistory;
import org.eclipse.tm.internal.terminal.control.ITerminalListener;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.TerminalViewControlFactory;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionClearAll;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionCopy;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionCut;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionPaste;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionSelectAll;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class TerminalView extends ViewPart implements ITerminalView, ITerminalListener {
    private static final String STORE_CONNECTION_TYPE = "ConnectionType"; //$NON-NLS-1$

    private static final String STORE_SETTING_SUMMARY = "SettingSummary"; //$NON-NLS-1$

    private static final String STORE_HAS_COMMAND_INPUT_FIELD = "HasCommandInputField"; //$NON-NLS-1$

	private static final String STORE_COMMAND_INPUT_FIELD_HISTORY = "CommandInputFieldHistory"; //$NON-NLS-1$

	private static final String STORE_TITLE = "Title"; //$NON-NLS-1$

	public static final String  FONT_DEFINITION = "terminal.views.view.font.definition"; //$NON-NLS-1$

	protected ITerminalViewControl fCtlTerminal;

	protected TerminalAction fActionTerminalNewTerminal;

	protected TerminalAction fActionTerminalConnect;

//	private TerminalAction fActionTerminalScrollLock;

	protected TerminalAction fActionTerminalDisconnect;

	protected TerminalAction fActionTerminalSettings;

	protected TerminalActionCopy fActionEditCopy;

	protected TerminalActionCut fActionEditCut;

	protected TerminalActionPaste fActionEditPaste;

	protected TerminalActionClearAll fActionEditClearAll;

	protected TerminalActionSelectAll fActionEditSelectAll;

	protected TerminalAction fActionToggleCommandInputField;

	protected TerminalPropertyChangeHandler fPropertyChangeHandler;

	protected boolean fMenuAboutToShow;

	private SettingsStore fStore;

	private CommandInputFieldWithHistory fCommandInputField;

	/**
	 * Listens to changes in the preferences
	 */
	private final IPropertyChangeListener fPreferenceListener=new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if(event.getProperty().equals(TerminalPreferencePage.PREF_LIMITOUTPUT)
					|| event.getProperty().equals(TerminalPreferencePage.PREF_BUFFERLINES)
					|| event.getProperty().equals(TerminalPreferencePage.PREF_INVERT_COLORS)) {
				updatePreferences();
			}
		}
	};
	public TerminalView() {
		Logger
				.log("==============================================================="); //$NON-NLS-1$
	}

	String findUniqueTitle(String title) {
		IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
		String id=	getViewSite().getId();
		Set names=new HashSet();
		for (int i = 0; i < pages.length; i++) {
			IViewReference[] views = pages[i].getViewReferences();
			for (int j = 0; j < views.length; j++) {
				IViewReference view = views[j];
				// only look for views with the same ID
				if(id.equals(view.getId())) {
					String name=view.getTitle();
					if(name!=null)
						names.add(view.getPartName());
				}
			}
		}
		// find a unique name
		int i=1;
		String uniqueTitle=title;
		while(true) {
			if(!names.contains(uniqueTitle))
				return uniqueTitle;
			uniqueTitle=title+" "+i++; //$NON-NLS-1$
		}
	}
	/**
	 * Update the text limits from the preferences
	 */
	private void updatePreferences() {
		Preferences preferences = TerminalViewPlugin.getDefault().getPluginPreferences();
//		boolean limitOutput = preferences.getBoolean(TerminalPreferencePage.PREF_LIMITOUTPUT);
//		if(!limitOutput)
//			bufferLineLimit=-1;
		int bufferLineLimit = preferences.getInt(TerminalPreferencePage.PREF_BUFFERLINES);
		fCtlTerminal.setBufferLineLimit(bufferLineLimit);
		fCtlTerminal.setInvertedColors(preferences.getBoolean(TerminalPreferencePage.PREF_INVERT_COLORS));
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
			// which is undesirable.  Therefore, we append the current time in
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
		//if (isConnected())
		if (fCtlTerminal.getState()!=TerminalState.CLOSED)
			return;
		if(fCtlTerminal.getTerminalConnector()==null)
			setConnector(showSettingsDialog());
		fCtlTerminal.connectTerminal();
	}

	public void updateStatus() {
		updateTerminalConnect();
		updateTerminalDisconnect();
		updateTerminalSettings();
	}

	public void updateTerminalConnect() {
		//boolean bEnabled = ((!isConnecting()) && (!fCtlTerminal.isConnected()));
		boolean bEnabled = (fCtlTerminal.getState()==TerminalState.CLOSED);

		fActionTerminalConnect.setEnabled(bEnabled);
	}

	private boolean isConnecting() {
		return fCtlTerminal.getState()==TerminalState.CONNECTING
		    || fCtlTerminal.getState()==TerminalState.OPENED;
	}

	public void onTerminalDisconnect() {
		fCtlTerminal.disconnectTerminal();
	}

	public void updateTerminalDisconnect() {
		boolean bEnabled = ((isConnecting()) || (fCtlTerminal.isConnected()));
		fActionTerminalDisconnect.setEnabled(bEnabled);
	}

	public void onTerminalSettings() {
		ITerminalConnector c=showSettingsDialog();
		if(c!=null) {
			setConnector(c);

			onTerminalConnect();
		}
	}

	private ITerminalConnector showSettingsDialog() {
		// When the settings dialog is opened, load the Terminal settings from the
		// persistent settings.

		TerminalSettingsDlg dlgTerminalSettings = new TerminalSettingsDlg(getViewSite().getShell(),fCtlTerminal.getConnectors(),fCtlTerminal.getTerminalConnector());
		dlgTerminalSettings.setTerminalTitle(getPartName());
		Logger.log("opening Settings dialog."); //$NON-NLS-1$

		if (dlgTerminalSettings.open() == Window.CANCEL) {
			Logger.log("Settings dialog cancelled."); //$NON-NLS-1$
			return null;
		}

		Logger.log("Settings dialog OK'ed."); //$NON-NLS-1$

		// When the settings dialog is closed, we persist the Terminal settings.

		saveSettings(dlgTerminalSettings.getConnector());
		setPartName(dlgTerminalSettings.getTerminalTitle());
		return dlgTerminalSettings.getConnector();
	}

	private void setConnector(ITerminalConnector connector) {
		fCtlTerminal.setConnector(connector);
	}

	public void updateTerminalSettings() {
		//boolean bEnabled = ((!isConnecting()) && (!fCtlTerminal.isConnected()));
		boolean bEnabled = (fCtlTerminal.getState()==TerminalState.CLOSED);

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
		} else if(fCtlTerminal.getTerminalConnector()==null){
			strTitle=ViewMessages.NO_CONNECTION_SELECTED;
		} else {
			// When parameter 'data' is null, we construct a descriptive string to
			// display in the content description line.
			String strConnected = getStateDisplayName(fCtlTerminal.getState());
			String summary = getSettingsSummary();
			//TODO Title should use an NLS String and com.ibm.icu.MessageFormat
			//In order to make the logic of assembling, and the separators, better adapt to foreign languages
			if(summary.length()>0)
				summary=summary+" - ";  //$NON-NLS-1$
			String name=fCtlTerminal.getTerminalConnector().getName();
			if(name.length()>0) {
				name+=": "; //$NON-NLS-1$
			}
			strTitle = name + "("+ summary + strConnected + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		setContentDescription(strTitle);
		getViewSite().getActionBars().getStatusLineManager().setMessage(
				strTitle);
		setTitleToolTip(getPartName()+": "+strTitle); //$NON-NLS-1$
	}
	/**
	 * @return the setting summary. If there is no connection, or the connection
	 * has not been initialized, use the last stored state.
	 */
	private String getSettingsSummary() {
		// TODO: use another mechanism than "?" for the magic non initialized state
		// see TerminalConnectorProxy.getSettingsSummary
		String summary="?"; //$NON-NLS-1$
		if(fCtlTerminal.getTerminalConnector()!=null)
			summary=fCtlTerminal.getSettingsSummary();
		if("?".equals(summary)) { //$NON-NLS-1$
			summary=fStore.get(STORE_SETTING_SUMMARY, ""); //$NON-NLS-1$
		}
		return summary;
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
		fCtlTerminal.setFont(JFaceResources.getFont(FONT_DEFINITION));
	}

	// ViewPart interface

	public void createPartControl(Composite wndParent) {
		// Bind plugin.xml key bindings to this plugin.  Overrides global Control-W key
		// sequence.

		setPartName(findUniqueTitle(ViewMessages.PROP_TITLE));
		setupControls(wndParent);
		setupActions();
		setupLocalToolBars();
		setupContextMenus();
		setupListeners(wndParent);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(wndParent, TerminalViewPlugin.HELPPREFIX + "terminal_page"); //$NON-NLS-1$

		onTerminalStatus();
		onTerminalFontChanged();
	}
	public void dispose() {
		Logger.log("entered."); //$NON-NLS-1$

		TerminalViewPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPreferenceListener);

		JFaceResources.getFontRegistry().removeListener(fPropertyChangeHandler);
		fCtlTerminal.disposeTerminal();
		super.dispose();
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
		ITerminalConnector[] connectors = makeConnectors();
		fCtlTerminal = TerminalViewControlFactory.makeControl(this, wndParent, connectors);

		String connectionType=fStore.get(STORE_CONNECTION_TYPE);
		for (int i = 0; i < connectors.length; i++) {
			connectors[i].load(getStore(connectors[i]));
			if(connectors[i].getId().equals(connectionType))
				fCtlTerminal.setConnector(connectors[i]);
		}
		setCommandInputField("true".equals(fStore.get(STORE_HAS_COMMAND_INPUT_FIELD))); //$NON-NLS-1$
		updatePreferences();
		TerminalViewPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(fPreferenceListener);

		// restore the title of this view
		String title=fStore.get(STORE_TITLE);
		if(title!=null && title.length()>0)
			setPartName(title);
	}

	/**
	 * @return a list of connectors this view can use
	 */
	protected ITerminalConnector[] makeConnectors() {
		ITerminalConnector[] connectors=TerminalConnectorExtension.makeTerminalConnectors();
		return connectors;
	}

	private void saveSettings(ITerminalConnector connector) {
		ITerminalConnector[] connectors=fCtlTerminal.getConnectors();
		for (int i = 0; i < connectors.length; i++) {
			connectors[i].save(getStore(connectors[i]));
		}
		if(connector!=null) {
			fStore.put(STORE_CONNECTION_TYPE,connector.getId());
		}
	}

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		fStore=new SettingsStore(memento);
	}

	public void saveState(IMemento memento) {
		super.saveState(memento);
		if(fCommandInputField!=null)
			fStore.put(STORE_COMMAND_INPUT_FIELD_HISTORY, fCommandInputField.getHistory());
		fStore.put(STORE_HAS_COMMAND_INPUT_FIELD,hasCommandInputField()?"true":"false");   //$NON-NLS-1$//$NON-NLS-2$
		fStore.put(STORE_SETTING_SUMMARY, getSettingsSummary());
		fStore.put(STORE_TITLE,getPartName());
		fStore.saveState(memento);
	}
	private ISettingsStore getStore(ITerminalConnector connector) {
		return new SettingStorePrefixDecorator(fStore,connector.getId()+"."); //$NON-NLS-1$
	}

	protected void setupActions() {
		fActionTerminalNewTerminal = new TerminalActionNewTerminal(this);
//		fActionTerminalScrollLock = new TerminalActionScrollLock(this);
		fActionTerminalConnect = new TerminalActionConnect(this);
		fActionTerminalDisconnect = new TerminalActionDisconnect(this);
		fActionTerminalSettings = new TerminalActionSettings(this);
		fActionEditCopy = new TerminalActionCopy(fCtlTerminal);
		fActionEditCut = new TerminalActionCut(fCtlTerminal);
		fActionEditPaste = new TerminalActionPaste(fCtlTerminal);
		fActionEditClearAll = new TerminalActionClearAll(fCtlTerminal);
		fActionEditSelectAll = new TerminalActionSelectAll(fCtlTerminal);
		fActionToggleCommandInputField = new TerminalActionToggleCommandInputField(this);
	}
	protected void setupLocalToolBars() {
		IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();

		toolBarMgr.add(fActionTerminalNewTerminal);
//		toolBarMgr.add(fActionTerminalScrollLock);
		toolBarMgr.add(fActionTerminalConnect);
		toolBarMgr.add(fActionTerminalDisconnect);
		toolBarMgr.add(fActionTerminalSettings);
		toolBarMgr.add(fActionToggleCommandInputField);
	}

	protected void setupContextMenus() {
		Control ctlText;
		MenuManager menuMgr;
		Menu menu;
		TerminalContextMenuHandler contextMenuHandler;

		ctlText = fCtlTerminal.getControl();
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
//		menuMgr.add(fActionTerminalScrollLock);


		// Other plug-ins can contribute there actions here
		menuMgr.add(new Separator("Additions")); //$NON-NLS-1$
	}

	protected void setupListeners(Composite wndParent) {
		fPropertyChangeHandler = new TerminalPropertyChangeHandler();
		JFaceResources.getFontRegistry().addListener(fPropertyChangeHandler);
	}

	protected class TerminalContextMenuHandler implements MenuListener, IMenuListener {
		public void menuHidden(MenuEvent event) {
			fMenuAboutToShow = false;
			fActionEditCopy.updateAction(fMenuAboutToShow);
		}

		public void menuShown(MenuEvent e) {
			//
		}
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
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FONT_DEFINITION)) {
				onTerminalFontChanged();
			}
		}
	}

	public boolean hasCommandInputField() {
		return fCommandInputField!=null;
	}
	public void setCommandInputField(boolean on) {
		// save the old history
		if(fCommandInputField!=null) {
			fStore.put(STORE_COMMAND_INPUT_FIELD_HISTORY, fCommandInputField.getHistory());
			fCommandInputField=null;
		}
		if(on) {
			// TODO make history size configurable
			fCommandInputField=new CommandInputFieldWithHistory(100);
			fCommandInputField.setHistory(fStore.get(STORE_COMMAND_INPUT_FIELD_HISTORY));
		}
		fCtlTerminal.setCommandInputField(fCommandInputField);
	}

	public boolean isScrollLock() {
		return fCtlTerminal.isScrollLock();
	}

	public void setScrollLock(boolean on) {
		fCtlTerminal.setScrollLock(on);
	}
}
