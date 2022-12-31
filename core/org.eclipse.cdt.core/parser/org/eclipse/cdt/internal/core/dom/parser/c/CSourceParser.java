package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserMode;

/**
 * Source Parser for the C Language Syntax.
 */
public class CSourceParser implements ISourceCodeParser {

	public CSourceParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			ICParserExtensionConfiguration config, IIndex index) {

	}

	@Override
	public IASTTranslationUnit parse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean encounteredError() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IASTCompletionNode getCompletionNode() {
		// TODO Auto-generated method stub
		return null;
	}

}
