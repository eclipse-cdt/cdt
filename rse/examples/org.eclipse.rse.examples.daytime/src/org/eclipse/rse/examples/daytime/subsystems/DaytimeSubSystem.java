/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * David Dykstal (IBM) - [217556] remove service subsystem types
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Martin Oberhuber (Wind River) - [218304] Improve deferred adapter loading
 * David McKnight   (IBM)        - [272882] [api] Handle exceptions in IService.initService()
 *******************************************************************************/

package org.eclipse.rse.examples.daytime.subsystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.examples.daytime.Activator;
import org.eclipse.rse.examples.daytime.model.DaytimeResource;
import org.eclipse.rse.examples.daytime.service.IDaytimeService;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.model.ISystemRegistryUI;

/**
 * This is our subsystem, which manages the remote connection and resources for
 * a particular Service (system connection) object.
 */
public class DaytimeSubSystem extends SubSystem {

	private IDaytimeService fDaytimeService;

	public DaytimeSubSystem(IHost host, IConnectorService connectorService, IDaytimeService daytimeService) {
		super(host, connectorService);
		fDaytimeService = daytimeService;
	}

	public void initializeSubSystem(IProgressMonitor monitor)throws SystemMessageException {
		//This is called after connect - expand the daytime node.
		// Always called in worker thread.
		super.initializeSubSystem(monitor);
		//TODO find a more elegant solution for expanding the item, e.g. use implicit connect like filters
        final ISystemRegistryUI sr = RSEUIPlugin.getTheSystemRegistryUI();
        final SystemResourceChangeEvent event = new SystemResourceChangeEvent(this, ISystemResourceChangeEvents.EVENT_SELECT_EXPAND, null);
        //TODO bug 150919: postEvent() should not be necessary asynchronously
        //sr.postEvent(event);
        Display.getDefault().asyncExec(new Runnable() {
        	public void run() { sr.postEvent(event); }
        });
	}

	public boolean hasChildren() {
		return isConnected();
	}

	public IDaytimeService getDaytimeService() {
		return fDaytimeService;
	}

	public Object[] getChildren() {
		if (isConnected()) {
			try {
				String daytime = fDaytimeService.getTimeOfDay();
				DaytimeResource node = new DaytimeResource(this);
				node.setDaytime(daytime);
				return new Object[] { node };
			} catch(Exception e) {
				String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_FAILED, getHostName());
				SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID, ICommonMessageIds.MSG_CONNECT_FAILED, IStatus.ERROR, msgTxt, e);
				SystemMessageObject msgobj = new SystemMessageObject(msg, ISystemMessageObject.MSGTYPE_ERROR,this);
				return new Object[] { msgobj };
			}
		} else {
			return new Object[0];
		}
	}

	public void uninitializeSubSystem(IProgressMonitor monitor) {
		super.uninitializeSubSystem(monitor);
	}

	public Class getServiceType() {
		return IDaytimeService.class;
	}

	public void switchServiceFactory(ISubSystemConfiguration factory) {
		// not applicable here

	}

}
