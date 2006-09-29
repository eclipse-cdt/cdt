/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.widgets;

/**
 * Any listener that wants to be notified of state changes to the inheritable
 * entry field widget must implement this interface.
 */
public interface IInheritableEntryFieldStateChangeListener {
	
	/**
	 * Called when the state of the inheritable field has changed. The new state of the field can be queried by calling
	 * <code>isLocal</code> on on the entry field.
	 * @param field the inheritable field which has changed state.
	 */
	public void stateChanged(InheritableEntryField field);
}