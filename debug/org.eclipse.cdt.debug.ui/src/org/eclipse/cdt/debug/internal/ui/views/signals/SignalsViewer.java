/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.signals;

import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Enter type comment.
 * 
 * @since: Jan 30, 2003
 */
public class SignalsViewer extends TableViewer
{
	// String constants
	protected static final String YES_VALUE = "yes";
	protected static final String NO_VALUE = "no";

	// Column properties
	private static final String CP_NAME = "name";
	private static final String CP_PASS = "pass";
	private static final String CP_SUSPEND = "suspend";
	private static final String CP_DESCRIPTION = "description";

	private IDebugExceptionHandler fExceptionHandler = null;

	/**
	 * Constructor for SignalsViewer.
	 * @param parent
	 * @param style
	 */
	public SignalsViewer( Composite parent, int style )
	{
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

		columns[0].setText( "Name" );
		columns[1].setText( "Pass" );
		columns[2].setText( "Suspend" );
		columns[3].setText( "Description" );

		PixelConverter pc = new PixelConverter( parent );
		columns[0].setWidth( pc.convertWidthInCharsToPixels( 20 ) );
		columns[1].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		columns[2].setWidth( pc.convertWidthInCharsToPixels( 15 ) );
		columns[3].setWidth( pc.convertWidthInCharsToPixels( 50 ) );

		CellEditor cellEditor = new ComboBoxCellEditor( table, new String[]{ YES_VALUE, NO_VALUE } );
		setCellEditors( new CellEditor[]{ null, cellEditor, cellEditor, null } );
		setColumnProperties( new String[]{ CP_NAME, CP_PASS, CP_SUSPEND, CP_DESCRIPTION } );
		setCellModifier( createCellModifier() );
	}
	
	private ICellModifier createCellModifier()
	{
		return new ICellModifier()
					{
						public boolean canModify( Object element, String property )
						{
							if ( element instanceof ICSignal )
							{
								return ((ICSignal)element).getDebugTarget().isSuspended();
							}
							return false;
						}

						public Object getValue( Object element, String property )
						{
							if ( element instanceof ICSignal )
							{
								if ( CP_PASS.equals( property ) )
								{
									return ( ((ICSignal)element).isPassEnabled() ) ? new Integer( 0 ) : new Integer( 1 );
								}
								else if ( CP_SUSPEND.equals( property ) )
								{
									return ( ((ICSignal)element).isStopEnabled() ) ? new Integer( 0 ) : new Integer( 1 );
								}
							}
							return null;
						}

						public void modify( Object element, String property, Object value )
						{
							IStructuredSelection sel = (IStructuredSelection)getSelection();
							Object entry = sel.getFirstElement();
							if ( entry instanceof ICSignal && value instanceof Integer )
							{
								try
								{
									boolean enable = ( ((Integer)value).intValue() == 0 );
									if ( CP_PASS.equals( property ) )
									{
										((ICSignal)entry).setPassEnabled( enable );
									}
									else if ( CP_SUSPEND.equals( property ) )
									{
										((ICSignal)entry).setStopEnabled( enable );
									}
									refresh( entry );
								}
								catch( DebugException e )
								{
									Display.getCurrent().beep();
								}
							}
						}
					};
	}

	protected IDebugExceptionHandler getExceptionHandler()
	{
		return fExceptionHandler;
	}

	protected void setExceptionHandler( IDebugExceptionHandler handler )
	{
		fExceptionHandler = handler;
	}
}
