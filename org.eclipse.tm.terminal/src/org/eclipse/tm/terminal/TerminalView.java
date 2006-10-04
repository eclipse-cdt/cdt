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

package org.eclipse.tm.terminal;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.part.ViewPart;

public class TerminalView extends ViewPart implements TerminalTarget, TerminalConsts
{
    protected static final String m_SecondaryTerminalCountMutex = ""; //$NON-NLS-1$
    protected static int m_SecondaryTerminalCount = 0;

    protected TerminalCtrl                  m_ctlTerminal;
    protected TerminalAction                m_actionTerminalNewTerminal;
    protected TerminalAction                m_actionTerminalConnect;
    protected TerminalAction                m_actionTerminalDisconnect;
    protected TerminalAction                m_actionTerminalSettings;
    protected TerminalAction                m_actionEditCopy;
    protected TerminalAction                m_actionEditCut;
    protected TerminalAction                m_actionEditPaste;
    protected TerminalAction                m_actionEditClearAll;
    protected TerminalAction                m_actionEditSelectAll;
    protected TerminalMenuHandlerEdit       m_MenuHandlerEdit;
    protected TerminalPropertyChangeHandler m_PropertyChangeHandler;
    protected TerminalSettings              m_TerminalSettings;
    protected boolean                       m_bMenuAboutToShow;
	/** Remember the item with which we contributed the shortcutt to unregister them again! */
	private IContextActivation fRememberedContextActivation;		

    /**
     * 
     */
    public TerminalView()
    {
        Logger.log("==============================================================="); //$NON-NLS-1$
        setupView();
    }

    // TerminalTarget interface

    /**
     *
     */
    public void execute(String strMsg,Object data)
    {
        if (strMsg.equals(ON_TERMINAL_FOCUS))
        {
            onTerminalFocus(data);
        }
        else if (strMsg.equals(ON_TERMINAL_NEW_TERMINAL))
        {
            onTerminalNewTerminal(data);
        }
        else if (strMsg.equals(ON_TERMINAL_CONNECT))
        {
            onTerminalConnect(data);
        }
        else if (strMsg.equals(ON_UPDATE_TERMINAL_CONNECT))
        {
            onUpdateTerminalConnect(data);
        }
        else if (strMsg.equals(ON_TERMINAL_CONNECTING))
        {
            onTerminalConnecting(data);
        }
        else if (strMsg.equals(ON_TERMINAL_DISCONNECT))
        {
            onTerminalDisconnect(data);
        }
        else if (strMsg.equals(ON_UPDATE_TERMINAL_DISCONNECT))
        {
            onUpdateTerminalDisconnect(data);
        }
        else if (strMsg.equals(ON_TERMINAL_SETTINGS))
        {
            onTerminalSettings(data);
        }
        else if (strMsg.equals(ON_UPDATE_TERMINAL_SETTINGS))
        {
            onUpdateTerminalSettings(data);
        }
        else if (strMsg.equals(ON_TERMINAL_STATUS))
        {
            onTerminalStatus(data);
        }
        else if (strMsg.equals(ON_TERMINAL_FONTCHANGED))
        {
            onTerminalFontChanged(data);
        }
        else if (strMsg.equals(ON_EDIT_COPY))
        {
            onEditCopy(data);
        }
        else if (strMsg.equals(ON_UPDATE_EDIT_COPY))
        {
            onUpdateEditCopy(data);
        }
        else if (strMsg.equals(ON_EDIT_CUT))
        {
            onEditCut(data);
        }
        else if (strMsg.equals(ON_UPDATE_EDIT_CUT))
        {
            onUpdateEditCut(data);
        }
        else if (strMsg.equals(ON_EDIT_PASTE))
        {
            onEditPaste(data);
        }
        else if (strMsg.equals(ON_UPDATE_EDIT_PASTE))
        {
            onUpdateEditPaste(data);
        }
        else if (strMsg.equals(ON_EDIT_CLEARALL))
        {
            onEditClearAll(data);
        }
        else if (strMsg.equals(ON_UPDATE_EDIT_CLEARALL))
        {
            onUpdateEditClearAll(data);
        }
        else if (strMsg.equals(ON_EDIT_SELECTALL))
        {
            onEditSelectAll(data);
        }
        else if (strMsg.equals(ON_UPDATE_EDIT_SELECTALL))
        {
            onUpdateEditSelectAll(data);
        }
        else
        {
        }
    }

