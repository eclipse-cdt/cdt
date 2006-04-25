/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * A String class for strings stored in the database. The idea
 * is to minimize how often we extract the string.
 * 
 * @author Doug Schaefer
 */
public class DBString {

	private final Database db;
	private final int offset;
	
	DBString(Database db, int offset) {
		this.db = db;
		this.offset = offset;
	}
	
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		try {
			if (obj instanceof DBString) {
				DBString string = (DBString)obj;
				if (db == string.db && offset == string.offset)
					return true;
				
				Chunk chunk1 = db.getChunk(offset);
				Chunk chunk2 = string.db.getChunk(string.offset);
				
				int n1 = chunk1.getChar(offset);
				int n2 = chunk2.getChar(string.offset);
				if (n1 != n2)
					return false;
				
				for (int i = 0; i < n1; ++i) {
					int coffset1 = offset + 2 + i * 2;
					int coffset2 = string.offset + 2 + i * 2;
					if (chunk1.getChar(coffset1) != chunk2.getChar(coffset2))
						return false;
				}
				return true;
			} else if (obj instanceof String) {
				String string = (String)obj;
				Chunk chunk = db.getChunk(offset);

				// Make sure size is the same
				int n = chunk.getChar(offset);
				if (n != string.length())
					return false;
				
				// Check each character
				for (int i = 0; i < n; ++i) {
					int coffset = offset + 2 + i * 2;
					if (chunk.getChar(coffset) != string.charAt(i))
						return false;
				}
				
				return true;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return false;
	}
	
	public int hashCode() {
		// Custom hash code function to allow DBStrings in hashmaps.
		return offset;
	}
	
	public String getString() throws CoreException {
		return new String(db.getChars(offset));
	}
}
