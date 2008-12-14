/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova (MontaVista) - initial API and implementation
 * Yu-Fen Kuo (MontaVista)      - [227572] RSE Terminal doesn't reset the "connected" state when the shell exits
 * Anna Dushistova (MontaVista) - [228577] [rseterminal] Clean up RSE Terminal impl
 ********************************************************************************/
package org.eclipse.rse.internal.terminals.ui.views;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.terminals.ITerminalShell;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;

public class RSETerminalConnectorImpl extends TerminalConnectorImpl {
    private OutputStream fOutputStream;
    private InputStream fInputStream;
    IHost host;
    private RSETerminalConnectionThread fConnection;
    private ITerminalShell shell;

    public RSETerminalConnectorImpl(IHost host) {
        super();
        this.host = host;
    }

    public void connect(ITerminalControl control) {
    	super.connect(control);
        fConnection = new RSETerminalConnectionThread(this, control);
        fConnection.start();
    }

    public void doDisconnect() {
        fConnection.disconnect();
        if (getInputStream() != null) {
            try {
                getInputStream().close();
            } catch (Exception exception) {
            	RSECorePlugin.getDefault().getLogger().logError("Error while closing input stream", exception); //$NON-NLS-1$
            }
        }

        if (getTerminalToRemoteStream() != null) {
            try {
                getTerminalToRemoteStream().close();
            } catch (Exception exception) {
            	RSECorePlugin.getDefault().getLogger().logError("Error while closing terminal-to-remote stream", exception); //$NON-NLS-1$
            }
        }
    }

    public OutputStream getTerminalToRemoteStream() {
        return fOutputStream;
    }

    public String getSettingsSummary() {
        return "RSE: " + host.getName();
    }

    public boolean isLocalEcho() {
        return shell.isLocalEcho();
    }

    public void setTerminalSize(int newWidth, int newHeight) {
    	if(shell != null)
    	{	
            shell.setTerminalSize(newWidth, newHeight);
    	}    
    }

    public InputStream getInputStream() {
        return fInputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        fOutputStream = outputStream;
    }

    public void setInputStream(InputStream inputStream) {
        fInputStream = inputStream;
    }

    public void setTerminalHostShell(ITerminalShell shell) {
        this.shell = shell;
    }

    public ITerminalShell getTerminalHostShell() {
        return shell;
    }

}
