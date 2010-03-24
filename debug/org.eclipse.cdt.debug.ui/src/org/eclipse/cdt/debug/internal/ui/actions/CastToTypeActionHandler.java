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
		protected void configureShell( Shell shell ) {
			super.configureShell( shell );
			shell.setImage( CDebugImages.get( CDebugImages.IMG_LCL_CAST_TO_TYPE ) );
		}
	}

	private ICastToType fCastToType = null;

	private IStatus fStatus = null;

	private IWorkbenchPart fTargetPart;

	public CastToTypeActionHandler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
	    fTargetPart = HandlerUtil.getActivePartChecked(event);
	    
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
		ICastToType castToType = getCastToType(evaluationContext);
		setBaseEnabled( castToType != null );
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

	protected ICastToType getCastToType() {
		return fCastToType;
	}

	protected void setCastToType( ICastToType castToType ) {
		fCastToType = castToType;
	}

	public IStatus getStatus() {
		return fStatus;
	}

	public void setStatus( IStatus status ) {
		fStatus = status;
	}

	protected void doAction( ICastToType castToType ) throws DebugException {
		String currentType = castToType.getCurrentType().trim();
		CastToTypeDialog dialog = new CastToTypeDialog( CDebugUIPlugin.getActiveWorkbenchShell(), currentType );
		if ( dialog.open() == Window.OK ) {
			String newType = dialog.getValue().trim();
			castToType.cast( newType );
			if ( getSelectionProvider() != null )
				getSelectionProvider().setSelection( new StructuredSelection( castToType ) );
		}
	}

	private ISelectionProvider getSelectionProvider() {
		return (fTargetPart instanceof IDebugView) ? ((IDebugView)fTargetPart).getViewer() : null;
	}
}
