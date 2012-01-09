/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;
import java.util.regex.Pattern;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;

/**
 * Test that PDOM correctly track declarations, definitions and references of
 * objects
 * 
 * @author ELaskavaia@qnx.com
 * 
 */
public class DefDeclTests extends PDOMTestBase {
	private String projectName = null;

	protected PDOM pdom;
	protected ICProject cproject;
	
	public static Test suite() {
		return suite(DefDeclTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		String requiredName = "defDeclTests";
		cproject = createProject(requiredName);
		this.projectName = cproject.getElementName();
		pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject);
		pdom.acquireReadLock();
	}

	@Override
	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
	}

	private IBinding findSingleBinding(String elName) throws CoreException {
		IBinding[] binds = pdom.findBindings(Pattern.compile(elName), true,
				IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(1, binds.length);
		assertEquals(elName, binds[0].getName());
		IBinding element = binds[0];
		return element;
	}

	private void checkReference(IBinding element, String mark, int checkCount)
			throws Exception {
		checkUsage(element, mark, checkCount, IIndex.FIND_REFERENCES);
	}

	private void checkDeclaration(IBinding element, String mark, int num)
			throws Exception {
		checkUsage(element, mark, num, IIndex.FIND_DECLARATIONS);
	}

	private void checkDefinition(IBinding element, String mark, int countNum)
			throws Exception {
		checkUsage(element, mark, countNum, IIndex.FIND_DEFINITIONS);
	}

	private void checkUsage(IBinding element, String mark, int countNum,
			int flags) throws Exception {
		if (mark == null || countNum == 0) {
			getFirstUsage(element, 0, flags);
		} else {
			IName[] usage = pdom.findNames(element, flags);
			if (countNum >= 0)
				assertEquals(countNum, usage.length);
			String fail = null;
			boolean found = false;
			for (int i = 0; i < usage.length; i++) {
				IName name = usage[i];
				IASTFileLocation loc = name.getFileLocation();
				String fileName = new File(loc.getFileName()).getName();
				int markLine;
				try {
					markLine = getMarkLine(mark, fileName);
				} catch (AssertionFailedError e) {
					fail = e.getMessage();
					continue;
				}
				int nodeLine = getLineNumber(loc.getNodeOffset(), fileName);
				if (markLine != nodeLine) {
					fail = "Marker at line " + markLine + ", actual at line "
							+ nodeLine;
				} else {
					found = true;
				}
			}
			if (found == false)
				fail(fail);
		}
	}

	/**
	 * Get references defined by flags. If k>0 check that there are k of them.
	 * 
	 * @param binding
	 * @param k -
	 *            number of references, if k==-1 no check
	 * @return first references or null of non
	 * @throws CoreException
	 */
	private IName getFirstUsage(IBinding binding, int k, int flags)
			throws CoreException {
		IName[] decls = pdom.findNames(binding, flags);
		if (k >= 0)
			assertEquals(k, decls.length);
		if (decls.length > 0) {
			IName ref = decls[0];
			return ref;
		} else {
			return null;
		}
	}

	protected void assertAtMark(IASTFileLocation loc, String mark)
			throws Exception {
		String fileName = new File(loc.getFileName()).getName();
		int markLine = getMarkLine(mark, fileName);
		int nodeLine = getLineNumber(loc.getNodeOffset(), fileName);
		assertEquals(markLine, nodeLine);
	}

	private int getMarkLine(String mark, String fileName) throws Exception,
			BadLocationException {
		int markLine = getLineNumber(offset(fileName, mark), fileName);
		return markLine;
	}

	protected int getLineNumber(int position, String projectRelativePath)
			throws Exception {
		Path fullPath = new Path(projectName + "/" + projectRelativePath);
		return TestSourceReader.getLineNumber(position, fullPath);
	}

	public void assertDefDeclRef(String name, String testNum, int def,
			int decl, int ref) throws Exception {
		String elName = name + testNum;
		IBinding binding = findSingleBinding(elName);
		checkDefinition(binding, "def" + testNum, def);
		checkDeclaration(binding, "decl" + testNum, decl);
		checkReference(binding, "ref" + testNum, ref);
	}

	private IIndexFile getSingleFile(IIndexFileLocation ifl) throws CoreException {
		IIndexFile[] files= pdom.getFiles(ILinkage.C_LINKAGE_ID, ifl);
		assertEquals(1, files.length);
		return files[0];
	}
	/* ------------------ Tests Started Here ------------------------ */
	public void testInit() {
		// will fail if setUp fails, maybe timelimit is too small for warm-up
	}

	public void testSimpleDeclUsage_f01() throws Exception {
		assertDefDeclRef("foo", "01", 0, 1, 1);
	}

	public void testKRDeclUsage_f02() throws Exception {
		assertDefDeclRef("foo", "02", 0, 1, 1);
	}

	public void testImplicitDeclPostDecl_f03() throws Exception {
		assertDefDeclRef("foo", "03", 0, 1, 1);
	}

	public void testImplicitDeclPostDef_f04() throws Exception {
		assertDefDeclRef("foo", "04", 1, 0, 1);
	}

	public void testImplicitDeclNone_f05() throws Exception {
		assertDefDeclRef("foo", "05", 0, 0, 1);
	}

	public void testNonLocalDefintion_f06() throws Exception {
		assertDefDeclRef("foo", "06", 1, 1, 1);
	}

	public void testWrongMatchedStaticDefinition() throws Exception {
		String elName = "foo" + "07";
		IIndexBinding[] binds = pdom.findBindings(Pattern.compile(elName), true,	IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(2, binds.length);
		assertTrue(binds[0].isFileLocal() != binds[1].isFileLocal());
		if (binds[0].isFileLocal()) {
			IIndexBinding b= binds[0]; binds[0]= binds[1]; binds[1]= b;
		}
			
		assertEquals(elName, binds[0].getName());
		checkDefinition(binds[0], "def" + "07", 0);
		checkDeclaration(binds[0], "decl" + "07", 1);
		checkReference(binds[0], "ref" + "07", 1);

		assertEquals(elName, binds[1].getName());
		assertTrue(binds[1].getLocalToFile().getLocation().getFullPath().endsWith("second.c"));
		checkDefinition(binds[1], "def" + "07", 1);
		checkDeclaration(binds[1], "decl" + "07", 0);
		checkReference(binds[1], "ref" + "07", 0);
	}

	public void testStaticBindings_f08() throws Exception {
		String elName = "foo" + "08";

		IIndexFileLocation ifl= IndexLocationFactory.getIFL((ITranslationUnit) cproject.findElement(new Path("func.c")));
		IIndexFile file= getSingleFile(ifl);
		int offset= TestSourceReader.indexOfInFile("foo08();", new Path(ifl.getFullPath()));
		IIndexName[] names= file.findNames(offset, 5);
		assertEquals(1, names.length);
		
		IBinding element = pdom.findBinding((IIndexFragmentName)names[0]);
		assertEquals(elName, element.getName());
		checkDefinition(element, "def" + "08", 1);
		checkReference(element, "ref" + "08", 1);

		// check the other file
		ifl= IndexLocationFactory.getIFL((ITranslationUnit) cproject.findElement(new Path("second.c")));
		file= getSingleFile(ifl);
		offset= TestSourceReader.indexOfInFile("foo08();", new Path(ifl.getFullPath()));
		names= file.findNames(offset, 5);
		assertEquals(1, names.length);
		
		element = pdom.findBinding((IIndexFragmentName)names[0]);
		assertEquals(elName, element.getName());
		checkDefinition(element, "defS" + "08", 1);
		checkReference(element, "refS" + "08", 1);
	}

	public void testSimpleGlobalWrite_v09() throws Exception {
		assertDefDeclRef("var", "_v09", 1, 0, 1);
	}

	public void testGlobalInitRead_v10() throws Exception {
		assertDefDeclRef("var", "_v10", 1, 0, 1);
	}

	public void testGlobalInitRead2_v11() throws Exception {
		assertDefDeclRef("var", "_v11", 1, 0, 1);
	}

	public void testDeclUseDef_v12() throws Exception {
		assertDefDeclRef("var", "_v12", 1, 1, 1);
	}

	public void testDeclDefUse_v13() throws Exception {
		assertDefDeclRef("var", "_v13", 1, 1, 1);
	}

	public void testDefDeclUse_v14() throws Exception {
		// Hmm. This test seems to work, but Find Declaration in the UI does not
		// work
		assertDefDeclRef("var", "_v14", 1, 1, 1);
	}

	public void testNamedStruct_t01() throws Exception {
		assertDefDeclRef("type", "_t01", 1, 0, 1);
	}

	public void testStructPreDefintion_t02() throws Exception {
		assertDefDeclRef("type", "_t02", 0, 1, 1);
	}

	public void testStructRecursive_t03() throws Exception {
		assertDefDeclRef("type", "_t03", 1, 1, 1);
	}

	public void testStructAndTypedef_t04() throws Exception {
		String num = "_t04";
		String elName = "type" + num;
		
		IBinding[] bindings = pdom.findBindings(Pattern.compile(elName), false, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(2,bindings.length);
		
		IBinding typedef = bindings[0] instanceof ITypedef ? bindings[0] : bindings[1];
		IBinding struct = bindings[0] instanceof ICompositeType ? bindings[0] : bindings[1];
		
		checkReference(typedef, "ref" + num, 1);
		checkDefinition(typedef, "def" + num, 1);
		
		checkReference(struct, "refS" + num, 1);
		checkDefinition(struct, "defS" + num, 1);
	}

	public void testTypedefAndAnonymousStruct_t05() throws Exception {
		assertDefDeclRef("type", "_t05", 1, 0, 1);
	}
}
