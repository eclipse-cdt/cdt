/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArraySet;

/**
 * Represents part of the input to the preprocessor. This may be a file or the result of a macro expansion.
 * @since 5.0
 */
final class ScannerContext {
	enum BranchKind {eIf, eElif, eElse, eEnd}
	enum CodeState {eActive, eParseInactive, eSkipInactive}

	final static class Conditional {
		private final CodeState fInitialState;
		private BranchKind fLast;
		private boolean fTakeElse= true;

		Conditional(CodeState state) {
			fInitialState= state;
			fLast= BranchKind.eIf;
		}

		boolean canHaveActiveBranch(boolean withinExpansion) {
			return fTakeElse && isActive(withinExpansion);
		}

		public boolean isActive(boolean withinExpansion) {
			return withinExpansion || fInitialState == CodeState.eActive;
		}
	}
	
	private CodeState fInactiveState= CodeState.eSkipInactive;
	private final int fDepth;
	private final ILocationCtx fLocationCtx;
	private final ScannerContext fParent;
	private final Lexer fLexer;
	private Token fTokens;
	private ArrayList<Conditional> fConditionals= null;
	private CodeState fCurrentState= CodeState.eActive;
	private IncludeSearchPathElement fFoundOnPath;
	private String fFoundViaDirective;
	private CharArraySet fInternalModifications;
	private CharArrayObjectMap<char[]> fSignificantMacros;
	private boolean fPragmaOnce;
	private int fLoadedVersionCount;
	

	/**
	 * @param ctx 
	 * @param parent context to be used after this context is done.
	 */
	public ScannerContext(ILocationCtx ctx, ScannerContext parent, Lexer lexer) {
		fLocationCtx= ctx;
		fParent= parent;
		fLexer= lexer;
		fDepth = parent == null ? 0 : parent.fDepth+1;
	}
	
	public ScannerContext(ILocationCtx ctx, ScannerContext parent, TokenList tokens) {
		this(ctx, parent, (Lexer) null);
		fTokens= tokens.first();
		fInactiveState= CodeState.eSkipInactive;  // no branches in result of macro expansion
	}

	public void setParseInactiveCode(boolean val) {
		fInactiveState= val ? CodeState.eParseInactive : CodeState.eSkipInactive;
	}
	
	/**
	 * Returns the location context associated with this scanner context.
	 */
	public final ILocationCtx getLocationCtx() {
		return fLocationCtx;
	}
	
	/**
	 * Returns the parent context which will be used after this context is finished.
	 * May return <code>null</code>.
	 */
	public final ScannerContext getParent() {
		return fParent;
	}

	/**
	 * Returns the depth of this context, equals the number of parents of this context.
	 */
	public final int getDepth() {
		return fDepth;
	}
	
	/**
	 * Returns the lexer for this context.
	 */
	public final Lexer getLexer() {
		return fLexer;
	}

	/**
	 * Needs to be called whenever we change over to another branch of conditional 
	 * compilation. Returns the conditional associated with the branch or <code>null</code>,
	 * if the change is not legal at this point.
	 */
	public final Conditional newBranch(BranchKind branchKind, boolean withinExpansion) {
		if (fConditionals == null) {
			fConditionals= new ArrayList<Conditional>();
		}
		
		Conditional result;
		
		// an if starts a new conditional construct
		if (branchKind == BranchKind.eIf) {
			fConditionals.add(result= new Conditional(fCurrentState));
			return result;
		}
		
		// if we are not inside of an conditional there shouldn't be an #else, #elsif or #end
		final int pos= fConditionals.size()-1;
		if (pos < 0) {
			return null;
		}
		
		// an #end just pops one construct and restores state
		if (branchKind == BranchKind.eEnd) {
			result= fConditionals.remove(pos);
			return result;
		}
		
		// #elif or #else cannot appear after another #else
		result= fConditionals.get(pos);
		if (result.fLast == BranchKind.eElse)
			return null;
		
		// store last kind
		result.fLast= branchKind;
		return result;
	}

