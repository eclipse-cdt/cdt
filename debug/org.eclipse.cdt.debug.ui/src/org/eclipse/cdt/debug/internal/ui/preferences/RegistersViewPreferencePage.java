/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.preferences;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
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
	private Button fAutoRefreshField = null;

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
		createSpacer( getFieldEditorParent(), 2 );
		fAutoRefreshField = ControlFactory.createCheckBox( getFieldEditorParent(), "Auto-Refresh by default" );
		fAutoRefreshField.setSelection( CDebugCorePlugin.getDefault().getPluginPreferences().getBoolean( ICDebugConstants.PREF_REGISTERS_AUTO_REFRESH ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init( IWorkbench workbench )
	{
	}

	public static void initDefaults( IPreferenceStore store )
	{
		PreferenceConverter.setDefault( store,
										ICDebugPreferenceConstants.CHANGED_REGISTER_RGB,
										new RGB( 255, 0, 0 ) );
		CDebugCorePlugin.getDefault().getPluginPreferences().setDefault( ICDebugConstants.PREF_REGISTERS_AUTO_REFRESH, true );
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
		storeValues();
		CDebugUIPlugin.getDefault().savePluginPreferences();
		CDebugCorePlugin.getDefault().savePluginPreferences();
		return ok;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults()
	{
		setDefaultValues();
		super.performDefaults();
	}

	private void setDefaultValues()
	{
		fAutoRefreshField.setSelection( CDebugCorePlugin.getDefault().getPluginPreferences().getDefaultBoolean( ICDebugConstants.PREF_REGISTERS_AUTO_REFRESH ) );
	}

	private void storeValues()
	{
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_REGISTERS_AUTO_REFRESH, fAutoRefreshField.getSelection() );
	}
}
