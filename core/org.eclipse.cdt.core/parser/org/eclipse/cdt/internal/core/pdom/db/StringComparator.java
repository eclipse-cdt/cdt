/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class StringComparator implements IBTreeComparator {

	protected Database db;
	protected int offset;
	
	public StringComparator(Database db, int offset) {
		this.db = db;
		this.offset = offset;
	}
	
	public int compare(int record1, int record2) throws CoreException {
		Chunk chunk1 = db.getChunk(record1);
		Chunk chunk2 = db.getChunk(record2);
		
		int i1 = record1 + offset;
		int i2 = record2 + offset;
		char c1 = chunk1.getChar(i1);
		char c2 = chunk2.getChar(i2);
		
		while (c1 != 0 && c2 != 0) {
			if (c1 < c2)
				return -1;
			if (c1 > c2)
				return 1;
			
			i1 += 2;
			i2 += 2;
			c1 = chunk1.getChar(i1);
			c2 = chunk2.getChar(i2);
		}

		if (c1 == c2)
			return 0;
		else if (c1 == 0)
			return -1;
		else
			return 1;
	}

}
