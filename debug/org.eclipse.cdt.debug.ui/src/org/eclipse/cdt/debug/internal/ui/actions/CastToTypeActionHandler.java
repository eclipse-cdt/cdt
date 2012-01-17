/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Nokia - adapt to new command framework 
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.model.ICastToType;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The delegate of the "Cast To Type" action.
 */
public class CastToTypeActionHandler extends AbstractHandler {

	static protected class CastToTypeInputValidator implements IInputValidator {

		public CastToTypeInputValidator() {
		}

		@Override
		public String isValid( String newText ) {
			if ( newText.trim().length() == 0 ) {
				return ActionMessages.getString( "CastToTypeActionDelegate.0" ); //$NON-NLS-1$
			}
			return null;
		}
	}

	protected class CastToTypeDialog extends InputDialog {

		public CastToTypeDialog( Shell parentShell, String initialValue ) {
			super( parentShell, ActionMessages.getString( "CastToTypeActionDelegate.1" ), //$NON-NLS-1$
					ActionMessages.getString( "CastToTypeActionDelegate.2" ), //$NON-NLS-1$
					initialValue, 
					new CastToTypeInputValidator() );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		@Override
		protected void configureShell( Shell shell ) {
			super.configureShell( shell );
			shell.setImage( CDebugImages.get( CDebugImages.IMG_LCL_CAST_TO_TYPE ) );
		}
	}

	private ICastToType[] fCastableItems = new ICastToType[0];

	private IStatus fStatus = null;

	private IWorkbenchPart fTargetPart;

	public CastToTypeActionHandler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    fTargetPart = HandlerUtil.getActivePartChecked(event);
	    
		if ( getCastToType() == null || getCastToType().length == 0  )
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
				CDebugUIPlugin.errorDialog( ActionMessages.getString( "CastToTypeActionDelegate.3" ), getStatus() ); //$NON-NLS-1$
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
	        	while(iter.hasNext()) {
	        		Object element = DebugPlugin.getAdapter(iter.next(), ICastToType.class);
	        		if (element instanceof ICastToType) {
	        			if (((ICastToType)element).canCast()) {
		        		    castableItems.add((ICastToType)element);
	        			}
                    }
                }
            }
	    }
	    return castableItems.toArray(new ICastToType[castableItems.size()]);
	}

	protected ICastToType[] getCastToType() {
		return fCastableItems;
	}

	protected void setCastToType( ICastToType[] castableItems ) {
		fCastableItems = castableItems;
	}

	public IStatus getStatus() {
		return fStatus;
	}

	public void setStatus( IStatus status ) {
		fStatus = status;
	}

	protected void doAction( ICastToType[] castableItems ) throws DebugException {
		String currentType = castableItems[0].getCurrentType().trim();
		CastToTypeDialog dialog = new CastToTypeDialog( CDebugUIPlugin.getActiveWorkbenchShell(), currentType );
		if ( dialog.open() == Window.OK ) {
			String newType = dialog.getValue().trim();
			for ( ICastToType castableItem : castableItems ) {
				castableItem.cast( newType );
			}
			if ( getSelectionProvider() != null )
				getSelectionProvider().setSelection( new StructuredSelection( castableItems ) );
		}
	}

	private ISelectionProvider getSelectionProvider() {
		return (fTargetPart instanceof IDebugView) ? ((IDebugView)fTargetPart).getViewer() : null;
	}
}
