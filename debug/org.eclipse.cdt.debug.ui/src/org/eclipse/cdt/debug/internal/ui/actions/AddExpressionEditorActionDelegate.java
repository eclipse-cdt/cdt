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

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionDelegate;
 

/**
 * The "Add Expression" action contribution to editors.
 */
public class AddExpressionEditorActionDelegate extends ActionDelegate implements IEditorActionDelegate {

	private IEditorPart fEditorPart;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor( IAction action, IEditorPart targetEditor ) {
		setEditorPart( targetEditor );
	}

	private IEditorPart getEditorPart() {
		return fEditorPart;
	}

	private void setEditorPart( IEditorPart editorPart ) {
		fEditorPart = editorPart;
	}

	public void run( IAction action ) {
		String text = getSelectedText();
		ExpressionDialog dlg = new ExpressionDialog( getShell(), text );
		if ( dlg.open() != Window.OK )
			return;
		createExpression( dlg.getExpression() );
		activateExpressionView();
	}

	private String getSelectedText() {
		ISelection selection = getTargetSelection();
		if ( selection != null && selection instanceof ITextSelection ) {
			return ((ITextSelection)selection).getText().trim();
		}
		return ""; //$NON-NLS-1$
	}

	protected ISelection getTargetSelection() {
		IWorkbenchPart part = getEditorPart();
		if ( part != null ) {
			ISelectionProvider provider = part.getSite().getSelectionProvider();
			if ( provider != null ) {
				return provider.getSelection();
			}
		}
		return null;
	}

	private void createExpression( String text ) {
		IWatchExpression watchExpression= DebugPlugin.getDefault().getExpressionManager().newWatchExpression( text );
		DebugPlugin.getDefault().getExpressionManager().addExpression( watchExpression );
		IAdaptable context = DebugUITools.getDebugContext();
		if ( context instanceof IDebugElement )
			watchExpression.setExpressionContext( (IDebugElement)context );
	}

	protected Shell getShell() {
		return ( getEditorPart() != null ) ? getEditorPart().getSite().getShell() : CDebugUIPlugin.getActiveWorkbenchShell();
	}

	private void activateExpressionView() {
		IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
		if ( window != null ) {
			IWorkbenchPage page = window.getActivePage();
			if ( page != null ) {
				try {
					page.showView( IDebugUIConstants.ID_EXPRESSION_VIEW );
				}
				catch( PartInitException e ) {
				}
			}
		}
	}
}
