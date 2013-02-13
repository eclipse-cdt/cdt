/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.internal.core.pdom.tag;

import java.util.Collections;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.dom.ast.tag.IWritableTag;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.PDOMStringSet;
import org.eclipse.core.runtime.CoreException;

/**
 * Not thread-safe.
 */
public class PDOMTagIndex {

	private final Database db;
	private final long ptr;
	private long record;

	private static enum Fields
	{
		TaggerIds,
		Tags,
		_last;

		public final long offset = ordinal() * Database.PTR_SIZE;
		public static int sizeof = _last.ordinal() * Database.PTR_SIZE;
	}

	private PDOMStringSet taggerIds;
	private BTree tags;

	public PDOMTagIndex( Database db, long ptr ) throws CoreException
	{
		this.db = db;
		this.ptr = ptr;
		this.record = db.getRecPtr( ptr );
	}

	public void clear()
	{
		tags = null;
		taggerIds = null;
	}

	private long getFieldAddress( Fields field ) throws CoreException
	{
		if( record == 0 )
		{
			record = db.malloc( Fields.sizeof );
			db.putRecPtr( ptr, record );
		}

		return record + field.offset;
	}

	private PDOMStringSet getTaggerIds() throws CoreException
	{
		if( taggerIds == null )
			taggerIds = new PDOMStringSet( db, getFieldAddress( Fields.TaggerIds ) );
		return taggerIds;
	}

	private BTree getTagsBTree() throws CoreException
	{
		if( tags == null )
			tags = new BTree(db, getFieldAddress( Fields.Tags ), new PDOMTag.BTreeComparator(db));
		return tags;
	}

	/**
	 * Return the record storing the specified tagger id.  Create a new record if needed.
	 */
	private long getIdRecord( String taggerId )
	{
		assert taggerId != null;
		assert ! taggerId.isEmpty();

		if( db == null
		 || taggerId == null
		 || taggerId.isEmpty())
			return 0L;

		try
		{
			long record = getTaggerIds().find( taggerId );
			if( record == 0 )
				record = getTaggerIds().add( taggerId );
			return record;
		}
		catch( CoreException e )
		{
			CCorePlugin.log( e );
		}

		return 0L;
	}

	public IWritableTag createTag( long record, String id, int len )
	{
		if( db == null )
			return null;

		long idRecord = getIdRecord( id );
		if( idRecord == 0L )
			return null;

		try
		{
			PDOMTag tag = new PDOMTag( db, len );
			tag.setNode( record );
			tag.setTaggerId( idRecord );

			// return the tag if it was properly inserted
			long inserted = getTagsBTree().insert( tag.getRecord() );
			if( inserted == tag.getRecord() )
				return tag;

			// TODO check that the existing record has the same length

			// otherwise destroy this provisional one and return the tag that was actually inserted
			// TODO figure out what this case means
			tag.destroy();
			return inserted == 0 ? null : new PDOMTag( db, inserted );
		}
		catch( CoreException e )
		{
			CCorePlugin.log( e );
		}

		return null;
	}

	public ITag getTag( long record, String id )
	{
		if( db == null )
			return null;

		long idRecord = getIdRecord( id );
		if( idRecord == 0L )
			return null;

		PDOMTag.BTreeVisitor v = new PDOMTag.BTreeVisitor( db, record, idRecord );
		try { getTagsBTree().accept(v); }
		catch( CoreException e ) { CCorePlugin.log(e); }

		return v.hasResult ? new PDOMTag( db, v.tagRecord ) : null;
	}

	public Iterable<ITag> getTags()
	{
		BTree btree = null;
		try { btree = getTagsBTree(); }
		catch( CoreException e ) { CCorePlugin.log( e ); return Collections.emptyList(); }

		return
			new BTreeIterable<ITag>(
					btree,
					new BTreeIterable.Factory<ITag>()
					{
						@Override public ITag create( long record ) { return new PDOMTag( db, record ); }
					} );
	}

	private static PDOMTagIndex getTagIndex( PDOM pdom )
	{
		if( pdom == null )
			return null;

		try { return pdom.getTagIndex(); }
		catch( CoreException e ) { CCorePlugin.log(e); }
		return null;
	}

	// common implementations
	public static IWritableTag createTag( PDOM pdom, long record, String id, int len )
	{
		PDOMTagIndex index = getTagIndex( pdom );
		if( index == null )
			return null;

		return index.createTag( record, id, len );
	}

	public static ITag getTag( PDOM pdom, long record, String id )
	{
		PDOMTagIndex index = getTagIndex( pdom );
		if( index == null )
			return null;

		return index.getTag( record, id );
	}

	public static Iterable<ITag> getTags( PDOM pdom )
	{
		PDOMTagIndex index = getTagIndex( pdom );
		if( index == null )
			return Collections.emptyList();

		return index.getTags();
	}
}
