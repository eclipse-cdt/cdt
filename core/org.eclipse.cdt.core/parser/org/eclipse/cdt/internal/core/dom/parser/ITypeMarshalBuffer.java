/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Thomas Corbat
 *     Sergey Prigogin (Google)
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
	final static byte BASIC_TYPE=     				  1;
	final static byte POINTER_TYPE=        			  2;
	final static byte ARRAY_TYPE=            		  3;
	final static byte CVQUALIFIER_TYPE=      		  4;
	final static byte FUNCTION_TYPE=    			  5;
	final static byte REFERENCE_TYPE=        		  6;
	final static byte POINTER_TO_MEMBER_TYPE=   	  7;
	final static byte PACK_EXPANSION_TYPE= 			  8;
	final static byte PROBLEM_TYPE= 				  9;
	final static byte VALUE= 				   	     10;
	final static byte DEPENDENT_EXPRESSION_TYPE=     11;
	final static byte UNKNOWN_MEMBER=			     12;
	final static byte UNKNOWN_MEMBER_CLASS_INSTANCE= 13;
	final static byte DEFERRED_CLASS_INSTANCE=     	 14;
	final static byte ALIAS_TEMPLATE =				 15;

	final static byte
		EVAL_BINARY= 1,
		EVAL_BINARY_TYPE_ID = 2,
		EVAL_BINDING = 3,
		EVAL_COMMA = 4,
		EVAL_COMPOUND = 5,
		EVAL_CONDITIONAL = 6,
		EVAL_FIXED= 7,
		EVAL_FUNCTION_CALL= 8,
		EVAL_FUNCTION_SET= 9,
		EVAL_ID= 10,
		EVAL_INIT_LIST= 11,
		EVAL_MEMBER_ACCESS= 12,
		EVAL_TYPE_ID= 13,
		EVAL_UNARY= 14,
		EVAL_UNARY_TYPE_ID = 15;

	static final byte KIND_MASK= 				   15;

	final static int FLAG1	= 0x10;
	final static int FLAG2	= 0x20;
	final static int FLAG3	= 0x40;
	final static int FLAG4	= 0x80;

	CoreException unmarshallingError();

	IType unmarshalType() throws CoreException;
	IValue unmarshalValue() throws CoreException;
	IBinding unmarshalBinding() throws CoreException;
	ISerializableEvaluation unmarshalEvaluation() throws CoreException;
	ICPPTemplateArgument unmarshalTemplateArgument() throws CoreException;
	int getByte() throws CoreException;
	int getFixedInt() throws CoreException;

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
