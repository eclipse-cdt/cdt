/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.ui.sourcelookup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.SourceLookupFactory;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLocator;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.CheckedListDialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IListAdapter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.LayoutUtil;
import org.eclipse.cdt.debug.internal.ui.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.Separator;
import org.eclipse.cdt.debug.internal.ui.wizards.AddSourceLocationWizard;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * The composite widget shared by the source lookup launch tab and property page.
 */
public class SourceLookupBlock implements Observer {

	private Composite fControl = null;

	private CheckedListDialogField fGeneratedSourceListField;

	private SourceListDialogField fAddedSourceListField;

	private SelectionButtonDialogField fSearchForDuplicateFiles;

	private ILaunchConfigurationDialog fLaunchConfigurationDialog = null;

	private boolean fIsDirty = false;

	private IProject fProject = null;

	/**
	 * Constructor for SourceLookupBlock.
	 */
	public SourceLookupBlock() {
		fGeneratedSourceListField = createGeneratedSourceListField();
		fAddedSourceListField = createAddedSourceListField();
		fSearchForDuplicateFiles = createSearchForDuplicateFilesButton();
	}

	public void createControl( Composite parent ) {
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
		((CheckboxTableViewer)fGeneratedSourceListField.getTableViewer()).addCheckStateListener( new ICheckStateListener() {

			public void checkStateChanged( CheckStateChangedEvent event ) {
				if ( event.getElement() instanceof IProjectSourceLocation )
					doCheckStateChanged();
			}
		} );
		new Separator().doFillIntoGrid( fControl, 3, converter.convertHeightInCharsToPixels( 1 ) );
		fAddedSourceListField.doFillIntoGrid( fControl, 3 );
		LayoutUtil.setHorizontalSpan( fAddedSourceListField.getLabelControl( null ), 2 );
		LayoutUtil.setWidthHint( fAddedSourceListField.getLabelControl( null ), converter.convertWidthInCharsToPixels( 40 ) );
		LayoutUtil.setHorizontalGrabbing( fAddedSourceListField.getListControl( null ) );
		//		new Separator().doFillIntoGrid( fControl, 3, converter.convertHeightInCharsToPixels( 1 ) );
		fSearchForDuplicateFiles.doFillIntoGrid( fControl, 3 );
	}

	public Control getControl() {
		return fControl;
	}

