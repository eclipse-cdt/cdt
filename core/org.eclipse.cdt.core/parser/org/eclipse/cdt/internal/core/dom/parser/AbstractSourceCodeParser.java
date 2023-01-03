package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationListOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.AbstractParserLogService;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IInactiveCodeToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * Abstract class for the regular C and C++ Parser.
 */
public abstract class AbstractSourceCodeParser implements ISourceCodeParser {

	protected final AbstractParserLogService log;
	protected final IScanner scanner;
	protected final ParserMode mode;
	protected final INodeFactory nodeFactory;

	protected boolean passing = true;
	protected boolean cancelled = false;
	protected IToken declarationMark;
	protected IToken nextToken;
	protected IToken lastTokenFromScanner;
	protected ASTCompletionNode completionNode;

	private boolean activeCode = true;

	public AbstractSourceCodeParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			INodeFactory factory, IBuiltinBindingsProvider provider) {
		this.scanner = scanner;
		this.nodeFactory = factory;
		this.log = wrapLogService(logService);
		this.mode = parserMode;
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
		destroyCompilationUnit();
		ast.freeze(); // Make the AST immutable.
		return ast;
	}

	@Override
	public void cancel() {
		cancelled = true;
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

	protected abstract void destroyCompilationUnit();

	protected abstract IASTDeclaration declaration(DeclarationOptions option)
			throws BacktrackException, EndOfFileException;

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
		declarationList(getCompilationUnit(), DeclarationOptions.GLOBAL, false, 0);
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

	protected ASTCompletionNode createCompletionNode(IToken token) {
		// the preprocessor may deliver tokens for literals or header-names.
		if (completionNode == null && token != null && token.getType() == IToken.tCOMPLETION) {
			completionNode = new ASTCompletionNode(token, getCompilationUnit());
		}
		return completionNode;
	}

	protected IASTProblemDeclaration skipProblemDeclaration(int offset) {
		return skipProblemDeclaration(offset, null);
	}

	protected IASTProblemDeclaration skipProblemDeclaration(int offset, IASTProblem origProblem) {
		passing = false;
		declarationMark = null;
		int endOffset = skipToSemiOrClosingBrace(offset, false);
		IASTProblem problem = createProblem(IProblem.SYNTAX_ERROR, offset, endOffset - offset);
		if (origProblem != null && origProblem.getID() != IProblem.SYNTAX_ERROR) {
			problem.setOriginalProblem(origProblem);
		}
		return buildProblemDeclaration(problem);
	}

	protected IASTProblem createProblem(BacktrackException exception) {
		IASTProblem result = exception.getProblem();
		if (result == null) {
			result = createProblem(IProblem.SYNTAX_ERROR, exception.getOffset(), exception.getLength());
		}
		return result;
	}

	protected IASTDeclaration[] problemDeclaration(int offset, BacktrackException backtrack,
			DeclarationOptions option) {
		passing = false;
		IASTProblem origProblem = createProblem(backtrack);

		// a node was detected by assuming additional tokens (e.g. missing semicolon)
		IASTNode n = backtrack.getNodeBeforeProblem();
		if (n instanceof IASTDeclaration) {
			IToken la1 = lookaheadWithEndOfFile(1);
			if (la1 == null || la1.getOffset() > offset) {
				declarationMark = null;
				return new IASTDeclaration[] { (IASTDeclaration) n, buildProblemDeclaration(origProblem) };
			}
		}

		if (declarationMark != null && activeCode) {
			IASTDeclaration trailingProblem = null;
			offset = declarationMark.getOffset();

			// try to skip identifiers (undefined macros?)
			IASTDeclaration decl = null;
			int endOffset = 0;
			loop: while (declarationMark != null && declarationMark.getType() == IToken.tIDENTIFIER) {
				endOffset = declarationMark.getEndOffset();
				declarationMark = declarationMark.getNext();
				if (declarationMark != null) {
					backup(declarationMark);
					// avoid creating an empty declaration
					switch (lookaheadTypeWithEndOfFile(1)) {
					case 0: // eof
					case IToken.tEOC:
					case IToken.tSEMI:
						break loop;
					}
					try {
						decl = declaration(option);
						break;
					} catch (BacktrackException exception) {
						n = exception.getNodeBeforeProblem();
						if (n instanceof IASTDeclaration) {
							decl = (IASTDeclaration) n;
							trailingProblem = buildProblemDeclaration(exception.getProblem());
							break;
						}
					} catch (EndOfFileException exception) {
						endOffset = getEndOffset();
						break;
					}
				}
			}
			declarationMark = null;

			if (decl != null) {
				IASTProblem problem = createProblem(IProblem.SYNTAX_ERROR, offset, endOffset - offset);
				IASTDeclaration pd = buildProblemDeclaration(problem);
				if (trailingProblem != null)
					return new IASTDeclaration[] { pd, decl, trailingProblem };
				return new IASTDeclaration[] { pd, decl };
			}
		}

		return new IASTDeclaration[] { skipProblemDeclaration(offset, origProblem) };
	}

	/**
	 * Roll back to a previous point, reseting the queue of tokens.
	 * @param mark a token previously obtained via {@link #mark()}.
	 */
	protected final void backup(IToken mark) {
		nextToken = mark;
	}

	protected final IASTProblem createProblem(int signal, int offset, int length) {
		IASTProblem result = nodeFactory.newProblem(signal, CharArrayUtils.EMPTY, true);
		((ASTNode) result).setOffsetAndLength(offset, length);
		return result;
	}

	protected final int getCodeBranchNesting() {
		return scanner.getCodeBranchNesting();
	}

	protected final int getEndOffset() {
		if (lastTokenFromScanner == null)
			return 0;
		return lastTokenFromScanner.getEndOffset();
	}

	/**
	 * Returns the next token without advancing. Same as {@code LA(1)}.
	 */
	protected final IToken lookahead() throws EndOfFileException {
		IToken token = nextToken(true);
		checkEndOfInactive(token);
		return token;
	}

	/**
	 * Returns one of the next tokens. With {@code i == 1}, the next token is returned.
	 * @param i number of tokens to look ahead, must be greater than 0.
	 */
	protected final IToken lookahead(int i) throws EndOfFileException {
		IToken token = lookaheadToken(i, true);
		checkEndOfInactive(token);
		return token;
	}

	protected final IToken lookaheadWithEndOfFile(int i) {
		try {
			return lookahead(i);
		} catch (EndOfFileException exception) {
			return null;
		}
	}

	/**
	 * Look ahead in the token list and return the token type.
	 * @param i number of tokens to look ahead, must be greater or equal to 0.
	 * @return The type of that token
	 */
	protected final int lookaheadType(int i) throws EndOfFileException {
		return lookahead(i).getType();
	}

	protected final int lookaheadTypeWithEndOfFile(int i) {
		try {
			return lookaheadType(i);
		} catch (EndOfFileException exception) {
			return 0;
		}
	}

	protected final void skipInactiveCode() throws OffsetLimitReachedException {
		IToken token = nextToken;
		if (activeCode && (token == null || token.getType() != IToken.tINACTIVE_CODE_START))
			return;
		try {
			activeCode = true;
			while (token != null && token.getType() != IToken.tINACTIVE_CODE_END) {
				token = token.getNext();
			}

			if (token != null) {
				nextToken = token.getNext();
			} else {
				nextToken = null;
				scanner.skipInactiveCode();
			}
		} catch (OffsetLimitReachedException exception) {
			if (mode == ParserMode.COMPLETION_PARSE) {
				createCompletionNode(exception.getFinalToken());
				throw exception;
			}
		}
	}

	private final void checkEndOfInactive(IToken token) throws EndOfFileException {
		final int type = token.getType();
		if (type == IToken.tINACTIVE_CODE_SEPARATOR || type == IToken.tINACTIVE_CODE_END)
			throw new EndOfFileException(token.getOffset(), true);
	}

	protected final IToken consume() throws EndOfFileException {
		IToken token = nextToken(true);
		checkEndOfInactive(token);

		nextToken = token.getNext();
		return token;
	}

	protected final boolean acceptInactiveCodeBoundary(int nesting) {
		try {
			while (true) {
				IToken token = nextToken(false);
				switch (token.getType()) {
				case IToken.tINACTIVE_CODE_START:
				case IToken.tINACTIVE_CODE_SEPARATOR:
					IInactiveCodeToken it = (IInactiveCodeToken) token;
					if (it.getNewNesting() < nesting
							|| (it.getNewNesting() == nesting && it.getOldNesting() == nesting)) {
						return false;
					}
					activeCode = false;
					nextToken = token.getNext(); // consume the token
					continue;
				case IToken.tINACTIVE_CODE_END:
					it = (IInactiveCodeToken) token;
					if (it.getNewNesting() < nesting
							|| (it.getNewNesting() == nesting && it.getOldNesting() == nesting)) {
						return false;
					}
					activeCode = true;
					nextToken = token.getNext(); // consume the token
					continue;
				default:
					return true;
				}
			}
		} catch (EndOfFileException exception) {
		}
		return true;
	}

	private int skipToSemiOrClosingBrace(int offset, boolean eatBrace) {
		passing = false;
		declarationMark = null;
		int depth = 0;
		int endOffset;
		loop: try {
			endOffset = lookahead(1).getOffset();
			while (true) {
				switch (lookaheadType(1)) {
				case IToken.tEOC:
					endOffset = getEndOffset();
					break loop;
				case IToken.tSEMI:
					if (depth == 0) {
						endOffset = consume().getEndOffset();
						break loop;
					}
					break;
				case IToken.tLBRACE:
					++depth;
					break;
				case IToken.tRBRACE:
					if (--depth <= 0) {
						if (depth == 0 || offset == endOffset || eatBrace) {
							endOffset = consume().getEndOffset(); // consume closing brace
						}
						if (lookaheadTypeWithEndOfFile(1) == IToken.tSEMI) {
							endOffset = consume().getEndOffset();
						}
						break loop;
					}
					break;
				}
				endOffset = consume().getEndOffset();
			}
		} catch (EndOfFileException exception) {
			endOffset = getEndOffset();
		}
		return endOffset;
	}

	private final IToken lookaheadToken(int i, boolean skipInactive) throws EndOfFileException {
		assert i >= 0;
		if (cancelled) {
			throw new ParseError(ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);
		}
		IToken token = nextToken(skipInactive);
		for (; i > 1; --i) {
			token = token.getNext();
			if (token == null)
				token = fetchToken(skipInactive);
		}
		return token;
	}

	private final IToken fetchToken(boolean skipInactive) throws EndOfFileException {
		try {
			IToken token = scanner.nextToken();
			if (skipInactive) {
				while (token.getType() == IToken.tINACTIVE_CODE_START) {
					scanner.skipInactiveCode();
					token = scanner.nextToken();
				}
			}
			if (lastTokenFromScanner != null)
				lastTokenFromScanner.setNext(token);
			lastTokenFromScanner = token;
			return token;
		} catch (OffsetLimitReachedException exception) {
			if (mode != ParserMode.COMPLETION_PARSE)
				throw new EndOfFileException(exception.getEndOffset());
			IToken completionToken = exception.getFinalToken();
			createCompletionNode(completionToken);
			if (exception.getOriginator() == OffsetLimitReachedException.ORIGIN_INACTIVE_CODE) {
				// If completion is invoked inside inactive code, there is no AST from which
				// to give the completion node a completion name, so we invent a name.
				// The invented name is not hooked up to the AST, but does have an offset
				// and length, so it can provide an accurate point of reference in
				// declaredBefore().
				IASTName completionName = nodeFactory.newInactiveCompletionName(completionToken.getCharImage(),
						getCompilationUnit());
				((ASTNode) completionName).setOffsetAndLength(completionToken.getOffset(), completionToken.getLength());
				completionNode.addName(completionName);

				// Consume the completion token so we don't try to parse an AST fragment from it.
				consume();
			}
			throw exception;
		}
	}

	private final IToken nextToken(boolean skipInactive) throws EndOfFileException {
		IToken token = nextToken;
		if (token != null)
			return token;

		token = fetchToken(skipInactive);
		nextToken = token;
		return token;
	}

	private final void declarationList(final IASTDeclarationListOwner unit, DeclarationOptions options,
			boolean upToBrace, int codeBranchNesting) {
		final boolean wasActive = activeCode;
		while (true) {
			final boolean ok = acceptInactiveCodeBoundary(codeBranchNesting);
			if (!ok) {
				// We left to an enclosing code branch. If we started in inactive code, it's time to leave.
				if (!wasActive)
					return;

				// If we started in active code, we need to skip the outer and therefore unrelated
				// inactive branches until we hit active code again.
				try {
					skipInactiveCode();
				} catch (OffsetLimitReachedException exception) {
					return;
				}
				codeBranchNesting = Math.min(getCodeBranchNesting() + 1, codeBranchNesting);

				// We could be at the start of inactive code so restart the loop.
				continue;
			}

			final boolean active = activeCode;
			IToken next = lookaheadWithEndOfFile(1);
			if (next == null || next.getType() == IToken.tEOC)
				return;

			if (upToBrace && next.getType() == IToken.tRBRACE && active == wasActive) {
				return;
			}

			final int offset = next.getOffset();
			declarationMark = next;
			next = null; // Don't hold on to the token while parsing namespaces, class bodies, etc.
			try {
				IASTDeclaration declaration = declaration(options);
				if (((ASTNode) declaration).getLength() == 0 && lookaheadTypeWithEndOfFile(1) != IToken.tEOC) {
					declaration = skipProblemDeclaration(offset);
				}
				addDeclaration(unit, declaration, active);
			} catch (BacktrackException exception) {
				IASTDeclaration[] decls = problemDeclaration(offset, exception, options);
				for (IASTDeclaration declaration : decls) {
					addDeclaration(unit, declaration, active);
				}
			} catch (EndOfFileException exception) {
				IASTDeclaration declaration = skipProblemDeclaration(offset);
				addDeclaration(unit, declaration, active);
				if (!exception.endsInactiveCode()) {
					break;
				}
			} finally {
				declarationMark = null;
			}
		}
	}

	protected static final ASTMarkInactiveVisitor MARK_INACTIVE = new ASTMarkInactiveVisitor();

	private void addDeclaration(final IASTDeclarationListOwner parent, IASTDeclaration declaration,
			final boolean active) {
		if (!active) {
			declaration.accept(MARK_INACTIVE);
		}
		parent.addDeclaration(declaration);
	}

	private IASTProblemDeclaration buildProblemDeclaration(IASTProblem problem) {
		IASTProblemDeclaration declaration = nodeFactory.newProblemDeclaration(problem);
		((ASTNode) declaration).setOffsetAndLength(((ASTNode) problem));
		return declaration;
	}

	protected static final class ASTMarkInactiveVisitor extends ASTGenericVisitor {
		public ASTMarkInactiveVisitor() {
			super(true);
			this.shouldVisitAmbiguousNodes = true;
		}

		@Override
		protected int genericVisit(IASTNode node) {
			((ASTNode) node).setInactive();
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(ASTAmbiguousNode node) {
			node.setInactive();
			IASTNode[] alternatives = node.getNodes();
			for (IASTNode alternative : alternatives) {
				if (!alternative.accept(this))
					return PROCESS_ABORT;
			}
			return PROCESS_CONTINUE;
		}
	}
}