	private void changeState(CodeState state, BranchKind kind, boolean withinExpansion, int offset) {
		if (!withinExpansion) {
			switch (state) {
			case eActive:
				if (fCurrentState == CodeState.eParseInactive) 
					stopInactive(offset, kind);
				break;
			case eParseInactive:
				switch (fCurrentState) {
				case eActive:
					startInactive(offset, kind);
					break;
				case eParseInactive:
					separateInactive(offset, kind);
					break;
				case eSkipInactive:
					assert false; // in macro expansions, only.
					break;
				}
				break;
			case eSkipInactive:
				// no need to report this to the parser.
				break;
			}
		}
		fCurrentState= state;
	}

	private void startInactive(int offset, BranchKind kind) {
		final int nesting = getCodeBranchNesting();
		final int oldNesting = getOldNestingLevel(kind, nesting);
		fTokens= new InactiveCodeToken(IToken.tINACTIVE_CODE_START, oldNesting, nesting, offset);
	}

	private void separateInactive(int offset, BranchKind kind) {
		final int nesting = getCodeBranchNesting();
		final int oldNesting = getOldNestingLevel(kind, nesting);
		fTokens= new InactiveCodeToken(IToken.tINACTIVE_CODE_SEPARATOR, oldNesting, nesting, offset);
	}

	private int getOldNestingLevel(BranchKind kind, int nesting) {
		switch (kind) {
		case eIf:
			return nesting - 1;
		case eElif:
		case eElse:
			return nesting;
		case eEnd:
			return nesting + 1;
		}
		return nesting;
	}

	private void stopInactive(int offset, BranchKind kind) {
		final int nesting = getCodeBranchNesting();
		final int oldNesting = getOldNestingLevel(kind, nesting);
		fTokens= new InactiveCodeToken(IToken.tINACTIVE_CODE_END, oldNesting, nesting, offset);
	}	

	public CodeState getCodeState() {
		return fCurrentState;
	}
	
	/**
	 * The preprocessor has to inform the context about the state of if- and elif- branches
	 */
	public CodeState setBranchState(Conditional cond, boolean isActive, boolean withinExpansion, int offset) {
		CodeState newState;
		if (isActive) {
			cond.fTakeElse= false;
			newState= cond.fInitialState;
		} else if (withinExpansion) {
			newState= CodeState.eParseInactive;
		} else {
			newState= fInactiveState;
		}
		changeState(newState, cond.fLast, withinExpansion, offset);
		return newState;
	}

	/**
	 * The preprocessor has to inform the context about the state of if- and elif- branches
	 */
	public CodeState setBranchEndState(Conditional cond, boolean withinExpansion, int offset) {
		// implicit state change
		CodeState newState = cond != null ? cond.fInitialState : CodeState.eActive;
		changeState(newState, BranchKind.eEnd, withinExpansion, offset);
		return newState;
	}

	/** 
	 * Returns the current token from this context. When called before calling nextPPToken() 
	 * a token of type {@link Lexer#tBEFORE_INPUT} will be returned.
	 * @since 5.0
	 */
	public final Token currentLexerToken() {
		if (fTokens != null) {
			return fTokens;
		}
		if (fLexer != null) {
			return fLexer.currentToken();
		}
		return new Token(IToken.tEND_OF_INPUT, null, 0, 0);
	}
	
	/** 
	 * Returns the next token from this context. 
	 */
	public Token nextPPToken() throws OffsetLimitReachedException {
		if (fTokens != null) {
			fTokens= (Token) fTokens.getNext();
			return currentLexerToken();
		}
		if (fLexer != null) {
			return fLexer.nextToken();
		}
		return new Token(IToken.tEND_OF_INPUT, null, 0, 0);
	}

	/**
	 * If this is a lexer based context the current line is consumed.
	 * @see Lexer#consumeLine(int)
	 */
	public int consumeLine(int originPreprocessorDirective) throws OffsetLimitReachedException {
		if (fLexer != null)
			return fLexer.consumeLine(originPreprocessorDirective);
		return -1;
	}

	public void clearInactiveCodeMarkerToken() {
		fTokens= null;
	}

	/**
	 * Returns the current nesting within code branches
	 */
	public int getCodeBranchNesting() {
		if (fConditionals == null)
			return 0;
		return fConditionals.size();
	}

	/**
	 * Returns the element of the include search path that was used to find this context, or <code>null</code> if not applicable.
	 */
	public IncludeSearchPathElement getFoundOnPath() {
		return fFoundOnPath;
	}

