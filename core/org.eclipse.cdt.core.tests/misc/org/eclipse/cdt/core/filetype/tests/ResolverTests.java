/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.filetype.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.core.filetype.ICFileTypeConstants;
import org.eclipse.cdt.core.filetype.ICFileTypeResolver;
import org.eclipse.cdt.core.filetype.ICLanguage;
import org.eclipse.cdt.core.internal.filetype.CFileType;
import org.eclipse.cdt.core.internal.filetype.CFileTypeAssociation;
import org.eclipse.cdt.core.internal.filetype.CLanguage;

public class ResolverTests extends TestCase {

	private ICFileTypeResolver resolver;
	private static final String PLUGIN_ID = "org.eclipse.cdt.core.filetype.tests";
	private static final String LANG_TEST = PLUGIN_ID + ".test";
	private static final String FT_TEST_HEADER = LANG_TEST + ".header";
	private static final String FT_TEST_SOURCE = LANG_TEST + ".source";
	private static final String FT_TEST_WHASAT = LANG_TEST + ".unknown";
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ResolverTests.class.getName());
		suite.addTest(new ResolverTests("testInternalCtors"));
		suite.addTest(new ResolverTests("testFileTypeResolution"));
		suite.addTest(new ResolverTests("testGetLanguages"));
		suite.addTest(new ResolverTests("testGetTypes"));
		suite.addTest(new ResolverTests("testGetFileTypeAssociations"));
		suite.addTest(new ResolverTests("testAdd"));
		suite.addTest(new ResolverTests("testRemove"));
		return suite;
	}
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		resolver = CCorePlugin.getDefault().getFileTypeResolver();
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		resolver = null;
		super.tearDown();
	}

	/**
	 * Constructor for ResolverTests.
	 * @param name
	 */
	public ResolverTests(String name) {
		super(name);
	}

	public final void testInternalCtors() {
		ICLanguage 				lang	= null;
		ICFileType 				type	= null;
		ICFileTypeAssociation	assoc	= null;

		// Language
		
		try {
			lang = new CLanguage(LANG_TEST, null);
		} catch (IllegalArgumentException e) {
		}
		assertNull(lang);
		
		try {
			lang = new CLanguage(LANG_TEST, "");
		} catch (IllegalArgumentException e) {
		}
		assertNull(lang);

		try {
			lang = new CLanguage(null, "L");
		} catch (IllegalArgumentException e) {
		}
		assertNull(lang);


		try {
			lang = new CLanguage("", "L");
		} catch (IllegalArgumentException e) {
		}
		assertNull(lang);

		lang = new CLanguage(LANG_TEST, "Test Language");
		assertNotNull(lang);

		// File type
		// str id, cls lang, str name, int type

		try {
			type = new CFileType(FT_TEST_HEADER, lang, "T", -1);
		} catch (IllegalArgumentException e) {
		}
		assertNull(type);

		try {
			type = new CFileType(FT_TEST_HEADER, lang, "T", 0x04091998);
		} catch (IllegalArgumentException e) {
		}
		assertNull(type);

		try {
			type = new CFileType(FT_TEST_HEADER, lang, null, ICFileType.TYPE_HEADER);
		} catch (IllegalArgumentException e) {
		}
		assertNull(type);

		try {
			type = new CFileType(FT_TEST_HEADER, lang, "", ICFileType.TYPE_HEADER);
		} catch (IllegalArgumentException e) {
		}
		assertNull(type);

		try {
			type = new CFileType(FT_TEST_HEADER, null, "T", ICFileType.TYPE_HEADER);
		} catch (IllegalArgumentException e) {
		}
		assertNull(type);

		try {
			type = new CFileType(null, lang, "T", ICFileType.TYPE_HEADER);
		} catch (IllegalArgumentException e) {
		}
		assertNull(type);

		try {
			type = new CFileType("", lang, "T", ICFileType.TYPE_HEADER);
		} catch (IllegalArgumentException e) {
		}
		assertNull(type);

		type = new CFileType(FT_TEST_HEADER, lang, "T", ICFileType.TYPE_HEADER);
		assertNotNull(type);

		// Association

		try {
			assoc = new CFileTypeAssociation("*.xyz", null);
		} catch (IllegalArgumentException e) {
		}
		assertNull(assoc);
		
		try {
			assoc = new CFileTypeAssociation(null, type);
		} catch (IllegalArgumentException e) {
		}
		assertNull(assoc);

		try {
			assoc = new CFileTypeAssociation("", type);
		} catch (IllegalArgumentException e) {
		}
		assertNull(assoc);

		assoc = new CFileTypeAssociation("*.xyz", type);
		assertNotNull(assoc);
	}
	
	private void doTestFileTypeResolution(String fileName, String expectedTypeId) { 
		ICFileType typeByName = resolver.getFileType(fileName);

		assertNotNull(typeByName);
		assertEquals(expectedTypeId, typeByName.getId());
	
		ICFileType typeById = resolver.getFileTypeById(typeByName.getId());
		
		assertNotNull(typeById);
		assertEquals(typeByName, typeById);
		
		ICLanguage languageById = resolver.getLanguageById(typeByName.getLanguage().getId());

		assertNotNull(languageById);
		assertEquals(typeByName.getLanguage().getId(), languageById.getId());
	}
	
	public final void testFileTypeResolution() {
		// - Null string, Empty string, Strings w/spaces
		doTestFileTypeResolution(null, ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(" ", ICFileTypeConstants.FT_UNKNOWN);

		// Odd filenames
		doTestFileTypeResolution(".", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(".c.", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(".cpp.", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("file.c.", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("file.cpp.", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("file.c.input", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("file.cpp.input", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("c", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("cpp", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("numerical", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("some/path/file.c", ICFileTypeConstants.FT_C_SOURCE);
		doTestFileTypeResolution("some/path/file.cpp", ICFileTypeConstants.FT_CXX_SOURCE);
		
		// C source/header
		doTestFileTypeResolution("file.c", ICFileTypeConstants.FT_C_SOURCE);
		doTestFileTypeResolution("file.h", ICFileTypeConstants.FT_C_HEADER);
		doTestFileTypeResolution("some.file.c", ICFileTypeConstants.FT_C_SOURCE);
		doTestFileTypeResolution("some.file.h", ICFileTypeConstants.FT_C_HEADER);
		
		// C++ source/header
		doTestFileTypeResolution("file.cpp", ICFileTypeConstants.FT_CXX_SOURCE);
		doTestFileTypeResolution("file.cxx", ICFileTypeConstants.FT_CXX_SOURCE);
		doTestFileTypeResolution("file.cc", ICFileTypeConstants.FT_CXX_SOURCE);
		doTestFileTypeResolution("file.C", ICFileTypeConstants.FT_CXX_SOURCE);
		doTestFileTypeResolution("file.hpp", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("file.hxx", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("file.hh", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("file.H", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("some.file.cpp", ICFileTypeConstants.FT_CXX_SOURCE);
		doTestFileTypeResolution("some.file.hxx", ICFileTypeConstants.FT_CXX_HEADER);

		// Assembly
		doTestFileTypeResolution("file.asm", ICFileTypeConstants.FT_ASM_SOURCE);
		doTestFileTypeResolution("file.s", ICFileTypeConstants.FT_ASM_SOURCE);
		doTestFileTypeResolution("file.S", ICFileTypeConstants.FT_ASM_SOURCE);
		
		// Std C++ library
		doTestFileTypeResolution("algorithm", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("bitset", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("deque", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("exception", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("fstream", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("functional", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("iomanip", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("ios", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("iosfwd", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("iostream", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("istream", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("iterator", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("limits", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("list", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("locale", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("map", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("memory", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("new", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("numeric", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("ostream", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("queue", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("set", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("sstream", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("stack", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("stdexcept", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("streambuf", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("string", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("typeinfo", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("utility", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("valarray", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution("vector", ICFileTypeConstants.FT_CXX_HEADER);
		
		// Failure cases
		doTestFileTypeResolution("file.txt", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("file.doc", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("files", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("FILES", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("stream", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("streambu", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("streambuff", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution("sstreams", ICFileTypeConstants.FT_UNKNOWN);
	}


	public final void testGetLanguages() {
		ICLanguage[] languages = resolver.getLanguages();

		assertNotNull(languages);

		for (int i = 0; i < languages.length; i++) {
			ICLanguage lang = resolver.getLanguageById(languages[i].getId());
			assertNotNull(lang);
			assertEquals(languages[i], lang);
		}
	}

	public final void testGetTypes() {
		ICFileType[] types = resolver.getFileTypes();

		assertNotNull(types);

		for (int i = 0; i < types.length; i++) {
			ICFileType type = resolver.getFileTypeById(types[i].getId());
			assertNotNull(type);
			assertEquals(types[i], type);
		}
	}
	
	public final void testGetFileTypeAssociations() {
		ICFileTypeAssociation[] assocs = resolver.getFileTypeAssociations();

		assertNotNull(assocs);

		for (int i = 0; i < assocs.length; i++) {
			// Check the pattern

			String pattern = assocs[i].getPattern();

			assertNotNull(pattern);
			assertTrue(pattern.length() > 0);

			// Check the file type
			
			ICFileType type = assocs[i].getType();
			
			assertNotNull(type);

			ICFileType typeById = resolver.getFileTypeById(type.getId());
			
			assertNotNull(typeById);
			assertEquals(type, typeById);

			// Check the type's language
			
			ICLanguage langIn = type.getLanguage();
			
			assertNotNull(langIn);
			
			String langId = langIn.getId();
			
			assertNotNull(langId);
			assertTrue(langId.length() > 0);
			
			ICLanguage langOut = resolver.getLanguageById(langId);

			assertNotNull(langOut);
			assertEquals(langIn, langOut);
		}
	}

	public final void testAdd() {
		boolean result = false;
		
		// Languages
		
		ICLanguage langIn = new CLanguage(LANG_TEST, "Test Language");
		
		result = resolver.removeLanguage(langIn);
		assertFalse(result);

		result = resolver.addLanguage(langIn);
		assertTrue(result);
		
		ICLanguage langOut = resolver.getLanguageById(LANG_TEST);
		assertNotNull(langOut);
		assertEquals(langIn, langOut);

		// File types

		ICFileType th = new CFileType(FT_TEST_HEADER, langIn, "Test Language Header", ICFileType.TYPE_HEADER);
		ICFileType ts = new CFileType(FT_TEST_SOURCE, langIn, "Test Language Source", ICFileType.TYPE_SOURCE);
		ICFileType tu = new CFileType(FT_TEST_WHASAT, langIn, "Test Language Unknown", ICFileType.TYPE_UNKNOWN);
		
		// -- header
		
		result = resolver.removeFileType(th);
		assertFalse(result);

		result = resolver.addFileType(th);
		assertTrue(result);

		ICFileType thOut = resolver.getFileTypeById(FT_TEST_HEADER);
		assertNotNull(thOut);
		assertEquals(th, thOut);

		// -- source

		result = resolver.removeFileType(ts);
		assertFalse(result);

		result = resolver.addFileType(ts);
		assertTrue(result);

		ICFileType tsOut = resolver.getFileTypeById(FT_TEST_SOURCE);
		assertNotNull(tsOut);
		assertEquals(ts, tsOut);

		// -- unknown

		result = resolver.removeFileType(tu);
		assertFalse(result);

		result = resolver.addFileType(tu);
		assertTrue(result);

		ICFileType tuOut = resolver.getFileTypeById(FT_TEST_WHASAT);
		assertNotNull(tuOut);
		assertEquals(tu, tuOut);

		// File type associations

		ICFileTypeAssociation tha = new CFileTypeAssociation("*.aest", th);
		ICFileTypeAssociation tsa = new CFileTypeAssociation("*.test", th);
		ICFileTypeAssociation tua = new CFileTypeAssociation("*.zest", th);

		// -- header

		result = resolver.removeFileTypeAssociation(tha);
		assertFalse(result);

		result = resolver.addFileTypeAssociation(tha);
		assertTrue(result);

		ICFileType thaOut = resolver.getFileType("file.aest");
		assertNotNull(thaOut);
		assertEquals(tha.getType(), thaOut);
		
		// -- source

		result = resolver.removeFileTypeAssociation(tsa);
		assertFalse(result);

		result = resolver.addFileTypeAssociation(tsa);
		assertTrue(result);

		ICFileType tsaOut = resolver.getFileType("file.test");
		assertNotNull(tsaOut);
		assertEquals(tsa.getType(), tsaOut);


		// -- unknown

		result = resolver.removeFileTypeAssociation(tua);
		assertFalse(result);

		result = resolver.addFileTypeAssociation(tua);
		assertTrue(result);

		ICFileType tuaOut = resolver.getFileType("file.zest");
		assertNotNull(tuaOut);
		assertEquals(tua.getType(), tuaOut);
	}

	public final void testRemove() {
		boolean result = false;
		
		// Languages
		
		ICLanguage lang = resolver.getLanguageById(LANG_TEST);
		assertNotNull(lang);
		assertEquals(LANG_TEST, lang.getId());
		
		result = resolver.removeLanguage(lang);
		assertTrue(result);

		result = resolver.removeLanguage(lang);
		assertFalse(result);

		// File types

		ICFileType ft = resolver.getFileTypeById(FT_TEST_HEADER);
		
		result = resolver.removeFileType(ft);
		assertTrue(result);

		result = resolver.removeFileType(ft);
		assertFalse(result);

		ft = resolver.getFileTypeById(FT_TEST_SOURCE);
		
		result = resolver.removeFileType(ft);
		assertTrue(result);

		result = resolver.removeFileType(ft);
		assertFalse(result);

		ft = resolver.getFileTypeById(FT_TEST_WHASAT);
		
		result = resolver.removeFileType(ft);
		assertTrue(result);

		result = resolver.removeFileType(ft);
		assertFalse(result);

		// File type associations

		ICFileTypeAssociation[] assocs = resolver.getFileTypeAssociations();
		assertNotNull(assocs);
		assertTrue(assocs.length > 3);

		for (int i = 0; i < assocs.length; i++) {
			if (assocs[i].getType().getLanguage().getId().equals(LANG_TEST)) {
				resolver.removeFileTypeAssociation(assocs[i]);
			}
		}
		
	}

}
