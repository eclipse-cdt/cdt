/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.core.runtime.CoreException;

/**
 * Buffer for marshalling and unmarshalling types.
 */
public interface ITypeMarshalBuffer {
	final static byte BASIC_TYPE=     			1;
	final static byte POINTER=        			2;
	final static byte ARRAY=            		3;
	final static byte CVQUALIFIER=      		4;
	final static byte FUNCTION_TYPE=    		5;
	final static byte REFERENCE=        		6;
	final static byte POINTER_TO_MEMBER=    	7;
	final static byte PACK_EXPANSION= 			8;
	final static byte PROBLEM_TYPE= 			9;
	static final byte KIND_MASK = 0xf;
	
	final static int FLAG1	= 0x10;
	final static int FLAG2	= 0x20;
	final static int FLAG3	= 0x40;
	final static int FLAG4	= 0x80;

	CoreException unmarshallingError();
	
	IType unmarshalType() throws CoreException;
	int getByte() throws CoreException;
	int getShort() throws CoreException;
	IValue getValue() throws CoreException;

	void marshalType(IType type) throws CoreException;
	void putByte(byte data);
	void putShort(short data);
	void putValue(IValue val) throws CoreException;
}
