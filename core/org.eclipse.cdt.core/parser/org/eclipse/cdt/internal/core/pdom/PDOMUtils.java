/**
 * 
 */
package org.eclipse.cdt.internal.core.pdom;

import java.io.IOException;

import org.eclipse.cdt.internal.core.pdom.db.Chunk;
import org.eclipse.cdt.internal.core.pdom.db.Database;

/**
 * @author dschaefer
 *
 */
public class PDOMUtils {

	public static int stringCompare(Database db, int record1, int record2) throws IOException {
		Chunk chunk1 = db.getChunk(record1);
		Chunk chunk2 = db.getChunk(record2);
		
		int i1 = record1;
		int i2 = record2;
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
	
	public static int stringCompare(Database db, int record1, char[] record2) throws IOException {
		Chunk chunk1 = db.getChunk(record1);
		
		int i1 = record1;
		int i2 = 0;
		char c1 = chunk1.getChar(i1);
		char c2 = i2 < record2.length ? record2[i2] : 0;
		
		while (c1 != 0 && c2 != 0) {
			if (c1 < c2)
				return -1;
			if (c1 > c2)
				return 1;
			
			i1 += 2;
			++i2;
			c1 = chunk1.getChar(i1);
			c2 = i2 < record2.length ? record2[i2] : 0;
		}

		if (c1 == c2)
			return 0;
		else if (c1 == 0)
			return -1;
		else
			return 1;

	}
}
