/*******************************************************************************
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.ast.tag;

import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.dom.ast.tag.IWritableTag;

/**
 * A trivial implementation that stores all data in memory.
 *
 * @see NonCachedTaggable
 */
public class Tag implements IWritableTag {
	private final String taggerId;
	private final byte[] buff;

	public Tag(String taggerId, int dataLen) {
		this.taggerId = taggerId;
		this.buff = new byte[dataLen];
	}

	@Override
	public String getTaggerId() {
		return taggerId;
	}

	@Override
	public int getDataLen() {
		return buff.length;
	}

	private boolean isInBounds(int offset, int len) {
		return offset >= 0 && offset < buff.length && offset + len <= buff.length;
	}

	@Override
	public boolean putByte(int offset, byte b) {
		if (!isInBounds(offset, 1))
			return false;

		buff[offset] = b;
		return true;
	}

	@Override
	public boolean putBytes(int offset, byte[] data, int len) {
		len = len >= 0 ? len : data.length;
		if (!isInBounds(offset, len))
			return false;

		System.arraycopy(data, 0, buff, offset, len);
		return true;
	}

	@Override
	public int getByte(int offset) {
		return isInBounds(offset, 1) ? buff[offset] : ITag.FAIL;
	}

	@Override
	public byte[] getBytes(int offset, int len) {
		len = len >= 0 ? len : buff.length - offset;
		if (!isInBounds(offset, len))
			return null;

		byte[] data = new byte[len];
		System.arraycopy(buff, offset, data, 0, len);
		return data;
	}
}
