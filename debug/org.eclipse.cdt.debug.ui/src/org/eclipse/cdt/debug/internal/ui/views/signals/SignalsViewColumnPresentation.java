/*******************************************************************************
 * Copyright (c) 2010, 2011 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     CodeSourcery - Initial API and implementation
 *     Wind River Systems - flexible hierarchy Signals view (bug 338908)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.signals;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.internal.ui.elements.adapters.VariableColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Signals view column presentation.
 */
public class SignalsViewColumnPresentation implements IColumnPresentation {

    private static final String PREFIX = CDebugUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$
    
    public static final String ID = PREFIX + "signalsViewColumnPresentationId"; //$NON-NLS-1$

    private static final String COLUMN_ID_NAME = VariableColumnPresentation.COLUMN_VARIABLE_NAME;
    private static final String COLUMN_ID_PASS = PREFIX + "signalsColumn.pass"; //$NON-NLS-1$
    private static final String COLUMN_ID_STOP = PREFIX + "signalsColumn.stop"; //$NON-NLS-1$
    private static final String COLUMN_ID_DESC = PREFIX + "signalsColumn.desc"; //$NON-NLS-1$

	private static final String CL_NAME = SignalsMessages.getString( "SignalsViewer.4" ); //$NON-NLS-1$
	private static final String CL_PASS = SignalsMessages.getString( "SignalsViewer.5" ); //$NON-NLS-1$
	private static final String CL_SUSPEND = SignalsMessages.getString( "SignalsViewer.6" ); //$NON-NLS-1$
	private static final String CL_DESCRIPTION = SignalsMessages.getString( "SignalsViewer.7" ); //$NON-NLS-1$

    private static final String[] ALL_COLUMNS = new String[] { 
        COLUMN_ID_NAME, 
        COLUMN_ID_PASS, 
        COLUMN_ID_STOP,
        COLUMN_ID_DESC
    };

    private static final String[] INITIAL_COLUMNS = new String[] { 
        COLUMN_ID_NAME, 
        COLUMN_ID_PASS, 
        COLUMN_ID_STOP,
        COLUMN_ID_DESC
    };

    @Override
	public void init( IPresentationContext context ) {
    } 

    @Override
	public void dispose() {
    }

    @Override
	public String[] getAvailableColumns() {
        return ALL_COLUMNS;
    }

    @Override
	public String[] getInitialColumns() {
        return INITIAL_COLUMNS;
    }

    @Override
	public String getHeader( String id ) {
        if ( COLUMN_ID_NAME.equals( id ) ) {
            return CL_NAME;
        }
        if ( COLUMN_ID_PASS.equals( id ) ) {
            return CL_PASS;
        }
        if ( COLUMN_ID_STOP.equals( id ) ) {
            return CL_SUSPEND;
        }
        if ( COLUMN_ID_DESC.equals( id ) ) {
            return CL_DESCRIPTION;
        }
        return null;
    }

    @Override
	public ImageDescriptor getImageDescriptor( String id ) {
        return null;
    }

    @Override
	public String getId() {
        return ID;
    }

    @Override
	public boolean isOptional() {
        return true;
    }
}
