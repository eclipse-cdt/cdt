/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.ui.sourcelookup;

import java.util.List;

import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.IDirectorySourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CDirectorySourceLocation;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLocator;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IListAdapter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.LayoutUtil;
import org.eclipse.cdt.debug.internal.ui.dialogfields.ListDialogField;
import org.eclipse.cdt.debug.internal.ui.wizards.AddSourceLocationWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 
 * Enter type comment.
 * 
 * @since Dec 18, 2002
 */
public class SourceLookupBlock
{
	private class SourceListDialogField extends ListDialogField
	{
		public SourceListDialogField( IListAdapter adapter, String[] buttonLabels, ILabelProvider lprovider )
		{
			super( adapter, buttonLabels, lprovider );
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
			tableLayout.addColumnData( new ColumnWeightData( 1, true ) );
			new TableColumn( table, SWT.NULL );
			tableLayout.addColumnData( new ColumnWeightData( 1, true ) );

			TableColumn[] columns = table.getColumns();
			columns[0].setText( "Location" );
			columns[1].setText( "Association" );
			
			return viewer;
		}
	}

	private class SourceLookupAdapter implements IListAdapter
	{
		public void customButtonPressed( DialogField field, int index )
		{
			doButtonPressed( index );
		}

		public void selectionChanged( DialogField field )
		{
			doSelectionChanged();
		}
	}
	
	private static class SourceLookupLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		public Image getColumnImage( Object element, int columnIndex )
		{
			if ( columnIndex == 0 )
			{
				if ( element instanceof IProjectSourceLocation )
				{
					return CDebugImages.get( CDebugImages.IMG_OBJS_PROJECT );
				}
				if ( element instanceof IDirectorySourceLocation )
				{
					return CDebugImages.get( CDebugImages.IMG_OBJS_FOLDER );
				}
			}
			return null;
		}

