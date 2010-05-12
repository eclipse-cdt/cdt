/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
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
	
	private ITranslationUnit fTU= null;
	private boolean fTUComputed= false;
	private int fParseOffset= -1;
	private boolean fParseOffsetComputed= false;
	private IASTCompletionNode fCN= null;
	private boolean fCNComputed= false;
	private IIndex fIndex = null;
	private int fContextInfoPosition;
	
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
	}
	
	/**
	 * Creates a new context.
	 * 
	 * @param unit the translation unit in <code>document</code>
	 */
	public CContentAssistInvocationContext(ITranslationUnit unit, boolean isCompletion) {
		super();
		fTU= unit;
		fTUComputed= true;
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
	public ITranslationUnit getTranslationUnit() {
		if (!fTUComputed) {
			fTUComputed= true;
			fTU= CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
		}
		return fTU;
	}
	
	/**
	 * Returns the project of the translation unit that content assist is invoked in,
	 * <code>null</code> if none.
	 * 
	 * @return the current C project, possibly <code>null</code>
	 */
	public ICProject getProject() {
		ITranslationUnit unit= getTranslationUnit();
		return unit == null ? null : unit.getCProject();
	}
		
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
		
		if (fCNComputed) return fCN;
		
		fCNComputed = true;

		int offset = getParseOffset();
		if (offset < 0) return null;
		
		ICProject proj= getProject();
		if (proj == null) return null;
		
		try {
			IIndexManager manager= CCorePlugin.getIndexManager();
			fIndex = manager.getIndex(proj, IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);

			try {
				fIndex.acquireReadLock();
			} catch (InterruptedException e) {
				fIndex = null;
			}

			boolean parseNonIndexed= CUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.PREF_USE_STRUCTURAL_PARSE_MODE);
			int flags = parseNonIndexed ? ITranslationUnit.AST_SKIP_INDEXED_HEADERS : ITranslationUnit.AST_SKIP_ALL_HEADERS;
			flags |= ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT;
			
			fCN = fTU.getCompletionNode(fIndex, flags, offset);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		
		return fCN;
	}
	
	public int getParseOffset() {
		if (!fParseOffsetComputed) {
			fParseOffsetComputed= true;
			fContextInfoPosition= guessContextInformationPosition();
			if (fIsCompletion) {
				fParseOffset = guessCompletionPosition(getInvocationOffset());
			} else if (fContextInfoPosition > 0) {
				fParseOffset = guessCompletionPosition(fContextInfoPosition);
			} else {
				fParseOffset = -1;
			}
		}
		
		return fParseOffset;
	}

	/**
	 * @return the offset where context information (parameter hints) starts.
	 */
	public int getContextInformationOffset() {
		getParseOffset();
		return fContextInfoPosition;
	}
	
	/**
	 * Try to find a sensible completion position backwards in case the given offset
	 * is inside a function call argument list.
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
			pos= scanner.findOpeningPeer(pos, bound, '(', ')');
			if (pos == CHeuristicScanner.NOT_FOUND) return contextPosition;
			
			token = scanner.previousToken(pos, bound);
		}
		
		if (token == Symbols.TokenLPAREN) {
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
			if (paren == CHeuristicScanner.NOT_FOUND)
				break;
			int token= scanner.previousToken(paren - 1, bound);
			// next token must be a method name (identifier) or the closing angle of a
			// constructor call of a template type.
			if (token == Symbols.TokenIDENT || token == Symbols.TokenGREATERTHAN) {
				return paren + 1;
			}
			pos= paren - 1;
		} while (true);
		
		return -1;
	}
	
	/**
	 * Get the editor content assist is invoked in.
	 * 
	 * @return the editor, may be <code>null</code>
	 */
	public IEditorPart getEditor() {
		return fEditor;
	}

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
}
