package org.eclipse.cdt.ui.newui;

import java.util.Comparator;

import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.ui.newui.AbstractExportTab.ExtData;

public class CDTListComparator implements Comparator {
	private static CDTListComparator comparator = null;

	public static CDTListComparator getInstance() {
		if (comparator == null)
			comparator = new CDTListComparator();
		return comparator;
	}
	public int compare(Object a, Object b) {
		if (a == null || b == null) 
			return 0;
		if (a instanceof ICLanguageSetting) {
			ICLanguageSetting c1 = (ICLanguageSetting)a;
			ICLanguageSetting c2 = (ICLanguageSetting)b;
			return c1.getName().compareToIgnoreCase(c2.getName());
		} 
		if (a instanceof ICLanguageSettingEntry) {
			ICLanguageSettingEntry c1 = (ICLanguageSettingEntry)a;
			ICLanguageSettingEntry c2 = (ICLanguageSettingEntry)b;
			return c1.getName().compareToIgnoreCase(c2.getName());
		} 
		if (a instanceof ICConfigurationDescription) {
			ICConfigurationDescription c1 = (ICConfigurationDescription)a;
			ICConfigurationDescription c2 = (ICConfigurationDescription)b;
			return c1.getName().compareToIgnoreCase(c2.getName());
		}
		if (a instanceof ExtData) {
			ExtData c1 = (ExtData)a;
			ExtData c2 = (ExtData)b;
			return c1.getName().compareToIgnoreCase(c2.getName());
		} 
		if (a instanceof ICdtVariable) {
			ICdtVariable c1 = (ICdtVariable) a;
			ICdtVariable c2 = (ICdtVariable) b;
			return c1.getName().compareToIgnoreCase(c2.getName());			
		}
		if (a instanceof IConfigurationElement) {
			IConfigurationElement e1 = (IConfigurationElement)a;
			IConfigurationElement e2 = (IConfigurationElement)b;
			return AbstractPage.getWeight(e1).compareTo(AbstractPage.getWeight(e2)); 
		}
		
		return a.toString().compareTo(b.toString());
	}

}
