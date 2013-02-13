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
	private static class Visitor implements IBTreeVisitor
	{
		public final List<Long> records = new LinkedList<Long>();

		// Visit all nodes
		@Override public int compare(long record) throws CoreException { return 0; }

		@Override
		public boolean visit( long record ) throws CoreException
		{
			records.add( Long.valueOf( record ) );
			return true;
		}
	}

	public static interface Factory<T>
	{
		public T create( long record );
	}

	private static class BTreeIterator<T> implements Iterator<T>
	{
		private final Factory<T> factory;
		private final Iterator<Long> records;

		public BTreeIterator( Factory<T> factory, Iterable<Long> records )
		{
			this.factory = factory;
			this.records = records.iterator();
		}

		@Override public void remove() { }
		@Override public boolean hasNext() { return records.hasNext(); }
		@Override public T next() { return factory.create( records.next() ); }
	}

	private final BTree btree;
	private final Factory<T> factory;

	public BTreeIterable( BTree btree, Factory<T> factory )
	{
		this.btree = btree;
		this.factory = factory;
	}

	@Override
	public Iterator<T> iterator()
	{
		Visitor v = new Visitor();
		try { btree.accept( v ); }
		catch( CoreException e ) { CCorePlugin.log( e ); return Collections.<T>emptyList().iterator(); }
		return new BTreeIterator<T>( factory, v.records );
	}
}
