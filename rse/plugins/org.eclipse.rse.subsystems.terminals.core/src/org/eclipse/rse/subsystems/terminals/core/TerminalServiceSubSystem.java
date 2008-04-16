/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo (MontaVista) - initial API and implementation
 ********************************************************************************/

package org.eclipse.rse.subsystems.terminals.core;

import java.util.ArrayList;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.internal.services.terminals.ITerminalService;
import org.eclipse.rse.subsystems.terminals.core.elements.TerminalElement;
import org.eclipse.swt.widgets.Display;

public final class TerminalServiceSubSystem extends SubSystem implements
        ITerminalServiceSubSystem, ICommunicationsListener {

    protected ITerminalService _hostService;

    private ArrayList children;

    public class Refresh implements Runnable {
        private TerminalServiceSubSystem _ss;

        public Refresh(TerminalServiceSubSystem ss) {
            _ss = ss;
        }

        public void run() {
            ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
            registry.fireEvent(new SystemResourceChangeEvent(_ss,
                    ISystemResourceChangeEvents.EVENT_REFRESH, _ss));
        }
    }

    protected TerminalServiceSubSystem(IHost host,
            IConnectorService connectorService) {
        super(host, connectorService);
    }

    public TerminalServiceSubSystem(IHost host,
            IConnectorService connectorService, ITerminalService hostService) {
        super(host, connectorService);
        _hostService = hostService;
    }

    public ITerminalService getTerminalService() {
        return _hostService;
    }

    public Class getServiceType() {
		return ITerminalService.class;
	}

	public void addChild(TerminalElement element) {
        if (children == null) {
            children = new ArrayList();
            // if this is first shell, start listening so that on disconnect, we
            // persist
            getConnectorService().addCommunicationsListener(this);
        }
        children.add(element);
        Display.getDefault().asyncExec(new Refresh(this));
    }

    public void removeChild(TerminalElement element) {
        if (children != null) {
            children.remove(element);
        }
        if (children == null) {
            getConnectorService().removeCommunicationsListener(this);
        }
        Display.getDefault().asyncExec(new Refresh(this));
    }

    public Object[] getChildren() {
        if (children != null)
            return children.toArray();
        return null;
    }

    public boolean hasChildren() {
        if (children != null && children.size() > 0)
            return true;
        return false;
    }

    public void setTerminalService(ITerminalService service) {
        _hostService = service;
    }

    public void communicationsStateChange(CommunicationsEvent e) {
        switch (e.getState()) {
        case CommunicationsEvent.AFTER_DISCONNECT:
            // no longer listen
            getConnectorService().removeCommunicationsListener(this);
            // if (_cmdShells != null) _cmdShells.clear();
            // if (_envVars != null) _envVars.clear();
            // _defaultShell = null;

            break;

        case CommunicationsEvent.BEFORE_DISCONNECT:
        case CommunicationsEvent.CONNECTION_ERROR:
            // remove all shells
            // saveShellState(_cmdShells);
            // if (getShells().length > 0)
            // {
            // Display.getDefault().asyncExec(new CancelAllShells());
            // // cancelAllShells();
            // }
            break;
        default:
            break;
        }

    }

    public boolean isPassiveCommunicationsListener() {
        return true;
    }

}