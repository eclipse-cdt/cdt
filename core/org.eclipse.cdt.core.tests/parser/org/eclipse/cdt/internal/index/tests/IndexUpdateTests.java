/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.index.tests;

import java.util.Arrays;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class IndexUpdateTests extends IndexTestBase {

	private static final String EXPLICIT = "explicit";
	private static final String VIRTUAL = "virtual";
	private static final String PROTECTED = "protected";
	private static final String PUBLIC = "public";
	private static final String PRIVATE = "private";
	private static final String CHAR = "char";
	private static final String INLINE = "inline";
	private static final String MUTABLE = "mutable";
	private static final String STATIC = "static";
	private static final String REGISTER = "register";
	private static final String AUTO = "auto";
	private static final String SHORT = "short int";
	private static final String INT = "int";
	private static final String IMPLICIT= "implicit";

	public static TestSuite suite() {
		TestSuite suite= suite(IndexUpdateTests.class, "_");
		suite.addTest(new IndexUpdateTests("deleteProject"));
		return suite;
	}

	private ICProject fCppProject= null;
	private ICProject fCProject= null;
	private IIndex fIndex= null;
	private StringBuffer[] fContents;
	private IFile fFile;
	private IFile fHeader;
	private int fContentUsed;
	
	public IndexUpdateTests(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		super.setUp();
		if (fCppProject == null) {
			fCppProject= CProjectHelper.createCCProject("indexUpdateTestsCpp", null, IPDOMManager.ID_FAST_INDEXER);
		}
		if (fCProject == null) {
			fCProject= CProjectHelper.createCProject("indexUpdateTestsC", null, IPDOMManager.ID_FAST_INDEXER);
		}
		fIndex= CCorePlugin.getIndexManager().getIndex(new ICProject[] {fCProject, fCppProject});
	}

	private void setupHeader(int totalFileVersions, boolean cpp) throws Exception {
		if (fContents == null) {
			fContents= getContentsForTest(totalFileVersions);
			fContentUsed= -1;
		}
		IProject project= cpp ? fCppProject.getProject() : fCProject.getProject();
		fFile= TestSourceReader.createFile(project, "header.h", fContents[++fContentUsed].toString());
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, NPM));
	}

	private void setupFile(int totalFileVersions, boolean cpp) throws Exception {
		if (fContents == null) {
			fContents= getContentsForTest(totalFileVersions);
			fContentUsed= -1;
		}
		IProject project= cpp ? fCppProject.getProject() : fCProject.getProject();
		fFile= TestSourceReader.createFile(project, "file" + (cpp ? ".cpp" : ".c"), fContents[++fContentUsed].toString());
		fContentUsed= 0;
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, NPM));
	}
	
	private void updateFile() throws Exception {
		fFile= TestSourceReader.createFile(fFile.getParent(), fFile.getName(), fContents[++fContentUsed].toString());
		TestSourceReader.waitUntilFileIsIndexed(fIndex, fFile, INDEXER_WAIT_TIME);
	}
	
	public void tearDown() throws Exception {
		fIndex= null;
		if (fFile != null) {
			fFile.delete(true, NPM);
		}
		if (fHeader != null) {
			fHeader.delete(true, NPM);
		}
		CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, NPM);
		super.tearDown();
	}
		
	public void deleteProject() {
		if (fCProject != null) {
			CProjectHelper.delete(fCProject);
			fCProject= null;
		}
		if (fCppProject != null) {
			CProjectHelper.delete(fCppProject);
			fCppProject= null;
		}
	}
	
	// int globalVar;
	
	// short globalVar;
	
	// auto int globalVar;
	
	// register int globalVar;
	public void testGlobalCVariable() throws Exception {
		setupFile(4, false);
		checkVariable("globalVar", INT, new String[] {});
		updateFile();
		checkVariable("globalVar", SHORT, new String[] {});
		updateFile();
		checkVariable("globalVar", INT, new String[] {AUTO});
		updateFile();
		checkVariable("globalVar", INT, new String[] {REGISTER});
	}

	private void checkVariable(String name, String type, String[] modifiers) throws Exception {
		fIndex.acquireReadLock();
		try {
			IVariable var = (IVariable) findBinding(name);
			checkVariable(var, type, modifiers);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	private void checkVariable(IVariable var, String type, String[] modifiers) throws DOMException {
		assertEquals(msg(), type, ASTTypeUtil.getType(var.getType()));
		checkModifier(modifiers, AUTO, var.isAuto());
		checkModifier(modifiers, REGISTER, var.isRegister());
		checkModifier(modifiers, STATIC, var.isStatic());
	}

	private void checkModifier(String[] modifiers, String modifier, boolean actual) throws DOMException {
		assertEquals(msg(modifier), hasMod(modifiers, modifier), actual);
	}

	private boolean hasMod(String[] modifiers, String mod) {
		return Arrays.asList(modifiers).contains(mod);
	}

	private IIndexBinding findBinding(String name) throws CoreException {
		String[] names= name.split("::");
		char[][] nchars= new char[names.length][];
		for (int i = 0; i < nchars.length; i++) {
			nchars[i]= names[i].toCharArray();
		}
		return fIndex.findBindings(nchars, IndexFilter.ALL_DECLARED, NPM)[0];
	}

	private String msg() {
		return "Update #" + fContentUsed;
	}

	private String msg(String modifier) {
		return msg() + "; " + modifier + ":";
	}

	// int globalVar;
	
	// short globalVar;
	
	// auto int globalVar;
	
	// register int globalVar;
	
	public void testGlobalCppVariable() throws Exception {
		setupFile(4, true);
		checkCppVariable("globalVar", INT, new String[]{});
		updateFile();
		checkCppVariable("globalVar", SHORT, new String[]{});
		updateFile();
		checkCppVariable("globalVar", INT, new String[]{AUTO});
		updateFile();
		checkCppVariable("globalVar", INT, new String[]{REGISTER});
	}

	private void checkCppVariable(String name, String type, String[] modifiers) throws Exception {
		fIndex.acquireReadLock();
		try {
			ICPPVariable var = (ICPPVariable) findBinding(name);
			checkCppVariable(var, type, modifiers);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	private void checkCppVariable(ICPPVariable var, String type, String[] modifiers) throws Exception {
		checkVariable(var, type, modifiers);
		checkModifier(modifiers, MUTABLE, var.isMutable());
	}

	// int globalFunction(int a, int b){};
	
	// short globalFunction(int a, int b){};

	// int globalFunction(char a){};

	// inline int globalFunction(char a){};
	public void testCFunction() throws Exception {
		setupFile(4, false);
		checkFunction("globalFunction", new String[] {INT, INT, INT}, new String[]{});
		updateFile();
		checkFunction("globalFunction", new String[] {SHORT, INT, INT}, new String[]{});
		updateFile();
		checkFunction("globalFunction", new String[] {INT, CHAR}, new String[]{});
		updateFile();
		checkFunction("globalFunction", new String[] {INT, CHAR}, new String[]{INLINE});
	}

	private void checkFunction(String name, String[] types, String[] modifiers) throws Exception {
		fIndex.acquireReadLock();
		try {
			IFunction func = (IFunction) findBinding(name);
			checkFunction(func, types, modifiers);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	private void checkFunction(IFunction func, String[] types, String[] modifiers)
			throws DOMException {
		assertEquals(msg(), types[0], ASTTypeUtil.getType(func.getType().getReturnType()));
		IParameter[] params= func.getParameters();
		assertEquals(msg(), types.length-1, params.length);
		for (int i = 0; i < params.length; i++) {
			IParameter parameter = params[i];
			assertEquals(msg(), types[i+1], ASTTypeUtil.getType(parameter.getType()));
		}
		checkModifier(modifiers, INLINE, func.isInline());
		checkModifier(modifiers, STATIC, func.isStatic());
	}

	
	// int globalFunction(int a, int b){};
	
	// short globalFunction(int a, int b){};

	// int globalFunction(char a){};

	// inline int globalFunction(char a){};
	public void testCppFunction() throws Exception {
		setupFile(4, true);
		checkFunction("globalFunction", new String[] {INT, INT, INT}, new String[]{});
		updateFile();
		checkFunction("globalFunction", new String[] {SHORT, INT, INT}, new String[]{});
		updateFile();
		checkFunction("globalFunction", new String[] {INT, CHAR}, new String[]{});
		updateFile();
		checkFunction("globalFunction", new String[] {INT, CHAR}, new String[]{INLINE});
	}
	
	
	// struct my_struct {int fField;};
	
	// struct my_struct {short fField;};
	
	public void testCField() throws Exception {
		setupFile(2, false);
		checkVariable("my_struct::fField", INT, new String[]{});
		updateFile();
		checkVariable("my_struct::fField", SHORT, new String[]{});
	}

	
	// class MyClass {int fField;};
	
	// class MyClass {short fField;};
	
	// class MyClass {mutable int fField;};

	// class MyClass {public: int fField;};

	// class MyClass {protected: int fField;};

	// class MyClass {private: int fField;};

	// class MyClass {private: static int fField;};
	public void testCppField() throws Exception {
		setupFile(7, true);
		checkCppField("MyClass::fField", INT, new String[]{PRIVATE});
		updateFile();
		checkCppField("MyClass::fField", SHORT, new String[]{PRIVATE});
		updateFile();
		checkCppField("MyClass::fField", INT, new String[]{PRIVATE, MUTABLE});
		updateFile();
		checkCppField("MyClass::fField", INT, new String[]{PUBLIC});
		updateFile();
		checkCppField("MyClass::fField", INT, new String[]{PROTECTED});
		updateFile();
		checkCppField("MyClass::fField", INT, new String[]{PRIVATE});
		updateFile();
		checkCppField("MyClass::fField", INT, new String[]{PRIVATE, STATIC});
	}

	private void checkCppField(String name, String type, String[] modifiers) throws Exception {
		fIndex.acquireReadLock();
		try {
			ICPPField field = (ICPPField) findBinding(name);
			checkCppVariable(field, type, modifiers);
			checkCppMember(field, modifiers);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	private void checkCppMember(ICPPMember member, String[] modifiers) throws Exception {
		int visibility= member.getVisibility();
		checkModifier(modifiers, PUBLIC, visibility == ICPPMember.v_public);
		checkModifier(modifiers, PROTECTED, visibility == ICPPMember.v_protected);
		checkModifier(modifiers, PRIVATE, visibility == ICPPMember.v_private);
	}


	// class MyClass {int method(int a, int b);};
	
	// class MyClass {short method(int a, int b);};
	
	// class MyClass {int method(char a);};
	
	// class MyClass {inline int method(char a);};

	// class MyClass {virtual int method(char a);};

	// class MyClass {public: int method(char a);};

	// class MyClass {protected: int method(char a);};

	// class MyClass {private: int method(char a);};

	// class MyClass {int method(char a){};};

	public void testCppMethod() throws Exception {
		setupFile(9, true);
		checkCppMethod("MyClass::method", new String[] {INT, INT, INT}, new String[]{PRIVATE});
		updateFile();
		checkCppMethod("MyClass::method", new String[] {SHORT, INT, INT}, new String[]{PRIVATE});
		updateFile();
		checkCppMethod("MyClass::method", new String[] {INT, CHAR}, new String[]{PRIVATE});
		updateFile();
		checkCppMethod("MyClass::method", new String[] {INT, CHAR}, new String[]{PRIVATE, INLINE});
		updateFile();
		checkCppMethod("MyClass::method", new String[] {INT, CHAR}, new String[]{PRIVATE, VIRTUAL});
		updateFile();
		checkCppMethod("MyClass::method", new String[] {INT, CHAR}, new String[]{PUBLIC});
		updateFile();
		checkCppMethod("MyClass::method", new String[] {INT, CHAR}, new String[]{PROTECTED});
		updateFile();
		checkCppMethod("MyClass::method", new String[] {INT, CHAR}, new String[]{PRIVATE});
		updateFile();
		checkCppMethod("MyClass::method", new String[] {INT, CHAR}, new String[]{PRIVATE, INLINE});
	}

	// class MyClass {protected: int method(int a, int b);};
	
	// #include "header.h"
	// int MyClass::method(int a, int b);
	
	// #include "header.h"
	// char MyClass::method(int a, int b);
	
	public void testFixedCppMethod() throws Exception {
		setupHeader(3, true);
		checkCppMethod("MyClass::method", new String[] {INT, INT, INT}, new String[]{PROTECTED});
		setupFile(0, true);
		checkCppMethod("MyClass::method", new String[] {INT, INT, INT}, new String[]{PROTECTED});
		updateFile();
		checkCppMethod("MyClass::method", new String[] {INT, INT, INT}, new String[]{PROTECTED});
	}

	private void checkCppMethod(String name, String[] types, String[] modifiers) throws Exception {
		fIndex.acquireReadLock();
		try {
			ICPPMethod method = (ICPPMethod) findBinding(name);
			checkCppMethod(method, types, modifiers);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	private void checkCppMethod(ICPPMethod method, String[] types, String[] modifiers)
			throws DOMException, Exception {
		checkFunction(method, types, modifiers);
		checkCppMember(method, modifiers);
		checkModifier(modifiers, VIRTUAL, method.isVirtual());
		checkModifier(modifiers, IMPLICIT, method.isImplicit());
	}
	
	// class MyClass {MyClass(int a, int b);};
	
	// class MyClass {MyClass(char a, int b);};
	
	// class MyClass {explicit MyClass(char a, int b);};
	
	// class MyClass {public: MyClass(char a, int b);};

	// class MyClass {protected: MyClass(char a, int b);};

	// class MyClass {private: MyClass(char a, int b);};

	public void testCppConstructor() throws Exception {
		setupFile(6, true);
		checkCppConstructor("MyClass::MyClass", new String[] {"", INT, INT}, new String[]{PRIVATE});
		updateFile();
		checkCppConstructor("MyClass::MyClass", new String[] {"", CHAR, INT}, new String[]{PRIVATE});
		updateFile();
		checkCppConstructor("MyClass::MyClass", new String[] {"", CHAR, INT}, new String[]{PRIVATE,EXPLICIT});
		updateFile();
		checkCppConstructor("MyClass::MyClass", new String[] {"", CHAR, INT}, new String[]{PUBLIC});
		updateFile();
		checkCppConstructor("MyClass::MyClass", new String[] {"", CHAR, INT}, new String[]{PROTECTED});
		updateFile();
		checkCppConstructor("MyClass::MyClass", new String[] {"", CHAR, INT}, new String[]{PRIVATE});
	}

	private void checkCppConstructor(String name, String[] types, String[] modifiers) throws Exception {
		fIndex.acquireReadLock();
		try {
			ICPPConstructor ctor = (ICPPConstructor) findBinding(name);
			checkCppConstructor(ctor, types, modifiers);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	private void checkCppConstructor(ICPPConstructor ctor, String[] types, String[] modifiers) throws Exception {
		checkFunction(ctor, types, modifiers);
		checkCppMember(ctor, modifiers);
		checkModifier(modifiers, EXPLICIT, ctor.isExplicit());
	}

	// class MyClass {};
	
	// class MyClass {protected: MyClass(void);};
	
	// class MyClass {explicit MyClass(const MyClass& rhs);};
	
	// class MyClass {public: MyClass& operator=(const MyClass& rhs) {}};

	// class MyClass {};
	public void testImplicitMethods() throws Exception {
		setupFile(5, true);
		checkImplicitMethods("MyClass", 
				new String[] {IMPLICIT, PUBLIC}, 
				new String[] {IMPLICIT, PUBLIC},
				new String[] {IMPLICIT, PUBLIC});
		updateFile();
		checkImplicitMethods("MyClass", 
				new String[] {PROTECTED}, 
				new String[] {IMPLICIT, PUBLIC},
				new String[] {IMPLICIT, PUBLIC});
		updateFile();
		checkImplicitMethods("MyClass", 
				null, // no default constructor, because we declared the copy constr.
				new String[] {EXPLICIT, PRIVATE},
				new String[] {IMPLICIT, PUBLIC});
		updateFile();
		checkImplicitMethods("MyClass", 
				new String[] {IMPLICIT, PUBLIC}, 
				new String[] {IMPLICIT, PUBLIC},
				new String[] {INLINE, PUBLIC});
		updateFile();
		checkImplicitMethods("MyClass", 
				new String[] {IMPLICIT, PUBLIC}, 
				new String[] {IMPLICIT, PUBLIC},
				new String[] {IMPLICIT, PUBLIC});
	}

	private void checkImplicitMethods(String name, String[] m1, String[] m2, String[] m3) throws Exception {
		fIndex.acquireReadLock();
		try {
			final char[] nchars = name.toCharArray();
			final String refType = name + " &";
			final String constRefType = "const " + refType;
			IBinding[] ctors= fIndex.findBindings(new char[][]{nchars, nchars}, IndexFilter.ALL_DECLARED_OR_IMPLICIT, NPM);
			assertEquals(m1 == null ? 1 : 2, ctors.length);
			final IType[] parameterTypes = ((ICPPConstructor) ctors[0]).getType().getParameterTypes();
			if (parameterTypes.length!=1 || !(parameterTypes[0] instanceof ICPPReferenceType)) {
				IBinding h= ctors[0]; ctors[0]= ctors[1]; ctors[1]= h;
			}
			if (m1 != null) {
				checkCppConstructor((ICPPConstructor) ctors[1], new String[]{"", "void"}, m1);
			}
			checkCppConstructor((ICPPConstructor) ctors[0], new String[]{"", constRefType}, m2);

			IBinding assignmentOp= fIndex.findBindings(new char[][]{nchars, "operator =".toCharArray()}, IndexFilter.ALL_DECLARED_OR_IMPLICIT, NPM)[0];
			checkCppMethod((ICPPMethod) assignmentOp, new String[]{refType, constRefType}, m3);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	// typedef int myType;
	
	// typedef short myType;
	public void testCTypedef() throws Exception {
		setupFile(2, false);
		checkTypedef("myType", INT);
		updateFile();
		checkTypedef("myType", SHORT);
	}

	private void checkTypedef(String name, String type) throws Exception {
		fIndex.acquireReadLock();
		try {
			ITypedef var = (ITypedef) findBinding(name);
			checkTypedef(var, type);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	private void checkTypedef(ITypedef var, String type) throws DOMException {
		assertEquals(msg(), type, ASTTypeUtil.getType(var.getType()));
	}
	
	// typedef int myType;
	
	// typedef short myType;
	public void testCppTypedef() throws Exception {
		setupFile(2, true);
		checkTypedef("myType", INT);
		updateFile();
		checkTypedef("myType", SHORT);
	}

	// namespace aNs {
	// } 
	// namespace nsAlias= aNs;
	
	// namespace bNs {
	// }
	// namespace nsAlias= bNs;
	public void testNamespaceAlias() throws Exception {
		setupFile(2, true);
		checkNamespaceAlias("nsAlias", "aNs");
		updateFile();
		checkNamespaceAlias("nsAlias", "bNs");
	}

	private void checkNamespaceAlias(String name, String target) throws Exception {
		fIndex.acquireReadLock();
		try {
			ICPPNamespaceAlias nsalias = (ICPPNamespaceAlias) findBinding(name);
			assertEquals(msg(), target, nsalias.getBinding().getName());
		} finally {
			fIndex.releaseReadLock();
		}
	}	
}
