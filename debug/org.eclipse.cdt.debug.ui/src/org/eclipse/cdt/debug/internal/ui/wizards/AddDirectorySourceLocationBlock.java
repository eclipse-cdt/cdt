/***************************************************************************************************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 **************************************************************************************************************************************************************/
package org.eclipse.cdt.debug.internal.ui.wizards;

import org.eclipse.cdt.debug.core.sourcelookup.IDirectorySourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.SourceLookupFactory;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.cdt.debug.internal.ui.SWTUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddDirectorySourceLocationBlock {

	private Composite fControl = null;

	private Text fLocationText = null;

	private Text fAssociationText = null;

	private Button fAssocitedCheckButton = null;

	private Button fSearchSubfoldersButton = null;

	private Shell fShell = null;

	private IPath fInitialAssosciationPath = null;

	/**
	 * Constructor for AddDirectorySourceLocationBlock.
	 */
	public AddDirectorySourceLocationBlock( IPath initialAssosciationPath ) {
		fInitialAssosciationPath = initialAssosciationPath;
	}

	public void createControl( Composite parent ) {
		fShell = parent.getShell();
		fControl = new Composite( parent, SWT.NONE );
		fControl.setLayout( new GridLayout() );
		fControl.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		fControl.setFont( JFaceResources.getDialogFont() );
		createLocationControls( fControl );
		createAssociationControls( fControl );
		createSearchSubfoldersButton( fControl );
		setInitialAssociationPath();
	}

	private void setInitialAssociationPath() {
		fAssociationText.setEnabled( (fInitialAssosciationPath != null) );
		fAssocitedCheckButton.setSelection( (fInitialAssosciationPath != null) );
		if ( fInitialAssosciationPath != null ) {
			fAssociationText.setText( fInitialAssosciationPath.toOSString() );
		}
	}

	public Control getControl() {
		return fControl;
	}

	protected void createLocationControls( Composite parent ) {
		PixelConverter converter = new PixelConverter( parent );
		Label label = new Label( parent, SWT.NONE );
		label.setText( WizardMessages.getString( "AddDirectorySourceLocationBlock.0" ) ); //$NON-NLS-1$
		label.setLayoutData( new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL ) );
		Composite composite = new Composite( parent, SWT.NONE );
		composite.setLayout( new GridLayout( 2, false ) );
		GridData data = new GridData( GridData.FILL_BOTH );
		data.widthHint = converter.convertWidthInCharsToPixels( 70 );
		composite.setLayoutData( data );
		fLocationText = new Text( composite, SWT.SINGLE | SWT.BORDER );
		fLocationText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL ) );
		Button button = createButton( composite, WizardMessages.getString( "AddDirectorySourceLocationBlock.1" ) ); //$NON-NLS-1$
		button.addSelectionListener( new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(SelectionEvent)
			 */
			public void widgetSelected( SelectionEvent e ) {
				selectLocation();
			}
		} );
	}

	protected void selectLocation() {
		DirectoryDialog dialog = new DirectoryDialog( fShell );
		dialog.setMessage( WizardMessages.getString( "AddDirectorySourceLocationBlock.2" ) ); //$NON-NLS-1$
		String result = dialog.open();
		if ( result != null ) {
			fLocationText.setText( result );
		}
	}

	protected void createAssociationControls( Composite parent ) {
		Composite composite = new Composite( parent, SWT.NONE );
		composite.setLayout( new GridLayout() );
		GridData data = new GridData( GridData.FILL_BOTH );
		composite.setLayoutData( data );
		fAssocitedCheckButton = new Button( composite, SWT.CHECK );
		fAssocitedCheckButton.setText( WizardMessages.getString( "AddDirectorySourceLocationBlock.3" ) ); //$NON-NLS-1$
		fAssociationText = new Text( composite, SWT.SINGLE | SWT.BORDER );
		fAssociationText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		fAssocitedCheckButton.addSelectionListener( new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(SelectionEvent)
			 */
			public void widgetSelected( SelectionEvent e ) {
				associationSelectionChanged();
			}
		} );
	}

	protected void associationSelectionChanged() {
		boolean checked = fAssocitedCheckButton.getSelection();
		fAssociationText.setEnabled( checked );
		if ( !checked )
			fAssociationText.setText( "" ); //$NON-NLS-1$
	}

	protected void createSearchSubfoldersButton( Composite parent ) {
		Composite composite = new Composite( parent, SWT.NONE );
		composite.setLayout( new GridLayout() );
		GridData data = new GridData( GridData.FILL_BOTH );
		composite.setLayoutData( data );
		fSearchSubfoldersButton = new Button( composite, SWT.CHECK );
		fSearchSubfoldersButton.setText( WizardMessages.getString( "AddDirectorySourceLocationBlock.4" ) ); //$NON-NLS-1$
	}

	protected Button createButton( Composite parent, String label ) {
		Button button = new Button( parent, SWT.PUSH );
		button.setText( label );
		GridData data = new GridData( GridData.END );
		button.setLayoutData( data );
		SWTUtil.setButtonDimensionHint( button );
		button.setFont( parent.getFont() );
		return button;
	}

	public String getLocationPath() {
		return fLocationText.getText().trim();
	}

	public String getAssociationPath() {
		if ( fAssocitedCheckButton.getSelection() ) {
			return fAssociationText.getText().trim();
		}
		return ""; //$NON-NLS-1$
	}

	public boolean searchSubfolders() {
		return (fSearchSubfoldersButton != null) ? fSearchSubfoldersButton.getSelection() : false;
	}

	public IDirectorySourceLocation getSourceLocation() {
		if ( isLocationPathValid() ) {
			Path association = (isAssociationPathValid()) ? new Path( getAssociationPath() ) : null;
			return SourceLookupFactory.createDirectorySourceLocation( new Path( getLocationPath() ), association, searchSubfolders() );
		}
		return null;
	}

	public void addDirectoryModifyListener( ModifyListener listener ) {
		if ( fLocationText != null ) {
			fLocationText.addModifyListener( listener );
		}
	}

	public void addAssociationModifyListener( ModifyListener listener ) {
		if ( fAssociationText != null ) {
			fAssociationText.addModifyListener( listener );
		}
	}

	public void removeDirectoryModifyListener( ModifyListener listener ) {
		if ( fLocationText != null ) {
			fLocationText.removeModifyListener( listener );
		}
	}

	public void removeAssociationModifyListener( ModifyListener listener ) {
		if ( fAssociationText != null ) {
			fAssociationText.removeModifyListener( listener );
		}
	}

	private boolean isLocationPathValid() {
		if ( fLocationText != null && Path.EMPTY.isValidPath( fLocationText.getText().trim() ) ) {
			Path path = new Path( fLocationText.getText().trim() );
			return (path.toFile().exists() && path.toFile().isAbsolute());
		}
		return false;
	}

	public boolean isAssociationPathValid() {
		String pathString = getAssociationPath();
		if ( pathString.length() > 0 ) {
			return Path.EMPTY.isValidPath( pathString );
		}
		return true;
	}
}
