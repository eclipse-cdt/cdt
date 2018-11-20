/*******************************************************************************
 * Copyright (c) 2009, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.CStringValue;
import org.eclipse.cdt.internal.core.dom.parser.CompositeValue;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.FloatingPointValue;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateNonTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * For marshalling types to byte arrays.
 */
public final class TypeMarshalBuffer implements ITypeMarshalBuffer {
	public static final byte[] EMPTY = new byte[Database.TYPE_SIZE];
	public static final byte NULL_TYPE = 0x00;
	public static final byte INDIRECT_TYPE = 0x1F;
	public static final byte BINDING_TYPE = 0x1E;
	public static final byte UNSTORABLE_TYPE = 0x1D;

	public static final IType UNSTORABLE_TYPE_PROBLEM = new ProblemType(ISemanticProblem.TYPE_NOT_PERSISTED);

	private final PDOMLinkage fLinkage;
	private int fPos;
	private byte[] fBuffer;

	/**
	 * Constructor for output buffer.
	 */
	public TypeMarshalBuffer(PDOMLinkage linkage) {
		fLinkage = linkage;
	}

	/**
	 * Constructor for input buffer.
	 */
	public TypeMarshalBuffer(PDOMLinkage linkage, byte[] data) {
		fLinkage = linkage;
		fBuffer = data;
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
			putShort(NULL_TYPE);
		} else {
			PDOMNode pb = fLinkage.addTypeBinding(binding);
			if (pb == null && binding instanceof ITypedef) {
				// Since typedef defined in a local scope cannot be stored in the index,
				// store the target type instead.
				IType type = ((ITypedef) binding).getType();
				if (type instanceof ISerializableType) {
					((ISerializableType) type).marshal(this);
					return;
				} else if (type instanceof IBinding) {
					pb = fLinkage.addTypeBinding((IBinding) type);
				}
			}
			if (pb == null) {
				putShort(UNSTORABLE_TYPE);
			} else {
				putShort(BINDING_TYPE);
				putByte((byte) 0);
				putRecordPointer(pb.getRecord());
			}
		}
	}

	@Override
	public IBinding unmarshalBinding() throws CoreException {
		int oldPos = fPos;
		short firstBytes = getShort();
		if (firstBytes == BINDING_TYPE) {
			fPos += 1;
			long rec = getRecordPointer();
			return (IBinding) PDOMNode.load(fLinkage.getPDOM(), rec);
		} else if (firstBytes == NULL_TYPE) {
			return null;
		} else if (firstBytes == UNSTORABLE_TYPE) {
			return new ProblemBinding(null, ISemanticProblem.TYPE_NOT_PERSISTED);
		}

		fPos = oldPos; // fLinkage.unmarshalBinding() will read firstBytes again
		return fLinkage.unmarshalBinding(this);
	}

	@Override
	public void marshalType(IType type) throws CoreException {
		if (type instanceof ISerializableType) {
			((ISerializableType) type).marshal(this);
		} else if (type == null) {
			putShort(NULL_TYPE);
		} else if (type instanceof IBinding) {
			marshalBinding((IBinding) type);
		} else {
			CCorePlugin.log("Cannot serialize " + ASTTypeUtil.getType(type) + " (" + type.getClass().getName() + ")"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			putShort(UNSTORABLE_TYPE);
		}
	}

	@Override
	public IType unmarshalType() throws CoreException {
		int oldPos = fPos;
		short firstBytes = getShort();
		if (firstBytes == BINDING_TYPE) {
			fPos += 1;
			long rec = getRecordPointer();
			return (IType) PDOMNode.load(fLinkage.getPDOM(), rec);
		} else if (firstBytes == NULL_TYPE) {
			return null;
		} else if (firstBytes == UNSTORABLE_TYPE) {
			return UNSTORABLE_TYPE_PROBLEM;
		}

		fPos = oldPos; // fLinkage.unmarshalType() will read firstBytes again
		return fLinkage.unmarshalType(this);
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
	public ICPPEvaluation unmarshalEvaluation() throws CoreException {
		return fLinkage.unmarshalEvaluation(this);
	}

	@Override
	public ICPPExecution unmarshalExecution() throws CoreException {
		return fLinkage.unmarshalExecution(this);
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
	public IValue unmarshalValue() throws CoreException {
		short firstBytes = getShort();
		if (firstBytes == TypeMarshalBuffer.NULL_TYPE)
			return null;
		switch ((firstBytes & ITypeMarshalBuffer.KIND_MASK)) {
		case ITypeMarshalBuffer.INTEGRAL_VALUE:
			return IntegralValue.unmarshal(firstBytes, this);
		case ITypeMarshalBuffer.FLOATING_POINT_VALUE:
			return FloatingPointValue.unmarshal(firstBytes, this);
		case ITypeMarshalBuffer.C_STRING_VALUE:
			return CStringValue.unmarshal(firstBytes, this);
		case ITypeMarshalBuffer.COMPOSITE_VALUE:
			return CompositeValue.unmarshal(firstBytes, this);
		case ITypeMarshalBuffer.DEPENDENT_VALUE:
			return DependentValue.unmarshal(firstBytes, this);
		}
		throw new CoreException(CCorePlugin.createStatus("Cannot unmarshal a value, first bytes=" + firstBytes)); //$NON-NLS-1$
	}

	@Override
	public void marshalTemplateArgument(ICPPTemplateArgument arg) throws CoreException {
		if (arg.isNonTypeValue()) {
			putShort(VALUE);
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
		int oldPos = fPos;
		short firstBytes = getShort();
		if (firstBytes == VALUE) {
			return new CPPTemplateNonTypeArgument(unmarshalEvaluation());
		} else {
			fPos = oldPos;
			IType type = unmarshalType();
			IType originalType = unmarshalType();
			if (originalType == null)
				originalType = type;
			return new CPPTemplateTypeArgument(type, originalType);
		}
	}

	private void request(int i) {
		if (fBuffer == null) {
			if (i <= Database.TYPE_SIZE) {
				fBuffer = new byte[Database.TYPE_SIZE];
			} else {
				fBuffer = new byte[i];
			}
		} else {
			final int bufLen = fBuffer.length;
			int needLen = fPos + i;
			if (needLen > bufLen) {
				needLen = Math.max(needLen, 2 * bufLen);
				byte[] newBuffer = new byte[needLen];
				System.arraycopy(fBuffer, 0, newBuffer, 0, fPos);
				fBuffer = newBuffer;
			}
		}
	}

	@Override
	public void putByte(byte b) {
		request(1);
		fBuffer[fPos++] = b;
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
		int p = fPos;
		fBuffer[--p] = (byte) (value);
		value >>= 8;
		fBuffer[--p] = (byte) (value);
		value >>= 8;
		fBuffer[--p] = (byte) (value);
		value >>= 8;
		fBuffer[--p] = (byte) (value);
	}

	@Override
	public int getFixedInt() throws CoreException {
		if (fPos + 4 > fBuffer.length)
			throw unmarshallingError();
		int result = 0;
		result |= fBuffer[fPos++] & 0xff;
		result <<= 8;
		result |= fBuffer[fPos++] & 0xff;
		result <<= 8;
		result |= fBuffer[fPos++] & 0xff;
		result <<= 8;
		result |= fBuffer[fPos++] & 0xff;
		return result;
	}

	@Override
	public void putShort(short value) {
		putInt(value);
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
	public short getShort() throws CoreException {
		int result = getInt();
		if (result > Short.MAX_VALUE)
			unmarshallingError();
		return (short) result;
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
		Database.putRecPtr(record, fBuffer, fPos);
		fPos += Database.PTR_SIZE;
	}

	private long getRecordPointer() throws CoreException {
		final int pos = fPos;
		fPos += Database.PTR_SIZE;
		if (fPos > fBuffer.length) {
			fPos = fBuffer.length;
			throw unmarshallingError();
		}
		return Database.getRecPtr(fBuffer, pos);
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
		int len = getInt();
		char[] chars = new char[len];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) getInt();
		}
		return chars;
	}
}
