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
public abstract class StringVisitor implements IBTreeVisitor {

	public final Database db;
	private final int offset;
	private final String key;
	
	public StringVisitor(Database db, int offset, String key) {
		this.db = db;
		this.offset = offset;
		this.key = key;
	}

	public int compare(int record) throws CoreException {
		Chunk chunk = db.getChunk(record);
		int i1 = record + offset;
		int i2 = 0;
		int n2 = key.length();
		char c1 = chunk.getChar(i1);
		char c2 = i2 < n2 ? key.charAt(i2) : 0;
		
		while (c1 != 0 && c2 != 0) {
			if (c1 < c2)
				return -1;
			if (c1 > c2)
				return 1;
			
			i1 += 2;
			i2 += 1;
			c1 = chunk.getChar(i1);
			c2 = i2 < n2 ? key.charAt(i2) : 0;
		}

		if (c1 == c2)
			return 0;
		else if (c1 == 0)
			return -1;
		else
			return 1;
	}

}
