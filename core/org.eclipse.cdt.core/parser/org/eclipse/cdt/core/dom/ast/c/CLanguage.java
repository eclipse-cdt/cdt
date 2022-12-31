package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.parser.AbstractCLikeLanguage;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.CScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.dom.parser.c.CSourceParser;

/**
 * ILanguage implementation for the DOM no-dialect, pure C Parser.
 */
public class CLanguage extends AbstractCLikeLanguage implements IActualCLanguage {

	@Override
	public String getId() {
		// Must match the id in the extension
		return CCorePlugin.PLUGIN_ID + ".c"; //$NON-NLS-1$
	}

	@Override
	public int getLinkageID() {
		return ILinkage.C_LINKAGE_ID;
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration(IScannerInfo info) {
		return CScannerExtensionConfiguration.getConfiguration(info);
	}

	@Override
	protected ISourceCodeParser createParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			IIndex index) {
		return new CSourceParser(scanner, parserMode, logService, new ANSICParserExtensionConfiguration(), index);
	}

	@Override
	protected ParserLanguage getParserLanguage() {
		return ParserLanguage.C;
	}

}
