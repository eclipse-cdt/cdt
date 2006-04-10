/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.shells.ui.view;

import org.eclipse.rse.services.clientserver.messages.SystemMessage;

/**
 * Interface that is to be implemented by anyone interested in 
 *  listening to modify events on the command widget.
 */
public interface ISystemCommandTextModifyListener 
{
	
	/**
	 * Callback from SystemCommandTextField when the user modifies the command.
	 * @param cmdText - current contents of the field
	 * @param errorMessage - potential error detected by the default validator
	 */
	public void commandModified(String cmdText, SystemMessage errorMessage);
	
}