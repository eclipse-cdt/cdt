/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is 
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Initial Contributors:
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 ********************************************************************************/
package org.eclipse.rse.core.subsystems;

public interface ICredentials {
	
	String getPassword();
	
	String getUserId();
	
}
