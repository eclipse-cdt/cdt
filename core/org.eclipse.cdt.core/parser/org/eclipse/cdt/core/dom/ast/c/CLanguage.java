package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.parser.AbstractCLikeLanguage;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;

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
	protected ISourceCodeParser createParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			IIndex index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ParserLanguage getParserLanguage() {
		return ParserLanguage.C;
	}

}
