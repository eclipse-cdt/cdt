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
package org.eclipse.cdt.debug.internal.ui.wizards;

import java.io.File;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IDirectorySourceLocation;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.ui.sourcelookup.INewSourceLocationWizard;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;

/**
 * The wizard to add a file system directory based source location to the source locator.
 */
public class AddDirectorySourceLocationWizard extends Wizard implements INewSourceLocationWizard {

	protected static final String PAGE_NAME = "AddDirectorySourceLocationWizardPage"; //$NON-NLS-1$

	public class AddDirtectorySourceLocationWizardPage extends WizardPage {

		private AddDirectorySourceLocationBlock fAttachBlock;

		/**
		 * Constructor for AddDirtectorySourceLocationWizardPage.
		 * 
		 * @param pageName
		 * @param title
		 * @param titleImage
		 */
		public AddDirtectorySourceLocationWizardPage( AddDirectorySourceLocationWizard wizard, IPath initialAssociationPath ) {
			super( PAGE_NAME, WizardMessages.getString( "AddDirectorySourceLocationWizard.0" ), CDebugImages.DESC_WIZBAN_ADD_DIR_SOURCE_LOCATION ); //$NON-NLS-1$
			setWindowTitle( WizardMessages.getString( "AddDirectorySourceLocationWizard.1" ) ); //$NON-NLS-1$
			setMessage( WizardMessages.getString( "AddDirectorySourceLocationWizard.2" ) ); //$NON-NLS-1$
			setWizard( wizard );
			fAttachBlock = new AddDirectorySourceLocationBlock( initialAssociationPath );
		}

		/**
		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
		 */
		public void createControl( Composite parent ) {
			Composite composite = new Composite( parent, SWT.NULL );
			composite.setLayout( new GridLayout() );
			composite.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
			fAttachBlock.createControl( composite );
			fAttachBlock.addDirectoryModifyListener( new ModifyListener() {

				public void modifyText( ModifyEvent e ) {
					directoryChanged();
				}
			} );
			fAttachBlock.addAssociationModifyListener( new ModifyListener() {

				public void modifyText( ModifyEvent e ) {
					associationChanged();
				}
			} );
			setControl( composite );
			updateState();
		}

		protected void directoryChanged() {
			updateState();
		}

		protected void associationChanged() {
			updateState();
		}

		private void updateState() {
			boolean complete = true;
			setErrorMessage( null );
			String dirText = fAttachBlock.getLocationPath();
			if ( dirText.length() == 0 ) {
				setErrorMessage( CDebugUIPlugin.getResourceString( "internal.ui.wizards.AddDirectorySourceLocationWizard.ErrorDirectoryEmpty" ) ); //$NON-NLS-1$
				complete = false;
			}
			else {
				File file = new File( dirText );
				if ( !file.exists() || !file.isDirectory() ) {
					setErrorMessage( WizardMessages.getString( "AddDirectorySourceLocationWizard.3" ) ); //$NON-NLS-1$
					complete = false;
				}
				else if ( !file.isAbsolute() ) {
					setErrorMessage( WizardMessages.getString( "AddDirectorySourceLocationWizard.4" ) ); //$NON-NLS-1$
					complete = false;
				}
			}
			setPageComplete( complete );
		}

		private IDirectorySourceLocation getSourceLocation() {
			return fAttachBlock.getSourceLocation();
		}

		protected boolean finish() {
			fSourceLocation = getSourceLocation();
			return (fSourceLocation != null);
		}
	}

	protected IDirectorySourceLocation fSourceLocation = null;

	private IPath fInitialAssociationPath = null;

	/**
	 * Constructor for AddDirectorySourceLocationWizard.
	 */
	public AddDirectorySourceLocationWizard() {
		super();
	}

	/**
	 * Constructor for AddDirectorySourceLocationWizard.
	 */
	public AddDirectorySourceLocationWizard( IPath initialAssociationPath ) {
		super();
		fInitialAssociationPath = initialAssociationPath;
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		AddDirtectorySourceLocationWizardPage page = (AddDirtectorySourceLocationWizardPage)getStartingPage();
		if ( page != null ) {
			return page.finish();
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.ui.sourcelookup.INewSourceLocationWizard#getDescription()
	 */
	public String getDescription() {
		return WizardMessages.getString( "AddDirectorySourceLocationWizard.5" ); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		addPage( new AddDirtectorySourceLocationWizardPage( this, fInitialAssociationPath ) );
	}

	/**
	 * @see org.eclipse.cdt.debug.ui.sourcelookup.INewSourceLocationWizard#getSourceLocation()
	 */
	public ICSourceLocation getSourceLocation() {
		return fSourceLocation;
	}
}
