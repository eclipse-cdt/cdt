/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.validators;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;


/**
 * On remote selection dialogs, you can pass an implementation of this interface to validate that
 *  it is ok to enable the OK button when the user selects a remote object. If you return
 *  a SystemMessage, ok will be disabled and the message will be shown on the message line.
 *  Return a SystemMessage with blank in the first level text to disable OK without showing
 *  an error message.
 * <p>
 * Alternatively you can just subclass {@link org.eclipse.rse.ui.validators.ValidatorRemoteSelection}
 */
public interface IValidatorRemoteSelection 
{
	
	/**
	 * The user has selected one or more remote objects. Return null if OK is to be enabled, or a SystemMessage
	 *  if it is not to be enabled. The message will be displayed on the message line.
	 */
	public SystemMessage isValid(IHost selectedConnection, Object[] selectedObjects, ISystemRemoteElementAdapter[] remoteAdaptersForSelectedObject);
}