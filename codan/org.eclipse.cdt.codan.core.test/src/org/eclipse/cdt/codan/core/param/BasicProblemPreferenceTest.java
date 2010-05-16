/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor.PreferenceType;
import org.junit.Test;

/**
 * Test for BasicProblemPreference
 */
public class BasicProblemPreferenceTest extends TestCase {
	private static final String TEST_STR = "aaa"; //$NON-NLS-1$
	BasicProblemPreference pref;
	String key = "xxx"; //$NON-NLS-1$

	@Override
	protected void setUp() throws Exception {
		pref = new BasicProblemPreference(key, "My Value"); //$NON-NLS-1$
	}

	@Test
	public void testIntegerExportValue() {
		pref.setType(PreferenceType.TYPE_INTEGER);
		pref.setValue(22);
		String value = pref.exportValue();
		assertEquals(String.valueOf(22), value);
	}

	@Test
	public void testIntegerImportValue() {
		pref.setType(PreferenceType.TYPE_INTEGER);
		pref.importValue("22"); //$NON-NLS-1$
		assertEquals(22, pref.getValue());
	}

	@Test
	public void testStringExportValue() {
		pref.setType(PreferenceType.TYPE_STRING);
		pref.setValue(TEST_STR);
		String value = pref.exportValue();
		assertEquals(TEST_STR, value);
	}

	@Test
	public void testStringImportValue() {
		pref.setType(PreferenceType.TYPE_STRING);
		pref.importValue(TEST_STR);
		assertEquals(TEST_STR, pref.getValue());
	}

	@Test
	public void testBooleanImportValue() {
		pref.setType(PreferenceType.TYPE_BOOLEAN);
		pref.setValue(Boolean.TRUE);
		String value = pref.exportValue();
		assertEquals("true", value); //$NON-NLS-1$
		pref.importValue(TEST_STR);
		assertEquals(Boolean.FALSE, pref.getValue());
	}

	@Test
	public void testFileImportValue() {
		pref.setType(PreferenceType.TYPE_FILE);
		File file = new File("file.c"); //$NON-NLS-1$
		pref.setValue(file);
		String value = pref.exportValue();
		assertEquals(file.getName(), value);
		pref.importValue(file.getName());
		assertEquals(file, pref.getValue());
	}

	public void testBadKey() {
		try {
			pref.setKey(null);
			fail("Should be exception"); //$NON-NLS-1$
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	public void testBadType() {
		try {
			pref.setType(null);
			fail("Should be exception"); //$NON-NLS-1$
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testStringImportValueNum() {
		pref.setType(PreferenceType.TYPE_STRING);
		pref.importValue("42.5");
		assertEquals("42.5", pref.getValue());
	}

	/**
	 * @param str
	 */
	protected void checkImportExport(String str) {
		pref.setType(PreferenceType.TYPE_STRING);
		pref.setValue(str);
		pref.importValue(pref.exportValue());
		assertEquals(str, pref.getValue());
	}

	@Test
	public void testStringExportSpecial() {
		checkImportExport("a=b");
		checkImportExport("\"");
		checkImportExport("33");
		checkImportExport("22.4");
		checkImportExport("a,b");
		checkImportExport("{a+b}");
		checkImportExport("\b");
	}
	//	public void testEscape() {
	//		String str = "\"a\"";
	//		String res = pref.escape(str);
	//		assertEquals("\"\\\"a\\\"\"", res);
	//	}
	//
	//	public void testUnEscape() {
	//		String res = "\"a\"";
	//		String str = "\"\\\"a\\\"\"";
	//		assertEquals(res, pref.unescape(str));
	//	}
}
