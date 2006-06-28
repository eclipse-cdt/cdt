/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.propertypages;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.internal.model.SystemPreferenceChangeEvent;
import org.eclipse.rse.internal.model.SystemProfileManager;
import org.eclipse.rse.model.ISystemPreferenceChangeEvents;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.Mnemonics;
import org.eclipse.rse.ui.RSESystemTypeAdapter;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemConnectionForm;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * Root preference page for Remote Systems Plugin
 */
public class RemoteSystemsPreferencePage 
       extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, 
                                                    ISystemPreferencesConstants
{
    private SystemBooleanFieldEditor showFilterPoolsEditor;
    private SystemBooleanFieldEditor qualifyConnectionNamesEditor;
    private SystemBooleanFieldEditor rememberStateEditor;
    private SystemBooleanFieldEditor useDeferredQueryEditor;

	// yantzi: artemis 60, restore from cache when available
	private SystemBooleanFieldEditor restoreFromCache;
	private Composite innerComposite;

	private SystemTypeFieldEditor systemTypesEditor;
    private SystemBooleanFieldEditor showNewConnectionPromptEditor;
    private boolean lastShowFilterPoolsValue = false;
    private boolean lastQualifyConnectionNamesValue = false;
    private boolean lastRememberStateValue = true; // changed in R2 by Phil. Not sure about migration!
	private boolean lastRestoreFromCacheValue = true; // yantzi: new in artemis 6.0
    private boolean lastShowNewConnectionPromptValue = true;
    private boolean lastUseDeferredQueryValue = false;
	
	/**
	 * Constructor
	 */
	public RemoteSystemsPreferencePage() 
	{
		super(GRID);
		setTitle(SystemResources.RESID_PREF_ROOT_PAGE);		
		setPreferenceStore(RSEUIPlugin.getDefault().getPreferenceStore());
		setDescription(SystemResources.RESID_PREF_ROOT_TITLE);
	}
	/**
	 * We intercept to set the help
	 */
	public void createControl(Composite parent) 
	{
		super.createControl(parent);
	}
	
	/**
	 * GUI widgets for preferences page
	 */
	protected void createFieldEditors() 
	{
        // DEFAULT SYSTEM TYPE		
		SystemComboBoxFieldEditor systemTypeEditor = new SystemComboBoxFieldEditor(
			ISystemPreferencesConstants.SYSTEMTYPE,
			SystemResources.RESID_PREF_SYSTEMTYPE_PREFIX_LABEL,
			RSECorePlugin.getDefault().getRegistry().getSystemTypeNames(),
			true, // readonly
			getFieldEditorParent()
		);
		systemTypeEditor.setToolTipText(SystemResources.RESID_PREF_SYSTEMTYPE_PREFIX_TOOLTIP);
		addField(systemTypeEditor);

        // ENABLED STATE AND DEFAULT USERID PER SYSTEM TYPE 
        systemTypesEditor = new SystemTypeFieldEditor(
            ISystemPreferencesConstants.SYSTEMTYPE_VALUES,
            SystemResources.RESID_PREF_USERID_PERTYPE_PREFIX_LABEL,
            getFieldEditorParent()
        );
        addField(systemTypesEditor); 
        systemTypesEditor.setToolTipText(SystemResources.RESID_PREF_USERID_PERTYPE_PREFIX_TOOLTIP);
		
        // QUALIFY CONNECTION NAMES
		qualifyConnectionNamesEditor = new SystemBooleanFieldEditor(
			ISystemPreferencesConstants.QUALIFY_CONNECTION_NAMES,
			SystemResources.RESID_PREF_QUALIFYCONNECTIONNAMES_PREFIX_LABEL,
			getFieldEditorParent()
		);
		addField(qualifyConnectionNamesEditor);
		qualifyConnectionNamesEditor.setToolTipText(SystemResources.RESID_PREF_QUALIFYCONNECTIONNAMES_PREFIX_TOOLTIP);
		lastQualifyConnectionNamesValue = getPreferenceStore().getBoolean(qualifyConnectionNamesEditor.getPreferenceName());										

        // SHOW FILTER POOLS 
	    showFilterPoolsEditor = new SystemBooleanFieldEditor(
			ISystemPreferencesConstants.SHOWFILTERPOOLS,
			SystemResources.RESID_PREF_SHOWFILTERPOOLS_PREFIX_LABEL,
			getFieldEditorParent()
		);
		addField(showFilterPoolsEditor);
		showFilterPoolsEditor.setToolTipText(SystemResources.RESID_PREF_SHOWFILTERPOOLS_PREFIX_TOOLTIP);
		lastShowFilterPoolsValue = getPreferenceStore().getBoolean(showFilterPoolsEditor.getPreferenceName());

        // SHOW "NEW CONNECTION..." PROMPT INSIDE REMOTE SYSTEMS VIEW
	    showNewConnectionPromptEditor = new SystemBooleanFieldEditor(
			ISystemPreferencesConstants.SHOWNEWCONNECTIONPROMPT,
			SystemResources.RESID_PREF_SHOWNEWCONNECTIONPROMPT_PREFIX_LABEL,
			getFieldEditorParent()
		);
		addField(showNewConnectionPromptEditor);
		showNewConnectionPromptEditor.setToolTipText(SystemResources.RESID_PREF_SHOWNEWCONNECTIONPROMPT_PREFIX_TOOLTIP);
		lastShowNewConnectionPromptValue = getPreferenceStore().getBoolean(showNewConnectionPromptEditor.getPreferenceName());

        // REMEMBER STATE
		rememberStateEditor = new SystemBooleanFieldEditor(
			ISystemPreferencesConstants.REMEMBER_STATE,
			SystemResources.RESID_PREF_REMEMBERSTATE_PREFIX_LABEL,
			getFieldEditorParent()
		);
		addField(rememberStateEditor);
		rememberStateEditor.setToolTipText(SystemResources.RESID_PREF_REMEMBERSTATE_PREFIX_TOOLTIP);
		lastRememberStateValue = getPreferenceStore().getBoolean(rememberStateEditor.getPreferenceName());		

		// Restore from cache
		innerComposite = SystemWidgetHelpers.createComposite(getFieldEditorParent(), SWT.NULL);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 20;
		innerComposite.setLayoutData(gd);
		restoreFromCache = new SystemBooleanFieldEditor(
			ISystemPreferencesConstants.RESTORE_STATE_FROM_CACHE,
			SystemResources.RESID_PREF_RESTOREFROMCACHE_PREFIX_LABEL,
			innerComposite
		);
		restoreFromCache.setEnabled(lastRememberStateValue, innerComposite);
		addField(restoreFromCache);
		restoreFromCache.setToolTipText(SystemResources.RESID_PREF_RESTOREFROMCACHE_PREFIX_TOOLTIP);
		lastRestoreFromCacheValue = getPreferenceStore().getBoolean(ISystemPreferencesConstants.RESTORE_STATE_FROM_CACHE);

		// USE DEFERRED QUERY
		useDeferredQueryEditor = new SystemBooleanFieldEditor(
		        ISystemPreferencesConstants.USE_DEFERRED_QUERIES,
		        SystemResources.RESID_PREF_USEDEFERREDQUERIES_PREFIX_LABEL,
		        getFieldEditorParent())
		        ;
		addField(useDeferredQueryEditor);
		useDeferredQueryEditor.setToolTipText(SystemResources.RESID_PREF_USEDEFERREDQUERIES_PREFIX_TOOLTIP);
		lastUseDeferredQueryValue = getPreferenceStore().getBoolean(useDeferredQueryEditor.getPreferenceName());
		
		/** FIXME - UDA should not be so coupled to core
		 * might need a new preference page for this
        // CASCADE USER-DEFINED ACTIONS BY PROFILE
		SystemBooleanFieldEditor cascadeUDAsEditor = new SystemBooleanFieldEditor(
			ISystemPreferencesConstants.CASCADE_UDAS_BYPROFILE,
			SystemUDAResources.RESID_PREF_UDAS_CASCADEBYPROFILE_LABEL,
			getFieldEditorParent()
		);
		addField(cascadeUDAsEditor);
		cascadeUDAsEditor.setToolTipText(SystemUDAResources.RESID_PREF_UDAS_CASCADEBYPROFILE_TOOLTIP);
		lastCascadeUDAsValue = getPreferenceStore().getBoolean(cascadeUDAsEditor.getPreferenceName());		
		**/	
		// set mnemonics
        (new Mnemonics()).setOnPreferencePage(true).setMnemonics(getFieldEditorParent());
        
        // set help
		SystemWidgetHelpers.setCompositeHelp(getFieldEditorParent(), RSEUIPlugin.HELPPREFIX+"rsep0000");
	}
	
	// ---------------------------------------------------------
	// GETTERS/SETTERS FOR EACH OF THE USER PREFERENCE VALUES...
	// ---------------------------------------------------------
	// DWD these preferences methods should be moved to SystemPreferencesManager since they are not a proper function of a preference page.
    /**
     * Return the names of the profiles the user has elected to make "active".
     */
	public static String[] getActiveProfiles()
	{
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		return parseStrings(store.getString(ISystemPreferencesConstants.ACTIVEUSERPROFILES));		
	}
	
    /**
     * Set the names of the profiles the user has elected to make "active".
     */
	public static void setActiveProfiles(String[] newProfileNames)
	{		
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.ACTIVEUSERPROFILES, makeString(newProfileNames));		
		savePreferenceStore();
	}
	
    /**
     * Return the ordered list of connection names. This is how user arranged his connections in the system view.
     */
	public static String[] getConnectionNamesOrder()
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		return parseStrings(store.getString(ISystemPreferencesConstants.ORDER_CONNECTIONS));		
	}
    /**
     * Set the ordered list of connection names. This is how user arranged his connections in the system view.
     */
	public static void setConnectionNamesOrder(String[] newConnectionNamesOrder)
	{		
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.ORDER_CONNECTIONS, makeString(newConnectionNamesOrder));		
		savePreferenceStore();
	}
    /**
     * Return true if the user has elected to show filter pools in the remote systems explorer view
     */
	public static boolean getShowFilterPoolsPreference() 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(ISystemPreferencesConstants.SHOWFILTERPOOLS);
	}
    /**
     * Toggle whether to show filter pools in the remote systems explorer view
     */
	public static void setShowFilterPoolsPreference(boolean show) 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.SHOWFILTERPOOLS,show);
		savePreferenceStore();
	}

    /**
     * Return true if the user has elected to show the "New Connection..." prompt in the Remote Systems view
     */
	public static boolean getShowNewConnectionPromptPreference() 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		boolean value = store.getBoolean(ISystemPreferencesConstants.SHOWNEWCONNECTIONPROMPT);
		return value;
	}
    /**
     * Toggle whether to show filter pools in the remote systems explorer view
     */
	public static void setShowNewConnectionPromptPreference(boolean show) 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.SHOWNEWCONNECTIONPROMPT,show);
		savePreferenceStore();
	}

    /**
     * Return true if the user has elected to show connection names qualified by profile
     */
	public static boolean getQualifyConnectionNamesPreference() 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(ISystemPreferencesConstants.QUALIFY_CONNECTION_NAMES);
	}
    /**
     * Set if the user has elected to show connection names qualified by profile
     */
	public static void setQualifyConnectionNamesPreference(boolean set) 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.QUALIFY_CONNECTION_NAMES,set);
		savePreferenceStore();
	}

    /**
     * Return true if the user has elected to remember the state of the Remote Systems view
     */
	public static boolean getRememberStatePreference() 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(ISystemPreferencesConstants.REMEMBER_STATE);
	}
    /**
     * Set if the user has elected to show connection names qualified by profile
     */
	public static void setRememberStatePreference(boolean set) 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.REMEMBER_STATE,set);
		savePreferenceStore();
	}

	/**
	 * Return true if the user has elected to restore the state of the Remote Systems view from cached information
	 */
	public static boolean getRestoreStateFromCachePreference() 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(ISystemPreferencesConstants.RESTORE_STATE_FROM_CACHE);
	}

	/**
	 * Set if the user has elected to restore the state of the Remote Systems view from cached information
	 */
	public static void setRestoreStateFromCachePreference(boolean set) 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.RESTORE_STATE_FROM_CACHE, set);
		savePreferenceStore();
	}

    /**
     * Return true if the user has elected to show user defined actions cascaded by profile
     */
	public static boolean getCascadeUserActionsPreference() 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(ISystemPreferencesConstants.CASCADE_UDAS_BYPROFILE);
	}
    /**
     * Set if the user has elected to show user defined actions cascaded by profile
     */
	public static void setCascadeUserActionsPreference(boolean set) 
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.CASCADE_UDAS_BYPROFILE,set);
		savePreferenceStore();
	}
    /**
     * Return the userId to default to on the Create Connection wizard, per the given system type.
     * 
     * @see SystemConnectionForm
     */
	public static String getUserIdPreference(String systemType)
	{
		if (systemType == null)
		  return null;

		IRSESystemType sysType = RSECorePlugin.getDefault().getRegistry().getSystemType(systemType);
		Object adapter = sysType.getAdapter(IRSESystemType.class);
		if (adapter instanceof RSESystemTypeAdapter)
		{
			RSESystemTypeAdapter sysTypeAdapter = (RSESystemTypeAdapter)adapter;
			return sysTypeAdapter.getDefaultUserId(sysType);
		}
		else
			return null;
	}
	
    /**
     * Set the default userId per the given system type.
     */
	public static void setUserIdPreference(String systemType, String userId)
	{
		IRSESystemType sysType = RSECorePlugin.getDefault().getRegistry().getSystemType(systemType);
		RSESystemTypeAdapter sysTypeAdapter = (RSESystemTypeAdapter)(sysType.getAdapter(IRSESystemType.class));
		if (sysTypeAdapter != null)
			sysTypeAdapter.setDefaultUserId(sysType, userId);
		else
			return;
		// following needs to stay in synch with modify() method in SystemTypeFieldEditor...
		String value = RSEUIPlugin.getDefault().getPreferenceStore().getString(ISystemPreferencesConstants.SYSTEMTYPE_VALUES);
		Hashtable keyValues = null;
	    if ((value == null) || (value.length()==0)) // not initialized yet?
	    {
    		keyValues = new Hashtable();
	    	// nothing to do, as we have read from systemTypes extension points already
	    }
	    else
	    {
	    	keyValues = parseString(value);
	    }
	    
	    String defaultUserId = sysTypeAdapter.getDefaultUserId(sysType);
	    
	    if (defaultUserId == null) {
	    	defaultUserId = "null";
	    }
	    
	    keyValues.put(sysType.getName(), "" + sysTypeAdapter.isEnabled(sysType) + SystemTypeFieldEditor.EACHVALUE_DELIMITER + defaultUserId);
		String s = SystemTypeFieldEditor.createString(keyValues);

		if (s != null)
			RSEUIPlugin.getDefault().getPreferenceStore().setValue(ISystemPreferencesConstants.SYSTEMTYPE_VALUES, s);	
				        
		savePreferenceStore();
	}


    /**
     * Return the hashtable where the key is a string identifying a particular object, and 
     *  the value is the user Id for that object.
     */
	public static Hashtable getUserIdsPerKey()
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		Hashtable keyValues = null;
		String value = store.getString(ISystemPreferencesConstants.USERIDPERKEY);
		if (value != null)
		  keyValues = parseString(value);
		else
		{
		  keyValues = new Hashtable();
		}
		return keyValues;
	}
	/**
	 * Set/store the user ids that are saved keyed by some key.
	 */
	public static void setUserIdsPerKey(Hashtable uidsPerKey)
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();				
		store.setValue(ISystemPreferencesConstants.USERIDPERKEY, makeString(uidsPerKey));		
		savePreferenceStore();
	}

    /**
     * Return the System type to default to on the Create Connection wizard.
     * 
     * @see SystemConnectionForm
     */
	public static String getSystemTypePreference()
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		return store.getString(ISystemPreferencesConstants.SYSTEMTYPE);		
	}

    /**
     * Return the history for the folder combo box widget
     */
	public static String[] getFolderHistory()
	{
		return getWidgetHistory(ISystemPreferencesConstants.HISTORY_FOLDER);
	}
    /**
     * Set the history for the folder combo box widget.
     */
	public static void setFolderHistory(String[] newHistory)
	{		
		setWidgetHistory(ISystemPreferencesConstants.HISTORY_FOLDER, newHistory);
	}
    /**
     * Return the history for a widget given an arbitrary key uniquely identifying it
     */
	public static String[] getWidgetHistory(String key)
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		return parseStrings(store.getString(key));		
	}
    /**
     * Set the history for a widget given an arbitrary key uniquely identifying it.
     */
	public static void setWidgetHistory(String key, String[] newHistory)
	{		
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(key, makeString(newHistory));		
		savePreferenceStore();
	}

		
	// -------------------------------------------------
	// MISCELLANEOUS METHODS...
	// -------------------------------------------------
	
	/**
	 * Parse out list of key-value pairs into a hashtable
	 */
	protected static Hashtable parseString(String allvalues)
	{
		StringTokenizer tokens = new StringTokenizer(allvalues, "=;");
		Hashtable keyValues = new Hashtable(10);
		int count = 0;
		String token1=null;
		String token2=null;
		while (tokens.hasMoreTokens())
		{
			count++;
			if ((count % 2) == 0) // even number
			{
			  token2 = tokens.nextToken();
			  keyValues.put(token1, token2);
			}
			else
			  token1 = tokens.nextToken();
		}
		return keyValues;
	}
	/**
	 * Convert hashtable of key-value pairs into a single string
	 */
	protected static String makeString(Hashtable keyValues)
	{
		Enumeration keys = keyValues.keys();
		StringBuffer sb = new StringBuffer();
		while (keys.hasMoreElements())
		{
			String key = (String)keys.nextElement();
			String value = (String)keyValues.get(key);
			if ((value != null) && (value.length()>0))
			{
			  sb.append(key);
			  sb.append('=');
			  sb.append(value);
			  sb.append(';');
			}
		}
		return sb.toString();
	}
		
	/**
	 * Parse out list of multiple values into a string array per value
	 */
	protected static String[] parseStrings(String allvalues)
	{
		if (allvalues == null)
		  return new String[0];
		//StringTokenizer tokens = new StringTokenizer(allvalues, ";");
		String[] tokens = allvalues.split(";");
		return tokens;
		/*
		Vector v = new Vector();
		int idx=0;
		while (tokens.hasMoreTokens())
		 v.addElement(tokens.nextToken());
		String keyValues[] = new String[v.size()];
		for (idx=0;idx<v.size();idx++)
		  keyValues[idx] = (String)v.elementAt(idx);
		return keyValues;
		*/
	}
	/**
	 * Make a single string out of an array of strings
	 */
	protected static String makeString(String[] values)
	{
        StringBuffer allValues = new StringBuffer();
        boolean first = true;
		for (int idx=0; idx<values.length; idx++)
		{
		   if (values[idx] != null)
		   {
		     if (!first)
		     {
		       allValues = allValues.append(';');
		     }
		     allValues.append(values[idx]);
             first = false;
		   }
		}
		return allValues.toString();
	}
	
	/**
	 * Save the preference store.
	 */
	private static void savePreferenceStore()
	{
		/* plugin preferences and preference stores are actually the same store and are flushed to disk using this call */
		RSEUIPlugin.getDefault().savePluginPreferences();
	}

	public void init(IWorkbench workbench) 
	{
	}

    /**
     * Initialize our preference store with our defaults.
     * This is called in RSEUIPlugin.initializeDefaultPreferences
     */
	public static void initDefaults(IPreferenceStore store, boolean showNewConnectionPromptDefault) 
	{
		store.setDefault(ISystemPreferencesConstants.SYSTEMTYPE,               ISystemPreferencesConstants.DEFAULT_SYSTEMTYPE);
		store.setDefault(ISystemPreferencesConstants.QUALIFY_CONNECTION_NAMES, ISystemPreferencesConstants.DEFAULT_QUALIFY_CONNECTION_NAMES);		
		store.setDefault(ISystemPreferencesConstants.SHOWFILTERPOOLS,          ISystemPreferencesConstants.DEFAULT_SHOWFILTERPOOLS);
		
		String defaultProfileNames = ISystemPreferencesConstants.DEFAULT_ACTIVEUSERPROFILES;
		String userProfileName = SystemProfileManager.getDefaultPrivateSystemProfileName();
		defaultProfileNames += ";" + userProfileName;
		
		
		store.setDefault(ISystemPreferencesConstants.ACTIVEUSERPROFILES,       defaultProfileNames);
		store.setDefault(ISystemPreferencesConstants.ORDER_CONNECTIONS,        ISystemPreferencesConstants.DEFAULT_ORDER_CONNECTIONS);
		store.setDefault(ISystemPreferencesConstants.HISTORY_FOLDER,           ISystemPreferencesConstants.DEFAULT_HISTORY_FOLDER);
		store.setDefault(ISystemPreferencesConstants.REMEMBER_STATE,           ISystemPreferencesConstants.DEFAULT_REMEMBER_STATE);
		store.setDefault(ISystemPreferencesConstants.RESTORE_STATE_FROM_CACHE, ISystemPreferencesConstants.DEFAULT_RESTORE_STATE_FROM_CACHE);
		store.setDefault(ISystemPreferencesConstants.SHOWNEWCONNECTIONPROMPT,  showNewConnectionPromptDefault);
		store.setDefault(ISystemPreferencesConstants.CASCADE_UDAS_BYPROFILE,   ISystemPreferencesConstants.DEFAULT_CASCADE_UDAS_BYPROFILE);
		
		store.setDefault(ISystemPreferencesConstants.USE_DEFERRED_QUERIES, ISystemPreferencesConstants.DEFAULT_USE_DEFERRED_QUERIES);
	}
	
	/**
	 * we don't use this after all because it causes an event to be fired as the
	 *  user makes each change. We prefer to wait until Apply or Defaults are pressed.
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
       
        // yantzi: artemis 6.0
        if (event.getSource() == rememberStateEditor)
        {
        	Object newValue = event.getNewValue();
        	if (newValue instanceof Boolean)
        	{
        		restoreFromCache.setEnabled(((Boolean) newValue).booleanValue(), innerComposite);
        	}
        }

        super.propertyChange(event);		
	}

    /**
     * Override of parent so we can fire changes to our views
     */
    public boolean performOk() 
    {
    	boolean ok = super.performOk();
    	savePreferenceStore(); // better save to disk, just in case.
        if (!RSEUIPlugin.getDefault().isSystemRegistryActive())    	
          	return ok;
    	if (showFilterPoolsEditor != null)
    	{
    	   	boolean newValue = showFilterPoolsEditor.getBooleanValue();
    	   	if (newValue != lastShowFilterPoolsValue)
    	   	{
    	     	RSEUIPlugin.getDefault().getSystemRegistry().setShowFilterPools(newValue);
    	     	firePreferenceChangeEvent(ISystemPreferenceChangeEvents.EVENT_SHOWFILTERPOOLS,lastShowFilterPoolsValue,newValue);
    	   	}
    	   	lastShowFilterPoolsValue = newValue;
    	}
    	if (showNewConnectionPromptEditor != null)
    	{
    	   	boolean newValue = showNewConnectionPromptEditor.getBooleanValue();
    	   	if (newValue != lastShowNewConnectionPromptValue)
    	   	{
    	     	RSEUIPlugin.getDefault().getSystemRegistry().setShowNewHostPrompt(newValue);
    	   	}
    	   	lastShowNewConnectionPromptValue = newValue;    		
    	}
    	if (qualifyConnectionNamesEditor != null)
    	{
    	   	boolean newValue = qualifyConnectionNamesEditor.getBooleanValue();
    	   	if (newValue != lastQualifyConnectionNamesValue)
    	   	{
    	     	RSEUIPlugin.getDefault().getSystemRegistry().setQualifiedHostNames(newValue);
    	     	firePreferenceChangeEvent(ISystemPreferenceChangeEvents.EVENT_QUALIFYCONNECTIONNAMES,lastQualifyConnectionNamesValue,newValue);
    	   	}
    	   	lastQualifyConnectionNamesValue = newValue;    		
    	}
		if (rememberStateEditor != null)
		{
		  	boolean newValue = rememberStateEditor.getBooleanValue();
		   	if (newValue != lastRememberStateValue)
		   	{
			 	firePreferenceChangeEvent(ISystemPreferenceChangeEvents.EVENT_RESTORESTATE,lastRememberStateValue,newValue);
		   	}
		   	lastRememberStateValue = newValue;    		
		}
		if (useDeferredQueryEditor != null)
		{
		  	boolean newValue = useDeferredQueryEditor.getBooleanValue();
		   	if (newValue != lastUseDeferredQueryValue)
		   	{
			 	firePreferenceChangeEvent(ISystemPreferenceChangeEvents.EVENT_RESTORESTATE,lastUseDeferredQueryValue,newValue);
		   	}
		   	lastUseDeferredQueryValue = newValue;    		
		}

    	return ok;
    }	
    
    /**
     * Fire a preference change event
     */
    private void firePreferenceChangeEvent(int type, boolean oldValue, boolean newValue)
    {
    	RSEUIPlugin.getDefault().getSystemRegistry().fireEvent(
    	  new SystemPreferenceChangeEvent(type,
    	                                  oldValue ? Boolean.TRUE : Boolean.FALSE,
    	                                  newValue ? Boolean.TRUE : Boolean.FALSE));
    } 
}