package org.eclipse.cdt.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.resources.*;
import org.eclipse.core.runtime.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Abstract base class for plug-ins that integrate with the Eclipse platform UI.
 * <p>
 * Subclasses obtain the following capabilities:
 * </p>
 * <p>
 * Preferences
 * <ul>
 * <li> Preferences are read the first time <code>getPreferenceStore</code> is
 *      called. </li>
 * <li> Preferences are found in the file whose name is given by the constant
 *      <code>FN_PREF_STORE</code>. A preference file is looked for in the plug-in's 
 *		read/write state area.</li>
 * <li> Subclasses should reimplement <code>initializeDefaultPreferences</code>
 *      to set up any default values for preferences. These are the values 
 *      typically used if the user presses the Default button in a preference
 *      dialog. </li>
 * <li>	The plug-in's install directory is checked for a file whose name is given by 
 *		<code>FN_DEFAULT_PREFERENCES</code>.
 *      This allows a plug-in to ship with a read-only copy of a preference file 
 *      containing default values for certain settings different from the 
 *      hard-wired default ones (perhaps as a result of localizing, or for a
 *      common configuration).</li>
 * <li> Plug-in code can call <code>savePreferenceStore</code> to cause 
 *      non-default settings to be saved back to the file in the plug-in's
 *      read/write state area. </li>
 * <li> Preferences are also saved automatically on plug-in shutdown.</li>
 * </ul>
 * Dialogs
 * <ul>
 * <li> Dialog store are read the first time <code>getDialogSettings</code> is 
 *      called.</li>
 * <li> The dialog store allows the plug-in to "record" important choices made
 *      by the user in a wizard or dialog, so that the next time the
 *      wizard/dialog is used the widgets can be defaulted to better values. A
 *      wizard could also use it to record the last 5 values a user entered into
 *      an editable combo - to show "recent values". </li>
 * <li> The dialog store is found in the file whose name is given by the
 *      constant <code>FN_DIALOG_STORE</code>. A dialog store file is first
 *      looked for in the plug-in's read/write state area; if not found there,
 *      the plug-in's install directory is checked.
 *      This allows a plug-in to ship with a read-only copy of a dialog store
 *      file containing initial values for certain settings.</li>
 * <li> Plug-in code can call <code>saveDialogSettings</code> to cause settings to
 *      be saved in the plug-in's read/write state area. A plug-in may opt to do
 *      this each time a wizard or dialog is closed to ensure the latest 
 *      information is always safe on disk. </li>
 * <li> Dialog settings are also saved automatically on plug-in shutdown.</li>
 * </ul>
 * Images
 * <ul>
 * <li> A typical UI plug-in will have some images that are used very frequently
 *      and so need to be cached and shared.  The plug-in's image registry 
 *      provides a central place for a plug-in to store its common images. 
 *      Images managed by the registry are created lazily as needed, and will be
 *      automatically disposed of when the plug-in shuts down. Note that the
 *      number of registry images should be kept to a minimum since many OSs
 *      have severe limits on the number of images that can be in memory at once.
 * </ul>
 * <p>
 * For easy access to your plug-in object, use the singleton pattern. Declare a
 * static variable in your plug-in class for the singleton. Store the first
 * (and only) instance of the plug-in class in the singleton when it is created.
 * Then access the singleton when needed through a static <code>getDefault</code>
 * method.
 * </p>
 */
public abstract class AbstractPlugin extends Plugin
{
	/**
	 * The name of the preference storage file (value
	 * <code>"pref_store.ini"</code>).
	 */
	private static final String FN_PREF_STORE= "pref_store.ini";//$NON-NLS-1$
	/**
	 * The name of the default preference settings file (value
	 * <code>"preferences.ini"</code>).
	 */
	private static final String FN_DEFAULT_PREFERENCES= "preferences.ini";//$NON-NLS-1$

