/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions; 

import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;
 
/**
 * The "Remove Register Group" action.
 */
public class RemoveRegisterGroupActionDelegate extends ActionDelegate implements IObjectActionDelegate {

	private IRegisterGroup[] fRegisterGroups;

	/** 
	 * Constructor for RemoveRegisterGroupActionDelegate. 
	 */
	public RemoveRegisterGroupActionDelegate() {
		super();
		setRegisterGroups( new IRegisterGroup[0] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		ArrayList list = new ArrayList();
		if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			Iterator it = ss.iterator();
			while( it.hasNext() ) {
				Object o = it.next();
				if ( o instanceof IRegisterGroup ) {
					list.add( o );
				}
			}
		}
		setRegisterGroups( (IRegisterGroup[])list.toArray( new IRegisterGroup[list.size()] ) );
	}

	protected IRegisterGroup[] getRegisterGroups() {
		return fRegisterGroups;
	}

	protected void setRegisterGroups( IRegisterGroup[] registerGroups ) {
		fRegisterGroups = registerGroups;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		IRegisterGroup[] groups = getRegisterGroups();
		if ( groups.length > 0 ) {
			IDebugTarget target = groups[0].getDebugTarget();
			if ( target instanceof ICDebugTarget ) {
				((ICDebugTarget)target).removeRegisterGroups( groups );
			}
		}
	}
}
