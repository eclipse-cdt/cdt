/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateNonTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * For marshalling types to byte arrays.
 */
public final class TypeMarshalBuffer implements ITypeMarshalBuffer {
	public static final byte[] EMPTY= { 0, 0, 0, 0, 0, 0 };
	public static final byte NULL_TYPE= 0;
	public static final byte INDIRECT_TYPE= (byte) -1;
	public static final byte BINDING_TYPE= (byte) -2;
	public static final byte UNSTORABLE_TYPE= (byte) -3;

	public static final IType UNSTORABLE_TYPE_PROBLEM = new ProblemType(ISemanticProblem.TYPE_NOT_PERSISTED);

	static {
		assert EMPTY.length == Database.TYPE_SIZE;
	}

	private final PDOMLinkage fLinkage;
	private int fPos;
	private byte[] fBuffer;

	/**
	 * Constructor for output buffer.
	 */
	public TypeMarshalBuffer(PDOMLinkage linkage) {
		fLinkage= linkage;
	}

	/**
	 * Constructor for input buffer.
	 */
	public TypeMarshalBuffer(PDOMLinkage linkage, byte[] data) {
		fLinkage= linkage;
		fBuffer= data;
	}

	public int getPosition() {
		return fPos;
	}

	public void setPosition(int pos) {
		assert 0 <= pos && pos <= fPos;
		fPos = pos;
	}

	public byte[] getBuffer() {
		return fBuffer;
	}

	@Override
	public void marshalBinding(IBinding binding) throws CoreException {
		if (binding instanceof ISerializableType) {
			((ISerializableType) binding).marshal(this);
		} else if (binding == null) {
			putByte(NULL_TYPE);
		} else {
			PDOMNode pb= fLinkage.addTypeBinding(binding);
			if (pb == null) {
				putByte(UNSTORABLE_TYPE);
			} else {
				putByte(BINDING_TYPE);
				putByte((byte) 0);
				putRecordPointer(pb.getRecord());
			}
		}
	}