	/**
	 * Storage for preferences; <code>null</code> if not yet initialized.
	 */
	private PreferenceStore preferenceStore = null;

/**
 * Creates an abstract plug-in runtime object for the given plug-in descriptor.
 * <p>
 * Note that instances of plug-in runtime classes are automatically created 
 * by the platform in the course of plug-in activation.
 * </p>
 *
 * @param descriptor the plug-in descriptor
 */
public AbstractPlugin(IPluginDescriptor descriptor) {
	super(descriptor);
}

/**
 * Returns the preference store for this UI plug-in.
 * This preference store is used to hold persistent settings for this plug-in in
 * the context of a workbench. Some of these settings will be user controlled, 
 * whereas others may be internal setting that are never exposed to the user.
 * <p>
 * If an error occurs reading the preference store, an empty preference store is
 * quietly created, initialized with defaults, and returned.
 * </p>
 * <p>
 * Subclasses should reimplement <code>initializeDefaultPreferences</code> if
 * they have custom graphic images to load.
 * </p>
 *
 * @return the preference store 
 */
public IPropertyStore getPreferenceStore() {
	if (preferenceStore == null) {
		loadPreferenceStore();
		initializeDefaultPreferences(preferenceStore);
		initializePluginPreferences(preferenceStore);
	}
	return preferenceStore;
}


/** 
 * Initializes a preference store with default preference values 
 * for this plug-in.
 * <p>
 * This method is called after the preference store is initially loaded
 * (default values are never stored in preference stores).
 * <p><p>
 * The default implementation of this method does nothing.
 * Subclasses should reimplement this method if the plug-in has any preferences.
 * </p>
 *
 * @param store the preference store to fill
 */
protected void initializeDefaultPreferences(IPropertyStore store) {
}

/**
 * Sets default preferences defined in the plugin directory.
 * If there are no default preferences defined, or some other
 * problem occurs, we fail silently.
 */
private void initializePluginPreferences(IPropertyStore store) {
	URL baseURL = getDescriptor().getInstallURL();

	URL iniURL= null;
	try {
		iniURL = new URL(baseURL, FN_DEFAULT_PREFERENCES);
	} catch (MalformedURLException e) {
		return;
	}

	Properties ini = new Properties();
	InputStream is = null;
	try {
		is = iniURL.openStream();
		ini.load(is);
	}
	catch (IOException e) {
		// Cannot read ini file;
		return;
	}
	finally {
		try { 
			if (is != null)
				is.close(); 
		} catch (IOException e) {}
	}

	Enumeration enum = ini.propertyNames();
	while (enum.hasMoreElements()) {
		String key = (String)enum.nextElement();
		store.setDefault(key, ini.getProperty(key));
	}
}

/**
 * Loads the preference store for this plug-in.
 * The default implementation looks for a standard named file in the 
 * plug-in's read/write state area. If no file is found or a problem
 * occurs, a new empty preference store is silently created. 
 * <p>
 * This framework method may be overridden, although this is typically 
 * unnecessary.
 * </p>
 */
protected void loadPreferenceStore() {
	String readWritePath = getStateLocation().append(FN_PREF_STORE).toOSString();
	preferenceStore = new PreferenceStore(readWritePath);
	try {
		preferenceStore.load();
	}
	catch (IOException e) {
		// Load failed, perhaps because the file does not yet exist.
		// At any rate we just return and leave the store empty.
	}
	return;
}

/**
 * Saves this plug-in's preference store.
 * Any problems which arise are silently ignored.
 */
protected void savePreferenceStore() {
	if (preferenceStore == null) {
		return;
	}
	try {
		preferenceStore.save(); // the store knows its filename - no need to pass it
	}
	catch (IOException e) {
	}
}

/**
 * The <code>AbstractPlugin</code> implementation of this <code>Plugin</code>
 * method saves this plug-in's preference and dialog stores and shuts down 
 * its image registry (if they are in use). Subclasses may extend this method,
 * but must send super first.
 */
public void shutdown() throws CoreException {
	super.shutdown();
	savePreferenceStore();
	preferenceStore = null;
}

public Object getAdapter(Class adapter) {
    return Platform.getAdapterManager().getAdapter(this, adapter);
}

}