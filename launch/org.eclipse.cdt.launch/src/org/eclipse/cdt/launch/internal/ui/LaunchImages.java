package org.eclipse.cdt.launch.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/*
 * (c) Copyright QN Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
public class LaunchImages {
	private static final String NAME_PREFIX= LaunchUIPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH= NAME_PREFIX.length();
	
	// The plugin registry
	private static ImageRegistry imageRegistry = new ImageRegistry();

	// Subdirectory (under the package containing this class) where 16 color images are
	private static URL fgIconBaseURL;
	static {
		try {
			fgIconBaseURL= new URL(LaunchUIPlugin.getDefault().getDescriptor().getInstallURL(), "icons/" );
		} catch (MalformedURLException e) {
			//LaunchUIPlugin.getDefault().log(e);
		}
	}	

	private static final String T_TABS = "tabs/";

	public static String IMG_VIEW_MAIN_TAB = NAME_PREFIX + "main_tab.gif";
	public static String IMG_VIEW_ARGUMENTS_TAB = NAME_PREFIX + "arguments_tab.gif";
	public static String IMG_VIEW_ENVIRONMENT_TAB = NAME_PREFIX + "environment_tab.gif";
	public static String IMG_VIEW_DEBUGGER_TAB = NAME_PREFIX + "debugger_tab.gif";
	public static String IMG_VIEW_SOURCE_TAB = NAME_PREFIX + "source_tab.gif";

	public static final ImageDescriptor DESC_TAB_MAIN= createManaged(T_TABS, IMG_VIEW_MAIN_TAB);
	public static final ImageDescriptor DESC_TAB_ARGUMENTS = createManaged(T_TABS, IMG_VIEW_ARGUMENTS_TAB);
	public static final ImageDescriptor DESC_TAB_ENVIRONMENT = createManaged(T_TABS, IMG_VIEW_ENVIRONMENT_TAB);
	public static final ImageDescriptor DESC_TAB_DEBUGGER = createManaged(T_TABS, IMG_VIEW_DEBUGGER_TAB);
	public static final ImageDescriptor DESC_TAB_SOURCE = createManaged(T_TABS, IMG_VIEW_SOURCE_TAB);
	
	public static void initialize() {
	}
	
	private static ImageDescriptor createManaged(String prefix, String name) {
		return createManaged(imageRegistry, prefix, name);
	}
	
	private static ImageDescriptor createManaged(ImageRegistry registry, String prefix, String name) {
		ImageDescriptor result= ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
		registry.put(name, result);
		return result;
	}
	
	public static Image get(String key) {
		return imageRegistry.get(key);
	}
	
	private static ImageDescriptor create(String prefix, String name) {
		return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
	}
	
	private static URL makeIconFileURL(String prefix, String name) {
		StringBuffer buffer= new StringBuffer(prefix);
		buffer.append(name);
		try {
			return new URL(fgIconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			CUIPlugin.getDefault().log(e);
			return null;
		}
	}
	
	/**
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	static ImageRegistry getImageRegistry() {
		return imageRegistry;
	}
}
