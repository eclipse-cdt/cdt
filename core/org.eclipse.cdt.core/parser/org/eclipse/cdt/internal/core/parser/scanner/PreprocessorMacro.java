/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;

/**
 * Models macros used by the preprocessor
 * @since 5.0
 */
abstract class PreprocessorMacro implements IMacroBinding {
	final private char[] fName;

	public PreprocessorMacro(char[] name) {
		fName= name;
	}
	
	final public ILinkage getLinkage() {
		return Linkage.NO_LINKAGE;
	}

	final public char[] getNameCharArray() {
		return fName;
	}

	final public String getName() {
		return new String(fName);
	}

	public IScope getScope() {
		return null;
	}

	public boolean isFunctionStyle() {
		return false;
	}
	
	public char[][] getParameterList() {
		return null;
	}
	
	public char[][] getParameterPlaceholderList() {
		return null;
	}

	public Object getAdapter(Class clazz) {
		return null;
	}

	public int hasVarArgs() {
		return FunctionStyleMacro.NO_VAARGS;
	}

	public String toString() {
		char[][] p= getParameterList();
		if (p == null) {
			return getName();
		}
		StringBuffer buf= new StringBuffer();
		buf.append(getNameCharArray());
		buf.append('(');
		for (int i = 0; i < p.length; i++) {
			if (i>0) {
				buf.append(',');
			}
			buf.append(p[i]);
		}
		buf.append(')');
		return buf.toString();
	}
	public abstract TokenList getTokens(MacroDefinitionParser parser, LexerOptions lexOptions);
}

abstract class DynamicStyleMacro extends PreprocessorMacro {

	public DynamicStyleMacro(char[] name) {
		super(name);
	}
	public char[] getExpansion() {
		return getExpansionImage();
	}
	public char[] getExpansionImage() {
		return execute().getCharImage();
	}
	public abstract Token execute();

	public TokenList getTokens(MacroDefinitionParser mdp, LexerOptions lexOptions) {
		TokenList result= new TokenList();
		result.append(execute());
		return result;
	}
}

class ObjectStyleMacro extends PreprocessorMacro {
	private final char[] fExpansion;
	final int fExpansionOffset;
	final int fEndOffset;
	private TokenList fExpansionTokens;
	
	public ObjectStyleMacro(char[] name, char[] expansion) {
		this(name, 0, expansion.length, null, expansion);
	}

	public ObjectStyleMacro(char[] name, int expansionOffset, int endOffset, TokenList expansion, char[] source) {
		super(name);
		fExpansionOffset= expansionOffset;
		fEndOffset= endOffset;
		fExpansion= source;
		fExpansionTokens= expansion;
		if (expansion != null) {
			setSource(expansion.first());
		}
	}

	public int getExpansionOffset() {
		return fExpansionOffset;
	}
	
	public int getExpansionEndOffset() {
		return fEndOffset;
	}
	
	private void setSource(Token t) {
		while (t != null) {
			t.fSource= this;
			t= (Token) t.getNext();
		}
	}

	public char[] getExpansion() {
		TokenList tl= getTokens(new MacroDefinitionParser(), new LexerOptions());
		StringBuffer buf= new StringBuffer();
		Token t= tl.first();
		if (t == null) {
			return CharArrayUtils.EMPTY;
		}
		int endOffset= t.getOffset();
		for (; t != null; t= (Token) t.getNext()) {
			if (endOffset < t.getOffset()) {
				buf.append(' ');
			}
			buf.append(t.getCharImage());
			endOffset= t.getEndOffset();
		}
		final int length= buf.length(); 
		final char[] expansion= new char[length];
		buf.getChars(0, length, expansion, 0);
		return expansion;
	}

	public char[] getExpansionImage() {
		final int length = fEndOffset - fExpansionOffset;
		if (length == fExpansion.length) {
			return fExpansion;
		}
		char[] result= new char[length];
		System.arraycopy(fExpansion, fExpansionOffset, result, 0, length);
		return result;
	}
	
	public TokenList getTokens(MacroDefinitionParser mdp, LexerOptions lexOptions) {
		if (fExpansionTokens == null) {
			fExpansionTokens= new TokenList();
			Lexer lex= new Lexer(fExpansion, fExpansionOffset, fEndOffset, lexOptions, ILexerLog.NULL, this);
			try {
				mdp.parseExpansion(lex, ILexerLog.NULL, getNameCharArray(), getParameterPlaceholderList(), fExpansionTokens);
			} catch (OffsetLimitReachedException e) {
			}
		}
		return fExpansionTokens;
	}
}


class FunctionStyleMacro extends ObjectStyleMacro {
	public static final int NO_VAARGS 	= 0;    // M(a)
	public static final int VAARGS	  	= 1;	// M(...)
	public static final int NAMED_VAARGS= 2;	// M(a...)
	
	final private char[][] fParamList;
	final private int fHasVarArgs;
	private char[] fSignature;
	
	public FunctionStyleMacro(char[] name, char[][] paramList, int hasVarArgs, char[] expansion) {
		super(name, expansion);
		fParamList = paramList;
		fHasVarArgs= hasVarArgs;
	}

	public FunctionStyleMacro(char[] name, char[][] paramList, int hasVarArgs, int expansionFileOffset, int endFileOffset, 
			TokenList expansion, char[] source) {
		super(name, expansionFileOffset, endFileOffset, expansion, source);
		fParamList = paramList;
		fHasVarArgs= hasVarArgs;
	}
	
	public char[][] getParameterList() {
		final int length = fParamList.length;
		if (fHasVarArgs == NO_VAARGS || length==0) {
			return fParamList;
		}
		char[][] result= new char[length][];
		System.arraycopy(fParamList, 0, result, 0, length-1);
		if (fHasVarArgs == VAARGS) {
			result[length-1]= Keywords.cpELLIPSIS;
		}
		else {
			final char[] param= fParamList[length-1];
			final int plen= param.length;
			final int elen = Keywords.cpELLIPSIS.length;
			final char[] rp= new char[plen+elen];
			System.arraycopy(param, 0, rp, 0, plen);
			System.arraycopy(Keywords.cpELLIPSIS, 0, rp, plen, elen);
			result[length-1]= rp;
		}
		return result;
	}

	public char[][] getParameterPlaceholderList() {
		return fParamList;
	}

	public char[] getSignature() {
	    if (fSignature != null) {
	        return fSignature;
	    }
	    
	    StringBuffer result= new StringBuffer();
	    result.append(getName());
	    result.append('(');
	    
	    final int lastIdx= fParamList.length-1;
	    if (lastIdx >= 0) {
	    	for (int i = 0; i < lastIdx; i++) {
	    		result.append(fParamList[i]);
	    		result.append(',');
	    	}
	    	switch(fHasVarArgs) {
	    	case VAARGS:
	    		result.append(Keywords.cpELLIPSIS);
	    		break;
	    	case NAMED_VAARGS:
	    		result.append(fParamList[lastIdx]);
	    		result.append(Keywords.cpELLIPSIS);
	    		break;
	    	default:
	    		result.append(fParamList[lastIdx]);
	    		break;
	    	}
	    }
	    result.append(')');
	    final int len= result.length();
	    final char[] sig= new char[len];
	    result.getChars(0, len, sig, 0);
	    fSignature= sig;
	    return sig;
	}
		
	/**
	 * Returns one of {@link #NO_VAARGS}, {@link #VAARGS} or {@link #NAMED_VAARGS}.
	 */
	public int hasVarArgs() {
		return fHasVarArgs;
	}
		
	public boolean isFunctionStyle() {
		return true;
	}
}