	@Override
	public IBinding unmarshalBinding() throws CoreException {
		if (fPos >= fBuffer.length)
			throw unmarshallingError();

		byte firstByte= fBuffer[fPos];
		if (firstByte == BINDING_TYPE) {
			fPos += 2;
			long rec= getRecordPointer();
			return (IBinding) fLinkage.getNode(rec);
		} else if (firstByte == NULL_TYPE || firstByte == UNSTORABLE_TYPE) {
			fPos++;
			return null;
		}

		return fLinkage.unmarshalBinding(this);
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
			assert false : "Cannot serialize " + ASTTypeUtil.getType(type) + " (" + type.getClass().getName() + ")";   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			putByte(UNSTORABLE_TYPE);
		}
	}

	@Override
	public IType unmarshalType() throws CoreException {
		if (fPos >= fBuffer.length)
			throw unmarshallingError();

		byte firstByte= fBuffer[fPos];
		if (firstByte == BINDING_TYPE) {
			fPos += 2;
			long rec= getRecordPointer();
			return (IType) fLinkage.getNode(rec);
		} else if (firstByte == NULL_TYPE) {
			fPos++;
			return null;
		} else if (firstByte == UNSTORABLE_TYPE) {
			fPos++;
			return UNSTORABLE_TYPE_PROBLEM;
		}

		return fLinkage.unmarshalType(this);
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
	public ISerializableEvaluation unmarshalEvaluation() throws CoreException {
		if (fPos >= fBuffer.length)
			throw unmarshallingError();

		byte firstByte= fBuffer[fPos];
		if (firstByte == NULL_TYPE) {
			fPos++;
			return null;
		}
		return fLinkage.unmarshalEvaluation(this);
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
	public IValue unmarshalValue() throws CoreException {
		if (fPos >= fBuffer.length)
			throw unmarshallingError();

		return Value.unmarshal(this);
	}

	@Override
	public void marshalTemplateArgument(ICPPTemplateArgument arg) throws CoreException {
		if (arg.isNonTypeValue()) {
			putByte(VALUE);
			arg.getNonTypeEvaluation().marshal(this, true);
		} else {
			final IType typeValue = arg.getTypeValue();
			final IType originalTypeValue = arg.getOriginalTypeValue();
			marshalType(typeValue);
			if (typeValue != originalTypeValue) {
				marshalType(originalTypeValue);
			} else {
				marshalType(null);
			}
		}
	}

	@Override
	public ICPPTemplateArgument unmarshalTemplateArgument() throws CoreException {
		int firstByte= getByte();
		if (firstByte == VALUE) {
			return new CPPTemplateNonTypeArgument((ICPPEvaluation) unmarshalEvaluation(), null);
		} else {
			fPos--;
			IType type = unmarshalType();
			IType originalType = unmarshalType();
			if (originalType == null || originalType == UNSTORABLE_TYPE_PROBLEM)
				originalType= type;
			return new CPPTemplateTypeArgument(type, originalType);
		}
	}

	private void request(int i) {
		if (fBuffer == null) {
			if (i <= Database.TYPE_SIZE) {
				fBuffer= new byte[Database.TYPE_SIZE];
			} else {
				fBuffer= new byte[i];
			}
		} else {
			final int bufLen = fBuffer.length;
			int needLen = fPos + i;
			if (needLen > bufLen) {
				needLen= Math.max(needLen, 2 * bufLen);
				byte[] newBuffer= new byte[needLen];
				System.arraycopy(fBuffer, 0, newBuffer, 0, fPos);
				fBuffer= newBuffer;
			}
		}
	}

	@Override
	public void putByte(byte b) {
		request(1);
		fBuffer[fPos++]= b;
	}

	@Override
	public int getByte() throws CoreException {
		if (fPos + 1 > fBuffer.length)
			throw unmarshallingError();
		return 0xff & fBuffer[fPos++];
	}

	@Override
	public CoreException unmarshallingError() {
		return new CoreException(CCorePlugin.createStatus("Unmarshalling error")); //$NON-NLS-1$
	}

	public CoreException marshallingError() {
		return new CoreException(CCorePlugin.createStatus("Marshalling error")); //$NON-NLS-1$
	}

	@Override
	public void putFixedInt(int value) {
		request(4);
		fPos += 4;
		int p= fPos;
		fBuffer[--p]= (byte) (value); value >>= 8;
		fBuffer[--p]= (byte) (value); value >>= 8;
		fBuffer[--p]= (byte) (value); value >>= 8;
		fBuffer[--p]= (byte) (value);
	}

	@Override
	public int getFixedInt() throws CoreException {
		if (fPos + 4 > fBuffer.length)
			throw unmarshallingError();
		int result= 0;
		result |= fBuffer[fPos++] & 0xff; result <<= 8;
		result |= fBuffer[fPos++] & 0xff; result <<= 8;
		result |= fBuffer[fPos++] & 0xff; result <<= 8;
		result |= fBuffer[fPos++] & 0xff;
		return result;
	}

	@Override
	public void putInt(int value) {
		do {
			int b = value & 0x7F;
			value >>>= 7;
			if (value != 0)
				b |= 0x80;
			putByte((byte) b);
		} while (value != 0);
	}

	@Override
	public void putLong(long value) {
		do {
			int b = (int) value & 0x7F;
			value >>>= 7;
			if (value != 0)
				b |= 0x80;
			putByte((byte) b);
		} while (value != 0);
	}

	@Override
	public int getInt() throws CoreException {
		int b = getByte();
		int value = b & 0x7F;
		for (int shift = 7; (b & 0x80) != 0; shift += 7) {
			b = getByte();
			value |= (b & 0x7F) << shift;
		}
		return value;
	}

	@Override
	public long getLong() throws CoreException {
		int b = getByte();
		long value = b & 0x7F;
		for (int shift = 7; (b & 0x80) != 0; shift += 7) {
			b = getByte();
			value |= (b & 0x7F) << shift;
		}
		return value;
	}

	private void putRecordPointer(long record) {
		request(Database.PTR_SIZE);
		Chunk.putRecPtr(record, fBuffer, fPos);
		fPos += Database.PTR_SIZE;
	}

	private long getRecordPointer() throws CoreException {
		final int pos= fPos;
		fPos += Database.PTR_SIZE;
		if (fPos > fBuffer.length) {
			fPos= fBuffer.length;
			throw unmarshallingError();
		}
		return Chunk.getRecPtr(fBuffer, pos);
	}

	@Override
	public void putCharArray(char[] chars) {
		putInt(chars.length);
		for (char c : chars) {
			putInt(c & 0xFFFF);
		}
	}

	@Override
	public char[] getCharArray() throws CoreException {
		int len= getInt();
		char[] chars= new char[len];
		for (int i = 0; i < chars.length; i++) {
			chars[i]= (char) getInt();
		}
		return chars;
	}
}
