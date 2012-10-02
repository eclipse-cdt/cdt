/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
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
	final static byte BASIC_TYPE=     				1;
	final static byte POINTER_TYPE=        			2;
	final static byte ARRAY_TYPE=            		3;
	final static byte CVQUALIFIER_TYPE=      		4;
	final static byte FUNCTION_TYPE=    			5;
	final static byte REFERENCE_TYPE=        		6;
	final static byte POINTER_TO_MEMBER_TYPE=   	7;
	final static byte PACK_EXPANSION_TYPE= 			8;
	final static byte PROBLEM_TYPE= 				9;
	final static byte VALUE= 				   	   10;
	final static byte DEPENDENT_EXPRESSION_TYPE=   11;
	final static byte UNKNOWN_MEMBER=			   12;
	final static byte UNKNOWN_MEMBER_CLASS_INSTANCE= 13;
	final static byte DEFERRED_CLASS_INSTANCE=     	 14;

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
	int getShort() throws CoreException;
	int getInt() throws CoreException;
	long getLong() throws CoreException;
	char[] getCharArray() throws CoreException;

	void marshalType(IType type) throws CoreException;
	void marshalValue(IValue value) throws CoreException;
	void marshalBinding(IBinding binding) throws CoreException;
	void marshalEvaluation(ISerializableEvaluation eval, boolean includeValue) throws CoreException;
	void marshalTemplateArgument(ICPPTemplateArgument arg) throws CoreException;
	void putByte(byte data);
	void putShort(short data);
	void putInt(int data);
	void putLong(long data);
	void putCharArray(char[] data);
}
