/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;
import java.util.regex.Pattern;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
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

	public static Test suite() {
		return suite(DefDeclTests.class);
	}

	protected void setUp() throws Exception {
		String requiredName = "defDeclTests";
		ICProject cproject = createProject(requiredName);
		this.projectName = cproject.getElementName();
		pdom = (PDOM) CCorePlugin.getPDOMManager().getPDOM(cproject);
		pdom.acquireReadLock();
	}

	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
	}

	private IBinding findSingleBinding(String elName) throws CoreException {
		IBinding[] binds = pdom.findBindings(Pattern.compile(elName), true,
				new IndexFilter(), new NullProgressMonitor());
		assertEquals(1, binds.length);
		assertEquals(elName, binds[0].getName());
		IBinding element = binds[0];
		return element;
	}

	private void checkReference(IBinding element, String mark, int checkCount)
			throws CoreException, BadLocationException {
		checkUsage(element, mark, checkCount, IIndex.FIND_REFERENCES);
	}

	private void checkDeclaration(IBinding element, String mark, int num)
			throws CoreException, BadLocationException {
		checkUsage(element, mark, num, IIndex.FIND_DECLARATIONS);
	}

	private void checkDefinition(IBinding element, String mark, int countNum)
			throws CoreException, BadLocationException {
		checkUsage(element, mark, countNum, IIndex.FIND_DEFINITIONS);
	}

	private void checkUsage(IBinding element, String mark, int countNum,
			int flags) throws CoreException, BadLocationException {
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
			throws CoreException, BadLocationException {
		String fileName = new File(loc.getFileName()).getName();
		int markLine = getMarkLine(mark, fileName);
		int nodeLine = getLineNumber(loc.getNodeOffset(), fileName);
		assertEquals(markLine, nodeLine);
	}

	private int getMarkLine(String mark, String fileName) throws CoreException,
			BadLocationException {
		int markLine = getLineNumber(offset(fileName, mark), fileName);
		return markLine;
	}

	protected int getLineNumber(int position, String projectRelativePath)
			throws CoreException {
		Path fullPath = new Path(projectName + "/" + projectRelativePath);
		ITextFileBufferManager fbm = FileBuffers.getTextFileBufferManager();
		fbm.connect(fullPath, new NullProgressMonitor());
		try {
			ITextFileBuffer buf = FileBuffers.getTextFileBufferManager()
					.getTextFileBuffer(fullPath);
			Assert.assertTrue("Could not find " + fullPath.toString(), buf
					.getModificationStamp() > 0);
			String content = buf.getDocument().get();
			int len = content.length();
			int line = 1;
			for (int i = 0; i < len && i < position; i++) {
				char c = content.charAt(i);
				if (c == '\n')
					line++;
			}
			return line;
		} finally {
			fbm.disconnect(fullPath, new NullProgressMonitor());
		}
	}

	public void assertDefDeclRef(String name, String testNum, int def,
			int decl, int ref) throws CoreException, BadLocationException {
		String elName = name + testNum;
		IBinding binding = findSingleBinding(elName);
		checkDefinition(binding, "def" + testNum, def);
		checkDeclaration(binding, "decl" + testNum, decl);
		checkReference(binding, "ref" + testNum, ref);
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

	public void testWrongMatchedStaticDefinition_unexpected() throws Exception {
		assertDefDeclRef("foo", "07", 1, 1, 1);
	}

	public void testStaticBindings_f08_unexpected() throws Exception {
		// should be 2 bindings, otherwise how to distinguish proper def/decl
		// pairs?
		// static elements cannot be found on global scope, see bug 161216
		String elName = "foo" + "08";
		IBinding[] binds = pdom.findBindings(Pattern.compile(elName), true,
				IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(0, binds.length);
//		assertEquals(elName, binds[0].getName());
//		IBinding element = binds[0];
//		IBinding binding = element;
//		checkDefinition(binding, "def" + "08", 2);
//		checkReference(binding, "ref" + "08", 2);
//		checkDefinition(binding, "defS" + "08", 2);
//		checkReference(binding, "refS" + "08", 2);
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

	public void _testStructAndTypedef_t04_unexpected() throws Exception {
		String num = "_t04";
		String elName = "type" + num;
		
		IBinding[] bindings = pdom.findBindings(Pattern.compile(elName), false, new IndexFilter(), new NullProgressMonitor());
		assertEquals(2,bindings.length);
		
		IBinding typedef = bindings[0] instanceof ITypedef ? bindings[0] : bindings[1];
		IBinding struct = bindings[0] instanceof ICompositeType ? bindings[0] : bindings[1];
		
		checkReference(typedef, "ref" + num, 1);
		checkDeclaration(typedef, "def" + num, 1);
		
		checkReference(struct, "refS" + num, 1);
		checkDefinition(struct, "defS" + num, 1);
	}

	public void testTypedefAndAnonymousStruct_t05() throws Exception {
		assertDefDeclRef("type", "_t05", 1, 0, 1);
	}
}
