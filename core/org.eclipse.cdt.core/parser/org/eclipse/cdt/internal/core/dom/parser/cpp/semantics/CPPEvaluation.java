/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.core.runtime.CoreException;

public abstract class CPPEvaluation implements ICPPEvaluation {

	private static class SignatureBuilder implements ITypeMarshalBuffer {
		private static final byte NULL_TYPE= 0;
		private static final byte UNSTORABLE_TYPE= (byte) -1;
		private static final char[] HEX_DIGITS =
				{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

		private final StringBuilder fBuffer;
		private boolean hexMode;

		/**
		 * Constructor for input buffer.
		 */
		public SignatureBuilder() {
			fBuffer= new StringBuilder();
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
				putByte(NULL_TYPE);
			} else {
				appendSeparator();
				if (binding instanceof ICPPBinding) {
					fBuffer.append(ASTTypeUtil.getQualifiedName((ICPPBinding) binding));
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
				putByte(NULL_TYPE);
			} else if (type instanceof IBinding) {
				marshalBinding((IBinding) type);
			} else {
				assert false : "Cannot serialize " + ASTTypeUtil.getType(type) + " (" + type.getClass().getName() + ")"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				putByte(UNSTORABLE_TYPE);
			}
		}

		@Override
		public void marshalEvaluation(ISerializableEvaluation eval, boolean includeValues) throws CoreException {
			if (eval == null) {
				putByte(NULL_TYPE);
			} else {
				eval.marshal(this, includeValues);
			}
		}

		@Override
		public void marshalValue(IValue value) throws CoreException {
			if (value instanceof Value) {
				((Value) value).marshall(this);
			} else {
				putByte(NULL_TYPE);
			}
		}

		@Override
		public void putByte(byte b) {
			appendHexDigit(b >> 4);
			appendHexDigit(b);
		}

		@Override
		public void putShort(short value) {
			appendHexDigit(value >> 12);
			appendHexDigit(value >> 8);
			appendHexDigit(value >> 4);
			appendHexDigit(value);
		}

		@Override
		public void putInt(int value) {
			appendHexDigit(value >> 28);
			appendHexDigit(value >> 24);
			appendHexDigit(value >> 20);
			appendHexDigit(value >> 16);
			appendHexDigit(value >> 12);
			appendHexDigit(value >> 8);
			appendHexDigit(value >> 4);
			appendHexDigit(value);
		}

		@Override
		public void putLong(long value) {
			appendHexDigit((int) (value >> 60));
			appendHexDigit((int) (value >> 56));
			appendHexDigit((int) (value >> 52));
			appendHexDigit((int) (value >> 48));
			appendHexDigit((int) (value >> 44));
			appendHexDigit((int) (value >> 40));
			appendHexDigit((int) (value >> 36));
			appendHexDigit((int) (value >> 32));
			appendHexDigit((int) (value >> 28));
			appendHexDigit((int) (value >> 24));
			appendHexDigit((int) (value >> 20));
			appendHexDigit((int) (value >> 16));
			appendHexDigit((int) (value >> 12));
			appendHexDigit((int) (value >> 8));
			appendHexDigit((int) (value >> 4));
			appendHexDigit((int) value);
		}

		@Override
		public void putCharArray(char[] chars) {
			appendSeparator();
			for (char c : chars) {
				fBuffer.append(c);
			}
		}

		private void appendHexDigit(int val) {
			if (hexMode) {
				appendSeparator();
				fBuffer.append("0x"); //$NON-NLS-1$
				hexMode = true;
			}
			fBuffer.append(HEX_DIGITS[val & 0xF]);
		}

		private void appendSeparator() {
			if (fBuffer.length() != 0)
				fBuffer.append(' ');
			hexMode = false;
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
		public ISerializableEvaluation unmarshalEvaluation() throws CoreException {
			throw new UnsupportedOperationException();
		}

		@Override
		public IValue unmarshalValue() throws CoreException {
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
		public int getShort() throws CoreException {
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

	@Override
	public char[] getSignature() {
		SignatureBuilder buf = new SignatureBuilder();
		try {
			marshal(buf, true);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[] { '?' };
		}
		return buf.getSignature();
	}
}