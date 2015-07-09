/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Richard Eames
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator.EvalException;

/**
 * Represents a C++ literal.
 */
public class CPPASTLiteralExpression extends ASTNode implements ICPPASTLiteralExpression {
	private static final EvalFixed EVAL_TRUE = new EvalFixed(CPPBasicType.BOOLEAN, PRVALUE, Value.create(1));
	private static final EvalFixed EVAL_FALSE = new EvalFixed(CPPBasicType.BOOLEAN, PRVALUE, Value.create(0));
	private static final EvalFixed EVAL_NULL_PTR = new EvalFixed(CPPBasicType.NULL_PTR, PRVALUE, Value.create(0));

	public static final CPPASTLiteralExpression INT_ZERO =
			new CPPASTLiteralExpression(lk_integer_constant, new char[] {'0'});
	
    private int fKind;
    private char[] fValue = CharArrayUtils.EMPTY;
    private int fStringLiteralSize = -1;  // Accounting for escape sequences and the null terminator.
    private char[] fSuffix = CharArrayUtils.EMPTY;
    private boolean fIsCompilerSuffix = true;
	private ICPPEvaluation fEvaluation;

    public CPPASTLiteralExpression() {
	}

	public CPPASTLiteralExpression(int kind, char[] value) {
		this.fKind = kind;
		this.fValue = value;
	}
	
	public CPPASTLiteralExpression(int kind, char[] value, char[] suffix) {
		this(kind, value);
		this.setSuffix(suffix);
	}

	@Override
	public CPPASTLiteralExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTLiteralExpression copy(CopyStyle style) {
		CPPASTLiteralExpression copy = new CPPASTLiteralExpression(fKind,
				fValue == null ? null : fValue.clone(),
				fSuffix == null ? null : fSuffix.clone());
		copy.setOffsetAndLength(this);
		return copy(copy, style);
	}

	@Override
	public int getKind() {
        return fKind;
    }

    @Override
	public void setKind(int value) {
        assertNotFrozen();
        fKind = value;
    }

    @Override
	public char[] getValue() {
    	return fValue;
    }

    @Override
	public void setValue(char[] value) {
        assertNotFrozen();
    	this.fValue= value;
    }
    
    public char[] getSuffix() {
		return fSuffix;
	}

	public void setSuffix(char[] suffix) {
		this.fSuffix = suffix;
	}
	
	public void calculateSuffix() {
		this.calculateSuffix(CharArrayUtils.EMPTY);
	}
	
