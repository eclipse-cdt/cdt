/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.preferences;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
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
	// Timeout preference widgets
	private IntegerFieldEditor fTimeoutText;

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

		Composite comp = createGroupComposite( composite, 1, "Communication" );
		//Add in an intermediate composite to allow for spacing
		Composite spacingComposite = new Composite( comp, SWT.NONE );
		layout = new GridLayout();
		spacingComposite.setLayout( layout );
		data = new GridData( GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL );
		data.horizontalSpan = 2;
		spacingComposite.setLayoutData( data );

		fTimeoutText = new IntegerFieldEditor( CDebugModel.PREF_REQUEST_TIMEOUT, "Debugger &timeout (ms):", spacingComposite );
		fTimeoutText.setPreferenceStore( CDebugUIPlugin.getDefault().getPreferenceStore() );
		fTimeoutText.setPreferencePage( this );
		fTimeoutText.setValidateStrategy( StringFieldEditor.VALIDATE_ON_KEY_STROKE );
		fTimeoutText.setValidRange( CDebugModel.MIN_REQUEST_TIMEOUT, CDebugModel.MAX_REQUEST_TIMEOUT );
		String minValue = Integer.toString( CDebugModel.MIN_REQUEST_TIMEOUT );
		String maxValue = Integer.toString( CDebugModel.MAX_REQUEST_TIMEOUT );
		fTimeoutText.setErrorMessage( MessageFormat.format( "The valid value range is [{0},{1}].", new String[]{ minValue, maxValue } ) );
		fTimeoutText.load();
		fTimeoutText.setPropertyChangeListener( 
					new IPropertyChangeListener()
						{
							public void propertyChange( PropertyChangeEvent event )
							{
								if ( event.getProperty().equals( FieldEditor.IS_VALID ) )
									setValid( fTimeoutText.isValid() );
							}
						} );

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
		Group comp = new Group( parent, SWT.NONE );
		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		comp.setLayout( layout );
		//GridData
		GridData gd = new GridData();
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalAlignment = GridData.FILL;
		comp.setLayoutData( gd );
		comp.setText( labelText );
		return comp;
	}
		
	/**
	 * Set the values of the component widgets based on the
	 * values in the preference store
	 */
	private void setValues()
	{
		IPreferenceStore store = getPreferenceStore();

		fTimeoutText.setStringValue( new Integer( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( CDebugModel.PREF_REQUEST_TIMEOUT ) ).toString() );
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
		store.setDefault( ICDebugPreferenceConstants.PREF_SHOW_CHAR_VALUES, false );
	}

	/**
	 * @see DialogPage#dispose()
	 */
	public void dispose()
	{
		super.dispose();
		getPreferenceStore().removePropertyChangeListener( getPropertyChangeListener() );
	}
}
