/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.preferences;

import org.eclipse.cdt.debug.internal.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.WorkbenchChainedTextFontFieldEditor;

/**
 * 
 * Enter type comment.
 * 
 * @since Jul 25, 2002
 */
public class MemoryViewPreferencePage extends FieldEditorPreferencePage
									  implements IWorkbenchPreferencePage
{

	/**
	 * Constructor for MemoryViewPreferencePage.
	 * @param style
	 */
	public MemoryViewPreferencePage()
	{
		super( GRID );
		setDescription( "Debug Memory View Settings." );
		setPreferenceStore( CDebugUIPlugin.getDefault().getPreferenceStore() );
	}

	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl( Composite parent ) 
	{
		super.createControl( parent );
		WorkbenchHelp.setHelp( parent, ICDebugHelpContextIds.MEMORY_PREFERENCE_PAGE );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors()
	{
/*
		String[][] sizes = { { "Byte", "1" }, 
							 { "Half Word", "2" },
							 { "Word", "4" },
							 { "Double Word", "8" },
//							 { "Float", "8" },
//							 { "Double Float", "16" }, 
						   };
		addField( new ComboFieldEditor( ICDebugPreferenceConstants.PREF_MEMORY_SIZE, "Size:", sizes, getFieldEditorParent() ) );

		String[][] formats = { { "Hexadecimal", "0" }, 
							   { "Binary", "1" },
//							   { "Octal", "2" },
//							   { "Signed Decimal", "3" },
//							   { "Unsigned Decimal", "4" }, 
							 };
		addField( new ComboFieldEditor( ICDebugPreferenceConstants.PREF_MEMORY_FORMAT, "Format:", formats, getFieldEditorParent() ) );

		String[][] bytesPerRow = { { "4", "4" }, { "8", "8" },
								   { "16", "16" }, { "32", "32" },
								   { "64", "64" }, { "128", "128" } };
		addField( new ComboFieldEditor( ICDebugPreferenceConstants.PREF_MEMORY_BYTES_PER_ROW, "Bytes Per Row:", bytesPerRow, getFieldEditorParent() ) );

		addField( new BooleanFieldEditor( ICDebugPreferenceConstants.PREF_MEMORY_DISPLAY_ASCII, "Display ASCII", getFieldEditorParent() ) );
*/
		ColorFieldEditor foreground = new ColorFieldEditor( ICDebugPreferenceConstants.MEMORY_FOREGROUND_RGB, "Text Color:", getFieldEditorParent() );
		ColorFieldEditor background = new ColorFieldEditor( ICDebugPreferenceConstants.MEMORY_BACKGROUND_RGB, "Background Color:", getFieldEditorParent() );
		ColorFieldEditor address = new ColorFieldEditor( ICDebugPreferenceConstants.MEMORY_ADDRESS_RGB, "Address Color:", getFieldEditorParent() );
		ColorFieldEditor changed = new ColorFieldEditor( ICDebugPreferenceConstants.MEMORY_CHANGED_RGB, "Changed Value Color:", getFieldEditorParent() );
		ColorFieldEditor dirty = new ColorFieldEditor( ICDebugPreferenceConstants.MEMORY_DIRTY_RGB, "Modified Value Color:", getFieldEditorParent() );

		FontFieldEditor font = new FontFieldEditor( ICDebugPreferenceConstants.MEMORY_FONT, "Font:", getFieldEditorParent() );

		addField( foreground );
		addField( background );
		addField( address );
		addField( changed );
		addField( dirty );
		addField( font );

		StringFieldEditor paddingChar = new StringFieldEditor( ICDebugPreferenceConstants.PREF_MEMORY_PADDING_CHAR, "Padding Character:", 1, getFieldEditorParent() );
		paddingChar.setTextLimit( 1 );
		addField( paddingChar );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init( IWorkbench workbench )
	{
	}

	protected void createSpacer( Composite composite, int columnSpan )
	{
		Label label = new Label( composite, SWT.NONE );
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData( gd );
	}

	public static void initDefaults( IPreferenceStore store ) 
	{
		store.setDefault( ICDebugPreferenceConstants.PREF_MEMORY_PADDING_CHAR, ICDebugPreferenceConstants.DEFAULT_MEMORY_PADDING_CHAR );	
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
	public boolean performOk()
	{
		boolean ok = super.performOk();
		CDebugUIPlugin.getDefault().savePluginPreferences();
		return ok;
	}
}
