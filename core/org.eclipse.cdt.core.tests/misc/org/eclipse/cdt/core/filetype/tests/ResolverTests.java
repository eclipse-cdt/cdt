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

import java.util.ArrayList;
import java.util.List;

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
	protected IResolverModel		model;
	private static IProject		project;
	
	private static final String PLUGIN_ID = "org.eclipse.cdt.core.filetype.tests"; //$NON-NLS-1$
	private static final String LANG_TEST = PLUGIN_ID + ".test"; //$NON-NLS-1$
	private static final String FT_TEST_HEADER = LANG_TEST + ".header"; //$NON-NLS-1$
	private static final String FT_TEST_SOURCE = LANG_TEST + ".source"; //$NON-NLS-1$
	private static final String FT_TEST_WHASAT = LANG_TEST + ".unknown"; //$NON-NLS-1$
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ResolverTests.class.getName());
		suite.addTest(new ResolverTests("testInternalCtors")); //$NON-NLS-1$
		suite.addTest(new ResolverTests("testDefaultFileTypeResolution")); //$NON-NLS-1$
		suite.addTest(new ResolverTests("testWorkspaceFileTypeResolution")); //$NON-NLS-1$
		suite.addTest(new ResolverTests("testProjectFileTypeResolution")); //$NON-NLS-1$
		suite.addTest(new ResolverTests("testGetLanguages")); //$NON-NLS-1$
		suite.addTest(new ResolverTests("testGetTypes")); //$NON-NLS-1$
		suite.addTest(new ResolverTests("testGetFileTypeAssociations")); //$NON-NLS-1$
		suite.addTest(new ResolverTests("testAdd")); //$NON-NLS-1$
		suite.addTest(new ResolverTests("testRemove")); //$NON-NLS-1$
		suite.addTest(new ResolverTests("testChangeNotifications")); //$NON-NLS-1$

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
		IProject project = root.getProject("testResolverProject"); //$NON-NLS-1$
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
		
		//model.setResolver(null);
		//model.setResolver(project, null);
		
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

