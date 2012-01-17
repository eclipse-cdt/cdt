/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ken Ryall (Nokia) - 207675
*******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.preferences;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.ICDIFormat;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Preference page for debug preferences that apply specifically to C/C++ Debugging.
 */
public class CDebugPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private IWorkbench fWorkbench;

	private Combo fVariableFormatCombo;

	private Combo fExpressionFormatCombo;

	private Combo fRegisterFormatCombo;

	private Combo fCharsetCombo;
	
	// Format constants
	private static int[] fFormatIds = new int[]{ ICDIFormat.NATURAL, ICDIFormat.HEXADECIMAL, ICDIFormat.DECIMAL, ICDIFormat.BINARY };

	private static String[] fFormatLabels = new String[]{ PreferenceMessages.getString( "CDebugPreferencePage.0" ), PreferenceMessages.getString( "CDebugPreferencePage.1" ), PreferenceMessages.getString( "CDebugPreferencePage.2" ), PreferenceMessages.getString( "CDebugPreferencePage.14" ) }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private PropertyChangeListener fPropertyChangeListener;

	private Button fShowBinarySourceFilesButton;

	protected class PropertyChangeListener implements IPropertyChangeListener {

		private boolean fHasStateChanged = false;

		@Override
		public void propertyChange( PropertyChangeEvent event ) {
			if ( event.getProperty().equals( ICDebugPreferenceConstants.PREF_SHOW_HEX_VALUES ) ) {
				fHasStateChanged = true;
			}
			else if ( event.getProperty().equals( ICDebugPreferenceConstants.PREF_SHOW_CHAR_VALUES ) ) {
				fHasStateChanged = true;
			}
		}

		protected boolean hasStateChanged() {
			return fHasStateChanged;
		}
	}

	/**
	 * Constructor for CDebugPreferencePage.
	 */
	public CDebugPreferencePage() {
		super();
		setPreferenceStore( CDebugUIPlugin.getDefault().getPreferenceStore() );
		getPreferenceStore().addPropertyChangeListener( getPropertyChangeListener() );
		setDescription( PreferenceMessages.getString( "CDebugPreferencePage.3" ) ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents( Composite parent ) {
		getWorkbench().getHelpSystem().setHelp( getControl(), ICDebugHelpContextIds.C_DEBUG_PREFERENCE_PAGE );
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
		createViewSettingPreferences( composite );
		createSpacer( composite, 1 );
		createBinarySettings( composite );
		setValues();
		return composite;
	}

	/**
	 * Creates composite group and sets the default layout data.
	 * 
	 * @param parent
	 *            the parent of the new composite
	 * @param numColumns
	 *            the number of columns for the new composite
	 * @param labelText
	 *            the text label of the new composite
	 * @return the newly-created composite
	 */
	private Composite createGroupComposite( Composite parent, int numColumns, String labelText ) {
		return ControlFactory.createGroup( parent, labelText, numColumns );
	}

	/**
	 * Set the values of the component widgets based on the values in the preference store
	 */
	private void setValues() {
		fVariableFormatCombo.select( getFormatIndex( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT ) ) );
		fExpressionFormatCombo.select( getFormatIndex( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT ) ) );
		fRegisterFormatCombo.select( getFormatIndex( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT ) ) );
		fCharsetCombo.setText( CDebugCorePlugin.getDefault().getPluginPreferences().getString( ICDebugConstants.PREF_CHARSET ) );
		fShowBinarySourceFilesButton.setSelection( CCorePlugin.getDefault().getPluginPreferences().getBoolean( CCorePreferenceConstants.SHOW_SOURCE_FILES_IN_BINARIES ) );
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

	protected PropertyChangeListener getPropertyChangeListener() {
		if ( fPropertyChangeListener == null ) {
			fPropertyChangeListener = new PropertyChangeListener();
		}
		return fPropertyChangeListener;
	}

	/**
	 * Set the default preferences for this page.
	 */
	public static void initDefaults( IPreferenceStore store ) {
		store.setDefault( ICDebugPreferenceConstants.PREF_SHOW_HEX_VALUES, false );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		getPreferenceStore().removePropertyChangeListener( getPropertyChangeListener() );
	}

	/**
	 * Create the view setting preferences composite widget
	 */
	private void createViewSettingPreferences( Composite parent ) {
		Composite comp = createGroupComposite( parent, 1, PreferenceMessages.getString( "CDebugPreferencePage.4" ) ); //$NON-NLS-1$
		Composite formatComposite = ControlFactory.createCompositeEx( comp, 2, 0 );
		((GridLayout)formatComposite.getLayout()).makeColumnsEqualWidth = true;
		fVariableFormatCombo = createComboBox( formatComposite, PreferenceMessages.getString( "CDebugPreferencePage.8" ), fFormatLabels, fFormatLabels[0] ); //$NON-NLS-1$
		fExpressionFormatCombo = createComboBox( formatComposite, PreferenceMessages.getString( "CDebugPreferencePage.9" ), fFormatLabels, fFormatLabels[0] ); //$NON-NLS-1$
		fRegisterFormatCombo = createComboBox( formatComposite, PreferenceMessages.getString( "CDebugPreferencePage.10" ), fFormatLabels, fFormatLabels[0] ); //$NON-NLS-1$
		String[] charsetNames = getCharsetNames();
		fCharsetCombo = createComboBox( formatComposite, PreferenceMessages.getString( "CDebugPreferencePage.16" ), charsetNames, charsetNames[0] ); //$NON-NLS-1$
	}

	private String[] getCharsetNames() {
		ArrayList names = new ArrayList();
		SortedMap setmap = Charset.availableCharsets();
		
		for (Iterator iterator = setmap.keySet().iterator(); iterator.hasNext();) {
			String entry = (String) iterator.next();
			names.add(entry);
		}
		return (String[]) names.toArray(new String[names.size()]);
	}

	private void createBinarySettings( Composite parent ) {
		fShowBinarySourceFilesButton = createCheckButton( parent, PreferenceMessages.getString("CDebugPreferencePage.15") );		 //$NON-NLS-1$
	}

	/**
	 * Creates a button with the given label and sets the default configuration data.
	 */
	private Button createCheckButton( Composite parent, String label ) {
		Button button = new Button( parent, SWT.CHECK | SWT.LEFT );
		button.setText( label );
		// FieldEditor GridData
		GridData data = new GridData();
		button.setLayoutData( data );
		return button;
	}

	/**
	 * Creates a button with the given label and sets the default configuration data.
	 */
	private Combo createComboBox( Composite parent, String label, String[] items, String selection ) {
		ControlFactory.createLabel( parent, label );
		Combo combo = ControlFactory.createSelectCombo( parent, items, selection );
		combo.setLayoutData( new GridData() );
		return combo;
	}

	protected void createSpacer( Composite composite, int columnSpan ) {
		Label label = new Label( composite, SWT.NONE );
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData( gd );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		storeValues();
		if ( getPropertyChangeListener().hasStateChanged() ) {
			refreshViews();
		}
		CDebugUIPlugin.getDefault().savePluginPreferences();
		CDebugCorePlugin.getDefault().savePluginPreferences();
		return true;
	}

	/**
	 * Refresh the variables and expression views as changes have occurred that affects these views.
	 */
	private void refreshViews() {
		BusyIndicator.showWhile( getShell().getDisplay(), new Runnable() {

			@Override
			public void run() {
				// Refresh interested views
				IWorkbenchWindow[] windows = CDebugUIPlugin.getDefault().getWorkbench().getWorkbenchWindows();
				IWorkbenchPage page = null;
				for( int i = 0; i < windows.length; i++ ) {
					page = windows[i].getActivePage();
					if ( page != null ) {
						refreshViews( page, IDebugUIConstants.ID_EXPRESSION_VIEW );
						refreshViews( page, IDebugUIConstants.ID_VARIABLE_VIEW );
						refreshViews( page, IDebugUIConstants.ID_REGISTER_VIEW );
					}
				}
			}
		} );
	}

	/**
	 * Refresh all views in the given workbench page with the given view id
	 */
	protected void refreshViews( IWorkbenchPage page, String viewID ) {
		IViewPart part = page.findView( viewID );
		if ( part != null ) {
			IDebugView adapter = (IDebugView)part.getAdapter( IDebugView.class );
			if ( adapter != null ) {
				Viewer viewer = adapter.getViewer();
				if ( viewer instanceof StructuredViewer ) {
					((StructuredViewer)viewer).refresh();
				}
			}
		}
	}

	/**
	 * Store the preference values based on the state of the component widgets
	 */
	private void storeValues() {
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT, getFormatId( fVariableFormatCombo.getSelectionIndex() ) );
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT, getFormatId( fExpressionFormatCombo.getSelectionIndex() ) );
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT, getFormatId( fRegisterFormatCombo.getSelectionIndex() ) );
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_CHARSET, fCharsetCombo.getItem( fCharsetCombo.getSelectionIndex()) );
		CCorePlugin.getDefault().getPluginPreferences().setValue( CCorePreferenceConstants.SHOW_SOURCE_FILES_IN_BINARIES, fShowBinarySourceFilesButton.getSelection() );
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
		fVariableFormatCombo.select( getFormatIndex( CDebugCorePlugin.getDefault().getPluginPreferences().getDefaultInt( ICDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT ) ) );
		fExpressionFormatCombo.select( getFormatIndex( CDebugCorePlugin.getDefault().getPluginPreferences().getDefaultInt( ICDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT ) ) );
		fRegisterFormatCombo.select( getFormatIndex( CDebugCorePlugin.getDefault().getPluginPreferences().getDefaultInt( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT ) ) );
		fCharsetCombo.setText(  CDebugCorePlugin.getDefault().getPluginPreferences().getDefaultString( ICDebugConstants.PREF_CHARSET )  );
	}

	private static int getFormatId( int index ) {
		return (index >= 0 && index < fFormatIds.length) ? fFormatIds[index] : fFormatIds[0];
	}

	private static int getFormatIndex( int id ) {
		for( int i = 0; i < fFormatIds.length; ++i )
			if ( fFormatIds[i] == id )
				return i;
		return -1;
	}

	private IWorkbench getWorkbench() {
		return fWorkbench;
	}
}
