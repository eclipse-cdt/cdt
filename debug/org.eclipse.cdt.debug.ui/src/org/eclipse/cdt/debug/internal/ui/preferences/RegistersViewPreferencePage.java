/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.preferences;

import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * 
 * A page to set the preferences for the registers view
 * 
 * @since Sep 16, 2002
 */
public class RegistersViewPreferencePage extends FieldEditorPreferencePage
										 implements IWorkbenchPreferencePage
{
	/**
	 * Constructor for RegistersViewPreferencePage.
	 * @param style
	 */
	public RegistersViewPreferencePage()
	{
		super( GRID );
		setDescription( "Registers View Settings." );
		setPreferenceStore( CDebugUIPlugin.getDefault().getPreferenceStore() );
	}

	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl( Composite parent )
	{
		super.createControl( parent );
		WorkbenchHelp.setHelp( parent, ICDebugHelpContextIds.REGISTERS_PREFERENCE_PAGE );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors()
	{
		addField( new ColorFieldEditor( ICDebugPreferenceConstants.CHANGED_REGISTER_RGB, "&Changed register value color:", getFieldEditorParent() ) );		
		createSpacer( getFieldEditorParent(), 1 );
		addField( new BooleanFieldEditor( IDebugUIConstants.PREF_SHOW_TYPE_NAMES, "Show type &names by default", SWT.NONE, getFieldEditorParent() ) );
		createSpacer( getFieldEditorParent(), 1 );
		addField( new BooleanFieldEditor( ICDebugPreferenceConstants.PREF_REGISTERS_AUTO_REFRESH, "Auto-Refresh by default", getFieldEditorParent() ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init( IWorkbench workbench )
	{
	}

	public static void initDefaults( IPreferenceStore store )
	{
		store.setDefault( IDebugUIConstants.PREF_SHOW_TYPE_NAMES, false );
		PreferenceConverter.setDefault( store,
										ICDebugPreferenceConstants.CHANGED_REGISTER_RGB,
										new RGB( 255, 0, 0 ) );
		store.setDefault( ICDebugPreferenceConstants.PREF_REGISTERS_AUTO_REFRESH, true );
	}

	protected void createSpacer( Composite composite, int columnSpan )
	{
		Label label = new Label( composite, SWT.NONE );
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData( gd );
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk()
	{
		boolean ok = super.performOk();
		CDebugUIPlugin.getDefault().savePluginPreferences();
		return ok;
	}
}
