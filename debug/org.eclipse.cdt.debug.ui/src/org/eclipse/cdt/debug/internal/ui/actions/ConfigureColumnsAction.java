/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Configures visible columns in an asynch tree viewer/
 * 
 * @since 5.0
 */
public class ConfigureColumnsAction extends Action implements IUpdate {

    private TreeModelViewer fViewer;

    class ColumnContentProvider implements IStructuredContentProvider {

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        @Override
		public Object[] getElements( Object inputElement ) {
            return ((IColumnPresentation)inputElement).getAvailableColumns();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        @Override
		public void dispose() {
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        @Override
		public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        }
    }

    class ColumnLabelProvider extends LabelProvider {

        private Map fImages = new HashMap();

        @Override
		public Image getImage( Object element ) {
            ImageDescriptor imageDescriptor = fViewer.getColumnPresentation().getImageDescriptor( (String)element );
            if ( imageDescriptor != null ) {
                Image image = (Image)fImages.get( imageDescriptor );
                if ( image == null ) {
                    image = imageDescriptor.createImage();
                    fImages.put( imageDescriptor, image );
                }
                return image;
            }
            return null;
        }

        @Override
		public String getText( Object element ) {
            return fViewer.getColumnPresentation().getHeader( (String)element );
        }

        @Override
		public void dispose() {
            super.dispose();
            Iterator iterator = fImages.values().iterator();
            while( iterator.hasNext() ) {
                Image image = (Image)iterator.next();
                image.dispose();
            }
            fImages.clear();
        }
    }

    public ConfigureColumnsAction( TreeModelViewer viewer ) {
        setText( "Select &Columns..." );
        setId( CDebugUIPlugin.getUniqueIdentifier() + ".ConfigureColumnsAction" ); //$NON-NLS-1$
        PlatformUI.getWorkbench().getHelpSystem().setHelp( this, ICDebugHelpContextIds.CONFIGURE_COLUMNS_ACTION );
        fViewer = viewer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IUpdate#update()
     */
    @Override
	public void update() {
        setEnabled( fViewer.isShowColumns() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
	public void run() {
        ListSelectionDialog dialog = new ListSelectionDialog( fViewer.getControl().getShell(), fViewer.getColumnPresentation(), new ColumnContentProvider(),
                new ColumnLabelProvider(), "Select the &columns to display:" );
        PlatformUI.getWorkbench().getHelpSystem().setHelp( this, ICDebugHelpContextIds.CONFIGURE_COLUMNS_DIALOG );
        String[] visibleColumns = fViewer.getVisibleColumns();
        List initialSelection = new ArrayList( visibleColumns.length );
        for( int i = 0; i < visibleColumns.length; i++ ) {
            initialSelection.add( visibleColumns[i] );
        }
        dialog.setTitle( "Select Columns" );
        dialog.setInitialElementSelections( initialSelection );
        if ( dialog.open() == Window.OK ) {
            Object[] result = dialog.getResult();
            if ( result.length == 0 ) {
                fViewer.setShowColumns( false );
            }
            else {
                String[] ids = new String[result.length];
                System.arraycopy( result, 0, ids, 0, result.length );
                fViewer.resetColumnSizes( ids );
                fViewer.setVisibleColumns( ids );
            }
        }
    }
}
