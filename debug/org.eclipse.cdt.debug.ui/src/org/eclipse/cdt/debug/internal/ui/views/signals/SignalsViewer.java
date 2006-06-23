/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.signals;

import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


/**
 * Signals viewer.
 *
 * @since: Mar 8, 2004
 */
public class SignalsViewer extends TableViewer {

	// String constants
	protected static final String YES_VALUE = SignalsMessages.getString( "SignalsViewer.8" ); //$NON-NLS-1$
	protected static final String NO_VALUE = SignalsMessages.getString( "SignalsViewer.9" ); //$NON-NLS-1$

	// Column properties
	private static final String CP_NAME = "name"; //$NON-NLS-1$
	private static final String CP_PASS = "pass"; //$NON-NLS-1$
	private static final String CP_SUSPEND = "suspend"; //$NON-NLS-1$
	private static final String CP_DESCRIPTION = "description"; //$NON-NLS-1$

	// Column labels
	private static final String CL_NAME = SignalsMessages.getString( "SignalsViewer.4" ); //$NON-NLS-1$
	private static final String CL_PASS = SignalsMessages.getString( "SignalsViewer.5" ); //$NON-NLS-1$
	private static final String CL_SUSPEND = SignalsMessages.getString( "SignalsViewer.6" ); //$NON-NLS-1$
	private static final String CL_DESCRIPTION = SignalsMessages.getString( "SignalsViewer.7" ); //$NON-NLS-1$

	/**
	 * Constructor for SignalsViewer
	 * 
	 * @param parent
	 * @param style
	 */
	public SignalsViewer( Composite parent, int style ) {
		super( parent, style );
		Table table = getTable();
		table.setLinesVisible( true );
		table.setHeaderVisible( true );		
		table.setLayoutData( new GridData( GridData.FILL_BOTH ) );

		// Create the table columns
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		TableColumn[] columns = table.getColumns();
		columns[0].setResizable( true );
		columns[1].setResizable( false );
		columns[2].setResizable( false );
		columns[3].setResizable( true );

		columns[0].setText( CL_NAME );
		columns[1].setText( CL_PASS );
		columns[2].setText( CL_SUSPEND );
		columns[3].setText( CL_DESCRIPTION );

		PixelConverter pc = new PixelConverter( parent );
		columns[0].setWidth( pc.convertWidthInCharsToPixels( 20 ) );
		columns[1].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		columns[2].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		columns[3].setWidth( pc.convertWidthInCharsToPixels( 50 ) );

		setColumnProperties( new String[]{ CP_NAME, CP_PASS, CP_SUSPEND, CP_DESCRIPTION } );
	}
}
