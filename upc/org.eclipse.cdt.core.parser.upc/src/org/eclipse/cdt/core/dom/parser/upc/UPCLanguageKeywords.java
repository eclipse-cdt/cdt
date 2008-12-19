package org.eclipse.cdt.core.dom.parser.upc;

import org.eclipse.cdt.core.dom.parser.CLanguageKeywords;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.ParserLanguage;

public class UPCLanguageKeywords extends CLanguageKeywords {

	public UPCLanguageKeywords(IScannerExtensionConfiguration config) {
		super(ParserLanguage.C, config);
	}

	private static String[] upcKeywords = UPCKeyword.getAllKeywords();
	
	@Override
	public String[] getKeywords() {
		return upcKeywords;
	}
}
