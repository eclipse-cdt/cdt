/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.preferences;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.ICDIFormat;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * 
 * Preference page for debug preferences that apply specifically to
 * C/C++ Debugging.
 * 
 * @since Oct 3, 2002
 */
public class CDebugPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
	// View setting widgets
	private Button fPathsButton;
/*
	private Combo fVariableFormatCombo;
	private Combo fExpressionFormatCombo;
	private Combo fRegisterFormatCombo;
*/
	// Disassembly setting widgets
	private Button fAutoDisassemblyButton;

	// Format constants
//	private static int[] fFormatIds = new int[]{ ICDIFormat.NATURAL, ICDIFormat.HEXADECIMAL, ICDIFormat.DECIMAL };
//	private static String[] fFormatLabels = new String[] { "Natural", "Hexadecimal", "Decimal" };

	private PropertyChangeListener fPropertyChangeListener;

	protected class PropertyChangeListener implements IPropertyChangeListener
	{
		private boolean fHasStateChanged = false;

		public void propertyChange( PropertyChangeEvent event )
		{
			if ( event.getProperty().equals( ICDebugPreferenceConstants.PREF_SHOW_HEX_VALUES ) )
			{
				fHasStateChanged = true;
			}
			else if ( event.getProperty().equals( ICDebugPreferenceConstants.PREF_SHOW_CHAR_VALUES ) )
			{
				fHasStateChanged = true;
			}
		}

		protected boolean hasStateChanged()
		{
			return fHasStateChanged;
		}
	}

	/**
	 * Constructor for CDebugPreferencePage.
	 */
	public CDebugPreferencePage()
	{
		super();
		setPreferenceStore( CDebugUIPlugin.getDefault().getPreferenceStore() );
		getPreferenceStore().addPropertyChangeListener( getPropertyChangeListener() );
		setDescription( "General settings for C/C++ Debugging." );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContents( Composite parent )
	{
		WorkbenchHelp.setHelp( getControl(), ICDebugHelpContextIds.C_DEBUG_PREFERENCE_PAGE );

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
		createDisassemblySettingPreferences( composite );
		setValues();

		return composite;
	}

	/**
	 * Creates composite group and sets the default layout data.
	 *
	 * @param parent  the parent of the new composite
	 * @param numColumns  the number of columns for the new composite
	 * @param labelText  the text label of the new composite
	 * @return the newly-created composite
	 */
	private Composite createGroupComposite( Composite parent, int numColumns, String labelText )
	{
		return ControlFactory.createGroup( parent, labelText, numColumns );
	}
		
	/**
	 * Set the values of the component widgets based on the
	 * values in the preference store
	 */
	private void setValues()
	{
		IPreferenceStore store = getPreferenceStore();

		fPathsButton.setSelection( store.getBoolean( ICDebugPreferenceConstants.PREF_SHOW_FULL_PATHS ) );
		fAutoDisassemblyButton.setSelection( CDebugCorePlugin.getDefault().getPluginPreferences().getBoolean( ICDebugConstants.PREF_AUTO_DISASSEMBLY ) );
/*
		fVariableFormatCombo.select( getFormatIndex( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT ) ) );
		fExpressionFormatCombo.select( getFormatIndex( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT ) ) );
		fRegisterFormatCombo.select( getFormatIndex( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT ) ) );
*/
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init( IWorkbench workbench )
	{
	}

	protected PropertyChangeListener getPropertyChangeListener()
	{
		if ( fPropertyChangeListener == null )
		{
			fPropertyChangeListener = new PropertyChangeListener();
		}
		return fPropertyChangeListener;
	}

	/**
	 * Set the default preferences for this page.
	 */
	public static void initDefaults(IPreferenceStore store)
	{
		store.setDefault( ICDebugPreferenceConstants.PREF_SHOW_HEX_VALUES, false );
		store.setDefault( ICDebugPreferenceConstants.PREF_SHOW_FULL_PATHS, true );
		CDebugCorePlugin.getDefault().getPluginPreferences().setDefault( ICDebugConstants.PREF_AUTO_DISASSEMBLY, false );
		CDebugCorePlugin.getDefault().getPluginPreferences().setDefault( ICDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT, ICDIFormat.NATURAL );
		CDebugCorePlugin.getDefault().getPluginPreferences().setDefault( ICDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT, ICDIFormat.NATURAL );
		CDebugCorePlugin.getDefault().getPluginPreferences().setDefault( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT, ICDIFormat.NATURAL );
	}

	/**
	 * @see DialogPage#dispose()
	 */
	public void dispose()
	{
		super.dispose();
		getPreferenceStore().removePropertyChangeListener( getPropertyChangeListener() );
	}

	/**
	 * Create the view setting preferences composite widget
	 */
	private void createViewSettingPreferences( Composite parent )
	{
		Composite comp = createGroupComposite( parent, 1, "Opened view default settings" );
		fPathsButton = createCheckButton( comp, "Show full &paths" );
/*
		Composite formatComposite = ControlFactory.createCompositeEx( comp, 2, 0 );
		((GridLayout)formatComposite.getLayout()).makeColumnsEqualWidth = true;
		fVariableFormatCombo = createComboBox( formatComposite, "Default variable format:", fFormatLabels, fFormatLabels[0] );
		fExpressionFormatCombo = createComboBox( formatComposite, "Default expression format:", fFormatLabels, fFormatLabels[0] );
		fRegisterFormatCombo = createComboBox( formatComposite, "Default register format:", fFormatLabels, fFormatLabels[0] );
*/
	}

	/**
	 * Create the disassembly setting preferences composite widget
	 */
	private void createDisassemblySettingPreferences( Composite parent )
	{
		Composite comp = createGroupComposite( parent, 1, "Disassembly options" );
		fAutoDisassemblyButton = createCheckButton( comp, "Automatically switch to &disassembly mode" );
	}

	/**
	 * Creates a button with the given label and sets the default 
	 * configuration data.
	 */
	private Button createCheckButton( Composite parent, String label )
	{
		Button button = new Button( parent, SWT.CHECK | SWT.LEFT );
		button.setText( label );
		// FieldEditor GridData
		GridData data = new GridData();
		button.setLayoutData( data );
		return button;
	}

	/**
	 * Creates a button with the given label and sets the default 
	 * configuration data.
	 */
//	private Combo createComboBox( Composite parent, String label, String[] items, String selection )
//	{
//		ControlFactory.createLabel( parent, label );
//		Combo combo = ControlFactory.createSelectCombo( parent, items, selection );
//		combo.setLayoutData( new GridData() );
//		return combo;
//	}

	protected void createSpacer( Composite composite, int columnSpan )
	{
		Label label = new Label( composite, SWT.NONE );
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData( gd );
	}

	/**
	 * @see IPreferencePage#performOk()
	 * Also, notifies interested listeners
	 */
	public boolean performOk()
	{
		storeValues();
		if ( getPropertyChangeListener().hasStateChanged() )
		{
			refreshViews();
		}
		CDebugUIPlugin.getDefault().savePluginPreferences();
		CDebugCorePlugin.getDefault().savePluginPreferences();
		return true;
	}

	/**
	 * Refresh the variables and expression views as changes
	 * have occurred that affects these views.
	 */
	private void refreshViews()
	{
		BusyIndicator.showWhile( getShell().getDisplay(), 
								 new Runnable()
									{
										public void run()
										{
											// Refresh interested views
											IWorkbenchWindow[] windows = CDebugUIPlugin.getDefault().getWorkbench().getWorkbenchWindows();
											IWorkbenchPage page = null;
											for ( int i = 0; i < windows.length; i++ )
											{
												page = windows[i].getActivePage();
												if ( page != null )
												{
													refreshViews( page, IDebugUIConstants.ID_EXPRESSION_VIEW );
													refreshViews( page, IDebugUIConstants.ID_VARIABLE_VIEW );
													refreshViews( page, ICDebugUIConstants.ID_REGISTERS_VIEW );
												}
											}
										}
									} );
	}

	/**
	 * Refresh all views in the given workbench page with the given view id
	 */
	protected void refreshViews( IWorkbenchPage page, String viewID )
	{
		IViewPart part = page.findView( viewID );
		if ( part != null )
		{
			IDebugView adapter = (IDebugView)part.getAdapter( IDebugView.class );
			if ( adapter != null )
			{
				Viewer viewer = adapter.getViewer();
				if ( viewer instanceof StructuredViewer )
				{
					((StructuredViewer)viewer).refresh();
				}
			}
		}
	}

	/**
	 * Store the preference values based on the state of the component widgets
	 */
	private void storeValues()
	{
		IPreferenceStore store = getPreferenceStore();
		store.setValue( ICDebugPreferenceConstants.PREF_SHOW_FULL_PATHS, fPathsButton.getSelection() );
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_AUTO_DISASSEMBLY, fAutoDisassemblyButton.getSelection() );
/*
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT, getFormatId( fVariableFormatCombo.getSelectionIndex() ) );
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT, getFormatId( fExpressionFormatCombo.getSelectionIndex() ) );
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT, getFormatId( fRegisterFormatCombo.getSelectionIndex() ) );
*/
	}

	/**
	 * Sets the default preferences.
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults()
	{
		setDefaultValues();
		super.performDefaults();
	}

	private void setDefaultValues()
	{
		IPreferenceStore store = getPreferenceStore();
		fPathsButton.setSelection( store.getDefaultBoolean( ICDebugPreferenceConstants.PREF_SHOW_FULL_PATHS ) );
		fAutoDisassemblyButton.setSelection( CDebugCorePlugin.getDefault().getPluginPreferences().getDefaultBoolean( ICDebugConstants.PREF_AUTO_DISASSEMBLY ) );
/*
		fVariableFormatCombo.select( getFormatIndex( CDebugCorePlugin.getDefault().getPluginPreferences().getDefaultInt( ICDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT ) ) );
		fExpressionFormatCombo.select( getFormatIndex( CDebugCorePlugin.getDefault().getPluginPreferences().getDefaultInt( ICDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT ) ) );
		fRegisterFormatCombo.select( getFormatIndex( CDebugCorePlugin.getDefault().getPluginPreferences().getDefaultInt( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT ) ) );
*/
	}
/*	
	private static int getFormatId( int index )
	{
		return ( index >= 0 && index < fFormatIds.length ) ? fFormatIds[index] : fFormatIds[0];
	}

	private static int getFormatIndex( int id )
	{
		for ( int i = 0; i < fFormatIds.length; ++i )
			if ( fFormatIds[i] == id )
				return i;
		return -1;
	}
*/
}
