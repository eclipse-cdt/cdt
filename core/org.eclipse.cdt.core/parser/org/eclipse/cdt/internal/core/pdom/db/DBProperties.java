/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;

/**
 * DBProperties is a bare-bones implementation of a String->String mapping. It is neither
 * a Map or a Properties subclass, because of their more general applications.
 */
public class DBProperties {
	static final int PROP_INDEX = 0;
	static final int RECORD_SIZE = 4;
	
	protected BTree index;
	protected Database db;
	protected long record;
	
	/**
	 * Allocate storage for a new DBProperties record in the specified database
	 * @param db
	 * @throws CoreException
	 */
	public DBProperties(Database db) throws CoreException {
		this.record= db.malloc(RECORD_SIZE);
		this.index= new BTree(db, record + PROP_INDEX, DBProperty.getComparator(db));
		this.db= db;
	}
	
	/**
	 * Creates an object for accessing an existing DBProperties record at the specified location of the specified database
	 * @param db
	 * @param record
	 * @throws CoreException
	 */
	public DBProperties(Database db, long record) throws CoreException {
		this.record= record;
		this.index= new BTree(db, record + PROP_INDEX, DBProperty.getComparator(db));
		this.db= db;
	}
	
	/**
	 * Read the named property from this properties storage
	 * @param key a case-sensitive identifier for a property, or null
	 * @return the value associated with the key, or null if either no such property is set, or the specified key was null
	 * @throws CoreException
	 */
	public String getProperty(String key) throws CoreException {
		if(key!=null) {
			DBProperty existing= DBProperty.search(db, index, key);
			if(existing!=null) {
				return existing.getValue().getString();
			}
		}
		return null;
	}
	
	/**
	 * Read the named property from this properties storage, returning the default value if there is no such property
	 * @param key a case-sensitive identifier for a property, or null
	 * @param defaultValue a value to return in case the specified key was null
	 * @return the value associated with the key, or the specified default value if either no such property is set, or
	 * the specified key was null
	 * @throws CoreException
	 */
	public String getProperty(String key, String defaultValue) throws CoreException {
		String val= getProperty(key);
		return (val == null) ? defaultValue : val;
	}

	/**
	 * Returns the Set of property names stored in this object
	 * @return the Set of property names stored in this object
	 * @throws CoreException
	 */
	public Set<String> getKeySet() throws CoreException {
		return DBProperty.getKeySet(db, index);
	}

	/**
	 * Write the key, value mapping to the properties. If a mapping for the
	 * same key already exists, it is overwritten.
	 * @param key a non-null property name
	 * @param value a value to associate with the key. may not be null.
	 * @throws CoreException
	 * @throws NullPointerException if key is null
	 */
	public void setProperty(String key, String value) throws CoreException {
		removeProperty(key);
		DBProperty newProperty= new DBProperty(db, key, value);
		index.insert(newProperty.getRecord());
	}

	/**
	 * Deletes a property from this DBProperties object
	 * @param key
	 * @return whether a property with matching key existed and was removed, or false if the key was null
	 * @throws CoreException
	 */
	public boolean removeProperty(String key) throws CoreException {
		if(key!=null) {
			DBProperty existing= DBProperty.search(db, index, key);
			if(existing != null) {
				index.delete(existing.getRecord());
				existing.delete();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Deletes all properties, does not delete the record associated with the object itself - that is
	 * it can be re-populated.
	 * @throws CoreException
	 */
	public void clear() throws CoreException {
		index.accept(new IBTreeVisitor(){
			@Override
			public int compare(long record) throws CoreException {
				return 0;
			}
			@Override
			public boolean visit(long record) throws CoreException {
				new DBProperty(db, record).delete();
				return false; // there should never be duplicates
			}
		});
	}
	
	/**
	 * Deletes all properties stored in this object and the record associated with this object itself.
	 * <br><br>
	 * <b>The behaviour of objects of this class after calling this method is undefined</b>
	 * @throws CoreException
	 */
	public void delete() throws CoreException {
		clear();
		db.free(record);
	}

	public long getRecord() {
		return record;
	}
	
	private static class DBProperty {
		static final int KEY = 0;
		static final int VALUE = 4;
		@SuppressWarnings("hiding")
		static final int RECORD_SIZE = 8;
		
		Database db;
		long record;
		
		public long getRecord() {
			return record;
		}
		
		/**
		 * Allocates and initializes a record in the specified database for a DBProperty record
		 * @param db
		 * @param key a non-null property name
		 * @param value a non-null property value
		 * @throws CoreException
		 */
		DBProperty(Database db, String key, String value) throws CoreException {
			assert key!=null;
			assert value!=null;
			IString dbkey= db.newString(key);
			IString dbvalue= db.newString(value);
			this.record= db.malloc(RECORD_SIZE);
			db.putRecPtr(record + KEY, dbkey.getRecord());
			db.putRecPtr(record + VALUE, dbvalue.getRecord());
			this.db= db;
		}
		
		/**
		 * Returns an object for accessing an existing DBProperty record at the specified location in the
		 * specified database
		 * @param db
		 * @param record
		 */
		DBProperty(Database db, long record) {
			this.record= record;
			this.db= db;
		}
		
		public IString getKey() throws CoreException {
			return db.getString(db.getRecPtr(record + KEY));
		}
		
		public IString getValue() throws CoreException {
			return db.getString(db.getRecPtr(record + VALUE));
		}
		
		public static IBTreeComparator getComparator(final Database db) {
			return new IBTreeComparator() {
				@Override
				public int compare(long record1, long record2) throws CoreException {
					IString left= db.getString(db.getRecPtr(record1 + KEY));
					IString right= db.getString(db.getRecPtr(record2 + KEY));
					return left.compare(right, true);
				}
			};
		}
		
		public static DBProperty search(final Database db, final BTree index, final String key) throws CoreException {
			final DBProperty[] result= new DBProperty[1];
			index.accept(new IBTreeVisitor(){
				@Override
				public int compare(long record) throws CoreException {
					return db.getString(db.getRecPtr(record + KEY)).compare(key, true);
				}
				@Override
				public boolean visit(long record) throws CoreException {
					result[0] = new DBProperty(db, record);
					return false; // there should never be duplicates
				}
			});
			return result[0];
		}
		
		public static Set<String> getKeySet(final Database db, final BTree index) throws CoreException {
			final Set<String> result= new HashSet<String>();
			index.accept(new IBTreeVisitor(){
				@Override
				public int compare(long record) throws CoreException {
					return 0;
				}
				@Override
				public boolean visit(long record) throws CoreException {
					result.add(new DBProperty(db, record).getKey().getString());
					return true; // there should never be duplicates
				}
			});
			return result;
		}
		
		public void delete() throws CoreException {
			db.getString(db.getRecPtr(record + KEY)).delete();
			db.getString(db.getRecPtr(record + VALUE)).delete();
			db.free(record);
		}
	}
}
