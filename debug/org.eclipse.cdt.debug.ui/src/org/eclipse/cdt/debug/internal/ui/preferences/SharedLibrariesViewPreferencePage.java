/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.preferences;

import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
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
		addField( new BooleanFieldEditor( ICDebugPreferenceConstants.PREF_SHARED_LIBRARIES_AUTO_REFRESH, "Auto-Refresh by default", getFieldEditorParent() ) );
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
		store.setDefault( ICDebugPreferenceConstants.PREF_SHARED_LIBRARIES_AUTO_REFRESH, true );
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
		CDebugUIPlugin.getDefault().savePluginPreferences();
		return ok;
	}
}
