/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.index.tests;

import java.io.File;
import java.io.FileWriter;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

public class Bug246129 extends IndexTestBase {

	public static TestSuite suite() {
		TestSuite suite = suite(Bug246129.class, "_");
		// suite.addTest(new Bug246129("include ext/../type.h"));
		return suite;
	}

	private ICProject fProject;

	private IFile fSource; 
	
	private IFolder fWrapperIncludeFolder;

	private IFolder fIncludeFolder;
	
	private File fTmpDir;
	
	private File fExternalWrapperIncludeFolder;

	private File fExternalWrapperHeader;
	
	private File fExternalIncludeFolder;
	
	private File fExternalHeader;
	
	private File fExternalExtFolder;
	
	IIndex fIndex;
	
	boolean fFalseFriendsAccepted;
	
	public Bug246129(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (fProject == null) {

			// Populate workspace
			fProject = createProject(true, "resources/indexTests/bug246129");
			
			fSource = fProject.getProject().getFile("source.cpp");
			
			fWrapperIncludeFolder = fProject.getProject().getFolder(
					"wrapper_include");
			
			fIncludeFolder = fProject.getProject().getFolder("include");

			// Create header files external to the workspace.
			fTmpDir = CProjectHelper.freshDir();
			
			fExternalWrapperIncludeFolder = new File(fTmpDir,
					"wrapper_include");
			fExternalWrapperIncludeFolder.mkdir();
			
			fExternalWrapperHeader = new File(
					fExternalWrapperIncludeFolder, "external_type.h");
			fExternalWrapperHeader.createNewFile();
			FileWriter writer = new FileWriter(fExternalWrapperHeader);
			writer.write("#ifndef EXTERNAL_WRAPPER_TYPE_H_\n");
			writer.write("#define EXTERNAL_WRAPPER_TYPE_H_\n");
			writer.write("#include <ext/../external_type.h>\n");
			writer.write("class ExternalWrapper {\n");
			writer.write("};\n");
			writer.write("#endif\n");
			writer.close();
			
			fExternalIncludeFolder = new File(fTmpDir, "include");
			fExternalIncludeFolder.mkdir();
			
			fExternalExtFolder = new File(fExternalIncludeFolder, "ext");
			fExternalExtFolder.mkdir();

			fExternalHeader = new File(fExternalIncludeFolder,
					"external_type.h");
			fExternalHeader.createNewFile();
			writer = new FileWriter(fExternalHeader);
			writer.write("#ifndef EXTERNAL_TYPE_H_\n");
			writer.write("#define EXTERNAL_TYPE_H_\n");
			writer.write("class ExternalType {\n");
			writer.write("};\n");
			writer.write("#endif\n");
			writer.close();

			// The indexer needs non-empty build info in order to index
			// source files if index-all-files is turned off.
			IPathEntry[] entries = new IPathEntry[] { CoreModel
					.newIncludeEntry(fProject.getPath(), null,
							fWrapperIncludeFolder.getLocation()),
					CoreModel.newIncludeEntry(fProject.getPath(), null,
							fIncludeFolder.getLocation()) };
			
			fProject.setRawPathEntries(entries, npm());

			// However, the scanner info provider used by the unit tests
			// needs separate setup, and this one must be complete.
			TestScannerProvider.sIncludes = new String[] {
					fWrapperIncludeFolder.getLocation().toOSString(),
					fIncludeFolder.getLocation().toOSString(),
					fExternalWrapperIncludeFolder.getAbsolutePath(),
					fExternalIncludeFolder.getAbsolutePath() };

			IndexerPreferences.set(fProject.getProject(),
					IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, "false");

			File falseFriendDirectory = new File(fWrapperIncludeFolder
					.getLocation().toOSString()
					+ "/ext/..");

			fFalseFriendsAccepted = falseFriendDirectory.exists();
			
			CCorePlugin.getIndexManager().reindex(fProject);
			assertTrue(CCorePlugin.getIndexManager().joinIndexer(10000, npm()));
			fIndex = CCorePlugin.getIndexManager().getIndex(fProject);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		fExternalWrapperHeader.delete();
		fExternalWrapperIncludeFolder.delete();
		
		fExternalHeader.delete();
		fExternalExtFolder.delete();
		fExternalIncludeFolder.delete();

		fTmpDir.delete();
		
		super.tearDown();
	}

	private void assertSymbolInIndex(String symbolName) throws Exception {
		IIndexBinding[] bindings = fIndex.findBindings(
				symbolName
				.toCharArray(), false, IndexFilter.ALL, npm());
		assertTrue(bindings.length > 0);
	}
	
	public void testIndex() throws Exception {

		try {

			fIndex.acquireReadLock();
			IIndexFile[] indexFiles = fIndex.getAllFiles();

			// Check that all header files have been found, provided the
			// File implementation does support it.
			if (fFalseFriendsAccepted) {
				assertEquals(3, indexFiles.length);
			} else {
				assertEquals(5, indexFiles.length);
			}
			
			// The wrapper classes are found regardless whether false friends
			// are
			// accepted or not.
			assertSymbolInIndex("Wrapper");
			assertSymbolInIndex("ExternalWrapper");

			// The Type class is only known on platforms with a File
			// implementation sorting out the false friends.
			if (!fFalseFriendsAccepted) {
				assertSymbolInIndex("Type");
				assertSymbolInIndex("ExternalType");
			}
			
			// Check that all paths are normalized.
			for (IIndexFile indexFile : indexFiles) {

				IIndexInclude[] includes = indexFile.getIncludes();
				
				for (IIndexInclude i : includes) {
					IIndexFileLocation location = i.getIncludesLocation();
					assertNotNull(location);
					
					assertFalse(location.getURI().toASCIIString()
							.contains(".."));

					String fullPath = location.getFullPath();
					if (fullPath != null) {
						assertFalse(fullPath.contains(".."));
					}
				}
			}
			
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	private void assertSymbolInAst(IScope scope, String symbolName)
			throws Exception {
		IBinding[] bindings = scope.find(symbolName);
		assertTrue(bindings.length > 0);
	}
	
	public void testAst() throws Exception {
		ITranslationUnit tu = CoreModel.getDefault().createTranslationUnitFrom(
				fProject, fSource.getLocation());

		IASTTranslationUnit ast = tu.getAST();
		
		// The wrapper classes are found regardless whether false friends
		// are
		// accepted or not.
		IScope topLevel = ast.getScope();
		assertSymbolInAst(topLevel, "Wrapper");
		assertSymbolInAst(topLevel, "ExternalWrapper");

		// The Type class is only known on platforms with a File
		// implementation sorting out the false friends.
		if (!fFalseFriendsAccepted) {
			assertSymbolInAst(topLevel, "Type");
			assertSymbolInAst(topLevel, "ExternalType");
		}

		// Check that all paths are normalized.
		IASTPreprocessorIncludeStatement[] includes = ast
				.getIncludeDirectives();		
		for (IASTPreprocessorIncludeStatement i : includes) {
			String includedPath = i.getPath();

			assertNotNull(includedPath);
			assertFalse(includedPath.contains(".."));
		}
	}
}
