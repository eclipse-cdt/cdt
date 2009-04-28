/********************************************************************************
 * Copyright (c) 2008, 2009 MontaVista Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo       (MontaVista) - initial API and implementation
 * Yu-Fen Kuo       (MontaVista) - [227572] RSE Terminal doesn't reset the "connected" state when the shell exits
 * Anna Dushistova  (MontaVista) - [228577] [rseterminal] Clean up RSE Terminal impl
 * Martin Oberhuber (Wind River) - [228577] [rseterminal] Further cleanup
 * Anna Dushistova  (MontaVista) - [227569] [rseterminal][api] Provide a "generic" Terminal subsystem
 * Anna Dushistova  (MontaVista) - [257638] [rseterminal] Terminal subsystem doesn't have service properties
 * David McKnight   (IBM)        - [272882] [api] Handle exceptions in IService.initService()
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
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.terminals.ITerminalService;
import org.eclipse.rse.subsystems.terminals.core.elements.TerminalElement;
import org.eclipse.swt.widgets.Display;

/**
 * A Subsystem that has terminal instances as children.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the <a href="http://www.eclipse.org/dsdp/tm/">Target Management</a> team.
 * </p>
 */
public class TerminalServiceSubSystem extends SubSystem implements
		ITerminalServiceSubSystem, ICommunicationsListener {

	private ITerminalService _hostService = null;

	private ArrayList children = new ArrayList();

	/**
	 * Constructor.
	 *
	 * @since 1.0
	 */
	public TerminalServiceSubSystem(IHost host,
			IConnectorService connectorService, ITerminalService hostService) {
		super(host, connectorService);
		_hostService = hostService;
	}

	private void fireAsyncRefresh(final Object target) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
				registry.fireEvent(new SystemResourceChangeEvent(target,
						ISystemResourceChangeEvents.EVENT_REFRESH, target));

			}
		});
	}

	/**
	 * Return the Terminal Service associated with this subsystem.
	 *
	 * @since 1.0
	 */
	public ITerminalService getTerminalService() {
		return _hostService;
	}

	public Class getServiceType() {
		return ITerminalService.class;
	}

	public void addChild(TerminalElement element) {
		if (element != null) {
			synchronized (children) {
				children.add(element);
			}
			fireAsyncRefresh(this);
		}
	}

	public void removeChild(TerminalElement element) {
		if (element != null) {
			synchronized (children) {
				children.remove(element);
			}
			fireAsyncRefresh(this);
		}
	}

	public void removeChild(String terminalTitle) {
		removeChild(getChild(terminalTitle));
	}

	public TerminalElement getChild(String terminalTitle) {
		synchronized (children) {
			for (Iterator it = children.iterator(); it.hasNext();) {
				TerminalElement element = (TerminalElement) it.next();
				if (element.getName().equals(terminalTitle))
					return element;
			}
		}
		return null;
	}

	public Object[] getChildren() {
		synchronized (children) {
			return children.toArray();
		}
	}

	public boolean hasChildren() {
		synchronized (children) {
			return !children.isEmpty();
		}
	}

	/**
	 * Set the terminal service associated with this subsystem.
	 *
	 * @since 1.0
	 */
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
			Display.getDefault().asyncExec(new Runnable() {
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

	/**
	 * Set the terminal service associated with this subsystem.
	 */
	public void cancelAllTerminals() {
		Object[] terminals;
		synchronized (children) {
			terminals = getChildren();
			children.clear();
		}
		if (terminals.length > 0) {
			for (int i = terminals.length - 1; i >= 0; i--) {
				TerminalElement element = (TerminalElement) terminals[i];
				try {
					removeTerminalElement(element);
				} catch (Exception e) {
					RSECorePlugin.getDefault().getLogger().logError(
							"Error removing terminal", e); //$NON-NLS-1$
				}
			}
			fireAsyncRefresh(this);
		}
	}

	private void removeTerminalElement(TerminalElement element) {
		element.getTerminalShell().exit();
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		registry.fireEvent(new SystemResourceChangeEvent(element,
				ISystemResourceChangeEvents.EVENT_COMMAND_SHELL_REMOVED, null));
	}

	public void initializeSubSystem(IProgressMonitor monitor) throws SystemMessageException {
		super.initializeSubSystem(monitor);
		getConnectorService().addCommunicationsListener(this);
	}

	public void uninitializeSubSystem(IProgressMonitor monitor) {
		getConnectorService().removeCommunicationsListener(this);
		super.uninitializeSubSystem(monitor);
	}

	public boolean canSwitchTo(ISubSystemConfiguration configuration) {
		return (configuration instanceof ITerminalServiceSubSystemConfiguration);
	}

	/**
	 * @inheritDoc
	 * @since 1.0
	 */
	public ITerminalServiceSubSystemConfiguration getParentRemoteTerminalSubSystemConfiguration() {
		return (ITerminalServiceSubSystemConfiguration) super
				.getSubSystemConfiguration();
	}

}