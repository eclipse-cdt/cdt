/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/

package org.eclipse.tm.terminal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class TerminalPlugin extends AbstractUIPlugin
    implements TerminalConsts
{
    protected static TerminalPlugin m_Default;

    protected TerminalProperties    m_Properties;
    protected ResourceBundle        m_ResourceBundle;

    /**
     * The constructor.
     */
    public TerminalPlugin()
    {
        super();

        m_Default = this;

        setupPlugin();
    }
    
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
    }
    
    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
    }

    // AbstractUIPlugin interface
    
    /**
     * 
     */
    protected void initializeImageRegistry(ImageRegistry imageRegistry)
    {
        HashMap map;
        
        map = new HashMap();
         
        try 
        {
            // Local toolbars
            map.put(TERMINAL_IMAGE_NEW_TERMINAL, "newterminal.gif"); //$NON-NLS-1$
            map.put(TERMINAL_IMAGE_CLCL_CONNECT, "connect_co.gif"); //$NON-NLS-1$
            map.put(TERMINAL_IMAGE_CLCL_DISCONNECT, "disconnect_co.gif"); //$NON-NLS-1$
            map.put(TERMINAL_IMAGE_CLCL_SETTINGS, "properties_tsk.gif"); //$NON-NLS-1$

            loadImageRegistry(imageRegistry, TERMINAL_IMAGE_DIR_LOCALTOOL, map);

            map.clear();

            // Enabled local toolbars
            map.put(TERMINAL_IMAGE_NEW_TERMINAL, "newterminal.gif"); //$NON-NLS-1$
            map.put(TERMINAL_IMAGE_ELCL_CONNECT, "connect_co.gif"); //$NON-NLS-1$
            map.put(TERMINAL_IMAGE_ELCL_DISCONNECT, "disconnect_co.gif"); //$NON-NLS-1$
            map.put(TERMINAL_IMAGE_ELCL_SETTINGS, "properties_tsk.gif"); //$NON-NLS-1$

            loadImageRegistry(imageRegistry, TERMINAL_IMAGE_DIR_ELCL, map);

            map.clear();

            // Disabled local toolbars
            map.put(TERMINAL_IMAGE_NEW_TERMINAL, "newterminal.gif"); //$NON-NLS-1$
            map.put(TERMINAL_IMAGE_DLCL_CONNECT, "connect_co.gif"); //$NON-NLS-1$
            map.put(TERMINAL_IMAGE_DLCL_DISCONNECT, "disconnect_co.gif"); //$NON-NLS-1$
            map.put(TERMINAL_IMAGE_DLCL_SETTINGS, "properties_tsk.gif"); //$NON-NLS-1$

            loadImageRegistry(imageRegistry, TERMINAL_IMAGE_DIR_DLCL, map);

            map.clear();
        } 
        catch(MalformedURLException malformedURLException) 
        {
                malformedURLException.printStackTrace();
        }
    }

    /**
     * 
     */
    protected void initializeDefaultPreferences(IPreferenceStore store)
    {
        store.setDefault(TERMINAL_PREF_LIMITOUTPUT, TERMINAL_DEFAULT_LIMITOUTPUT);
        store.setDefault(TERMINAL_PREF_BUFFERLINES, TERMINAL_DEFAULT_BUFFERLINES);
        store.setDefault(TERMINAL_PREF_TIMEOUT_SERIAL, TERMINAL_DEFAULT_TIMEOUT_SERIAL);
        store.setDefault(TERMINAL_PREF_TIMEOUT_NETWORK, TERMINAL_DEFAULT_TIMEOUT_NETWORK);
    }

    // Operations

    /**
     * Returns the shared instance.
     */
    public static TerminalPlugin getDefault()
    {
        return m_Default;
    }

    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not found.
     */
    public static String getResourceString(String strKey)
    {
        ResourceBundle resourceBundle;
        
        resourceBundle = m_Default.getResourceBundle();
        
        try
        {
            return resourceBundle.getString(strKey);
        }
        catch(MissingResourceException missingResourceException)
        {
            return strKey;
        }
    }

    /**
     * 
     */
    public static boolean isLogInfoEnabled()
    {
        return isOptionEnabled(TERMINAL_TRACE_DEBUG_LOG_INFO);
    }

    /**
     * 
     */
    public static boolean isLogErrorEnabled()
    {
        return isOptionEnabled(TERMINAL_TRACE_DEBUG_LOG_ERROR);
    }

    /**
     * 
     */
    public static boolean isLogEnabled()
    {
        return isOptionEnabled(TERMINAL_TRACE_DEBUG_LOG);
    }

    /**
     * 
     */
    public static boolean isOptionEnabled(String strOption)
    {
        String  strEnabled;
        Boolean boolEnabled;
        boolean bEnabled;
        
        strEnabled = Platform.getDebugOption(strOption);
        if (strEnabled == null)
            return false;

        boolEnabled = new Boolean(strEnabled);
        bEnabled    = boolEnabled.booleanValue();            

        return bEnabled;            
    }

    /**
     * 
     */
    public TerminalProperties getTerminalProperties()
    {
        return m_Properties;
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle()
    {
        return m_ResourceBundle;
    }
    
    /**
     * 
     */
    protected void loadImageRegistry(ImageRegistry  imageRegistry,
                                     String         strDir,
                                     HashMap        map)
        throws MalformedURLException
    {
        URL             url;
        ImageDescriptor imageDescriptor;
        Iterator        keys;
        String          strKey;
        String          strFile;
        
        keys        = map.keySet().iterator();
        
        while(keys.hasNext())
        {
            strKey  = (String) keys.next();
            strFile = (String) map.get(strKey);
            
            if (strFile != null)
            {
                url                             = TerminalPlugin.getDefault().getBundle().getEntry(TERMINAL_IMAGE_DIR_ROOT + strDir + strFile);
                imageDescriptor = ImageDescriptor.createFromURL(url);
                imageRegistry.put(strKey,imageDescriptor);
            }
        }
    }

    /**
     * 
     */
    protected void setupPlugin()
    {
        setupData();
        setupLog();
        setupResources();
    }

    /**
     * 
     */
    protected void setupData()
    {
        m_Properties = new TerminalProperties();
    }
    
    /**
     * 
     */
    protected void setupLog()
    {
    }
    
    /**
     * 
     */
    protected void setupResources()
    {
        Package pkg;
        String  strPkg;
        String  strBundle;
        
        pkg         = TerminalPlugin.class.getPackage();
        strPkg      = pkg.getName();
        strBundle   = strPkg + ".PluginResources"; //$NON-NLS-1$
        
        try
        {
            m_ResourceBundle = ResourceBundle.getBundle(strBundle);
        }
        catch(MissingResourceException missingResourceException)
        {
            m_ResourceBundle = null;
        }
    }
}
