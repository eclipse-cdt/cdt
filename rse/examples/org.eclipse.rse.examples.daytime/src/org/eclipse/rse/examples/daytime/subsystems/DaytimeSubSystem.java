/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 *******************************************************************************/

package org.eclipse.rse.examples.daytime.subsystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.examples.daytime.model.DaytimeResource;
import org.eclipse.rse.examples.daytime.service.IDaytimeService;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemMessageObject;
import org.eclipse.rse.model.SystemMessageObject;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;

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

	public void initializeSubSystem(IProgressMonitor monitor) {
		// nothing to do
	}
	
	public boolean hasChildren() {
		return isConnected();
	}
	
	public IDaytimeService getDaytimeService() {
		return fDaytimeService;
	}
	
	public Object[] getChildren() {
		try {
			String daytime = fDaytimeService.getTimeOfDay();
			DaytimeResource node = new DaytimeResource(this);
			node.setDaytime(daytime);
			return new Object[] { node };
		} catch(Exception e) {
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_FAILED);
			msg.makeSubstitution(getHostName(), e);
			SystemMessageObject msgobj = new SystemMessageObject(msg, ISystemMessageObject.MSGTYPE_ERROR,this);
			return new Object[] { msgobj };
		}
	}

	public void uninitializeSubSystem(IProgressMonitor monitor) {
		//nothing to do
	}

}
