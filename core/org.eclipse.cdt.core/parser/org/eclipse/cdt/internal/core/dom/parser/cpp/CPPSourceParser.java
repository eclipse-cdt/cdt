package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.dom.parser.AbstractSourceCodeParser;

/**
 * Source Parser for the C++ Language Syntax.
 */
public class CPPSourceParser extends AbstractSourceCodeParser {

	public CPPSourceParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			ICPPParserExtensionConfiguration config, IIndex index) {
		super(scanner, parserMode, logService, CPPNodeFactory.getDefault(), config.getBuiltinBindingsProvider());
	}

}