		public String getColumnText( Object element, int columnIndex )
		{
			if ( columnIndex == 0 )
			{
				if ( element instanceof IProjectSourceLocation )
				{
					return ((IProjectSourceLocation)element).getProject().getName();
				}
				if ( element instanceof IDirectorySourceLocation )
				{
					return ((IDirectorySourceLocation)element).getDirectory().toOSString();
				}
			}
			else if ( columnIndex == 1 )
			{
				if ( element instanceof IDirectorySourceLocation && ((IDirectorySourceLocation)element).getAssociation() != null )
				{
					return ((IDirectorySourceLocation)element).getAssociation().toOSString();
				}
			}
			return "";
		}
	}

	// Column properties
	private static final String CP_LOCATION = "location";
	private static final String CP_ASSOCIATION = "association";

	private Composite fControl = null;
	private Shell fShell = null;
	private SourceListDialogField fSourceListField;
	private ILaunchConfigurationDialog fLaunchConfigurationDialog = null;
	private boolean fIsDirty = false;
	private ICSourceLocator fLocator = null;
	private IProject fProject = null;

	/**
	 * Constructor for SourceLookupBlock.
	 */
	public SourceLookupBlock()
	{
		String[] buttonLabels = new String[] 
		{
			/* 0 */ "Add...",
			/* 1 */ null,
			/* 2 */ "Up",
			/* 3 */ "Down",
			/* 4 */ null,
			/* 5 */ "Remove",
			/* 6 */ null,
			/* 7 */ "Restore Defaults",
		};

		SourceLookupAdapter adapter = new SourceLookupAdapter();

		fSourceListField = new SourceListDialogField( adapter, buttonLabels, new SourceLookupLabelProvider() );
		fSourceListField.setLabelText( "Source Locations" );
		fSourceListField.setUpButtonIndex( 2 );
		fSourceListField.setDownButtonIndex( 3 );
		fSourceListField.setRemoveButtonIndex( 5 );
	}

	public void createControl( Composite parent )
	{
		fShell = parent.getShell();
		fControl = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		fControl.setLayout( layout );
		fControl.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		fControl.setFont( JFaceResources.getDialogFont() );

		PixelConverter converter = new PixelConverter( fControl );
		
		fSourceListField.doFillIntoGrid( fControl, 3 );
		LayoutUtil.setHorizontalSpan( fSourceListField.getLabelControl( null ), 2 );
		LayoutUtil.setWidthHint( fSourceListField.getLabelControl( null ), converter.convertWidthInCharsToPixels( 40 ) );
		LayoutUtil.setHorizontalGrabbing( fSourceListField.getListControl( null ) );

		TableViewer viewer = fSourceListField.getTableViewer();
		Table table = viewer.getTable();
		CellEditor cellEditor = new TextCellEditor( table );
		viewer.setCellEditors( new CellEditor[]{ null, cellEditor } );
		viewer.setColumnProperties( new String[]{ CP_LOCATION, CP_ASSOCIATION } );
		viewer.setCellModifier( createCellModifier() );
	}

	private ICellModifier createCellModifier()
	{
		return new ICellModifier()
					{
						public boolean canModify( Object element, String property )
						{
							return ( element instanceof CDirectorySourceLocation && property.equals( CP_ASSOCIATION ) );
						}

						public Object getValue( Object element, String property )
						{
							if ( element instanceof CDirectorySourceLocation && property.equals( CP_ASSOCIATION ) )
							{
								return ( ((CDirectorySourceLocation)element).getAssociation() != null ) ? 
												((CDirectorySourceLocation)element).getAssociation().toOSString() : "";
							}
							return null;
						}

						public void modify( Object element, String property, Object value )
						{
							Object entry = getSelection();
							if ( entry instanceof CDirectorySourceLocation && 
								 property.equals( CP_ASSOCIATION ) && 
								 value instanceof String )
							{
								Path association = new Path( (String)value );
								if ( association.isValidPath( (String)value ) )
								{
									((CDirectorySourceLocation)entry).setAssociation( association );
									fSourceListField.refresh();
									updateLaunchConfigurationDialog();
								}
							}
						}
					};
	}

	public Control getControl()
	{
		return fControl;
	}
	
	public void initialize( ICSourceLocator locator )
	{
		fLocator = locator;
		ICSourceLocation[] locations = new ICSourceLocation[0];
		if ( fLocator != null )
			locations = fLocator.getSourceLocations();
		resetLocations( locations );
	}

	private void resetLocations( ICSourceLocation[] locations )
	{
		fSourceListField.removeAllElements();
		for ( int i = 0; i < locations.length; ++i )
		{
			fSourceListField.addElement( locations[i] );
		}
	}

	protected void doButtonPressed( int index )
	{
		switch( index )
		{
			case 0:		// Add...
				if ( addSourceLocation() )
					fIsDirty = true;
				break;
			case 7:
				restoreDefaults();
			case 2:
			case 3:
			case 5:
				fIsDirty = true;
				break;
		}
		if ( isDirty() )
			updateLaunchConfigurationDialog();
	}
	
	protected void doSelectionChanged()
	{
	}
	
	public ICSourceLocation[] getSourceLocations()
	{
		return (ICSourceLocation[])fSourceListField.getElements().toArray( new ICSourceLocation[fSourceListField.getElements().size()] );
	}
	
	private boolean addSourceLocation()
	{
		AddSourceLocationWizard wizard = new AddSourceLocationWizard( getSourceLocations() );
		WizardDialog dialog = new WizardDialog( fControl.getShell(), wizard );
		if ( dialog.open() == Window.OK )
		{
			fSourceListField.addElement( wizard.getSourceLocation() );
			return true;
		}
		return false;
	}

	private void updateLaunchConfigurationDialog()
	{
		if ( getLaunchConfigurationDialog() != null )
		{
			getLaunchConfigurationDialog().updateMessage();
			getLaunchConfigurationDialog().updateButtons();
		}
		fIsDirty = false;
	}

	public ILaunchConfigurationDialog getLaunchConfigurationDialog()
	{
		return fLaunchConfigurationDialog;
	}

	public void setLaunchConfigurationDialog( ILaunchConfigurationDialog launchConfigurationDialog )
	{
		fLaunchConfigurationDialog = launchConfigurationDialog;
	}

	public boolean isDirty()
	{
		return fIsDirty;
	}
	
	protected Object getSelection()
	{
		List list = fSourceListField.getSelectedElements();
		return ( list.size() > 0 ) ? list.get( 0 ) : null;
	}
	
	protected void restoreDefaults()
	{
		ICSourceLocation[] locations = new ICSourceLocation[0];
		if ( getProject() != null )
			locations = CSourceLocator.getDefaultSourceLocations( getProject() );
		resetLocations( locations );
	}

	public IProject getProject()
	{
		return fProject;
	}

	public void setProject( IProject project )
	{
		fProject = project;
	}
}