    // Message handlers
    
    /**
     * 
     */
    protected void onTerminalFocus(Object data)
    {
        m_ctlTerminal.setFocus();
    }
    
    /**
     * Display a new Terminal view.  This method is called when the user clicks the New
     * Terminal button in any Terminal view's toolbar.
     */
    protected void onTerminalNewTerminal(Object data)
    {
        Logger.log("creating new Terminal instance."); //$NON-NLS-1$

        try
        {
            // The second argument to showView() is a unique String identifying the
            // secondary view instance.  If it ever matches a previously used secondary
            // view identifier, then this call will not create a new Terminal view,
            // which is undesireable.  Therefore, we append the current time in
            // milliseconds to the secondary view identifier to ensure it is always
            // unique.  This code runs only when the user clicks the New Terminal
            // button, so there is no risk that this code will run twice in a single
            // millisecond.

            getSite().getPage().showView("org.eclipse.tm.terminal.TerminalView",//$NON-NLS-1$
                                         "SecondaryTerminal" + System.currentTimeMillis(), //$NON-NLS-1$
                                         IWorkbenchPage.VIEW_ACTIVATE);
        }
        catch (PartInitException ex)
        {
            Logger.logException(ex);
        }
    }

    /**
     * 
     */
    protected void onTerminalConnect(Object data)
    {
        if (m_ctlTerminal.isConnected())
            return;
                
        m_ctlTerminal.connectTerminal(m_TerminalSettings);

        execute(ON_UPDATE_TERMINAL_CONNECT,null);
        execute(ON_UPDATE_TERMINAL_DISCONNECT,null);
        execute(ON_UPDATE_TERMINAL_SETTINGS,null);
    }
    
    /**
     *
     */
    protected void onTerminalConnecting(Object data)
    {
        execute(ON_UPDATE_TERMINAL_CONNECT,null);
        execute(ON_UPDATE_TERMINAL_DISCONNECT,null);
        execute(ON_UPDATE_TERMINAL_SETTINGS,null);
    }
    
    /**
     * 
     */
    protected void onUpdateTerminalConnect(Object data)
    {
        boolean bEnabled;
        
        bEnabled = ((!m_ctlTerminal.isConnecting()) &&
                    (!m_ctlTerminal.isConnected()));
                    
        m_actionTerminalConnect.setEnabled(bEnabled);
    }

    /**
     * 
     */
    protected void onTerminalDisconnect(Object data)
    {
        if ((!m_ctlTerminal.isConnecting()) &&
            (!m_ctlTerminal.isConnected()))
        {
            execute(ON_TERMINAL_STATUS, null);
            execute(ON_UPDATE_TERMINAL_CONNECT, null);
            execute(ON_UPDATE_TERMINAL_DISCONNECT, null);
            execute(ON_UPDATE_TERMINAL_SETTINGS, null);
            return;
        }

        m_ctlTerminal.disconnectTerminal();

        execute(ON_UPDATE_TERMINAL_CONNECT,null);
        execute(ON_UPDATE_TERMINAL_DISCONNECT,null);
        execute(ON_UPDATE_TERMINAL_SETTINGS,null);
    }
    
    /**
     * 
     */
    protected void onUpdateTerminalDisconnect(Object data)
    {
        boolean bEnabled;
        
        bEnabled = ((m_ctlTerminal.isConnecting())   ||
                    (m_ctlTerminal.isConnected()));
                    
        m_actionTerminalDisconnect.setEnabled(bEnabled);
    }

    /**
     * 
     */
    protected void onTerminalSettings(Object data)
    {
        TerminalSettingsDlg dlgTerminalSettings;
        int nStatus;

        // When the settings dialog is opened, load the Terminal settings from the
        // persistent settings.

        m_TerminalSettings.importSettings(getPartName());            

        dlgTerminalSettings = new TerminalSettingsDlg(getViewSite().getShell());
        dlgTerminalSettings.loadSettings(m_TerminalSettings);
            
        Logger.log("opening Settings dialog."); //$NON-NLS-1$

        nStatus = dlgTerminalSettings.open();

        if (nStatus == TerminalConsts.TERMINAL_ID_CANCEL)
        {
            Logger.log("Settings dialog cancelled."); //$NON-NLS-1$
            return;
        }

        Logger.log("Settings dialog OK'ed."); //$NON-NLS-1$

        // When the settings dialog is closed, we persist the Terminal settings.

        m_TerminalSettings.exportSettings(getPartName());

        execute(ON_TERMINAL_CONNECT,null);
    }
    
