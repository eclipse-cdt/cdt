/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.internal.core.pdom.tag;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.tag.IWritableTag;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;

public class PDOMTag implements IWritableTag
{
	private final Database db;
	private final long record;

	private int dataLen = -1;

	private static enum Fields
	{
		Node,
		TaggerId,
		DataLen,
		Data;

		public final long offset = ordinal() * Database.PTR_SIZE;
		public static int sizeof( int datalen ) { return (int)Data.offset + datalen; }

		public long getRecPtr( Database db, long instance ) throws CoreException
		{
			return db.getRecPtr( instance + offset );
		}

		public void putRecPtr( Database db, long instance, long value ) throws CoreException
		{
			db.putRecPtr( instance + offset, value );
		}

		public void put( Database db, long instance, byte value ) throws CoreException
		{
			db.putByte( instance + offset, value );
		}

		public byte getByte( Database db, long instance ) throws CoreException
		{
			return db.getByte( instance + offset );
		}

		public void put( Database db, long instance, int value ) throws CoreException
		{
			db.putInt( instance + offset, value );
		}

		public int getInt( Database db, long instance ) throws CoreException
		{
			return db.getInt( instance + offset );
		}
	}

	public PDOMTag( Database db, long record )
	{
		this.db = db;
		this.record = record;
	}

	public PDOMTag( Database db, int datalen ) throws CoreException
	{
		this.db = db;
		this.record = db.malloc( Fields.sizeof( datalen ) );
		Fields.DataLen.put( db, record, datalen );
	}

	public long getRecord() { return record; }

	public void destroy() throws CoreException
	{
		if( db != null
		 && record != 0 )
			db.free( record );
	}

	public static class BTreeComparator implements IBTreeComparator
	{
		private final Database db;
		public BTreeComparator( Database db ) { this.db = db; }

		@Override
		public int compare(long record1, long record2) throws CoreException
		{
			if( record1 == record2 )
				return 0;

			long node1 = Fields.Node.getRecPtr( db, record1 );
			long node2 = Fields.Node.getRecPtr( db, record2 );
			if( node1 < node2 )
				return -1;
			if( node1 > node2 )
				return 1;

			long tagger1 = Fields.TaggerId.getRecPtr( db, record1 );
			long tagger2 = Fields.TaggerId.getRecPtr( db, record2 );
			if( tagger1 < tagger2 )
				return -1;
			if( tagger1 > tagger2 )
				return 1;

			return 0;
		}
	}

	public static class BTreeVisitor implements IBTreeVisitor
	{
		private final Database db;
		private final long node2;
		private final long tagger2;

		public boolean hasResult = false;
		public long tagRecord = 0;

		public BTreeVisitor( Database db, long node2, long tagger2 )
		{
			this.db = db;
			this.node2 = node2;
			this.tagger2 = tagger2;
		}

		@Override
		public int compare(long record1) throws CoreException {
			long node1 = Fields.Node.getRecPtr( db, record1 );
			if( node1 < node2 )
				return -1;
			if( node1 > node2 )
				return 1;

			long tagger1 = Fields.TaggerId.getRecPtr( db, record1 );
			if( tagger1 < tagger2 )
				return -1;
			if( tagger1 > tagger2 )
				return 1;

			return 0;
		}

		@Override
		public boolean visit(long record) throws CoreException {
			tagRecord = record;
			hasResult = true;
			return false;
		}
	}

	public void setNode( long node ) throws CoreException
	{
		Fields.Node.putRecPtr( db, record, node );
	}

	public void setTaggerId( long idRecord ) throws CoreException
	{
		Fields.TaggerId.putRecPtr( db, record, idRecord );
	}

	private int getDataLen()
	{
		if( dataLen < 0 )
			try { dataLen = Fields.DataLen.getInt( db, record ); }
			catch( CoreException e ) { CCorePlugin.log( e ); return 0; }

		return dataLen;
	}

	private boolean isInBounds( int offset )
	{
		return offset >= 0 && offset < getDataLen();
	}

	@Override
	public boolean putByte( int offset, byte buff )
	{
		if( ! isInBounds( offset ) )
			return false;

		try { Fields.Data.put( db, record + offset, buff ); return true; }
		catch( CoreException e ) { CCorePlugin.log( e ); return false; }
	}

	@Override
	public int getByte( int offset )
	{
		if( ! isInBounds( offset ) )
			return Fail;

		try { return Fields.Data.getByte( db, record + offset ); }
		catch( CoreException e ) { CCorePlugin.log( e ); return Fail; }
	}
}