	/**
	 * Returns the suffix of a user-defined literal integer or float
	 * @param compilerSuffixes
	 */
	public void calculateSuffix(char[] compilerSuffixes) {
		try {
			switch (fKind) {
			case lk_float_constant:
			case lk_integer_constant:
				int udOffset = (fValue[0] == '.' ? afterDecimalPoint(0) : integerLiteral());
				if (udOffset > 0) {
					/*
					 * 2.14.8.1
					 * "If a token matches both user-defined-literal and another literal kind, it is
					 * treated as the latter"
					 */
					setSuffix(CharArrayUtils.subarray(fValue, udOffset, -1));
					for (int i = 0; i < fSuffix.length; i++) {
						switch (fSuffix[i]) {
						case 'l': case 'L': 
						case 'u': case 'U':
						case 'f': case 'F':
							continue;
						}
						for (int j = 0; j < compilerSuffixes.length; j++) {
							if (fSuffix[i] == compilerSuffixes[j]) {
								continue;
							}
						}
						fIsCompilerSuffix = false;
						// Remove the suffix from the value if it's a UDL
						setValue(CharArrayUtils.subarray(fValue, 0, udOffset));
						break;
					}
				}
				break;
			case lk_string_literal: 
				{
					final int offset = CharArrayUtils.lastIndexOf('"', fValue, CharArrayUtils.indexOf('"', fValue) + 1);
					if (offset > 0) {
						setSuffix(CharArrayUtils.subarray(fValue, offset + 1, -1));
					}
				}
				break;
			case lk_char_constant: 
				{
					final int offset = CharArrayUtils.lastIndexOf('\'', fValue, CharArrayUtils.indexOf('\'', fValue) + 1);
					if (offset > 0) {
						setSuffix(CharArrayUtils.subarray(fValue, offset + 1, -1));
					}
				}
				break;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// pass
		}
	}
	
	@Override
	public String toString() {
        return new String(fValue);
    }

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		return IASTImplicitDestructorName.EMPTY_NAME_ARRAY; // Literal expression does not call destructors.
	}

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitExpressions) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        if (action.shouldVisitExpressions) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}  
        return true;
    }
    
    private int computeStringLiteralSize() {
    	int start = 0, end = fValue.length - 1;
    	boolean isRaw = false;
    	
    	// Skip past a prefix affecting the character type.
    	if (fValue[0] == 'L' || fValue[0] == 'u' || fValue[0] == 'U') {
    		++start;
    	}
    	
    	// If there is an 'R' prefix, skip past it but take note of it.
    	if (fValue[start] == 'R') {
    		++start;
    		isRaw = true;
    	}
    	
    	// Now we should have a quote-enclosed string. Skip past the quotes.
    	if (!(fValue[start] == '"' && fValue[end] == '"')) {
    		// Unexpected!
    		return 0;
    	}
    	++start;
    	--end;
    	
    	// If we have a raw string, skip past the raw prefix.
    	if (isRaw) {
    		while (fValue[start] != '(' && start <= end) {
    			++start;
    			--end;
    		}
    		
    		// Now we should have a parenthesis-enclosed string.
    		if (!(fValue[start] == '(' && fValue[end] == ')')) {
    			// Unexpected!
    			return 0;
    		}
    		
    		// Since the string is raw, we don't need to process
    		// escape sequences, so the size is just the number
    		// of remaining characters, plus 1 for the null terminator.
    		return (end - start + 1) + 1;
    	}
    	
    	// Otherwise, we have a non-raw string and we need to
    	// process escape sequences.
    	int length = 0;
    	boolean escaping = false;
    	for (; start <= end; ++start) {
    		if (escaping) {
    			escaping = false;
    			++length;
    		} else if (fValue[start] == '\\') {
				escaping = true;
			} else {
				++length;
			}
    		// TODO: Handle fancier things like octal literals.
    	}
    	
    	// + 1 for null terminator.
    	return length + 1;
    }
    
	private IValue getStringLiteralSize() {
		if (fStringLiteralSize == -1) {
			fStringLiteralSize = computeStringLiteralSize();
		}
		return Value.create(fStringLiteralSize);
	}
	
	private IType getStringType() {
		if (fSuffix.length > 0) {
			return getUserDefinedLiteralOperatorType();
		}
		
		IType type = new CPPBasicType(getBasicCharKind(), 0, this);
		type = new CPPQualifierType(type, true, false);
		return new CPPArrayType(type, getStringLiteralSize());
	}
	
	private IType getCharType() {
		return fSuffix.length > 0 ? getUserDefinedLiteralOperatorType() : new CPPBasicType(getBasicCharKind(), 0, this);
    }
	
	// 13.5.8
	private IType getUserDefinedLiteralOperatorType() {
		IType ret = new ProblemType(ISemanticProblem.TYPE_UNRESOLVED_NAME);
		
		try {
			IBinding func = CPPSemantics.findUserDefinedLiteralOperator(this);
			if (func != null && func instanceof ICPPFunction) {
				ret = ((ICPPFunction) func).getType().getReturnType();
			}
		} catch (DOMException e) { /* ignore and return the problem type */ }
		
		return ret;
	}
	
	public char[] getOperatorName() {
		return CharArrayUtils.concat("operator \"\"".toCharArray(), fSuffix); //$NON-NLS-1$
	}
	
	public Kind getBasicCharKind() {
		switch (fValue[0]) {
    	case 'L':
    		return Kind.eWChar;
    	case 'u':
    		return Kind.eChar16;
    	case 'U':
    		return Kind.eChar32;
    	default:
    		return Kind.eChar;
    	}
	}
    
	private IType classifyTypeOfFloatLiteral() {
		final char[] lit= fSuffix;
		final int len= lit.length;
		Kind kind= Kind.eDouble;
		int flags= 0;
		if (len > 0) {
			if (fIsCompilerSuffix) {
				switch (lit[len - 1]) {
				case 'f': case 'F':
					kind= Kind.eFloat;
					break;
				case 'l': case 'L':
					flags |= IBasicType.IS_LONG;
					break;
				}
			} else {
				return getUserDefinedLiteralOperatorType();
			}
		}
		return new CPPBasicType(kind, flags, this);
	}

	private IType classifyTypeOfIntLiteral() {
		int makelong= 0;
		boolean unsigned= false;
		final char[] lit= fSuffix;
		int flags= 0;
		
		if (fIsCompilerSuffix) {
			for (int i= lit.length - 1; i >= 0; i--) {
				final char c= lit[i];
				if (!(c > 'f' && c <= 'z') && !(c > 'F' && c <= 'Z')) {
					break;
				}
				switch (c) {
				case 'u':
				case 'U':
					unsigned = true;
					break;
				case 'l':
				case 'L':
					makelong++;
					break;
				}
			}
	
			if (unsigned) {
				flags |= IBasicType.IS_UNSIGNED;
			}
			
			if (makelong > 1) {
				flags |= IBasicType.IS_LONG_LONG;
			} else if (makelong == 1) {
				flags |= IBasicType.IS_LONG;
			} 
		} else if (lit.length > 0) {
			return getUserDefinedLiteralOperatorType();
		}
		return new CPPBasicType(Kind.eInt, flags, this);
	}
	
	private int integerLiteral() {
		int i = 0;
		char c = fValue[i++];
		
		if (c == '0' && i < fValue.length) {
			// Probably octal/hex/binary
			c = fValue[i];
			switch ((c | 0x20)) {
			case 'x':
				return probablyHex(i);
			case 'b':
				return probablyBinary(i);
			case '0': case '1': case '2': case '3':
			case '4': case '5': case '6': case '7':
				/* octal-literal:
				*   0
				*   octal-literal octal-digit
				*/
				while (isOctal(c) && i < fValue.length) {
					c = fValue[++i];
				}
				break;
			case '.':
				return afterDecimalPoint(i);
			}
			/*
			 * If there is an 8 or 9, then we have a malformed octal
			 */
			if (c == '8' || c == '9') {
				// eat remaining numbers
				c = fValue[i];
				while (Character.isDigit(c) && i < fValue.length) {
					c = fValue[++i];
				}
				return i;
			}
		} else if (Character.isDigit(c)) {
			/* decimal-literal :
			*    nonzero-digit         (c has to be this to get into this else)
			*    decimal-literal digit
			*/
			c = fValue[i];
			while (Character.isDigit(c) && i < fValue.length) {
				c = fValue[++i];
			}
			
			if (c == '.') {
				return afterDecimalPoint(i);
			} else if ((c | 0x20) == 'e') {
				return exponentPart(i);
			}
		} else {
			// Somehow we got called and there wasn't a digit
			// Shouldn't get here
			assert false;
		}
		
		return i;
	}
	
	/*
	 * Called with the expectation that fValue[i] == '.'
	 */
	private int afterDecimalPoint(int i) {
		char c = fValue[++i];
		while (Character.isDigit(c) && i < fValue.length) {
			c = fValue[++i];
		}
		
		if ((c | 0x20) == 'e') {
			return exponentPart(i);
		}
		
		return i;
	}
	
	/*
	 * Called with the expectation that c == 'e'
	 */
	private int exponentPart(int i) {
		char c = fValue[++i];
		
		// optional '+' or '-'
		if (c == '+' || c == '-') {
			c = fValue[++i];
		}
		
		while (Character.isDigit(c) && i < fValue.length) {
			c = fValue[++i];
		}
		// If there were no digits following the 'e' then we have
		// D.De or .De which is a UDL on a double
		
		return i--;
	}
	
	// GCC's binary constant notation
	private int probablyBinary(int i) {
		char c = fValue[++i];
		
		if (c == '1' || c == '0') {
			while (c == '1' || c == '0' && i < fValue.length) {
				c = fValue[i++];
			}
			if (Character.isDigit(c)) {
				// UDL can't begin with digit, so this is a malformed binary
				return -1;
			} else if (c == '.') {
				// no such thing as binary floating point
				c = fValue[++i];
				while (Character.isDigit(c) && i < fValue.length) {
					c = fValue[i++];
				}
			}
		} else {
			// Here we have 0b or 0B
			return i - 1;
		}
		return i;
	}
	
	private int probablyHex(int i) {
		/* hexadecimal-literal
		 *   0x hexadecimal-digit
		 *   0X hexadecimal-digit
		 *   hexadecimal-literal hexadecimal-digit
		 */
		char c = fValue[++i];
		if (isHexDigit(c)) {
			while (isHexDigit(c) && i < fValue.length) {
				c = fValue[++i];
			}
			if (c == '.') {
				// Could be GCC's hex float
				return hexFloatAfterDecimal(i);
			} else if ((c | 0x20) == 'p') {
				return hexFloatExponent(i);
			}
		} else {
			return i - 1;
		}
		
		return i;
	}
	
	// Assumes fValue[i] == '.'
	private int hexFloatAfterDecimal(int i) {
		// 0xHHH.
		char c = fValue[++i];
		if (isHexDigit(c)) {
			while (isHexDigit(c) && i < fValue.length) {
				c = fValue[++i];
			}
			
			if ((c | 0x20) == 'p') {
				return hexFloatExponent(i);
			} else {
				// The parser is very confused at this point
				// as the expression is 0x1.f
				return -1;
			}
		}
		
		// Probably shouldn't be able to get here
		// we have 0xHHH.
		return -1;
	}
	
	// Assumes image[i] == 'p'
	private int hexFloatExponent(int i) {
		// 0xHH.HH[pP][-+]?DDDD
		char c = fValue[++i];
		
		if (c == '-' || c == '+') {
			c = fValue[++i];
		}
		
		if (Character.isDigit(c)) {
			while (Character.isDigit(c) && i < fValue.length) {
				c = fValue[++i];
			}
		} else {
			return i - 1;
		}
		return i;
	}
	
	private boolean isHexDigit(char c) {
		c |= 0x20;
		return ((c <= 'f' && c >= 'a') || (c <= '9' && c >= '0'));
	}
	
	private boolean isOctal(final char c) {
		return c >= '0' && c <= '7';
	}

    /**
     * @deprecated, use {@link #setValue(char[])}, instead.
     */
    @Override
	@Deprecated
	public void setValue(String value) {
        assertNotFrozen();
        this.fValue = value.toCharArray();
    }

    /**
     * @deprecated use {@link #CPPASTLiteralExpression(int, char[])}, instead.
     */
	@Deprecated
	public CPPASTLiteralExpression(int kind, String value) {
		this(kind, value.toCharArray());
	}
	
	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null)
			fEvaluation= createEvaluation();
		return fEvaluation;
	}
	
	private ICPPEvaluation createEvaluation() {
    	switch (fKind) {
    		case lk_this: {
    			IScope scope = CPPVisitor.getContainingScope(this);
    			IType type= CPPVisitor.getImpliedObjectType(scope);
    			if (type == null) 
    				return EvalFixed.INCOMPLETE;
    			return new EvalFixed(new CPPPointerType(type), PRVALUE, Value.UNKNOWN);
    		}
    		case lk_true:
    			return EVAL_TRUE;
    		case lk_false:
    			return EVAL_FALSE;
    		case lk_char_constant:
    			return new EvalFixed(getCharType(), PRVALUE, createCharValue());
    		case lk_float_constant:
    			return new EvalFixed(classifyTypeOfFloatLiteral(), PRVALUE, Value.UNKNOWN);
    		case lk_integer_constant:
    			return new EvalFixed(classifyTypeOfIntLiteral(), PRVALUE, createIntValue());
    		case lk_string_literal:
    			return new EvalFixed(getStringType(), LVALUE, Value.UNKNOWN);
    		case lk_nullptr:
    			return EVAL_NULL_PTR;
    	}
		return EvalFixed.INCOMPLETE;
	}

	private IValue createCharValue() {
		try {
			final char[] image= getValue();
			if (image.length > 1 && image[0] == 'L') 
				return Value.create(ExpressionEvaluator.getChar(image, 2));
			return Value.create(ExpressionEvaluator.getChar(image, 1));
		} catch (EvalException e) {
			return Value.UNKNOWN;
		}
	}
	
	private IValue createIntValue() {
		try {
			return Value.create(ExpressionEvaluator.getNumber(getValue()));
		} catch (EvalException e) {
			return Value.UNKNOWN;
		}
	}

	@Override
	public IType getExpressionType() {
		return getEvaluation().getTypeOrFunctionSet(this);
	}
	
	@Override
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}

	@Override
	public ValueCategory getValueCategory() {
		return getKind() == lk_string_literal ? LVALUE : PRVALUE;
	}
}