    /**
     * 
     */
    protected void onUpdateTerminalSettings(Object data)
    {
        boolean bEnabled;
        
        bEnabled = ((!m_ctlTerminal.isConnecting()) &&
                    (!m_ctlTerminal.isConnected()));
                    
        m_actionTerminalSettings.setEnabled(bEnabled);
    }

    /**
     *
     */
    protected void onTerminalStatus(Object data)
    {
        String strConnType;
        String strConnected;
        String strSerialPort;
        String strBaudRate;
        String strDataBits;
        String strStopBits;
        String strParity;
        String strFlowControl;
        String strHost;
        String strNetworkPort;
        String strText;
        String strTitle;
        
        if (m_ctlTerminal.isDisposed())
            return;
        
        if (data != null)
        {
            // When parameter 'data' is not null, it is a String containing text to
            // display in the view's content description line.  This is used by class
            // TerminalText when it processes an ANSI OSC escape sequence that commands
            // the terminal to display text in its title bar.

            strTitle = (String)data;
        }
        else
        {
            // When parameter 'data' is null, we construct a descriptive string to
            // display in the content description line.

            if (m_ctlTerminal.isConnecting())
            {
                strConnected = "CONNECTING..."; //$NON-NLS-1$
            }
            else if (m_ctlTerminal.isOpened())
            {
                strConnected = "OPENED"; //$NON-NLS-1$
            }
            else if (m_ctlTerminal.isConnected())
            {
                strConnected = "CONNECTED"; //$NON-NLS-1$
            }
            else
            {
                strConnected = "CLOSED"; //$NON-NLS-1$
            }

            strConnType = m_TerminalSettings.getConnType();
            if (strConnType.equals(TERMINAL_CONNTYPE_SERIAL))
            {
                strSerialPort   = m_TerminalSettings.getSerialPort();
                strBaudRate     = m_TerminalSettings.getBaudRate();
                strDataBits     = m_TerminalSettings.getDataBits();
                strStopBits     = m_TerminalSettings.getStopBits();
                strParity       = m_TerminalSettings.getParity();
                strFlowControl  = m_TerminalSettings.getFlowControl();
                strText         = " ("              +  //$NON-NLS-1$
                    strSerialPort     + 
                    ", "              +  //$NON-NLS-1$
                    strBaudRate       + 
                    ", "              +  //$NON-NLS-1$
                    strDataBits       + 
                    ", "              +  //$NON-NLS-1$
                    strStopBits       + 
                    ", "              +  //$NON-NLS-1$
                    strParity         + 
                    ", "              +  //$NON-NLS-1$
                    strFlowControl    + 
                    " - "             +  //$NON-NLS-1$
                    strConnected      +
                    ")"; //$NON-NLS-1$
            }
            else if (strConnType.equals(TERMINAL_CONNTYPE_NETWORK))
            {
                strHost         = m_TerminalSettings.getHost();
                strNetworkPort  = m_TerminalSettings.getNetworkPort();
                strText         = " ("              +  //$NON-NLS-1$
                    strHost           + 
                    ":"               +  //$NON-NLS-1$
                    strNetworkPort    + 
                    " - "             +  //$NON-NLS-1$
                    strConnected      +
                    ")"; //$NON-NLS-1$
            }
            else
            {
                strText = ""; //$NON-NLS-1$
            }
        
            strTitle = TERMINAL_PROP_TITLE + strText;
        }

        setContentDescription(strTitle);
        getViewSite().getActionBars().getStatusLineManager().setMessage(strTitle);
    }
    
