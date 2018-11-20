/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Thomas Corbat (IFS)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.text.Symbols;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ICEditorContentAssistInvocationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;

/**
 * Describes the context of a content assist invocation in a C/C++ editor.
 * <p>
 * Clients may instantiate. A client that created a context is responsible for its disposal.
 * </p>
 *
 * @since 4.0
 */
public class CContentAssistInvocationContext extends ContentAssistInvocationContext
		implements ICEditorContentAssistInvocationContext {
	private final IEditorPart fEditor;
	private final boolean fIsCompletion;
	private final boolean fIsAutoActivated;
	private IIndex fIndex;
	// Whether we are doing completion (false) or just showing context information (true).
	private boolean fIsContextInformationStyle;
	// The parse offset is the end of the name we are doing completion on.
	// Since this name can be adjusted by adjustCompletionNode(), the parse offset
	// may need a corresponding adjustment, and this stores the adjusted offset.
	private int fAdjustedParseOffset = -1;

	private Lazy<Integer> fContextInfoPosition = new Lazy<Integer>() {
		@Override
		protected Integer calculateValue() {
			return guessContextInformationPosition();
		}
	};

	private final Lazy<ITranslationUnit> fTU;

	private final Lazy<Integer> fParseOffset = new Lazy<Integer>() {
		@Override
		protected Integer calculateValue() {
			int result = doCalculate();
			if (result != getInvocationOffset()) {
				// If guessCompletionPosition() chose a parse offset that's different
				// from the invocation offset, we are proposing completions for a name
				// that's not right under the cursor, and so we just want to show
				// context information.
				fIsContextInformationStyle = true;
			}
			fAdjustedParseOffset = result;
			return result;
		}

		private int doCalculate() {
			if (fIsCompletion) {
				return guessCompletionPosition(getInvocationOffset());
			}
			int contextInfoPosition = fContextInfoPosition.value();
			if (contextInfoPosition > 0) {
				return guessCompletionPosition(contextInfoPosition);
			}
			return -1;
		}
	};

	// Helper function for adjustCompletionName().
	private IASTName getAdjustedCompletionName(IASTName completionName) {
		if (completionName.getSimpleID().length > 0) {
			return null;
		}
		if (completionName.getParent() instanceof IASTIdExpression
				&& completionName.getParent().getParent() instanceof IASTInitializerList) {
			IASTNode initList = completionName.getParent().getParent();
			if (initList.getParent() instanceof IASTDeclarator
					&& initList.getParent().getParent() instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration decl = (IASTSimpleDeclaration) completionName.getParent().getParent().getParent()
						.getParent();
				if (decl.getDeclSpecifier() instanceof IASTNamedTypeSpecifier) {
					return ((IASTNamedTypeSpecifier) decl.getDeclSpecifier()).getName();
				}
			} else if (initList.getParent() instanceof ICPPASTSimpleTypeConstructorExpression) {
				ICPPASTSimpleTypeConstructorExpression expr = (ICPPASTSimpleTypeConstructorExpression) initList
						.getParent();
				if (expr.getDeclSpecifier() instanceof IASTNamedTypeSpecifier) {
					return ((IASTNamedTypeSpecifier) expr.getDeclSpecifier()).getName();
				}
			} else if (initList.getParent() instanceof ICPPASTConstructorChainInitializer) {
				ICPPASTConstructorChainInitializer ctorInit = (ICPPASTConstructorChainInitializer) initList.getParent();
				return ctorInit.getMemberInitializerId();
			}
		}
		return null;
	}

	/**
	 * Choose a better completion node based on information in the AST.
	 *
	 * Currently, this makes an adjustment in one case: if the completion node is
	 * an empty name at the top level of one of the initializers of an initializer
	 * list that may be a constructor call, the completion name is adjusted to
	 * instead be the name preceding the initializer list (which will either name
	 * the type being constructed, or a variable of that type). This allows us to
	 * offer completions for the constructors of this type.
	 *
	 * Currently we handle initializer lists in three contexts:
	 *   1) simple declaration
	 *   2) simple type constructor expression
	 *   3) constructor chain initializer
	 *
	 * TODO: Handle the additional context of a return-expression:
	 *         S foo() {
	 *           return {...};  // can invoke S constructor with args inside {...}
	 *         }
	 *
	 * Note that for constructor calls with () rather than {} syntax, we
	 * accomplish the same goal by different means: in getParseOffset(), we choose
	 * a parse offset that will give us the desired completion node to begin with.
	 * We can't do this with the {} syntax, because in getParseOffset() we don't
	 * have an AST yet (the choice is made using CHeuristicScanner), and we cannot
	 * distinguish other uses of {} from the desired ones.
	 *
	 * @param completionName the initial completion name, based on the parse offset
	 *                       chosen using CHeuristicScanner
	 * @return the adjusted completion name, if different from the initial completion
	 *         name, or {@code null}
	 */
	private IASTCompletionNode adjustCompletionNode(IASTCompletionNode existing) {
		IASTName[] names = existing.getNames();
		// If there are multiple completion names, there is a parser ambiguity
		// near the completion location. Just bail in this case.
		if (names.length != 1) {
			return null;
		}
		IASTName completionName = names[0];
		IASTName newCompletionName = getAdjustedCompletionName(completionName);
		if (newCompletionName != null) {
			IToken newCompletionToken = null;
			try {
				newCompletionToken = newCompletionName.getSyntax();
			} catch (ExpansionOverlapsBoundaryException e) {
			}
			if (newCompletionToken != null) {
				ASTCompletionNode newCompletionNode = new ASTCompletionNode(newCompletionToken,
						existing.getTranslationUnit());
				newCompletionNode.addName(newCompletionName);
				return newCompletionNode;
			}
		}
		return null;
	}

	private final Lazy<IASTCompletionNode> fCN = new Lazy<IASTCompletionNode>() {
		@Override
		protected IASTCompletionNode calculateValue() {
			int offset = getParseOffset();
			if (offset < 0)
				return null;

			ICProject proj = getProject();
			if (proj == null)
				return null;

			try {
				if (fIndex != null)
					throw new IllegalStateException("The method should not be called multiple times."); //$NON-NLS-1$

				IIndexManager manager = CCorePlugin.getIndexManager();
				fIndex = manager.getIndex(proj,
						IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_EXTENSION_FRAGMENTS_CONTENT_ASSIST);

				try {
					fIndex.acquireReadLock();
				} catch (InterruptedException e) {
					fIndex = null;
				}

				boolean parseNonIndexed = CUIPlugin.getDefault().getPreferenceStore()
						.getBoolean(PreferenceConstants.PREF_USE_STRUCTURAL_PARSE_MODE);
				int flags = parseNonIndexed ? ITranslationUnit.AST_SKIP_INDEXED_HEADERS
						: ITranslationUnit.AST_SKIP_ALL_HEADERS;
				flags |= ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT;

				IASTCompletionNode result = fTU.value().getCompletionNode(fIndex, flags, offset);
				if (result != null) {
					// The initial completion code is determined by the parse offset chosen
					// in getParseOffset() using CHeuristicScanner. Now that we have an AST,
					// we may want to use information in the AST to choose a better completion
					// node.
					IASTCompletionNode adjusted = adjustCompletionNode(result);
					if (adjusted != null) {
						// If we made an adjustment, we just want to show context information.
						fIsContextInformationStyle = true;
						result = adjusted;
						if (adjusted.getNames().length > 0) {
							// Make a corresponding adjustment to the parse offset.
							IASTFileLocation adjustedLocation = adjusted.getNames()[0].getFileLocation();
							fAdjustedParseOffset = adjustedLocation.getNodeOffset() + adjustedLocation.getNodeLength();
						}
					}
				}
				return result;
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
			return null;
		}
	};

	private final Lazy<Boolean> afterOpeningAngleBracket = new Lazy<Boolean>() {
		@Override
		protected Boolean calculateValue() {
			final int parseOffset = getParseOffset();
			final int invocationOffset = getInvocationOffset();
			final CHeuristicScanner scanner = new CHeuristicScanner(getDocument());
			final int parenthesisOffset = scanner.findOpeningPeer(invocationOffset, parseOffset, '<', '>');
			return parenthesisOffset != CHeuristicScanner.NOT_FOUND;
		}
	};

	private final Lazy<Boolean> afterOpeningParenthesisOrBrace = new Lazy<Boolean>() {
		@Override
		protected Boolean calculateValue() {
			final int invocationOffset = getInvocationOffset();
			final int parseOffset = fAdjustedParseOffset;
			final int bound = Math.max(-1, parseOffset - 1);
			final IDocument document = getDocument();
			final CHeuristicScanner scanner = new CHeuristicScanner(document);
			int start = invocationOffset;
			try {
				// The documentation of CHeuristicScanner.findOpeningPeer() says
				// "Note that <code>start</code> must not point to the closing peer, but to the first
				//  character being searched."
				// If we are completing in between two empty parentheses with no space between them,
				// this condition won't be satisfied, so we start the search one character earlier.
				if (document.getChar(start) == ')' || document.getChar(start) == '}')
					start -= 1;
			} catch (BadLocationException e) {
			}
			final int parenthesisOffset = scanner.findOpeningPeer(start, bound, '(', ')');
			if (parenthesisOffset != CHeuristicScanner.NOT_FOUND) {
				return true;
			}
			final int braceOffset = scanner.findOpeningPeer(start, bound, '{', '}');
			if (braceOffset != CHeuristicScanner.NOT_FOUND) {
				return true;
			}
			return false;
		}
	};

	private final Lazy<Boolean> inUsingDeclaration = new Lazy<Boolean>() {
		/**
		 * Checks whether the invocation offset is inside a using-declaration.
		 *
		 * @return {@code true} if the invocation offset is inside a using-declaration
		 */
		@Override
		protected Boolean calculateValue() {
			IDocument doc = getDocument();
			int offset = Math.max(0, getInvocationOffset() - 1);

			// Look at the tokens preceding the invocation offset.
			CHeuristicScanner.TokenStream tokenStream = new CHeuristicScanner.TokenStream(doc, offset);
			int token = tokenStream.previousToken();

			// There may be a partially typed identifier which is being completed.
			if (token == Symbols.TokenIDENT)
				token = tokenStream.previousToken();

			// Before that, there may be any number of "namespace::" token pairs.
			while (token == Symbols.TokenDOUBLECOLON) {
				token = tokenStream.previousToken();
				if (token == Symbols.TokenUSING) { // there could also be a leading "::" for global namespace
					return true;
				} else if (token != Symbols.TokenIDENT) {
					return false;
				} else {
					token = tokenStream.previousToken();
				}
			}

			// Before that, there must be a "using" token.
			return token == Symbols.TokenUSING;
		}
	};

	private final Lazy<Boolean> followedBySemicolon = new Lazy<Boolean>() {
		@Override
		protected Boolean calculateValue() {
			final IDocument doc = getDocument();
			final int offset = getInvocationOffset();
			final CHeuristicScanner.TokenStream tokenStream = new CHeuristicScanner.TokenStream(doc, offset);
			final int token = tokenStream.nextToken();
			return token == Symbols.TokenSEMICOLON;
		}
	};

	private final Lazy<Boolean> followedByOpeningParen = new Lazy<Boolean>() {
		@Override
		protected Boolean calculateValue() {
			final IDocument doc = getDocument();
			final int offset = getInvocationOffset();
			final CHeuristicScanner.TokenStream tokenStream = new CHeuristicScanner.TokenStream(doc, offset);
			final int token = tokenStream.nextToken();
			return token == Symbols.TokenLPAREN;
		}
	};

	private final Lazy<String> functionParameterDelimiter = new Lazy<String>() {
		@Override
		protected String calculateValue() {
			String propertyKey = DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_PARAMETERS;
			Map<String, String> options = getProject().getOptions(true);
			return options.get(propertyKey).equals(CCorePlugin.INSERT) ? ", " : ","; //$NON-NLS-1$ //$NON-NLS-2$
		}
	};

	private final Lazy<String> templateParameterDelimiter = new Lazy<String>() {
		@Override
		protected String calculateValue() {
			String propertyKey = DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_TEMPLATE_PARAMETERS;
			Map<String, String> options = getProject().getOptions(true);
			return options.get(propertyKey).equals(CCorePlugin.INSERT) ? ", " : ","; //$NON-NLS-1$ //$NON-NLS-2$
		}
	};

	/**
	 * Creates a new context.
	 *
	 * @param viewer the viewer used by the editor
	 * @param offset the invocation offset
	 * @param editor the editor that content assist is invoked in
	 * @param isAutoActivated indicates whether content assist was auto-activated
	 */
	public CContentAssistInvocationContext(ITextViewer viewer, int offset, IEditorPart editor, boolean isCompletion,
			boolean isAutoActivated) {
		super(viewer, offset);
		Assert.isNotNull(editor);
		fEditor = editor;
		fIsCompletion = isCompletion;
		fIsContextInformationStyle = !isCompletion;
		fIsAutoActivated = isAutoActivated;
		fTU = new Lazy<ITranslationUnit>() {
			@Override
			protected ITranslationUnit calculateValue() {
				return CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
			}
		};
	}

	/**
	 * Creates a new context.
	 *
	 * @param unit the translation unit in <code>document</code>
	 */
	public CContentAssistInvocationContext(final ITranslationUnit unit, boolean isCompletion) {
		super();
		fTU = new Lazy<ITranslationUnit>() {
			@Override
			protected ITranslationUnit calculateValue() {
				return unit;
			}
		};
		fEditor = null;
		fIsCompletion = isCompletion;
		fIsContextInformationStyle = !isCompletion;
		fIsAutoActivated = false;
	}

	/**
	 * Returns the translation unit that content assist is invoked in, <code>null</code> if there
	 * is none.
	 *
	 * @return the translation unit that content assist is invoked in, possibly <code>null</code>
	 */
	@Override
	public ITranslationUnit getTranslationUnit() {
		assertNotDisposed();
		return fTU.value();
	}

	/**
	 * Returns the project of the translation unit that content assist is invoked in,
	 * <code>null</code> if none.
	 *
	 * @return the current C project, possibly <code>null</code>
	 */
	@Override
	public ICProject getProject() {
		assertNotDisposed();
		ITranslationUnit unit = getTranslationUnit();
		return unit == null ? null : unit.getCProject();
	}

	@Override
	public IASTCompletionNode getCompletionNode() {
		assertNotDisposed();
		// For scalability.
		if (fEditor != null && fEditor instanceof CEditor) {
			CEditor editor = (CEditor) fEditor;

			// Check to make sure we should attempt local parsing completions... for remote projects
			// we should not do this.
			if (!editor.shouldProcessLocalParsingCompletions()) {
				return null;
			}
			if (editor.isEnableScalablilityMode()) {
				if (editor.isParserBasedContentAssistDisabled()) {
					return null;
				}
				if (isAutoActivated() && editor.isContentAssistAutoActivartionDisabled()) {
					return null;
				}
			}
		}
		return fCN.value();
	}

	@Override
	public int getParseOffset() {
		assertNotDisposed();
		return fParseOffset.value();
	}

	public int getAdjustedParseOffset() {
		assertNotDisposed();
		return fAdjustedParseOffset;
	}

	/**
	 * @return the offset where context information (parameter hints) starts.
	 */
	@Override
	public int getContextInformationOffset() {
		assertNotDisposed();
		return fContextInfoPosition.value();
	}

	/**
	 * Try to find a sensible completion position backwards in case the given offset
	 * is inside a function call argument list or in template arguments.
	 *
	 * @param contextPosition  the starting position
	 * @return a sensible completion offset
	 */
	protected int guessCompletionPosition(int contextPosition) {
		assertNotDisposed();
		CHeuristicScanner scanner = new CHeuristicScanner(getDocument());
		int bound = Math.max(-1, contextPosition - 200);

		int pos = scanner.findNonWhitespaceBackward(contextPosition - 1, bound);
		if (pos == CHeuristicScanner.NOT_FOUND)
			return contextPosition;

		int token = scanner.previousToken(pos, bound);

		if (token == Symbols.TokenCOMMA) {
			int openingParenthesisPos = scanner.findOpeningPeer(pos, bound, '(', ')');
			int openingAngleBracketPos = scanner.findOpeningPeer(pos, bound, '<', '>');
			pos = Math.max(openingParenthesisPos, openingAngleBracketPos);
			if (pos == CHeuristicScanner.NOT_FOUND)
				return contextPosition;

			token = scanner.previousToken(pos, bound);
		}

		if (token == Symbols.TokenLPAREN || token == Symbols.TokenLESSTHAN) {
			pos = scanner.findNonWhitespaceBackward(pos - 1, bound);
			if (pos == CHeuristicScanner.NOT_FOUND)
				return contextPosition;

			token = scanner.previousToken(pos, bound);

			if (token == Symbols.TokenGREATERTHAN) {
				// skip template arguments
				pos = scanner.findOpeningPeer(pos - 1, '<', '>');
				if (pos == CHeuristicScanner.NOT_FOUND)
					return contextPosition;
				pos = scanner.findNonWhitespaceBackward(pos - 1, bound);
				if (pos == CHeuristicScanner.NOT_FOUND)
					return contextPosition;
				token = scanner.previousToken(pos, bound);
			}

			if (token == Symbols.TokenIDENT) {
				return pos + 1;
			}
		}

		return contextPosition;
	}

	/**
	 * Try to find the smallest offset inside the opening parenthesis of a function call
	 * argument list.
	 *
	 * @return the offset of the function call parenthesis plus 1 or -1 if the invocation
	 *     offset is not inside a function call (or similar)
	 */
	protected int guessContextInformationPosition() {
		assertNotDisposed();
		final int contextPosition = getInvocationOffset();

		CHeuristicScanner scanner = new CHeuristicScanner(getDocument());
		int bound = Math.max(-1, contextPosition - 200);

		// try the innermost scope of parentheses that looks like a method call
		int pos = contextPosition - 1;
		do {
			int paren = scanner.findOpeningPeer(pos, bound, '(', ')');
			int angle = scanner.findOpeningPeer(pos, bound, '<', '>');
			int nearestPeer = Math.max(paren, angle);
			if (nearestPeer == CHeuristicScanner.NOT_FOUND)
				break;
			int token = scanner.previousToken(nearestPeer - 1, bound);
			// next token must be a method name (identifier) or the closing angle of a
			// constructor call of a template type.
			if (token == Symbols.TokenIDENT || token == Symbols.TokenGREATERTHAN) {
				return nearestPeer + 1;
			}
			pos = nearestPeer - 1;
		} while (true);

		return -1;
	}

	/**
	 * Get the editor content assist is invoked in.
	 *
	 * @return the editor, may be <code>null</code>
	 */
	@Override
	public IEditorPart getEditor() {
		assertNotDisposed();
		return fEditor;
	}

	@Override
	public boolean isContextInformationStyle() {
		assertNotDisposed();
		return fIsContextInformationStyle;
	}

	public boolean isAutoActivated() {
		assertNotDisposed();
		return fIsAutoActivated;
	}

	@Override
	public void dispose() {
		if (fIndex != null) {
			fIndex.releaseReadLock();
			fIndex = null;
		}
		super.dispose();
	}

	public boolean isAfterOpeningParenthesisOrBrace() {
		assertNotDisposed();
		return afterOpeningParenthesisOrBrace.value();
	}

	public boolean isAfterOpeningAngleBracket() {
		assertNotDisposed();
		return afterOpeningAngleBracket.value();
	}

	public boolean isInUsingDirective() {
		assertNotDisposed();
		return inUsingDeclaration.value();
	}

	public boolean isFollowedBySemicolon() {
		assertNotDisposed();
		return followedBySemicolon.value();
	}

	public boolean isFollowedByOpeningParen() {
		assertNotDisposed();
		return followedByOpeningParen.value();
	}

	public String getFunctionParameterDelimiter() {
		assertNotDisposed();
		return functionParameterDelimiter.value();
	}

	public String getTemplateParameterDelimiter() {
		assertNotDisposed();
		return templateParameterDelimiter.value();
	}
}
