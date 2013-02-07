/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.internal.core.dom.ast.tag;

import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.dom.ast.tag.IWritableTag;

/**
 * A trivial implementation that stores all data in memory.
 * @see SimpleTaggable
 */
public class SimpleTag implements IWritableTag
{
	private final byte[] buff;

	public SimpleTag( int dataLen )
	{
		this.buff = new byte[dataLen];
	}

	private boolean isInBounds( int offset, int len )
	{
		return offset >= 0
			&& offset < buff.length
			&& ( offset + len ) <= buff.length;
	}

	@Override
	public boolean putByte( int offset, byte b )
	{
		if( ! isInBounds( offset, 1 ) )
			return false;

		buff[offset] = b;
		return true;
	}

	@Override
	public int getByte( int offset )
	{
		return isInBounds( offset, 1 ) ? buff[offset] : ITag.Fail;
	}
}
