/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [180562] dont implement ISystemOutputRemoteTypes
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [174945] Remove obsolete icons from rse.shells.ui
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [189130] Move SystemIFileProperties from UI to Core
 * David McKnight   (IBM)        - [196842] Don't have open menu for folders
 * Xuan Chen        (IBM)        - [160775] [api] rename (at least within a zip) blocks UI thread
 * David McKnight   (IBM)        - [189873] Improve remote shell editor open action with background jobs
 * David McKnight   (IBM)        - [216252] [nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * Xuan Chen        (IBM)        - [223126] [api][breaking] Remove API related to User Actions in RSE Core/UI
 * Yu-Fen Kuo       (MontaVista) - Adopted from SystemViewRemoteOutputAdapter
 * Anna Dushistova  (MontaVista) - Adopted from SystemViewRemoteOutputAdapter
 * Yu-Fen Kuo       (MontaVista) - [227572] RSE Terminal doesn't reset the "connected" state when the shell exits
 * Zhou Renjian     (Kortide)    - [282256] "null:..." status message for launched terminal
 *******************************************************************************/
package org.eclipse.rse.internal.terminals.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.internal.terminals.ui.Activator;
import org.eclipse.rse.internal.terminals.ui.TerminalUIResources;
import org.eclipse.rse.internal.terminals.ui.actions.RemoveTerminalAction;
import org.eclipse.rse.internal.terminals.ui.actions.ShowInTerminalViewAction;
import org.eclipse.rse.services.terminals.ITerminalShell;
import org.eclipse.rse.subsystems.terminals.core.ITerminalServiceSubSystemConfiguration;
import org.eclipse.rse.subsystems.terminals.core.elements.TerminalElement;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class TerminalViewElementAdapter extends AbstractSystemViewAdapter
        implements ISystemRemoteElementAdapter {
    private List actions = null;
    private ShowInTerminalViewAction showInTerminalViewAction;
    private RemoveTerminalAction closeTerminalAction;

    public TerminalViewElementAdapter() {
        actions = new ArrayList();
    }

    public void addActions(SystemMenuManager menu,
            IStructuredSelection selection, Shell parent, String menuGroup) {
        Object firstSelection = selection.getFirstElement();

        if (firstSelection != null) {
            if (firstSelection instanceof TerminalElement) {
                TerminalElement cmdShell = (TerminalElement) firstSelection;
                if (showInTerminalViewAction == null) {
                    showInTerminalViewAction = new ShowInTerminalViewAction(
                            getShell());

                }
                menu.add(ISystemContextMenuConstants.GROUP_OPEN,
                        showInTerminalViewAction);

                getTerminalActions((ITerminalServiceSubSystemConfiguration) cmdShell
                        .getSubSystem().getSubSystemConfiguration());

                menu.add(ISystemContextMenuConstants.GROUP_CHANGE,
                        closeTerminalAction);
            }
        } else {
            return;
        }
    }

    public List getTerminalActions(ITerminalServiceSubSystemConfiguration factory) {
        actions.clear();
        if (actions.size() == 0) {
            if (closeTerminalAction == null) {
                closeTerminalAction = new RemoveTerminalAction(getShell());
            }
            actions.add(closeTerminalAction);
        }
        return actions;
    }

    public Object[] getChildren(IAdaptable element, IProgressMonitor monitor) {
        return null;
    }

    public ImageDescriptor getImageDescriptor(Object element) {
        if (element instanceof TerminalElement){
            TerminalElement terminalElement = (TerminalElement)element;
            ITerminalShell terminalShell = terminalElement.getTerminalShell();
            if (terminalShell != null){
                if (terminalShell.isActive())
                    return Activator.getDefault().getImageDescriptor(
                            Activator.ICON_ID_TERMINAL_SUBSYSTEM_LIVE);
            }
        }
        return Activator.getDefault().getImageDescriptor(
                Activator.ICON_ID_TERMINAL_SUBSYSTEM);
    }

    public Object getParent(Object element) {
        return null;
    }

    public String getType(Object element) {
        return TerminalUIResources.TerminalViewElementAdapter_type;
    }

    public boolean hasChildren(IAdaptable element) {
        return false;
    }

    protected IPropertyDescriptor[] internalGetPropertyDescriptors() {
        return null;
    }

    protected Object internalGetPropertyValue(Object key) {
        return null;
    }

    public String getAbsoluteParentName(Object element) {
        return null;
    }

    public Object getRemoteParent(Object element, IProgressMonitor monitor)
            throws Exception {
         return null;
    }

    public String[] getRemoteParentNamesInUse(Object element,
            IProgressMonitor monitor) throws Exception {
        return null;
    }

    public boolean refreshRemoteObject(Object oldElement, Object newElement) {
        return false;
    }

    public String getRemoteSubType(Object element) {
        return null;
    }

    public String getRemoteType(Object element) {
        return null;
    }

    public String getRemoteTypeCategory(Object element) {
        return null;
    }

    public String getSubSystemConfigurationId(Object element) {
        return null;
    }

    public String getText(Object element) {
        return element.toString();
    }

    public String getAbsoluteName(Object object) {
        return object.toString();
    }

}
