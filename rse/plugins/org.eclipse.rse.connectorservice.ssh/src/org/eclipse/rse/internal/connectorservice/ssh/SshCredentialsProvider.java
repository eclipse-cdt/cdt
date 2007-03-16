/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - 168977: refactoring IConnectorService
 ********************************************************************************/

package org.eclipse.rse.internal.connectorservice.ssh;

import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.ui.subsystems.StandardCredentialsProvider;

public class SshCredentialsProvider extends StandardCredentialsProvider {
	
	public SshCredentialsProvider(IConnectorService connectorService) {
		super(connectorService);
	}
	
	public boolean requiresPassword() {
		return false;
	}

	public boolean requiresUserId() {
		return false;
	}
	
}
