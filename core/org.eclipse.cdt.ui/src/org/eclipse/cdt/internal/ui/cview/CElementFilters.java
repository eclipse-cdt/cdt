package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.internal.ui.util.StringMatcher;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;

/**
 * The FiltersContent provides the elements for use by the list dialog
 * for selecting the patterns to apply.
 */ 
public class CElementFilters {
	static List definedFilters = null;
	static List defaultFilters = null;
	static StringMatcher [] matchers = null;
	static String FILTERS_TAG= "CElementFilters"; //$NON-NLS-1$
	static String COMMA_SEPARATOR = ","; //$NON-NLS-1$

	/**
	 * Returns the filters which are enabled by default.
	 *
	 * @return a list of strings
	 */
	public static List getDefaultFilters() {
		if (defaultFilters == null) {
			readFilters();
		}
		return defaultFilters;
	}

	/**
	 * Returns the filters currently defined for the workbench. 
	 */
	public static List getDefinedFilters() {
		if (definedFilters == null) {
			// Overide the default by the user preference
			CUIPlugin plugin = CUIPlugin.getDefault();
			String storedPatterns= plugin.getPluginPreferences().getString(FILTERS_TAG);

			if (storedPatterns.length() > 0) {
				StringTokenizer entries = new StringTokenizer(storedPatterns, COMMA_SEPARATOR);
				definedFilters = new ArrayList();

				while (entries.hasMoreElements()) {
					String nextToken = entries.nextToken();
					definedFilters.add(nextToken);
				}
			} else {
				readFilters();
			}
		}
		return definedFilters;
	}

	public static StringMatcher [] getMatchers() {
		if (matchers == null) {
			List list = getDefinedFilters();
			matchers = new StringMatcher[list.size()];
			for (int i = 0; i < matchers.length; i++) {
				matchers[i] = new StringMatcher((String)(list.get(i)), true, false);
			}
		}
		return matchers;
	}

	/**
     * Define new Patterns for the Duration of the session.
     */
    public static void setPatterns(String[] newPatterns) {
		//System.out.println ("SetPatterns call");
        matchers = new StringMatcher[newPatterns.length];
        for (int i = 0; i < newPatterns.length; i++) {
			//System.out.println ("Patterns " + newPatterns[i]);
            matchers[i] = new StringMatcher(newPatterns[i], true, false);
        }
		//CElementFactory.getDefault().refreshDeadBranchParents();
    }


	public static boolean match(String name) {
		StringMatcher [] m = getMatchers();
		if (m == null)
			return false;
		//System.out.println ("Pattern " + name);
		for (int i = 0; i < m.length; i++) {
			if (m[i].match(name)) {
				//System.out.println ("Match " + name);
				return true;
			}
		}
		return false;
	}

	private CElementFilters() {
	}
	/**
 	 * Reads the filters currently defined for the workbench. 
 	 */
	static void readFilters() {
		definedFilters = new ArrayList();
		defaultFilters = new ArrayList();
		CUIPlugin plugin = CUIPlugin.getDefault();
		if (plugin != null) {
			IExtensionPoint extension = plugin.getDescriptor().getExtensionPoint(FILTERS_TAG);
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for(int i = 0; i < extensions.length; i++){
					IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
					for(int j = 0; j < configElements.length; j++){
						String pattern = configElements[j].getAttribute("pattern"); //$NON-NLS-1$
						if (pattern != null)
							definedFilters.add(pattern);
						String selected = configElements[j].getAttribute("selected"); //$NON-NLS-1$
						if (selected != null && selected.equalsIgnoreCase("true")) //$NON-NLS-1$
							defaultFilters.add(pattern);
					}
				}
			}		
		}
	}
}
