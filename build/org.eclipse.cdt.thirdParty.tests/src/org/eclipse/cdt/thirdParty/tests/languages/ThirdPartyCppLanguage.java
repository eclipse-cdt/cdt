package org.eclipse.cdt.thirdParty.tests.languages;

import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;

public class ThirdPartyCppLanguage extends GPPLanguage {
	
	protected final static GPPScannerExtensionConfiguration THIRD_PARTY_CPP_SCANNER_CONFIG= new GPPScannerExtensionConfiguration();
	protected final static GPPParserExtensionConfiguration THIRD_PARTY_CPP_PARSER_CONFIG= new GPPParserExtensionConfiguration();

	private final static String DEFAULT_ID = "org.eclipse.cdt.thirdParty.tests.thirdPartyCpp"; //$NON-NLS-1$

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return THIRD_PARTY_CPP_SCANNER_CONFIG;
	}
	
	@Override
	protected ICPPParserExtensionConfiguration getParserExtensionConfiguration() {
		return THIRD_PARTY_CPP_PARSER_CONFIG;
	}

	@Override
	public String getId() {
		return DEFAULT_ID;
	}

	@Override
	public String getName() {
		return "Third Party C++";
	}	
}
