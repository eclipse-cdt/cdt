/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.internal.ui.preferences;

import com.ibm.icu.text.MessageFormat;
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

/**
 * Page for preferences that apply specifically to GDB MI.
 */
public class MIPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IPropertyChangeListener {

	/**
	 * This class exists to provide visibility to the
	 * <code>refreshValidState</code> method and to perform more intelligent
	 * clearing of the error message.
	 */
	protected class MIIntegerFieldEditor extends IntegerFieldEditor {						
		
		public MIIntegerFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
		}
		
		/**
		 * @see org.eclipse.jface.preference.FieldEditor#refreshValidState()
		 */
		@Override
		protected void refreshValidState() {
			super.refreshValidState();
		}
		
		/**
		 * Clears the error message from the message line if the error
		 * message is the error message from this field editor.
		 */
		@Override
		protected void clearErrorMessage() {
			if (canClearErrorMessage()) {
				super.clearErrorMessage();
			}
		}
	}
	public class MIPreferenceStore implements IPreferenceStore {
		
		private Preferences fPreferences;

		private HashMap fListeners = new HashMap();
		
		public MIPreferenceStore( Preferences pref ) {
			fPreferences = pref;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
		 */
		@Override
		public void addPropertyChangeListener( final IPropertyChangeListener listener ) {
			Preferences.IPropertyChangeListener l = new Preferences.IPropertyChangeListener() {
				
				@Override
				public void propertyChange( org.eclipse.core.runtime.Preferences.PropertyChangeEvent event ) {
					listener.propertyChange( new PropertyChangeEvent( MIPreferenceStore.this, event.getProperty(), event.getNewValue(), event.getOldValue() ) );
				}
			};
			fListeners.put( listener, l );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#contains(java.lang.String)
		 */
		@Override
		public boolean contains( String name ) {
			return getPreferences().contains( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#firePropertyChangeEvent(java.lang.String, java.lang.Object, java.lang.Object)
		 */
		@Override
		public void firePropertyChangeEvent( String name, Object oldValue, Object newValue ) {
			Iterator it = fListeners.keySet().iterator();
			while( it.hasNext() ) {
				((IPropertyChangeListener)it.next()).propertyChange( new PropertyChangeEvent( this, name, oldValue, newValue ) );
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getBoolean(java.lang.String)
		 */
		@Override
		public boolean getBoolean( String name ) {
			return fPreferences.getBoolean( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultBoolean(java.lang.String)
		 */
		@Override
		public boolean getDefaultBoolean( String name ) {
			return fPreferences.getDefaultBoolean( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultDouble(java.lang.String)
		 */
		@Override
		public double getDefaultDouble( String name ) {
			return fPreferences.getDefaultDouble( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultFloat(java.lang.String)
		 */
		@Override
		public float getDefaultFloat( String name ) {
			return fPreferences.getDefaultFloat( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultInt(java.lang.String)
		 */
		@Override
		public int getDefaultInt( String name ) {
			return fPreferences.getDefaultInt( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultLong(java.lang.String)
		 */
		@Override
		public long getDefaultLong( String name ) {
			return fPreferences.getDefaultLong( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultString(java.lang.String)
		 */
		@Override
		public String getDefaultString( String name ) {
			return fPreferences.getDefaultString( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getDouble(java.lang.String)
		 */
		@Override
		public double getDouble( String name ) {
			return fPreferences.getDouble( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getFloat(java.lang.String)
		 */
		@Override
		public float getFloat( String name ) {
			return fPreferences.getFloat( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getInt(java.lang.String)
		 */
		@Override
		public int getInt( String name ) {
			return fPreferences.getInt( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getLong(java.lang.String)
		 */
		@Override
		public long getLong( String name ) {
			return fPreferences.getLong( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#getString(java.lang.String)
		 */
		@Override
		public String getString( String name ) {
			return fPreferences.getString( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#isDefault(java.lang.String)
		 */
		@Override
		public boolean isDefault( String name ) {
			return fPreferences.isDefault( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#needsSaving()
		 */
		@Override
		public boolean needsSaving() {
			return getPreferences().needsSaving();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#putValue(java.lang.String, java.lang.String)
		 */
		@Override
		public void putValue( String name, String value ) {
			getPreferences().setValue( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
		 */
		@Override
		public void removePropertyChangeListener( IPropertyChangeListener listener ) {
			fListeners.remove( listener );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, double)
		 */
		@Override
		public void setDefault( String name, double value ) {
			getPreferences().setDefault( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, float)
		 */
		@Override
		public void setDefault( String name, float value ) {
			getPreferences().setDefault( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, int)
		 */
		@Override
		public void setDefault( String name, int value ) {
			getPreferences().setDefault( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, long)
		 */
		@Override
		public void setDefault( String name, long value ) {
			getPreferences().setDefault( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, java.lang.String)
		 */
		@Override
		public void setDefault( String name, String defaultObject ) {
			getPreferences().setDefault( name, defaultObject );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, boolean)
		 */
		@Override
		public void setDefault( String name, boolean value ) {
			getPreferences().setDefault( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setToDefault(java.lang.String)
		 */
		@Override
		public void setToDefault( String name ) {
			getPreferences().setToDefault( name );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, double)
		 */
		@Override
		public void setValue( String name, double value ) {
			getPreferences().setValue( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, float)
		 */
		@Override
		public void setValue( String name, float value ) {
			getPreferences().setValue( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, int)
		 */
		@Override
		public void setValue( String name, int value ) {
			getPreferences().setValue( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, long)
		 */
		@Override
		public void setValue( String name, long value ) {
			getPreferences().setValue( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, java.lang.String)
		 */
		@Override
		public void setValue( String name, String value ) {
			getPreferences().setValue( name, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, boolean)
		 */
		@Override
		public void setValue( String name, boolean value ) {
			getPreferences().setValue( name, value );
		}

		protected Preferences getPreferences() {
			return fPreferences;
		}
	}

	private IWorkbench fWorkbench;

	// Debugger timeout preference widgets
	private MIIntegerFieldEditor fDebugTimeoutText;

	// Launch timeout preference widgets
	private MIIntegerFieldEditor fLaunchTimeoutText;

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
	@Override
	protected Control createContents( Composite parent ) {
		getWorkbench().getHelpSystem().setHelp( getControl(), IMIHelpContextIds.MI_PREFERENCE_PAGE );
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
	@Override
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
	@Override
	protected void performDefaults() {
		setDefaultValues();
		super.performDefaults();
	}

	private void setDefaultValues() {
		fDebugTimeoutText.loadDefault();
		fLaunchTimeoutText.loadDefault();
		fRefreshSolibsButton.loadDefault();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
	 */
	@Override
	public void init( IWorkbench workbench ) {
		fWorkbench = workbench;
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
		fDebugTimeoutText.setPropertyChangeListener( this );
		fLaunchTimeoutText = createTimeoutField( IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT, PreferenceMessages.getString( "MIPreferencePage.3" ), spacingComposite ); //$NON-NLS-1$
		fLaunchTimeoutText.setPropertyChangeListener( this );
		fRefreshSolibsButton = createCheckbox( IMIConstants.PREF_SHARED_LIBRARIES_AUTO_REFRESH, PreferenceMessages.getString( "MIPreferencePage.6" ), spacingComposite ); //$NON-NLS-1$
	}

	/**
	 * Store the preference values based on the state of the component widgets
	 */
	private void storeValues() {
		fDebugTimeoutText.store();
		fLaunchTimeoutText.store();
		fRefreshSolibsButton.store();
	}

	private MIIntegerFieldEditor createTimeoutField( String preference, String label, Composite parent ) {
		MIIntegerFieldEditor toText = new MIIntegerFieldEditor( preference, label, parent );
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

	private BooleanFieldEditor createCheckbox( String preference, String label, Composite parent ) {
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
	@Override
	public void dispose() {
		fDebugTimeoutText.dispose();
		fLaunchTimeoutText.dispose();
		fRefreshSolibsButton.dispose();
		super.dispose();
	}

	protected MIIntegerFieldEditor getLaunchTimeoutText() {
		return fLaunchTimeoutText;
	}

	protected MIIntegerFieldEditor getDebugTimeoutText() {
		return fDebugTimeoutText;
	}

	/**
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {

		if (event.getProperty().equals(FieldEditor.IS_VALID)) {
			boolean newValue = ((Boolean) event.getNewValue()).booleanValue();
			// If the new value is true then we must check all field editors.
			// If it is false, then the page is invalid in any case.
			MIIntegerFieldEditor launchTimeout = getLaunchTimeoutText();
			MIIntegerFieldEditor debugTimeout = getDebugTimeoutText();
			if (newValue) {
				if (launchTimeout != null && event.getSource() != launchTimeout) {
					launchTimeout.refreshValidState();
				} 
				if (debugTimeout != null && event.getSource() != debugTimeout) {
					debugTimeout.refreshValidState();
				}
			} 
			setValid(launchTimeout.isValid() && debugTimeout.isValid());
			getContainer().updateButtons();
			updateApplyButton();
		}
	}

	protected boolean canClearErrorMessage() {
		MIIntegerFieldEditor launchTimeout = getLaunchTimeoutText();
		MIIntegerFieldEditor debugTimeout = getDebugTimeoutText();
		boolean validLaunch = false;
		boolean validDebug = false;
		if (launchTimeout != null) {
			validLaunch = launchTimeout.isValid();
		}
		if (debugTimeout != null) {
			validDebug = debugTimeout.isValid();
		}
		return validLaunch && validDebug;
	}

	private IWorkbench getWorkbench() {
		return fWorkbench;
	}
	
}