	/**
	 * Returns the directive with which the this context was found, or <code>null</code> if not applicable.
	 */
	public String getFoundViaDirective() {
		return fFoundViaDirective;
	}

	/**
	 * Returns the element of the include search path that was used to find this context, or <code>null</code> if not applicable.
	 */
	public void setFoundOnPath(IncludeSearchPathElement foundOnPath, String viaDirective) {
		fFoundOnPath= foundOnPath;
		fFoundViaDirective= viaDirective;
	}
	
	public void trackSignificantMacros() {
		fInternalModifications= new CharArraySet(5);
		fSignificantMacros= new CharArrayObjectMap<char[]>(5);
	}
	
	public void setPragmaOnce(boolean val) {
		fPragmaOnce= val;
	}
	
	public boolean isPragmaOnce() {
		return fPragmaOnce;
	}

	public void internalModification(char[] macroName) {
		if (fInternalModifications != null)
			fInternalModifications.put(macroName);
	}
	
	public void significantMacro(IMacroBinding macro) {
		final char[] macroName= macro.getNameCharArray();
		if (fInternalModifications != null && !fInternalModifications.containsKey(macroName)) {
			fSignificantMacros.put(macroName, macro.getExpansion());
		}
	}
	
	public void significantMacroDefined(char[] macroName) {
		if (fInternalModifications != null && !fInternalModifications.containsKey(macroName)) {
			addSignificantMacroDefined(macroName);
		}
	}

	private void addSignificantMacroDefined(char[] macroName) {
		char[] old= fSignificantMacros.put(macroName, SignificantMacros.DEFINED);
		if (old != null && old != SignificantMacros.DEFINED) {
			// Put back more detailed condition
			fSignificantMacros.put(macroName, old);
		}
	}
	
	public void significantMacroUndefined(char[] macroName) {
		if (fInternalModifications != null && !fInternalModifications.containsKey(macroName)) {
			fSignificantMacros.put(macroName, SignificantMacros.UNDEFINED);
		}
	}
	
	public CharArrayObjectMap<char[]> getSignificantMacros() {
		return fSignificantMacros;
	}

	public void propagateSignificantMacros() {
		if (fInternalModifications == null)
			return;
		
		if (fParent != null) {
			final CharArraySet local = fParent.fInternalModifications;
			if (local != null) {
				final CharArrayObjectMap<char[]> significant = fParent.fSignificantMacros;
				for (int i=0; i<fSignificantMacros.size(); i++) {
					final char[] name = fSignificantMacros.keyAt(i);
					if (!local.containsKey(name)) {
						final char[] value= fSignificantMacros.getAt(i);
						if (value == SignificantMacros.DEFINED) {
							if (!local.containsKey(name)) {
								fParent.addSignificantMacroDefined(name);
							}
						} else {
							significant.put(name, value);
						}
					}
				}
				local.addAll(fInternalModifications);
			}
		}
		fInternalModifications= null;
		fSignificantMacros= null;
	}

	public void addSignificantMacros(ISignificantMacros sm) {
		if (fInternalModifications == null)
			return;
		
		sm.accept(new ISignificantMacros.IVisitor() {
			public boolean visitValue(char[] macro, char[] value) {
				if (!fInternalModifications.containsKey(macro)) {
					fSignificantMacros.put(macro, value);
				}
				return true;
			}
			public boolean visitUndefined(char[] macro) {
				if (!fInternalModifications.containsKey(macro)) {
					fSignificantMacros.put(macro, SignificantMacros.UNDEFINED);
				}
				return true;
			}
			public boolean visitDefined(char[] macro) {
				if (!fInternalModifications.containsKey(macro)) {
					fSignificantMacros.put(macro, SignificantMacros.DEFINED);
				}
				return true;
			}
		});
	}

	public int getLoadedVersionCount() {
		return fLoadedVersionCount;
	}

	public void setLoadedVersionCount(int count) {
		fLoadedVersionCount= count;
	}
	
	@Override
	public String toString() {
		if (fParent == null)
			return fLocationCtx.toString();
		
		return fParent.toString() + "\n" + fLocationCtx.toString(); //$NON-NLS-1$
	}
}
