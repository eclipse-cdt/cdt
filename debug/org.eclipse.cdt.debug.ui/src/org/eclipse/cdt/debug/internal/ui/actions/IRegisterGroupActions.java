/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Alvaro Sanchez-Leon (Ericsson AB) - Initial API and implementation (Bug 235747)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Interface for different implementations of "Register Group" operations
 */
public interface IRegisterGroupActions {
	/**
	 * Process the action to Add / Create a new user Register Group based on the selected registers provided
	 * 
	 * @param part
	 * @param selection
	 * @throws DebugException
	 */
	public void addRegisterGroup(IWorkbenchPart part, IStructuredSelection selection) throws DebugException;

	/**
	 * Returns whether an Add Register Group operation can be performed with the given elements selected in the
	 * Registers view.
	 * 
	 * @param part
	 * @param selection
	 * @return
	 */
	public boolean canAddRegisterGroup(IWorkbenchPart part, IStructuredSelection selection);

	/**
	 * Process the Edit Register Group action
	 * 
	 * @param part
	 * @param selection
	 * @throws DebugException
	 */
	public void editRegisterGroup(IWorkbenchPart part, IStructuredSelection selection) throws DebugException;

	/**
	 * Determine if the selection can be processed to edit a Register Group
	 * 
	 * @param part
	 * @param selection
	 * @return
	 */
	public boolean canEditRegisterGroup(IWorkbenchPart part, IStructuredSelection selection);

	/**
	 * Process the Removal of the register group(s) derived from the selection
	 * 
	 * @param part
	 * @param selection
	 * @throws DebugException
	 */
	public void removeRegisterGroup(IWorkbenchPart part, IStructuredSelection selection) throws DebugException;

	/**
	 * Determine if the selection can be processed to remove a Register Group
	 * 
	 * @param part
	 * @param selection
	 * @return
	 */
	public boolean canRemoveRegisterGroup(IWorkbenchPart part, IStructuredSelection selection);

	/**
	 * Process the Restore to the default Register Groups e.g. removing all user defined register groups
	 * 
	 * @param part
	 * @param selection
	 * @throws DebugException
	 */
	public void restoreDefaultGroups(IWorkbenchPart part, IStructuredSelection selection) throws DebugException;

	/**
	 * Determine if the selection can be processed to restore the default Register groups
	 * 
	 * @param part
	 * @param selection
	 * @return
	 */
	public boolean canRestoreDefaultGroups(IWorkbenchPart part, IStructuredSelection selection);

}
