package org.eclipse.cdt.thirdParty.tests.languages;

import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;

public class ThirdPartyCLanguage extends GCCLanguage {
	
	protected final static GCCScannerExtensionConfiguration THIRD_PARTY_C_SCANNER_CONFIG= new GCCScannerExtensionConfiguration();
	protected final static GCCParserExtensionConfiguration THIRD_PARTY_C_PARSER_CONFIG= new GCCParserExtensionConfiguration();

	private final static String DEFAULT_ID = "org.eclipse.cdt.thirdParty.tests.thirdPartyC"; //$NON-NLS-1$

	@Override
	protected ICParserExtensionConfiguration getParserExtensionConfiguration() {
		return THIRD_PARTY_C_PARSER_CONFIG;
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return THIRD_PARTY_C_SCANNER_CONFIG;
	}

	@Override
	public String getId() {
		return DEFAULT_ID;
	}

	@Override
	public String getName() {
		return "Third Party C";
	}
}
