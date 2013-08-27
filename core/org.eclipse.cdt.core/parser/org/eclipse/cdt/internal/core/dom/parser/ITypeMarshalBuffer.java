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
	final static byte BASIC_TYPE                    = 0x01;
	final static byte POINTER_TYPE                  = 0x02;
	final static byte ARRAY_TYPE                    = 0x03;
	final static byte CVQUALIFIER_TYPE              = 0x04;
	final static byte FUNCTION_TYPE                 = 0x05;
	final static byte REFERENCE_TYPE                = 0x06;
	final static byte POINTER_TO_MEMBER_TYPE        = 0x07;
	final static byte PACK_EXPANSION_TYPE           = 0x08;
	final static byte PROBLEM_TYPE                  = 0x09;
	final static byte VALUE                         = 0x0A;
	final static byte DEPENDENT_EXPRESSION_TYPE     = 0x0B;
	final static byte UNKNOWN_MEMBER                = 0x0C;
	final static byte UNKNOWN_MEMBER_CLASS_INSTANCE = 0x0D;
	final static byte DEFERRED_CLASS_INSTANCE       = 0x0E;
	final static byte ALIAS_TEMPLATE                = 0x0F;
	final static byte TYPE_TRANSFORMATION           = 0x10;
	// Can add more types up to 0x1C, after that it will collide with TypeMarshalBuffer.UNSTORABLE_TYPE. 

	final static byte
		EVAL_BINARY         = 0x01,
		EVAL_BINARY_TYPE_ID = 0x02,
		EVAL_BINDING        = 0x03,
		EVAL_COMMA          = 0x04,
		EVAL_COMPOUND       = 0x05,
		EVAL_CONDITIONAL    = 0x06,
		EVAL_FIXED          = 0x07,
		EVAL_FUNCTION_CALL  = 0x08,
		EVAL_FUNCTION_SET   = 0x09,
		EVAL_ID             = 0x0A,
		EVAL_INIT_LIST      = 0x0B,
		EVAL_MEMBER_ACCESS  = 0x0C,
		EVAL_PARAMETER_PACK = 0x0D,
		EVAL_TYPE_ID        = 0x0E,
		EVAL_UNARY          = 0x0F,
		EVAL_UNARY_TYPE_ID  = 0x10;
	// Can add more evaluations up to 0x1C, after that it will collide with TypeMarshalBuffer.UNSTORABLE_TYPE.

	static final short KIND_MASK = 0x001F;

	final static short FLAG1 = 0x0020;
	final static short FLAG2 = 0x0040;
	final static short FLAG3 = 0x0080;
	final static short FLAG4 = 0x0100;
	final static short FLAG5 = 0x0200;
	final static short FLAG6 = 0x0400;
	final static short FLAG7 = 0x0800;
	final static short FLAG8 = 0x1000;
	final static short FLAG9 = 0x2000;
	
	final static short FIRST_FLAG       = FLAG1;
	final static short SECOND_LAST_FLAG = FLAG8;
	final static short LAST_FLAG        = FLAG9;

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
