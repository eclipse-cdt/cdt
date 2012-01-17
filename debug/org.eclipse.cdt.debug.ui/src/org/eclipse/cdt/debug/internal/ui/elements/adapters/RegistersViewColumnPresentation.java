/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * CodeSourcery - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.internal.ui.elements.adapters.VariableColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Registers View columns
 */
public class RegistersViewColumnPresentation implements IColumnPresentation {

    private static final String PREFIX = CDebugUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$
    
    public static final String ID = PREFIX + "registersViewColumnPresentationId"; //$NON-NLS-1$

    private static final String COLUMN_ID_NAME = VariableColumnPresentation.COLUMN_VARIABLE_NAME;
    private static final String COLUMN_ID_TYPE = VariableColumnPresentation.COLUMN_VARIABLE_TYPE;
    private static final String COLUMN_ID_VALUE = VariableColumnPresentation.COLUMN_VARIABLE_VALUE;

    private static final String[] ALL_COLUMNS = new String[] { 
        COLUMN_ID_NAME, 
        COLUMN_ID_TYPE, 
        COLUMN_ID_VALUE
    };

    private static final String[] INITIAL_COLUMNS = new String[] { 
        COLUMN_ID_NAME, 
        COLUMN_ID_VALUE 
    };

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#init(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
     */
    @Override
	public void init( IPresentationContext context ) {
    } 

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#dispose()
     */
    @Override
	public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#getAvailableColumns()
     */
    @Override
	public String[] getAvailableColumns() {
        return ALL_COLUMNS;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#getInitialColumns()
     */
    @Override
	public String[] getInitialColumns() {
        return INITIAL_COLUMNS;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#getHeader(java.lang.String)
     */
    @Override
	public String getHeader( String id ) {
        if ( COLUMN_ID_TYPE.equals( id ) ) {
            return ElementAdapterMessages.RegistersViewColumnPresentation_0;
        }
        if ( COLUMN_ID_NAME.equals( id ) ) {
            return ElementAdapterMessages.RegistersViewColumnPresentation_1;
        }
        if ( COLUMN_ID_VALUE.equals( id ) ) {
            return ElementAdapterMessages.RegistersViewColumnPresentation_2;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#getImageDescriptor(java.lang.String)
     */
    @Override
	public ImageDescriptor getImageDescriptor( String id ) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#getId()
     */
    @Override
	public String getId() {
        return ID;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation#isOptional()
     */
    @Override
	public boolean isOptional() {
        return true;
    }
}
