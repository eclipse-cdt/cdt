/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Fix 154874 - handle files with space or $ in the name 
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [174945] Remove obsolete icons from rse.shells.ui
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty() 
 * Martin Oberhuber (Wind River) - [187218] Fix error reporting for connect()
 * Kevin Doyle (IBM)			 - [187083] Launch Shell action available on folders inside virtual files 
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared 
 * Yu-Fen Kuo       (MontaVista) - Adapted from SystemCommandAction
 ********************************************************************************/

package org.eclipse.rse.internal.terminals.ui.actions;

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.terminals.ui.Activator;
import org.eclipse.rse.internal.terminals.ui.TerminalServiceHelper;
import org.eclipse.rse.internal.terminals.ui.views.TerminalViewer;
import org.eclipse.rse.internal.terminals.ui.views.TerminalsUI;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.terminals.core.ITerminalServiceSubSystem;
import org.eclipse.rse.subsystems.terminals.core.elements.TerminalElement;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Shell;

/**
 * action to launch a terminal from either the terminal subsystem or selected
 * directory
 * 
 */
public class LaunchTerminalAction extends SystemBaseAction {

    private ITerminalServiceSubSystem subSystem;
    private IRemoteFile selected;
    private ISystemFilterReference selectedFilterRef;

    public LaunchTerminalAction(String text, ImageDescriptor image, Shell shell) {
        super(text, image, shell);
    }

    /**
     * Constructor for LaunchTerminalAction
     * 
     * @param parent
     * @param subSystem
     *                the terminal subsystem to use if launching a terminal
     */
    public LaunchTerminalAction(Shell parent,
            ITerminalServiceSubSystem subSystem) {
        this(Activator.getResourceString("LaunchTerminalAction.title"), //$NON-NLS-1$
                Activator.getResourceString("LaunchTerminalAction.tooltip"), //$NON-NLS-1$
                parent, subSystem);
    }

    /**
     * Constructor for LaunchTerminalAction
     * 
     * @param title
     *                title of the action
     * @param tooltip
     *                tooltip of the action
     * @param parent
     * @param subSystem
     *                the terminal subsystem to use if launching a terminal
     */
    public LaunchTerminalAction(String title, String tooltip, Shell parent,
            ITerminalServiceSubSystem subSystem) {
        this(title, tooltip, Activator.getDefault().getImageDescriptor(
                Activator.ICON_ID_LAUNCH_TERMINAL), parent,

        subSystem);
    }

    /**
     * Constructor for LaunchTerminalAction
     * 
     * @param title
     *                title of the action
     * @param tooltip
     *                tooltip of the action
     * @param descriptor
     *                image descriptor to be displayed for this action
     * @param parent
     * @param subSystem
     *                the terminal subsystem to use if launching a terminal
     */
    public LaunchTerminalAction(String title, String tooltip,
            ImageDescriptor descriptor, Shell parent,
            ITerminalServiceSubSystem subSystem) {
        super(title, tooltip, descriptor, parent);
        this.subSystem = subSystem;

    }

    /**
     * @return the terminal subsystem
     */
    public ITerminalServiceSubSystem getSubSystem() {
        return subSystem;
    }

    /**
     * settor for the terminal subsystem
     * 
     * @param subSystem
     *                terminal subsystem
     */
    public void setSubSystem(ITerminalServiceSubSystem subSystem) {
        this.subSystem = subSystem;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.rse.ui.actions.SystemBaseAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
     */
    public boolean updateSelection(IStructuredSelection selection) {
        boolean enable = false;

        Iterator e = selection.iterator();
        Object selectedObject = e.next();

        if (selectedObject != null) {
            if (selectedObject instanceof ISystemFilterReference) {
                selectedFilterRef = (ISystemFilterReference) selectedObject;
                selected = null;
                enable = true;
            }
            if (selectedObject instanceof IRemoteFile) {
                selected = (IRemoteFile) selectedObject;
                selectedFilterRef = null;
                IHost host = selected.getParentRemoteFileSubSystem().getHost();
                subSystem = TerminalServiceHelper.getTerminalSubSystem(host);
                // If the selected object is a virtual folder then we need to
                // select the parent
                // of the archive
                if (ArchiveHandlerManager.isVirtual(selected.getAbsolutePath())) {
                    IRemoteFileSubSystem rfss = selected
                            .getParentRemoteFileSubSystem();
                    String file = selected.getAbsolutePath();
                    // Get the archive's path
                    file = file.substring(0, file
                            .indexOf(ArchiveHandlerManager.VIRTUAL_SEPARATOR));
                    // Get the parent of the archive's path
                    file = file.substring(0, file.lastIndexOf(selected
                            .getSeparator()));
                    try {
                        selected = rfss.getRemoteFileObject(file, null);
                    } catch (SystemMessageException exc) {
                    }
                }
                if (!selected.isFile()) {
                    enable = checkObjectType(selected);
                }
            } else if (selectedObject instanceof ITerminalServiceSubSystem) {
                subSystem = (ITerminalServiceSubSystem) selectedObject;
                enable = true;
            }
        }

        return enable;
    }

    private ITerminalServiceSubSystem getTerminalSubSystem() {
        IHost currentHost = null;

        if (selectedFilterRef != null) {
            ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) selectedFilterRef)
                    .getAdapter(ISystemViewElementAdapter.class);
            if (adapter != null) {
                ISubSystem ss = adapter.getSubSystem(selectedFilterRef);
                if (ss != null) {
                    currentHost = ss.getHost();
                }
            }
        } else if (selected != null) {
            currentHost = selected.getSystemConnection();
        }
        if (currentHost != null) {
            return TerminalServiceHelper.getTerminalSubSystem(currentHost);

        }

        return getSubSystem();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.rse.ui.actions.SystemBaseAction#run()
     */
    public void run() {
        if (selectedFilterRef != null) {
            ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) selectedFilterRef)
                    .getAdapter(ISystemViewElementAdapter.class);
            if (adapter != null) {
                ISubSystem ss = adapter.getSubSystem(selectedFilterRef);
                if (ss != null) {
                    Object target = ss.getTargetForFilter(selectedFilterRef);
                    if (target != null && target instanceof IRemoteFile) {
                        selected = (IRemoteFile) target;
                    }
                }
            }
        }

        ITerminalServiceSubSystem terminalSubSystem = getTerminalSubSystem();
        if (terminalSubSystem != null) {
            TerminalsUI terminalsUI = TerminalsUI.getInstance();
            TerminalViewer viewer = terminalsUI.activateTerminalsView();
            if (!terminalSubSystem.isConnected()) {
                try {
                    terminalSubSystem.connect(new NullProgressMonitor(), false);

                } catch (Exception e) {
                    Activator.logError(e.getLocalizedMessage(), e);
                }
            }
            if (terminalSubSystem.isConnected()) {
                CTabItem tab = viewer.getTabFolder().createTabItem(
                        terminalSubSystem.getHost(), getInitialDirectoryCmd());
                TerminalElement element = TerminalServiceHelper
                        .createTerminalElement(tab, terminalSubSystem);
                terminalSubSystem.addChild(element);

            }
        }
    }

    private String getInitialDirectoryCmd() {
        if (selected == null)
            return null;
        String path = TerminalServiceHelper.getWorkingDirectory(selected);

        String cdCmd = "cd " + PathUtility.enQuoteUnix(path); //$NON-NLS-1$
        if (getTerminalSubSystem().getHost().getSystemType().isWindows()) {
            cdCmd = "cd /d \"" + path + '\"'; //$NON-NLS-1$
        }
        return cdCmd + "\r"; //$NON-NLS-1$
    }

}
