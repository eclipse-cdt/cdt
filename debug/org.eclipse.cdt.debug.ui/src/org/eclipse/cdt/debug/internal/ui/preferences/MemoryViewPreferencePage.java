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
package org.eclipse.cdt.debug.internal.ui.preferences;

import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The preference page of the Memory view.
 */
public class MemoryViewPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Constructor for MemoryViewPreferencePage.
	 */
	public MemoryViewPreferencePage() {
		super( GRID );
		setDescription( PreferenceMessages.getString( "MemoryViewPreferencePage.0" ) ); //$NON-NLS-1$
		setPreferenceStore( CDebugUIPlugin.getDefault().getPreferenceStore() );
	}

	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl( Composite parent ) {
		super.createControl( parent );
		WorkbenchHelp.setHelp( parent, ICDebugHelpContextIds.MEMORY_PREFERENCE_PAGE );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		/*
		 * String[][] sizes = { { "Byte", "1" }, { "Half Word", "2" }, { "Word", "4" }, { "Double Word", "8" }, // { "Float", "8" }, // { "Double Float", "16" }, };
		 * addField( new ComboFieldEditor( ICDebugPreferenceConstants.PREF_MEMORY_SIZE, "Size:", sizes, getFieldEditorParent() ) );
		 * 
		 * String[][] formats = { { "Hexadecimal", "0" }, { "Binary", "1" }, // { "Octal", "2" }, // { "Signed Decimal", "3" }, // { "Unsigned Decimal", "4" }, };
		 * addField( new ComboFieldEditor( ICDebugPreferenceConstants.PREF_MEMORY_FORMAT, "Format:", formats, getFieldEditorParent() ) );
		 * 
		 * String[][] bytesPerRow = { { "4", "4" }, { "8", "8" }, { "16", "16" }, { "32", "32" }, { "64", "64" }, { "128", "128" } }; addField( new
		 * ComboFieldEditor( ICDebugPreferenceConstants.PREF_MEMORY_BYTES_PER_ROW, "Bytes Per Row:", bytesPerRow, getFieldEditorParent() ) );
		 * 
		 * addField( new BooleanFieldEditor( ICDebugPreferenceConstants.PREF_MEMORY_DISPLAY_ASCII, "Display ASCII", getFieldEditorParent() ) );
		 */
		ColorFieldEditor foreground = new ColorFieldEditor( ICDebugPreferenceConstants.MEMORY_FOREGROUND_RGB, PreferenceMessages.getString( "MemoryViewPreferencePage.1" ), getFieldEditorParent() ); //$NON-NLS-1$
		ColorFieldEditor background = new ColorFieldEditor( ICDebugPreferenceConstants.MEMORY_BACKGROUND_RGB, PreferenceMessages.getString( "MemoryViewPreferencePage.2" ), getFieldEditorParent() ); //$NON-NLS-1$
		ColorFieldEditor address = new ColorFieldEditor( ICDebugPreferenceConstants.MEMORY_ADDRESS_RGB, PreferenceMessages.getString( "MemoryViewPreferencePage.3" ), getFieldEditorParent() ); //$NON-NLS-1$
		ColorFieldEditor changed = new ColorFieldEditor( ICDebugPreferenceConstants.MEMORY_CHANGED_RGB, PreferenceMessages.getString( "MemoryViewPreferencePage.4" ), getFieldEditorParent() ); //$NON-NLS-1$
//		ColorFieldEditor dirty = new ColorFieldEditor( ICDebugPreferenceConstants.MEMORY_DIRTY_RGB, "Modified Value Color:", getFieldEditorParent() );
		FontFieldEditor font = new FontFieldEditor( ICDebugPreferenceConstants.MEMORY_FONT, PreferenceMessages.getString( "MemoryViewPreferencePage.5" ), getFieldEditorParent() ); //$NON-NLS-1$
		addField( foreground );
		addField( background );
		addField( address );
		addField( changed );
		//		addField( dirty );
		addField( font );
		StringFieldEditor paddingChar = createPaddingCharacterField();
		paddingChar.setTextLimit( 1 );
		addField( paddingChar );
		createSpacer( getFieldEditorParent(), 1 );
		createDefaultSettingsFields();
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

	public static void initDefaults( IPreferenceStore store ) {
		store.setDefault( ICDebugPreferenceConstants.PREF_MEMORY_PADDING_CHAR, ICDebugPreferenceConstants.DEFAULT_MEMORY_PADDING_CHAR );
		store.setDefault( ICDebugPreferenceConstants.PREF_MEMORY_AUTO_REFRESH, true );
		store.setDefault( ICDebugPreferenceConstants.PREF_MEMORY_SHOW_ASCII, true );
		PreferenceConverter.setDefault( store, ICDebugPreferenceConstants.MEMORY_FONT, ICDebugPreferenceConstants.DEFAULT_MEMORY_FONT );
		PreferenceConverter.setDefault( store, ICDebugPreferenceConstants.MEMORY_FOREGROUND_RGB, ICDebugPreferenceConstants.DEFAULT_MEMORY_FOREGROUND_RGB );
		PreferenceConverter.setDefault( store, ICDebugPreferenceConstants.MEMORY_BACKGROUND_RGB, ICDebugPreferenceConstants.DEFAULT_MEMORY_BACKGROUND_RGB );
		PreferenceConverter.setDefault( store, ICDebugPreferenceConstants.MEMORY_ADDRESS_RGB, ICDebugPreferenceConstants.DEFAULT_MEMORY_ADDRESS_RGB );
		PreferenceConverter.setDefault( store, ICDebugPreferenceConstants.MEMORY_CHANGED_RGB, ICDebugPreferenceConstants.DEFAULT_MEMORY_CHANGED_RGB );
		PreferenceConverter.setDefault( store, ICDebugPreferenceConstants.MEMORY_DIRTY_RGB, ICDebugPreferenceConstants.DEFAULT_MEMORY_DIRTY_RGB );
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean ok = super.performOk();
		CDebugUIPlugin.getDefault().savePluginPreferences();
		return ok;
	}

	private StringFieldEditor createPaddingCharacterField() {
		return new StringFieldEditor( ICDebugPreferenceConstants.PREF_MEMORY_PADDING_CHAR, PreferenceMessages.getString( "MemoryViewPreferencePage.6" ), 1, getFieldEditorParent() ) //$NON-NLS-1$
		{

			protected boolean doCheckState() {
				return (getTextControl().getText().length() == 1);
			}
		};
	}

	private void createDefaultSettingsFields() {
		addField( new BooleanFieldEditor( ICDebugPreferenceConstants.PREF_MEMORY_AUTO_REFRESH, PreferenceMessages.getString( "MemoryViewPreferencePage.7" ), SWT.NONE, getFieldEditorParent() ) ); //$NON-NLS-1$
		addField( new BooleanFieldEditor( ICDebugPreferenceConstants.PREF_MEMORY_SHOW_ASCII, PreferenceMessages.getString( "MemoryViewPreferencePage.8" ), SWT.NONE, getFieldEditorParent() ) ); //$NON-NLS-1$
	}
}
