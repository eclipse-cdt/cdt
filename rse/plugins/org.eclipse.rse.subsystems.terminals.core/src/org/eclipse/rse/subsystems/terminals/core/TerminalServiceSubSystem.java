/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo      (MontaVista) - initial API and implementation
 * Yu-Fen Kuo      (MontaVista) - [227572] RSE Terminal doesn't reset the "connected" state when the shell exits
 * Anna Dushistova (MontaVista) - [228577] [rseterminal] Clean up RSE Terminal impl
 ********************************************************************************/

package org.eclipse.rse.subsystems.terminals.core;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
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

    protected ITerminalService _hostService = null;

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
        children = new ArrayList();
    }

    public TerminalServiceSubSystem(IHost host,
            IConnectorService connectorService, ITerminalService hostService) {
        super(host, connectorService);
        _hostService = hostService;
        children = new ArrayList();
    }

    public ITerminalService getTerminalService() {
        return _hostService;
    }

    public Class getServiceType() {
		return ITerminalService.class;
	}

	public void addChild(TerminalElement element) {
		synchronized (children) {
            children.add(element);
		}
        Display.getDefault().asyncExec(new Refresh(this));
    }

    public void removeChild(TerminalElement element) {
		synchronized (children) {
            if (children.size() > 0) {
                children.remove(element);
            }
		}
		Display.getDefault().asyncExec(new Refresh(this));
    }

    public void removeChild(String terminalTitle) {
        if (children.size() > 0) {
            TerminalElement element = getChild(terminalTitle);
            if (element != null){
        		synchronized (children) {
                    children.remove(element);
        		}
                Display.getDefault().asyncExec(new Refresh(this));
            }
        }
        
    }
    public TerminalElement getChild(String terminalTitle) {
        if (children != null) {
            Iterator iterator = children.iterator();
            while (iterator.hasNext()){
                TerminalElement element = (TerminalElement)iterator.next();
                if (element.getName().equals(terminalTitle))
                    return element;
            }
        }
        return null;
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
            break;

        case CommunicationsEvent.BEFORE_DISCONNECT:
        case CommunicationsEvent.CONNECTION_ERROR:
            Display.getDefault().asyncExec(new Runnable(){
                public void run() {
                    cancelAllTerminals();
                }
            });
            break;
        default:
            break;
        }

    }

    public boolean isPassiveCommunicationsListener() {
        return true;
    }
    public void cancelAllTerminals() {
        if (children == null || children.size() == 0)
            return;

        for (int i = children.size() - 1; i >= 0; i--) {
            TerminalElement element = (TerminalElement) children.get(i);

            try {
                removeTerminalElement(element);
            } catch (Exception e) {
            	RSECorePlugin.getDefault().getLogger().logError("Error removing terminal", e); //$NON-NLS-1$
            }

        }

        children.clear();
        Display.getDefault().asyncExec(new Refresh(this));

    }

    private void removeTerminalElement(TerminalElement element) {
        element.getTerminalShell().exit();
        ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
        registry.fireEvent(new SystemResourceChangeEvent(element, ISystemResourceChangeEvents.EVENT_COMMAND_SHELL_REMOVED, null));
    }
    public void initializeSubSystem(IProgressMonitor monitor) {
        super.initializeSubSystem(monitor);
        getConnectorService().addCommunicationsListener(this);
    }

    public void uninitializeSubSystem(IProgressMonitor monitor) {
        getConnectorService().removeCommunicationsListener(this);
        super.uninitializeSubSystem(monitor);
    }
}