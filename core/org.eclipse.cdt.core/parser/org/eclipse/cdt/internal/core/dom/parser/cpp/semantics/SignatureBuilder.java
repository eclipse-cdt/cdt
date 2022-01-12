/*******************************************************************************
 * Copyright (c) 2012, 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.core.runtime.CoreException;

class SignatureBuilder implements ITypeMarshalBuffer {
	private static final short NULL_TYPE = 0x0000;
	private static final short UNSTORABLE_TYPE = 0x001D;

	private final StringBuilder fBuffer;

	/**
	 * Constructor for input buffer.
	 */
	public SignatureBuilder() {
		fBuffer = new StringBuilder();
	}

	@Override
	public String toString() {
		return fBuffer.toString();
	}

	public char[] getSignature() {
		return CharArrayUtils.extractChars(fBuffer);
	}

	@Override
	public void marshalBinding(IBinding binding) throws CoreException {
		if (binding instanceof ISerializableType) {
			((ISerializableType) binding).marshal(this);
		} else if (binding == null) {
			putShort(NULL_TYPE);
		} else {
			appendSeparator();
			if (binding instanceof ICPPBinding) {
				if (binding instanceof ICPPTemplateParameter) {
					ICPPTemplateParameter param = (ICPPTemplateParameter) binding;
					fBuffer.append(param.isParameterPack() ? '*' : '#');
					fBuffer.append(param.getParameterID());
				} else {
					fBuffer.append(ASTTypeUtil.getQualifiedName((ICPPBinding) binding));
				}
			} else {
				fBuffer.append(binding.getNameCharArray());
			}
		}
	}

	@Override
	public void marshalType(IType type) throws CoreException {
		if (type instanceof ISerializableType) {
			((ISerializableType) type).marshal(this);
		} else if (type == null) {
			putShort(NULL_TYPE);
		} else if (type instanceof IBinding) {
			marshalBinding((IBinding) type);
		} else if (type instanceof UniqueType) {
			// UniqueType is not an ISerializableType because there should never be
			// a need to write it to the index, but it can appear in a signature
			// during partial ordering of function templates.
			appendSeparator();
			fBuffer.append("Unique(@"); //$NON-NLS-1$
			fBuffer.append(Integer.toHexString(System.identityHashCode(type)));
			fBuffer.append(')');
		} else {
			assert false : "Cannot serialize " + ASTTypeUtil.getType(type) + " (" + type.getClass().getName() + ")"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			putShort(UNSTORABLE_TYPE);
		}
	}

	@Override
	public void marshalEvaluation(ICPPEvaluation eval, boolean includeValues) throws CoreException {
		if (eval == null) {
			putShort(NULL_TYPE);
		} else {
			eval.marshal(this, includeValues);
		}
	}

	@Override
	public void marshalExecution(ICPPExecution exec, boolean includeValue) throws CoreException {
		if (exec == null) {
			putShort(NULL_TYPE);
		} else {
			exec.marshal(this, includeValue);
		}
	}

	@Override
	public void marshalValue(IValue value) throws CoreException {
		if (value != null) {
			value.marshal(this);
		} else {
			putShort(NULL_TYPE);
		}
	}

	@Override
	public void marshalTemplateArgument(ICPPTemplateArgument arg) throws CoreException {
		if (arg.isNonTypeValue()) {
			putShort(VALUE);
			arg.getNonTypeEvaluation().marshal(this, true);
		} else {
			marshalType(arg.getTypeValue());
		}
	}

	@Override
	public void putByte(byte value) {
		appendSeparator();
		fBuffer.append(value);
	}

	@Override
	public void putFixedInt(int value) {
		appendSeparator();
		fBuffer.append(value);
	}

	@Override
	public void putShort(short value) {
		appendSeparator();
		fBuffer.append(value);
	}

	@Override
	public void putInt(int value) {
		appendSeparator();
		fBuffer.append(value);
	}

	@Override
	public void putLong(long value) {
		appendSeparator();
		fBuffer.append(value);
	}

	@Override
	public void putCharArray(char[] chars) {
		appendSeparator();
		for (char c : chars) {
			fBuffer.append(c);
		}
	}

	private void appendSeparator() {
		if (fBuffer.length() != 0)
			fBuffer.append(' ');
	}

	@Override
	public IBinding unmarshalBinding() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IType unmarshalType() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ICPPEvaluation unmarshalEvaluation() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ICPPExecution unmarshalExecution() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IValue unmarshalValue() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ICPPTemplateArgument unmarshalTemplateArgument() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getByte() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public CoreException unmarshallingError() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getFixedInt() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getShort() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getInt() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLong() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public char[] getCharArray() throws CoreException {
		throw new UnsupportedOperationException();
	}
}