/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.memory;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Enter type comment.
 * 
 * @since Jun 12, 2003
 */
public class MemoryViewAction extends Action implements IUpdate
{
	/** The text operation code */
	private int fOperationCode = -1;
	/** The text operation target */
	private ITextOperationTarget fOperationTarget;
	/** The text operation target provider */
	private IAdaptable fTargetProvider;

	public MemoryViewAction( ITextOperationTarget target, int operationCode )
	{
		super();
		fOperationCode = operationCode;
		fOperationTarget = target;
		update();
	}

	public MemoryViewAction( IAdaptable targetProvider, int operationCode )
	{
		super();
		fTargetProvider = targetProvider;
		fOperationCode = operationCode;
		update();
	}

	/**
	 * The <code>TextOperationAction</code> implementation of this 
	 * <code>IUpdate</code> method discovers the operation through the current
	 * editor's <code>ITextOperationTarget</code> adapter, and sets the
	 * enabled state accordingly.
	 */
	public void update()
	{
		if ( fTargetProvider != null && fOperationCode != -1 )
		{
			ITextOperationTarget target = getTextOperationTarget();
			boolean isEnabled = ( target != null && target.canDoOperation( fOperationCode ) );
			setEnabled( isEnabled );
		}
	}

	/**
	 * The <code>TextOperationAction</code> implementation of this 
	 * <code>IAction</code> method runs the operation with the current
	 * operation code.
	 */
	public void run()
	{
		ITextOperationTarget target = getTextOperationTarget();
		if ( fOperationCode != -1 && target != null )
			target.doOperation( fOperationCode );
	}

	private ITextOperationTarget getTextOperationTarget()
	{
		if ( fOperationTarget == null )
		{
			if ( fTargetProvider != null )
				return (ITextOperationTarget)fTargetProvider.getAdapter( ITextOperationTarget.class ); 
		}
		return fOperationTarget;
	}
}
