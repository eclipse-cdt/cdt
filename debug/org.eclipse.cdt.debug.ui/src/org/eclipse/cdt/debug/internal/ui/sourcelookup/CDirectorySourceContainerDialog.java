/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui.sourcelookup; 

import java.io.File;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IStringButtonAdapter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.LayoutUtil;
import org.eclipse.cdt.debug.internal.ui.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.Separator;
import org.eclipse.cdt.debug.internal.ui.dialogfields.StringButtonDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
 
/**
 * The dialog for selecting the local file system for which a source 
 * container will be created.
 */
public class CDirectorySourceContainerDialog extends TitleAreaDialog {

	private StringButtonDialogField fDirectoryField;
	private SelectionButtonDialogField fSubfoldersField;
	private File fDirectory;
	private boolean fSearchSubfolders;

	/** 
	 * Constructor for CDirectorySourceContainerDialog. 
	 */
	public CDirectorySourceContainerDialog( Shell parentShell ) {
		this( parentShell, new File( "" ), false ); //$NON-NLS-1$
	}

	/** 
	 * Constructor for CDirectorySourceContainerDialog. 
	 */
	public CDirectorySourceContainerDialog( Shell parentShell, File directory, boolean searchSubfolders ) {
		super( parentShell );
		fDirectory = directory;
		fSearchSubfolders = searchSubfolders;
		fDirectoryField = new StringButtonDialogField( 
				new IStringButtonAdapter() {
					
					public void changeControlPressed( DialogField field ) {
						browse();
					}
				} );
		fDirectoryField.setLabelText( SourceLookupUIMessages.getString( "CDirectorySourceContainerDialog.0" ) ); //$NON-NLS-1$
		fDirectoryField.setButtonLabel( SourceLookupUIMessages.getString( "CDirectorySourceContainerDialog.1" ) ); //$NON-NLS-1$
		fDirectoryField.setDialogFieldListener( 
				new IDialogFieldListener() {

					public void dialogFieldChanged( DialogField field ) {
						// TODO Auto-generated method stub
						
					}
				} );

		fSubfoldersField = new SelectionButtonDialogField( SWT.CHECK );
		fSubfoldersField.setLabelText( SourceLookupUIMessages.getString( "CDirectorySourceContainerDialog.2" ) ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea( Composite parent ) {
		setTitle( SourceLookupUIMessages.getString( "CDirectorySourceContainerDialog.3" ) ); //$NON-NLS-1$
		Font font = parent.getFont();
		Composite composite = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout();
		layout.numColumns = Math.max( fDirectoryField.getNumberOfControls(), fSubfoldersField.getNumberOfControls() );
		layout.marginHeight = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_MARGIN );
		layout.marginWidth = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_MARGIN );
//		layout.verticalSpacing = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_SPACING );
//		layout.horizontalSpacing = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_SPACING );
		composite.setLayout( layout );
		GridData data = new GridData( GridData.FILL_BOTH );
		composite.setLayoutData( data );
		composite.setFont( font );

		Dialog.applyDialogFont( composite );
		PlatformUI.getWorkbench().getHelpSystem().setHelp( getShell(), ICDebugHelpContextIds.ADD_DIRECTORY_CONTAINER_DIALOG );

		PixelConverter converter = new PixelConverter( composite );
		fDirectoryField.doFillIntoGrid( composite, layout.numColumns );
		LayoutUtil.setHorizontalGrabbing( fDirectoryField.getTextControl( null ) );

		new Separator().doFillIntoGrid( composite, layout.numColumns, converter.convertHeightInCharsToPixels( 1 ) );

		fSubfoldersField.doFillIntoGrid( composite, layout.numColumns );

		initialize();

		setMessage( null );
		return super.createDialogArea( parent );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell( Shell newShell ) {
		newShell.setText( SourceLookupUIMessages.getString( "CDirectorySourceContainerDialog.4" ) ); //$NON-NLS-1$
		super.configureShell( newShell );
	}

	protected void browse() {
		String last = fDirectoryField.getText();
		DirectoryDialog dialog = new DirectoryDialog( getShell(), SWT.SINGLE );
		dialog.setFilterPath( last );
		String result = dialog.open();
		if ( result == null ) {
			return;
		}
		fDirectoryField.setText( result );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		fDirectory = new File( fDirectoryField.getText() );
		fSearchSubfolders = fSubfoldersField.isSelected();
		super.okPressed();
	}
	
	public File getDirectory() {
		return fDirectory;
	}
	
	public boolean isSearchSubfolders() {
		return fSearchSubfolders;
	}
	
	private void initialize() {
		fDirectoryField.setText( getDirectory().getPath() );
		fSubfoldersField.setSelection( isSearchSubfolders() );
	}
}
