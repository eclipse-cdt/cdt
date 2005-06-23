/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.propertypages; 

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * The property page for a module.
 */
public class ModulePropertyPage extends PropertyPage {

	private Label fTypeField;
	private Label fCPUField;
	private Label fBaseAddressField;
	private Label fSizeField;
	private Label fSymbolsField;
	protected Text fSymbolsFileField;
	protected Button fBrowseButton;

	private ModuleProperties fProperties = null;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents( Composite parent ) {
		noDefaultAndApplyButton();
		Composite composite = new Composite( parent, SWT.NONE );
		Font font = parent.getFont();
		composite.setFont( font );
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		composite.setLayout( topLayout );
		composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		
		createFields( composite );
		initializeFields();

		setValid( true );
		return composite;
	}

	protected ICModule getModule() {
		return (ICModule)getElement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if ( getModuleProperties() != null && getModuleProperties().isDirty() ) {
			final IPath path = (IPath)getModuleProperties().getProperty( ModuleProperties.SYMBOLS_FILE );
			final ICModule module = getModule(); 
			if ( module != null ) {
				
				DebugPlugin.getDefault().asyncExec( 
						new Runnable() {
							public void run() {
								try {
									module.setSymbolsFileName( path );
								}
								catch( DebugException e ) {
									failed( PropertyPageMessages.getString( "ModulePropertyPage.15" ), e ); //$NON-NLS-1$
								}
							}
						} );
			}
		}
		return super.performOk();
	}

	protected ModuleProperties getModuleProperties() {
		if ( fProperties == null ) {
			fProperties = ModuleProperties.create( getModule() );
		}
		return fProperties;
	}

	protected void failed( String message, Throwable e ) {
		MultiStatus ms = new MultiStatus( CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, message, null );
		ms.add( new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), null ) );
		CDebugUtils.error( ms, getModule() );
	}

	private void createFields( Composite parent ) {
		fTypeField = createField( parent, PropertyPageMessages.getString( "ModulePropertyPage.0" ) ); //$NON-NLS-1$
		fCPUField = createField( parent, PropertyPageMessages.getString( "ModulePropertyPage.4" ) ); //$NON-NLS-1$
		fBaseAddressField = createField( parent, PropertyPageMessages.getString( "ModulePropertyPage.6" ) ); //$NON-NLS-1$
		fSizeField = createField( parent, PropertyPageMessages.getString( "ModulePropertyPage.8" ) ); //$NON-NLS-1$
		fSymbolsField = createField( parent, PropertyPageMessages.getString( "ModulePropertyPage.10" ) ); //$NON-NLS-1$
		createSymbolsFileField( parent );
	}

	private Label createField( Composite parent, String label ) {
		Font font = parent.getFont();
		Label l = new Label( parent, SWT.LEFT );
		l.setText( label );
		GridData gd = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
		l.setLayoutData( gd );
		l.setFont( font );
		Label v = new Label( parent, SWT.LEFT );
		gd = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
		v.setLayoutData( gd );
		v.setFont( font );
		return v;
	}

	private void createSymbolsFileField( Composite parent ) {
		Font font = parent.getFont();

		// Separator
		Label l = new Label( parent, SWT.LEFT );
		GridData gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 2;
		l.setLayoutData( gd );

		l = new Label( parent, SWT.LEFT );
		l.setText( PropertyPageMessages.getString( "ModulePropertyPage.13" ) ); //$NON-NLS-1$
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 2;
		l.setLayoutData( gd );
		l.setFont( font );
		Composite composite = new Composite( parent, SWT.NONE );
		composite.setFont( font );
		GridLayout layout = new GridLayout();
		layout.numColumns = 5;
		composite.setLayout( layout );
		gd = new GridData( GridData.FILL_BOTH );
		gd.horizontalSpan = 2;
		composite.setLayoutData( gd );

		// Text
		fSymbolsFileField = new Text( composite, SWT.SINGLE | SWT.BORDER );
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 4;
		fSymbolsFileField.setLayoutData( gd );

		fBrowseButton = new Button( composite, SWT.PUSH );
		fBrowseButton.setText( PropertyPageMessages.getString( "ModulePropertyPage.3" ) ); //$NON-NLS-1$
		fBrowseButton.addSelectionListener( 
			new SelectionListener() {

				public void widgetSelected( SelectionEvent e ) {
					FileDialog dialog = new FileDialog( fBrowseButton.getShell() );
					dialog.setFileName( ((IPath)getModuleProperties().getProperty( ModuleProperties.SYMBOLS_FILE )).toOSString() );
					String fn = dialog.open();
					if ( fn != null ) {
						IPath path = new Path( fn );
						fSymbolsFileField.setText( path.toOSString() );
						getModuleProperties().setProperty( ModuleProperties.SYMBOLS_FILE, path );
					}
				}

				public void widgetDefaultSelected( SelectionEvent e ) {
				}
			} );
	}

	private void initializeFields() {
		// Type
		Integer type = (Integer)getModuleProperties().getProperty( ModuleProperties.TYPE );
		String value = PropertyPageMessages.getString( "ModulePropertyPage.16" ); //$NON-NLS-1$
		if ( type.intValue() == ICModule.EXECUTABLE ) {
			value = PropertyPageMessages.getString( "ModulePropertyPage.1" ); //$NON-NLS-1$
		}
		if ( type.intValue() == ICModule.SHARED_LIBRARY ) {
			value = PropertyPageMessages.getString( "ModulePropertyPage.2" ); //$NON-NLS-1$
		}
		fTypeField.setText( value );

		// CPU
		String cpu = (String)getModuleProperties().getProperty( ModuleProperties.CPU );
		value = ( cpu != null ) ? cpu : PropertyPageMessages.getString( "ModulePropertyPage.5" ); //$NON-NLS-1$
		fCPUField.setText( value );
	
		// Base address
		IAddress address = (IAddress)getModuleProperties().getProperty( ModuleProperties.BASE_ADDRESS );
		value = ( address != null && !address.isZero() ) ? address.toHexAddressString() : PropertyPageMessages.getString( "ModulePropertyPage.7" ); //$NON-NLS-1$
		fBaseAddressField.setText( value );

		// Size
		Long size = (Long)getModuleProperties().getProperty( ModuleProperties.SIZE );
		value = ( size != null && size.longValue() > 0 ) ? size.toString() : PropertyPageMessages.getString( "ModulePropertyPage.9" ); //$NON-NLS-1$
		fSizeField.setText( value );

		// Symbols flag
		Boolean loaded = (Boolean)getModuleProperties().getProperty( ModuleProperties.SYMBOLS_LOADED );
		value = ( loaded != null && loaded.booleanValue() ) ? PropertyPageMessages.getString( "ModulePropertyPage.11" ) : PropertyPageMessages.getString( "ModulePropertyPage.12" ); //$NON-NLS-1$ //$NON-NLS-2$
		fSymbolsField.setText( value );
	
		// Symbols file:
		IPath path = (IPath)getModuleProperties().getProperty( ModuleProperties.SYMBOLS_FILE );
		value = ( path != null ) ? path.toOSString() : PropertyPageMessages.getString( "ModulePropertyPage.14" ); //$NON-NLS-1$
		fSymbolsFileField.setText( value );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		if ( getModuleProperties() != null ) {
			getModuleProperties().dispose();
		}
		super.dispose();
	}
}
