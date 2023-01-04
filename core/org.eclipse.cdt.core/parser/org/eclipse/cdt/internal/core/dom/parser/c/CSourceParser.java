package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.AbstractSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.BacktrackException;
import org.eclipse.cdt.internal.core.dom.parser.DeclarationOptions;

/**
 * Source Parser for the C Language Syntax.
 */
public class CSourceParser extends AbstractSourceCodeParser {

	private IASTTranslationUnit compilationUnit;
	private IIndex index;

	public CSourceParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			ICParserExtensionConfiguration config, IIndex index) {
		super(scanner, parserMode, logService, CNodeFactory.getDefault(), config.getBuiltinBindingsProvider());
		this.index = index;
	}

	@Override
	protected IASTTranslationUnit getCompilationUnit() {
		return compilationUnit;
	}

	@Override
	protected void createCompilationUnit() throws Exception {
		compilationUnit = nodeFactory.newTranslationUnit(scanner);
		compilationUnit.setIndex(index);

		// add built-in names to the scope
		if (builtinBindingsProvider != null) {
			if (compilationUnit instanceof ASTTranslationUnit) {
				((ASTTranslationUnit) compilationUnit).setupBuiltinBindings(builtinBindingsProvider);
			}
		}
	}

	@Override
	protected void destroyCompilationUnit() {
		compilationUnit = null;
	}

	@Override
	protected IASTDeclaration declaration(DeclarationOptions option) throws BacktrackException, EndOfFileException {
		return null;
	}

}
