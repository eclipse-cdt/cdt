/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.tests;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.qt.core.index.QMakeInfo;
import org.eclipse.cdt.internal.qt.core.index.QMakeParser;
import org.eclipse.cdt.internal.qt.core.index.QMakeVersion;

public class SimpleTests extends TestCase {

	public void testQMakeVersion() throws Exception {
		// Make sure null is returned for invalid version strings.
		assertNull(QMakeVersion.create(null));
		assertNull(QMakeVersion.create(""));
		assertNull(QMakeVersion.create("30"));
		assertNull(QMakeVersion.create("2.a"));

		// Check an expected value, avoid 0 to confirm that all digits are propertly parsed.
		QMakeVersion two_dot_one = QMakeVersion.create("2.1");
		assertNotNull(two_dot_one);
		assertEquals(2, two_dot_one.getMajor());
		assertEquals(1, two_dot_one.getMinor());

		// Check the common expected value, make sure leading/trailing whitespace is ignored
		QMakeVersion three_dot_zero = QMakeVersion.create("	3.0 ");
		assertNotNull(three_dot_zero);
		assertEquals(3, three_dot_zero.getMajor());
		assertEquals(0, three_dot_zero.getMinor());
	}

	public void testQMakeInfo() throws Exception {
		StringReader content = new StringReader("A = \\\\\\\"\nB = A\\n\\tB\nC = \"A \\\" B\" \"A \\\" B\"");
		BufferedReader reader = new BufferedReader(content);

		Map<String, String> result = QMakeParser.parse(QMakeInfo.PATTERN_EVAL_LINE, reader);
		String A = QMakeParser.qmake3DecodeValue(result.get("A"));
		assertNotNull(A);
		assertEquals("\\\"", A);

		String B = QMakeParser.qmake3DecodeValue(result.get("B"));
		assertNotNull(B);
		assertEquals("A\n\tB", B);

		List<String> C = QMakeParser.qmake3DecodeValueList(result, "C");
		assertNotNull(C);
		assertEquals(2, C.size());
		assertEquals("A \" B", C.get(0));
		assertEquals("A \" B", C.get(1));

		List<String> D = QMakeParser.qmake3DecodeValueList(result, "D");
		assertNotNull(D);
		assertEquals(0, D.size());
	}

}
