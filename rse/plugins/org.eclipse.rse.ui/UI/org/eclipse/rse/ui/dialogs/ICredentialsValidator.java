/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David Dykstal (IBM) - Changed from ISignonValidator to generalize the concept and
 * remove the UI dependencies.
 ********************************************************************************/

package org.eclipse.rse.ui.dialogs;

import org.eclipse.rse.core.subsystems.ICredentials;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;


/**
 * Interface for providing a credentials validator. This will be used when
 * credentials are acquired or when they are about to be used. Can check 
 * credentials for expiration or for validity.
 * <p>
 * Any context will need to be set in the implementations of this interface
 * prior to its validation.
 */
public interface ICredentialsValidator {
	
	/**
	 * Verify if credentials are valid.
	 * 
	 * @param credentials The credentials to be validated.
	 * 
	 * @return null if the credentials are valid, a SystemMessage describing the 
	 * type of problem if invalid.
	 */	
	public SystemMessage validate(ICredentials credentials);
		
}