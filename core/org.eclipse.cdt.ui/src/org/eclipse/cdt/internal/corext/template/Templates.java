package org.eclipse.cdt.internal.corext.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.ui.CUIPlugin;

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jface.dialogs.ErrorDialog;

/**
 * <code>Templates</code> gives access to the available templates.
 */
public class Templates extends TemplateSet {

	private static final String DEFAULT_FILE= "default-templates.xml"; //$NON-NLS-1$
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
			CUIPlugin.log(e);
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
		return Templates.class.getResourceAsStream(DEFAULT_FILE);
	}

	private static File getTemplateFile() {
		IPath path= CUIPlugin.getDefault().getStateLocation();
		path= path.append(TEMPLATE_FILE);
		
		return path.toFile();
	}
}

