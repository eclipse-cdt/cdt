package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.AbstractParserLogService;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserMode;

/**
 * Abstract class for the regular C and C++ Parser.
 */
public abstract class AbstractSourceCodeParser implements ISourceCodeParser {

	protected final AbstractParserLogService log;
	protected final IScanner scanner;

	protected boolean passing = true;
	protected ASTCompletionNode completionNode;

	public AbstractSourceCodeParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			INodeFactory factory, IBuiltinBindingsProvider provider) {
		this.scanner = scanner;
		this.log = wrapLogService(logService);
	}

	@Override
	public IASTTranslationUnit parse() {
		long start = log.isTracing() ? System.currentTimeMillis() : 0;
		processCompilationUnit();
		long end = log.isTracing() ? System.currentTimeMillis() : 0;
		resolveAmbiguities();
		IASTTranslationUnit ast = getCompilationUnit();
		if (log.isTracing()) {
			ITranslationUnit unit = ast.getOriginatingTranslationUnit();
			String name = unit == null ? "<unknown>" : unit.getElementName(); //$NON-NLS-1$
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
		return completionNode;
	}

	protected abstract IASTTranslationUnit getCompilationUnit();

	protected abstract void createCompilationUnit() throws Exception;

	protected abstract void deleteCompilationUnit();

	protected void processCompilationUnit() {
		try {
			createCompilationUnit();
		} catch (Exception exception) {
			logException("translationUnit::createCompilationUnit()", exception); //$NON-NLS-1$
			return;
		}
		parseCompilationUnit();
	}

	protected void parseCompilationUnit() {

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

	protected void logException(String methodName, Exception exception) {
		if (!(exception instanceof EndOfFileException) && exception != null) {
			if (log.isTracing()) {
				String message = String.format("Parser: Unexpected exception in %s:%s::%s. w/%s", //$NON-NLS-1$
						methodName, exception.getClass().getName(), exception.getMessage(), scanner);
				log.traceLog(message);
			}
			log.traceException(exception);
		}
	}
}
