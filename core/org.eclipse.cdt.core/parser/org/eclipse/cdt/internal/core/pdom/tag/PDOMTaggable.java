/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.internal.core.pdom.tag;

import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.dom.ast.tag.ITaggable;
import org.eclipse.cdt.core.dom.ast.tag.IWritableTag;
import org.eclipse.cdt.internal.core.pdom.PDOM;

public class PDOMTaggable implements ITaggable
{
	private final PDOM pdom;
	private final long record;

	public PDOMTaggable( PDOM pdom, long record )
	{
		this.pdom = pdom;
		this.record = record;
	}

	@Override
	public IWritableTag createTag( String id, int len )
	{
		return PDOMTagIndex.createTag( pdom, record, id, len );
	}

	@Override
	public ITag getTag( String id )
	{
		return PDOMTagIndex.getTag( pdom, record,  id );
	}
}
