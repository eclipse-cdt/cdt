/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Action to toggle the use of contributed variables content providers on and off.
 * When on, all registered variables content providers for the current debug model
 * are used.  When off, the default content provider (that shows all children)
 * is used for all debug models.
 */
public class ToggleShowColumnsAction extends Action implements IUpdate {

    private TreeModelViewer fViewer;

    public ToggleShowColumnsAction( TreeModelViewer viewew ) {
        super( "&Show Columns", IAction.AS_CHECK_BOX );
        fViewer = viewew;
        setToolTipText( "Show Columns" );
        setImageDescriptor( CDebugImages.DESC_OBJS_COMMON_TAB );
        setId( CDebugUIPlugin.getUniqueIdentifier() + ".ToggleShowColumsAction" ); //$NON-NLS-1$
        PlatformUI.getWorkbench().getHelpSystem().setHelp( this, ICDebugHelpContextIds.SHOW_COLUMNS_ACTION );
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
	public void run() {
        if ( fViewer.getControl().isDisposed() ) {
            return;
        }
        BusyIndicator.showWhile( fViewer.getControl().getDisplay(), new Runnable() {
            @Override
			public void run() {
                fViewer.setShowColumns( isChecked() );
            }
        } );
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IUpdate#update()
     */
    @Override
	public void update() {
        setEnabled( fViewer.canToggleColumns() );
        setChecked( fViewer.isShowColumns() );
    }
}
