/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.ui.sourcelookup;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.cdt.debug.internal.core.sourcelookup.CDirectorySourceLocation;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IListAdapter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.ListDialogField;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


class SourceListDialogField extends ListDialogField
{
	public class ObservableSourceList extends Observable
	{
		protected synchronized void setChanged()
		{
			super.setChanged();
		}
	}

	// String constants
	protected static final String YES_VALUE = "yes";
	protected static final String NO_VALUE = "no";

	// Column properties
	private static final String CP_LOCATION = "location";
	private static final String CP_ASSOCIATION = "association";
	private static final String CP_SEARCH_SUBFOLDERS = "searchSubfolders";
	
	private ObservableSourceList fObservable = new ObservableSourceList();

	public SourceListDialogField( String title, IListAdapter listAdapter )
	{
		super( listAdapter, 
			   new String[] 
					{
						/* 0 */ "Add...",
						/* 1 */ null,
						/* 2 */ "Up",
						/* 3 */ "Down",
						/* 4 */ null,
						/* 5 */ "Remove",
					},
			   new SourceLookupLabelProvider() );
		setUpButtonIndex( 2 );
		setDownButtonIndex( 3 );
		setRemoveButtonIndex( 5 );
		setLabelText( title );
	}

	protected boolean managedButtonPressed( int index )
	{
		super.managedButtonPressed( index );
		return false;
	}

	protected TableViewer createTableViewer( Composite parent )
	{
		TableViewer viewer = super.createTableViewer( parent );
		Table table = viewer.getTable();

		TableLayout tableLayout = new TableLayout();
		table.setLayout( tableLayout );

		GridData gd = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL );
		gd.grabExcessVerticalSpace = true;
		gd.grabExcessHorizontalSpace = true;
		table.setLayoutData( gd );
		
		table.setLinesVisible( true );
		table.setHeaderVisible( true );		

		new TableColumn( table, SWT.NULL );
		tableLayout.addColumnData( new ColumnWeightData( 2, true ) );
		new TableColumn( table, SWT.NULL );
		tableLayout.addColumnData( new ColumnWeightData( 2, true ) );
		new TableColumn( table, SWT.NULL );
		tableLayout.addColumnData( new ColumnWeightData( 1, true ) );

		TableColumn[] columns = table.getColumns();
		columns[0].setText( "Location" );
		columns[1].setText( "Association" );
		columns[2].setText( "Search subfolders" );
		
		CellEditor textCellEditor = new TextCellEditor( table );
		CellEditor comboCellEditor = new ComboBoxCellEditor( table, new String[]{ YES_VALUE, NO_VALUE } );
		viewer.setCellEditors( new CellEditor[]{ null, textCellEditor, comboCellEditor } );
		viewer.setColumnProperties( new String[]{ CP_LOCATION, CP_ASSOCIATION, CP_SEARCH_SUBFOLDERS } );
		viewer.setCellModifier( createCellModifier() );

		return viewer;
	}

	private ICellModifier createCellModifier()
	{
		return new ICellModifier()
					{
						public boolean canModify( Object element, String property )
						{
							return ( element instanceof CDirectorySourceLocation && ( property.equals( CP_ASSOCIATION ) || property.equals( CP_SEARCH_SUBFOLDERS ) ) );
						}

						public Object getValue( Object element, String property )
						{
							if ( element instanceof CDirectorySourceLocation && property.equals( CP_ASSOCIATION ) )
							{
								return ( ((CDirectorySourceLocation)element).getAssociation() != null ) ? 
												((CDirectorySourceLocation)element).getAssociation().toOSString() : "";
							}
							if ( element instanceof CDirectorySourceLocation && property.equals( CP_SEARCH_SUBFOLDERS ) )
							{
								return ( ((CDirectorySourceLocation)element).searchSubfolders() ) ? new Integer( 0 ) : new Integer( 1 );
							}
							return null;
						}

						public void modify( Object element, String property, Object value )
						{
							Object entry = getSelection();
							if ( entry instanceof CDirectorySourceLocation )
							{
								if ( property.equals( CP_ASSOCIATION ) && value instanceof String )
								{
									IPath association = new Path( (String)value );
									if ( association.isValidPath( (String)value ) )
									{
										((CDirectorySourceLocation)entry).setAssociation( association );
										setChanged();
									}
								}
								if ( property.equals( CP_SEARCH_SUBFOLDERS ) && value instanceof Integer )
								{
									((CDirectorySourceLocation)entry).setSearchSubfolders( ((Integer)value).intValue() == 0 );
									setChanged();
								}
								if ( hasChanged() )
								{
									refresh();
									notifyObservers();
								}
							}
						}
					};
	}

	protected Object getSelection()
	{
		List list = getSelectedElements();
		return ( list.size() > 0 ) ? list.get( 0 ) : null;
	}

	public synchronized void addObserver( Observer o )
	{
		fObservable.addObserver( o );
	}

	public synchronized void deleteObserver( Observer o )
	{
		fObservable.deleteObserver( o );
	}

	public synchronized boolean hasChanged()
	{
		return fObservable.hasChanged();
	}

	public void notifyObservers()
	{
		fObservable.notifyObservers();
	}

	public void notifyObservers( Object arg )
	{
		fObservable.notifyObservers( arg );
	}

	public void dispose()
	{
	}

	protected void setChanged()
	{
		fObservable.setChanged();
	}
}