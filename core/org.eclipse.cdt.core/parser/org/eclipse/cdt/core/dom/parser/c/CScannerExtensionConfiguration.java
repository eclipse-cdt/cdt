package org.eclipse.cdt.core.dom.parser.c;

import org.eclipse.cdt.core.dom.parser.AbstractScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.IPreprocessorDirective;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.Keywords;

public class CScannerExtensionConfiguration extends AbstractScannerExtensionConfiguration {
	private static CScannerExtensionConfiguration config = null;

	public CScannerExtensionConfiguration() {
		addPreprocessorKeyword(Keywords.cWARNING, IPreprocessorDirective.ppWarning); // C23
	}

	public static CScannerExtensionConfiguration getConfiguration(IScannerInfo info) {
		if (config == null) {
			config = new CScannerExtensionConfiguration();
		}
		return config;
	}

}
