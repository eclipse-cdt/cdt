/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - added J2SE 1.5 support
 *******************************************************************************/
package org.eclipse.cdt.core.model.util;

import org.eclipse.cdt.internal.core.CharOperation;

//TODO move this class to CoreModel?

/**
 * Provides methods for encoding and decoding type and method signature strings.
 * <p>
 * Signatures obtained from parsing source (".java") files differ subtly from
 * ones obtained from pre-compiled binary (".class") files in class names are
 * usually left unresolved in the former. For example, the normal resolved form
 * of the type "String" embeds the class's package name ("Ljava.lang.String;"
 * or "Ljava/lang/String;"), whereas the unresolved form contains only what is
 * written "QString;".
 * </p>
 * <p>
 * Generic types introduce to the Java language in J2SE 1.5 add three new
 * facets to signatures: type variables, parameterized types with type arguments,
 * and formal type parameters. <it>Rich</it> signatures containing these facets
 * only occur when dealing with code that makes overt use of the new language
 * features. All other code, and certainly all Java code written or compiled
 * with J2SE 1.4 or earlier, involved only <it>simple</it> signatures.
 * </p>
 * <p>
 * The syntax for a type signature is:
 * <pre>
 * TypeSignature ::=
 *     "B"  // byte
 *   | "C"  // char
 *   | "D"  // double
 *   | "F"  // float
 *   | "I"  // int
 *   | "J"  // long
 *   | "S"  // short
 *   | "V"  // void
 *   | "Z"  // boolean
 *   | "T" + Identifier + ";" // type variable
 *   | "[" + TypeSignature  // array X[]
 *   | ResolvedClassTypeSignature
 *   | UnresolvedClassTypeSignature
 * 
 * ResolvedClassTypeSignature ::= // resolved named type (in compiled code)
 *     "L" + Identifier + OptionalTypeArguments
 *           ( ( "." | "/" ) + Identifier + OptionalTypeArguments )* + ";"
 * 
 * UnresolvedClassTypeSignature ::= // unresolved named type (in source code)
 *     "Q" + Identifier + OptionalTypeArguments
 *           ( ( "." | "/" ) + Identifier + OptionalTypeArguments )* + ";"
 * 
 * OptionalTypeArguments ::=
 *     "&lt;" + TypeArgument+ + "&gt;" 
 *   |
 * 
 * TypeArgument ::=
 *   | TypeSignature
 *   | "*" // wildcard ?
 *   | "+" TypeSignature // wildcard ? extends X
 *   | "-" TypeSignature // wildcard ? super X
 * </pre>
 * </p>
 * <p>
 * Examples:
 * <ul>
 *   <li><code>"[[I"</code> denotes <code>int[][]</code></li>
 *   <li><code>"Ljava.lang.String;"</code> denotes <code>java.lang.String</code> in compiled code</li>
 *   <li><code>"QString;"</code> denotes <code>String</code> in source code</li>
 *   <li><code>"Qjava.lang.String;"</code> denotes <code>java.lang.String</code> in source code</li>
 *   <li><code>"[QString;"</code> denotes <code>String[]</code> in source code</li>
 *   <li><code>"QMap&lt;QString;&ast;&gt;;"</code> denotes <code>Map&lt;String,?&gt;</code> in source code</li>
 *   <li><code>"Qjava.util.List&ltTV;&gt;;"</code> denotes <code>java.util.List&lt;V&gt;</code> in source code</li>
 * </ul>
 * </p>
 * <p>
 * The syntax for a method signature is: 
 * <pre>
 * MethodSignature ::= "(" + ParamTypeSignature* + ")" + ReturnTypeSignature
 * ParamTypeSignature ::= TypeSignature
 * ReturnTypeSignature ::= TypeSignature
 * </pre>
 * <p>
 * Examples:
 * <ul>
 *   <li><code>"()I"</code> denotes <code>int foo()</code></li>
 *   <li><code>"([Ljava.lang.String;)V"</code> denotes <code>void foo(java.lang.String[])</code> in compiled code</li>
 *   <li><code>"(QString;)QObject;"</code> denotes <code>Object foo(String)</code> in source code</li>
 * </ul>
 * </p>
 * <p>
 * The syntax for a formal type parameter signature is:
 * <pre>
 * FormalTypeParameterSignature ::=
 *     TypeVariableName + OptionalClassBound + InterfaceBound*
 * TypeVariableName ::= Identifier
 * OptionalClassBound ::=
 *     ":"
 *   | ":" + TypeSignature
 * InterfaceBound ::= 
 *     ":" + TypeSignature
 * </pre>
 * <p>
 * Examples:
 * <ul>
 *   <li><code>"X:"</code> denotes <code>X</code></li>
 *   <li><code>"X:QReader;"</code> denotes <code>X extends Reader</code> in source code</li>
 *   <li><code>"X:QReader;:QSerializable;"</code> denotes <code>X extends Reader & Serializable</code> in source code</li>
 * </ul>
 * </p>
 * <p>
 * This class provides static methods and constants only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 */
public final class Signature {

	/**
	 * Character constant indicating the primitive type boolean in a signature.
	 * Value is <code>'Z'</code>.
	 */
	public static final char C_BOOLEAN 		= 'Z';

	/**
	 * Character constant indicating the primitive type byte in a signature.
	 * Value is <code>'B'</code>.
	 */
	public static final char C_BYTE 		= 'B';

	/**
	 * Character constant indicating the primitive type char in a signature.
	 * Value is <code>'C'</code>.
	 */
	public static final char C_CHAR 		= 'C';

	/**
	 * Character constant indicating the primitive type double in a signature.
	 * Value is <code>'D'</code>.
	 */
	public static final char C_DOUBLE 		= 'D';

	/**
	 * Character constant indicating the primitive type float in a signature.
	 * Value is <code>'F'</code>.
	 */
	public static final char C_FLOAT 		= 'F';

	/**
	 * Character constant indicating the primitive type int in a signature.
	 * Value is <code>'I'</code>.
	 */
	public static final char C_INT 			= 'I';
	
	/**
	 * Character constant indicating the semicolon in a signature.
	 * Value is <code>';'</code>.
	 */
	public static final char C_SEMICOLON 			= ';';

	/**
	 * Character constant indicating the colon in a signature.
	 * Value is <code>':'</code>.
	 * @since 3.0
	 */
	public static final char C_COLON 			= ':';

	/**
	 * Character constant indicating the primitive type long in a signature.
	 * Value is <code>'J'</code>.
	 */
	public static final char C_LONG			= 'J';
	
	/**
	 * Character constant indicating the primitive type short in a signature.
	 * Value is <code>'S'</code>.
	 */
	public static final char C_SHORT		= 'S';
	
	/**
	 * Character constant indicating result type void in a signature.
	 * Value is <code>'V'</code>.
	 */
	public static final char C_VOID			= 'V';
	
	/**
	 * Character constant indicating result const in a signature.
	 * Value is <code>'K'</code>.
	 */
	public static final char C_CONST		= 'K';

	/**
	 * Character constant indicating the start of a resolved type variable in a 
	 * signature. Value is <code>'T'</code>.
	 * @since 3.0
	 */
	public static final char C_TYPE_VARIABLE	= 'T';
	
	/**
	 * Character constant indicating a wildcard type argument 
	 * in a signature.
	 * Value is <code>'&ast;'</code>.
	 * @since 3.0
	 */
	public static final char C_STAR	= '*';
	
	/** 
	 * Character constant indicating the dot in a signature. 
	 * Value is <code>'.'</code>.
	 */
	public static final char C_DOT			= '.';
	
	/** 
	 * Character constant indicating the dollar in a signature.
	 * Value is <code>'$'</code>.
	 */
	public static final char C_DOLLAR			= '$';

	/** 
	 * Character constant indicating an array type in a signature.
	 * Value is <code>'['</code>.
	 */
	public static final char C_ARRAY		= '[';

