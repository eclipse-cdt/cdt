/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.internal.core.pdom.tag;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;

public class BTreeIterable<T> implements Iterable<T>
{
	public static interface Descriptor<T>
	{
		public int compare( long record ) throws CoreException;
		public T create( long record );
	}

	private final BTree btree;
	private final Descriptor<T> descriptor;

	public BTreeIterable( BTree btree, Descriptor<T> descriptor )
	{
		this.btree = btree;
		this.descriptor = descriptor;
	}

	@Override
	public Iterator<T> iterator()
	{
		Visitor v = new Visitor();
		try { btree.accept( v ); }
		catch( CoreException e ) { CCorePlugin.log( e ); return Collections.<T>emptyList().iterator(); }
		return new BTreeIterator( v.records );
	}

	private class Visitor implements IBTreeVisitor
	{
		public final List<Long> records = new LinkedList<Long>();

		@Override
		public int compare( long record ) throws CoreException
		{
			return BTreeIterable.this.descriptor.compare( record );
		}

		@Override
		public boolean visit( long record ) throws CoreException
		{
			records.add( Long.valueOf( record ) );
			return true;
		}
	}

	private class BTreeIterator implements Iterator<T>
	{
		private final Iterator<Long> records;

		public BTreeIterator( Iterable<Long> records )
		{
			this.records = records.iterator();
		}

		@Override public void remove() { }
		@Override public boolean hasNext() { return records.hasNext(); }
		@Override public T next() { return BTreeIterable.this.descriptor.create( records.next() ); }
	}
}
