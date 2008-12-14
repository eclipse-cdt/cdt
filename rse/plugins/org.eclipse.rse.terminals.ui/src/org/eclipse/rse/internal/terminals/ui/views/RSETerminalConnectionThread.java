/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova (MontaVista) - initial API and implementation
 * Anna Dushistova (MontaVista) - [228577] [rseterminal] Clean up RSE Terminal impl
 * Anna Dushistova (MontaVista) - [246592] Terminal session issues "cd /
 ********************************************************************************/
package org.eclipse.rse.internal.terminals.ui.views;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.internal.terminals.ui.TerminalServiceHelper;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.terminals.ITerminalService;
import org.eclipse.rse.services.terminals.ITerminalShell;
import org.eclipse.rse.subsystems.terminals.core.ITerminalServiceSubSystem;
import org.eclipse.rse.subsystems.terminals.core.TerminalServiceSubSystem;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

public class RSETerminalConnectionThread extends Thread {
    private final ITerminalControl fControl;
    private final RSETerminalConnectorImpl fConn;
    private ITerminalShell shell;

    public RSETerminalConnectionThread(RSETerminalConnectorImpl conn,
            ITerminalControl control) {
        super();
        fControl = control;
        fConn = conn;
        fControl.setState(TerminalState.CONNECTING);
    }

    public void run() {
        ITerminalServiceSubSystem subsystem = TerminalServiceHelper
                .getTerminalSubSystem(fConn.host);

        try {
            subsystem.connect(new NullProgressMonitor(), false);
        } catch (Exception e1) {
            connectFailed(e1.getMessage(), e1.getMessage());
        }
        try {
            if (subsystem instanceof TerminalServiceSubSystem) {
				ITerminalService ts = ((TerminalServiceSubSystem) subsystem).getTerminalService();
				shell = ts.launchTerminal("ansi", null, null, null, null, new NullProgressMonitor());
			}
            fConn.setInputStream(shell.getInputStream());
            fConn.setOutputStream(shell.getOutputStream());
        } catch (SystemMessageException e) {
        	RSECorePlugin.getDefault().getLogger().logError("Error launching terminal", e); //$NON-NLS-1$
        }
        fConn.setTerminalHostShell(shell);
        fControl.setState(TerminalState.CONNECTED);
        try {
            // this is the workaround to delay read data
            // otherwise we might end of with race condition to processText
            // before dimension is set.
            Thread.sleep(500);
            // read data until the connection gets terminated
            readDataForever(fConn.getInputStream());
        } catch (InterruptedIOException e) {
            // we got interrupted: we are done...
        } catch (IOException e) {
        	RSECorePlugin.getDefault().getLogger().logError("Error while reading data", e); //$NON-NLS-1$
        } catch (InterruptedException e) {
        }
        // when reading is done, we set the state to closed
        fControl.setState(TerminalState.CLOSED);
    }

    /**
     * disconnect the ssh session
     */
    void disconnect() {
        interrupt();
    }

    /**
     * Read the data from the connection and display it in the terminal.
     *
     * @param in
     * @throws IOException
     */
    private void readDataForever(InputStream in) throws IOException {
        // read the data
        byte bytes[] = new byte[32 * 1024];
        int n;
        // read until the thread gets interrupted....
        while ((n = in.read(bytes)) != -1) {
            fControl.getRemoteToTerminalOutputStream().write(bytes, 0, n);
        }
    }

    private void connectFailed(String terminalText, String msg) {
        Logger.log(terminalText);
        fControl.displayTextInTerminal(terminalText);
        fControl.setState(TerminalState.CLOSED);
        fControl.setMsg(msg);
    }
}