	/** 
	 * Character constant indicating the start of a resolved, named type in a 
	 * signature. Value is <code>'L'</code>.
	 */
	public static final char C_RESOLVED		= 'L';

	/** 
	 * Character constant indicating the start of an unresolved, named type in a
	 * signature. Value is <code>'Q'</code>.
	 */
	public static final char C_UNRESOLVED	= 'Q';

	/**
	 * Character constant indicating the end of a named type in a signature. 
	 * Value is <code>';'</code>.
	 */
	public static final char C_NAME_END		= ';';

	/**
	 * Character constant indicating the start of a parameter type list in a
	 * signature. Value is <code>'('</code>.
	 */
	public static final char C_PARAM_START	= '(';

	/**
	 * Character constant indicating the end of a parameter type list in a 
	 * signature. Value is <code>')'</code>.
	 */
	public static final char C_PARAM_END	= ')';

	/**
	 * Character constant indicating the start of a formal type parameter
	 * (or type argument) list in a signature. Value is <code>'&lt;'</code>.
	 * @since 3.0
	 */
	public static final char C_GENERIC_START	= '<';

	/**
	 * Character constant indicating the end of a generic type list in a 
	 * signature. Value is <code>'%gt;'</code>.
	 * @since 3.0
	 */
	public static final char C_GENERIC_END	= '>';

	/**
	 * String constant for the signature of the primitive type boolean.
	 * Value is <code>"Z"</code>.
	 */
	public static final String SIG_BOOLEAN 		= "Z"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type byte. 
	 * Value is <code>"B"</code>.
	 */
	public static final String SIG_BYTE 		= "B"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type char.
	 * Value is <code>"C"</code>.
	 */
	public static final String SIG_CHAR 		= "C"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type double.
	 * Value is <code>"D"</code>.
	 */
	public static final String SIG_DOUBLE 		= "D"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type float.
	 * Value is <code>"F"</code>.
	 */
	public static final String SIG_FLOAT 		= "F"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type int.
	 * Value is <code>"I"</code>.
	 */
	public static final String SIG_INT 			= "I"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type long.
	 * Value is <code>"J"</code>.
	 */
	public static final String SIG_LONG			= "J"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type short.
	 * Value is <code>"S"</code>.
	 */
	public static final String SIG_SHORT		= "S"; //$NON-NLS-1$

	/** String constant for the signature of result type void.
	 * Value is <code>"V"</code>.
	 */
	public static final String SIG_VOID			= "V"; //$NON-NLS-1$
	

	/**
	 * Kind constant for a class type signature.
	 * @see #getTypeSignatureKind(String)
	 * @since 3.0
	 */
	public static int CLASS_TYPE_SIGNATURE = 1;

	/**
	 * Kind constant for a base (primitive or void) type signature.
	 * @see #getTypeSignatureKind(String)
	 * @since 3.0
	 */
	public static int BASE_TYPE_SIGNATURE = 2;

	/**
	 * Kind constant for a type variable signature.
	 * @see #getTypeSignatureKind(String)
	 * @since 3.0
	 */
	public static int TYPE_VARIABLE_SIGNATURE = 3;

	/**
	 * Kind constant for an array type signature.
	 * @see #getTypeSignatureKind(String)
	 * @since 3.0
	 */
	public static int ARRAY_TYPE_SIGNATURE = 4;

	private static final char[] BOOLEAN = {'b', 'o', 'o', 'l', 'e', 'a', 'n'};
	private static final char[] BYTE = {'b', 'y', 't', 'e'};
	private static final char[] CHAR = {'c', 'h', 'a', 'r'};
	private static final char[] DOUBLE = {'d', 'o', 'u', 'b', 'l', 'e'};
	private static final char[] FLOAT = {'f', 'l', 'o', 'a', 't'};
	private static final char[] INT = {'i', 'n', 't'};
	private static final char[] LONG = {'l', 'o', 'n', 'g'};
	private static final char[] SHORT = {'s', 'h', 'o', 'r', 't'};
	private static final char[] VOID = {'v', 'o', 'i', 'd'};
	private static final char[] CONST = {'c', 'o', 'n', 's', 't'};
	
