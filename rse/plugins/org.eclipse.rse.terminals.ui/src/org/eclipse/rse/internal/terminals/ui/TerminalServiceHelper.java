/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo (MontaVista) - initial API and implementation
 ********************************************************************************/

package org.eclipse.rse.internal.terminals.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.terminals.core.ITerminalServiceSubSystem;
import org.eclipse.rse.subsystems.terminals.core.elements.TerminalElement;
import org.eclipse.swt.custom.CTabItem;

public class TerminalServiceHelper {

    /**
     * Constructor for TerminalServiceHelper.
     */
    public TerminalServiceHelper() {
        super();
    }

    /**
     * Helper method to return the path to change-directory to, given a selected
     * remote file object
     */
    public static String getWorkingDirectory(IRemoteFile selectedFile) {
        String path = null;
        if (selectedFile.isDirectory())
            path = selectedFile.getAbsolutePath();
        else
            path = selectedFile.getParentPath();
        return path;
    }

    public static ITerminalServiceSubSystem getTerminalSubSystem(
            IHost connection) {
        ISystemRegistry systemRegistry = RSECorePlugin.getTheSystemRegistry();
        ISubSystem[] subsystems = systemRegistry.getSubSystems(connection);
        for (int i = 0; i < subsystems.length; i++) {
            if ("ssh.terminals".equals(subsystems[i].getSubSystemConfiguration().getId())) {
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
            TerminalElement element = new TerminalElement(item.getText(),
                    terminalServiceSubSystem);
            terminalServiceSubSystem.removeChild(element);
        }

    }
}