/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.internal.ui.preferences;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import org.eclipse.cdt.debug.mi.core.IMIConstants;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.internal.ui.IMIHelpContextIds;
import org.eclipse.cdt.debug.mi.internal.ui.MIUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Page for preferences that apply specifically to GDB MI.
 */
public class MIPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public class MIPreferenceStore implements IPreferenceStore {
		
		private Preferences fPreferences;

		private HashMap fListeners = new HashMap();
		
		public MIPreferenceStore( Preferences pref ) {
			fPreferences = pref;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
		 */
		public void addPropertyChangeListener( final IPropertyChangeListener listener ) {
			Preferences.IPropertyChangeListener l = new Preferences.IPropertyChangeListener() {
				
				public void propertyChange( org.eclipse.core.runtime.Preferences.PropertyChangeEvent event ) {
					listener.propertyChange( new PropertyChangeEvent( MIPreferenceStore.this, event.getProperty(), event.getNewValue(), event.getOldValue() ) );
				}
			};
			fListeners.put( listener, l );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#contains(java.lang.String)
		 */
		public boolean contains( String name ) {
			return getPreferences().contains( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#firePropertyChangeEvent(java.lang.String, java.lang.Object, java.lang.Object)
		 */
		public void firePropertyChangeEvent( String name, Object oldValue, Object newValue ) {
			Iterator it = fListeners.keySet().iterator();
			while( it.hasNext() ) {
				((IPropertyChangeListener)it.next()).propertyChange( new PropertyChangeEvent( this, name, oldValue, newValue ) );
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getBoolean(java.lang.String)
		 */
		public boolean getBoolean( String name ) {
			return fPreferences.getBoolean( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultBoolean(java.lang.String)
		 */
		public boolean getDefaultBoolean( String name ) {
			return fPreferences.getDefaultBoolean( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultDouble(java.lang.String)
		 */
		public double getDefaultDouble( String name ) {
			return fPreferences.getDefaultDouble( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultFloat(java.lang.String)
		 */
		public float getDefaultFloat( String name ) {
			return fPreferences.getDefaultFloat( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultInt(java.lang.String)
		 */
		public int getDefaultInt( String name ) {
			return fPreferences.getDefaultInt( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultLong(java.lang.String)
		 */
		public long getDefaultLong( String name ) {
			return fPreferences.getDefaultLong( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultString(java.lang.String)
		 */
		public String getDefaultString( String name ) {
			return fPreferences.getDefaultString( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getDouble(java.lang.String)
		 */
		public double getDouble( String name ) {
			return fPreferences.getDouble( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getFloat(java.lang.String)
		 */
		public float getFloat( String name ) {
			return fPreferences.getFloat( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getInt(java.lang.String)
		 */
		public int getInt( String name ) {
			return fPreferences.getInt( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getLong(java.lang.String)
		 */
		public long getLong( String name ) {
			return fPreferences.getLong( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getString(java.lang.String)
		 */
		public String getString( String name ) {
			return fPreferences.getString( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#isDefault(java.lang.String)
		 */
		public boolean isDefault( String name ) {
			return fPreferences.isDefault( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#needsSaving()
		 */
		public boolean needsSaving() {
			return getPreferences().needsSaving();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#putValue(java.lang.String, java.lang.String)
		 */
		public void putValue( String name, String value ) {
			getPreferences().setValue( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
		 */
		public void removePropertyChangeListener( IPropertyChangeListener listener ) {
			fListeners.remove( listener );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, double)
		 */
		public void setDefault( String name, double value ) {
			getPreferences().setDefault( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, float)
		 */
		public void setDefault( String name, float value ) {
			getPreferences().setDefault( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, int)
		 */
		public void setDefault( String name, int value ) {
			getPreferences().setDefault( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, long)
		 */
		public void setDefault( String name, long value ) {
			getPreferences().setDefault( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, java.lang.String)
		 */
		public void setDefault( String name, String defaultObject ) {
			getPreferences().setDefault( name, defaultObject );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, boolean)
		 */
		public void setDefault( String name, boolean value ) {
			getPreferences().setDefault( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setToDefault(java.lang.String)
		 */
		public void setToDefault( String name ) {
			getPreferences().setToDefault( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, double)
		 */
		public void setValue( String name, double value ) {
			getPreferences().setValue( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, float)
		 */
		public void setValue( String name, float value ) {
			getPreferences().setValue( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, int)
		 */
		public void setValue( String name, int value ) {
			getPreferences().setValue( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, long)
		 */
		public void setValue( String name, long value ) {
			getPreferences().setValue( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, java.lang.String)
		 */
		public void setValue( String name, String value ) {
			getPreferences().setValue( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, boolean)
		 */
		public void setValue( String name, boolean value ) {
			getPreferences().setValue( name, value );
		}

		protected Preferences getPreferences() {
			return fPreferences;
		}
	}

	private final static String GDB_MI_HELP_CONTEXT = MIUIPlugin.PLUGIN_ID + "mi_preference_page_context"; //$NON-NLS-1$

	// Debugger timeout preference widgets
	private IntegerFieldEditor fDebugTimeoutText;

	// Launch timeout preference widgets
	private IntegerFieldEditor fLaunchTimeoutText;

	private BooleanFieldEditor fRefreshRegistersButton;

	private BooleanFieldEditor fRefreshSolibsButton;

	private MIPreferenceStore fMICorePreferenceStore = new MIPreferenceStore( MIPlugin.getDefault().getPluginPreferences() );

	/**
	 * Constructor for MIPreferencePage.
	 */
	public MIPreferencePage() {
		super();
		setPreferenceStore( MIUIPlugin.getDefault().getPreferenceStore() );
		setDescription( PreferenceMessages.getString( "MIPreferencePage.0" ) ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContents( Composite parent ) {
		WorkbenchHelp.setHelp( getControl(), IMIHelpContextIds.MI_PREFERENCE_PAGE );
		//The main composite
		Composite composite = new Composite( parent, SWT.NULL );
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout( layout );
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData( data );
		createSpacer( composite, 1 );
		createCommunicationPreferences( composite );
		WorkbenchHelp.setHelp( composite, GDB_MI_HELP_CONTEXT );
		return composite;
	}

	/**
	 * Creates composite group and sets the default layout data.
	 * 
	 * @param parent the parent of the new composite
	 * @param numColumns the number of columns for the new composite
	 * @param labelText the text label of the new composite
	 * @return the newly-created composite
	 */
	private Composite createGroupComposite( Composite parent, int numColumns, String labelText ) {
		return ControlFactory.createGroup( parent, labelText, numColumns );
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean result = super.performOk();
		storeValues();
		MIUIPlugin.getDefault().savePluginPreferences();
		MIPlugin.getDefault().savePluginPreferences();
		return result;
	}

	/**
	 * Sets the default preferences.
	 * 
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		setDefaultValues();
		super.performDefaults();
	}

	private void setDefaultValues() {
		fDebugTimeoutText.loadDefault();
		fLaunchTimeoutText.loadDefault();
		fRefreshRegistersButton.loadDefault();
		fRefreshSolibsButton.loadDefault();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init( IWorkbench workbench ) {
	}

	protected void createSpacer( Composite composite, int columnSpan ) {
		Label label = new Label( composite, SWT.NONE );
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData( gd );
	}

	private void createCommunicationPreferences( Composite composite ) {
		Composite comp = createGroupComposite( composite, 1, PreferenceMessages.getString( "MIPreferencePage.1" ) ); //$NON-NLS-1$
		//Add in an intermediate composite to allow for spacing
		Composite spacingComposite = new Composite( comp, SWT.NONE );
		GridLayout layout = new GridLayout();
		spacingComposite.setLayout( layout );
		GridData data = new GridData();
		data.horizontalSpan = 2;
		spacingComposite.setLayoutData( data );
		fDebugTimeoutText = createTimeoutField( IMIConstants.PREF_REQUEST_TIMEOUT, PreferenceMessages.getString( "MIPreferencePage.2" ), spacingComposite ); //$NON-NLS-1$
		fDebugTimeoutText.setPropertyChangeListener( new IPropertyChangeListener() {

			public void propertyChange( PropertyChangeEvent event ) {
				if ( event.getProperty().equals( FieldEditor.IS_VALID ) )
					setValid( getDebugTimeoutText().isValid() );
			}
		} );
		fLaunchTimeoutText = createTimeoutField( IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT, PreferenceMessages.getString( "MIPreferencePage.3" ), spacingComposite ); //$NON-NLS-1$
		fLaunchTimeoutText.setPropertyChangeListener( new IPropertyChangeListener() {

			public void propertyChange( PropertyChangeEvent event ) {
				if ( event.getProperty().equals( FieldEditor.IS_VALID ) )
					setValid( getLaunchTimeoutText().isValid() );
			}
		} );
		fRefreshRegistersButton = createRefreshField( IMIConstants.PREF_REGISTERS_AUTO_REFRESH, PreferenceMessages.getString( "MIPreferencePage.5" ), spacingComposite ); //$NON-NLS-1$
		fRefreshSolibsButton = createRefreshField( IMIConstants.PREF_SHARED_LIBRARIES_AUTO_REFRESH, PreferenceMessages.getString( "MIPreferencePage.6" ), spacingComposite ); //$NON-NLS-1$
	}

	/**
	 * Store the preference values based on the state of the component widgets
	 */
	private void storeValues() {
		fDebugTimeoutText.store();
		fLaunchTimeoutText.store();
		fRefreshRegistersButton.store();
		fRefreshSolibsButton.store();
	}

	private IntegerFieldEditor createTimeoutField( String preference, String label, Composite parent ) {
		IntegerFieldEditor toText = new IntegerFieldEditor( preference, label, parent );
		GridData data = new GridData();
		data.widthHint = convertWidthInCharsToPixels( 10 );
		toText.getTextControl( parent ).setLayoutData( data );
		toText.setPreferenceStore( getMICorePreferenceStore() );
		toText.setPage( this );
		toText.setValidateStrategy( StringFieldEditor.VALIDATE_ON_KEY_STROKE );
		toText.setValidRange( IMIConstants.MIN_REQUEST_TIMEOUT, IMIConstants.MAX_REQUEST_TIMEOUT );
		String minValue = Integer.toString( IMIConstants.MIN_REQUEST_TIMEOUT );
		String maxValue = Integer.toString( IMIConstants.MAX_REQUEST_TIMEOUT );
		toText.setErrorMessage( MessageFormat.format( PreferenceMessages.getString( "MIPreferencePage.4" ), new String[]{ minValue, maxValue } ) ); //$NON-NLS-1$
		toText.load();
		return toText;
	}

	private BooleanFieldEditor createRefreshField( String preference, String label, Composite parent ) {
		BooleanFieldEditor field = new BooleanFieldEditor( preference, label, parent );
		field.setPage( this );
		field.setPreferenceStore( new MIPreferenceStore( MIPlugin.getDefault().getPluginPreferences() ) );
		field.load();
		return field;
	}

	protected MIPreferenceStore getMICorePreferenceStore() {
		return fMICorePreferenceStore;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		fDebugTimeoutText.dispose();
		fLaunchTimeoutText.dispose();
		fRefreshRegistersButton.dispose();
		fRefreshSolibsButton.dispose();
		super.dispose();
	}

	protected IntegerFieldEditor getLaunchTimeoutText() {
		return fLaunchTimeoutText;
	}

	protected IntegerFieldEditor getDebugTimeoutText() {
		return fDebugTimeoutText;
	}
}