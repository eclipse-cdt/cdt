/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.filetype.tests;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.core.filetype.ICFileTypeConstants;
import org.eclipse.cdt.core.filetype.ICFileTypeResolver;
import org.eclipse.cdt.core.filetype.ICLanguage;
import org.eclipse.cdt.core.filetype.IResolverChangeListener;
import org.eclipse.cdt.core.filetype.IResolverModel;
import org.eclipse.cdt.core.filetype.ResolverChangeEvent;
import org.eclipse.cdt.core.filetype.ResolverDelta;
import org.eclipse.cdt.core.internal.filetype.CFileType;
import org.eclipse.cdt.core.internal.filetype.CFileTypeAssociation;
import org.eclipse.cdt.core.internal.filetype.CFileTypeResolver;
import org.eclipse.cdt.core.internal.filetype.CLanguage;
import org.eclipse.cdt.core.internal.filetype.ResolverModel;
import org.eclipse.cdt.testplugin.CTestPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class ResolverTests extends TestCase {

	private ICFileTypeResolver	workspaceResolver;
	private ICFileTypeResolver	projectResolver;
	private IResolverModel		model;
	private static IProject		project;
	
	private static final String PLUGIN_ID = "org.eclipse.cdt.core.filetype.tests";
	private static final String LANG_TEST = PLUGIN_ID + ".test";
	private static final String FT_TEST_HEADER = LANG_TEST + ".header";
	private static final String FT_TEST_SOURCE = LANG_TEST + ".source";
	private static final String FT_TEST_WHASAT = LANG_TEST + ".unknown";
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ResolverTests.class.getName());
		suite.addTest(new ResolverTests("testInternalCtors"));
		suite.addTest(new ResolverTests("testDefaultFileTypeResolution"));
		suite.addTest(new ResolverTests("testWorkspaceFileTypeResolution"));
		suite.addTest(new ResolverTests("testProjectFileTypeResolution"));
		suite.addTest(new ResolverTests("testGetLanguages"));
		suite.addTest(new ResolverTests("testGetTypes"));
		suite.addTest(new ResolverTests("testGetFileTypeAssociations"));
		suite.addTest(new ResolverTests("testAdd"));
		suite.addTest(new ResolverTests("testRemove"));
		suite.addTest(new ResolverTests("testChangeNotifications"));

		TestSetup wrapper = new TestSetup(suite) {
			protected void setUp() throws Exception {
				oneTimeSetUp();
			}
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};

		return wrapper;
	}

	private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = proj.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = natureId;
		description.setNatureIds(newNatures);
		proj.setDescription(description, monitor);
	}
	
	static void oneTimeSetUp() throws Exception {
		IWorkspaceRoot root = CTestPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("testResolverProject");
		if (!project.exists()) {
			project.create(null);
		} else {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		}
		if (!project.isOpen()) {
			project.open(null);
		}
		if (!project.hasNature(CProjectNature.C_NATURE_ID)) {
			addNatureToProject(project, CProjectNature.C_NATURE_ID, null);
		}
		ResolverTests.project = project;
	}

	static void oneTimeTearDown() throws Exception {
		project.delete(true, true, null);
	}
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		model = CCorePlugin.getDefault().getResolverModel();
		
		model.setResolver(null);
		model.setResolver(project, null);
		
		workspaceResolver = model.getResolver();
		projectResolver = model.getResolver(project);
		
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		workspaceResolver = null;
		workspaceResolver = null;
		projectResolver = null;
		model = null;
		super.tearDown();
	}

	/**
	 * Constructor for ResolverTests.
	 * @param name
	 */
	public ResolverTests(String name) {
		super(name);
	}

	private ICFileTypeResolver createResolver() {
		return new CFileTypeResolver();
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
	
	private void doTestFileTypeResolution(ICFileTypeResolver resolver, String fileName, String expectedTypeId) { 
		ICFileType typeByName = resolver.getFileType(fileName);

		assertNotNull(typeByName);
		assertEquals(expectedTypeId, typeByName.getId());
	
		ICFileType typeById = model.getFileTypeById(typeByName.getId());
		
		assertNotNull(typeById);
		assertEquals(typeByName, typeById);
		
		ICLanguage languageById = model.getLanguageById(typeByName.getLanguage().getId());

		assertNotNull(languageById);
		assertEquals(typeByName.getLanguage().getId(), languageById.getId());
	}
	
	public final void testDefaultFileTypeResolution() {
		// - Null string, Empty string, Strings w/spaces
		doTestFileTypeResolution(workspaceResolver, null, ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, " ", ICFileTypeConstants.FT_UNKNOWN);

		// Odd filenames
		doTestFileTypeResolution(workspaceResolver, ".", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, ".c.", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, ".cpp.", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "file.c.", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "file.cpp.", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "file.c.input", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "file.cpp.input", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "c", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "cpp", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "numerical", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "some/path/file.c", ICFileTypeConstants.FT_C_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "some/path/file.cpp", ICFileTypeConstants.FT_CXX_SOURCE);
		
		// C source/header
		doTestFileTypeResolution(workspaceResolver, "file.c", ICFileTypeConstants.FT_C_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "file.h", ICFileTypeConstants.FT_C_HEADER);
		doTestFileTypeResolution(workspaceResolver, "some.file.c", ICFileTypeConstants.FT_C_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "some.file.h", ICFileTypeConstants.FT_C_HEADER);
		
		// C++ source/header
		doTestFileTypeResolution(workspaceResolver, "file.cpp", ICFileTypeConstants.FT_CXX_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "file.cxx", ICFileTypeConstants.FT_CXX_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "file.cc", ICFileTypeConstants.FT_CXX_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "file.C", ICFileTypeConstants.FT_CXX_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "file.hpp", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "file.hxx", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "file.hh", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "file.H", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "some.file.cpp", ICFileTypeConstants.FT_CXX_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "some.file.hxx", ICFileTypeConstants.FT_CXX_HEADER);

		// Assembly
		doTestFileTypeResolution(workspaceResolver, "file.asm", ICFileTypeConstants.FT_ASM_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "file.s", ICFileTypeConstants.FT_ASM_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "file.S", ICFileTypeConstants.FT_ASM_SOURCE);
		
		// Std C++ library
		doTestFileTypeResolution(workspaceResolver, "algorithm", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "bitset", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "deque", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "exception", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "fstream", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "functional", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "iomanip", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "ios", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "iosfwd", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "iostream", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "istream", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "iterator", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "limits", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "list", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "locale", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "map", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "memory", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "new", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "numeric", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "ostream", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "queue", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "set", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "sstream", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "stack", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "stdexcept", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "streambuf", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "string", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "typeinfo", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "utility", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "valarray", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "vector", ICFileTypeConstants.FT_CXX_HEADER);
		
		// Failure cases
		doTestFileTypeResolution(workspaceResolver, "file.txt", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "file.doc", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "files", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "FILES", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "stream", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "streambu", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "streambuff", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "sstreams", ICFileTypeConstants.FT_UNKNOWN);
	}

	public final void testWorkspaceFileTypeResolution() {

		// Reset the resolver
		model.setResolver(null);
		workspaceResolver = model.getResolver(); 
		
		// Validate that we are using the default resolver set...
		doTestFileTypeResolution(workspaceResolver, "file.c", ICFileTypeConstants.FT_C_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "file.h", ICFileTypeConstants.FT_C_HEADER);
		doTestFileTypeResolution(workspaceResolver, "file.cpp", ICFileTypeConstants.FT_CXX_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "file.hpp", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "file.s", ICFileTypeConstants.FT_ASM_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "file.sam", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "file.shari", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "file.delainey", ICFileTypeConstants.FT_UNKNOWN);
		
		// Set up a new resolver just for the tests
		// This one will only recognize '*.c', '*.h', and '*.sam'
		ICFileTypeResolver resolver = createResolver();
		
		resolver.addAssociation(model.createAssocation("*.sam", model.getFileTypeById(ICFileTypeConstants.FT_C_SOURCE)));
		resolver.addAssociation(model.createAssocation("*.shari", model.getFileTypeById(ICFileTypeConstants.FT_C_HEADER)));
		resolver.addAssociation(model.createAssocation("*.delainey", model.getFileTypeById(ICFileTypeConstants.FT_ASM_SOURCE)));
		
		// Set the workspace to use the new resolver
		model.setResolver(resolver);
		workspaceResolver = model.getResolver(); 

		// Test the known types
		doTestFileTypeResolution(workspaceResolver, "file.sam", ICFileTypeConstants.FT_C_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "file.shari", ICFileTypeConstants.FT_C_HEADER);
		doTestFileTypeResolution(workspaceResolver, "file.delainey", ICFileTypeConstants.FT_ASM_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "some.file.sam", ICFileTypeConstants.FT_C_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "some.file.shari", ICFileTypeConstants.FT_C_HEADER);
		doTestFileTypeResolution(workspaceResolver, "some.file.delainey", ICFileTypeConstants.FT_ASM_SOURCE);
		
		// Failure cases
		doTestFileTypeResolution(workspaceResolver, "file.c", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "file.h", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "file.cpp", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "file.hpp", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "file.s", ICFileTypeConstants.FT_UNKNOWN);
		
		// Reset the resolver
		model.setResolver(null);
		workspaceResolver = model.getResolver(); 
		
		// Validate that we are back to using the default resolver set...
		doTestFileTypeResolution(workspaceResolver, "file.c", ICFileTypeConstants.FT_C_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "file.h", ICFileTypeConstants.FT_C_HEADER);
		doTestFileTypeResolution(workspaceResolver, "file.cpp", ICFileTypeConstants.FT_CXX_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "file.hpp", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(workspaceResolver, "file.s", ICFileTypeConstants.FT_ASM_SOURCE);
		doTestFileTypeResolution(workspaceResolver, "file.sam", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "file.shari", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(workspaceResolver, "file.delainey", ICFileTypeConstants.FT_UNKNOWN);
	}
	
	public final void testProjectFileTypeResolution() {

		// Reset the resolver(s)
		model.setResolver(null);
		workspaceResolver = model.getResolver();
		
		model.setResolver(project, null);
		projectResolver = model.getResolver(project);
		
		// Validate that we are using the default resolver set...
		doTestFileTypeResolution(projectResolver, "file.c", ICFileTypeConstants.FT_C_SOURCE);
		doTestFileTypeResolution(projectResolver, "file.cpp", ICFileTypeConstants.FT_CXX_SOURCE);
		doTestFileTypeResolution(projectResolver, "file.hpp", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(projectResolver, "file.s", ICFileTypeConstants.FT_ASM_SOURCE);
		doTestFileTypeResolution(projectResolver, "file.sam", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(projectResolver, "file.shari", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(projectResolver, "file.delainey", ICFileTypeConstants.FT_UNKNOWN);
		
		// Set up a new resolver just for the tests
		// This one will only recognize '*.c', '*.h', and '*.sam'
		ICFileTypeResolver resolver = createResolver();
		
		resolver.addAssociation(model.createAssocation("*.sam", model.getFileTypeById(ICFileTypeConstants.FT_C_SOURCE)));
		resolver.addAssociation(model.createAssocation("*.shari", model.getFileTypeById(ICFileTypeConstants.FT_C_HEADER)));
		resolver.addAssociation(model.createAssocation("*.delainey", model.getFileTypeById(ICFileTypeConstants.FT_ASM_SOURCE)));
		
		// Set the workspace to use the new resolver
		model.setResolver(project, resolver);
		projectResolver = model.getResolver(project);

		// Test the known types
		doTestFileTypeResolution(projectResolver, "file.sam", ICFileTypeConstants.FT_C_SOURCE);
		doTestFileTypeResolution(projectResolver, "file.shari", ICFileTypeConstants.FT_C_HEADER);
		doTestFileTypeResolution(projectResolver, "file.delainey", ICFileTypeConstants.FT_ASM_SOURCE);
		doTestFileTypeResolution(projectResolver, "some.file.sam", ICFileTypeConstants.FT_C_SOURCE);
		doTestFileTypeResolution(projectResolver, "some.file.shari", ICFileTypeConstants.FT_C_HEADER);
		doTestFileTypeResolution(projectResolver, "some.file.delainey", ICFileTypeConstants.FT_ASM_SOURCE);
		
		// Failure cases
		doTestFileTypeResolution(projectResolver, "file.c", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(projectResolver, "file.h", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(projectResolver, "file.cpp", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(projectResolver, "file.hpp", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(projectResolver, "file.s", ICFileTypeConstants.FT_UNKNOWN);
		
		// Reset the resolver
		model.setResolver(project, null);
		projectResolver = model.getResolver(project);
		
		// Validate that we are back to using the default resolver set...
		doTestFileTypeResolution(projectResolver, "file.c", ICFileTypeConstants.FT_C_SOURCE);
		doTestFileTypeResolution(projectResolver, "file.h", ICFileTypeConstants.FT_C_HEADER);
		doTestFileTypeResolution(projectResolver, "file.cpp", ICFileTypeConstants.FT_CXX_SOURCE);
		doTestFileTypeResolution(projectResolver, "file.hpp", ICFileTypeConstants.FT_CXX_HEADER);
		doTestFileTypeResolution(projectResolver, "file.s", ICFileTypeConstants.FT_ASM_SOURCE);
		doTestFileTypeResolution(projectResolver, "file.sam", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(projectResolver, "file.shari", ICFileTypeConstants.FT_UNKNOWN);
		doTestFileTypeResolution(projectResolver, "file.delainey", ICFileTypeConstants.FT_UNKNOWN);
	}
	
	public final void testGetLanguages() {
		ICLanguage[] languages = model.getLanguages();

		assertNotNull(languages);

		for (int i = 0; i < languages.length; i++) {
			ICLanguage lang = model.getLanguageById(languages[i].getId());
			assertNotNull(lang);
			assertEquals(languages[i], lang);
		}
	}

	public final void testGetTypes() {
		ICFileType[] types = model.getFileTypes();

		assertNotNull(types);

		for (int i = 0; i < types.length; i++) {
			ICFileType type = model.getFileTypeById(types[i].getId());
			assertNotNull(type);
			assertEquals(types[i], type);
		}
	}
	
	public final void testGetFileTypeAssociations() {
		ICFileTypeAssociation[] assocs = workspaceResolver.getFileTypeAssociations();

		assertNotNull(assocs);

		for (int i = 0; i < assocs.length; i++) {
			// Check the pattern

			String pattern = assocs[i].getPattern();

			assertNotNull(pattern);
			assertTrue(pattern.length() > 0);

			// Check the file type
			
			ICFileType type = assocs[i].getType();
			
			assertNotNull(type);

			ICFileType typeById = model.getFileTypeById(type.getId());
			
			assertNotNull(typeById);
			assertEquals(type, typeById);

			// Check the type's language
			
			ICLanguage langIn = type.getLanguage();
			
			assertNotNull(langIn);
			
			String langId = langIn.getId();
			
			assertNotNull(langId);
			assertTrue(langId.length() > 0);
			
			ICLanguage langOut = model.getLanguageById(langId);

			assertNotNull(langOut);
			assertEquals(langIn, langOut);
		}
	}

	public final void testAdd() {
		boolean result = false;
		
		// Languages
		
		ICLanguage langIn = new CLanguage(LANG_TEST, "Test Language");
		
		result = ((ResolverModel) model).removeLanguage(langIn);
		assertFalse(result);

		result = ((ResolverModel) model).addLanguage(langIn);
		assertTrue(result);
		
		ICLanguage langOut = model.getLanguageById(LANG_TEST);
		assertNotNull(langOut);
		assertEquals(langIn, langOut);

		// File types

		ICFileType th = new CFileType(FT_TEST_HEADER, langIn, "Test Language Header", ICFileType.TYPE_HEADER);
		ICFileType ts = new CFileType(FT_TEST_SOURCE, langIn, "Test Language Source", ICFileType.TYPE_SOURCE);
		ICFileType tu = new CFileType(FT_TEST_WHASAT, langIn, "Test Language Unknown", ICFileType.TYPE_UNKNOWN);
		
		// -- header
		
		result = ((ResolverModel) model).removeFileType(th);
		assertFalse(result);

		result = ((ResolverModel) model).addFileType(th);
		assertTrue(result);

		ICFileType thOut = model.getFileTypeById(FT_TEST_HEADER);
		assertNotNull(thOut);
		assertEquals(th, thOut);

		// -- source

		result = ((ResolverModel) model).removeFileType(ts);
		assertFalse(result);

		result = ((ResolverModel) model).addFileType(ts);
		assertTrue(result);

		ICFileType tsOut = model.getFileTypeById(FT_TEST_SOURCE);
		assertNotNull(tsOut);
		assertEquals(ts, tsOut);

		// -- unknown

		result = ((ResolverModel) model).removeFileType(tu);
		assertFalse(result);

		result = ((ResolverModel) model).addFileType(tu);
		assertTrue(result);

		ICFileType tuOut = model.getFileTypeById(FT_TEST_WHASAT);
		assertNotNull(tuOut);
		assertEquals(tu, tuOut);

		// File type associations

		ICFileTypeAssociation tha = new CFileTypeAssociation("*.aest", th);
		ICFileTypeAssociation tsa = new CFileTypeAssociation("*.test", th);
		ICFileTypeAssociation tua = new CFileTypeAssociation("*.zest", th);

		// -- header

		result = workspaceResolver.removeAssociation(tha);
		assertFalse(result);

		result = workspaceResolver.addAssociation(tha);
		assertTrue(result);

		ICFileType thaOut = workspaceResolver.getFileType("file.aest");
		assertNotNull(thaOut);
		assertEquals(tha.getType(), thaOut);
		
		// -- source

		result = workspaceResolver.removeAssociation(tsa);
		assertFalse(result);

		result = workspaceResolver.addAssociation(tsa);
		assertTrue(result);

		ICFileType tsaOut = workspaceResolver.getFileType("file.test");
		assertNotNull(tsaOut);
		assertEquals(tsa.getType(), tsaOut);


		// -- unknown

		result = workspaceResolver.removeAssociation(tua);
		assertFalse(result);

		result = workspaceResolver.addAssociation(tua);
		assertTrue(result);

		ICFileType tuaOut = workspaceResolver.getFileType("file.zest");
		assertNotNull(tuaOut);
		assertEquals(tua.getType(), tuaOut);
	}

	public final void testRemove() {
		boolean result = false;

		// Languages
		
		ICLanguage lang = model.getLanguageById(LANG_TEST);
		ICFileType fth  = model.getFileTypeById(FT_TEST_HEADER);
		ICFileType fts  = model.getFileTypeById(FT_TEST_SOURCE);
		ICFileType ftu  = model.getFileTypeById(FT_TEST_WHASAT);

		// Test two file types
		
		result = ((ResolverModel) model).removeFileType(fth);
		assertTrue(result);

		result = ((ResolverModel) model).removeFileType(fth);
		assertFalse(result);

		result = ((ResolverModel) model).removeFileType(fts);
		assertTrue(result);

		result = ((ResolverModel) model).removeFileType(fts);
		assertFalse(result);

		// Removing the language should remove the
		// remaining file type
		
		assertNotNull(lang);
		assertEquals(LANG_TEST, lang.getId());
		
		result = ((ResolverModel) model).removeLanguage(lang);
		assertTrue(result);

		result = ((ResolverModel) model).removeLanguage(lang);
		assertFalse(result);

		result = ((ResolverModel) model).removeFileType(ftu);
		assertFalse(result);

		// File type associations

		ICFileTypeAssociation[] assocs = workspaceResolver.getFileTypeAssociations();
		assertNotNull(assocs);
		assertTrue(assocs.length > 3);

		for (int i = 0; i < assocs.length; i++) {
			if (assocs[i].getType().getLanguage().getId().equals(LANG_TEST)) {
				workspaceResolver.removeAssociation(assocs[i]);
			}
		}
		
	}

	class TestModelListener implements IResolverChangeListener {
		private ResolverChangeEvent fEvent;
		public TestModelListener() {
			model.addResolverChangeListener(this);
		}
		public void resolverChanged(ResolverChangeEvent event) {
			fEvent = event;
			model.removeResolverChangeListener(this);
			this.notifyAll();
		}
		public ResolverChangeEvent getEvent() {
			return fEvent;
		}
	}

	public final void testChangeNotifications() {
		ResolverModel			rawModel = ((ResolverModel) model);
		ResolverChangeEvent		event	 = null;	
		ICLanguage				lang 	 = new CLanguage(LANG_TEST, "Test Language");
		ICFileType				type	 = new CFileType("?", model.getLanguageById("?"), "?", ICFileType.TYPE_UNKNOWN);
		TestModelListener		listener = null;
		ResolverDelta[]			deltas	 = null;
		
		// Add language

		try {
			listener = new TestModelListener();
			synchronized (listener) {
				assertTrue(rawModel.addLanguage(lang));
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		assertNotNull(listener.getEvent());

		event  = listener.getEvent();
		deltas = event.getDeltas();
		
		assertNull(event.getResolver());
		assertEquals(1, deltas.length);
		assertEquals(ResolverDelta.ELEMENT_LANGUAGE, deltas[0].getElementType());
		assertEquals(ResolverDelta.EVENT_ADD, deltas[0].getEventType());
		assertNotNull(deltas[0].getElement());
		assertNull(deltas[0].getAssociation());
		assertNotNull(deltas[0].getLanguage());
		assertNull(deltas[0].getFileType());
		assertNull(deltas[0].getProject());
		
		// Repeated addition should not result in a change event
	
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				assertFalse(rawModel.addLanguage(lang));
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		assertNull(listener.getEvent());

		// Remove language
		
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				assertTrue(rawModel.removeLanguage(lang));
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		assertNotNull(listener.getEvent());

		event  = listener.getEvent();
		deltas = event.getDeltas();
		
		assertNull(event.getResolver());
		assertEquals(1, deltas.length);
		assertEquals(ResolverDelta.ELEMENT_LANGUAGE, deltas[0].getElementType());
		assertEquals(ResolverDelta.EVENT_REMOVE, deltas[0].getEventType());
		assertNotNull(deltas[0].getElement());
		assertNull(deltas[0].getAssociation());
		assertNotNull(deltas[0].getLanguage());
		assertNull(deltas[0].getFileType());
		assertNull(deltas[0].getProject());
		
		// Repeated removal should not result in a change event
		
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				assertFalse(rawModel.removeLanguage(lang));
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		assertNull(listener.getEvent());
		
		// Add file type

		try {
			listener = new TestModelListener();
			synchronized (listener) {
				assertTrue(rawModel.addFileType(type));
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		assertNotNull(listener.getEvent());

		event  = listener.getEvent();
		deltas = event.getDeltas();
		
		assertNull(event.getResolver());
		assertEquals(1, deltas.length);
		assertEquals(ResolverDelta.ELEMENT_FILETYPE, deltas[0].getElementType());
		assertEquals(ResolverDelta.EVENT_ADD, deltas[0].getEventType());
		assertNotNull(deltas[0].getElement());
		assertNull(deltas[0].getAssociation());
		assertNull(deltas[0].getLanguage());
		assertNotNull(deltas[0].getFileType());
		assertNull(deltas[0].getProject());

		// Repeated addition should not result in a change event
	
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				assertFalse(rawModel.addFileType(type));
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		assertNull(listener.getEvent());

		// Remove file type
		
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				assertTrue(rawModel.removeFileType(type));
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		assertNotNull(listener.getEvent());

		event  = listener.getEvent();
		deltas = event.getDeltas();
		
		assertNull(event.getResolver());
		assertEquals(1, deltas.length);
		assertEquals(ResolverDelta.ELEMENT_FILETYPE, deltas[0].getElementType());
		assertEquals(ResolverDelta.EVENT_REMOVE, deltas[0].getEventType());
		assertNotNull(deltas[0].getElement());
		assertNull(deltas[0].getAssociation());
		assertNull(deltas[0].getLanguage());
		assertNotNull(deltas[0].getFileType());
		assertNull(deltas[0].getProject());
		
		// Repeated removal should not result in a change event
		
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				assertFalse(rawModel.removeFileType(type));
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		assertNull(listener.getEvent());

		// Test setting workspace resolver
		
		ICFileTypeResolver testResolver = createResolver();
		
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				model.setResolver(testResolver);
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		assertNotNull(listener.getEvent());

		event  = listener.getEvent();
		deltas = event.getDeltas();
		
		assertNotNull(event.getResolver());
		assertTrue(deltas.length > 1);
		assertEquals(ResolverDelta.ELEMENT_WORKSPACE, deltas[0].getElementType());
		assertEquals(ResolverDelta.EVENT_SET, deltas[0].getEventType());
		assertNull(deltas[0].getElement());
		assertNull(deltas[0].getAssociation());
		assertNull(deltas[0].getLanguage());
		assertNull(deltas[0].getFileType());
		assertNull(deltas[0].getProject());
		
		// Test resetting workspace resolver

		try {
			listener = new TestModelListener();
			synchronized (listener) {
				model.setResolver(null);
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		assertNotNull(listener.getEvent());

		event  = listener.getEvent();
		deltas = event.getDeltas();
		
		assertNotNull(event.getResolver());
		assertTrue(deltas.length > 1);
		assertEquals(ResolverDelta.ELEMENT_WORKSPACE, deltas[0].getElementType());
		assertEquals(ResolverDelta.EVENT_SET, deltas[0].getEventType());
		assertNull(deltas[0].getElement());
		assertNull(deltas[0].getAssociation());
		assertNull(deltas[0].getLanguage());
		assertNull(deltas[0].getFileType());
		assertNull(deltas[0].getProject());

		// Test setting project resolver
		
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				model.setResolver(project, testResolver);
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		assertNotNull(listener.getEvent());

		event  = listener.getEvent();
		deltas = event.getDeltas();
		
		assertNotNull(event.getResolver());
		assertTrue(deltas.length > 1);
		assertEquals(ResolverDelta.ELEMENT_PROJECT, deltas[0].getElementType());
		assertEquals(ResolverDelta.EVENT_SET, deltas[0].getEventType());
		assertNotNull(deltas[0].getElement());
		assertNull(deltas[0].getAssociation());
		assertNull(deltas[0].getLanguage());
		assertNull(deltas[0].getFileType());
		assertNotNull(deltas[0].getProject());
		
		// Test resetting project resolver

		try {
			listener = new TestModelListener();
			synchronized (listener) {
				model.setResolver(project, null);
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		assertNotNull(listener.getEvent());

		event  = listener.getEvent();
		deltas = event.getDeltas();

		assertNotNull(event.getResolver());
		assertTrue(deltas.length > 1);
		assertEquals(ResolverDelta.ELEMENT_PROJECT, deltas[0].getElementType());
		assertEquals(ResolverDelta.EVENT_SET, deltas[0].getEventType());
		assertNotNull(deltas[0].getElement());
		assertNull(deltas[0].getAssociation());
		assertNull(deltas[0].getLanguage());
		assertNull(deltas[0].getFileType());
		assertNotNull(deltas[0].getProject());
	}

}
