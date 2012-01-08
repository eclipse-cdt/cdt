/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *	  Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;

/**
 * This represents a declaration specifier for a built-in type.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTSimpleDeclSpecifier extends IASTDeclSpecifier {
	/**
	 * @since 5.2
	 */
	public static final ASTNodeProperty DECLTYPE_EXPRESSION = new ASTNodeProperty(
		"IASTSimpleDeclSpecifier.EXPRESSION [IASTExpression]"); //$NON-NLS-1$


	/**
	 * Used for omitted declaration specifiers. E.g. for declaration of constructors,
	 * or in plain c, where this defaults to an integer.
	 */
	public static final int t_unspecified = 0;

	/**
	 * <code>void x();</code>
	 */
	public static final int t_void = 1;

	/**
	 * <code>char c;</code>
	 */
	public static final int t_char = 2;

	/**
	 * <code>int i;</code>
	 */
	public static final int t_int = 3;

	/**
	 * <code>float f;</code>
	 */
	public static final int t_float = 4;

	/**
	 * <code>double d;</code>
	 */
	public static final int t_double = 5;

	/**
	 * Represents a boolean type (bool in c++, _Bool in c)
	 * @since 5.2
	 */
	public static final int t_bool = 6;

	/**
	 * <code>wchar_t c;</code>
	 * @since 5.2
	 */
	public static final int t_wchar_t = 7;

	/**
	 * <code>typeof 'c' c;</code>
	 * @since 5.2
	 */
	public static final int t_typeof = 8;

	/**
	 * <code>decltype('c') c;</code>
	 * @since 5.2
	 */
	public static final int t_decltype = 9;

	/**
	 * <code>auto c = expression;</code>
	 * @since 5.2
	 */
	public static final int t_auto = 10;

	/**
	 * <code>char16_t c;</code>
	 * @since 5.2
	 */
	public static final int t_char16_t = 11;

	/**
	 * <code>char32_t c;</code>
	 * @since 5.2
	 */
	public static final int t_char32_t = 12;


	/**
	 * @since 5.1
	 */
	@Override
	public IASTSimpleDeclSpecifier copy();

	/**
	 * This returns the built-in type for the declaration. The type is then
	 * refined by qualifiers for signed/unsigned and short/long. The type could
	 * also be unspecified which usually means int.
	 */
	public int getType();

	/**
	 * <code>signed char c;</code>
	 */
	public boolean isSigned();

	/**
	 * <code>unsigned int u;</code>
	 */
	public boolean isUnsigned();

	/**
	 * <code>short int s;</code>
	 */
	public boolean isShort();

	/**
	 * <code>long int l;</code>
	 */
	public boolean isLong();

	/**
	 * <code>long long int l;</code>
	 * @since 5.2
	 */
	public boolean isLongLong();

	/**
	 * <code>_Complex t</code>;
	 * @since 5.2
	 */
	public boolean isComplex();
	
	/**
	 * <code>_Imaginary t</code>;
	 * @since 5.2
	 */
	public boolean isImaginary();

	/**
	 * Returns the expression for simple declaration specifiers of type 
	 * {@link IASTSimpleDeclSpecifier#t_decltype} or {@link IASTSimpleDeclSpecifier#t_typeof}.
	 * Other simple declaration specifiers will return <code>null</code>.
	 * @since 5.2
	 */
	public IASTExpression getDeclTypeExpression();

	/**
	 * Not allowed on frozen ast.
	 * @see #getType()
	 */
	public void setType(int type);
	
	/**
	 * Not allowed on frozen ast.
	 * Sets this declaration specifier to the type based on {@link IBasicType.Kind}.
	 * @since 5.2
	 */
	public void setType(Kind kind);

	/**
	 * Not allowed on frozen ast.
	 * @see #isSigned()
	 */
	public void setSigned(boolean value);

	/**
	 * Not allowed on frozen ast.
	 * @see #isUnsigned()
	 */
	public void setUnsigned(boolean value);

	/**
	 * Not allowed on frozen ast.
	 * @see #isShort()
	 */
	public void setShort(boolean value);

	/**
	 * Not allowed on frozen ast.
	 * @see #isLong()
	 */
	public void setLong(boolean value);

	/**
	 * Not allowed on frozen ast.
	 * @see #isLongLong()
	 * @since 5.2
	 */
	public void setLongLong(boolean value);
	
	/**
	 * Not allowed on frozen ast.
	 * @see #isComplex()
	 * @since 5.2
	 */
	public void setComplex(boolean value);

	/**
	 * Not allowed on frozen ast.
	 * @see #isImaginary()
	 * @since 5.2
	 */
	public void setImaginary(boolean value);

	/**
	 * Not allowed on frozen ast.
	 * @see #getDeclTypeExpression()
	 * @since 5.2
	 */
	public void setDeclTypeExpression(IASTExpression expression);
	
	/**
	 * @deprecated all constants must be defined in this interface
	 */
	@Deprecated
	public static final int t_last = t_double; // used only in subclasses

}