	public void initialize( ILaunchConfiguration configuration ) {
		IProject project = getProjectFromLaunchConfiguration( configuration );
		if ( project != null ) {
			setProject( project );
			try {
				String id = configuration.getAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "" ); //$NON-NLS-1$
				if ( isEmpty( id ) || CDebugUIPlugin.getDefaultSourceLocatorID().equals( id ) || CDebugUIPlugin.getDefaultSourceLocatorOldID().equals( id ) ) {
					String memento = configuration.getAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, "" ); //$NON-NLS-1$
					if ( !isEmpty( memento ) )
						initializeFromMemento( memento );
					else
						initializeDefaults();
				}
			}
			catch( CoreException e ) {
				initializeDefaults();
			}
		}
		else {
			initializeGeneratedLocations( null, new ICSourceLocation[0] );
			resetAdditionalLocations( CDebugCorePlugin.getDefault().getCommonSourceLocations() );
			fSearchForDuplicateFiles.setSelection( CDebugCorePlugin.getDefault().getPluginPreferences().getBoolean( ICDebugConstants.PREF_SEARCH_DUPLICATE_FILES ) );
		}
	}

	private void initializeFromMemento( String memento ) throws CoreException {
		IPersistableSourceLocator locator = CDebugUIPlugin.createDefaultSourceLocator();
		locator.initializeFromMemento( memento );
		if ( locator instanceof IAdaptable ) {
			ICSourceLocator clocator = (ICSourceLocator)((IAdaptable)locator).getAdapter( ICSourceLocator.class );
			if ( clocator != null )
				initializeFromLocator( clocator );
		}
	}

	private void initializeDefaults() {
		fGeneratedSourceListField.removeAllElements();
		IProject project = getProject();
		if ( project != null && project.exists() && project.isOpen() ) {
			ICSourceLocation location = SourceLookupFactory.createProjectSourceLocation( project, true );
			fGeneratedSourceListField.addElement( location );
			fGeneratedSourceListField.setChecked( location, true );
			List list = CDebugUtils.getReferencedProjects( project );
			Iterator it = list.iterator();
			while( it.hasNext() ) {
				location = SourceLookupFactory.createProjectSourceLocation( (IProject)it.next(), true );
				fGeneratedSourceListField.addElement( location );
				fGeneratedSourceListField.setChecked( location, true );
			}
		}
		resetAdditionalLocations( CDebugCorePlugin.getDefault().getCommonSourceLocations() );
		fSearchForDuplicateFiles.setSelection( CDebugCorePlugin.getDefault().getPluginPreferences().getBoolean( ICDebugConstants.PREF_SEARCH_DUPLICATE_FILES ) );
	}

	private void initializeFromLocator( ICSourceLocator locator ) {
		ICSourceLocation[] locations = locator.getSourceLocations();
		initializeGeneratedLocations( locator.getProject(), locations );
		resetAdditionalLocations( locations );
		fSearchForDuplicateFiles.setSelection( locator.searchForDuplicateFiles() );
	}

	private void initializeGeneratedLocations( IProject project, ICSourceLocation[] locations ) {
		fGeneratedSourceListField.removeAllElements();
		if ( project == null || !project.exists() || !project.isOpen() )
			return;
		List list = CDebugUtils.getReferencedProjects( project );
		IProject[] refs = (IProject[])list.toArray( new IProject[list.size()] );
		ICSourceLocation loc = getLocationForProject( project, locations );
		boolean checked = (loc != null && ((IProjectSourceLocation)loc).isGeneric());
		if ( loc == null )
			loc = SourceLookupFactory.createProjectSourceLocation( project, true );
		fGeneratedSourceListField.addElement( loc );
		fGeneratedSourceListField.setChecked( loc, checked );
		for( int i = 0; i < refs.length; ++i ) {
			loc = getLocationForProject( refs[i], locations );
			checked = (loc != null);
			if ( loc == null )
				loc = SourceLookupFactory.createProjectSourceLocation( refs[i], true );
			fGeneratedSourceListField.addElement( loc );
			fGeneratedSourceListField.setChecked( loc, checked );
		}
	}

	private void resetGeneratedLocations( ICSourceLocation[] locations ) {
		fGeneratedSourceListField.checkAll( false );
		for( int i = 0; i < locations.length; ++i ) {
			if ( locations[i] instanceof IProjectSourceLocation && ((IProjectSourceLocation)locations[i]).isGeneric() )
				fGeneratedSourceListField.setChecked( locations[i], true );
		}
	}

	private void resetAdditionalLocations( ICSourceLocation[] locations ) {
		fAddedSourceListField.removeAllElements();
		for( int i = 0; i < locations.length; ++i ) {
			if ( !(locations[i] instanceof IProjectSourceLocation) || !((IProjectSourceLocation)locations[i]).isGeneric() )
				fAddedSourceListField.addElement( locations[i] );
		}
	}

	public void performApply( ILaunchConfigurationWorkingCopy configuration ) {
		IPersistableSourceLocator locator = CDebugUIPlugin.createDefaultSourceLocator();
		try {
			locator.initializeDefaults( configuration );
			if ( locator instanceof IAdaptable ) {
				ICSourceLocator clocator = (ICSourceLocator)((IAdaptable)locator).getAdapter( ICSourceLocator.class );
				if ( clocator != null && getProject() != null && getProject().equals( getProjectFromLaunchConfiguration( configuration ) ) ) {
					clocator.setSourceLocations( getSourceLocations() );
					clocator.setSearchForDuplicateFiles( searchForDuplicateFiles() );
				}
			}
			configuration.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, locator.getMemento() );
		}
		catch( CoreException e ) {
		}
	}

	protected void doCheckStateChanged() {
		fIsDirty = true;
		updateLaunchConfigurationDialog();
	}

	protected void doGeneratedSourceButtonPressed( int index ) {
		switch( index ) {
			case 0: // Select All
			case 1: // Deselect All
				fIsDirty = true;
				break;
		}
		if ( isDirty() )
			updateLaunchConfigurationDialog();
	}

	protected void doGeneratedSourceSelectionChanged() {
	}

	protected void doAddedSourceButtonPressed( int index ) {
		switch( index ) {
			case 0: // Add...
				if ( addSourceLocation() )
					fIsDirty = true;
				break;
			case 2: // Up
			case 3: // Down
			case 5: // Remove
				fIsDirty = true;
				break;
		}
		if ( isDirty() )
			updateLaunchConfigurationDialog();
	}

	public ICSourceLocation[] getSourceLocations() {
		ArrayList list = new ArrayList( getGeneratedSourceListField().getElements().size() + getAddedSourceListField().getElements().size() );
		Iterator it = getGeneratedSourceListField().getElements().iterator();
		while( it.hasNext() ) {
			IProjectSourceLocation location = (IProjectSourceLocation)it.next();
			if ( getGeneratedSourceListField().isChecked( location ) )
				list.add( location );
		}
		list.addAll( getAddedSourceListField().getElements() );
		return (ICSourceLocation[])list.toArray( new ICSourceLocation[list.size()] );
	}

	private boolean addSourceLocation() {
		AddSourceLocationWizard wizard = new AddSourceLocationWizard( getSourceLocations() );
		WizardDialog dialog = new WizardDialog( fControl.getShell(), wizard );
		if ( dialog.open() == Window.OK ) {
			fAddedSourceListField.addElement( wizard.getSourceLocation() );
			return true;
		}
		return false;
	}

	protected void updateLaunchConfigurationDialog() {
		if ( getLaunchConfigurationDialog() != null ) {
			getLaunchConfigurationDialog().updateMessage();
			getLaunchConfigurationDialog().updateButtons();
			fIsDirty = false;
		}
	}

	public ILaunchConfigurationDialog getLaunchConfigurationDialog() {
		return fLaunchConfigurationDialog;
	}

	public void setLaunchConfigurationDialog( ILaunchConfigurationDialog launchConfigurationDialog ) {
		fLaunchConfigurationDialog = launchConfigurationDialog;
	}

	public boolean isDirty() {
		return fIsDirty;
	}

	protected Object getSelection() {
		List list = fAddedSourceListField.getSelectedElements();
		return (list.size() > 0) ? list.get( 0 ) : null;
	}

	protected void restoreDefaults() {
		ICSourceLocation[] locations = new ICSourceLocation[0];
		if ( getProject() != null )
			locations = CSourceLocator.getDefaultSourceLocations( getProject() );
		resetGeneratedLocations( locations );
		resetAdditionalLocations( locations );
		fSearchForDuplicateFiles.setSelection( CDebugCorePlugin.getDefault().getPluginPreferences().getBoolean( ICDebugConstants.PREF_SEARCH_DUPLICATE_FILES ) );
	}

	public IProject getProject() {
		return fProject;
	}

	private void setProject( IProject project ) {
		fProject = project;
	}

	public SourceListDialogField getAddedSourceListField() {
		return fAddedSourceListField;
	}

	public CheckedListDialogField getGeneratedSourceListField() {
		return fGeneratedSourceListField;
	}

	private ICSourceLocation getLocationForProject( IProject project, ICSourceLocation[] locations ) {
		for( int i = 0; i < locations.length; ++i )
			if ( locations[i] instanceof IProjectSourceLocation && project.equals( ((IProjectSourceLocation)locations[i]).getProject() ) )
				return locations[i];
		return null;
	}

	public boolean searchForDuplicateFiles() {
		return (fSearchForDuplicateFiles != null) ? fSearchForDuplicateFiles.isSelected() : false;
	}

	private CheckedListDialogField createGeneratedSourceListField() {
		String[] generatedSourceButtonLabels = new String[]{
				/* 0 */SourceLookupMessages.getString( "SourceLookupBlock.0" ), //$NON-NLS-1$
				/* 1 */SourceLookupMessages.getString( "SourceLookupBlock.1" ), //$NON-NLS-1$
		};
		IListAdapter generatedSourceAdapter = new IListAdapter() {

			public void customButtonPressed( DialogField field, int index ) {
				doGeneratedSourceButtonPressed( index );
			}

			public void selectionChanged( DialogField field ) {
				doGeneratedSourceSelectionChanged();
			}
		};
		CheckedListDialogField field = new CheckedListDialogField( generatedSourceAdapter, generatedSourceButtonLabels, new SourceLookupLabelProvider() );
		field.setLabelText( SourceLookupMessages.getString( "SourceLookupBlock.2" ) ); //$NON-NLS-1$
		field.setCheckAllButtonIndex( 0 );
		field.setUncheckAllButtonIndex( 1 );
		field.setDialogFieldListener( new IDialogFieldListener() {

			public void dialogFieldChanged( DialogField f ) {
				doCheckStateChanged();
			}
		} );
		return field;
	}

	private SourceListDialogField createAddedSourceListField() {
		SourceListDialogField field = new SourceListDialogField( SourceLookupMessages.getString( "SourceLookupBlock.3" ), //$NON-NLS-1$
				new IListAdapter() {

					public void customButtonPressed( DialogField f, int index ) {
						doAddedSourceButtonPressed( index );
					}

					public void selectionChanged( DialogField f ) {
					}
				} );
		field.addObserver( this );
		return field;
	}

	private SelectionButtonDialogField createSearchForDuplicateFilesButton() {
		SelectionButtonDialogField button = new SelectionButtonDialogField( SWT.CHECK );
		button.setLabelText( SourceLookupMessages.getString( "SourceLookupBlock.4" ) ); //$NON-NLS-1$
		button.setDialogFieldListener( new IDialogFieldListener() {

			public void dialogFieldChanged( DialogField field ) {
				doCheckStateChanged();
			}
		} );
		return button;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update( Observable o, Object arg ) {
		if ( arg instanceof Integer && ((Integer)arg).intValue() == 0 ) {
			if ( addSourceLocation() )
				fIsDirty = true;
		}
		else
			fIsDirty = true;
		if ( fIsDirty )
			updateLaunchConfigurationDialog();
	}

	private boolean isEmpty( String string ) {
		return string == null || string.length() == 0;
	}

	public void dispose() {
		if ( getAddedSourceListField() != null ) {
			getAddedSourceListField().deleteObserver( this );
			getAddedSourceListField().dispose();
		}
	}

	private IProject getProjectFromLaunchConfiguration( ILaunchConfiguration configuration ) {
		try {
			String projectName = configuration.getAttribute( ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "" ); //$NON-NLS-1$
			if ( !isEmpty( projectName ) ) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
				if ( project != null && project.exists() && project.isOpen() )
					return project;
			}
		}
		catch( CoreException e ) {
		}
		return null;
	}
}
