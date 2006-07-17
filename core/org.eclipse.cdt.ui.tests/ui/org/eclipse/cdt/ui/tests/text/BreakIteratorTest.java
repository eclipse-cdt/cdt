/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import com.ibm.icu.text.BreakIterator;

import junit.framework.TestCase;


public class BreakIteratorTest extends TestCase {

	protected BreakIterator fBreakIterator;

	public void assertNextPositions(CharSequence ci, int position) {
		assertNextPositions(ci, new int[] {position});
	}

	public void assertNextPositions(CharSequence ci, int[] positions) {
		fBreakIterator.setText(ci.toString());
		
		// test next()
		for (int i = 0; i < positions.length; i++) {
			int pos= fBreakIterator.next(); 
			assertEquals(positions[i], pos);
		}
		
		// test following()
		int idx= 0;
		for (int i = 0; i < positions.length; i++) {
			int position= positions[i];
			while (idx < position) {
				if (!illegalPos(ci, idx))
					assertEquals(position, fBreakIterator.following(idx));
				idx++;
			}
		}
		
	}

	/**
	 * Check if we are in a multibyte delimiter
	 * @param idx
	 * @return
	 */
	private boolean illegalPos(CharSequence seq, int idx) {
		String DELIMS= "\n\r";
		if (idx == 0 || idx == seq.length())
			return false;
		char one= seq.charAt(idx - 1);
		char two= seq.charAt(idx);
		return one != two && DELIMS.indexOf(one) != -1 && DELIMS.indexOf(two) != -1;
	}

	public void assertPreviousPositions(CharSequence ci, int position) {
		assertPreviousPositions(ci, new int[] {position});
	}

	public void assertPreviousPositions(CharSequence ci, int[] positions) {
		fBreakIterator.setText(ci.toString());
		fBreakIterator.last();
		
		for (int i = positions.length - 1; i >= 0; i--) {
			int pos= fBreakIterator.previous(); 
			assertEquals(positions[i], pos);
		}
	
		// test preceding()
		int idx= ci.length();
		for (int i = positions.length - 1; i >= 0; i--) {
			int position= positions[i];
			while (idx > position) {
				if (!illegalPos(ci, idx))
					assertEquals(position, fBreakIterator.preceding(idx));
				idx--;
			}
		}
	}

}
