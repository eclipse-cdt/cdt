/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.qt.tests;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.qt.core.index.IQMakeInfo;
import org.eclipse.cdt.internal.qt.core.index.QMakeInfo;
import org.eclipse.cdt.internal.qt.core.index.QMakeParser;
import org.eclipse.cdt.internal.qt.core.index.QMakeVersion;

import junit.framework.TestCase;

public class QMakeTests extends TestCase {

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

	public void testQMake3Decoder() throws Exception {
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

	public void testQMakeInfo() throws Exception {
		StringReader qmake1Content = new StringReader(
				"QMAKE_VERSION:3.0\nQT_VERSION:5.2\nQT_INSTALL_IMPORTS:QtImports\nQT_INSTALL_QML:QtQmls\nQT_INSTALL_DOCS:QtDocs\nCustomKey:CustomValue\n");
		BufferedReader qmake1Reader = new BufferedReader(qmake1Content);
		Map<String, String> qmake1 = QMakeParser.parse(QMakeInfo.PATTERN_QUERY_LINE, qmake1Reader);

		StringReader qmake2Content = new StringReader(
				"QMAKE_INTERNAL_INCLUDED_FILES=Internal1 Internal2\nSOURCES=Source1 Source2\nHEADERS=Header1 Header2\nINCLUDEPATH=Include1 Include2\nDEFINES=Def1 Def2\nRESOURCES=Resource1 Resource2\nFORMS=Form1 Form2\nOTHER_FILES=Other1 Other2\nQML_IMPORT_PATH=CustomImport\n");
		BufferedReader qmake2Reader = new BufferedReader(qmake2Content);
		Map<String, String> qmake2 = QMakeParser.parse(QMakeInfo.PATTERN_EVAL_LINE, qmake2Reader);

		IQMakeInfo info = new QMakeInfo(true, qmake1, qmake2);

		assertNotNull(info);
		assertEquals(5, info.getQtVersion().getMajor());
		assertEquals(2, info.getQtVersion().getMinor());
		assertEquals(Arrays.asList("QtImports", "CustomImport"), info.getQtImportPath());
		assertEquals(Arrays.asList("QtQmls", "CustomImport"), info.getQtQmlPath());
		assertEquals(Arrays.asList("QtDocs"), info.getQtDocPath());
		assertEquals("CustomValue", info.getQMakeQueryMap().get("CustomKey"));

		assertEquals(Arrays.asList("Include1", "Include2"), info.getIncludePath());
		assertEquals(Arrays.asList("Def1", "Def2"), info.getDefines());
		assertEquals(Arrays.asList("Header1", "Header2"), info.getHeaderFiles());
		assertEquals(Arrays.asList("Source1", "Source2"), info.getSourceFiles());
		assertEquals(Arrays.asList("Resource1", "Resource2"), info.getResourceFiles());
		assertEquals(Arrays.asList("Form1", "Form2"), info.getFormFiles());
		assertEquals(Arrays.asList("Other1", "Other2"), info.getOtherFiles());
	}

}
