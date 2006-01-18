package org.eclipse.cdt.ui.tests.text.contentassist;

import org.eclipse.cdt.internal.ui.preferences.ProposalFilterPreferencesUtil;
import org.eclipse.cdt.internal.ui.preferences.ProposalFilterPreferencesUtil.ComboState;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.IPreferenceStore;

import junit.framework.TestCase;

/** 
 * This test covers the convenience methods 
 * in org.eclipse.cdt.internal.ui.preferences.ProposalFilterPreferencesUtil
 */
public class ProposalFilterPreferencesTest extends TestCase {

	public void testPreferences()  {
		// Check that the test filter is among the filternames
		String[] filterNames = ProposalFilterPreferencesUtil.getProposalFilterNames();
		int index = -1 ;
		for (int i = 0; i < filterNames.length; i++) {
			String name = filterNames[i];
			if (name.equals("Testing Completion Filter"))  {
				index = i ;
				break ;
			}
		}
		assertTrue("Did not find expected filter!", index>=0);
		
		// Set the preference to the tested filter 
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		String filterComboStateString = store.getString(ContentAssistPreference.PROPOSALS_FILTER);
		ProposalFilterPreferencesUtil.ComboState state = ProposalFilterPreferencesUtil.getComboState(filterComboStateString);
		StringBuffer newStateText = new StringBuffer();
		newStateText.append(index+1); // First entry is always the <Default Filter>, index+1 must be selected
		for (int i = 0; i < state.items.length; i++) {
			String item = state.items[i];
			newStateText.append(";");
			newStateText.append(item);
		}
		store.setValue(ContentAssistPreference.PROPOSALS_FILTER, newStateText.toString());
		
		// Now we can test preferred filter retrieval:
		IConfigurationElement preferredElement = ProposalFilterPreferencesUtil.getPreferredFilterElement();
		String extensionId = preferredElement.getAttribute("id");
		assertNotNull("Configuration element was not found!", extensionId);
		assertEquals("Unexpected element id", "org.eclipse.cdt.ui.tests.TestProposalFilter", extensionId);
	}
}
