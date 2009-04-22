/********************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [174945] Remove obsolete icons from rse.shells.ui
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * David McKnight   (IBM)        - [165680] "Show in Remote Shell View" does not work
 * Kevin Doyle      (IBM)        - [198534] Shell Menu Enablement Issue's
 * Radoslav Gerganov(ProSyst)    - [181563] Fix hardcoded Ctrl+Space for remote shell content assist
 * Yu-Fen Kuo       (MontaVista) - Adapted from SystemCommandsViewPart
 * Anna Dushistova  (MontaVista) - Adapted from SystemCommandsViewPart
 * Yu-Fen Kuo       (MontaVista) - [227572] RSE Terminal doesn't reset the "connected" state when the shell exits
 * Anna Dushistova  (MontaVista) - [228577] [rseterminal] Clean up RSE Terminal impl
 * Anna Dushistova  (MontaVista) - [238257] Request a help text when no tab is open in "Remote Shell", "Remote Monitor" and "Terminals" views
 * Anna Dushistova  (MontaVista) - [235097] [rseterminal] Cannot activate RSE Terminals View with the keyboard  
 * Anna Dushistova  (MontaVista) - [267609] [rseterminal] The first "Launch Terminal" command creates no terminal tab 
 *********************************************************************************/
package org.eclipse.rse.internal.terminals.ui.views;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.terminals.ui.TerminalUIResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.terminals.core.elements.TerminalElement;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.model.ISystemShellProvider;
import org.eclipse.rse.ui.view.IRSEViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

public class TerminalViewer extends ViewPart implements ISelectionListener,
        SelectionListener, ISelectionChangedListener,
        ISystemResourceChangeListener, ISystemShellProvider, IRSEViewPart,
        IMenuListener, ISystemMessageLine {

	private TerminalViewTab tabFolder;

	private PageBook pagebook;

	private Label noTabShownLabel;

    public static String VIEW_ID = "org.eclipse.rse.terminals.ui.view.TerminalView"; //$NON-NLS-1$

    public void createPartControl(Composite parent) {
    	pagebook = new PageBook(parent, SWT.NONE);
    	
        tabFolder = new TerminalViewTab(pagebook, this);
        tabFolder.getFolder().addSelectionListener(this);

        // Page 2: Nothing selected
        noTabShownLabel = new Label(pagebook, SWT.TOP + SWT.LEFT + SWT.WRAP);
        noTabShownLabel.setText(TerminalUIResources.TerminalViewer_text);   
        showEmptyPage();
        
        
        ISelectionService selectionService = getSite().getWorkbenchWindow()
                .getSelectionService();
        selectionService.addSelectionListener(this);

        ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();

        registry.addSystemResourceChangeListener(this);

        fillLocalToolBar();

    }

    public void setFocus() {
    	tabFolder.setFocus();
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        // TODO Auto-generated method stub

    }

    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

    public void widgetSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

    public void selectionChanged(SelectionChangedEvent event) {
        // TODO Auto-generated method stub

    }

    public void systemResourceChanged(ISystemResourceChangeEvent event) {
        if (event.getType() == ISystemResourceChangeEvents.EVENT_COMMAND_SHELL_REMOVED) {
            Object source = event.getSource();
            if (source instanceof TerminalElement) {
                tabFolder.disposePageFor(((TerminalElement) source).getName());
            }
        }else if(event.getType() == ISystemResourceChangeEvents.EVENT_REFRESH){
        	if(tabFolder.getItemCount() == 0)
            	showEmptyPage();
        	else
        		showTabsPage();
        }
    }

    public Shell getShell() {
        // TODO Auto-generated method stub
        return null;
    }

    public Viewer getRSEViewer() {
        // TODO Auto-generated method stub
        return null;
    }

    public void menuAboutToShow(IMenuManager manager) {
        // TODO Auto-generated method stub

    }

    public void clearErrorMessage() {
        // TODO Auto-generated method stub

    }

    public void clearMessage() {
        // TODO Auto-generated method stub

    }

    public String getErrorMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    public SystemMessage getSystemErrorMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setErrorMessage(String message) {
        // TODO Auto-generated method stub

    }

    public void setErrorMessage(SystemMessage message) {
        // TODO Auto-generated method stub

    }

    public void setErrorMessage(Throwable exc) {
        // TODO Auto-generated method stub

    }

    public void setMessage(String message) {
        // TODO Auto-generated method stub

    }

    public void setMessage(SystemMessage message) {
        // TODO Auto-generated method stub

    }

    public void fillLocalToolBar() {
        if (tabFolder != null) {
            // IActionBars actionBars = getViewSite().getActionBars();
            //
            // if (_shellActions == null || _shellActions.size() == 0)
            // {
            // updateShellActions();
            // _clearAction = new ClearAction();
            // _printTableAction = new SystemTablePrintAction(getTitle(), null);
            // IMenuManager menuManager = actionBars.getMenuManager();
            // addMenuItems(menuManager);
            // _statusLine = actionBars.getStatusLineManager();
            // }
            // IToolBarManager toolBarManager = actionBars.getToolBarManager();
            // addToolBarItems(toolBarManager);
            //
            // updateActionStates();
        }
    }

    public TerminalViewTab getTabFolder() {
        return tabFolder;
    }
    
    private void showEmptyPage() {
            pagebook.showPage(noTabShownLabel);
    }
    
    private void showTabsPage(){
        pagebook.showPage(tabFolder);
    }

}