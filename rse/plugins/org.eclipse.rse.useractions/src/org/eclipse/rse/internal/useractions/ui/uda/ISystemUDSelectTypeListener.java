package org.eclipse.rse.internal.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/**
 * @author coulthar
 *
 * This is an interface for listening for changes made to the
 * user-selected list of types for an action.
 * Used by the SystemUDSelectTypesForm class.
 */
public interface ISystemUDSelectTypeListener {
	/**
	 * The user has added or removed a type.
	 * Call getTypes() on given form to get the new list.
	 */
	public void selectedTypeListChanged(SystemUDSelectTypesForm form);

	/**
	 * The user has edited the master list of types. It needs to be refreshed.
	 * You must call setMasterTypes() to update the form's master type list
	 */
	public void masterTypeListChanged(SystemUDSelectTypesForm form);
}
