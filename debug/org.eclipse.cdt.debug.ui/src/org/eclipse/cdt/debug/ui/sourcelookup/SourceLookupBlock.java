/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.ui.sourcelookup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.IDirectorySourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.SourceLocationFactory;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CDirectorySourceLocation;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLocator;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.CheckedListDialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IListAdapter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.LayoutUtil;
import org.eclipse.cdt.debug.internal.ui.dialogfields.ListDialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.Separator;
import org.eclipse.cdt.debug.internal.ui.wizards.AddSourceLocationWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
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

	private static class SourceLookupLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		public Image getColumnImage( Object element, int columnIndex )
		{
			if ( columnIndex == 0 )
			{
				if ( element instanceof IProjectSourceLocation )
				{
					if ( ((IProjectSourceLocation)element).getProject().isOpen() )
						return CDebugImages.get( CDebugImages.IMG_OBJS_PROJECT );
					else
						return CDebugImages.get( CDebugImages.IMG_OBJS_CLOSED_PROJECT );
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
	private CheckedListDialogField fGeneratedSourceListField;
	private SourceListDialogField fAddedSourceListField;
	private SelectionButtonDialogField fSearchForDuplicateFiles;
	private ILaunchConfigurationDialog fLaunchConfigurationDialog = null;
	private boolean fIsDirty = false;
	private ICSourceLocator fLocator = null;
	private IProject fProject = null;

	/**
	 * Constructor for SourceLookupBlock.
	 */
	public SourceLookupBlock()
	{
		String[] generatedSourceButtonLabels = new String[] 
		{
			/* 0 */ "Select All",
			/* 1 */ "Deselect All",
		};

		String[] addedSourceButtonLabels = new String[] 
		{
			/* 0 */ "Add...",
			/* 1 */ null,
			/* 2 */ "Up",
			/* 3 */ "Down",
			/* 4 */ null,
			/* 5 */ "Remove",
		};

		IListAdapter generatedSourceAdapter = new IListAdapter()
													{
														public void customButtonPressed( DialogField field, int index )
														{
															doGeneratedSourceButtonPressed( index );
														}
	
														public void selectionChanged( DialogField field )
														{
															doGeneratedSourceSelectionChanged();
														}
													};

		fGeneratedSourceListField = new CheckedListDialogField( generatedSourceAdapter, generatedSourceButtonLabels, new SourceLookupLabelProvider() );
		fGeneratedSourceListField.setLabelText( "Generic Source Locations" );
		fGeneratedSourceListField.setCheckAllButtonIndex( 0 );
		fGeneratedSourceListField.setUncheckAllButtonIndex( 1 );
		fGeneratedSourceListField.setDialogFieldListener( new IDialogFieldListener()
																{
																	public void dialogFieldChanged( DialogField field )
																	{
																		doCheckStateChanged();
																	}

																} );
		IListAdapter addedSourceAdapter = new IListAdapter()
												{
													public void customButtonPressed( DialogField field, int index )
													{
														doAddedSourceButtonPressed( index );
													}

													public void selectionChanged( DialogField field )
													{
														doAddedSourceSelectionChanged();
													}
												};

		fAddedSourceListField = new SourceListDialogField( addedSourceAdapter, addedSourceButtonLabels, new SourceLookupLabelProvider() );
		fAddedSourceListField.setLabelText( "Additional Source Locations" );
		fAddedSourceListField.setUpButtonIndex( 2 );
		fAddedSourceListField.setDownButtonIndex( 3 );
		fAddedSourceListField.setRemoveButtonIndex( 5 );
		fSearchForDuplicateFiles = new SelectionButtonDialogField( SWT.CHECK );
		fSearchForDuplicateFiles.setLabelText( "Search for duplicate source files" );
		fSearchForDuplicateFiles.setDialogFieldListener( 
									new IDialogFieldListener()
									{
										public void dialogFieldChanged( DialogField field )
										{
											doCheckStateChanged();
										}

									} );
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
		
		fGeneratedSourceListField.doFillIntoGrid( fControl, 3 );
		LayoutUtil.setHorizontalSpan( fGeneratedSourceListField.getLabelControl( null ), 2 );
		LayoutUtil.setWidthHint( fGeneratedSourceListField.getLabelControl( null ), converter.convertWidthInCharsToPixels( 40 ) );
		LayoutUtil.setHorizontalGrabbing( fGeneratedSourceListField.getListControl( null ) );
		((CheckboxTableViewer)fGeneratedSourceListField.getTableViewer()).
								addCheckStateListener( new ICheckStateListener()
															{
																public void checkStateChanged( CheckStateChangedEvent event )
																{
																	if ( event.getElement() instanceof IProjectSourceLocation )
																		doCheckStateChanged();
																}
	
															} );

		new Separator().doFillIntoGrid( fControl, 3, converter.convertHeightInCharsToPixels( 1 ) );

		fAddedSourceListField.doFillIntoGrid( fControl, 3 );
		LayoutUtil.setHorizontalSpan( fAddedSourceListField.getLabelControl( null ), 2 );
		LayoutUtil.setWidthHint( fAddedSourceListField.getLabelControl( null ), converter.convertWidthInCharsToPixels( 40 ) );
		LayoutUtil.setHorizontalGrabbing( fAddedSourceListField.getListControl( null ) );

		TableViewer viewer = fAddedSourceListField.getTableViewer();
		Table table = viewer.getTable();
		CellEditor cellEditor = new TextCellEditor( table );
		viewer.setCellEditors( new CellEditor[]{ null, cellEditor } );
		viewer.setColumnProperties( new String[]{ CP_LOCATION, CP_ASSOCIATION } );
		viewer.setCellModifier( createCellModifier() );
		
//		new Separator().doFillIntoGrid( fControl, 3, converter.convertHeightInCharsToPixels( 1 ) );

		fSearchForDuplicateFiles.doFillIntoGrid( fControl, 3 );
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
									getAddedSourceListField().refresh();
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
		if ( fLocator != null )
		{
			ICSourceLocation[] locations = fLocator.getSourceLocations();
			initializeGeneratedLocations( fLocator.getProject(), locations );
			resetAdditionalLocations( locations );
			fSearchForDuplicateFiles.setSelection( fLocator.searchForDuplicateFiles() );
		} 
	}

	private void initializeGeneratedLocations( IProject project, ICSourceLocation[] locations )
	{
		fGeneratedSourceListField.removeAllElements();
		if ( project == null && project.exists() && project.isOpen() )
			return;
		List list = CDebugUtils.getReferencedProjects( project );
		IProject[] refs = (IProject[])list.toArray( new IProject[list.size()] );
		ICSourceLocation loc = getLocationForProject( project, locations );
		boolean checked = ( loc != null && ((IProjectSourceLocation)loc).isGeneric() );
		if ( loc == null )
			loc = SourceLocationFactory.createProjectSourceLocation( project, true );
		fGeneratedSourceListField.addElement( loc );
		fGeneratedSourceListField.setChecked( loc, checked );

		for ( int i = 0; i < refs.length; ++i )
		{
			loc = getLocationForProject( refs[i], locations );
			checked = ( loc != null );
			if ( loc == null )
				loc = SourceLocationFactory.createProjectSourceLocation( refs[i], true );
			fGeneratedSourceListField.addElement( loc );
			fGeneratedSourceListField.setChecked( loc, checked );
		}
	}

	private void resetGeneratedLocations( ICSourceLocation[] locations )
	{
		fGeneratedSourceListField.checkAll( false );
		for ( int i = 0; i < locations.length; ++i )
		{
			if ( locations[i] instanceof IProjectSourceLocation && 
				 ((IProjectSourceLocation)locations[i]).isGeneric() )
				fGeneratedSourceListField.setChecked( locations[i], true );
		}
	}

	private void resetAdditionalLocations( ICSourceLocation[] locations )
	{
		fAddedSourceListField.removeAllElements();
		for ( int i = 0; i < locations.length; ++i )
		{
			if ( !( locations[i] instanceof IProjectSourceLocation ) || !((IProjectSourceLocation)locations[i]).isGeneric() )
				fAddedSourceListField.addElement( locations[i] );
		}
	}

	protected void doAddedSourceButtonPressed( int index )
	{
		switch( index )
		{
			case 0:		// Add...
				if ( addSourceLocation() )
					fIsDirty = true;
				break;
			case 2:		// Up
			case 3:		// Down
			case 5:		// Remove
				fIsDirty = true;
				break;
		}
		if ( isDirty() )
			updateLaunchConfigurationDialog();
	}
	
	protected void doAddedSourceSelectionChanged()
	{
	}

	protected void doCheckStateChanged()
	{	
		fIsDirty = true;
		updateLaunchConfigurationDialog();
	}

	protected void doGeneratedSourceButtonPressed( int index )
	{
		switch( index )
		{
			case 0:		// Select All
			case 1:		// Deselect All
				fIsDirty = true;
				break;
		}
		if ( isDirty() )
			updateLaunchConfigurationDialog();
	}
	
	protected void doGeneratedSourceSelectionChanged()
	{
	}
	
	public ICSourceLocation[] getSourceLocations()
	{
		ArrayList list = new ArrayList( getGeneratedSourceListField().getElements().size() + getAddedSourceListField().getElements().size() );
		Iterator it = getGeneratedSourceListField().getElements().iterator();
		while( it.hasNext() )
		{
			IProjectSourceLocation location = (IProjectSourceLocation)it.next();
			if ( getGeneratedSourceListField().isChecked( location ) )
				list.add( location );
		}
		list.addAll( getAddedSourceListField().getElements() );
		return (ICSourceLocation[])list.toArray( new ICSourceLocation[list.size()] );
	}
	
	private boolean addSourceLocation()
	{
		AddSourceLocationWizard wizard = new AddSourceLocationWizard( getSourceLocations() );
		WizardDialog dialog = new WizardDialog( fControl.getShell(), wizard );
		if ( dialog.open() == Window.OK )
		{
			fAddedSourceListField.addElement( wizard.getSourceLocation() );
			return true;
		}
		return false;
	}

	protected void updateLaunchConfigurationDialog()
	{
		if ( getLaunchConfigurationDialog() != null )
		{
			getLaunchConfigurationDialog().updateMessage();
			getLaunchConfigurationDialog().updateButtons();
			fIsDirty = false;
		}
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
		List list = fAddedSourceListField.getSelectedElements();
		return ( list.size() > 0 ) ? list.get( 0 ) : null;
	}
	
	protected void restoreDefaults()
	{
		ICSourceLocation[] locations = new ICSourceLocation[0];
		if ( getProject() != null )
			locations = CSourceLocator.getDefaultSourceLocations( getProject() );
		resetGeneratedLocations( locations );
		resetAdditionalLocations( locations );
		fSearchForDuplicateFiles.setSelection( false );
	}

	public IProject getProject()
	{
		return fProject;
	}

	public void setProject( IProject project )
	{
		fProject = project;
	}

	public SourceListDialogField getAddedSourceListField()
	{
		return fAddedSourceListField;
	}

	public CheckedListDialogField getGeneratedSourceListField()
	{
		return fGeneratedSourceListField;
	}

	private ICSourceLocation getLocationForProject( IProject project, ICSourceLocation[] locations )
	{
		for ( int i = 0; i < locations.length; ++i )
			if ( locations[i] instanceof IProjectSourceLocation &&
				 project.equals( ((IProjectSourceLocation)locations[i]).getProject() ) )
				return locations[i];
		return null;
	}

	public boolean searchForDuplicateFiles()
	{
		return ( fSearchForDuplicateFiles != null ) ? fSearchForDuplicateFiles.isSelected() : false;
	}
}
