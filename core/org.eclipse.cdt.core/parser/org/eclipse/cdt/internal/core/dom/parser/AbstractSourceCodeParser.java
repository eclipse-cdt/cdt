package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.AbstractParserLogService;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserMode;

/**
 * Abstract class for the (pure) C and C++ Parser.
 */
public abstract class AbstractSourceCodeParser implements ISourceCodeParser {

	protected final AbstractParserLogService log;

	protected boolean passing = true;

	public AbstractSourceCodeParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			INodeFactory factory, IBuiltinBindingsProvider provider) {
		this.log = this.wrapLogService(logService);
	}

	@Override
	public IASTTranslationUnit parse() {
		long start = log.isTracing() ? System.currentTimeMillis() : 0;
		processCompilationUnit();
		long end = log.isTracing() ? System.currentTimeMillis() : 0;
		resolveAmbiguities();
		IASTTranslationUnit ast = getCompilationUnit();
		if (log.isTracing()) {
			ITranslationUnit tu = ast.getOriginatingTranslationUnit();
			String name = tu == null ? "<unknown>" : tu.getElementName(); //$NON-NLS-1$
			String message = String.format("Parsed %s: %d ms%s. Ambiguity resolution: %d ms", //$NON-NLS-1$
					name, end - start, passing ? "" : " - parse failure", System.currentTimeMillis() - end); //$NON-NLS-1$//$NON-NLS-2$
			log.traceLog(message);
		}
		deleteCompilationUnit();
		ast.freeze(); // Make the AST immutable.
		return ast;
	}

	@Override
	public void cancel() {
	}

	@Override
	public boolean encounteredError() {
		return !passing;
	}

	@Override
	public IASTCompletionNode getCompletionNode() {
		return null;
	}

	protected abstract IASTTranslationUnit getCompilationUnit();

	protected abstract void deleteCompilationUnit();

	protected void processCompilationUnit() {

	}

	protected void resolveAmbiguities() {
		final IASTTranslationUnit compilationUnit = getCompilationUnit();
		if (compilationUnit instanceof ASTTranslationUnit) {
			((ASTTranslationUnit) compilationUnit).resolveAmbiguities();
		}
	}

	protected AbstractParserLogService wrapLogService(IParserLogService logService) {
		if (logService instanceof AbstractParserLogService) {
			return (AbstractParserLogService) logService;
		}
		return new ParserLogServiceWrapper(logService);
	}
}
