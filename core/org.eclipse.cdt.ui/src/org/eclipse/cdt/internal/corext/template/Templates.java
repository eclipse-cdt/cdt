package org.eclipse.cdt.internal.corext.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

/**
 * <code>Templates</code> gives access to the available templates.
 */
public class Templates extends TemplateSet {

	private static final String DEFAULT_FILE= "default-templates.xml"; //$NON-NLS-1$
	private static final String NL_DEFAULT_FILE= "$nl$/org/eclipse/cdt/internal/corext/template/default-templates.xml"; //$NON-NLS-1$
	
	private static final String TEMPLATE_FILE= "templates.xml"; //$NON-NLS-1$

	/** Singleton. */
	private static Templates fgTemplates;

	/**
	 * Returns an instance of templates.
	 */
	public static Templates getInstance() {
		if (fgTemplates == null)
			fgTemplates= create();
		
		return fgTemplates;
	}

	private static Templates create() {
		Templates templates= new Templates();

		try {			
			File templateFile= getTemplateFile();
			if (templateFile.exists()) {
				templates.addFromFile(templateFile);
			} else {
				templates.addFromStream(getDefaultsAsStream());
				templates.saveToFile(templateFile);
			}

		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
			ErrorDialog.openError(null,
				TemplateMessages.getString("Templates.error.title"), //$NON-NLS-1$
				e.getMessage(), e.getStatus());

			templates.clear();
		}

		return templates;
	}	
	
	/**
	 * Resets the template set.
	 */
	public void reset() throws CoreException {
		clear();
		addFromFile(getTemplateFile());
	}

	/**
	 * Resets the template set with the default templates.
	 */
	public void restoreDefaults() throws CoreException {
		clear();
		addFromStream(getDefaultsAsStream());
	}

	/**
	 * Saves the template set.
	 */
	public void save() throws CoreException {					
		saveToFile(getTemplateFile());
	}

	private static InputStream getDefaultsAsStream() {
		URL defFile = getDefaultTemplateFile();
		if (defFile == null) return Templates.class.getResourceAsStream(DEFAULT_FILE);
		try {
            return defFile.openStream();
        } catch (IOException e) {
            return null;
        }
		
		//return Templates.class.getResourceAsStream(getDefaultTemplateFile());
	}
	/**
	 * Gets the resolved $nl$ path to the NL_DEFAULT_TEMPLATE file as a URL.
	 * If it doesn't exist, then null is returned. Calling procedures use
	 * DEFAULT_TEMPLATES if null is returned from this.
	 */
	public static URL getDefaultTemplateFile() {
    
	    Bundle bundle = Platform.getBundle("org.eclipse.cdt.ui"); //$NON-NLS-1$
		if (!BundleUtility.isReady(bundle))
			return null;

		URL fullPathString = BundleUtility.find(bundle, NL_DEFAULT_FILE);
		if (fullPathString == null) {
			try {
				fullPathString = new URL(NL_DEFAULT_FILE);
			} catch (MalformedURLException e) {
				return null;
			}
		}

		return fullPathString;
	    
	} 

	private static File getTemplateFile() {
		IPath path= CUIPlugin.getDefault().getStateLocation();
		path= path.append(TEMPLATE_FILE);
		
		return path.toFile();
	}
}

