package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.parser.AbstractCLikeLanguage;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.cpp.CPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;

/**
 * ILanguage implementation for the DOM no-dialect, pure C++ Parser.
 */
public class CPPLanguage extends AbstractCLikeLanguage implements IActualCPPLanguage {

	@Override
	public String getId() {
		// Must match the id in the extension
		return CCorePlugin.PLUGIN_ID + ".c++"; //$NON-NLS-1$
	}

	@Override
	public int getLinkageID() {
		return ILinkage.CPP_LINKAGE_ID;
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration(IScannerInfo info) {
		return CPPScannerExtensionConfiguration.getConfiguration(info);
	}

	@Override
	protected ISourceCodeParser createParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			IIndex index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ParserLanguage getParserLanguage() {
		return ParserLanguage.CPP;
	}

}