//	private ICFileTypeResolver createResolver() {
//		return new CFileTypeResolver();
//	}
	
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
			lang = new CLanguage(LANG_TEST, ""); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
		}
		assertNull(lang);

		try {
			lang = new CLanguage(null, "L"); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
		}
		assertNull(lang);


		try {
			lang = new CLanguage("", "L"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IllegalArgumentException e) {
		}
		assertNull(lang);

		lang = new CLanguage(LANG_TEST, "Test Language"); //$NON-NLS-1$
		assertNotNull(lang);

		// File type
		// str id, cls lang, str name, int type

		try {
			type = new CFileType(FT_TEST_HEADER, lang, "T", -1); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
		}
		assertNull(type);

		try {
			type = new CFileType(FT_TEST_HEADER, lang, "T", 0x04091998); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
		}
		assertNull(type);

		try {
			type = new CFileType(FT_TEST_HEADER, lang, null, ICFileType.TYPE_HEADER);
		} catch (IllegalArgumentException e) {
		}
		assertNull(type);

		try {
			type = new CFileType(FT_TEST_HEADER, lang, "", ICFileType.TYPE_HEADER); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
		}
		assertNull(type);

		try {
			type = new CFileType(FT_TEST_HEADER, null, "T", ICFileType.TYPE_HEADER); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
		}
		assertNull(type);

		try {
			type = new CFileType(null, lang, "T", ICFileType.TYPE_HEADER); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
		}
		assertNull(type);

		try {
			type = new CFileType("", lang, "T", ICFileType.TYPE_HEADER); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IllegalArgumentException e) {
		}
		assertNull(type);

		type = new CFileType(FT_TEST_HEADER, lang, "T", ICFileType.TYPE_HEADER); //$NON-NLS-1$
		assertNotNull(type);

		// Association

		try {
			assoc = new CFileTypeAssociation("*.xyz", null); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
		}
		assertNull(assoc);
		
		try {
			assoc = new CFileTypeAssociation(null, type);
		} catch (IllegalArgumentException e) {
		}
		assertNull(assoc);

		try {
			assoc = new CFileTypeAssociation("", type); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
		}
		assertNull(assoc);

		assoc = new CFileTypeAssociation("*.xyz", type); //$NON-NLS-1$
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
		doTestFileTypeResolution(workspaceResolver, "", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, " ", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$

		// Odd filenames
		doTestFileTypeResolution(workspaceResolver, ".", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, ".c.", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, ".cpp.", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.c.", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.cpp.", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.c.input", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.cpp.input", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "c", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "cpp", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "numerical", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "some/path/file.c", ICFileTypeConstants.FT_C_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "some/path/file.cpp", ICFileTypeConstants.FT_CXX_SOURCE); //$NON-NLS-1$
		
		// C source/header
		doTestFileTypeResolution(workspaceResolver, "file.c", ICFileTypeConstants.FT_C_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.h", ICFileTypeConstants.FT_C_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "some.file.c", ICFileTypeConstants.FT_C_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "some.file.h", ICFileTypeConstants.FT_C_HEADER); //$NON-NLS-1$
		
		// C++ source/header
		doTestFileTypeResolution(workspaceResolver, "file.cpp", ICFileTypeConstants.FT_CXX_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.cxx", ICFileTypeConstants.FT_CXX_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.cc", ICFileTypeConstants.FT_CXX_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.C", ICFileTypeConstants.FT_CXX_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.hpp", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.hxx", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.hh", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.H", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "some.file.cpp", ICFileTypeConstants.FT_CXX_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "some.file.hxx", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$

		// Assembly
		doTestFileTypeResolution(workspaceResolver, "file.asm", ICFileTypeConstants.FT_ASM_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.s", ICFileTypeConstants.FT_ASM_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.S", ICFileTypeConstants.FT_ASM_SOURCE); //$NON-NLS-1$
		
		// Std C++ library
		doTestFileTypeResolution(workspaceResolver, "algorithm", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "bitset", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "deque", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "exception", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "fstream", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "functional", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "iomanip", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "ios", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "iosfwd", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "iostream", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "istream", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "iterator", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "limits", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "list", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "locale", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "map", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "memory", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "new", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "numeric", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "ostream", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "queue", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "set", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "sstream", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "stack", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "stdexcept", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "streambuf", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "string", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "typeinfo", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "utility", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "valarray", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "vector", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		
		// Failure cases
		doTestFileTypeResolution(workspaceResolver, "file.txt", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.doc", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "files", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "FILES", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "stream", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "streambu", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "streambuff", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "sstreams", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
	}

	public final void testWorkspaceFileTypeResolution() {

		// Reset the resolver
		//model.setResolver(null);
		workspaceResolver = model.getResolver();
		//workspaceResolver = new WorkspaceResolver();
		
		// Validate that we are using the default resolver set...
		doTestFileTypeResolution(workspaceResolver, "file.c", ICFileTypeConstants.FT_C_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.h", ICFileTypeConstants.FT_C_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.cpp", ICFileTypeConstants.FT_CXX_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.hpp", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.s", ICFileTypeConstants.FT_ASM_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.sam", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.shari", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.delainey", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		
		// Set up a new resolver just for the tests
		// This one will only recognize '*.c', '*.h', and '*.sam'
		//ICFileTypeResolver resolver = createResolver();
		ICFileTypeResolver resolver = workspaceResolver.createWorkingCopy();
		resolver.removeAssociations(workspaceResolver.getFileTypeAssociations());

		ICFileTypeAssociation[] assocs = new ICFileTypeAssociation[1];
		
		assocs[0] =model.createAssocation("*.sam", model.getFileTypeById(ICFileTypeConstants.FT_C_SOURCE)); //$NON-NLS-1$
		resolver.addAssociations(assocs);

		assocs[0] = model.createAssocation("*.shari", model.getFileTypeById(ICFileTypeConstants.FT_C_HEADER)); //$NON-NLS-1$
		resolver.addAssociations(assocs);
		
		assocs[0] = model.createAssocation("*.delainey", model.getFileTypeById(ICFileTypeConstants.FT_ASM_SOURCE)); //$NON-NLS-1$
		resolver.addAssociations(assocs);

		// Set the workspace to use the new resolver
		//model.setResolver(resolver);
		//workspaceResolver = model.getResolver(); 
		workspaceResolver = resolver;

		// Test the known types
		doTestFileTypeResolution(workspaceResolver, "file.sam", ICFileTypeConstants.FT_C_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.shari", ICFileTypeConstants.FT_C_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.delainey", ICFileTypeConstants.FT_ASM_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "some.file.sam", ICFileTypeConstants.FT_C_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "some.file.shari", ICFileTypeConstants.FT_C_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "some.file.delainey", ICFileTypeConstants.FT_ASM_SOURCE); //$NON-NLS-1$
		
		// Failure cases
		doTestFileTypeResolution(workspaceResolver, "file.c", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.h", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.cpp", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.hpp", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.s", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		
		// Reset the resolver
		//model.setResolver(null);
		workspaceResolver = model.getResolver(); 
		
		// Validate that we are back to using the default resolver set...
		doTestFileTypeResolution(workspaceResolver, "file.c", ICFileTypeConstants.FT_C_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.h", ICFileTypeConstants.FT_C_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.cpp", ICFileTypeConstants.FT_CXX_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.hpp", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.s", ICFileTypeConstants.FT_ASM_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.sam", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.shari", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(workspaceResolver, "file.delainey", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
	}
	
	public final void testProjectFileTypeResolution() {

		// Reset the resolver(s)
		//model.setResolver(null);
		workspaceResolver = model.getResolver();
		
		//model.setResolver(project, null);
		//projectResolver = model.getResolver(project);
		projectResolver = model.createCustomResolver(project, workspaceResolver);
		
		// Validate that we are using the default resolver set...
		doTestFileTypeResolution(projectResolver, "file.c", ICFileTypeConstants.FT_C_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.cpp", ICFileTypeConstants.FT_CXX_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.hpp", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.s", ICFileTypeConstants.FT_ASM_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.sam", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.shari", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.delainey", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		
		// Set up a new resolver just for the tests
		// This one will only recognize '*.c', '*.h', and '*.sam'
		//ICFileTypeResolver resolver = createResolver();
		ICFileTypeResolver resolver = workspaceResolver.createWorkingCopy();
		resolver.removeAssociations(projectResolver.getFileTypeAssociations());
		ICFileTypeAssociation[] assocs = new ICFileTypeAssociation[1];

		assocs[0] = model.createAssocation("*.sam", model.getFileTypeById(ICFileTypeConstants.FT_C_SOURCE)); //$NON-NLS-1$
		resolver.addAssociations(assocs);
		
		assocs[0] = model.createAssocation("*.shari", model.getFileTypeById(ICFileTypeConstants.FT_C_HEADER)); //$NON-NLS-1$
		resolver.addAssociations(assocs);
		
		assocs[0] = model.createAssocation("*.delainey", model.getFileTypeById(ICFileTypeConstants.FT_ASM_SOURCE)); //$NON-NLS-1$
		resolver.addAssociations(assocs);
		
		// Set the workspace to use the new resolver
		//model.setResolver(project, resolver);
		//projectResolver = model.getResolver(project);
		projectResolver = resolver;

		// Test the known types
		doTestFileTypeResolution(projectResolver, "file.sam", ICFileTypeConstants.FT_C_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.shari", ICFileTypeConstants.FT_C_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.delainey", ICFileTypeConstants.FT_ASM_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "some.file.sam", ICFileTypeConstants.FT_C_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "some.file.shari", ICFileTypeConstants.FT_C_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "some.file.delainey", ICFileTypeConstants.FT_ASM_SOURCE); //$NON-NLS-1$
		
		// Failure cases
		doTestFileTypeResolution(projectResolver, "file.c", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.h", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.cpp", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.hpp", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.s", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		
		// Reset the resolver
		//model.setResolver(project, null);
		//projectResolver = model.getResolver(project);
		projectResolver = model.createCustomResolver(project, workspaceResolver);
		
		// Validate that we are back to using the default resolver set...
		doTestFileTypeResolution(projectResolver, "file.c", ICFileTypeConstants.FT_C_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.h", ICFileTypeConstants.FT_C_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.cpp", ICFileTypeConstants.FT_CXX_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.hpp", ICFileTypeConstants.FT_CXX_HEADER); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.s", ICFileTypeConstants.FT_ASM_SOURCE); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.sam", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.shari", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
		doTestFileTypeResolution(projectResolver, "file.delainey", ICFileTypeConstants.FT_UNKNOWN); //$NON-NLS-1$
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
		
		ICLanguage langIn = new CLanguage(LANG_TEST, "Test Language"); //$NON-NLS-1$
		ICLanguage[] langIns = new ICLanguage[]{langIn}; //$NON-NLS-1$

		result = ((ResolverModel) model).removeLanguages(langIns);
		assertFalse(result);

		result = ((ResolverModel) model).addLanguages(langIns);
		assertTrue(result);
		
		ICLanguage langOut = model.getLanguageById(LANG_TEST);
		assertNotNull(langOut);
		assertEquals(langIn, langOut);

		// File types

		ICFileType th = new CFileType(FT_TEST_HEADER, langIn, "Test Language Header", ICFileType.TYPE_HEADER); //$NON-NLS-1$
		ICFileType[] ths = new ICFileType[] {th};
		ICFileType ts = new CFileType(FT_TEST_SOURCE, langIn, "Test Language Source", ICFileType.TYPE_SOURCE); //$NON-NLS-1$
		ICFileType[] tss = new ICFileType[] {ts};
		ICFileType tu = new CFileType(FT_TEST_WHASAT, langIn, "Test Language Unknown", ICFileType.TYPE_UNKNOWN); //$NON-NLS-1$
		ICFileType[] tus = new ICFileType[] {tu};
		
		// -- header
		
		result = ((ResolverModel) model).removeFileTypes(ths);
		assertFalse(result);

		result = ((ResolverModel) model).addFileTypes(ths);
		assertTrue(result);

		ICFileType thOut = model.getFileTypeById(FT_TEST_HEADER);
		assertNotNull(thOut);
		assertEquals(th, thOut);

		// -- source

		result = ((ResolverModel) model).removeFileTypes(tss);
		assertFalse(result);

		result = ((ResolverModel) model).addFileTypes(tss);
		assertTrue(result);

		ICFileType tsOut = model.getFileTypeById(FT_TEST_SOURCE);
		assertNotNull(tsOut);
		assertEquals(ts, tsOut);

		// -- unknown

		result = ((ResolverModel) model).removeFileTypes(tus);
		assertFalse(result);

		result = ((ResolverModel) model).addFileTypes(tus);
		assertTrue(result);

		ICFileType tuOut = model.getFileTypeById(FT_TEST_WHASAT);
		assertNotNull(tuOut);
		assertEquals(tu, tuOut);

		// File type associations

		ICFileTypeAssociation tha = new CFileTypeAssociation("*.aest", th); //$NON-NLS-1$
		ICFileTypeAssociation[] thas = new ICFileTypeAssociation[] { tha };
		ICFileTypeAssociation tsa = new CFileTypeAssociation("*.test", th); //$NON-NLS-1$
		ICFileTypeAssociation[] tsas = new ICFileTypeAssociation[] {tsa};
		ICFileTypeAssociation tua = new CFileTypeAssociation("*.zest", th); //$NON-NLS-1$
		ICFileTypeAssociation[] tuas = new ICFileTypeAssociation[] { tua };

		// -- header

		result = workspaceResolver.removeAssociations(thas);
		assertFalse(result);

		result = workspaceResolver.addAssociations(thas);
		assertTrue(result);

		ICFileType thaOut = workspaceResolver.getFileType("file.aest"); //$NON-NLS-1$
		assertNotNull(thaOut);
		assertEquals(tha.getType(), thaOut);
		
		// -- source

		result = workspaceResolver.removeAssociations(tsas);
		assertFalse(result);

		result = workspaceResolver.addAssociations(tsas);
		assertTrue(result);

		ICFileType tsaOut = workspaceResolver.getFileType("file.test"); //$NON-NLS-1$
		assertNotNull(tsaOut);
		assertEquals(tsa.getType(), tsaOut);


		// -- unknown

		result = workspaceResolver.removeAssociations(tuas);
		assertFalse(result);

		result = workspaceResolver.addAssociations(tuas);
		assertTrue(result);

		ICFileType tuaOut = workspaceResolver.getFileType("file.zest"); //$NON-NLS-1$
		assertNotNull(tuaOut);
		assertEquals(tua.getType(), tuaOut);
	}

	public final void testRemove() {
		boolean result = false;

		// Languages
		
		ICLanguage lang = model.getLanguageById(LANG_TEST);
		ICLanguage[] langs = new ICLanguage[] { lang };
		ICFileType fth  = model.getFileTypeById(FT_TEST_HEADER);
		ICFileType[] fths = new ICFileType[] { fth };
		ICFileType fts  = model.getFileTypeById(FT_TEST_SOURCE);
		ICFileType[] ftss = new ICFileType[] { fts };
		ICFileType ftu  = model.getFileTypeById(FT_TEST_WHASAT);
		ICFileType[] ftus = new ICFileType[] { ftu };

		// Test two file types
		
		result = ((ResolverModel) model).removeFileTypes(fths);
		assertTrue(result);

		result = ((ResolverModel) model).removeFileTypes(fths);
		assertFalse(result);

		result = ((ResolverModel) model).removeFileTypes(ftss);
		assertTrue(result);

		result = ((ResolverModel) model).removeFileTypes(ftss);
		assertFalse(result);

		// Removing the language should remove the
		// remaining file type
		
		assertNotNull(lang);
		assertEquals(LANG_TEST, lang.getId());
		
		result = ((ResolverModel) model).removeLanguages(langs);
		assertTrue(result);

		result = ((ResolverModel) model).removeLanguages(langs);
		assertFalse(result);

		result = ((ResolverModel) model).removeFileTypes(ftus);
		assertFalse(result);

		// File type associations

		ICFileTypeAssociation[] assocs = workspaceResolver.getFileTypeAssociations();
		assertNotNull(assocs);
		assertTrue(assocs.length > 3);

		List list = new ArrayList();
		for (int i = 0; i < assocs.length; i++) {
			if (assocs[i].getType().getLanguage().getId().equals(LANG_TEST)) {
				list.add (assocs[i]);
				//workspaceResolver.removeAssociation(assocs[i]);
			}
		}
		assocs = (ICFileTypeAssociation[]) list.toArray(new ICFileTypeAssociation[list.size()]);
		workspaceResolver.removeAssociations(assocs);
		
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
		ICLanguage				lang 	 = new CLanguage(LANG_TEST, "Test Language"); //$NON-NLS-1$
		ICLanguage[]			langs	= new ICLanguage[] { lang };
		ICFileType				type	 = new CFileType("?", model.getLanguageById("?"), "?", ICFileType.TYPE_UNKNOWN); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ICFileType[]	types = new ICFileType[] { type };
		TestModelListener		listener = null;
		ResolverDelta[] 	deltas = null;

		// Add language
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				assertTrue(rawModel.addLanguages(langs));
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		event  = listener.getEvent();
		assertNotNull(event);

		deltas = event.getDeltas();
		assertEquals(1, deltas.length);
		assertNotNull(event.getResolver());
		assertNotNull(event.getResolver().getContainer());
		assertEquals(ResolverDelta.ELEMENT_LANGUAGE, deltas[0].getElementType());
		assertEquals(ResolverDelta.EVENT_ADD, deltas[0].getEventType());
		assertNotNull(deltas[0].getElement());
		assertNull(deltas[0].getAssociation());
		assertNull(deltas[0].getFileType());
		//assertNull(deltas[0].getProject());
		
		// Repeated addition should not result in a change event
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				assertFalse(rawModel.addLanguages(langs));
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
				assertTrue(rawModel.removeLanguages(langs));
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		event  = listener.getEvent();
		assertNotNull(event);

		deltas = event.getDeltas();
		assertEquals(1, deltas.length);
		assertNotNull(event.getResolver());
		assertNotNull(event.getResolver().getContainer());
		assertEquals(ResolverDelta.ELEMENT_LANGUAGE, deltas[0].getElementType());
		assertEquals(ResolverDelta.EVENT_REMOVE, deltas[0].getEventType());
		//assertNotNull(deltas[0].getElement());
		assertNull(deltas[0].getAssociation());
		assertNotNull(deltas[0].getLanguage());
		assertNull(deltas[0].getFileType());
		//assertNull(deltas[0].getProject());
		
		// Repeated removal should not result in a change event
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				assertFalse(rawModel.removeLanguages(langs));
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
				assertTrue(rawModel.addFileTypes(types));
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		event  = listener.getEvent();
		assertNotNull(event);

		deltas = event.getDeltas();
		assertEquals(1, deltas.length);
		assertNotNull(event.getResolver());
		assertNotNull(event.getResolver().getContainer());
		assertEquals(ResolverDelta.ELEMENT_FILETYPE, deltas[0].getElementType());
		assertEquals(ResolverDelta.EVENT_ADD, deltas[0].getEventType());
		assertNotNull(deltas[0].getElement());
		assertNull(deltas[0].getAssociation());
		assertNull(deltas[0].getLanguage());
		assertNotNull(deltas[0].getFileType());
		//assertNull(deltas[0].getProject());

		// Repeated addition should not result in a change event
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				assertFalse(rawModel.addFileTypes(types));
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
				assertTrue(rawModel.removeFileTypes(types));
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		event  = listener.getEvent();
		assertNotNull(event);

		deltas = event.getDeltas();
		assertEquals(1, deltas.length);
		assertNotNull(event.getResolver());
		assertNotNull(event.getResolver().getContainer());
		assertEquals(ResolverDelta.ELEMENT_FILETYPE, deltas[0].getElementType());
		assertEquals(ResolverDelta.EVENT_REMOVE, deltas[0].getEventType());
		assertNotNull(deltas[0].getElement());
		assertNull(deltas[0].getAssociation());
		assertNull(deltas[0].getLanguage());
		assertNotNull(deltas[0].getFileType());
		//assertNull(deltas[0].getProject());
		
		// Repeated removal should not result in a change event
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				assertFalse(rawModel.removeFileTypes(types));
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		assertNull(listener.getEvent());

//		// Test setting workspace resolver
//		ICFileTypeResolver testResolver = createResolver();
//		
//		try {
//			listener = new TestModelListener();
//			synchronized (listener) {
//				model.setResolver(testResolver);
//				listener.wait(3);
//			}
//		} catch (InterruptedException e) {
//			assertTrue(e.getMessage(), false);
//		}
//		
//		events  = listener.getEvents();
//		assertNotNull(events);
//	
//		assertTrue(events.length > 1);
//		assertNotNull(events[0].getContainer());
//		assertEquals(ResolverChangeEvent.ELEMENT_RESOLVER, events[0].getElementType());
//		assertEquals(ResolverChangeEvent.EVENT_SET, events[0].getEventType());
//		assertNotNull(events[0].getElement());
//		//assertNull(deltas[0].getAssociation());
//		//assertNull(deltas[0].getLanguage());
//		//assertNull(deltas[0].getFileType());
//		//assertNull(deltas[0].getProject());
//		
//		// Test resetting workspace resolver
//		try {
//			listener = new TestModelListener();
//			synchronized (listener) {
//				model.setResolver(null);
//				listener.wait(3);
//			}
//		} catch (InterruptedException e) {
//			assertTrue(e.getMessage(), false);
//		}
//		
//		events  = listener.getEvents();
//		assertNotNull(events);
//
//		
//		assertTrue(events.length > 1);
//		assertNotNull(events[0].getContainer());
//		assertEquals(ResolverChangeEvent.ELEMENT_RESOLVER, events[0].getElementType());
//		assertEquals(ResolverChangeEvent.EVENT_SET, events[0].getEventType());
//		assertNotNull(events[0].getElement());
//		//assertNull(deltas[0].getAssociation());
//		//assertNull(deltas[0].getLanguage());
//		//assertNull(deltas[0].getFileType());
//		//assertNull(deltas[0].getProject());
//
		// Test setting project resolver
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				//model.setResolver(project, testResolver);
				model.createCustomResolver(project, null);
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		event  = listener.getEvent();
		assertNotNull(event);

		deltas = event.getDeltas();
		//assertTrue(deltas.length >= 1);
		assertNotNull(event.getResolver().getContainer());
		assertTrue(event.resolverHasChanged());
		//assertEquals(ResolverChangeEvent.ELEMENT_RESOLVER, events[0].getElementType());
		//assertEquals(ResolverChangeEvent.EVENT_SET, events[0].getEventType());
		//assertNotNull(events[0].getElement());
		//assertNull(deltas[0].getAssociation());
		//assertNull(deltas[0].getLanguage());
		//assertNull(deltas[0].getFileType());
		//assertNotNull(deltas[0].getProject());
		
		// Test resetting project resolver
		try {
			listener = new TestModelListener();
			synchronized (listener) {
				//model.setResolver(project, null);
				model.removeCustomResolver(project);
				listener.wait(3);
			}
		} catch (InterruptedException e) {
			assertTrue(e.getMessage(), false);
		}
		
		event  = listener.getEvent();
		assertNotNull(event);

		deltas = event.getDeltas();
		//assertTrue(deltas.length >= 1);
		assertNotNull(event.getResolver().getContainer());
		assertTrue(event.resolverHasChanged());
		//assertEquals(ResolverChangeEvent.ELEMENT_RESOLVER, events[0].getElementType());
		//assertEquals(ResolverChangeEvent.EVENT_SET, events[0].getEventType());
		//assertNotNull(events[0].getElement());
		//assertNull(deltas[0].getAssociation());
		//assertNull(deltas[0].getLanguage());
		//assertNull(deltas[0].getFileType());
		//assertNotNull(deltas[0].getProject());
	}

}
