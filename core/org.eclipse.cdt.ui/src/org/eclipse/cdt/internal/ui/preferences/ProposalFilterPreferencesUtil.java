/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Combo;

/**
 * A class which encapsulates several utility functions
 * related to code completion preference settings.
 */
public class ProposalFilterPreferencesUtil {

	/**
	 * Private default constructor prevents instantiation
	 */
	private ProposalFilterPreferencesUtil() {
	}

	/**
	 * Get an array of proposal filter names
	 * (i.e. the human-readable text for display
	 * to fill into the Combo)
	 */
	public static String[] getProposalFilterNames() {
		ArrayList names = new ArrayList();
		try {
			IExtensionPoint point = Platform.getExtensionRegistry()
					.getExtensionPoint(CUIPlugin.PLUGIN_ID, "ProposalFilter"); //$NON-NLS-1$
			if (point != null) {
				IExtension[] extensions = point.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IExtension extension = extensions[i];
					IConfigurationElement[] elements = extension
							.getConfigurationElements();
					for (int j = 0; j < elements.length; ++j) {
						IConfigurationElement element = elements[j];
						if ("ProposalFilter".equals(element.getName())) { //$NON-NLS-1$
							String filterName = element.getAttribute("name");
							if (null != filterName) {
								names.add(filterName);
							}
						}
					}
				}
			}
		} catch (InvalidRegistryObjectException e) {
			// No action required since we will at least be using the fail-safe default filter
			CUIPlugin.getDefault().log(e);
		}
		String[] filterNames = (String[]) names
				.toArray(new String[names.size()]);
		return filterNames;
	}

	/**
	 * Look up all contributed completion proposal filters 
	 * and return their names as a semicolon-separated list
	 * plus a leading entry for the selected index 0,
	 * plus a leading <default> entry. <br>
	 * A Combo may be initialized from this string.
	 * @return The list of filter names
	 */
	public static String getProposalFilternamesAsString() {
		StringBuffer filterNames = new StringBuffer("0;");
		filterNames.append("<Default Filter>"); // TODO: NP externalize this!
		String[] names = getProposalFilterNames();
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			filterNames.append(";");
			filterNames.append(name);
		}
		return filterNames.toString();
	}

	/**
	 * Return the configuration element which corresponds 
	 * to the human-readable filter name
	 * @param filterName The human-readable filter name
	 * @return The configuration element, or null if there is none
	 */
	public static IConfigurationElement getElementForName(String filterName) {
		IConfigurationElement element = null;
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(CUIPlugin.PLUGIN_ID, "ProposalFilter"); //$NON-NLS-1$
		if (point != null) {
			try {
				IExtension[] extensions = point.getExtensions();
				if (extensions.length >= 1) {
					for (int i = 0; i < extensions.length; i++) {
						IExtension extension = extensions[i];

						IConfigurationElement[] elements = extension
								.getConfigurationElements();

						for (int j = 0; j < elements.length; ++j) {
							IConfigurationElement testElement = elements[j];
							if ("ProposalFilter".equals(testElement.getName())) { //$NON-NLS-1$
								String testName = testElement
										.getAttribute("name");
								if ((null != testName)
										&& (filterName.equals(testName))) {
									element = testElement;
									break;
								}
							}
						}
						// Did we find the corresponding element?
						if (null != element)
							break;
					}
				}
			} catch (InvalidRegistryObjectException e) {
				// In case of failure we'll just return null
			}
		}

		return element;
	}

	/**
	 * The state of a Combo consists of the list of entries
	 * and the index of the selected entry.
	 * This method converts the state of the given Combo 
	 * to a string representation for storage in a preference store. <br>
	 * The string contains a semicolon-separated list of entries.
	 * The first entry is the index of the selected entry.
	 * The following entries are the texts of the individual fields. <br>
	 * Since the semicolon is the separator, the entries cannot contain semicolons.
	 * This method will replace semicolons with commas if any are found.
	 * @param combo The Combo whose state shall be converted
	 * @return A string representation of the Combo state
	 */
	public static String comboStateAsString(Combo combo) {
		StringBuffer text = new StringBuffer();
		int selectionIndex = combo.getSelectionIndex();
		text.append(selectionIndex);
		String[] entries = combo.getItems();
		for (int i = 0; i < entries.length; i++) {
			text.append(";");
			String entry = entries[i].replaceAll(";", ",");
			text.append(entry);
		}
		return text.toString();
	}

	/**
	 * The state of a Combo consists of the list of entries
	 * and the index of the selected entry.
	 * This method takes a string representation of the state (e.g. from a preference store)
	 * and restores it into the Combo. <br>
	 * For a description of the text format see method comboStateAsString().
	 * @param combo The combo to be restored.
	 * @param text The text representation of the state.
	 */
	public static void restoreComboFromString(Combo combo, String text) {
		try {
			int endFirstEntry = text.indexOf(";");
			if (endFirstEntry > 0) { // First entry must contain at least one character
				String selectedString = text.substring(0, endFirstEntry);
				int selectedIndex = Integer.parseInt(selectedString);
				String[] entryList = text.substring(endFirstEntry + 1,
						text.length()).split(";");
				combo.setItems(entryList);
				combo.select(selectedIndex);
			}
		} catch (NumberFormatException e) {
			// If this fails we just return the unmodified Combo
		}
	}

	/**
	 * Convenience class wraps the data to initialize a Combo
	 */
	public static class ComboState {
		public int selectedIndex;

		public String[] items;
	}

	/** 
	 * Convenience method to extract the state of a Combo
	 * from the state string stored e.g. in a preference store
	 * @param comboPreference The state string
	 * @return A ComboState instance. 
	 */
	public static ComboState getComboState(String comboPreference) {
		ComboState state = new ComboState();
		try {
			int endFirstEntry = comboPreference.indexOf(";");
			if (endFirstEntry > 0) { // First entry must contain at least one character
				String selectedString = comboPreference.substring(0,
						endFirstEntry);
				state.selectedIndex = Integer.parseInt(selectedString);
				state.items = comboPreference.substring(endFirstEntry + 1,
						comboPreference.length()).split(";");
			}
		} catch (NumberFormatException e) {
			// If this fails we return an empty ComboState
			state.items = new String[0];
		}
		return state;
	}

	/**
	 * Look up the setting for the preferred proposal filter
	 * and return it's configuration element.
	 * @return The configuration element, or null if none is found.
	 */
	public static IConfigurationElement getPreferredFilterElement() {
		IConfigurationElement preferredElement = null;
		try {
			IPreferenceStore store = CUIPlugin.getDefault()
					.getPreferenceStore();
			String filterComboStateString = store
					.getString(ContentAssistPreference.PROPOSALS_FILTER);
			ComboState state = getComboState(filterComboStateString);
			preferredElement = getElementForName(state.items[state.selectedIndex]);
		} catch (Exception e) {
			// If anything goes wrong we'll just return null
		}
		return preferredElement;
	}

}