    /**
     *
     */
    protected void onTerminalFontChanged(Object data)
    {
        StyledText  ctlText;
        Font        font;
        
        ctlText = m_ctlTerminal.getTextWidget();
        font    = JFaceResources.getFont(TERMINAL_FONT_DEFINITION);
        ctlText.setFont(font);

        // Tell the TerminalCtrl singleton that the font has changed.

        m_ctlTerminal.onFontChanged();
    }
    
    /**
     * 
     */
    protected void onEditCopy(Object data)
    {
        ITextSelection  selection;
        String          strText;
        boolean         bEnabled;
            
        selection   = m_ctlTerminal.getSelection();
        strText     = selection.getText();
        bEnabled    = !strText.equals(""); //$NON-NLS-1$

        if (bEnabled) 
        {
            m_ctlTerminal.copy();
        }
        else
        {
            m_ctlTerminal.sendKey('\u0003');   
        }
    }
    
    /**
     * 
     */
    protected void onUpdateEditCopy(Object data)
    {
        ITextSelection  selection;
        String          strText;
        boolean         bEnabled;
           
        if (m_bMenuAboutToShow)
        {
            selection   = m_ctlTerminal.getSelection();
            strText     = selection.getText();
            bEnabled    = !strText.equals(""); //$NON-NLS-1$
        }
        else
        {
            bEnabled    = true;
        }

        m_actionEditCopy.setEnabled(bEnabled);            
    }
    
    /**
     * 
     */
    protected void onEditCut(Object data)
    {
        m_ctlTerminal.sendKey('\u0018');   
    }
    
    /**
     * 
     */
    protected void onUpdateEditCut(Object data)
    {
        boolean bEnabled;

        bEnabled = !m_bMenuAboutToShow;
        m_actionEditCut.setEnabled(bEnabled);            
    }
    
    /**
     * 
     */
    protected void onEditPaste(Object data)
    {
        m_ctlTerminal.paste();
    }
    
    /**
     * 
     */
    protected void onUpdateEditPaste(Object data)
    {
        Clipboard       clipboard;
        TextTransfer    textTransfer;
        String          strText;
        boolean         bConnected;
        boolean         bEnabled;
        
        clipboard       = m_ctlTerminal.getClipboard();
        textTransfer    = TextTransfer.getInstance();
        strText         = (String)clipboard.getContents(textTransfer);
        bConnected      = m_ctlTerminal.isConnected();
        
        bEnabled        = ((strText != null)     &&
                           (!strText.equals("")) && //$NON-NLS-1$
                           (bConnected));
                          
        m_actionEditPaste.setEnabled(bEnabled);
    }
    
    /**
     * 
     */
    protected void onEditClearAll(Object data)
    {
        m_ctlTerminal.clearTerminal();
    }
    
    /**
     * 
     */
    protected void onUpdateEditClearAll(Object data)
    {
        boolean bEnabled;
        
        bEnabled = !m_ctlTerminal.isEmpty();
        m_actionEditClearAll.setEnabled(bEnabled);
    }
    
    /**
     * 
     */
    protected void onEditSelectAll(Object data)
    {
        m_ctlTerminal.selectAll();
    }
    
    /**
     * 
     */
    protected void onUpdateEditSelectAll(Object data)
    {
        boolean bEnabled;
        
        bEnabled = !m_ctlTerminal.isEmpty();
        m_actionEditSelectAll.setEnabled(bEnabled);
    }

    // ViewPart interface
    
    /**
     * 
     */
    public void createPartControl(Composite wndParent)
    {
        // Bind plugin.xml key bindings to this plugin.  Overrides global Ctrl-W key
        // sequence.

    	/** Activate the sy context allowing shortcuts like F3(open declaration) in the view */
		IContextService ctxtService = (IContextService)getSite().getService(IContextService.class);
		fRememberedContextActivation = ctxtService.activateContext("org.eclipse.tm.terminal.TerminalPreferencePage"); //$NON-NLS-1$

        setupControls(wndParent);
        setupActions();
        setupMenus();
        setupToolBars();
        setupLocalMenus();
        setupLocalToolBars();
        setupContextMenus();
        setupListeners(wndParent);
        
        synchronized (m_SecondaryTerminalCountMutex)
        {
            setPartName(TERMINAL_PROP_TITLE + " " + m_SecondaryTerminalCount++); //$NON-NLS-1$
        }

        execute(ON_TERMINAL_STATUS,null);
    }

