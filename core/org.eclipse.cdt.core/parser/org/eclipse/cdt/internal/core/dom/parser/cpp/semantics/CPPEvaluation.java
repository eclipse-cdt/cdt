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
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.core.runtime.CoreException;

public abstract class CPPEvaluation implements ICPPEvaluation {

	private static class SignatureBuilder implements ITypeMarshalBuffer {
		private static final byte NULL_TYPE= 0;
		private static final byte UNSTORABLE_TYPE= (byte) -1;

		private final StringBuilder fBuffer;

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
		public void marshalTemplateArgument(ICPPTemplateArgument arg) throws CoreException {
			if (arg.isNonTypeValue()) {
				putByte(VALUE);
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
		public ISerializableEvaluation unmarshalEvaluation() throws CoreException {
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

	CPPEvaluation() {
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

	protected static IBinding resolveUnknown(ICPPUnknownBinding unknown, ICPPTemplateParameterMap tpMap,
			int packOffset, ICPPClassSpecialization within, IASTNode point) {
		try {
			return CPPTemplates.resolveUnknown(unknown, tpMap, packOffset, within, point);
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return unknown;
	}

	protected static ICPPTemplateArgument[] instantiateArguments(ICPPTemplateArgument[] args,
			ICPPTemplateParameterMap tpMap, int packOffset, ICPPClassSpecialization within, IASTNode point) {
		try {
			return CPPTemplates.instantiateArguments(args, tpMap, packOffset, within, point);
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return args;
	}
}