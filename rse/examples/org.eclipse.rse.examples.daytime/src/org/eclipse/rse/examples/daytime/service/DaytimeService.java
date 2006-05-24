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

package org.eclipse.rse.examples.daytime.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.examples.daytime.DaytimeResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;

/**
 * The DaytimeService implements the UI-less protocol for accessing the
 * daytime TCP service on a remote host. Other implementations of the
 * same interface might use other methods for retrieving the time of day.
 */
public class DaytimeService implements IDaytimeService {
	
	private String fHostname;

	public DaytimeService() {
		//nothing to do
	}

	public String getName() {
		return DaytimeResources.Daytime_Service_Name;
	}

	public String getDescription() {
		return DaytimeResources.Daytime_Service_Description;
	}

	public void initService(IProgressMonitor monitor) {
		//nothing to do
	}

	public SystemMessage getMessage(String messageID) {
		//dummy impl for now
		return null;
	}

	public void setHostName(String hostname) {
		fHostname = hostname;
	}

	public String getTimeOfDay() throws UnknownHostException, IOException {
		Socket s = new Socket(fHostname, 13);
		BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		String result = in.readLine();
		in.close();
		s.close();
		return result;
	}

	public void uninitService(IProgressMonitor monitor) {
		//nothing to do
	}

}
