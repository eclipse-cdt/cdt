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
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Enter type comment.
 * 
 * @since: Feb 7, 2003
 */
public class SharedLibrariesViewPreferencePage extends FieldEditorPreferencePage
											   implements IWorkbenchPreferencePage
{
	private Button fAutoRefreshField = null;

	/**
	 * Constructor for SharedLibrariesViewPreferencePage.
	 * @param style
	 */
	public SharedLibrariesViewPreferencePage()
	{
		super( GRID );
		setDescription( "Shared Libraries View Settings." );
		setPreferenceStore( CDebugUIPlugin.getDefault().getPreferenceStore() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors()
	{
		fAutoRefreshField = ControlFactory.createCheckBox( getFieldEditorParent(), "Auto-Refresh by default" );
		fAutoRefreshField.setSelection( CDebugCorePlugin.getDefault().getPluginPreferences().getBoolean( ICDebugConstants.PREF_SHARED_LIBRARIES_AUTO_REFRESH ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init( IWorkbench workbench )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent)
	{
		super.createControl( parent );
		WorkbenchHelp.setHelp( parent, ICDebugHelpContextIds.SHARED_LIBRARIES_PREFERENCE_PAGE );
	}

	public static void initDefaults( IPreferenceStore store )
	{
		CDebugCorePlugin.getDefault().getPluginPreferences().setDefault( ICDebugConstants.PREF_SHARED_LIBRARIES_AUTO_REFRESH, false );
	}

	protected void createSpacer( Composite composite, int columnSpan )
	{
		Label label = new Label( composite, SWT.NONE );
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData( gd );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
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
		fAutoRefreshField.setSelection( CDebugCorePlugin.getDefault().getPluginPreferences().getDefaultBoolean( ICDebugConstants.PREF_SHARED_LIBRARIES_AUTO_REFRESH ) );
	}

	private void storeValues()
	{
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_SHARED_LIBRARIES_AUTO_REFRESH, fAutoRefreshField.getSelection() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#adjustGridLayout()
	 */
	protected void adjustGridLayout()
	{
		super.adjustGridLayout();
		// If there are no editor fields on this page set the number of columns to prevent stack overflow 
		if ( ((GridLayout)getFieldEditorParent().getLayout()).numColumns == 0 )
			((GridLayout)getFieldEditorParent().getLayout()).numColumns = 2;
	}
}