	private static final String EMPTY = new String(CharOperation.NO_CHAR);
		
private Signature() {
	// Not instantiable
}

private static boolean checkPrimitiveType(char[] primitiveTypeName, char[] typeName) {
	return CharOperation.fragmentEquals(primitiveTypeName, typeName, 0, true) &&
		(typeName.length == primitiveTypeName.length
		 || Character.isWhitespace(typeName[primitiveTypeName.length])
		 || typeName[primitiveTypeName.length] == C_ARRAY
		 || typeName[primitiveTypeName.length] == C_DOT);
}

/**
 * Creates a new type signature with the given amount of array nesting added 
 * to the given type signature.
 *
 * @param typeSignature the type signature
 * @param arrayCount the desired number of levels of array nesting
 * @return the encoded array type signature
 * 
 * @since 2.0
 */
public static char[] createArraySignature(char[] typeSignature, int arrayCount) {
	if (arrayCount == 0) return typeSignature;
	int sigLength = typeSignature.length;
	char[] result = new char[arrayCount + sigLength];
	for (int i = 0; i < arrayCount; i++) {
		result[i] = C_ARRAY;
	}
	System.arraycopy(typeSignature, 0, result, arrayCount, sigLength);
	return result;
}
/**
 * Creates a new type signature with the given amount of array nesting added 
 * to the given type signature.
 *
 * @param typeSignature the type signature
 * @param arrayCount the desired number of levels of array nesting
 * @return the encoded array type signature
 */
public static String createArraySignature(String typeSignature, int arrayCount) {
	return new String(createArraySignature(typeSignature.toCharArray(), arrayCount));
}

/**
 * Creates a method signature from the given parameter and return type 
 * signatures. The encoded method signature is dot-based.
 *
 * @param parameterTypes the list of parameter type signatures
 * @param returnType the return type signature
 * @return the encoded method signature
 * 
 * @since 2.0
 */
public static char[] createMethodSignature(char[][] parameterTypes, char[] returnType) {
	int parameterTypesLength = parameterTypes.length;
	int parameterLength = 0;
	for (int i = 0; i < parameterTypesLength; i++) {
		parameterLength += parameterTypes[i].length;
		
	}
	int returnTypeLength = returnType.length;
	char[] result = new char[1 + parameterLength + 1 + returnTypeLength];
	result[0] = C_PARAM_START;
	int index = 1;
	for (int i = 0; i < parameterTypesLength; i++) {
		char[] parameterType = parameterTypes[i];
		int length = parameterType.length;
		System.arraycopy(parameterType, 0, result, index, length);
		index += length;
	}
	result[index] = C_PARAM_END;
	System.arraycopy(returnType, 0, result, index+1, returnTypeLength);
	return result;
}

/**
 * Creates a method signature from the given parameter and return type 
 * signatures. The encoded method signature is dot-based. This method
 * is equivalent to
 * <code>createMethodSignature(parameterTypes, returnType)</code>.
 *
 * @param parameterTypes the list of parameter type signatures
 * @param returnType the return type signature
 * @return the encoded method signature
 * @see Signature#createMethodSignature(char[][], char[])
 */
public static String createMethodSignature(String[] parameterTypes, String returnType) {
	int parameterTypesLenth = parameterTypes.length;
	char[][] parameters = new char[parameterTypesLenth][];
	for (int i = 0; i < parameterTypesLenth; i++) {
		parameters[i] = parameterTypes[i].toCharArray();
	}
	return new String(createMethodSignature(parameters, returnType.toCharArray()));
}

/**
 * Creates a new type signature from the given type name encoded as a character
 * array. The type name may contain primitive types or array types. However,
 * parameterized types are not supported.
 * This method is equivalent to
 * <code>createTypeSignature(new String(typeName),isResolved)</code>, although
 * more efficient for callers with character arrays rather than strings. If the 
 * type name is qualified, then it is expected to be dot-based.
 *
 * @param typeName the possibly qualified type name
 * @param isResolved <code>true</code> if the type name is to be considered
 *   resolved (for example, a type name from a binary class file), and 
 *   <code>false</code> if the type name is to be considered unresolved
 *   (for example, a type name found in source code)
 * @return the encoded type signature
 * @see #createTypeSignature(java.lang.String,boolean)
 */
public static String createTypeSignature(char[] typeName, boolean isResolved) {
	return new String(createCharArrayTypeSignature(typeName, isResolved));
}
/**
 * Creates a new type signature from the given type name encoded as a character
 * array. The type name may contain primitive types or array types. However,
 * parameterized types are not supported.
 * This method is equivalent to
 * <code>createTypeSignature(new String(typeName),isResolved).toCharArray()</code>,
 * although more efficient for callers with character arrays rather than strings.
 * If the type name is qualified, then it is expected to be dot-based.
 *
 * @param typeName the possibly qualified type name
 * @param isResolved <code>true</code> if the type name is to be considered
 *   resolved (for example, a type name from a binary class file), and 
 *   <code>false</code> if the type name is to be considered unresolved
 *   (for example, a type name found in source code)
 * @return the encoded type signature
 * @see #createTypeSignature(java.lang.String,boolean)
 * 
 * @since 2.0
 */
public static char[] createCharArrayTypeSignature(char[] typeName, boolean isResolved) {
	if (typeName == null) throw new IllegalArgumentException("null"); //$NON-NLS-1$
	int length = typeName.length;
	if (length == 0) throw new IllegalArgumentException(new String(typeName));

	int arrayCount = CharOperation.occurencesOf('[', typeName);
	char[] sig;
	
	switch (typeName[0]) {
		// primitive type?
		case 'b' :
			if (checkPrimitiveType(BOOLEAN, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_BOOLEAN;
				break;
			} else if (checkPrimitiveType(BYTE, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_BYTE;
				break;
			}
		case 'c':
			if (checkPrimitiveType(CHAR, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_CHAR;
				break;
			}
		case 'd':
			if (checkPrimitiveType(DOUBLE, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_DOUBLE;
				break;
			}
		case 'f':
			if (checkPrimitiveType(FLOAT, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_FLOAT;
				break;
			}
		case 'i':
			if (checkPrimitiveType(INT, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_INT;
				break;
			}
		case 'l':
			if (checkPrimitiveType(LONG, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_LONG;
				break;
			}
		case 's':
			if (checkPrimitiveType(SHORT, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_SHORT;
				break;
			}
		case 'v':
			if (checkPrimitiveType(VOID, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_VOID;
				break;
			}
		default:
			// non primitive type
			int sigLength = arrayCount + 1 + length + 1; // for example '[[[Ljava.lang.String;'
			sig = new char[sigLength];
			int sigIndex = arrayCount+1; // index in sig
			int startID = 0; // start of current ID in typeName
			int index = 0; // index in typeName
			while (index < length) {
				char currentChar = typeName[index];
				switch (currentChar) {
					case '.':
						if (startID == -1) throw new IllegalArgumentException(new String(typeName));
						if (startID < index) {
							sig = CharOperation.append(sig, sigIndex, typeName, startID, index);
							sigIndex += index-startID;
						}
						sig[sigIndex++] = C_DOT;
						index++;
						startID = index;
						break;
					case '[':
						if (startID != -1) {
							if (startID < index) {
								sig = CharOperation.append(sig, sigIndex, typeName, startID, index);
								sigIndex += index-startID;
							}
							startID = -1; // no more id after []
						}
						index++;
						break;
					default :
						if (startID != -1 && CharOperation.isWhitespace(currentChar)) {
							if (startID < index) {
								sig = CharOperation.append(sig, sigIndex, typeName, startID, index);
								sigIndex += index-startID;
							}
							startID = index+1;
						}
						index++;
						break;
				}
			}
			// last id
			if (startID != -1 && startID < index) {
				sig = CharOperation.append(sig, sigIndex, typeName, startID, index);
				sigIndex += index-startID;
			}
			
			// add L (or Q) at the beigininig and ; at the end
			sig[arrayCount] = isResolved ? C_RESOLVED : C_UNRESOLVED;
			sig[sigIndex++] = C_NAME_END;
			
			// resize if needed
			if (sigLength > sigIndex) {
				System.arraycopy(sig, 0, sig = new char[sigIndex], 0, sigIndex);
			}
	}

	// add array info
	for (int i = 0; i < arrayCount; i++) {
		sig[i] = C_ARRAY;
	}
	
	return sig;
}
/**
 * Creates a new type signature from the given type name. If the type name is qualified,
 * then it is expected to be dot-based. The type name may contain primitive
 * types or array types. However, parameterized types are not supported.
 * <p>
 * For example:
 * <pre>
 * <code>
 * createTypeSignature("int", hucairz) -> "I"
 * createTypeSignature("java.lang.String", true) -> "Ljava.lang.String;"
 * createTypeSignature("String", false) -> "QString;"
 * createTypeSignature("java.lang.String", false) -> "Qjava.lang.String;"
 * createTypeSignature("int []", false) -> "[I"
 * </code>
 * </pre>
 * </p>
 *
 * @param typeName the possibly qualified type name
 * @param isResolved <code>true</code> if the type name is to be considered
 *   resolved (for example, a type name from a binary class file), and 
 *   <code>false</code> if the type name is to be considered unresolved
 *   (for example, a type name found in source code)
 * @return the encoded type signature
 */
public static String createTypeSignature(String typeName, boolean isResolved) {
	return createTypeSignature(typeName == null ? null : typeName.toCharArray(), isResolved);
}

/**
 * Returns the array count (array nesting depth) of the given type signature.
 *
 * @param typeSignature the type signature
 * @return the array nesting depth, or 0 if not an array
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 * 
 * @since 2.0
 */
public static int getArrayCount(char[] typeSignature) throws IllegalArgumentException {	
	try {
		int count = 0;
		while (typeSignature[count] == C_ARRAY) {
			++count;
		}
		return count;
	} catch (ArrayIndexOutOfBoundsException e) { // signature is syntactically incorrect if last character is C_ARRAY
		throw new IllegalArgumentException();
	}
}
/**
 * Returns the array count (array nesting depth) of the given type signature.
 *
 * @param typeSignature the type signature
 * @return the array nesting depth, or 0 if not an array
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 */
public static int getArrayCount(String typeSignature) throws IllegalArgumentException {
	return getArrayCount(typeSignature.toCharArray());
}
/**
 * Returns the type signature without any array nesting.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getElementType({'[', '[', 'I'}) --> {'I'}.
 * </code>
 * </pre>
 * </p>
 * 
 * @param typeSignature the type signature
 * @return the type signature without arrays
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 * 
 * @since 2.0
 */
public static char[] getElementType(char[] typeSignature) throws IllegalArgumentException {
	int count = getArrayCount(typeSignature);
	if (count == 0) return typeSignature;
	int length = typeSignature.length;
	char[] result = new char[length-count];
	System.arraycopy(typeSignature, count, result, 0, length-count);
	return result;
}
/**
 * Returns the type signature without any array nesting.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getElementType("[[I") --> "I".
 * </code>
 * </pre>
 * </p>
 * 
 * @param typeSignature the type signature
 * @return the type signature without arrays
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 */
public static String getElementType(String typeSignature) throws IllegalArgumentException {
	return new String(getElementType(typeSignature.toCharArray()));
}
/**
 * Returns the number of parameter types in the given method signature.
 *
 * @param methodSignature the method signature
 * @return the number of parameters
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 * @since 2.0
 */
public static int getParameterCount(char[] methodSignature) throws IllegalArgumentException {
	try {
		int count = 0;
		int i = CharOperation.indexOf(C_PARAM_START, methodSignature);
		if (i < 0) {
			throw new IllegalArgumentException();
		}
		i++;
		for (;;) {
			if (methodSignature[i] == C_PARAM_END) {
				return count;
			}
			int e= scanTypeSignature(methodSignature, i);
			if (e < 0) {
				throw new IllegalArgumentException();
			}
			i = e + 1;
			count++;
		}
	} catch (ArrayIndexOutOfBoundsException e) {
		throw new IllegalArgumentException();
	}
}

/**
 * Returns the kind of type signature encoded by the given string.
 * 
 * @param typeSignature the type signature string
 * @return the kind of type signature; one of the kind constants:
 * {@link #ARRAY_TYPE_SIGNATURE}, {@link #CLASS_TYPE_SIGNATURE},
 * {@link #BASE_TYPE_SIGNATURE}, or {@link #TYPE_VARIABLE_SIGNATURE}
 * @exception IllegalArgumentException if this is not a type signature
 * @since 3.0
 */
public static int getTypeSignatureKind(char[] typeSignature) {
	// need a minimum 1 char
	if (typeSignature.length < 1) {
		throw new IllegalArgumentException();
	}
	char c = typeSignature[0];
	switch (c) {
		case C_ARRAY :
			return ARRAY_TYPE_SIGNATURE;
		case C_RESOLVED :
		case C_UNRESOLVED :
			return CLASS_TYPE_SIGNATURE;
		case C_TYPE_VARIABLE :
			return TYPE_VARIABLE_SIGNATURE;
		case C_BOOLEAN :
		case C_BYTE :
		case C_CHAR :
		case C_DOUBLE :
		case C_FLOAT :
		case C_INT :
		case C_LONG :
		case C_SHORT :
		case C_VOID :
			return BASE_TYPE_SIGNATURE;
		default :
			throw new IllegalArgumentException();
	}
}

/**
 * Returns the kind of type signature encoded by the given string.
 * 
 * @param typeSignature the type signature string
 * @return the kind of type signature; one of the kind constants:
 * {@link #ARRAY_TYPE_SIGNATURE}, {@link #CLASS_TYPE_SIGNATURE},
 * {@link #BASE_TYPE_SIGNATURE}, or {@link #TYPE_VARIABLE_SIGNATURE}
 * @exception IllegalArgumentException if this is not a type signature
 * @since 3.0
 */
public static int getTypeSignatureKind(String typeSignature) {
	// need a minimum 1 char
	if (typeSignature.length() < 1) {
		throw new IllegalArgumentException();
	}
	char c = typeSignature.charAt(0);
	switch (c) {
		case C_ARRAY :
			return ARRAY_TYPE_SIGNATURE;
		case C_RESOLVED :
		case C_UNRESOLVED :
			return CLASS_TYPE_SIGNATURE;
		case C_TYPE_VARIABLE :
			return TYPE_VARIABLE_SIGNATURE;
		case C_BOOLEAN :
		case C_BYTE :
		case C_CHAR :
		case C_DOUBLE :
		case C_FLOAT :
		case C_INT :
		case C_LONG :
		case C_SHORT :
		case C_VOID :
			return BASE_TYPE_SIGNATURE;
		default :
			throw new IllegalArgumentException();
	}
}

/**
 * Scans the given string for a type signature starting at the given index
 * and returns the index of the last character.
 * <pre>
 * TypeSignature:
 *  |  BaseTypeSignature
 *  |  ArrayTypeSignature
 *  |  ClassTypeSignature
 *  |  TypeVariableSignature
 * </pre>
 * 
 * @param string the signature string
 * @param start the 0-based character index of the first character
 * @return the 0-based character index of the last character
 * @exception IllegalArgumentException if this is not a type signature
 * @see #appendTypeSignature(char[], int, boolean, StringBuffer)
 */
private static int scanTypeSignature(char[] string, int start) {
	// need a minimum 1 char
	if (start >= string.length) {
		throw new IllegalArgumentException();
	}
	char c = string[start];
	switch (c) {
		case C_ARRAY :
			return scanArrayTypeSignature(string, start);
		case C_RESOLVED :
		case C_UNRESOLVED :
			return scanClassTypeSignature(string, start);
		case C_TYPE_VARIABLE :
			return scanTypeVariableSignature(string, start);
		case C_BOOLEAN :
		case C_BYTE :
		case C_CHAR :
		case C_DOUBLE :
		case C_FLOAT :
		case C_INT :
		case C_LONG :
		case C_SHORT :
		case C_VOID :
			return scanBaseTypeSignature(string, start);
		default :
			throw new IllegalArgumentException();
	}
}

/**
 * Scans the given string for a base type signature starting at the given index
 * and returns the index of the last character.
 * <pre>
 * BaseTypeSignature:
 *     <b>B</b> | <b>C</b> | <b>D</b> | <b>F</b> | <b>I</b>
 *   | <b>J</b> | <b>S</b> | <b>V</b> | <b>Z</b>
 * </pre>
 * Note that although the base type "V" is only allowed in method return types,
 * there is no syntactic ambiguity. This method will accept them anywhere
 * without complaint.
 * 
 * @param string the signature string
 * @param start the 0-based character index of the first character
 * @return the 0-based character index of the last character
 * @exception IllegalArgumentException if this is not a base type signature
 */
private static int scanBaseTypeSignature(char[] string, int start) {
	// need a minimum 1 char
	if (start >= string.length) {
		throw new IllegalArgumentException();
	}
	char c = string[start];
	if ("BCDFIJSVZ".indexOf(c) >= 0) { //$NON-NLS-1$
		return start;
	}
	throw new IllegalArgumentException();
}

/**
 * Scans the given string for an array type signature starting at the given
 * index and returns the index of the last character.
 * <pre>
 * ArrayTypeSignature:
 *     <b>[</b> TypeSignature
 * </pre>
 * 
 * @param string the signature string
 * @param start the 0-based character index of the first character
 * @return the 0-based character index of the last character
 * @exception IllegalArgumentException if this is not an array type signature
 * @see #appendArrayTypeSignature(char[], int, boolean, StringBuffer)
 */
private static int scanArrayTypeSignature(char[] string, int start) {
	// need a minimum 2 char
	if (start >= string.length - 1) {
		throw new IllegalArgumentException();
	}
	char c = string[start];
	if (c != C_ARRAY) { //$NON-NLS-1$
		throw new IllegalArgumentException();
	}
	return scanTypeSignature(string, start + 1);
}

/**
 * Scans the given string for a type variable signature starting at the given
 * index and returns the index of the last character.
 * <pre>
 * TypeVariableSignature:
 *     <b>T</b> Identifier <b>;</b>
 * </pre>
 * 
 * @param string the signature string
 * @param start the 0-based character index of the first character
 * @return the 0-based character index of the last character
 * @exception IllegalArgumentException if this is not a type variable signature
 */
private static int scanTypeVariableSignature(char[] string, int start) {
	// need a minimum 3 chars "Tx;"
	if (start >= string.length - 2) { 
		throw new IllegalArgumentException();
	}
	// must start in "T"
	char c = string[start];
	if (c != C_TYPE_VARIABLE) {
		throw new IllegalArgumentException();
	}
	int id = scanIdentifier(string, start + 1);
	c = string[id + 1];
	if (c == C_SEMICOLON) {
		return id + 1;
	}
	throw new IllegalArgumentException();
}

/**
 * Scans the given string for an identifier starting at the given
 * index and returns the index of the last character. 
 * Stop characters are: ";", ":", "&lt;", "&gt;", "/", ".".
 * 
 * @param string the signature string
 * @param start the 0-based character index of the first character
 * @return the 0-based character index of the last character
 * @exception IllegalArgumentException if this is not an identifier
 */
private static int scanIdentifier(char[] string, int start) {
	// need a minimum 1 char
	if (start >= string.length) { 
		throw new IllegalArgumentException();
	}
	int p = start;
	while (true) {
		char c = string[p];
		if (c == '<' || c == '>' || c == ':' || c == ';' || c == '.' || c == '/') {
			return p - 1;
		}
		p++;
		if (p == string.length) {
			return p - 1;
		}
	}
}

/**
 * Scans the given string for a class type signature starting at the given
 * index and returns the index of the last character.
 * <pre>
 * ClassTypeSignature:
 *     { <b>L</b> | <b>Q</b> } Identifier
 *           { { <b>/</b> | <b>.</b> Identifier [ <b>&lt;</b> TypeArgumentSignature* <b>&gt;</b> ] }
 *           <b>;</b>
 * </pre>
 * Note that although all "/"-identifiers most come before "."-identifiers,
 * there is no syntactic ambiguity. This method will accept them without
 * complaint.
 * 
 * @param string the signature string
 * @param start the 0-based character index of the first character
 * @return the 0-based character index of the last character
 * @exception IllegalArgumentException if this is not a class type signature
 * @see #appendClassTypeSignature(char[], int, boolean, StringBuffer)
 */
private static int scanClassTypeSignature(char[] string, int start) {
	// need a minimum 3 chars "Lx;"
	if (start >= string.length - 2) { 
		throw new IllegalArgumentException();
	}
	// must start in "L" or "Q"
	char c = string[start];
	if (c != C_RESOLVED && c != C_UNRESOLVED) {
		return -1;
	}
	int p = start + 1;
	while (true) {
		if (p >= string.length) {
			throw new IllegalArgumentException();
		}
		c = string[p];
		if (c == C_SEMICOLON) {
			// all done
			return p;
		} else if (c == C_GENERIC_START) {
			int e = scanTypeArgumentSignatures(string, p);
			p = e;
		} else if (c == C_DOT || c == '/') {
			int id = scanIdentifier(string, p + 1);
			p = id;
		}
		p++;
	}
}

/**
 * Scans the given string for a list of type argument signatures starting at
 * the given index and returns the index of the last character.
 * <pre>
 * TypeArgumentSignatures:
 *     <b>&lt;</b> TypeArgumentSignature* <b>&gt;</b>
 * </pre>
 * Note that although there is supposed to be at least one type argument, there
 * is no syntactic ambiguity if there are none. This method will accept zero
 * type argument signatures without complaint.
 * 
 * @param string the signature string
 * @param start the 0-based character index of the first character
 * @return the 0-based character index of the last character
 * @exception IllegalArgumentException if this is not a list of type arguments
 * signatures
 * @see #appendTypeArgumentSignatures(char[], int, boolean, StringBuffer)
 */
private static int scanTypeArgumentSignatures(char[] string, int start) {
	// need a minimum 2 char "<>"
	if (start >= string.length - 1) {
		throw new IllegalArgumentException();
	}
	char c = string[start];
	if (c != C_GENERIC_START) {
		throw new IllegalArgumentException();
	}
	int p = start + 1;
	while (true) {
		if (p >= string.length) {
			throw new IllegalArgumentException();
		}
		c = string[p];
		if (c == C_GENERIC_END) {
			return p;
		}
		int e = scanTypeArgumentSignature(string, p);
		p = e + 1;
	}
}

/**
 * Scans the given string for a type argument signature starting at the given
 * index and returns the index of the last character.
 * <pre>
 * TypeArgumentSignature:
 *     <b>&#42;</b>
 *  |  <b>+</b> TypeSignature
 *  |  <b>-</b> TypeSignature
 *  |  TypeSignature
 * </pre>
 * Note that although base types are not allowed in type arguments, there is
 * no syntactic ambiguity. This method will accept them without complaint.
 * 
 * @param string the signature string
 * @param start the 0-based character index of the first character
 * @return the 0-based character index of the last character
 * @exception IllegalArgumentException if this is not a type argument signature
 * @see #appendTypeArgumentSignature(char[], int, boolean, StringBuffer)
 */
private static int scanTypeArgumentSignature(char[] string, int start) {
	// need a minimum 1 char
	if (start >= string.length) {
		throw new IllegalArgumentException();
	}
	char c = string[start];
	if (c == C_STAR) {
		return start;
	}
	if (c == '+' || c == '-') {
		return scanTypeSignature(string, start + 1);
	}
	return scanTypeSignature(string, start);
}

/**
 * Returns the number of parameter types in the given method signature.
 *
 * @param methodSignature the method signature
 * @return the number of parameters
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 */
public static int getParameterCount(String methodSignature) throws IllegalArgumentException {
	return getParameterCount(methodSignature.toCharArray());
}
/**
 * Extracts the parameter type signatures from the given method signature. 
 * The method signature is expected to be dot-based.
 *
 * @param methodSignature the method signature
 * @return the list of parameter type signatures
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 * 
 * @since 2.0
 */
public static char[][] getParameterTypes(char[] methodSignature) throws IllegalArgumentException {
	try {
		int count = getParameterCount(methodSignature);
		char[][] result = new char[count][];
		if (count == 0) {
			return result;
		}
		int i = CharOperation.indexOf(C_PARAM_START, methodSignature);
		if (i < 0) {
			throw new IllegalArgumentException();
		}
		i++;
		int t = 0;
		for (;;) {
			if (methodSignature[i] == C_PARAM_END) {
				return result;
			}
			int e = scanTypeSignature(methodSignature, i);
			if (e < 0) {
				throw new IllegalArgumentException();
			}
			result[t] = CharOperation.subarray(methodSignature, i, e + 1);
			t++;
			i = e + 1;
		}
	} catch (ArrayIndexOutOfBoundsException e) {
		throw new IllegalArgumentException();
	}
}
/**
 * Extracts the parameter type signatures from the given method signature. 
 * The method signature is expected to be dot-based.
 *
 * @param methodSignature the method signature
 * @return the list of parameter type signatures
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 */
public static String[] getParameterTypes(String methodSignature) throws IllegalArgumentException {
	char[][] parameterTypes = getParameterTypes(methodSignature.toCharArray());
	int length = parameterTypes.length;
	String[] result = new String[length];
	for (int i = 0; i < length; i++) {
		result[i] = new String(parameterTypes[i]);
	}
	return result;
}

/**
 * Extracts the type variable name from the given formal type parameter
 * signature. The signature is expected to be dot-based.
 *
 * @param formalTypeParameterSignature the formal type parameter signature
 * @return the name of the type variable
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 * @since 3.0
 */
public static String getTypeVariable(String formalTypeParameterSignature) throws IllegalArgumentException {
	return new String(getTypeVariable(formalTypeParameterSignature.toCharArray()));
}

/**
 * Extracts the type variable name from the given formal type parameter
 * signature. The signature is expected to be dot-based.
 *
 * @param formalTypeParameterSignature the formal type parameter signature
 * @return the name of the type variable
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 * @since 3.0
 */
public static char[] getTypeVariable(char[] formalTypeParameterSignature) throws IllegalArgumentException {
	int p = CharOperation.indexOf(C_COLON, formalTypeParameterSignature);
	if (p < 0) {
		// no ":" means can't be a formal type parameter signature
		throw new IllegalArgumentException();
	}
	return CharOperation.subarray(formalTypeParameterSignature, 0, p);
}

/**
 * Extracts the class and interface bounds from the given formal type
 * parameter signature. The class bound, if present, is listed before
 * the interface bounds. The signature is expected to be dot-based.
 *
 * @param formalTypeParameterSignature the formal type parameter signature
 * @return the (possibly empty) list of type signatures for the bounds
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 * @since 3.0
 */
public static char[][] getTypeParameterBounds(char[] formalTypeParameterSignature) throws IllegalArgumentException {
	int p1 = CharOperation.indexOf(C_COLON, formalTypeParameterSignature);
	if (p1 < 0) {
		// no ":" means can't be a formal type parameter signature
		throw new IllegalArgumentException();
	}
	if (p1 == formalTypeParameterSignature.length - 1) {
		// no class or interface bounds
		return CharOperation.NO_CHAR_CHAR;
	}
	int p2 = CharOperation.indexOf(C_COLON, formalTypeParameterSignature, p1 + 1);
	char[] classBound;
	if (p2 < 0) {
		// no interface bounds
		classBound = CharOperation.subarray(formalTypeParameterSignature, p1 + 1, formalTypeParameterSignature.length);
		return new char[][] {classBound};
	}
	if (p2 == p1 + 1) {
		// no class bound, but 1 or more interface bounds
		classBound = null;
	} else {
		classBound = CharOperation.subarray(formalTypeParameterSignature, p1 + 1, p2);
	}
	char[][] interfaceBounds = CharOperation.splitOn(C_COLON, formalTypeParameterSignature, p2 + 1, formalTypeParameterSignature.length);
	if (classBound == null) {
		return interfaceBounds;
	}
	int resultLength = interfaceBounds.length + 1;
	char[][] result = new char[resultLength][];
	result[0] = classBound;
	System.arraycopy(interfaceBounds, 0, result, 1, interfaceBounds.length);
	return result;
}

/**
 * Extracts the class and interface bounds from the given formal type
 * parameter signature. The class bound, if present, is listed before
 * the interface bounds. The signature is expected to be dot-based.
 *
 * @param formalTypeParameterSignature the formal type parameter signature
 * @return the (possibly empty) list of type signatures for the bounds
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 * @since 3.0
 */
public static String[] getTypeParameterBounds(String formalTypeParameterSignature) throws IllegalArgumentException {
	char[][] bounds = getTypeParameterBounds(formalTypeParameterSignature.toCharArray());
	int length = bounds.length;
	String[] result = new String[length];
	for (int i = 0; i < length; i++) {
		result[i] = new String(bounds[i]);
	}
	return result;
}

/**
 * Returns a char array containing all but the last segment of the given 
 * dot-separated qualified name. Returns the empty char array if it is not qualified.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getQualifier({'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'O', 'b', 'j', 'e', 'c', 't'}) -> {'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g'}
 * getQualifier({'O', 'u', 't', 'e', 'r', '.', 'I', 'n', 'n', 'e', 'r'}) -> {'O', 'u', 't', 'e', 'r'}
 * </code>
 * </pre>
 * </p>
 *
 * @param name the name
 * @return the qualifier prefix, or the empty char array if the name contains no
 *   dots
 * @exception NullPointerException if name is null
 * @since 2.0
 */
public static char[] getQualifier(char[] name) {
	int lastDot = CharOperation.lastIndexOf(C_DOT, name);
	if (lastDot == -1) {
		return CharOperation.NO_CHAR;
	}
	return CharOperation.subarray(name, 0, lastDot);
}
/**
 * Returns a string containing all but the last segment of the given 
 * dot-separated qualified name. Returns the empty string if it is not qualified.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getQualifier("java.lang.Object") -> "java.lang"
 * getQualifier("Outer.Inner") -> "Outer"
 * </code>
 * </pre>
 * </p>
 *
 * @param name the name
 * @return the qualifier prefix, or the empty string if the name contains no
 *   dots
 * @exception NullPointerException if name is null
 */
public static String getQualifier(String name) {
	int lastDot = name.lastIndexOf(C_DOT);
	if (lastDot == -1) {
		return EMPTY;
	}
	return name.substring(0, lastDot);
}
/**
 * Extracts the return type from the given method signature. The method signature is 
 * expected to be dot-based.
 *
 * @param methodSignature the method signature
 * @return the type signature of the return type
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 * 
 * @since 2.0
 */
public static char[] getReturnType(char[] methodSignature) throws IllegalArgumentException {
	// skip type parameters
	int i = CharOperation.lastIndexOf(C_PARAM_END, methodSignature);
	if (i == -1) {
		throw new IllegalArgumentException();
	}
	// ignore any thrown exceptions
	int j = CharOperation.indexOf('^', methodSignature);
	int last = (j == -1 ? methodSignature.length : j);
	return CharOperation.subarray(methodSignature, i + 1, last);
}
/**
 * Extracts the return type from the given method signature. The method signature is 
 * expected to be dot-based.
 *
 * @param methodSignature the method signature
 * @return the type signature of the return type
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 */
public static String getReturnType(String methodSignature) throws IllegalArgumentException {
	return new String(getReturnType(methodSignature.toCharArray()));
}
/**
 * Returns the last segment of the given dot-separated qualified name.
 * Returns the given name if it is not qualified.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getSimpleName({'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'O', 'b', 'j', 'e', 'c', 't'}) -> {'O', 'b', 'j', 'e', 'c', 't'}
 * </code>
 * </pre>
 * </p>
 *
 * @param name the name
 * @return the last segment of the qualified name
 * @exception NullPointerException if name is null
 * @since 2.0
 */
public static char[] getSimpleName(char[] name) {
	int lastDot = CharOperation.lastIndexOf(C_DOT, name);
	if (lastDot == -1) {
		return name;
	}
	return CharOperation.subarray(name, lastDot + 1, name.length);
}
/**
 * Returns the last segment of the given dot-separated qualified name.
 * Returns the given name if it is not qualified.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getSimpleName("java.lang.Object") -> "Object"
 * </code>
 * </pre>
 * </p>
 *
 * @param name the name
 * @return the last segment of the qualified name
 * @exception NullPointerException if name is null
 */
public static String getSimpleName(String name) {
	int lastDot = name.lastIndexOf(C_DOT);
	if (lastDot == -1) {
		return name;
	}
	return name.substring(lastDot + 1, name.length());
}
/**
 * Returns all segments of the given dot-separated qualified name.
 * Returns an array with only the given name if it is not qualified.
 * Returns an empty array if the name is empty.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getSimpleNames({'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'O', 'b', 'j', 'e', 'c', 't'}) -> {{'j', 'a', 'v', 'a'}, {'l', 'a', 'n', 'g'}, {'O', 'b', 'j', 'e', 'c', 't'}}
 * getSimpleNames({'O', 'b', 'j', 'e', 'c', 't'}) -> {{'O', 'b', 'j', 'e', 'c', 't'}}
 * getSimpleNames("") -> {}
 * </code>
 * </pre>
 *
 * @param name the name
 * @return the list of simple names, possibly empty
 * @exception NullPointerException if name is null
 * @since 2.0
 */
public static char[][] getSimpleNames(char[] name) {
	if (name.length == 0) {
		return CharOperation.NO_CHAR_CHAR;
	}
	int dot = CharOperation.indexOf(C_DOT, name);
	if (dot == -1) {
		return new char[][] {name};
	}
	int n = 1;
	while ((dot = CharOperation.indexOf(C_DOT, name, dot + 1)) != -1) {
		++n;
	}
	char[][] result = new char[n + 1][];
	int segStart = 0;
	for (int i = 0; i < n; ++i) {
		dot = CharOperation.indexOf(C_DOT, name, segStart);
		result[i] = CharOperation.subarray(name, segStart, dot);
		segStart = dot + 1;
	}
	result[n] = CharOperation.subarray(name, segStart, name.length);
	return result;
}
/**
 * Returns all segments of the given dot-separated qualified name.
 * Returns an array with only the given name if it is not qualified.
 * Returns an empty array if the name is empty.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getSimpleNames("java.lang.Object") -> {"java", "lang", "Object"}
 * getSimpleNames("Object") -> {"Object"}
 * getSimpleNames("") -> {}
 * </code>
 * </pre>
 *
 * @param name the name
 * @return the list of simple names, possibly empty
 * @exception NullPointerException if name is null
 */
public static String[] getSimpleNames(String name) {
	char[][] simpleNames = getSimpleNames(name.toCharArray());
	int length = simpleNames.length;
	String[] result = new String[length];
	for (int i = 0; i < length; i++) {
		result[i] = new String(simpleNames[i]);
	}
	return result;
}
/**
 * Converts the given method signature to a readable form. The method signature is expected to
 * be dot-based.
 * <p>
 * For example:
 * <pre>
 * <code>
 * toString("([Ljava.lang.String;)V", "main", new String[] {"args"}, false, true) -> "void main(String[] args)"
 * </code>
 * </pre>
 * </p>
 * 
 * @param methodSignature the method signature to convert
 * @param methodName the name of the method to insert in the result, or 
 *   <code>null</code> if no method name is to be included
 * @param parameterNames the parameter names to insert in the result, or 
 *   <code>null</code> if no parameter names are to be included; if supplied,
 *   the number of parameter names must match that of the method signature
 * @param fullyQualifyTypeNames <code>true</code> if type names should be fully
 *   qualified, and <code>false</code> to use only simple names
 * @param includeReturnType <code>true</code> if the return type is to be
 *   included
 * @return the char array representation of the method signature
 * 
 * @since 2.0
 */
public static char[] toCharArray(char[] methodSignature, char[] methodName, char[][] parameterNames, boolean fullyQualifyTypeNames, boolean includeReturnType) {
	int firstParen = CharOperation.indexOf(C_PARAM_START, methodSignature);
	if (firstParen == -1) {
		throw new IllegalArgumentException();
	}
	
	StringBuffer buffer = new StringBuffer(methodSignature.length + 10);
	
	// return type
	if (includeReturnType) {
		char[] rts = getReturnType(methodSignature);
		appendTypeSignature(rts, 0 , fullyQualifyTypeNames, buffer);
		buffer.append(' ');
	}
	
	// selector
	if (methodName != null) {
		buffer.append(methodName);
	}
	
	// parameters
	buffer.append('(');
	char[][] pts = getParameterTypes(methodSignature);
	for (int i = 0; i < pts.length; i++) {
		appendTypeSignature(pts[i], 0 , fullyQualifyTypeNames, buffer);
		if (parameterNames != null) {
			buffer.append(' ');
			buffer.append(parameterNames[i]);
		}
		if (i != pts.length - 1) {
			buffer.append(',');
			buffer.append(' ');
		}
	}
	buffer.append(')');
	char[] result = new char[buffer.length()];
	buffer.getChars(0, buffer.length(), result, 0);
	return result;
}

/**
 * Converts the given type signature to a readable string. The signature is expected to
 * be dot-based.
 * 
 * <p>
 * For example:
 * <pre>
 * <code>
 * toString({'[', 'L', 'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'S', 't', 'r', 'i', 'n', 'g', ';'}) -> {'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'S', 't', 'r', 'i', 'n', 'g', '[', ']'}
 * toString({'I'}) -> {'i', 'n', 't'}
 * </code>
 * </pre>
 * </p>
 * <p>
 * Note: This method assumes that a type signature containing a <code>'$'</code>
 * is an inner type signature. While this is correct in most cases, someone could 
 * define a non-inner type name containing a <code>'$'</code>. Handling this 
 * correctly in all cases would have required resolving the signature, which 
 * generally not feasible.
 * </p>
 *
 * @param signature the type signature
 * @return the string representation of the type
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 * 
 * @since 2.0
 */
public static char[] toCharArray(char[] signature) throws IllegalArgumentException {
		int sigLength = signature.length;
		if (sigLength == 0 || signature[0] == C_PARAM_START || signature[0] == C_GENERIC_START) {
			return toCharArray(signature, CharOperation.NO_CHAR, null, true, true);
		}
		
		StringBuffer buffer = new StringBuffer(signature.length + 10);
		appendTypeSignature(signature, 0, true, buffer);
		char[] result = new char[buffer.length()];
		buffer.getChars(0, buffer.length(), result, 0);
		return result;
}

/**
 * Scans the given string for a type signature starting at the given
 * index and appends it to the given buffer, and returns the index of the last
 * character.
 * 
 * @param string the signature string
 * @param start the 0-based character index of the first character
 * @param fullyQualifyTypeNames <code>true</code> if type names should be fully
 *   qualified, and <code>false</code> to use only simple names
 * @param buffer the string buffer to append to
 * @return the 0-based character index of the last character
 * @exception IllegalArgumentException if this is not a type signature
 * @see #scanTypeSignature(char[], int)
 */
private static int appendTypeSignature(char[] string, int start, boolean fullyQualifyTypeNames, StringBuffer buffer) {
		// need a minimum 1 char
		if (start >= string.length) {
			throw new IllegalArgumentException();
		}
		char c = string[start];
		switch (c) {
			case C_ARRAY :
				return appendArrayTypeSignature(string, start, fullyQualifyTypeNames, buffer);
			case C_RESOLVED :
			case C_UNRESOLVED :
				return appendClassTypeSignature(string, start, fullyQualifyTypeNames, buffer);
			case C_TYPE_VARIABLE :
				int e = scanTypeVariableSignature(string, start);
				buffer.append(CharOperation.subarray(string, start + 1, e));
				return e;
			case C_BOOLEAN :
				buffer.append(BOOLEAN);
				return start;
			case C_BYTE :
				buffer.append(BYTE);
				return start;
			case C_CHAR :
				buffer.append(CHAR);
				return start;
			case C_DOUBLE :
				buffer.append(DOUBLE);
				return start;
			case C_FLOAT :
				buffer.append(FLOAT);
				return start;
			case C_INT :
				buffer.append(INT);
				return start;
			case C_LONG :
				buffer.append(LONG);
				return start;
			case C_SHORT :
				buffer.append(SHORT);
				return start;
			case C_VOID :
				buffer.append(VOID);
				return start;
			case C_CONST :
			    buffer.append(CONST);
			    return start;
			default :
				throw new IllegalArgumentException();
		}
}

/**
 * Scans the given string for an array type signature starting at the given
 * index and appends it to the given buffer, and returns the index of the last
 * character.
 * 
 * @param string the signature string
 * @param start the 0-based character index of the first character
 * @param fullyQualifyTypeNames <code>true</code> if type names should be fully
 *   qualified, and <code>false</code> to use only simple names
 * @return the 0-based character index of the last character
 * @exception IllegalArgumentException if this is not an array type signature
 * @see #scanArrayTypeSignature(char[], int)
 */
private static int appendArrayTypeSignature(char[] string, int start, boolean fullyQualifyTypeNames, StringBuffer buffer) {
	// need a minimum 2 char
	if (start >= string.length - 1) {
		throw new IllegalArgumentException();
	}
	char c = string[start];
	if (c != C_ARRAY) { //$NON-NLS-1$
		throw new IllegalArgumentException();
	}
	int e = appendTypeSignature(string, start + 1, fullyQualifyTypeNames, buffer);
	buffer.append('[');
	buffer.append(']');
	return e;
}

/**
 * Scans the given string for a class type signature starting at the given
 * index and appends it to the given buffer, and returns the index of the last
 * character.
 * 
 * @param string the signature string
 * @param start the 0-based character index of the first character
 * @param fullyQualifyTypeNames <code>true</code> if type names should be fully
 *   qualified, and <code>false</code> to use only simple names
 * @param buffer the string buffer to append to
 * @return the 0-based character index of the last character
 * @exception IllegalArgumentException if this is not a class type signature
 * @see #scanClassTypeSignature(char[], int)
 */
private static int appendClassTypeSignature(char[] string, int start, boolean fullyQualifyTypeNames, StringBuffer buffer) {
	// need a minimum 3 chars "Lx;"
	if (start >= string.length - 2) { 
		throw new IllegalArgumentException();
	}
	// must start in "L" or "Q"
	char c = string[start];
	if (c != C_RESOLVED && c != C_UNRESOLVED) {
		throw new IllegalArgumentException();
	}
	boolean resolved = (c == C_RESOLVED);
	boolean removePackageQualifiers = !fullyQualifyTypeNames;
	if (!resolved) {
		// keep everything in an unresolved name
		removePackageQualifiers = false;
	}
	int p = start + 1;
	int checkpoint = buffer.length();
	while (true) {
		if (p >= string.length) {
			throw new IllegalArgumentException();
		}
		c = string[p];
		switch(c) {
			case C_SEMICOLON :
				// all done
				return p;
			case C_GENERIC_START :
				int e = appendTypeArgumentSignatures(string, p, fullyQualifyTypeNames, buffer);
				// once we hit type arguments there are no more package prefixes
				removePackageQualifiers = false;
				p = e;
				break;
			case C_DOT :
				if (removePackageQualifiers) {
					// erase package prefix
					buffer.setLength(checkpoint);
				} else {
					buffer.append('.');
				}
				break;
			 case '/' :
				if (removePackageQualifiers) {
					// erase package prefix
					buffer.setLength(checkpoint);
				} else {
					buffer.append('/');
				}
				break;
			 case C_DOLLAR :
			 	if (resolved) {
					// once we hit "$" there are no more package prefixes
					removePackageQualifiers = false;
					/**
					 * Convert '$' in resolved type signatures into '.'.
					 * NOTE: This assumes that the type signature is an inner type
					 * signature. This is true in most cases, but someone can define a
					 * non-inner type name containing a '$'.
					 */
					buffer.append('.');
			 	}
			 	break;
			 default :
				buffer.append(c);
		}
		p++;
	}
}

/**
 * Scans the given string for a list of type arguments signature starting at the
 * given index and appends it to the given buffer, and returns the index of the
 * last character.
 * 
 * @param string the signature string
 * @param start the 0-based character index of the first character
 * @param fullyQualifyTypeNames <code>true</code> if type names should be fully
 *   qualified, and <code>false</code> to use only simple names
 * @param buffer the string buffer to append to
 * @return the 0-based character index of the last character
 * @exception IllegalArgumentException if this is not a list of type argument
 * signatures
 * @see #scanTypeArgumentSignatures(char[], int)
 */
private static int appendTypeArgumentSignatures(char[] string, int start, boolean fullyQualifyTypeNames, StringBuffer buffer) {
	// need a minimum 2 char "<>"
	if (start >= string.length - 1) {
		throw new IllegalArgumentException();
	}
	char c = string[start];
	if (c != C_GENERIC_START) {
		throw new IllegalArgumentException();
	}
	buffer.append('<');
	int p = start + 1;
	int count = 0;
	while (true) {
		if (p >= string.length) {
			throw new IllegalArgumentException();
		}
		c = string[p];
		if (c == C_GENERIC_END) {
			buffer.append('>');
			return p;
		}
		if (count != 0) {
			buffer.append(',');
		}
		int e = appendTypeArgumentSignature(string, p, fullyQualifyTypeNames, buffer);
		count++;
		p = e + 1;
	}
}

/**
 * Scans the given string for a type argument signature starting at the given
 * index and appends it to the given buffer, and returns the index of the last
 * character.
 * 
 * @param string the signature string
 * @param start the 0-based character index of the first character
 * @param fullyQualifyTypeNames <code>true</code> if type names should be fully
 *   qualified, and <code>false</code> to use only simple names
 * @param buffer the string buffer to append to
 * @return the 0-based character index of the last character
 * @exception IllegalArgumentException if this is not a type argument signature
 * @see #scanTypeArgumentSignature(char[], int)
 */
private static int appendTypeArgumentSignature(char[] string, int start, boolean fullyQualifyTypeNames, StringBuffer buffer) {
	// need a minimum 1 char
	if (start >= string.length) {
		throw new IllegalArgumentException();
	}
	char c = string[start];
	switch(c) {
		case C_STAR :
			buffer.append('?');
			return start;
		case '+' :
			buffer.append("? extends "); //$NON-NLS-1$
			return appendTypeSignature(string, start + 1, fullyQualifyTypeNames, buffer);
		case '-' :
			buffer.append("? super "); //$NON-NLS-1$
			return appendTypeSignature(string, start + 1, fullyQualifyTypeNames, buffer);
		default :
			return appendTypeSignature(string, start, fullyQualifyTypeNames, buffer);
	}
}

/**
 * Converts the given array of qualified name segments to a qualified name.
 * <p>
 * For example:
 * <pre>
 * <code>
 * toQualifiedName({{'j', 'a', 'v', 'a'}, {'l', 'a', 'n', 'g'}, {'O', 'b', 'j', 'e', 'c', 't'}}) -> {'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'O', 'b', 'j', 'e', 'c', 't'}
 * toQualifiedName({{'O', 'b', 'j', 'e', 'c', 't'}}) -> {'O', 'b', 'j', 'e', 'c', 't'}
 * toQualifiedName({{}}) -> {}
 * </code>
 * </pre>
 * </p>
 *
 * @param segments the list of name segments, possibly empty
 * @return the dot-separated qualified name, or the empty string
 * 
 * @since 2.0
 */
public static char[] toQualifiedName(char[][] segments) {
	int length = segments.length;
	if (length == 0) return CharOperation.NO_CHAR;
	if (length == 1) return segments[0];
	
	int resultLength = 0;
	for (int i = 0; i < length; i++) {
		resultLength += segments[i].length+1;
	}
	resultLength--;
	char[] result = new char[resultLength];
	int index = 0;
	for (int i = 0; i < length; i++) {
		char[] segment = segments[i];
		int segmentLength = segment.length;
		System.arraycopy(segment, 0, result, index, segmentLength);
		index += segmentLength;
		if (i != length-1) {
			result[index++] = C_DOT;
		}
	}
	return result;
}
/**
 * Converts the given array of qualified name segments to a qualified name.
 * <p>
 * For example:
 * <pre>
 * <code>
 * toQualifiedName(new String[] {"java", "lang", "Object"}) -> "java.lang.Object"
 * toQualifiedName(new String[] {"Object"}) -> "Object"
 * toQualifiedName(new String[0]) -> ""
 * </code>
 * </pre>
 * </p>
 *
 * @param segments the list of name segments, possibly empty
 * @return the dot-separated qualified name, or the empty string
 */
public static String toQualifiedName(String[] segments) {
	int length = segments.length;
	char[][] charArrays = new char[length][];
	for (int i = 0; i < length; i++) {
		charArrays[i] = segments[i].toCharArray();
	}
	return new String(toQualifiedName(charArrays));
}
/**
 * Converts the given type signature to a readable string. The signature is expected to
 * be dot-based.
 * 
 * <p>
 * For example:
 * <pre>
 * <code>
 * toString("[Ljava.lang.String;") -> "java.lang.String[]"
 * toString("I") -> "int"
 * </code>
 * </pre>
 * </p>
 * <p>
 * Note: This method assumes that a type signature containing a <code>'$'</code>
 * is an inner type signature. While this is correct in most cases, someone could 
 * define a non-inner type name containing a <code>'$'</code>. Handling this 
 * correctly in all cases would have required resolving the signature, which 
 * generally not feasible.
 * </p>
 *
 * @param signature the type signature
 * @return the string representation of the type
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 */
public static String toString(String signature) throws IllegalArgumentException {
	return new String(toCharArray(signature.toCharArray()));
}
/**
 * Converts the given method signature to a readable string. The method signature is expected to
 * be dot-based.
 * 
 * @param methodSignature the method signature to convert
 * @param methodName the name of the method to insert in the result, or 
 *   <code>null</code> if no method name is to be included
 * @param parameterNames the parameter names to insert in the result, or 
 *   <code>null</code> if no parameter names are to be included; if supplied,
 *   the number of parameter names must match that of the method signature
 * @param fullyQualifyTypeNames <code>true</code> if type names should be fully
 *   qualified, and <code>false</code> to use only simple names
 * @param includeReturnType <code>true</code> if the return type is to be
 *   included
 * @see #toCharArray(char[], char[], char[][], boolean, boolean)
 * @return the string representation of the method signature
 */
public static String toString(String methodSignature, String methodName, String[] parameterNames, boolean fullyQualifyTypeNames, boolean includeReturnType) {
	char[][] params;
	if (parameterNames == null) {
		params = null;
	} else {
		int paramLength = parameterNames.length;
		params = new char[paramLength][];
		for (int i = 0; i < paramLength; i++) {
			params[i] = parameterNames[i].toCharArray();
		}
	}
	return new String(toCharArray(methodSignature.toCharArray(), methodName == null ? null : methodName.toCharArray(), params, fullyQualifyTypeNames, includeReturnType));
}

}
