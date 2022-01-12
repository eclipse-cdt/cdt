/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Richard Eames
 *     Hansruedi Patzen (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.CStringValue;
import org.eclipse.cdt.internal.core.dom.parser.FloatingPointValue;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFunctionCall;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator.EvalException;

/**
 * Represents a C++ literal.
 */
public class CPPASTLiteralExpression extends ASTNode implements ICPPASTLiteralExpression {
	private static final EvalFixed EVAL_TRUE = new EvalFixed(CPPBasicType.BOOLEAN, PRVALUE, IntegralValue.create(true));
	private static final EvalFixed EVAL_FALSE = new EvalFixed(CPPBasicType.BOOLEAN, PRVALUE,
			IntegralValue.create(false));
	private static final EvalFixed EVAL_NULL_PTR = new EvalFixed(CPPBasicType.NULL_PTR, PRVALUE,
			IntegralValue.create(0));

	public static final CPPASTLiteralExpression INT_ZERO = new CPPASTLiteralExpression(lk_integer_constant,
			new char[] { '0' });

	private int fKind;
	/**
	 * The value of a CPPASTLiteralExpression consists of a literal and a suffix.
	 *
	 * E.g. 1f        -> literal: 1         suffix: f
	 *      1ull      -> literal: 1         suffix: ull
	 *      10.0_udl  -> literal: 10.0      suffix: _udl
	 *      "waldo"s  -> literal: "waldo"   suffix: s
	 *      'a'_udl   -> literal: 'a'       suffix: _udl
	 */
	private char[] fLiteral;
	private char[] fSuffix;
	private final char[] fNumericCompilerSuffixes;

	private int fStringLiteralSize; // Accounting for escape sequences and the null terminator.
	private ICPPEvaluation fEvaluation;
	private IBinding fUserDefinedLiteralOperator;
	private IASTImplicitName[] fImplicitNames;

	public CPPASTLiteralExpression(int kind, char[] value) {
		this(kind, value, CharArrayUtils.EMPTY);
	}

	public CPPASTLiteralExpression(int kind, char[] value, char[] numericCompilerSuffixes) {
		fKind = kind;
		fSuffix = getSuffix(kind, value, CharArrayUtils.EMPTY);
		fLiteral = getLiteral(value, fSuffix);
		fNumericCompilerSuffixes = (numericCompilerSuffixes == null) ? CharArrayUtils.EMPTY : numericCompilerSuffixes;
		fStringLiteralSize = -1;
	}

	private CPPASTLiteralExpression(CPPASTLiteralExpression other) {
		fKind = other.fKind;
		fLiteral = other.fLiteral;
		fSuffix = other.fSuffix;
		fNumericCompilerSuffixes = other.fNumericCompilerSuffixes;
		fStringLiteralSize = other.fStringLiteralSize;
		fEvaluation = other.fEvaluation;
		fUserDefinedLiteralOperator = other.fUserDefinedLiteralOperator;
		fImplicitNames = other.fImplicitNames;
	}

	@Override
	public CPPASTLiteralExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTLiteralExpression copy(CopyStyle style) {
		return copy(new CPPASTLiteralExpression(this), style);
	}

	@Override
	public int getKind() {
		return fKind;
	}

	@Override
	public void setKind(int kind) {
		assertNotFrozen();
		set(kind, getValue());
	}

	@Override
	public char[] getValue() {
		return CharArrayUtils.concat(fLiteral, fSuffix);
	}

	@Override
	public void setValue(char[] value) {
		assertNotFrozen();
		set(fKind, value);
	}

	/**
	 * Sets the kind, suffix and literal fields of this expression.
	 *
	 * @param kind the kind of this literal expression
	 * @param value the value of this literal expression,
	 *        this gets split into a literal and a suffix
	 */
	private void set(int kind, char[] value) {
		fKind = kind;
		fSuffix = getSuffix(kind, value, fSuffix);
		fLiteral = getLiteral(value, fSuffix);
		resetLazyFields();
	}

	/**
	 * Adds a suffix to this literal expression.
	 *
	 * @param suffix the suffix to be added.
	 */
	public void setSuffix(char[] suffix) {
		assertNotFrozen();
		fSuffix = (suffix == null) ? CharArrayUtils.EMPTY : suffix;
		resetLazyFields();
	}

	/**
	 * Resets the lazy evaluated fields. This is needed if any of the other fields change.
	 */
	private void resetLazyFields() {
		fStringLiteralSize = -1;
		fEvaluation = null;
		fUserDefinedLiteralOperator = null;
		fImplicitNames = null;
	}

	private boolean hasNumericCompilerSuffix() {
		if (hasNumericKind() && fSuffix.length == 1) {
			for (int j = 0; j < fNumericCompilerSuffixes.length; j++) {
				if (fSuffix[0] == fNumericCompilerSuffixes[j]) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasNumericKind() {
		return fKind == lk_integer_constant || fKind == lk_float_constant;
	}

	private static char[] getLiteral(char[] value, char[] suffix) {
		if (value == null) {
			return CharArrayUtils.EMPTY;
		}
		if (suffix == null || suffix.length == 0) {
			return value;
		}
		final char[] literal = CharArrayUtils.subarray(value, 0, value.length - suffix.length);
		return (literal == null) ? CharArrayUtils.EMPTY : literal;
	}

	private static char[] getSuffix(int kind, char[] value, char[] suffix) {
		if (value == null || value.length == 0) {
			return suffix;
		}
		int offset = 0;
		switch (kind) {
		case lk_float_constant:
		case lk_integer_constant:
			try {
				offset = (value[0] == '.') ? afterDecimalPoint(value, 0) : integerLiteral(value);
			} catch (ArrayIndexOutOfBoundsException e) {
			}
			break;
		case lk_string_literal:
			offset = CharArrayUtils.lastIndexOf('"', value, CharArrayUtils.indexOf('"', value) + 1) + 1;
			break;
		case lk_char_constant:
			offset = CharArrayUtils.lastIndexOf('\'', value, CharArrayUtils.indexOf('\'', value) + 1) + 1;
			break;
		}
		suffix = (offset > 0) ? CharArrayUtils.subarray(value, offset, -1) : suffix;
		return (suffix == null) ? CharArrayUtils.EMPTY : suffix;
	}

	private boolean hasNumericSuffix() {
		final int len = fSuffix.length;
		if (!hasSuffix() || !hasNumericKind() || len > 3) {
			return false;
		}
		/*
		 * 2.14.8.1
		 * "If a token matches both user-defined-literal and another literal kind, it is
		 * treated as the latter"
		 */
		if (len == 1) {
			switch (fSuffix[0]) {
			case 'u':
			case 'U':
			case 'f':
			case 'F':
			case 'l':
			case 'L':
				return true;
			}
		}
		if (len == 2) {
			switch (fSuffix[0]) {
			case 'u':
			case 'U':
				return Character.toLowerCase(fSuffix[1]) == 'l';
			case 'l':
			case 'L':
				return Character.toLowerCase(fSuffix[1]) == 'l' || Character.toLowerCase(fSuffix[1]) == 'u';
			}
		}
		if (len == 3) {
			switch (fSuffix[0]) {
			case 'u':
			case 'U':
				return Character.toLowerCase(fSuffix[1]) == 'l' && Character.toLowerCase(fSuffix[2]) == 'l';
			case 'l':
			case 'L':
				return Character.toLowerCase(fSuffix[1]) == 'l' && Character.toLowerCase(fSuffix[2]) == 'u';
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return new String(getValue());
	}

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		return IASTImplicitDestructorName.EMPTY_NAME_ARRAY; // Literal expression does not call destructors.
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitExpressions) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (action.shouldVisitImplicitNames) {
			for (IASTImplicitName name : getImplicitNames()) {
				if (!name.accept(action))
					return false;
			}
		}

		if (action.shouldVisitExpressions) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}

	private boolean hasSuffix() {
		return fSuffix.length > 0;
	}

	private int computeStringLiteralSize() {
		int start = 0, end = fLiteral.length - 1;
		boolean isRaw = false;

		// Skip past a prefix affecting the character type.
		if (fLiteral[0] == 'L' || fLiteral[0] == 'u' || fLiteral[0] == 'U') {
			if (fLiteral[1] == '8') {
				++start;
			}
			++start;
		}

		// If there is an 'R' prefix, skip past it but take note of it.
		if (fLiteral[start] == 'R') {
			++start;
			isRaw = true;
		}

		// Now we should have a quote-enclosed string. Skip past the quotes.
		if (!(fLiteral[start] == '"' && fLiteral[end] == '"')) {
			// Unexpected!
			return 0;
		}
		++start;
		--end;

		// If we have a raw string, skip past the raw prefix.
		if (isRaw) {
			while (fLiteral[start] != '(' && start <= end) {
				++start;
				--end;
			}

			// Now we should have a parenthesis-enclosed string.
			if (!(fLiteral[start] == '(' && fLiteral[end] == ')')) {
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
			} else if (fLiteral[start] == '\\') {
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
		return IntegralValue.create(fStringLiteralSize);
	}

	private IType getStringType() {
		IType type = new CPPBasicType(getBasicCharKind(), 0, this);
		type = new CPPQualifierType(type, true, false);
		return new CPPArrayType(type, getStringLiteralSize());
	}

	private IType getCharType() {
		return hasSuffix() ? getUserDefinedLiteralOperatorType() : new CPPBasicType(getBasicCharKind(), 0, this);
	}

	private IBinding getUserDefinedLiteralOperator() {
		if (hasSuffix() && !hasNumericSuffix() && fUserDefinedLiteralOperator == null) {
			try {
				fUserDefinedLiteralOperator = CPPSemantics.findUserDefinedLiteralOperator(this);
				if (fUserDefinedLiteralOperator instanceof IProblemBinding && hasNumericCompilerSuffix()) {
					fUserDefinedLiteralOperator = null;
					return null;
				}
			} catch (DOMException e) {
			}
			if (fUserDefinedLiteralOperator == null) {
				fUserDefinedLiteralOperator = new ProblemBinding(this, ISemanticProblem.BINDING_NOT_FOUND, fSuffix);
			}
		}
		return fUserDefinedLiteralOperator;
	}

	// 13.5.8
	private IType getUserDefinedLiteralOperatorType() {
		IBinding func = getUserDefinedLiteralOperator();
		if (func != null && func instanceof ICPPFunction) {
			return ((ICPPFunction) func).getType().getReturnType();
		}

		return new ProblemType(ISemanticProblem.TYPE_UNRESOLVED_NAME);
	}

	public char[] getOperatorName() {
		return CharArrayUtils.concat("operator \"\"".toCharArray(), fSuffix); //$NON-NLS-1$
	}

	public Kind getBasicCharKind() {
		switch (fLiteral[0]) {
		case 'L':
			return Kind.eWChar;
		case 'U':
			return Kind.eChar32;
		case 'u':
			// Bug 526724 u8 should result in Kind.eChar
			if (fLiteral[1] != '8') {
				return Kind.eChar16;
			}
			//$FALL-THROUGH$
		default:
			return Kind.eChar;
		}
	}

	private IType classifyTypeOfFloatLiteral() {
		Kind kind = Kind.eDouble;
		int flags = 0;
		if (hasSuffix()) {
			if (hasNumericSuffix()) {
				switch (fSuffix[0]) {
				case 'f':
				case 'F':
					kind = Kind.eFloat;
					break;
				case 'l':
				case 'L':
					flags |= IBasicType.IS_LONG;
					break;
				}
			} else {
				IType type = getUserDefinedLiteralOperatorType();
				if (type instanceof IProblemType && hasNumericCompilerSuffix()) {
					switch (fSuffix[0]) {
					case 'i':
					case 'j':
						flags |= IBasicType.IS_IMAGINARY;
						break;
					}
				} else {
					return type;
				}
			}
		}
		return new CPPBasicType(kind, flags, this);
	}

	private IType classifyTypeOfIntLiteral() {
		Kind kind = Kind.eInt;
		int flags = 0;

		if (hasSuffix()) {
			if (hasNumericSuffix()) {
				int makelong = 0;
				for (char c : fSuffix) {
					switch (c) {
					case 'u':
					case 'U':
						flags |= IBasicType.IS_UNSIGNED;
						break;
					case 'l':
					case 'L':
						makelong++;
						break;
					}
				}
				if (makelong > 1) {
					flags |= IBasicType.IS_LONG_LONG;
				} else if (makelong == 1) {
					flags |= IBasicType.IS_LONG;
				}
			} else {
				IType type = getUserDefinedLiteralOperatorType();
				if (type instanceof IProblemType && hasNumericCompilerSuffix()) {
					switch (fSuffix[0]) {
					case 'i':
					case 'j':
						flags |= IBasicType.IS_IMAGINARY;
						break;
					}
				} else {
					return type;
				}
			}
		}
		return new CPPBasicType(kind, flags, this);
	}

	private static int integerLiteral(char[] value) {
		int i = 0;
		char c = value[i++];

		if (c == '0' && i < value.length) {
			// Probably octal/hex/binary
			c = value[i];
			switch ((c | 0x20)) {
			case 'x':
				return probablyHex(value, i);
			case 'b':
				return probablyBinary(value, i);
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
				/* octal-literal:
				*   0
				*   octal-literal octal-digit
				*/
				while (isOctalOrSeparator(c) && i < value.length) {
					c = value[++i];
				}
				break;
			case '.':
				return afterDecimalPoint(value, i);
			}
			/*
			 * If there is an 8 or 9, then we have a malformed octal
			 */
			if (c == '8' || c == '9') {
				// eat remaining numbers
				c = value[i];
				while (Character.isDigit(c) && i < value.length) {
					c = value[++i];
				}
			}

			/*
			 * A floating-point constant could also have a leading zero
			 */
			return handleDecimalOrExponent(value, c, i);
		} else if (Character.isDigit(c)) {
			/* decimal-literal :
			*    nonzero-digit         (c has to be this to get into this else)
			*    decimal-literal digit
			*/
			c = value[i];
			while (isDigitOrSeparator(c) && i < value.length) {
				c = value[++i];
			}

			return handleDecimalOrExponent(value, c, i);
		} else {
			// Somehow we got called and there wasn't a digit
			// Shouldn't get here
			assert false;
		}

		return i;
	}

	/*
	 * Consumes a decimal point or exponent, if present.
	 */
	private static int handleDecimalOrExponent(char[] value, char c, int i) {
		if (c == '.') {
			return afterDecimalPoint(value, i);
		} else if ((c | 0x20) == 'e') {
			return exponentPart(value, i);
		}
		return i;
	}

	/*
	 * Called with the expectation that value[i] == '.'
	 */
	private static int afterDecimalPoint(char[] value, int i) {
		char c = value[++i];
		while (isDigitOrSeparator(c) && i < value.length) {
			c = value[++i];
		}

		if ((c | 0x20) == 'e') {
			return exponentPart(value, i);
		}

		return i;
	}

	/*
	 * Called with the expectation that c == 'e'
	 */
	private static int exponentPart(char[] value, int i) {
		char c = value[++i];

		// optional '+' or '-'
		if (c == '+' || c == '-') {
			c = value[++i];
		}

		while (isDigitOrSeparator(c) && i < value.length) {
			c = value[++i];
		}
		// If there were no digits following the 'e' then we have
		// D.De or .De which is a UDL on a double

		return i--;
	}

	// GCC's binary constant notation
	private static int probablyBinary(char[] value, int i) {
		char c = value[++i];

		if (c == '1' || c == '0') {
			while (c == '1' || c == '0' || c == '\'' && i < value.length) {
				c = value[i++];
			}
			if (Character.isDigit(c)) {
				// UDL can't begin with digit, so this is a malformed binary
				return -1;
			} else if (c == '.') {
				// no such thing as binary floating point
				c = value[++i];
				while (isDigitOrSeparator(c) && i < value.length) {
					c = value[i++];
				}
			}
		} else {
			// Here we have 0b or 0B
			return i - 1;
		}
		return i;
	}

	private static int probablyHex(char[] value, int i) {
		/* hexadecimal-literal
		 *   0x hexadecimal-digit
		 *   0X hexadecimal-digit
		 *   hexadecimal-literal hexadecimal-digit
		 */
		char c = value[++i];
		if (isHexDigitOrSeparator(c)) {
			while (isHexDigitOrSeparator(c) && i < value.length) {
				c = value[++i];
			}
			if (c == '.') {
				// Could be GCC's hex float
				return hexFloatAfterDecimal(value, i);
			} else if ((c | 0x20) == 'p') {
				return hexFloatExponent(value, i);
			}
		} else {
			return i - 1;
		}

		return i;
	}

	// Assumes value[i] == '.'
	private static int hexFloatAfterDecimal(char[] value, int i) {
		// 0xHHH.
		char c = value[++i];
		if (isHexDigitOrSeparator(c)) {
			while (isHexDigitOrSeparator(c) && i < value.length) {
				c = value[++i];
			}

			if ((c | 0x20) == 'p') {
				return hexFloatExponent(value, i);
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
	private static int hexFloatExponent(char[] value, int i) {
		// 0xHH.HH[pP][-+]?DDDD
		char c = value[++i];

		if (c == '-' || c == '+') {
			c = value[++i];
		}

		if (Character.isDigit(c)) {
			while (isDigitOrSeparator(c) && i < value.length) {
				c = value[++i];
			}
		} else {
			return i - 1;
		}
		return i;
	}

	private static boolean isHexDigitOrSeparator(char c) {
		char lc = Character.toLowerCase(c);
		return (lc <= 'f' && lc >= 'a') || (c <= '9' && c >= '0') || (c == '\'');
	}

	private static boolean isOctalOrSeparator(final char c) {
		return (c >= '0' && c <= '7') || (c == '\'');
	}

	private static boolean isDigitOrSeparator(final char c) {
		return Character.isDigit(c) || (c == '\'');
	}

	/**
	 * @deprecated, use {@link #setValue(char[])}, instead.
	 */
	@Override
	@Deprecated
	public void setValue(String value) {
		assertNotFrozen();
		set(fKind, value.toCharArray());
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
			fEvaluation = createEvaluation();
		return fEvaluation;
	}

	private ICPPEvaluation createLiteralEvaluation() {
		switch (fKind) {
		case lk_this: {
			IScope scope = CPPVisitor.getContainingScope(this);
			IType type = CPPVisitor.getImpliedObjectType(scope);
			if (type == null)
				return EvalFixed.INCOMPLETE;
			return new EvalFixed(new CPPPointerType(type), PRVALUE, IntegralValue.THIS);
		}
		case lk_true:
			return EVAL_TRUE;
		case lk_false:
			return EVAL_FALSE;
		case lk_char_constant:
			return new EvalFixed(getCharType(), PRVALUE, createCharValue());
		case lk_float_constant:
			return new EvalFixed(classifyTypeOfFloatLiteral(), PRVALUE, FloatingPointValue.create(fLiteral));
		case lk_integer_constant:
			return new EvalFixed(classifyTypeOfIntLiteral(), PRVALUE, createIntValue());
		case lk_string_literal:
			return new EvalFixed(getStringType(), LVALUE, CStringValue.create(fLiteral));
		case lk_nullptr:
			return EVAL_NULL_PTR;
		}
		return EvalFixed.INCOMPLETE;
	}

	private ICPPEvaluation createEvaluation() {
		ICPPEvaluation literalEval = createLiteralEvaluation();

		IBinding udlOperator = getUserDefinedLiteralOperator();
		if (udlOperator != null && literalEval != EvalFixed.INCOMPLETE) {
			if (udlOperator instanceof ICPPFunction) {
				ICPPFunction udlOpFunction = (ICPPFunction) udlOperator;
				EvalBinding op = new EvalBinding(udlOpFunction, udlOpFunction.getType(), this);
				ICPPEvaluation[] args = null;

				ICPPParameter params[] = udlOpFunction.getParameters();
				int paramCount = params.length;
				if (paramCount == 0) {
					// TODO: Support literal operator templates.
					args = new ICPPEvaluation[] { op };
				} else if (paramCount == 1) {
					//this means that we need to fall back to the raw literal operator
					if (params[0].getType() instanceof IPointerType) {
						char numValue[] = fLiteral;
						int numLen = numValue.length;
						char strValue[] = new char[numLen + 2];
						strValue[0] = '"';
						strValue[numLen + 1] = '"';
						System.arraycopy(numValue, 0, strValue, 1, numLen);

						IType type = new CPPBasicType(Kind.eChar, 0, this);
						type = new CPPQualifierType(type, true, false);
						type = new CPPArrayType(type, IntegralValue.create(numLen + 1));
						EvalFixed strEval = new EvalFixed(type, LVALUE, CStringValue.create(strValue));
						args = new ICPPEvaluation[] { op, strEval };
					} else {
						args = new ICPPEvaluation[] { op, literalEval };
					}
				} else if (paramCount == 2) {
					IValue sizeValue = IntegralValue.create(computeStringLiteralSize() - 1);
					EvalFixed literalSizeEval = new EvalFixed(CPPBasicType.INT, PRVALUE, sizeValue);
					args = new ICPPEvaluation[] { op, literalEval, literalSizeEval };
				}
				return new EvalFunctionCall(args, null, this);
			}
		}

		//has a user-defined literal suffix but didn't find a udl operator function => error
		if (hasSuffix() && !hasNumericSuffix() && !hasNumericCompilerSuffix()) {
			return EvalFixed.INCOMPLETE;
		}

		return literalEval;
	}

	private IValue createCharValue() {
		try {
			final int index = (fLiteral.length > 1 && fLiteral[0] == 'L') ? 2 : 1;
			return IntegralValue.create(ExpressionEvaluator.getChar(fLiteral, index));
		} catch (EvalException e) {
			return IntegralValue.UNKNOWN;
		}
	}

	private IValue createIntValue() {
		try {
			return IntegralValue.create(ExpressionEvaluator.getNumber(fLiteral));
		} catch (EvalException e) {
			return IntegralValue.UNKNOWN;
		}
	}

	@Override
	public IType getExpressionType() {
		return CPPEvaluation.getType(this);
	}

	@Override
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}

	@Override
	public ValueCategory getValueCategory() {
		return (fKind == lk_string_literal) ? LVALUE : PRVALUE;
	}

	@Override
	public IASTImplicitName[] getImplicitNames() {
		if (fImplicitNames == null) {
			if (!hasSuffix() || hasNumericSuffix()) {
				fImplicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			} else {
				IBinding userDefinedLiteralOperator = getUserDefinedLiteralOperator();
				if (userDefinedLiteralOperator == null && hasNumericCompilerSuffix()) {
					fImplicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
				} else {
					CPPASTImplicitName operatorName = new CPPASTImplicitName(fSuffix, this);
					operatorName.setOperator(true);
					operatorName.setBinding(userDefinedLiteralOperator);
					operatorName.setOffsetAndLength(getOffset() + fLiteral.length, fSuffix.length);
					fImplicitNames = new IASTImplicitName[] { operatorName };
				}
			}
		}
		return fImplicitNames;
	}
}
