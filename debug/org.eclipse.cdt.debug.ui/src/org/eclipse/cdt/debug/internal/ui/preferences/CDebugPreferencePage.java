/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.preferences;

import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
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
import org.eclipse.swt.widgets.Group;
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
	// Primitive display preference widgets
	private Button fHexButton;

	// View setting widgets
	private Button fPathsButton;

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
		createPrimitiveDisplayPreferences( composite );		
		createSpacer( composite, 1 );
		createViewSettingPreferences( composite );
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

		fHexButton.setSelection( store.getBoolean( ICDebugPreferenceConstants.PREF_SHOW_HEX_VALUES ) );
		fPathsButton.setSelection( store.getBoolean( ICDebugPreferenceConstants.PREF_SHOW_FULL_PATHS ) );
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
	 * Create the primitive display preferences composite widget
	 */
	private void createPrimitiveDisplayPreferences( Composite parent )
	{
		Composite comp = createGroupComposite( parent, 1, "Primitive type display options" );
		fHexButton = createCheckButton( comp, "Display &hexadecimal values (short, char, int, long)" );
	}

	/**
	 * Create the view setting preferences composite widget
	 */
	private void createViewSettingPreferences( Composite parent )
	{
		Composite comp = createGroupComposite( parent, 1, "Opened view default settings" );
		fPathsButton = createCheckButton( comp, "Show full &paths" );
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
		store.setValue( ICDebugPreferenceConstants.PREF_SHOW_HEX_VALUES, fHexButton.getSelection() );
		store.setValue( ICDebugPreferenceConstants.PREF_SHOW_FULL_PATHS, fPathsButton.getSelection() );
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
		fHexButton.setSelection( store.getDefaultBoolean( ICDebugPreferenceConstants.PREF_SHOW_HEX_VALUES ) );
	}
}
