/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.internal.ui.preferences;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.mi.core.IMIConstants;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.internal.ui.IMIHelpContextIds;
import org.eclipse.cdt.debug.mi.internal.ui.MIUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.preference.FieldEditor;
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
 * 
 * Page for preferences that apply specifically to GDB MI.
 * 
 * @since Oct 4, 2002
 */
public class MIPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
	private final static String GDB_MI_HELP_CONTEXT = MIUIPlugin.PLUGIN_ID + "mi_preference_page_context"; //$NON-NLS-1$
	// Debugger timeout preference widgets
	IntegerFieldEditor fDebugTimeoutText;

	// Launch timeout preference widgets
	IntegerFieldEditor fLaunchTimeoutText;

	/**
	 * Constructor for MIPreferencePage.
	 */
	public MIPreferencePage()
	{
		super();
		setPreferenceStore( MIUIPlugin.getDefault().getPreferenceStore() );
		setDescription( MIUIPlugin.getResourceString("internal.ui.preferences.MIPreferencePage.General_GDBMI_settings") ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContents( Composite parent )
	{
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

		setValues();

		WorkbenchHelp.setHelp(composite, GDB_MI_HELP_CONTEXT);	
		
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
		fDebugTimeoutText.setStringValue( new Integer( MIPlugin.getDefault().getPluginPreferences().getInt( IMIConstants.PREF_REQUEST_TIMEOUT ) ).toString() );
		fLaunchTimeoutText.setStringValue( new Integer( MIPlugin.getDefault().getPluginPreferences().getInt( IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT ) ).toString() );
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk()
	{
		storeValues();
		MIUIPlugin.getDefault().savePluginPreferences();
		MIPlugin.getDefault().savePluginPreferences();
		return true;
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
		fDebugTimeoutText.setStringValue( new Integer( IMIConstants.DEF_REQUEST_TIMEOUT ).toString() );
		fLaunchTimeoutText.setStringValue( new Integer( IMIConstants.DEF_REQUEST_LAUNCH_TIMEOUT ).toString() );
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

	private void createCommunicationPreferences( Composite composite )
	{
		Composite comp = createGroupComposite( composite, 1, MIUIPlugin.getResourceString("internal.ui.preferences.MIPreferencePage.Communication") ); //$NON-NLS-1$
		//Add in an intermediate composite to allow for spacing
		Composite spacingComposite = new Composite( comp, SWT.NONE );
		GridLayout layout = new GridLayout();
		spacingComposite.setLayout( layout );
		GridData data = new GridData();
		data.horizontalSpan = 2;
		spacingComposite.setLayoutData( data );

		fDebugTimeoutText = createTimeoutField( IMIConstants.PREF_REQUEST_TIMEOUT, MIUIPlugin.getResourceString("internal.ui.preferences.MIPreferencePage.Debugger_timeout"), spacingComposite ); //$NON-NLS-1$
		fDebugTimeoutText.setPropertyChangeListener( 
					new IPropertyChangeListener()
						{
							public void propertyChange( PropertyChangeEvent event )
							{
								if ( event.getProperty().equals( FieldEditor.IS_VALID ) )
									setValid( fDebugTimeoutText.isValid() );
							}
						} );

		fLaunchTimeoutText = createTimeoutField( IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT, MIUIPlugin.getResourceString("internal.ui.preferences.MIPreferencePage.Launch_timeout"), spacingComposite ); //$NON-NLS-1$
		fLaunchTimeoutText.setPropertyChangeListener( 
					new IPropertyChangeListener()
						{
							public void propertyChange( PropertyChangeEvent event )
							{
								if ( event.getProperty().equals( FieldEditor.IS_VALID ) )
									setValid( fLaunchTimeoutText.isValid() );
							}
						} );
	}

	/**
	 * Store the preference values based on the state of the
	 * component widgets
	 */
	private void storeValues()
	{
		MIPlugin.getDefault().getPluginPreferences().setValue( IMIConstants.PREF_REQUEST_TIMEOUT, fDebugTimeoutText.getIntValue() );
		MIPlugin.getDefault().getPluginPreferences().setValue( IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT, fLaunchTimeoutText.getIntValue() );
	}

	private IntegerFieldEditor createTimeoutField( String preference, String label, Composite parent )
	{
		IntegerFieldEditor toText = new IntegerFieldEditor( preference, label, parent );
		GridData data = new GridData();
		data.widthHint = convertWidthInCharsToPixels( 10 );
		toText.getTextControl( parent ).setLayoutData( data );
		toText.setPreferenceStore( MIUIPlugin.getDefault().getPreferenceStore() );
		toText.setPreferencePage( this );
		toText.setValidateStrategy( StringFieldEditor.VALIDATE_ON_KEY_STROKE );
		toText.setValidRange( IMIConstants.MIN_REQUEST_TIMEOUT, IMIConstants.MAX_REQUEST_TIMEOUT );
		String minValue = Integer.toString( IMIConstants.MIN_REQUEST_TIMEOUT );
		String maxValue = Integer.toString( IMIConstants.MAX_REQUEST_TIMEOUT );
		toText.setErrorMessage( MessageFormat.format( MIUIPlugin.getResourceString("internal.ui.preferences.MIPreferencePage.Error_Request_timeout_range"), new String[]{ minValue, maxValue } ) ); //$NON-NLS-1$
		toText.load();
		return toText;
	}
}
