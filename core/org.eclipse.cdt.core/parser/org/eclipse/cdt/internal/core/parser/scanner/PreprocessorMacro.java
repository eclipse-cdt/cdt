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
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;

/**
 * Models macros used by the preprocessor
 * @since 5.0
 */
abstract class PreprocessorMacro implements IPreprocessorMacro {
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
	
	public Object getAdapter(Class clazz) {
		return null;
	}
}

abstract class DynamicStyleMacro extends PreprocessorMacro {

	public DynamicStyleMacro(char[] name) {
		super(name);
	}
	public char[] getExpansion() {
		return execute().getCharImage();
	}
	public abstract Token execute();
}

class ObjectStyleMacro extends PreprocessorMacro {
	private static final Token NOT_INITIALIZED = new SimpleToken(0,0,0);
	
	private final char[] fExpansion;
	final int fExpansionOffset;
	final int fEndOffset;
//	private Token fExpansionTokens;
	
	public ObjectStyleMacro(char[] name, char[] expansion) {
		this(name, 0, expansion.length, NOT_INITIALIZED, expansion);
	}

	public ObjectStyleMacro(char[] name, int expansionOffset, int endOffset, Token expansion, char[] source) {
		super(name);
		fExpansionOffset= expansionOffset;
		fEndOffset= endOffset;
		fExpansion= source;
//		fExpansionTokens= expansion;
	}

	public int findParameter(char[] tokenImage) {
		return -1;
	}

	public char[] getExpansion() {
		final int length = fEndOffset - fExpansionOffset;
		if (length == fExpansion.length) {
			return fExpansion;
		}
		char[] result= new char[length];
		System.arraycopy(fExpansion, fEndOffset, result, 0, length);
		return result;
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
			Token expansion, char[] source) {
		super(name, expansionFileOffset, endFileOffset, expansion, source);
		fParamList = paramList;
		fHasVarArgs= hasVarArgs;
	}
	
	public char[][] getParamList() {
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
	
	public int findParameter(final char[] identifier) {
		for (int i=0; i < fParamList.length; i++) {
			if (CharArrayUtils.equals(fParamList[i], identifier)) {
				return i;
			}
		}
		return -1;
	}
	
	public boolean isFunctionStyle() {
		return true;
	}
}
