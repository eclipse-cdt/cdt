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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

	private ICastToType[] fCastableItems = new ICastToType[0];

	private IStatus fStatus = null;

	protected ICastToType[] getCastToType() {
		return fCastableItems;
	}

	protected void setCastToType( ICastToType[] castableItems ) {
		fCastableItems = castableItems;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if ( getCastToType() == null || getCastToType().length == 0 )
			return null;
		
		BusyIndicator.showWhile( Display.getCurrent(), new Runnable() {

			@Override
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
		ICastToType[] castableItems = getCastToType(evaluationContext);
		setBaseEnabled(castableItems.length > 0);
		setCastToType(castableItems);
	}
	
	private ICastToType[] getCastToType(Object evaluationContext) {
		List<ICastToType> castableItems = new ArrayList<ICastToType>();
	    if (evaluationContext instanceof IEvaluationContext) {
	        Object s = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_MENU_SELECTION_NAME);
	        if (s instanceof IStructuredSelection) {
	        	Iterator<?> iter = ((IStructuredSelection)s).iterator();
	        	while( iter.hasNext() ) {
	        		Object element = DebugPlugin.getAdapter(iter.next(), ICastToType.class);
	        		if (element instanceof ICastToType) {
	        			if (((ICastToType)element).isCasted()) {
	        				castableItems.add((ICastToType)element);
	        			}
                    }
                }
            }
        }
	    return castableItems.toArray(new ICastToType[castableItems.size()]);
	}
	
	public IStatus getStatus() {
		return fStatus;
	}

	public void setStatus( IStatus status ) {
		fStatus = status;
	}

	protected void doAction( ICastToType[] castableItems ) throws DebugException {
		for ( ICastToType castableItem : castableItems ) {
			castableItem.restoreOriginal();
		}
	}
}
