package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.List;
import java.util.Vector;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.cdt.internal.ui.util.StringMatcher;
import org.eclipse.cdt.internal.ui.CPlugin;

import org.eclipse.cdt.core.model.ICFolder;
import org.eclipse.cdt.core.model.ICProject;

public class CPatternFilter extends ViewerFilter {
	private String[] patterns;
	private StringMatcher[] matchers;

	static String COMMA_SEPARATOR = ","; //$NON-NLS-1$
	static String FILTERS_TAG= "CElementFilters"; //$NON-NLS-1$

	private void initializeFromPreferences() {
		CPlugin plugin= CPlugin.getDefault();
		String storedPatterns= plugin.getPreferenceStore().getString(FILTERS_TAG);

		if (storedPatterns.length() == 0) {
			List defaultFilters= FiltersContentProvider.getDefaultFilters();
			String[] patterns= new String[defaultFilters.size()];
			defaultFilters.toArray(patterns);
			setPatterns(patterns);
			return;
		}

		//Get the strings separated by a comma and filter them from the currently
		//defined ones

		List definedFilters = FiltersContentProvider.getDefinedFilters();

		StringTokenizer entries = new StringTokenizer(storedPatterns, COMMA_SEPARATOR);
		Vector patterns = new Vector();

		while (entries.hasMoreElements()) {
			String nextToken = entries.nextToken();
			if (definedFilters.indexOf(nextToken) > -1)
				patterns.addElement(nextToken);
		}

		//Convert to an array of Strings
		String[] patternArray = new String[patterns.size()];
		patterns.toArray(patternArray);
		setPatterns(patternArray);
	}

	/**
	 * Gets the patterns for the receiver. Returns the cached values if there
	 * are any - if not look it up.
	 */
	public String[] getPatterns() {
		if (patterns == null) {
			initializeFromPreferences();
		}
		return patterns;
	}

	/**
	 * Sets the patterns to filter out for the receiver.
	 */
	public void setPatterns(String[] newPatterns) {

		patterns = newPatterns;
		matchers = new StringMatcher[newPatterns.length];
		for (int i = 0; i < newPatterns.length; i++) {
			//Reset the matchers to prevent constructor overhead
			matchers[i] = new StringMatcher(newPatterns[i], true, false);
		}
	}


	/**
	 * Return the currently configured StringMatchers. If there aren't any look
	 * them up.
	 */
	private StringMatcher[] getMatchers() {
		if (matchers == null)
			initializeFromPreferences();
		return matchers;
	}

	/* (non-Javadoc)
	 * Method declared on ViewerFilter.
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		IResource resource = null;
		if (element instanceof IResource) {
			resource = (IResource) element;
		} else if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			resource = (IResource) adaptable.getAdapter(IResource.class);
		}

		// Only apply the rule for Projects and folders. 
		if (parentElement instanceof ICProject
			|| parentElement instanceof ICFolder) {
			if (resource != null) {
				String name = resource.getName();
				StringMatcher[] testMatchers = getMatchers();
				for (int i = 0; i < testMatchers.length; i++) {
					if (testMatchers[i].match(name))
						return false;
				}
			}
		}
		return true;
	}
}
