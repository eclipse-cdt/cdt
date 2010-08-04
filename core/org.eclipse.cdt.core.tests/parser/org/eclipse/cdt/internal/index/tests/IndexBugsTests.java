/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *    Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.index.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.pdom.CModelListener;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class IndexBugsTests extends BaseTestCase {
	private static final int INDEX_WAIT_TIME = 8000;
	private ICProject fCProject;
	protected IIndex fIndex;

	public IndexBugsTests(String name) {
		super(name);
	}

	protected class BindingAssertionHelper {
		protected IASTTranslationUnit tu;
		protected String contents;
    	
    	public BindingAssertionHelper(IFile file, String contents, IIndex index) throws CModelException, CoreException {
    		this.contents= contents;
    		this.tu= TestSourceReader.createIndexBasedAST(index, fCProject, file);
		}
    	
    	public IASTTranslationUnit getTranslationUnit() {
    		return tu;
    	}
    	
    	public IBinding assertProblem(String section, int len) {
    		IBinding binding= binding(section, len);
    		assertTrue("Non-ProblemBinding for name: " + section.substring(0, len),
    				binding instanceof IProblemBinding);
    		return binding;
    	}
    	
    	public <T extends IBinding> T assertNonProblem(String section, int len) {
    		IBinding binding= binding(section, len);
    		if (binding instanceof IProblemBinding) {
    			IProblemBinding problem= (IProblemBinding) binding;
    			fail("ProblemBinding for name: " + section.substring(0, len) + " (" + renderProblemID(problem.getID())+")"); 
    		}
    		if (binding == null) {
    			fail("Null binding resolved for name: " + section.substring(0, len));
    		}
    		return (T) binding;
    	}

    	public void assertNoName(String section, int len) {
			IASTName name= findName(section,len,false);
			if (name != null) {
				String selection = section.substring(0, len);
				fail("Found unexpected \"" + selection + "\": " + name.resolveBinding());
			}
    	}

    	/**
    	 * Asserts that there is exactly one name at the given location and that
    	 * it resolves to the given type of binding.
    	 */
    	public IASTImplicitName assertImplicitName(String section, int len, Class<?> bindingClass) {
    		IASTName name = findName(section,len,true);
    		final String selection = section.substring(0, len);
			assertNotNull("did not find \""+selection+"\"", name);
			
			assertInstance(name, IASTImplicitName.class);
			IASTImplicitNameOwner owner = (IASTImplicitNameOwner) name.getParent();
			IASTImplicitName[] implicits = owner.getImplicitNames();
			assertNotNull(implicits);
			
			if (implicits.length > 1) {
				boolean found = false;
				for (IASTImplicitName n : implicits) {
					if (((ASTNode)n).getOffset() == ((ASTNode)name).getOffset()) {
						assertFalse(found);
						found = true;
					}
				}
				assertTrue(found);
			}
			
    		assertEquals(selection, name.getRawSignature());
    		IBinding binding = name.resolveBinding();
    		assertNotNull(binding);
    		assertInstance(binding, bindingClass);
    		return (IASTImplicitName) name;
    	}
    	
    	public void assertNoImplicitName(String section, int len) {
    		IASTName name = findName(section,len,true);
    		final String selection = section.substring(0, len);
    		assertNull("found name \""+selection+"\"", name);
    	}
    	
    	public IASTImplicitName[] getImplicitNames(String section, int len) {
    		IASTName name = findName(section,len,true);
    		IASTImplicitNameOwner owner = (IASTImplicitNameOwner) name.getParent();
			IASTImplicitName[] implicits = owner.getImplicitNames();
			return implicits;
    	}
    	
    	private IASTName findName(String section, int len, boolean implicit) {
    		final int offset = contents.indexOf(section);
    		assertTrue(offset >= 0);
    		IASTNodeSelector selector = tu.getNodeSelector(null);
    		return implicit ? selector.findImplicitName(offset, len) : selector.findName(offset, len);
    	}

    	private String renderProblemID(int i) {
    		try {
    			for (Field field : IProblemBinding.class.getDeclaredFields()) {
    				if (field.getName().startsWith("SEMANTIC_")) {
    					if (field.getType() == int.class) {
    						Integer ci= (Integer) field.get(null);
    						if (ci.intValue() == i) {
    							return field.getName();
    						}
    					}
    				}
    			}
    		} catch(IllegalAccessException iae) {
    			throw new RuntimeException(iae);
    		}
    		return "Unknown problem ID";
    	}
    	
    	public <T extends IBinding> T assertNonProblem(String section, int len, Class<T> type, Class... cs) {
    		IBinding binding= binding(section, len);
    		assertTrue("ProblemBinding for name: " + section.substring(0, len),
    				!(binding instanceof IProblemBinding));
    		assertInstance(binding, type);
    		for (Class c : cs) {
    			assertInstance(binding, c);
    		}
    		return type.cast(binding);
    	}
    	
    	private IBinding binding(String section, int len) {
    		IASTName name = findName(section, len,false);
    		final String selection = section.substring(0, len);
			assertNotNull("Did not find \"" + selection + "\"", name);
    		assertEquals(selection, name.getRawSignature());
    			
    		IBinding binding = name.resolveBinding();
    		assertNotNull("No binding for " + name.getRawSignature(), binding);
    		
    		return name.resolveBinding();
    	}
    }

	public static TestSuite suite() {
		final TestSuite ts = suite(IndexBugsTests.class);
		ts.addTest(Bug246129.suite());
		return ts;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCCProject("__bugsTest__", "bin", IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().reindex(fCProject);
		waitForIndexer();
		fIndex= CCorePlugin.getIndexManager().getIndex(fCProject);
	}
	
	@Override
	protected void tearDown() throws Exception {
		if (fCProject != null) {
			CProjectHelper.delete(fCProject);
		}
		super.tearDown();
	}
	
	protected IProject getProject() {
		return fCProject.getProject();
	}
	
    protected String[] getContentsForTest(int blocks) throws IOException {
    	StringBuffer[] help= TestSourceReader.getContentsForTest(
    			CTestPlugin.getDefault().getBundle(), "parser", getClass(), getName(), blocks);
    	String[] result= new String[help.length];
    	int i= 0;
    	for (StringBuffer buf : help) {
			result[i++]= buf.toString();
		}
    	return result;
    }
    
    protected IFile createFile(IContainer container, String fileName, String contents) throws Exception {
    	return TestSourceReader.createFile(container, new Path(fileName), contents);
    }

	private void waitForIndexer() throws InterruptedException {
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		assertTrue(indexManager.joinIndexer(INDEX_WAIT_TIME, npm()));
		long waitms= 1;
		while (waitms < 2000 && indexManager.isIndexerSetupPostponed(fCProject)) {
			Thread.sleep(waitms);
			waitms *= 2;
		}
		assertTrue(indexManager.joinIndexer(INDEX_WAIT_TIME, npm()));
	}

	protected Pattern[] getPattern(String qname) {
		String[] parts= qname.split("::");
		Pattern[] result= new Pattern[parts.length];
		for (int i = 0; i < result.length; i++) {
			result[i]= Pattern.compile(parts[i]);			
		}
		return result;
	}

	protected void waitUntilFileIsIndexed(IFile file, int time) throws Exception {
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, time);
	}

	/**
	 * Attempts to get an IBinding from the initial specified number of characters
	 * from the specified code fragment. Fails the test if
	 * <ul>
	 *  <li> There is not a unique name with the specified criteria
	 *  <li> The binding associated with the name is null or a problem binding
     *  <li> The binding is not an instance of the specified class
	 * </ul>
	 * @param ast the AST to test.
	 * @param source the source code corresponding to the AST.
	 * @param section the code fragment to search for in the AST. The first occurrence of
	 *   an identical section is used.
	 * @param len the length of the specified section to use as a name. This can also be useful
	 *   for distinguishing between template names, and template ids.
	 * @param clazz an expected class type or interface that the binding should extend/implement
	 * @return the associated name's binding
	 */
	protected <T> T getBindingFromASTName(IASTTranslationUnit ast, String source, String section, int len,
			Class<T> clazz, Class... cs) {
		IASTName name= ast.getNodeSelector(null).findName(source.indexOf(section), len);
		assertNotNull("Name not found for \"" + section + "\"", name);
		assertEquals(section.substring(0, len), name.getRawSignature());
		
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding for " + section.substring(0, len), binding);
		assertTrue("ProblemBinding for name: " + section.substring(0, len),
				!(binding instanceof IProblemBinding));
		assertInstance(binding, clazz, cs);
		return clazz.cast(binding);
	}

	protected static <T> T assertInstance(Object o, Class<T> clazz, Class ... cs) {
		assertNotNull("Expected " + clazz.getName() + " but got null", o);
		assertTrue("Expected " + clazz.getName() + " but got " + o.getClass().getName(), clazz.isInstance(o));
		for (Class c : cs) {
			assertTrue("Expected " + clazz.getName() + " but got " + o.getClass().getName(), c.isInstance(o));
		}
		return clazz.cast(o);
	}

	// class A {
	// public:
	//   void one() {}
	//   void two() {}
	// };	

	// class A {
	// public:
	//   void three() {}
	//   void four() {}
	//   void five() {}
	// };
	public void test154563() throws Exception {
		// Because of fix for http://bugs.eclipse.org/193779 this test case passes.
		// However http://bugs.eclipse.org/154563 remains to be fixed.
		String[] content= getContentsForTest(2);
		
		IFile file= createFile(getProject(), "header.h", content[0]);
		waitUntilFileIsIndexed(file, INDEX_WAIT_TIME);
		
		IIndex index= CCorePlugin.getIndexManager().getIndex(fCProject);
		index.acquireReadLock();
		try {
			IBinding[] bs= index.findBindings("A".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bs.length); 
			assertTrue(bs[0] instanceof ICPPClassType);
			assertEquals(2, ((ICPPClassType)bs[0]).getDeclaredMethods().length);
		} finally {
			index.releaseReadLock();
		}
		
		file= createFile(getProject(), "header.h", content[1]);
		waitUntilFileIsIndexed(file, INDEX_WAIT_TIME);
		
		index= CCorePlugin.getIndexManager().getIndex(fCProject);
		index.acquireReadLock();
		try {
			IBinding[] bs= index.findBindings("A".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bs.length); 
			assertTrue(bs[0] instanceof ICPPClassType);
			assertEquals(3, ((ICPPClassType)bs[0]).getDeclaredMethods().length);
		} finally {
			index.releaseReadLock();
		}
	}

    public void test150906() throws Exception {
    	String fileName= "bug150906.c";
    	String varName= "arrayDataSize";
    	StringBuffer content= new StringBuffer();
    	content.append("unsigned char arrayData[] = {\n");
    	for (int i= 0; i< 1024 * 250 - 1; i++) {
    		content.append("0x00,");
    	}
    	content.append("0x00};\n");
    	content.append("unsigned int arrayDataSize = sizeof(arrayData);\n");
		int indexOfDecl = content.indexOf(varName);

		assertTrue(CCorePlugin.getIndexManager().joinIndexer(INDEX_WAIT_TIME, npm()));
		IFile file= createFile(getProject(), fileName, content.toString());
		// must be done in a reasonable amount of time
		waitUntilFileIsIndexed(file, INDEX_WAIT_TIME);
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings= fIndex.findBindings(getPattern("arrayDataSize"), true, IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			
			IIndexBinding binding= bindings[0];
			
			// check if we have the definition
			IIndexName[] decls= fIndex.findNames(binding, IIndex.FIND_DEFINITIONS);
			assertEquals(1, decls.length);
			assertEquals(indexOfDecl, decls[0].getNodeOffset());
		} finally {
			fIndex.releaseReadLock();
		}
    }

	// enum {e20070206};
	public void test156671() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0];

		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test156671.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IBinding[] bindings= fIndex.findBindings("e20070206".toCharArray(), IndexFilter.getFilter(ILinkage.CPP_LINKAGE_ID), npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof IEnumerator);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	public void test160281_1() throws Exception {
		waitForIndexer();
		IFile include= TestSourceReader.createFile(fCProject.getProject(), "inc/test160281_1.h", "");
		TestScannerProvider.sIncludes= new String[]{include.getLocation().removeLastSegments(1).toString()};
		TestScannerProvider.sIncludeFiles= new String[]{include.getName()};
		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test160281_1.cpp", "");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull(ifile);
			IIndexInclude[] includes= ifile.getIncludes();
			assertEquals(1, includes.length);
			IIndexInclude i= includes[0];
			assertEquals(file.getLocationURI(), i.getIncludedByLocation().getURI());
			assertEquals(include.getLocationURI(), i.getIncludesLocation().getURI());
			assertEquals(true, i.isSystemInclude());
			assertEquals(0, i.getNameOffset());
			assertEquals(0, i.getNameLength());
		} finally {
			fIndex.releaseReadLock();
		}
	}

	public void test160281_2() throws Exception {
		waitForIndexer();
		IFile include= TestSourceReader.createFile(fCProject.getProject(), "inc/test160281_2.h", "#define X y\n");
		TestScannerProvider.sIncludes= new String[]{include.getLocation().removeLastSegments(1).toString()};
		TestScannerProvider.sMacroFiles= new String[]{include.getName()};
		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test160281_2.cpp", "int X;");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull(ifile);
			IIndexInclude[] includes= ifile.getIncludes();
			assertEquals(1, includes.length);
			IIndexInclude i= includes[0];
			assertEquals(file.getLocationURI(), i.getIncludedByLocation().getURI());
			assertEquals(include.getLocationURI(), i.getIncludesLocation().getURI());
			assertEquals(true, i.isSystemInclude());
			assertEquals(0, i.getNameOffset());
			assertEquals(0, i.getNameLength());
			IIndexBinding[] bindings= fIndex.findBindings("y".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof IVariable);
		} finally {
			fIndex.releaseReadLock();
		}
	}

    //  namespace ns162011 {
    //    class Class162011 {
    //      friend void function162011(Class162011); 
    //    };
    //    void function162011(Class162011 x){};
    //  }
    public void test162011() throws Exception {
		String content = getContentsForTest(1)[0];
		String fileName = "bug162011.cpp";
		String funcName = "function162011";

		int indexOfDecl = content.indexOf(funcName);
		int indexOfDef  = content.indexOf(funcName, indexOfDecl+1);
		IFile file= createFile(getProject(), fileName, content);
		waitUntilFileIsIndexed(file, INDEX_WAIT_TIME);
		
		// make sure the ast is correct
		ITranslationUnit tu= (ITranslationUnit) fCProject.findElement(new Path(fileName));
		IASTTranslationUnit ast= tu.getAST();
		IASTName name= (IASTName) ast.getNodeSelector(null).findNode(indexOfDecl, funcName.length());
		IBinding astBinding= name.resolveBinding();

		IName[] astDecls= ast.getDeclarations(astBinding);
		assertEquals(2, astDecls.length);
		int i1= astDecls[0].getFileLocation().getNodeOffset();
		int i2= astDecls[1].getFileLocation().getNodeOffset();
		assertEquals(indexOfDecl, Math.min(i1, i2));
		assertEquals(indexOfDef, Math.max(i1, i2));

		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings= fIndex.findBindings(getPattern("ns162011::function162011"), true, IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			
			IIndexBinding binding= bindings[0];
			
			// check if we have the declaration
			IIndexName[] decls= fIndex.findNames(binding, IIndex.FIND_DECLARATIONS);
			assertEquals(1, decls.length);
			assertEquals(indexOfDecl, decls[0].getNodeOffset());

			// check if we have the definition
			decls= fIndex.findNames(binding, IIndex.FIND_DEFINITIONS);
			assertEquals(1, decls.length);
			assertEquals(indexOfDef, decls[0].getNodeOffset());
		} finally {
			fIndex.releaseReadLock();
		}
    }
	public void test164360_1() throws Exception {
		waitForIndexer();
		IFile include= TestSourceReader.createFile(fCProject.getProject(), "test164360.h", "");
		TestScannerProvider.sIncludeFiles= new String[]{include.getLocation().toOSString()};
		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test164360.cpp", "");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull(ifile);
			IIndexInclude[] includes= ifile.getIncludes();
			assertEquals(1, includes.length);
			IIndexInclude i= includes[0];
			assertEquals(file.getLocationURI(), i.getIncludedByLocation().getURI());
			assertEquals(include.getLocationURI(), i.getIncludesLocation().getURI());
			assertEquals(true, i.isSystemInclude());
			assertEquals(0, i.getNameOffset());
			assertEquals(0, i.getNameLength());
		} finally {
			fIndex.releaseReadLock();
		}
	}

	public void test164360_2() throws Exception {
		waitForIndexer();
		IFile include= TestSourceReader.createFile(fCProject.getProject(), "test164360.h", "");
		TestScannerProvider.sMacroFiles= new String[]{include.getLocation().toOSString()};
		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test164360.cpp", "");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull(ifile);
			IIndexInclude[] includes= ifile.getIncludes();
			assertEquals(1, includes.length);
			IIndexInclude i= includes[0];
			assertEquals(file.getLocationURI(), i.getIncludedByLocation().getURI());
			assertEquals(include.getLocationURI(), i.getIncludesLocation().getURI());
			assertEquals(true, i.isSystemInclude());
			assertEquals(0, i.getNameOffset());
			assertEquals(0, i.getNameLength());
		} finally {
			fIndex.releaseReadLock();
		}
	}

	// #define macro164500 1
	// #undef macro164500
	// #define macro164500 2
	public void test164500() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0];

		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test164500.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull(ifile);
			IIndexMacro[] macros= ifile.getMacros();
			assertEquals(3, macros.length);
			IIndexMacro m= macros[0];
			assertEquals("1", new String(m.getExpansionImage()));
			assertEquals("macro164500", new String(m.getName()));

			m= macros[2];
			assertEquals("2", new String(m.getExpansionImage()));
			assertEquals("macro164500", new String(m.getName()));
		} finally {
			fIndex.releaseReadLock();
		}
	}

	// // header.h
	// class E {};
	
	// #include "header.h"
	// E var;
	
	// // header.h	
	// enum E {A,B,C};
	public void test171834() throws Exception {
		CModelListener.sSuppressUpdateOfLastRecentlyUsed= false;
		waitForIndexer();

		ICProject cproject = CProjectHelper.createCCProject("seq1", "bin", IPDOMManager.ID_FAST_INDEXER);
		try {
			IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
			String[] testData = getContentsForTest(3);
			IFile header= TestSourceReader.createFile(cproject.getProject(), "header.h", testData[0]);
			IFile referer= TestSourceReader.createFile(cproject.getProject(), "content.cpp", testData[1]);
			TestSourceReader.waitUntilFileIsIndexed(index, referer, INDEX_WAIT_TIME);

			index.acquireReadLock();
			try {
				IBinding[] bindings = index.findBindings(Pattern.compile("var"), true, IndexFilter.ALL, new NullProgressMonitor());
				assertEquals(1, bindings.length);
				IType type = ((ICPPVariable)bindings[0]).getType();
				assertTrue(type instanceof ICPPClassType);
				assertEquals("var is not of type class", ICPPClassType.k_class, ((ICPPClassType)type).getKey());
			} finally {
				index.releaseReadLock();
			}

			InputStream in = new ByteArrayInputStream(testData[2].getBytes()); 
			header.setContents(in, IResource.FORCE, null);
			TestSourceReader.waitUntilFileIsIndexed(index, header, INDEX_WAIT_TIME);

			index.acquireReadLock();
			try {
				IBinding[] bindings = index.findBindings(Pattern.compile("var"), true, IndexFilter.ALL, new NullProgressMonitor());
				assertEquals(1, bindings.length);

				IType type = ((ICPPVariable)bindings[0]).getType();
				assertTrue(type instanceof IEnumeration);
			} finally {
				index.releaseReadLock();
			}
		} finally {
			CProjectHelper.delete(cproject);
		}
	}

	// typedef struct S20070201 {
	//    int a;
	// } S20070201;
	public void test172454_1() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0];

		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test172454.c", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IBinding[] bindings= fIndex.findBindings("S20070201".toCharArray(), IndexFilter.getFilter(ILinkage.C_LINKAGE_ID), npm());
			assertEquals(2, bindings.length);
			
			IBinding struct, typedef;
			if (bindings[0] instanceof ICompositeType) {
				struct= bindings[0];
				typedef= bindings[1];
			}
			else {
				struct= bindings[1];
				typedef= bindings[0];
			}
			
			assertTrue(struct instanceof ICompositeType);
			assertTrue(typedef instanceof ITypedef);
			assertTrue(((ITypedef) typedef).getType() instanceof ICompositeType);
			assertTrue(((ITypedef) typedef).isSameType((ICompositeType) struct));
		} finally {
			fIndex.releaseReadLock();
		}
	}

	// typedef struct S20070201 {
	//    int a;
	// } S20070201;
	public void test172454_2() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0];

		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test172454.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IBinding[] bindings= fIndex.findBindings("S20070201".toCharArray(), IndexFilter.getFilter(ILinkage.CPP_LINKAGE_ID), npm());
			assertEquals(2, bindings.length);
			
			IBinding struct, typedef;
			if (bindings[0] instanceof ICPPClassType) {
				struct= bindings[0];
				typedef= bindings[1];
			}
			else {
				struct= bindings[1];
				typedef= bindings[0];
			}
			
			assertTrue(struct instanceof ICPPClassType);
			assertTrue(((ICPPClassType)struct).getKey()==ICompositeType.k_struct);
			assertTrue(typedef instanceof ITypedef);
			IType aliased = ((ITypedef) typedef).getType();
			assertTrue(aliased instanceof ICPPClassType);
			assertTrue(((ICPPClassType)aliased).getKey()==ICompositeType.k_struct);
			assertTrue(((ITypedef) typedef).isSameType((ICompositeType) struct));
		} finally {
			fIndex.releaseReadLock();
		}
	}

	// typedef int T20070213;
	public void test173997() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0];

		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test173997.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IBinding[] bindings= fIndex.findBindings("T20070213".toCharArray(), IndexFilter.getFilter(ILinkage.CPP_LINKAGE_ID), npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof ITypedef);
			ITypedef td= (ITypedef) bindings[0];
			IType type= td.getType();
			assertTrue(type instanceof IBasicType);
			IBasicType btype= (IBasicType) type;
			assertEquals(IBasicType.t_int, btype.getType());
		} finally {
			fIndex.releaseReadLock();
		}
		
		long timestamp= file.getLocalTimeStamp();
		content= "int UPDATED20070213;\n" + content.replaceFirst("int", "float");
		file= TestSourceReader.createFile(fCProject.getProject(), "test173997.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			// double check if file was indexed
			IBinding[] bindings= fIndex.findBindings("UPDATED20070213".toCharArray(), IndexFilter.getFilter(ILinkage.CPP_LINKAGE_ID), npm());
			assertEquals(1, bindings.length);
			
			bindings= fIndex.findBindings("T20070213".toCharArray(), IndexFilter.getFilter(ILinkage.CPP_LINKAGE_ID), npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof ITypedef);
			ITypedef td= (ITypedef) bindings[0];
			IType type= td.getType();
			assertTrue(type instanceof IBasicType);
			IBasicType btype= (IBasicType) type;
			assertTrue(IBasicType.t_int != btype.getType());
			assertEquals(IBasicType.t_float, btype.getType());
		} finally {
			fIndex.releaseReadLock();
		}
	}

	// class A {};
	// class B {};
	// A var;
	
	// class A {};
	// class B {};
	// B var;
	public void test173997_2() throws Exception {
		String[] content= getContentsForTest(2);
		
		IFile file= createFile(getProject(), "header.h", content[0]);
		waitUntilFileIsIndexed(file, INDEX_WAIT_TIME);
		
		IIndex index= CCorePlugin.getIndexManager().getIndex(fCProject);
		index.acquireReadLock();
		try {
			IBinding[] bs= index.findBindings("var".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bs.length); 
			assertTrue(bs[0] instanceof ICPPVariable);
			assertTrue(((ICPPVariable)bs[0]).getType() instanceof ICPPClassType);
			assertEquals("A", ((ICPPClassType)(((ICPPVariable)bs[0]).getType())).getName());
		} finally {
			index.releaseReadLock();
		}
		
		file= createFile(getProject(), "header.h", content[1]);
		waitUntilFileIsIndexed(file, INDEX_WAIT_TIME);
		
		index= CCorePlugin.getIndexManager().getIndex(fCProject);
		index.acquireReadLock();
		try {
			IBinding[] bs= index.findBindings("var".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bs.length); 
			assertTrue(bs[0] instanceof ICPPVariable);
			assertTrue(((ICPPVariable)bs[0]).getType() instanceof ICPPClassType);
			assertEquals("B", ((ICPPClassType)(((ICPPVariable)bs[0]).getType())).getName());
		} finally {
			index.releaseReadLock();
		}
	}

	//  // header.h
	//	template <class T1> class Test {};
	//	template <class T2> void f() {}
	
	//  #include "header.h"
	//	struct A {};
	//	Test<A> a;
	//  void func() {
	//    f<A>();
	//  }

	//	template <class U1> class Test;
	//	template <class U2> void f();
	public void test253080() throws Exception {
		waitForIndexer();

		String[] testData = getContentsForTest(3);
		TestSourceReader.createFile(fCProject.getProject(), "header.h", testData[0]);
		IFile test= TestSourceReader.createFile(fCProject.getProject(), "test.cpp", testData[1]);
		TestSourceReader.createFile(fCProject.getProject(), "unrelated.cpp", testData[2]);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.reindex(fCProject);
		waitForIndexer();
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			IASTTranslationUnit ast = TestSourceReader.createIndexBasedAST(index, fCProject, test);
			getBindingFromASTName(ast, testData[1], "Test<A>", 7, ICPPTemplateInstance.class);
			getBindingFromASTName(ast, testData[1], "f<A>", 4, ICPPTemplateInstance.class);
		} finally {
			index.releaseReadLock();
		}
	}

	//	// test1.h
	//	template<class U> struct A {
	//	  typedef U value_type;
	//	};

	//	// test2.h
	//	#include "test1.h"
	//	template<class T> struct B {
	//	  typedef A<T> container_type;
	//	  typedef typename container_type::value_type value_type;
	//	};

	//	#include "test1.h"

	//	#include "test2.h"
	//	void f(int x);
	//	void test(B<int>::value_type x) {
	//	  f(x);
	//	}
	public void test257818_1() throws Exception {
		waitForIndexer();

		String[] testData = getContentsForTest(4);
		TestSourceReader.createFile(fCProject.getProject(), "test1.h", testData[0]);
		TestSourceReader.createFile(fCProject.getProject(), "test2.h", testData[1]);
		TestSourceReader.createFile(fCProject.getProject(), "test1.cpp", testData[2]);
		IFile test2= TestSourceReader.createFile(fCProject.getProject(), "test2.cpp", testData[3]);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.reindex(fCProject);
		waitForIndexer();
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			IASTTranslationUnit ast = TestSourceReader.createIndexBasedAST(index, fCProject, test2);
			getBindingFromASTName(ast, testData[3], "f(x)", 1, ICPPFunction.class);
		} finally {
			index.releaseReadLock();
		}
	}

	//	// test1.h
	//	template<class U> struct A {
	//	  typedef U value_type;
	//	};

	//	// test2.h
	//	#include "test1.h"
	//	template<class T> struct B {
	//	  typedef A<T> container_type;
	//	  typedef typename container_type::value_type value_type;
	//	  void m(value_type* p);
	//	};

	//	#include "test1.h"

	//	#include "test2.h"
	//	class C {};
	//	void test(B<C> x, C y) {
	//	  x.m(&y);
	//	}
	public void test257818_2() throws Exception {
		waitForIndexer();

		String[] testData = getContentsForTest(4);
		TestSourceReader.createFile(fCProject.getProject(), "test1.h", testData[0]);
		TestSourceReader.createFile(fCProject.getProject(), "test2.h", testData[1]);
		TestSourceReader.createFile(fCProject.getProject(), "test1.cpp", testData[2]);
		IFile test2= TestSourceReader.createFile(fCProject.getProject(), "test2.cpp", testData[3]);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.reindex(fCProject);
		waitForIndexer();
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			IASTTranslationUnit ast = TestSourceReader.createIndexBasedAST(index, fCProject, test2);
			getBindingFromASTName(ast, testData[3], "m(&y)", 1, ICPPMethod.class);
		} finally {
			index.releaseReadLock();
		}
	}

	// class A {}; class B {}; class C {};
	public void testIndexContentOverProjectDelete() throws Exception {
		waitForIndexer();

		/* Check that when a project is deleted, its index contents do not
         * appear in the initial index of a newly created project of the same name */
         
		String pname = "deleteTest"+System.currentTimeMillis();
		ICProject cproject = CProjectHelper.createCCProject(pname, "bin", IPDOMManager.ID_FAST_INDEXER);
		IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
		String content= getContentsForTest(1)[0];
		IFile file= TestSourceReader.createFile(cproject.getProject(), "content.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(index, file, INDEX_WAIT_TIME);
		CProjectHelper.delete(cproject);

		cproject = CProjectHelper.createCCProject(pname, "bin", IPDOMManager.ID_FAST_INDEXER);
		index = CCorePlugin.getIndexManager().getIndex(cproject);
		index.acquireReadLock();
		try {
			IBinding[] bindings = index.findBindings(Pattern.compile(".*"), false, IndexFilter.ALL, new NullProgressMonitor());
			assertEquals(0, bindings.length);
		} finally {
			index.releaseReadLock();
			CProjectHelper.delete(cproject);
		}
	}

	// class A {}; class B {}; class C {}; class D {};
	public void testIndexContentOverProjectMove() throws Exception {
		waitForIndexer();

		/* Check that the contents of an index is preserved over a project
         * move operation */

		ICProject cproject = CProjectHelper.createCCProject("moveTest", "bin", IPDOMManager.ID_FAST_INDEXER);
		IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
		String content= getContentsForTest(1)[0];
		IFile file= TestSourceReader.createFile(cproject.getProject(), "content.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(index, file, INDEX_WAIT_TIME);

		// move the project to a random new location
		File newLocation = CProjectHelper.freshDir();
		IProjectDescription description = cproject.getProject().getDescription();
		description.setLocationURI(newLocation.toURI());
		cproject.getProject().move(description, IResource.FORCE | IResource.SHALLOW, new NullProgressMonitor());	
		
		index = CCorePlugin.getIndexManager().getIndex(cproject);
		index.acquireReadLock();
		try {
			IBinding[] bindings = index.findBindings(Pattern.compile(".*"), false, IndexFilter.ALL_DECLARED, new NullProgressMonitor());
			assertEquals(4, bindings.length);
		} finally {
			index.releaseReadLock();
			CProjectHelper.delete(cproject);
		}
	}

	// class a {};
	// class A {};
	// namespace aa {
	//   class a {
	//     class e {
	//      class AA {class A{};};
	//     };
	//   };
	// };
	public void testFindBindingsWithPrefix() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0];

		IFile file= TestSourceReader.createFile(fCProject.getProject(), "testFBWP.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			final IndexFilter NON_FUNCTIONS = new IndexFilter() {
				@Override
				public boolean acceptBinding(IBinding binding) {
					return !(binding instanceof IFunction);
				}
			};
			
			IBinding[] bindings= fIndex.findBindingsForPrefix(new char[] {'a'}, true, NON_FUNCTIONS, null);
			assertEquals(3,bindings.length);
			
			bindings= fIndex.findBindingsForPrefix(new char[] {'a'}, false, NON_FUNCTIONS, null);
			assertEquals(6,bindings.length);
			
			bindings= fIndex.findBindingsForPrefix(new char[] {'a','A'}, true, NON_FUNCTIONS, null);
			assertEquals(1,bindings.length);
			
			bindings= fIndex.findBindingsForPrefix(new char[] {'a','A'}, false, NON_FUNCTIONS, null);
			assertEquals(2, bindings.length);
		} finally {
			fIndex.releaseReadLock();
		}		
	}
	
	// class a { class b { class c { void f(); }; }; };
	public void testFilterFindBindingsFQCharArray() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0];

		IFile file= TestSourceReader.createFile(fCProject.getProject(), "testFilterFindBindingsFQCharArray.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			final IndexFilter NON_CLASS = new IndexFilter() {
				@Override
				public boolean acceptBinding(IBinding binding) {
					return !(binding instanceof ICPPClassType);
				}
			};
			
			IBinding[] bindings= fIndex.findBindings(new char[][]{{'a'},{'b'},{'c'},{'f'}}, NON_CLASS, npm());
			assertEquals(1,bindings.length);
		} finally {
			fIndex.releaseReadLock();
		}		
	}
	
    // typedef struct {
    //    float   fNumber;
    //    int     iIdx;
    // } StructA_T;
	
	// #include "../__bugsTest__/common.h"
	// StructA_T gvar1;
	
	// #include "../__bugsTest__/common.h"
	// StructA_T gvar2;
	public void testFileInMultipleFragments_bug192352() throws Exception {
		String[] contents= getContentsForTest(3);
		
		ICProject p2 = CProjectHelper.createCCProject("__bugsTest_2_", "bin", IPDOMManager.ID_FAST_INDEXER);
		try {
			IFile f1= TestSourceReader.createFile(fCProject.getProject(), "common.h", contents[0]);
			IFile f2= TestSourceReader.createFile(fCProject.getProject(), "src.cpp", contents[1]);
			IFile f3= TestSourceReader.createFile(p2.getProject(), "src.cpp", contents[2]);
			waitForIndexer();

			IIndex index= CCorePlugin.getIndexManager().getIndex(new ICProject[]{fCProject, p2});
			index.acquireReadLock();
			try {
				IIndexBinding[] bindings= index.findBindings("StructA_T".toCharArray(), IndexFilter.ALL, npm());
				assertEquals(1, bindings.length);
				IIndexBinding binding= bindings[0];
				IIndexName[] names= index.findReferences(binding);
				assertEquals(2, names.length);
				names= index.findDeclarations(binding);
				assertEquals(1, names.length);
			} finally {
				index.releaseReadLock();
			}
		} finally {
			CProjectHelper.delete(p2);
		}
	}
	
	// #ifndef _h1
	// #define _h1
	// #define M v
	// #endif
	
	// #ifndef _h1
	// #include "header1.h"
	// #endif
	
	// #include "header1.h"
	// #include "header2.h"
	
	// #include "header2.h"
	// int M;
	
	// #include "header2.h"
	// #ifndef _h1
	// #include "header1.h"
	// #endif
	public void testIncludeGuardsOutsideOfHeader_Bug167100() throws Exception {
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		String[] contents= getContentsForTest(5);
		IFile f1= TestSourceReader.createFile(fCProject.getProject(), "header1.h", contents[0]);
		IFile f2= TestSourceReader.createFile(fCProject.getProject(), "header2.h", contents[1]);
		IFile f3= TestSourceReader.createFile(fCProject.getProject(), "src.cpp", contents[2]);
		indexManager.reindex(fCProject);
		waitForIndexer();
		IFile f4= TestSourceReader.createFile(fCProject.getProject(), "src2.cpp", contents[3]);
		IFile f5= TestSourceReader.createFile(fCProject.getProject(), "src3.cpp", contents[4]);
		waitForIndexer();
		
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			IIndexBinding[] bindings = index.findBindings("v".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			IIndexBinding binding = bindings[0];
			assertTrue(binding instanceof IVariable);
			IIndexName[] names = index.findNames(binding,
					IIndex.FIND_ALL_OCCURRENCES);
			assertEquals(1, names.length);
			assertEquals(f4.getFullPath().toString(), names[0].getFile().getLocation().getFullPath());
			
			IIndexFile idxFile= index.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(f5));
			IIndexInclude[] includes= idxFile.getIncludes();
			assertEquals(2, includes.length);
			assertTrue(includes[0].isActive());
			assertTrue(includes[0].isResolved());
			assertFalse(includes[1].isActive());
			assertTrue(includes[1].isResolved());
		} finally {
			index.releaseReadLock();
		}
	}
	
	
	// #define MAC(...) Bug200239
	
	// #include "header.h"
	// int MAC(1);
	// void func() {
	//    MAC()= MAC(1) + MAC(1,2);
	// }
	public void testVariadicMacros_Bug200239_1() throws Exception {
		String[] contents= getContentsForTest(2);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		IFile f1= TestSourceReader.createFile(fCProject.getProject(), "header.h", contents[0]);
		waitUntilFileIsIndexed(f1, INDEX_WAIT_TIME);
		IFile f2= TestSourceReader.createFile(fCProject.getProject(), "src.cpp", contents[1]);
		waitForIndexer();

		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings= fIndex.findBindings("Bug200239".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			IIndexName[] refs= fIndex.findReferences(bindings[0]);
			assertEquals(3, refs.length);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	// #define GMAC(x...) Bug200239
	
	// #include "header.h"
	// int GMAC(1);
	// void func() {
	//    GMAC()= GMAC(1) + GMAC(1,2);
	// }
	public void testVariadicMacros_Bug200239_2() throws Exception {
		String[] contents= getContentsForTest(2);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		IFile f1= TestSourceReader.createFile(fCProject.getProject(), "header.h", contents[0]);
		waitUntilFileIsIndexed(f1, INDEX_WAIT_TIME);
		IFile f2= TestSourceReader.createFile(fCProject.getProject(), "src.cpp", contents[1]);
		waitForIndexer();

		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings= fIndex.findBindings("Bug200239".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			IIndexName[] refs= fIndex.findReferences(bindings[0]);
			assertEquals(3, refs.length);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	
	// typedef bug200553_A bug200553_B;
	// typedef bug200553_B bug200553_A;
	public void testTypedefRecursionCpp_Bug200553() throws Exception {
		String[] contents= getContentsForTest(1);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		IFile f1= TestSourceReader.createFile(fCProject.getProject(), "src.cpp", contents[0]);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings= fIndex.findBindings("bug200553_A".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof ITypedef);
			checkTypedefDepth((ITypedef) bindings[0]);

			bindings= fIndex.findBindings("bug200553_B".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof ITypedef);
			checkTypedefDepth((ITypedef) bindings[0]);
		} finally {
			fIndex.releaseReadLock();
		}

		indexManager.update(new ICElement[] {fCProject}, IIndexManager.UPDATE_ALL);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings= fIndex.findBindings("bug200553_A".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof ITypedef);
			checkTypedefDepth((ITypedef) bindings[0]);

			bindings= fIndex.findBindings("bug200553_B".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof ITypedef);
			checkTypedefDepth((ITypedef) bindings[0]);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	private void checkTypedefDepth(ITypedef td) throws DOMException {
		int maxDepth= 20;
		IType type= td;
		while (--maxDepth > 0 && type instanceof ITypedef) {
			type= ((ITypedef) type).getType();
		}
		assertTrue(maxDepth > 0);
	}
	
	// typedef bug200553_A bug200553_B;
	// typedef bug200553_B bug200553_A;
	public void testTypedefRecursionC_Bug200553() throws Exception {
		String[] contents= getContentsForTest(1);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		IFile f1= TestSourceReader.createFile(fCProject.getProject(), "src.c", contents[0]);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings= fIndex.findBindings("bug200553_A".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof ITypedef);
			checkTypedefDepth((ITypedef) bindings[0]);

			bindings= fIndex.findBindings("bug200553_B".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof ITypedef);
			checkTypedefDepth((ITypedef) bindings[0]);
		} finally {
			fIndex.releaseReadLock();
		}

		indexManager.update(new ICElement[] {fCProject}, IIndexManager.UPDATE_ALL);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings= fIndex.findBindings("bug200553_A".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof ITypedef);
			checkTypedefDepth((ITypedef) bindings[0]);

			bindings= fIndex.findBindings("bug200553_B".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof ITypedef);
			checkTypedefDepth((ITypedef) bindings[0]);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	// #ifndef GUARD
	// #include "source.cpp"
	// #endif
	public void testIncludeSource_Bug199412() throws Exception {
		String[] contents= getContentsForTest(1);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		IFile f1= TestSourceReader.createFile(fCProject.getProject(), "source.cpp", contents[0]);
		waitForIndexer();
		
		final ITranslationUnit tu= (ITranslationUnit) fCProject.findElement(new Path("source.cpp"));
		Thread th= new Thread() {
			@Override
			public void run() {
				try {
					tu.getAST(fIndex, ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT);
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}	
			}
		};
		fIndex.acquireReadLock();
		try {
			th.start();
			th.join(5000);
			assertFalse(th.isAlive());
		} finally {
			try {
				th.stop();
			} finally {
				fIndex.releaseReadLock();
			}
		}
	}

	// void func_209049(long long x);
	public void testGPPTypes_Bug209049() throws Exception {
		String[] contents= getContentsForTest(1);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		IFile f1= TestSourceReader.createFile(fCProject.getProject(), "source.cpp", contents[0]);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings = fIndex.findBindings("func_209049".toCharArray(),
					IndexFilter.ALL, npm());
			IFunctionType ft = ((IFunction) bindings[0]).getType();
			assertEquals("void (long long int)", ASTTypeUtil.getType(ft));
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	// static inline void staticInHeader() {};
	
	// #include "header.h"
	// void f1() {
	//    staticInHeader();
	// }
	public void testStaticFunctionsInHeader_Bug180305() throws Exception {
		String[] contents= getContentsForTest(2);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		TestSourceReader.createFile(fCProject.getProject(), "header.h", contents[0]);
		TestSourceReader.createFile(fCProject.getProject(), "source1.cpp", contents[1]);
		TestSourceReader.createFile(fCProject.getProject(), "source2.cpp", contents[1]);
		indexManager.reindex(fCProject);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings = fIndex.findBindings("staticInHeader".toCharArray(),
					IndexFilter.ALL, npm());
			IFunction func = (IFunction) bindings[0];
			assertTrue(func.isStatic());
			IIndexName[] refs = fIndex.findReferences(func);
			assertEquals(2, refs.length);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	// static const int staticConstInHeader= 12;
	
	// #include "header.h"
	// void f1() {
	//    int a= staticConstInHeader;
	// }
	public void testStaticVariableInHeader_Bug180305() throws Exception {
		String[] contents= getContentsForTest(2);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		TestSourceReader.createFile(fCProject.getProject(), "header.h", contents[0]);
		TestSourceReader.createFile(fCProject.getProject(), "source1.cpp", contents[1]);
		TestSourceReader.createFile(fCProject.getProject(), "source2.cpp", contents[1]);
		indexManager.reindex(fCProject);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings = fIndex.findBindings("staticConstInHeader".toCharArray(),
					IndexFilter.ALL, npm());
			IVariable var = (IVariable) bindings[0];
			assertTrue(var.isStatic());
			IIndexName[] refs = fIndex.findReferences(var);
			assertEquals(2, refs.length);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	// static inline void staticInHeader() {};
	
	// #include "header.h"
	// void f1() {
	//    staticInHeader();
	// }
	public void testStaticFunctionsInHeaderC_Bug180305() throws Exception {
		String[] contents= getContentsForTest(2);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		TestSourceReader.createFile(fCProject.getProject(), "header.h", contents[0]);
		TestSourceReader.createFile(fCProject.getProject(), "source1.c", contents[1]);
		TestSourceReader.createFile(fCProject.getProject(), "source2.c", contents[1]);
		indexManager.reindex(fCProject);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings = fIndex.findBindings("staticInHeader".toCharArray(),
					IndexFilter.C_DECLARED_OR_IMPLICIT, npm());
			IFunction func = (IFunction) bindings[0];
			assertTrue(func.isStatic());
			IIndexName[] refs = fIndex.findReferences(func);
			assertEquals(2, refs.length);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	// static const int staticConstInHeader= 12;
	
	// #include "header.h"
	// void f1() {
	//    int a= staticConstInHeader;
	// }
	public void testStaticVariableInHeaderC_Bug180305() throws Exception {
		String[] contents= getContentsForTest(2);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		TestSourceReader.createFile(fCProject.getProject(), "header.h", contents[0]);
		TestSourceReader.createFile(fCProject.getProject(), "source1.c", contents[1]);
		TestSourceReader.createFile(fCProject.getProject(), "source2.c", contents[1]);
		indexManager.reindex(fCProject);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings = fIndex.findBindings("staticConstInHeader".toCharArray(),
					IndexFilter.C_DECLARED_OR_IMPLICIT, npm());
			IVariable var = (IVariable) bindings[0];
			assertTrue(var.isStatic());
			IIndexName[] refs = fIndex.findReferences(var);
			assertEquals(2, refs.length);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	// int ok;
	
	// #include "header.x"
	public void testNonStandardSuffix_Bug205778() throws Exception {
		String[] contents= getContentsForTest(2);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		TestSourceReader.createFile(fCProject.getProject(), "header.x", contents[0]);
		TestSourceReader.createFile(fCProject.getProject(), "source.cpp", contents[1]);
		indexManager.reindex(fCProject);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings = fIndex.findBindings("ok".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	// inline void MyClass::method() {}
	
	// class MyClass {
	//    void method();
	// };
	// #include "MyClass_inline.h"
	
	// #include "MyClass.h"
	public void testAddingMemberBeforeContainer_Bug203170() throws Exception {
		String[] contents= getContentsForTest(3);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		TestSourceReader.createFile(fCProject.getProject(), "MyClass_inline.h", contents[0]);
		TestSourceReader.createFile(fCProject.getProject(), "MyClass.h", contents[1]);
		TestSourceReader.createFile(fCProject.getProject(), "MyClass.cpp", contents[2]);
		indexManager.reindex(fCProject);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings = fIndex.findBindings(new char[][] { "MyClass".toCharArray(),
					"method".toCharArray() }, IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			IIndexName[] decls = fIndex.findDeclarations(bindings[0]);
			assertEquals(2, decls.length);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	
	// typedef int unrelated;
	
	// class unrelated {
	// public: int b;
	// };
	
	// #include "h1.h"
	// void test() {
	//    unrelated a;
	//    a.b;
	// }
	public void testUnrelatedTypedef_Bug214146() throws Exception {
		String[] contents= getContentsForTest(3);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		TestSourceReader.createFile(fCProject.getProject(), "s1.cpp", contents[0]);
		TestSourceReader.createFile(fCProject.getProject(), "h1.h", contents[1]);
		TestSourceReader.createFile(fCProject.getProject(), "s2.h", contents[2]);
		indexManager.reindex(fCProject);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings = fIndex.findBindings(new char[][] { "unrelated".toCharArray(),
					"b".toCharArray() }, IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			IIndexName[] decls = fIndex.findNames(bindings[0], IIndex.FIND_ALL_OCCURRENCES);
			assertEquals(2, decls.length);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	// #undef BBB
	
	// #define BBB
	// #include "header.h"
	// #ifdef BBB
	//    int bug227088;
	// #else
	//    int ok;
	// #endif
	public void testUndefInHeader_Bug227088() throws Exception {
		String[] contents= getContentsForTest(2);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		TestSourceReader.createFile(fCProject.getProject(), "header.h", contents[0]);
		TestSourceReader.createFile(fCProject.getProject(), "s1.cpp", contents[1]);
		TestSourceReader.createFile(fCProject.getProject(), "s2.cpp", contents[1]);
		indexManager.reindex(fCProject);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings = fIndex.findBindings("bug227088".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(0, bindings.length);
			bindings = fIndex.findBindings("ok".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			IIndexName[] decls = fIndex.findNames(bindings[0], IIndex.FIND_ALL_OCCURRENCES);
			assertEquals(2, decls.length);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	// #define BUG ok
	
	// int BUG;

	// #include "common.h"
	// #include "header.h"
	public void testCommonHeader_Bug228012() throws Exception {
		String[] contents= getContentsForTest(3);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		TestSourceReader.createFile(fCProject.getProject(), "common.h", contents[0]);
		IFile hfile= TestSourceReader.createFile(fCProject.getProject(), "header.h", contents[1]);
		TestSourceReader.createFile(fCProject.getProject(), "source.cpp", contents[2]);
		indexManager.reindex(fCProject);
		waitForIndexer();
		ITranslationUnit tu= (ITranslationUnit) CoreModel.getDefault().create(hfile);
		fIndex.acquireReadLock();
		try {
			IASTTranslationUnit ast= tu.getAST(fIndex, ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT | ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			IASTSimpleDeclaration decl= (IASTSimpleDeclaration) ast.getDeclarations()[0];
			assertEquals("ok", decl.getDeclarators()[0].getName().toString());
		} finally {
			fIndex.releaseReadLock();
		}
	}

	// #include "h2.h"
	
	// int BUG;

	// #define BUG ok
	// #include "h1.h"
	public void testIndirectContext_Bug267907() throws Exception {
		String[] contents= getContentsForTest(3);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		TestSourceReader.createFile(fCProject.getProject(), "h1.h", contents[0]);
		IFile hfile= TestSourceReader.createFile(fCProject.getProject(), "h2.h", contents[1]);
		TestSourceReader.createFile(fCProject.getProject(), "source.cpp", contents[2]);
		indexManager.reindex(fCProject);
		waitForIndexer();
		ITranslationUnit tu= (ITranslationUnit) CoreModel.getDefault().create(hfile);
		fIndex.acquireReadLock();
		try {
			IASTTranslationUnit ast= tu.getAST(fIndex, ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT | ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			IASTSimpleDeclaration decl= (IASTSimpleDeclaration) ast.getDeclarations()[0];
			assertEquals("ok", decl.getDeclarators()[0].getName().toString());
		} finally {
			fIndex.releaseReadLock();
		}
	}

	
	// #include <header.h>
	// #define _CONCAT(x,y) x##y
	// #define CONCAT(x,y) _CONCAT(x,y)
	public void testIncludeHeuristics_Bug213562() throws Exception {
		String contents= getContentsForTest(1)[0];
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		TestSourceReader.createFile(fCProject.getProject(), "f1/g/header.h", "#define ID one\n");
		TestSourceReader.createFile(fCProject.getProject(), "f2/header.h",    "#define ID two\n");
		TestSourceReader.createFile(fCProject.getProject(), "f1/g/h/header.h", "#define ID three\n");
		TestSourceReader.createFile(fCProject.getProject(), "f1/g/source.cpp", contents + "int CONCAT(one, ID);\n");
		TestSourceReader.createFile(fCProject.getProject(), "f2/g/source.cpp", contents + "int CONCAT(two, ID);\n");
		TestSourceReader.createFile(fCProject.getProject(), "f1/g/h/source.cpp", contents + "int CONCAT(three, ID);\n");
		
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings= fIndex.findBindings("oneone".toCharArray(), IndexFilter.ALL_DECLARED, new NullProgressMonitor());
			assertEquals(1, bindings.length);
			bindings= fIndex.findBindings("twotwo".toCharArray(), IndexFilter.ALL_DECLARED, new NullProgressMonitor());
			assertEquals(1, bindings.length);
			bindings= fIndex.findBindings("threethree".toCharArray(), IndexFilter.ALL_DECLARED, new NullProgressMonitor());
			assertEquals(1, bindings.length);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	public void testIncludeHeuristicsFlag_Bug213562() throws Exception {
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		TestSourceReader.createFile(fCProject.getProject(), "f1/header.h", "");
		IFile f1= TestSourceReader.createFile(fCProject.getProject(), "source1.cpp", "#include \"header.h\"\n");
		IFile f2= TestSourceReader.createFile(fCProject.getProject(), "source2.cpp", "#include \"f1/header.h\"\n");
		indexManager.reindex(fCProject);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexFile f= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(f1));
			IIndexInclude i= f.getIncludes()[0];
			assertTrue(i.isResolvedByHeuristics());

			f= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(f2));
			i= f.getIncludes()[0];
			assertFalse(i.isResolvedByHeuristics());
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	
	// #include "dir"
	// #include "header.h"
	public void testInclusionOfFolders_Bug243682() throws Exception {
		String contents= getContentsForTest(1)[0];
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		IFile sol= TestSourceReader.createFile(fCProject.getProject(), "f1/header.h", "");
		TestSourceReader.createFile(fCProject.getProject(), "dir/dummy.h", "");
		TestSourceReader.createFile(fCProject.getProject(), "header.h/dummy.h", "");
		IFile f1= TestSourceReader.createFile(fCProject.getProject(), "source1.cpp", contents);
		indexManager.reindex(fCProject);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexFile f= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(f1));
			IIndexInclude[] is= f.getIncludes();
			assertFalse(is[0].isResolved());
			assertTrue(is[1].isResolvedByHeuristics());
			assertEquals(sol.getFullPath().toString(), is[1].getIncludesLocation().getFullPath());
		} finally {
			fIndex.releaseReadLock();
		}
	}	
	
	
	// #ifndef B_H
	// #include "b.h"
	// #endif
	//
	// #ifndef A_H_
	// #define A_H_
	// int aOK;
	// #endif /* A_H_ */

	// #ifndef A_H_
	// #include "a.h"
	// #endif
	//
	// #ifndef B_H_
	// #define B_H_
	// int bOK;
	// #endif

	// #include "a.h"
	public void testStrangeIncludeStrategy_Bug249884() throws Exception {
		String[] contents= getContentsForTest(3);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		IFile ah= TestSourceReader.createFile(fCProject.getProject(), "a.h", contents[0]);
		TestSourceReader.createFile(fCProject.getProject(), "b.h", contents[1]);
		TestSourceReader.createFile(fCProject.getProject(), "source.cpp", contents[2]);
		indexManager.reindex(fCProject);
		waitForIndexer();
		ITranslationUnit tu= (ITranslationUnit) CoreModel.getDefault().create(ah);
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings= fIndex.findBindings("aOK".toCharArray(), IndexFilter.ALL_DECLARED, new NullProgressMonitor());
			assertEquals(1, bindings.length);
			fIndex.findBindings("bOK".toCharArray(), IndexFilter.ALL_DECLARED, new NullProgressMonitor());
			assertEquals(1, bindings.length);
			IASTTranslationUnit ast= tu.getAST(fIndex, ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT | ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			final IASTDeclaration[] decls = ast.getDeclarations();
			assertEquals(1, decls.length);
			IASTSimpleDeclaration decl= (IASTSimpleDeclaration) decls[0];
			assertEquals("aOK", decl.getDeclarators()[0].getName().toString());
		} finally {
			fIndex.releaseReadLock();
		}
	}

	// struct s {int a;};
	// struct s x[]= {{.a=1,},{.a=2}};
	public void testReferencesInDesignators_Bug253690() throws Exception {
		String code= getContentsForTest(1)[0];
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test.c", code);
		waitUntilFileIsIndexed(file, 4000);
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings = fIndex.findBindings("a".toCharArray(), false, IndexFilter.ALL_DECLARED, npm());
			assertEquals(1, bindings.length);
			IIndexName[] refs = fIndex.findNames(bindings[0], IIndex.FIND_REFERENCES);
			assertEquals(2, refs.length);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	//	namespace ns {
	//		template<typename T> class X {};
	//	}
	//	class Y : public ns::X<int> {
	//	};
	public void testInstanceInheritance_258745() throws Exception {
		String code= getContentsForTest(1)[0];
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test.cpp", code);
		waitUntilFileIsIndexed(file, 4000);
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings = fIndex.findBindings("Y".toCharArray(), false, IndexFilter.ALL_DECLARED, npm());
			assertEquals(1, bindings.length);
			ICPPClassType ct= (ICPPClassType) bindings[0];
			final ICPPBase[] bases = ct.getBases();
			assertEquals(1, bases.length);
			IBinding inst = bases[0].getBaseClass();
			assertTrue(inst instanceof ICPPTemplateInstance);

			IIndexName name= (IIndexName) bases[0].getBaseClassSpecifierName();
			IBinding inst2= fIndex.findBinding(name);
			assertEquals(inst, inst2);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	
	// #include "B.cpp"

	// static int STATIC;
	// void ref() {STATIC=1;}
	public void testStaticVarInSourceIncluded_Bug265821() throws Exception {
		String[] contents= getContentsForTest(2);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		IFile a= TestSourceReader.createFile(fCProject.getProject(), "A.cpp", contents[0]);
		IFile b= TestSourceReader.createFile(fCProject.getProject(), "B.cpp", contents[1]);
		indexManager.reindex(fCProject);
		waitForIndexer();
		ITranslationUnit tu= (ITranslationUnit) CoreModel.getDefault().create(b);
		fIndex.acquireReadLock();
		try {
			IASTTranslationUnit ast= tu.getAST(fIndex, ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT | ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			IBinding var= ((IASTSimpleDeclaration) ast.getDeclarations()[0]).getDeclarators()[0].getName().resolveBinding();
			IIndexBinding adapted = fIndex.adaptBinding(var);
			assertNotNull(adapted);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	// int a;
	
	// #include "a.h"
	// void test() {a=0;}
	public void testDeclarationForBinding_Bug254844() throws Exception {
		String[] contents= getContentsForTest(2);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		IFile a= TestSourceReader.createFile(fCProject.getProject(), "a.h", contents[0]);
		IFile b= TestSourceReader.createFile(fCProject.getProject(), "b.h", contents[0]);
		IFile source= TestSourceReader.createFile(fCProject.getProject(), "source.cpp", contents[1]);
		indexManager.reindex(fCProject);
		waitForIndexer();
		ITranslationUnit tu= (ITranslationUnit) CoreModel.getDefault().create(source);
		fIndex.acquireReadLock();
		try {
			IASTTranslationUnit ast= tu.getAST(fIndex, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			IIndexFileSet fileset= ast.getIndexFileSet();
			IBinding var= getBindingFromASTName(ast, contents[1], "a=", 1, IBinding.class);
			IName[] decls= ast.getDeclarations(var);
			assertEquals(2, decls.length);
			int check= 0;
			for (IName name : decls) {
				assert name instanceof IIndexName;
				IIndexName iName= (IIndexName) name;
				if (iName.getFileLocation().getFileName().endsWith("a.h")) {
					check |= 1;
					assertTrue(fileset.contains(iName.getFile()));
				} else {
					check |= 2;
					assertFalse(fileset.contains(iName.getFile()));
				}
			}
			assertEquals(3, check);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	//  // a.h
	//	namespace ns {
	//	struct A {
	//	  int i;
	//	};
	//  }
	
	//  #include "a.h"
	//	using ns::A;
	//  void test() {
	//    A a;
	//    a.i = 0;
	//    a.j = 0;
	//  }

	//  // b.h
	//	struct A {
	//	  int j;
	//	};
	
	//  #include "b.h"
	//  void test() {
	//    A a;
	//    a.i = 0;
	//    a.j = 0;
	//  }
	public void testDisambiguationByReachability_268704_1() throws Exception {
		waitForIndexer();

		String[] testData = getContentsForTest(4);
		TestSourceReader.createFile(fCProject.getProject(), "a.h", testData[0]);
		IFile a = TestSourceReader.createFile(fCProject.getProject(), "a.cpp", testData[1]);
		TestSourceReader.createFile(fCProject.getProject(), "b.h", testData[2]);
		IFile b = TestSourceReader.createFile(fCProject.getProject(), "b.cpp", testData[3]);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.reindex(fCProject);
		waitForIndexer();
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			BindingAssertionHelper aHelper = new BindingAssertionHelper(a, testData[1], index);
			aHelper.assertNonProblem("A a;", 1, ICPPClassType.class);
			aHelper.assertNonProblem("i = 0;", 1, ICPPVariable.class);
			aHelper.assertProblem("j = 0;", 1);
			BindingAssertionHelper bHelper = new BindingAssertionHelper(b, testData[3], index);
			aHelper.assertNonProblem("A a;", 1, ICPPClassType.class);
			bHelper.assertProblem("i = 0;", 1);
			bHelper.assertNonProblem("j = 0;", 1, ICPPVariable.class);
		} finally {
			index.releaseReadLock();
		}
	}

	//  // a.h
	//	namespace ns {
	//	  enum E1 { e = 1 };
	//	}
	
	//  #include "a.h"
	//	using namespace ns;
	//	int i = e;

	//  // b.h
	//	enum E2 { e = 2 };
	
	//  #include "b.h"
	//	int i = e;
	public void testDisambiguationByReachability_268704_2() throws Exception {
		waitForIndexer();

		String[] testData = getContentsForTest(4);
		TestSourceReader.createFile(fCProject.getProject(), "a.h", testData[0]);
		IFile a = TestSourceReader.createFile(fCProject.getProject(), "a.cpp", testData[1]);
		TestSourceReader.createFile(fCProject.getProject(), "b.h", testData[2]);
		IFile b = TestSourceReader.createFile(fCProject.getProject(), "b.cpp", testData[3]);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.reindex(fCProject);
		waitForIndexer();
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			BindingAssertionHelper aHelper = new BindingAssertionHelper(a, testData[1], index);
			IEnumerator e1 = aHelper.assertNonProblem("e;", 1, IEnumerator.class);
			assertEquals(1, e1.getValue().numericalValue().longValue());
			BindingAssertionHelper bHelper = new BindingAssertionHelper(b, testData[3], index);
			IEnumerator e2 = bHelper.assertNonProblem("e;", 1, IEnumerator.class);
			assertEquals(2, e2.getValue().numericalValue().longValue());
		} finally {
			index.releaseReadLock();
		}
	}

	//  // a.h
	//  int xx;

	//  #include "a.h"
	//  int yy= xx;

	//  // b.h
	//  int xx();

	//	#include "b.h"
	//	void test() {
	//	  xx();
	//	}
	public void testDisambiguationByReachability_268704_3() throws Exception {
		String[] testData = getContentsForTest(4);
		TestSourceReader.createFile(fCProject.getProject(), "a.h", testData[0]);
		IFile a = TestSourceReader.createFile(fCProject.getProject(), "a.cpp", testData[1]);
		TestSourceReader.createFile(fCProject.getProject(), "b.h", testData[2]);
		IFile b = TestSourceReader.createFile(fCProject.getProject(), "b.cpp", testData[3]);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.reindex(fCProject);
		waitForIndexer();
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			BindingAssertionHelper aHelper = new BindingAssertionHelper(a, testData[1], index);
			IVariable b1 = aHelper.assertNonProblem("xx;", 2, IVariable.class);
			BindingAssertionHelper bHelper = new BindingAssertionHelper(b, testData[3], index);
			IFunction f = bHelper.assertNonProblem("xx();", 2, IFunction.class);
		} finally {
			index.releaseReadLock();
		}
	}

	//  // header.h
	//	template<class T>
	//	struct A {
	//	  void m(const T& p);
	//	};

	//  #include "header.h"
	//	namespace {
	//	enum E { e1 };
	//	}
	//
	//	void test() {
	//	  A<E> a;
	//	  a.m(e1);
	//	}

	//	enum E { e2	};
	public void testDisambiguationByReachability_281782() throws Exception {
		waitForIndexer();

		String[] testData = getContentsForTest(3);
		TestSourceReader.createFile(fCProject.getProject(), "header.h", testData[0]);
		IFile test= TestSourceReader.createFile(fCProject.getProject(), "test.cpp", testData[1]);
		TestSourceReader.createFile(fCProject.getProject(), "unrelated.cpp", testData[2]);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.reindex(fCProject);
		waitForIndexer();
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			IASTTranslationUnit ast = TestSourceReader.createIndexBasedAST(index, fCProject, test);
			getBindingFromASTName(ast, testData[1], "m(e1)", 1, ICPPMethod.class);
		} finally {
			index.releaseReadLock();
		}
	}

	//  // a.h
	//	#undef AAA
	
	//  // b.h
	//  #include "a.h"
	//	#define AAA

	//  // source.c
	//	#include "b.h"
	//  #ifdef AAA
	//  int ok;
	//  #endif
	public void testPreprocessingStatementOrder_270806_1() throws Exception {
		waitForIndexer();
		String[] testData = getContentsForTest(3);
		TestSourceReader.createFile(fCProject.getProject(), "a.h", testData[0]);
		TestSourceReader.createFile(fCProject.getProject(), "b.h", testData[1]);
		IFile s= TestSourceReader.createFile(fCProject.getProject(), "s1.c", testData[2]);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.reindex(fCProject);
		waitForIndexer();
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			IASTTranslationUnit tu = TestSourceReader.createIndexBasedAST(index, fCProject, s);
			IASTPreprocessorStatement[] pstmts= tu.getAllPreprocessorStatements();
			IASTPreprocessorStatement ifndef= pstmts[1];
			assertInstance(ifndef, IASTPreprocessorIfdefStatement.class);
			assertTrue(((IASTPreprocessorIfdefStatement) ifndef).taken());
		} finally {
			index.releaseReadLock();
		}
	}

	//  // a.h
	//	#undef AAA
	
	//  // b.h
	//	#define AAA
	//  #include "a.h"

	//  // source.c
	//	#include "b.h"
	//  #ifdef AAA
	//  int bug;
	//  #endif
	public void testPreprocessingStatementOrder_270806_2() throws Exception {
		waitForIndexer();
		String[] testData = getContentsForTest(3);
		TestSourceReader.createFile(fCProject.getProject(), "a.h", testData[0]);
		TestSourceReader.createFile(fCProject.getProject(), "b.h", testData[1]);
		IFile s= TestSourceReader.createFile(fCProject.getProject(), "s1.c", testData[2]);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.reindex(fCProject);
		waitForIndexer();
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			IASTTranslationUnit tu = TestSourceReader.createIndexBasedAST(index, fCProject, s);
			IASTPreprocessorStatement[] pstmts= tu.getAllPreprocessorStatements();
			IASTPreprocessorStatement ifndef= pstmts[1];
			assertInstance(ifndef, IASTPreprocessorIfdefStatement.class);
			assertFalse(((IASTPreprocessorIfdefStatement) ifndef).taken());
		} finally {
			index.releaseReadLock();
		}
	}

	//	namespace X {}
	//	namespace Y {}
	//	#define AAA
	//	#define BBB
	//  #include "inc.h"
	//  #include <inc.h>
	//  using namespace X;
	//  using namespace Y;
	public void testPreprocessingStatementOrder_270806_3() throws Exception {
		waitForIndexer();
		String[] testData = getContentsForTest(1);
		IFile f= TestSourceReader.createFile(fCProject.getProject(), "a.cpp", testData[0]);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		waitUntilFileIsIndexed(f, 4000);
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			IIndexFile file= index.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(f));
			// check order of includes
			IIndexInclude[] incs = file.getIncludes();
			assertEquals(2, incs.length);
			assertFalse(incs[0].isSystemInclude());
			assertTrue(incs[1].isSystemInclude());
			// check order of macros
			IIndexMacro[] macros = file.getMacros();
			assertEquals(2, macros.length);
			assertEquals("AAA", macros[0].getName());
			assertEquals("BBB", macros[1].getName());
			// check order of using directives
			ICPPUsingDirective[] uds = file.getUsingDirectives();
			assertEquals(2, uds.length);
			assertEquals("X", new String(uds[0].getNominatedScope().getScopeName().getSimpleID()));
			assertEquals("Y", new String(uds[1].getNominatedScope().getScopeName().getSimpleID()));
			assertTrue(uds[0].getPointOfDeclaration() < uds[1].getPointOfDeclaration());
		} finally {
			index.releaseReadLock();
		}
	}
	
	//	template<typename T> void f(T t) throw (T) {}
	public void testFunctionTemplateWithThrowsException_293021() throws Exception {
		waitForIndexer();
		String testData = getContentsForTest(1)[0].toString();
		IFile f= TestSourceReader.createFile(fCProject.getProject(), "testFunctionTemplateWithThrowsException_293021.cpp", testData);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		waitUntilFileIsIndexed(f, 4000);
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			IIndexFile file= index.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(f));
			int idx= testData.indexOf("f(");
			IIndexName[] names = file.findNames(idx, idx+1);
			assertEquals(1, names.length);
			ICPPFunctionTemplate ft= (ICPPFunctionTemplate) index.findBinding(names[0]);
			final IType[] espec = ft.getExceptionSpecification();
			ICPPTemplateParameter par= (ICPPTemplateParameter) espec[0];
			assertEquals(ft, par.getOwner());
		} finally {
			index.releaseReadLock();
		}
	}
	
	//  // a.h
	//	class P {};
	
	//  // b.h
	//	namespace P {class C {};}

	//  // source1.cpp
	// #include "a.h" 
	// P p;

	//  // source2.cpp
	// #include "b.h" 
	// P::C c;
	public void testDisambiguateClassVsNamespace_297686() throws Exception {
		waitForIndexer();
		String[] testData = getContentsForTest(4);
		TestSourceReader.createFile(fCProject.getProject(), "a.h", testData[0]);
		TestSourceReader.createFile(fCProject.getProject(), "b.h", testData[1]);
		IFile s1= TestSourceReader.createFile(fCProject.getProject(), "s1.cpp", testData[2]);
		IFile s2= TestSourceReader.createFile(fCProject.getProject(), "s2.cpp", testData[3]);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.reindex(fCProject);
		waitForIndexer();
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			IASTTranslationUnit tu = TestSourceReader.createIndexBasedAST(index, fCProject, s1);
			IASTSimpleDeclaration sdecl= (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IVariable var= (IVariable) sdecl.getDeclarators()[0].getName().resolveBinding();
			assertFalse(var.getType() instanceof IProblemBinding);
			assertTrue(var.getType() instanceof ICPPClassType);

			tu = TestSourceReader.createIndexBasedAST(index, fCProject, s2);
			sdecl= (IASTSimpleDeclaration) tu.getDeclarations()[0];
			var= (IVariable) sdecl.getDeclarators()[0].getName().resolveBinding();
			assertFalse(var.getType() instanceof IProblemBinding);
			assertTrue(var.getType() instanceof ICPPClassType);
		} finally {
			index.releaseReadLock();
		}
	}

	//  // a.h
	//	struct Error{};
	
	//  // b.h
	//	void Error(int errCode) {}

	//  // source1.cpp
	// #include "a.h" 
	// Error d;

	//  // source2.cpp
	// Error d;  // Problem, without inclusion we need to prefer the function.
	public void testDisambiguateObjectVsType_304479() throws Exception {
		waitForIndexer();
		String[] testData = getContentsForTest(4);
		TestSourceReader.createFile(fCProject.getProject(), "a.h", testData[0]);
		TestSourceReader.createFile(fCProject.getProject(), "b.h", testData[1]);
		IFile s1= TestSourceReader.createFile(fCProject.getProject(), "s1.cpp", testData[2]);
		IFile s2= TestSourceReader.createFile(fCProject.getProject(), "s2.cpp", testData[3]);
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.reindex(fCProject);
		waitForIndexer();
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			IASTTranslationUnit tu = TestSourceReader.createIndexBasedAST(index, fCProject, s1);
			IASTSimpleDeclaration sdecl= (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IVariable var= (IVariable) sdecl.getDeclarators()[0].getName().resolveBinding();
			assertFalse(var.getType() instanceof IProblemBinding);
			assertTrue(var.getType() instanceof ICPPClassType);

			tu = TestSourceReader.createIndexBasedAST(index, fCProject, s2);
			sdecl= (IASTSimpleDeclaration) tu.getDeclarations()[0];
			var= (IVariable) sdecl.getDeclarators()[0].getName().resolveBinding();
			assertTrue(var.getType() instanceof IProblemBinding);
		} finally {
			index.releaseReadLock();
		}
	}

	public void testUpdateNonSrcFolderHeader_283080() throws Exception {
		IIndexBinding[] r;
		
		final IProject prj = fCProject.getProject();
		final IFolder src= prj.getFolder("src");
		final IFolder h= prj.getFolder("h");
		src.create(true, false, null);
		h.create(true, false, null);
		assertTrue(src.exists());
		assertTrue(h.exists());

		ICProjectDescription desc= CCorePlugin.getDefault().getProjectDescription(prj);
		assertNotNull(desc);
		desc.getActiveConfiguration().setSourceEntries(new ICSourceEntry[] {
				new CSourceEntry(src, new IPath[0], ICSettingEntry.SOURCE_PATH)
		});
		CCorePlugin.getDefault().setProjectDescription(prj, desc);
		TestSourceReader.createFile(h, "a.h", "int version1;");
		waitForIndexer(fCProject);
		
		final IIndex index= CCorePlugin.getIndexManager().getIndex(fCProject);
		index.acquireReadLock();
		try {
			r = index.findBindings("version1".toCharArray(), IndexFilter.ALL_DECLARED, null);
			assertEquals(0, r.length);
		} finally {
			index.releaseReadLock();
		}
		
		IFile s= TestSourceReader.createFile(h, "a.h", "int version2;");
		waitForIndexer(fCProject);
		index.acquireReadLock();
		try {
			r = index.findBindings("version2".toCharArray(), IndexFilter.ALL_DECLARED, null);
			assertEquals(0, r.length);
		} finally {
			index.releaseReadLock();
		}

		s= TestSourceReader.createFile(src, "source.cpp", "#include \"../h/a.h\"");
		waitUntilFileIsIndexed(s, INDEX_WAIT_TIME);
		index.acquireReadLock();
		try {
			r = index.findBindings("version2".toCharArray(), IndexFilter.ALL_DECLARED, null);
			assertEquals(1, r.length);
		} finally {
			index.releaseReadLock();
		}
		
		s= TestSourceReader.createFile(h, "a.h", "int version3;");
		waitUntilFileIsIndexed(s, INDEX_WAIT_TIME);
		index.acquireReadLock();
		try {
			r = index.findBindings("version2".toCharArray(), IndexFilter.ALL_DECLARED, null);
			assertEquals(0, r.length);
			r = index.findBindings("version3".toCharArray(), IndexFilter.ALL_DECLARED, null);
			assertEquals(1, r.length);
		} finally {
			index.releaseReadLock();
		}
	}

	public void testUpdateForContentTypeChange_283080() throws Exception {
		IIndexBinding[] r;
		
		final IProject prj = fCProject.getProject();
		IFile file= TestSourceReader.createFile(prj, "a.cpp", "// \u0110 \n int a;");
		file.setCharset("US-ASCII", new NullProgressMonitor());
		waitForIndexer(fCProject);
		
		final IIndex index= CCorePlugin.getIndexManager().getIndex(fCProject);
		int offset1= 0;
		index.acquireReadLock();
		try {
			r = index.findBindings("a".toCharArray(), IndexFilter.ALL_DECLARED, null);
			assertEquals(1, r.length);
			IIndexName[] defs = index.findDefinitions(r[0]);
			assertEquals(1, defs.length);
			offset1= defs[0].getNodeOffset();
		} finally {
			index.releaseReadLock();
		}
		
		file.setCharset("UTF-8", new NullProgressMonitor());
		waitForIndexer(fCProject);
		int offset2= 0;
		index.acquireReadLock();
		try {
			r = index.findBindings("a".toCharArray(), IndexFilter.ALL_DECLARED, null);
			assertEquals(1, r.length);
			IIndexName[] defs = index.findDefinitions(r[0]);
			assertEquals(1, defs.length);
			offset1= defs[0].getNodeOffset();
		} finally {
			index.releaseReadLock();
		}
		
		assertTrue(offset1 != offset2);
	}
}