    /**
     * 
     */
    public void dispose() 
    {
        Logger.log("entered."); //$NON-NLS-1$

        setPartName("Terminal"); //$NON-NLS-1$

        TerminalPlugin  plugin;
        FontRegistry    fontRegistry;
        IWorkbench      workbench;
        WorkbenchWindow workbenchWindow;
        MenuManager     menuMgr;
        Menu            menu;

		/** The context (for short cuts) was set above, now unset it again */
		if (fRememberedContextActivation != null) {
			IContextService ctxService = (IContextService)getSite().getService(IContextService.class);
			ctxService.deactivateContext(fRememberedContextActivation);
			fRememberedContextActivation = null;
		}

        fontRegistry    = JFaceResources.getFontRegistry();
        plugin          = TerminalPlugin.getDefault();
        workbench       = plugin.getWorkbench();
        workbenchWindow = (WorkbenchWindow) workbench.getActiveWorkbenchWindow();
        menuMgr         = workbenchWindow.getMenuManager();
        menuMgr         = (MenuManager) menuMgr.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
        menu            = menuMgr.getMenu();

        fontRegistry.removeListener(m_PropertyChangeHandler);
        menuMgr.removeMenuListener(m_MenuHandlerEdit);
        
        if (menu != null) 
                menu.removeMenuListener(m_MenuHandlerEdit);
        
        m_ctlTerminal.disposeTerminal();
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus()
    {
        execute(ON_TERMINAL_FOCUS,null);
    }
    
    // Operations
    
    /**
     * 
     */
    protected void setupView()
    {
        m_TerminalSettings  = new TerminalSettings(getPartName());
        m_bMenuAboutToShow  = false;
    }

    /**
     * This method creates the top-level control for the Terminal view.
     */
    protected void setupControls(Composite wndParent)
    {
        try
        {
            m_ctlTerminal = new TerminalCtrl(this, wndParent);
        }
        catch (Exception ex)
        {
            Logger.logException(ex);
        }
    }

    /**
     * 
     */
    protected void setupActions()
    {
        IViewSite   viewSite;
        IActionBars actionBars;

        viewSite                    = getViewSite();
        actionBars                  = viewSite.getActionBars();
        m_actionTerminalNewTerminal = new TerminalActionNewTerminal(this);
        m_actionTerminalConnect     = new TerminalActionConnect(this);
        m_actionTerminalDisconnect  = new TerminalActionDisconnect(this);
        m_actionTerminalSettings    = new TerminalActionSettings(this);
        m_actionEditCopy            = new TerminalActionCopy(this);
        m_actionEditCut             = new TerminalActionCut(this);
        m_actionEditPaste           = new TerminalActionPaste(this);
        m_actionEditClearAll        = new TerminalActionClearAll(this);
        m_actionEditSelectAll       = new TerminalActionSelectAll(this);
        
        actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
                                          m_actionEditCopy);

        actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(),
                                          m_actionEditCut);

        actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
                                          m_actionEditPaste);

        actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
                                          m_actionEditSelectAll);
    }

    /**
     * 
     */
    protected void setupMenus()
    {
        TerminalPlugin  plugin;
        IWorkbench      workbench;
        WorkbenchWindow workbenchWindow;
        MenuManager     menuMgr;
        Menu            menu;

        m_MenuHandlerEdit   = new TerminalMenuHandlerEdit();
        plugin              = TerminalPlugin.getDefault();
        workbench           = plugin.getWorkbench();
        workbenchWindow     = (WorkbenchWindow) workbench.getActiveWorkbenchWindow();
        menuMgr             = workbenchWindow.getMenuManager();
        menuMgr             = (MenuManager)menuMgr.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
        menu                = menuMgr.getMenu();

        menuMgr.addMenuListener(m_MenuHandlerEdit);
        menu.addMenuListener(m_MenuHandlerEdit);
    }

    /**
     * 
     */
    protected void setupToolBars()
    {
    }

    /**
     * 
     */
    protected void setupLocalMenus()
    {
    }

    /**
     * 
     */
    protected void setupLocalToolBars()
    {
        IViewSite       viewSite;
        IActionBars     actionBars;
        IToolBarManager toolBarMgr;
        
        viewSite    = getViewSite();
        actionBars  = viewSite.getActionBars();
        toolBarMgr  = actionBars.getToolBarManager();
        
        toolBarMgr.add(m_actionTerminalNewTerminal);
        toolBarMgr.add(m_actionTerminalConnect);
        toolBarMgr.add(m_actionTerminalDisconnect);
        toolBarMgr.add(m_actionTerminalSettings);
    }

    /**
     * 
     */
    protected void setupContextMenus()
    {
        StyledText                  ctlText;
        MenuManager                 menuMgr;
        Menu                        menu;
        TerminalContextMenuHandler  contextMenuHandler;

        ctlText             = m_ctlTerminal.getTextWidget();        
        menuMgr             = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menu                = menuMgr.createContextMenu(ctlText);
        contextMenuHandler  = new TerminalContextMenuHandler();
        
        ctlText.setMenu(menu);
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(contextMenuHandler);
        menu.addMenuListener(contextMenuHandler);
    }
    
    /**
     * 
     */
    protected void loadContextMenus(IMenuManager menuMgr)
    {
        menuMgr.add(m_actionEditCopy);
        menuMgr.add(m_actionEditPaste);
        menuMgr.add(new Separator());
        menuMgr.add(m_actionEditClearAll);
        menuMgr.add(m_actionEditSelectAll);

        // Other plug-ins can contribute there actions here
        menuMgr.add(new Separator("Additions")); //$NON-NLS-1$
    }

    /**
     * 
     */
    protected void setupListeners(Composite wndParent)
    {
        getViewSite().getPage().addPartListener(new IPartListener() {
                public void partClosed(IWorkbenchPart part)
                {
                    if (part instanceof TerminalView)
                        part.dispose();
                }

                public void partActivated(IWorkbenchPart part) { }
                public void partBroughtToTop(IWorkbenchPart part) { }
                public void partDeactivated(IWorkbenchPart part) { }
                public void partOpened(IWorkbenchPart part) { }
            });

        FontRegistry fontRegistry = JFaceResources.getFontRegistry();
        m_PropertyChangeHandler = new TerminalPropertyChangeHandler();
        fontRegistry.addListener(m_PropertyChangeHandler);
    }

    // Inner classes
    
    /**
      * 
      */
     protected class TerminalMenuHandlerEdit 
        implements  MenuListener,
                    IMenuListener
     {
         /**
          * 
          */
         protected String   m_strActionDefinitionIdCopy;
         protected String   m_strActionDefinitionIdPaste;
         protected String   m_strActionDefinitionIdSelectAll;

         protected int      m_nAcceleratorCopy;
         protected int      m_nAcceleratorPaste;
         protected int      m_nAcceleratorSelectAll;

         /**
          * 
          */
         protected TerminalMenuHandlerEdit()
         {
             super();
             
             m_strActionDefinitionIdCopy        = ""; //$NON-NLS-1$
             m_strActionDefinitionIdPaste       = ""; //$NON-NLS-1$
             m_strActionDefinitionIdSelectAll   = ""; //$NON-NLS-1$

             m_nAcceleratorCopy                 = 0;
             m_nAcceleratorPaste                = 0;
             m_nAcceleratorSelectAll            = 0;
         }
        
         // IMenuListener interface

         /**
          * 
          */
         public void menuAboutToShow(IMenuManager menuMgr)
         {
             ActionContributionItem item;
             RetargetAction         action;

             m_bMenuAboutToShow = true;
             execute(ON_UPDATE_EDIT_COPY,null);
             execute(ON_UPDATE_EDIT_CUT,null);
             execute(ON_UPDATE_EDIT_PASTE,null);
             execute(ON_UPDATE_EDIT_SELECTALL,null);

             item = (ActionContributionItem)menuMgr.find(ActionFactory.COPY.getId());
             action                              = (RetargetAction) item.getAction();
             m_strActionDefinitionIdCopy         = action.getActionDefinitionId();
             m_nAcceleratorCopy                  = action.getAccelerator();
             action.setActionDefinitionId(null);
             action.enableAccelerator(false);
             item.update();

             item = (ActionContributionItem)menuMgr.find(ActionFactory.PASTE.getId());
             action                              = (RetargetAction) item.getAction();
             m_strActionDefinitionIdPaste        = action.getActionDefinitionId();
             m_nAcceleratorPaste                 = action.getAccelerator();
             action.setActionDefinitionId(null);
             action.enableAccelerator(false);
             item.update();

             item = (ActionContributionItem)menuMgr.find(ActionFactory.SELECT_ALL.getId());
             action                              = (RetargetAction) item.getAction();
             m_strActionDefinitionIdSelectAll    = action.getActionDefinitionId();
             m_nAcceleratorSelectAll             = action.getAccelerator();
             action.setActionDefinitionId(null);
             action.enableAccelerator(false);
             item.update();
         }
         
         // MenuListener interface

         /**
          * 
          */
         public void menuShown(MenuEvent event)
         {
         }
         
         /**
          * 
          */
         public void menuHidden(MenuEvent event)
         {
             TerminalPlugin         plugin;
             IWorkbench             workbench;
             WorkbenchWindow        workbenchWindow;
             MenuManager            menuMgr;
             ActionContributionItem item;
             RetargetAction         action;

             m_bMenuAboutToShow = false;
             execute(ON_UPDATE_EDIT_COPY,null);
             execute(ON_UPDATE_EDIT_CUT,null);
             
             plugin             = TerminalPlugin.getDefault();
             workbench          = plugin.getWorkbench();
             workbenchWindow    = (WorkbenchWindow) workbench.getActiveWorkbenchWindow();
             menuMgr            = workbenchWindow.getMenuManager();
             menuMgr = (MenuManager) menuMgr.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
             
             item               = (ActionContributionItem) menuMgr.find(ActionFactory.COPY.getId());
             action             = (RetargetAction) item.getAction();
             action.setActionDefinitionId(m_strActionDefinitionIdCopy);
             action.setAccelerator(m_nAcceleratorCopy);
             action.enableAccelerator(true);
             item.update();

             item = (ActionContributionItem) menuMgr.find(ActionFactory.PASTE.getId());
             action             = (RetargetAction) item.getAction();
             action.setActionDefinitionId(m_strActionDefinitionIdPaste);
             action.setAccelerator(m_nAcceleratorPaste);
             action.enableAccelerator(true);
             item.update();

             item = (ActionContributionItem) menuMgr.find(ActionFactory.SELECT_ALL.getId());
             action             = (RetargetAction) item.getAction();
             action.setActionDefinitionId(m_strActionDefinitionIdSelectAll);
             action.setAccelerator(m_nAcceleratorSelectAll);
             action.enableAccelerator(true);
             item.update();
         }
     }

    /**
     * 
     */
    protected class TerminalContextMenuHandler 
        implements MenuListener, IMenuListener
    {
        /**
         * 
         */
        protected TerminalContextMenuHandler()
        {
            super();
        }
        
        // MenuListener interface

        /**
         * 
         */
        public void menuHidden(MenuEvent event) 
        {
            m_bMenuAboutToShow = false;
            execute(ON_UPDATE_EDIT_COPY,null);
        }
        
        public void menuShown(MenuEvent e) 
        {
        }

        // IMenuListener interface

        /**
         * 
         */
        public void menuAboutToShow(IMenuManager menuMgr)
        {
            m_bMenuAboutToShow = true;
            execute(ON_UPDATE_EDIT_COPY,null);
            execute(ON_UPDATE_EDIT_PASTE,null);
            execute(ON_UPDATE_EDIT_CLEARALL,null);
            execute(ON_UPDATE_EDIT_SELECTALL,null);

            loadContextMenus(menuMgr);
        }
    }
    
    /**
     * 
     */
    protected class TerminalPropertyChangeHandler implements IPropertyChangeListener
    {
        /**
         * 
         */
        protected TerminalPropertyChangeHandler()
        {
            super();
        }
        
        // IPropertyChangeListener interface

        public void propertyChange(PropertyChangeEvent event)
        {
            String strProperty;
            
            strProperty = event.getProperty();
            if (strProperty.equals(TERMINAL_FONT_DEFINITION)) 
            {
                execute(ON_TERMINAL_FONTCHANGED,event);
            }
            else
            {
            }
        }
    }    
}
