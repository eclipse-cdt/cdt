/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Thomas Corbat
 *     Sergey Prigogin (Google)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.core.runtime.CoreException;

/**
 * Buffer for marshalling and unmarshalling types.
 */
public interface ITypeMarshalBuffer {
	final static short BASIC_TYPE                    = 0x0001;
	final static short POINTER_TYPE                  = 0x0002;
	final static short ARRAY_TYPE                    = 0x0003;
	final static short CVQUALIFIER_TYPE              = 0x0004;
	final static short FUNCTION_TYPE                 = 0x0005;
	final static short REFERENCE_TYPE                = 0x0006;
	final static short POINTER_TO_MEMBER_TYPE        = 0x0007;
	final static short PACK_EXPANSION_TYPE           = 0x0008;
	final static short PROBLEM_TYPE                  = 0x0009;
	final static short VALUE                         = 0x000A;
	final static short DEPENDENT_EXPRESSION_TYPE     = 0x000B;
	final static short UNKNOWN_MEMBER                = 0x000C;
	final static short UNKNOWN_MEMBER_CLASS_INSTANCE = 0x000D;
	final static short DEFERRED_CLASS_INSTANCE       = 0x000E;
	final static short ALIAS_TEMPLATE                = 0x000F;
	// can add more types up to 0x001C, after that it will collide with TypeMarshalBuffer.UNSTORABLE_TYPE 

	final static short
		EVAL_BINARY         = 0x0001,
		EVAL_BINARY_TYPE_ID = 0x0002,
		EVAL_BINDING        = 0x0003,
		EVAL_COMMA          = 0x0004,
		EVAL_COMPOUND       = 0x0005,
		EVAL_CONDITIONAL    = 0x0006,
		EVAL_FIXED          = 0x0007,
		EVAL_FUNCTION_CALL  = 0x0008,
		EVAL_FUNCTION_SET   = 0x0009,
		EVAL_ID             = 0x000A,
		EVAL_INIT_LIST      = 0x000B,
		EVAL_MEMBER_ACCESS  = 0x000C,
		EVAL_PARAMETER_PACK = 0x000D,
		EVAL_TYPE_ID        = 0x000E,
		EVAL_UNARY          = 0x000F,
		EVAL_UNARY_TYPE_ID  = 0x0010;
	// can add more evaluations up to 0x001C, after that it will collide with TypeMarshalBuffer.UNSTORABLE_TYPE

	static final short KIND_MASK = 0x001F;

	final static short FLAG1	= 0x0020;
	final static short FLAG2	= 0x0040;
	final static short FLAG3	= 0x0080;
	final static short FLAG4	= 0x0100;

	CoreException unmarshallingError();

	IType unmarshalType() throws CoreException;
	IValue unmarshalValue() throws CoreException;
	IBinding unmarshalBinding() throws CoreException;
	ISerializableEvaluation unmarshalEvaluation() throws CoreException;
	ICPPTemplateArgument unmarshalTemplateArgument() throws CoreException;
	int getByte() throws CoreException;
	int getFixedInt() throws CoreException;

	/**
	 * Reads a 16-bit integer stored in the variable length base-128 encoding. 
	 */
	public short getShort() throws CoreException;
	
	/**
	 * Reads a 32-bit integer stored in the variable length base-128 encoding.
	 */
	public int getInt() throws CoreException;

	/**
	 * Reads a 64-bit integer stored in the variable length base-128 encoding.
	 */
	public long getLong() throws CoreException;

	char[] getCharArray() throws CoreException;

	void marshalType(IType type) throws CoreException;
	void marshalValue(IValue value) throws CoreException;
	void marshalBinding(IBinding binding) throws CoreException;
	void marshalEvaluation(ISerializableEvaluation eval, boolean includeValue) throws CoreException;
	void marshalTemplateArgument(ICPPTemplateArgument arg) throws CoreException;
	void putByte(byte data);
	void putFixedInt(int data);

	/**
	 * Writes a 16-bit integer in the variable length base-128 encoding.
	 * @param value the value to write
	 */
	public void putShort(short value);
	
	/**
	 * Writes a 32-bit integer in the variable length base-128 encoding. Each byte, except the last
	 * byte, has the most significant bit set – this indicates that there are further bytes to come.
	 * The lower 7 bits of each byte are used to store the two-complement representation of
	 * the number in groups of 7 bits, least significant group first.
	 * 
	 * <p>Here is number of bytes depending on the encoded value:
	 * <pre>
	 * Value                   Number of bytes
	 * [0,127]                          1
	 * [128,16383]                      2
	 * [16384,2097151]                  3
	 * [2097152,268435455]              4
	 * [268435456,Integer.MAX_VALUE]    5
	 * negative                         5
	 * </pre>
	 *
	 * @param value the value to write
	 */
	public void putInt(int value);

	/**
	 * Writes a 64-bit integer in the variable length base-128 encoding. Each byte, except the last
	 * byte, has the most significant bit set – this indicates that there are further bytes to come.
	 * The lower 7 bits of each byte are used to store the two-complement representation of
	 * the number in groups of 7 bits, least significant group first.
	 * 
	 * <p>Here is number of bytes depending on the encoded value:
	 * <pre>
	 * Value                   Number of bytes
	 * [0,127]                          1
	 * [128,16383]                      2
	 * [16384,2097151]                  3
	 * [2097152,268435455]              4
	 * [268435456,2^35-1]               5
	 * [2^35,2^42-1]                    6
	 * [2^42,2^49-1]                    7
	 * [2^49,2^56-1]                    8
	 * [2^56,2^63-1]                    9
	 * negative                        10
	 * </pre>
	 *
	 * @param value the value to write
	 */
	public void putLong(long value);

	void putCharArray(char[] data);
}
