package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The FiltersContentProvider provides the elements for use by the list dialog
 * for selecting the patterns to apply.
 */ 
class FiltersContentProvider implements IStructuredContentProvider {
	private static List fgDefinedFilters;
	private static List fgDefaultFilters;

	private CPatternFilter filter; 
	
	/**
	 * Disposes of this content provider.  
	 * This is called by the viewer when it is disposed.
	 */
	public void dispose() {}
	/**
	 * Returns the filters which are enabled by default.
	 *
	 * @return a list of strings
	 */
	public static List getDefaultFilters() {
		if (fgDefaultFilters == null) {
			readFilters();
		}
		return fgDefaultFilters;
	}
	/**
	 * Returns the filters currently defined for the workbench. 
	 */
	public static List getDefinedFilters() {
		if (fgDefinedFilters == null) {
			readFilters();
		}
		return fgDefinedFilters;
	}
	/* (non-Jaadoc)
	 * Method declared in IStructuredContentProvider.
	 */
	public Object[] getElements(Object inputElement) {
		return getDefinedFilters().toArray();
	}
	/**
	 * Return the initially selected values
	 * @return java.lang.String[]
	 */
	public String[] getInitialSelections() {
		return filter.getPatterns();
	}
	/* (non-Javadoc)
	 * Method declared on IContentProvider.
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	/**
	 * Create a FiltersContentProvider using the selections from the suppliec
	 * resource filter.
	 */
	public FiltersContentProvider(CPatternFilter filter) {
		this.filter= filter;
	}
	/**
 	 * Reads the filters currently defined for the workbench. 
 	 */
	private static void readFilters() {
		fgDefinedFilters = new ArrayList();
		fgDefaultFilters = new ArrayList();
		CUIPlugin plugin = CUIPlugin.getDefault();
		if (plugin != null) {
			IExtensionPoint extension = plugin.getDescriptor().getExtensionPoint(CPatternFilter.FILTERS_TAG);
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for(int i = 0; i < extensions.length; i++){
					IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
					for(int j = 0; j < configElements.length; j++){
						String pattern = configElements[j].getAttribute("pattern"); //$NON-NLS-1$
						if (pattern != null)
							fgDefinedFilters.add(pattern);
						String selected = configElements[j].getAttribute("selected"); //$NON-NLS-1$
						if (selected != null && selected.equalsIgnoreCase("true")) //$NON-NLS-1$
							fgDefaultFilters.add(pattern);
					}
				}
			}		
		}
	}
}
