/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.navigator;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;

/**
 * A Common Navigator adapted <code>WorkingSetFilterActionGroup</code> adding persistency.
 * 
 * @see org.eclipse.ui.actions.WorkingSetFilterActionGroup
 */
public class CNavigatorWorkingSetActionGroup extends WorkingSetFilterActionGroup {

	private static final String TAG_WORKING_SET_NAME= "workingSetName"; //$NON-NLS-1$

	/**
	 * Create a new working set filter action group.
	 * @param shell
	 * @param workingSetUpdater
	 */
	public CNavigatorWorkingSetActionGroup(Shell shell, IPropertyChangeListener workingSetUpdater) {
		super(shell, workingSetUpdater);
	}

	/**
	 * Saves the state of the filter actions in a memento.
	 * 
	 * @param memento the memento
	 */
	public void saveState(IMemento memento) {
		String workingSetName= ""; //$NON-NLS-1$
		IWorkingSet workingSet= getWorkingSet();
		if (workingSet != null) {
			workingSetName= workingSet.getName();
		}
		memento.putString(TAG_WORKING_SET_NAME, workingSetName);
	}

	/**
	 * Restores the state of the filter actions from a memento.
	 * <p>
	 * Note: This method does not refresh the viewer.
	 * </p>
	 * @param memento
	 */	
	public void restoreState(IMemento memento) {
		String workingSetName= memento.getString(TAG_WORKING_SET_NAME);
		IWorkingSet workingSet= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(workingSetName);
		setWorkingSet(workingSet);
	}
	
}
