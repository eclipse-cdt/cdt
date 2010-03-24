/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Nokia - port to command framework
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.ICastToType;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The delegate of the "Restore Default Type" action.
 */
public class RestoreDefaultTypeActionHandler extends AbstractHandler {

	private ICastToType fCastToType = null;

	private IStatus fStatus = null;

	protected ICastToType getCastToType() {
		return fCastToType;
	}

	protected void setCastToType( ICastToType castToType ) {
		fCastToType = castToType;
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if ( getCastToType() == null )
			return null;
		
		BusyIndicator.showWhile( Display.getCurrent(), new Runnable() {

			public void run() {
				try {
					doAction( getCastToType() );
					setStatus( null );
				}
				catch( DebugException e ) {
					setStatus( e.getStatus() );
				}
			}
		} );
		if ( getStatus() != null && !getStatus().isOK() ) {
			IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
			if ( window != null ) {
				CDebugUIPlugin.errorDialog( ActionMessages.getString( "RestoreDefaultTypeActionDelegate.0" ), getStatus() ); //$NON-NLS-1$
			}
			else {
				CDebugUIPlugin.log( getStatus() );
			}
		}
		
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		ICastToType castToType = getCastToType(evaluationContext);
		setBaseEnabled( castToType != null && castToType.isCasted() );
		setCastToType(castToType);
	}
	
	private ICastToType getCastToType(Object evaluationContext) {
	    if (evaluationContext instanceof IEvaluationContext) {
	        Object s = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_MENU_SELECTION_NAME);
	        if (s instanceof IStructuredSelection) {
	            IStructuredSelection ss = (IStructuredSelection)s;
	            if (!ss.isEmpty()) {
	   	            return (ICastToType)DebugPlugin.getAdapter(ss.getFirstElement(), ICastToType.class);
	            }
	        }
	    }
	    return null;
	}
	
	public IStatus getStatus() {
		return fStatus;
	}

	public void setStatus( IStatus status ) {
		fStatus = status;
	}

	protected void doAction( ICastToType castToType ) throws DebugException {
		castToType.restoreOriginal();
	}
}
