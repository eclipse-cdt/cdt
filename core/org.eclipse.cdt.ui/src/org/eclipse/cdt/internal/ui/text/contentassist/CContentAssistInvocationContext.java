/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ICEditorContentAssistInvocationContext;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.text.Symbols;


/**
 * Describes the context of a content assist invocation in a C/C++ editor.
 * <p>
 * Clients may use but not subclass this class.
 * </p>
 * 
 * @since 4.0
 */
public class CContentAssistInvocationContext extends ContentAssistInvocationContext implements ICEditorContentAssistInvocationContext {
	private final IEditorPart fEditor;
	private final boolean fIsCompletion;
	private final boolean fIsAutoActivated;
	private IIndex fIndex = null;
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

	private final Lazy<IASTCompletionNode> fCN = new Lazy<IASTCompletionNode>() {
		@Override
		protected IASTCompletionNode calculateValue() {
			int offset = getParseOffset();
			if (offset < 0) return null;
			
			ICProject proj= getProject();
			if (proj == null) return null;
			
			try {
				IIndexManager manager= CCorePlugin.getIndexManager();
				fIndex = manager.getIndex(proj, IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_EXTENSION_FRAGMENTS_CONTENT_ASSIST);

				try {
					fIndex.acquireReadLock();
				} catch (InterruptedException e) {
					fIndex = null;
				}

				boolean parseNonIndexed= CUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.PREF_USE_STRUCTURAL_PARSE_MODE);
				int flags = parseNonIndexed ? ITranslationUnit.AST_SKIP_INDEXED_HEADERS : ITranslationUnit.AST_SKIP_ALL_HEADERS;
				flags |= ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT;
				
				return fTU.value().getCompletionNode(fIndex, flags, offset);
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

	private final Lazy<Boolean> afterOpeningParenthesis = new Lazy<Boolean>() {
		@Override
		protected Boolean calculateValue() {
			final int invocationOffset = getInvocationOffset();
			final int parseOffset = getParseOffset();
			final int bound = Math.max(-1, parseOffset - 1);
			final CHeuristicScanner scanner = new CHeuristicScanner(getDocument());
			final int parenthesisOffset = scanner.findOpeningPeer(invocationOffset, bound, '(', ')');
			return parenthesisOffset != CHeuristicScanner.NOT_FOUND;
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
				if (token == Symbols.TokenUSING) {  // there could also be a leading "::" for global namespace
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
	
	/**
	 * Creates a new context.
	 * 
	 * @param viewer the viewer used by the editor
	 * @param offset the invocation offset
	 * @param editor the editor that content assist is invoked in
	 * @param isAutoActivated indicates whether content assist was auto-activated
	 */
	public CContentAssistInvocationContext(ITextViewer viewer, int offset, IEditorPart editor, boolean isCompletion, boolean isAutoActivated) {
		super(viewer, offset);
		Assert.isNotNull(editor);
		fEditor= editor;
		fIsCompletion= isCompletion;
		fIsAutoActivated= isAutoActivated;
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
		fTU= new Lazy<ITranslationUnit>() {
			@Override
			protected ITranslationUnit calculateValue() {
				return unit;
			}
		};
		fEditor= null;
		fIsCompletion= isCompletion;
		fIsAutoActivated= false;
	}
	
	/**
	 * Returns the translation unit that content assist is invoked in, <code>null</code> if there
	 * is none.
	 * 
	 * @return the translation unit that content assist is invoked in, possibly <code>null</code>
	 */
	@Override
	public ITranslationUnit getTranslationUnit() {
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
		ITranslationUnit unit= getTranslationUnit();
		return unit == null ? null : unit.getCProject();
	}
		
	@Override
	public IASTCompletionNode getCompletionNode() {
		//for scalability
		if (fEditor != null && fEditor instanceof CEditor) {
			CEditor editor = (CEditor)fEditor;
			
			// check to make sure we should attempt local parsing completions... for remote projects
			// we should not do this
			if(!editor.shouldProcessLocalParsingCompletions()) {
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
		return fParseOffset.value();
	}

	/**
	 * @return the offset where context information (parameter hints) starts.
	 */
	@Override
	public int getContextInformationOffset() {
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
		CHeuristicScanner scanner= new CHeuristicScanner(getDocument());
		int bound= Math.max(-1, contextPosition - 200);
		
		int pos= scanner.findNonWhitespaceBackward(contextPosition - 1, bound);
		if (pos == CHeuristicScanner.NOT_FOUND) return contextPosition;
		
		int token= scanner.previousToken(pos, bound);
		
		if (token == Symbols.TokenCOMMA) {
			int openingParenthesisPos = scanner.findOpeningPeer(pos, bound, '(', ')');
			int openingAngleBracketPos = scanner.findOpeningPeer(pos, bound, '<', '>');
			pos = Math.max(openingParenthesisPos, openingAngleBracketPos);
			if (pos == CHeuristicScanner.NOT_FOUND) return contextPosition;
			
			token = scanner.previousToken(pos, bound);
		}
		
		if (token == Symbols.TokenLPAREN || token == Symbols.TokenLESSTHAN) {
			pos= scanner.findNonWhitespaceBackward(pos - 1, bound);
			if (pos == CHeuristicScanner.NOT_FOUND) return contextPosition;
			
			token= scanner.previousToken(pos, bound);
			
			if (token == Symbols.TokenGREATERTHAN) {
				// skip template arguments
				pos= scanner.findOpeningPeer(pos - 1, '<', '>');
				if (pos == CHeuristicScanner.NOT_FOUND) return contextPosition;
				pos= scanner.findNonWhitespaceBackward(pos - 1, bound);
				if (pos == CHeuristicScanner.NOT_FOUND) return contextPosition;
				token= scanner.previousToken(pos, bound);
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
		final int contextPosition= getInvocationOffset();
		
		CHeuristicScanner scanner= new CHeuristicScanner(getDocument());
		int bound= Math.max(-1, contextPosition - 200);
		
		// try the innermost scope of parentheses that looks like a method call
		int pos= contextPosition - 1;
		do {
			int paren= scanner.findOpeningPeer(pos, bound, '(', ')');
			int angle= scanner.findOpeningPeer(pos, bound, '<', '>');
			int nearestPeer = Math.max(paren, angle);
			if (nearestPeer == CHeuristicScanner.NOT_FOUND)
				break;
			int token= scanner.previousToken(nearestPeer - 1, bound);
			// next token must be a method name (identifier) or the closing angle of a
			// constructor call of a template type.
			if (token == Symbols.TokenIDENT || token == Symbols.TokenGREATERTHAN) {
				return nearestPeer + 1;
			}
			pos= nearestPeer - 1;
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
		return fEditor;
	}

	@Override
	public boolean isContextInformationStyle() {
		return !fIsCompletion || (getParseOffset() != getInvocationOffset());
	}
	
	public boolean isAutoActivated() {
		return fIsAutoActivated;
	}

	@Override
	public void dispose() {
		if (fIndex != null) {
			fIndex.releaseReadLock();
		}
		super.dispose();
	}

	public boolean isAfterOpeningParenthesis() {
		return afterOpeningParenthesis.value();
	}

	public boolean isAfterOpeningAngleBracket() {
		return afterOpeningAngleBracket.value();
	}

	public boolean isInUsingDirective() {
		return inUsingDeclaration.value();
	}

	public boolean isFollowedBySemicolon() {
		return followedBySemicolon.value();
	}
}
