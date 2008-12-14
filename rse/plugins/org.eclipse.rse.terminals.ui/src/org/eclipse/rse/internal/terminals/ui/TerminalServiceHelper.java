/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo      (MontaVista) - initial API and implementation
 * Yu-Fen Kuo      (MontaVista) - [227572] RSE Terminal doesn't reset the "connected" state when the shell exits
 * Anna Dushistova (MontaVista) - [227535] [rseterminal][api] terminals.ui should not depend on files.core
 * Anna Dushistova (MontaVista) - [227569] [rseterminal][api] Provide a "generic" Terminal subsystem
 ********************************************************************************/

package org.eclipse.rse.internal.terminals.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.terminals.ui.views.RSETerminalConnector;
import org.eclipse.rse.internal.terminals.ui.views.TerminalViewTab;
import org.eclipse.rse.services.terminals.ITerminalShell;
import org.eclipse.rse.subsystems.terminals.core.ITerminalServiceSubSystem;
import org.eclipse.rse.subsystems.terminals.core.elements.TerminalElement;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;

public class TerminalServiceHelper {

    /**
     * Constructor for TerminalServiceHelper.
     */
    public TerminalServiceHelper() {
        super();
    }

    public static ITerminalServiceSubSystem getTerminalSubSystem(
            IHost connection) {
        ISystemRegistry systemRegistry = RSECorePlugin.getTheSystemRegistry();
        ISubSystem[] subsystems = systemRegistry.getSubSystems(connection);
        for (int i = 0; i < subsystems.length; i++) {
        	if (subsystems[i] instanceof ITerminalServiceSubSystem) {
                ITerminalServiceSubSystem subSystem = (ITerminalServiceSubSystem) subsystems[i];
                return subSystem;
            }
        }
        return null;
    }

    public static ITerminalServiceSubSystem[] getTerminalSubSystems(
            IHost connection) {
        List results = new ArrayList();
        ISystemRegistry systemRegistry = RSECorePlugin.getTheSystemRegistry();
        ISubSystem[] subsystems = systemRegistry.getSubSystems(connection);
        for (int i = 0; i < subsystems.length; i++) {
            if (subsystems[i] instanceof ITerminalServiceSubSystem) {
                ITerminalServiceSubSystem subSystem = (ITerminalServiceSubSystem) subsystems[i];
                results.add(subSystem);
            }
        }
        return (ITerminalServiceSubSystem[]) results
                .toArray(new ITerminalServiceSubSystem[results.size()]);
    }

    public static TerminalElement createTerminalElement(CTabItem item,
            ITerminalServiceSubSystem terminalServiceSubSystem) {
        TerminalElement element = new TerminalElement(item.getText(),
                terminalServiceSubSystem);
        return element;
    }

    public static void removeTerminalElementFromHost(CTabItem item, IHost host) {
        ITerminalServiceSubSystem terminalServiceSubSystem = getTerminalSubSystem(host);
        if (terminalServiceSubSystem != null) {
            TerminalElement element = terminalServiceSubSystem.getChild(item.getText());
            terminalServiceSubSystem.removeChild(element);
        }

    }

    public static void updateTerminalShellForTerminalElement(CTabItem item) {
        Object data = item.getData();
        if (data instanceof IHost){
            IHost host = (IHost) data;
            ITerminalServiceSubSystem terminalServiceSubSystem = TerminalServiceHelper.getTerminalSubSystem(host);
            TerminalElement element = terminalServiceSubSystem.getChild(item.getText());
            if (element != null){
                ITerminalShell terminalShell = getTerminalShellFromTab(item);
                if (element.getTerminalShell() != terminalShell){
                    element.setTerminalShell(terminalShell);
                }
            }
        }
    }
    private static ITerminalShell getTerminalShellFromTab(CTabItem item) {
        ITerminalShell terminalShell = null;
        ITerminalViewControl terminalViewControl = (ITerminalViewControl) item
                .getData(TerminalViewTab.DATA_KEY_CONTROL);
        ITerminalConnector terminalConnector = terminalViewControl
                .getTerminalConnector();
        if (terminalConnector instanceof RSETerminalConnector) {
            RSETerminalConnector rseTerminalConnector = (RSETerminalConnector) terminalConnector;
            terminalShell = rseTerminalConnector.getTerminalHostShell();
        }
        return terminalShell;
    }
}