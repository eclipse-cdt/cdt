/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

public class IndexUpdateTests extends IndexTestBase {

	private static final String EXPLICIT = "explicit";
	private static final String VIRTUAL = "virtual";
	private static final String PURE_VIRTUAL= "pure-virtual";
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

	@Override
	public void setUp() throws Exception {
		super.setUp();
		if (fCppProject == null) {
			fCppProject= CProjectHelper.createCCProject("indexUpdateTestsCpp", null, IPDOMManager.ID_FAST_INDEXER);
		}
		if (fCProject == null) {
			fCProject= CProjectHelper.createCProject("indexUpdateTestsC", null, IPDOMManager.ID_FAST_INDEXER);
		}
		CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, npm());
		fIndex= CCorePlugin.getIndexManager().getIndex(new ICProject[] {fCProject, fCppProject});
	}

	private void setupHeader(int totalFileVersions, boolean cpp) throws Exception {
		if (fContents == null) {
			fContents= getContentsForTest(totalFileVersions);
			fContentUsed= -1;
		}
		IProject project= cpp ? fCppProject.getProject() : fCProject.getProject();
		fHeader= TestSourceReader.createFile(project, "header.h", fContents[++fContentUsed].toString());
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, npm()));
	}

	private void setupFile(int totalFileVersions, boolean cpp) throws Exception {
		if (fContents == null) {
			fContents= getContentsForTest(totalFileVersions);
			fContentUsed= -1;
		}
		IProject project= cpp ? fCppProject.getProject() : fCProject.getProject();
		fFile= TestSourceReader.createFile(project, "file" + (cpp ? ".cpp" : ".c"), fContents[++fContentUsed].toString());
		TestSourceReader.waitUntilFileIsIndexed(fIndex, fFile, INDEXER_WAIT_TIME);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, npm()));
	}
	
	private void updateFile() throws Exception {
		// Append variable comment to the end of the file to change its contents.
		// Indexer would not reindex the file if its contents remain the same. 
		fFile= TestSourceReader.createFile(fFile.getParent(), fFile.getName(),
				fContents[++fContentUsed].toString() + "\n// " + fContentUsed); 
		TestSourceReader.waitUntilFileIsIndexed(fIndex, fFile, INDEXER_WAIT_TIME);
	}
	
	@Override
	public void tearDown() throws Exception {
		fIndex= null;
		if (fFile != null) {
			fFile.delete(true, npm());
		}
		if (fHeader != null) {
			fHeader.delete(true, npm());
		}
		CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, npm());
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

	private void checkValue(String name, Long value) throws Exception {
		fIndex.acquireReadLock();
		try {
			IBinding b = findBinding(name);
			IValue v= null;
			if (b instanceof IVariable) 
				v= ((IVariable) b).getInitialValue();
			else if (b instanceof IEnumerator) 
				v= ((IEnumerator) b).getValue();
			else 
				fail();
			
			if (value == null)
				assertNull(v);
			else 
				assertEquals(value, v.numericalValue());
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
		return fIndex.findBindings(nchars, IndexFilter.ALL_DECLARED, npm())[0];
	}

	private String msg() {
		return "Update #" + fContentUsed;
	}

	private String msg(String modifier) {
		return msg() + "; " + modifier + ":";
	}

	// int globalVar;

	// short globalVar;

	// register int globalVar;

	public void testGlobalCppVariable() throws Exception {
		setupFile(3, true);
		checkCppVariable("globalVar", INT, new String[]{});
		updateFile();
		checkCppVariable("globalVar", SHORT, new String[]{});
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
	
	// class MyClass {virtual int method(char a) = 0;};

	public void testCppMethod() throws Exception {
		setupFile(10, true);
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
		updateFile();
		checkCppMethod("MyClass::method", new String[] {INT, CHAR}, new String[]{PRIVATE, VIRTUAL, PURE_VIRTUAL});
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
		checkModifier(modifiers, PURE_VIRTUAL, method.isPureVirtual());
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
			IIndexBinding[] ctors= fIndex.findBindings(new char[][]{nchars, nchars}, IndexFilter.ALL_DECLARED_OR_IMPLICIT, npm());

			int count= 0;
			for (int i = 0; i < ctors.length; i++) {
				IIndexBinding ctor= ctors[i];
				if (ctor.isFileLocal()) {
					ctors[count++]= ctor;
				}
			}
			assertEquals(m1 == null ? 1 : 2, count);
			final IType[] parameterTypes = ((ICPPConstructor) ctors[0]).getType().getParameterTypes();
			if (parameterTypes.length!=1 || !(parameterTypes[0] instanceof ICPPReferenceType)) {
				IIndexBinding h= ctors[0]; ctors[0]= ctors[1]; ctors[1]= h;
			}
			if (m1 != null) {
				checkCppConstructor((ICPPConstructor) ctors[1], new String[]{"", "void"}, m1);
			}
			checkCppConstructor((ICPPConstructor) ctors[0], new String[]{"", constRefType}, m2);

			IIndexBinding[] assignmentOps= fIndex.findBindings(new char[][]{nchars, "operator =".toCharArray()}, IndexFilter.ALL_DECLARED_OR_IMPLICIT, npm());
			count= 0;
			for (int i = 0; i < assignmentOps.length; i++) {
				IIndexBinding assignmentOp= assignmentOps[i];
				if (assignmentOp.isFileLocal()) {
					assignmentOps[count++]= assignmentOp;
				}
			}
			assertEquals(1, count);
			checkCppMethod((ICPPMethod) assignmentOps[0], new String[]{refType, constRefType}, m3);
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
	
	// struct myType {
	//    int a;
	// };
	
	// union myType {
	//    int a;
	// };

	// typedef int myType;

	// enum myType {};
	public void testChangingTypePlainC() throws Exception {
		setupFile(4, false);
		IBinding binding;
		ICompositeType ct;
		fIndex.acquireReadLock();
		try {
			binding = findBinding("myType");
			assertTrue(binding instanceof ICompositeType);
			ct = (ICompositeType) binding;
			assertTrue(ct.getKey() == ICompositeType.k_struct);
		} finally {
			fIndex.releaseReadLock();
		}
		
		updateFile();
		fIndex.acquireReadLock();
		try {
			binding = findBinding("myType");
			assertTrue(binding instanceof ICompositeType);
			ct = (ICompositeType) binding;
			assertTrue(ct.getKey() == ICompositeType.k_union);
		} finally {
			fIndex.releaseReadLock();
		}
		
		updateFile();
		fIndex.acquireReadLock();
		try {
			binding = findBinding("myType");
			assertTrue(binding instanceof ITypedef);
			ITypedef td = (ITypedef) binding;
			assertEquals(INT, ASTTypeUtil.getType(td.getType()));
		} finally {
			fIndex.releaseReadLock();
		}
		
		updateFile();
		fIndex.acquireReadLock();
		try {
			binding = findBinding("myType");
			assertTrue(binding instanceof IEnumeration);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	
	// class myType {
	//    int a;
	// };
	
	// struct myType {
	//    int a;
	// };
	
	// union myType {
	//    int a;
	// };

	// typedef int myType;

	// enum myType {};
	public void testChangingTypeCPP() throws Exception {
		setupFile(4, true);
		IBinding binding;
		ICompositeType ct;
		fIndex.acquireReadLock();
		try {
			binding = findBinding("myType");
			assertTrue(binding instanceof ICompositeType);
			ct = (ICompositeType) binding;
			assertTrue(ct.getKey() == ICompositeType.k_struct);
		} finally {
			fIndex.releaseReadLock();
		}
		
		updateFile();
		fIndex.acquireReadLock();
		try {
			binding = findBinding("myType");
			assertTrue(binding instanceof ICompositeType);
			ct = (ICompositeType) binding;
			assertTrue(ct.getKey() == ICompositeType.k_union);
		} finally {
			fIndex.releaseReadLock();
		}
		
		updateFile();
		fIndex.acquireReadLock();
		try {
			binding = findBinding("myType");
			assertTrue(binding instanceof ITypedef);
			ITypedef td = (ITypedef) binding;
			assertEquals(INT, ASTTypeUtil.getType(td.getType()));
		} finally {
			fIndex.releaseReadLock();
		}
		
		updateFile();
		fIndex.acquireReadLock();
		try {
			binding = findBinding("myType");
			assertTrue(binding instanceof IEnumeration);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	// template<typename T> class CT {};

	// template<typename T=int> class CT {};

	// template<typename T=char> class CT {};

	// template<int U, typename T> struct CT {};
	
	// template<template<typename T> class V> class CT {};

	// template<template<template<typename I> class T> class V> class CT {};

	// template<typename U> class CT {};
	public void testClassTemplates() throws Exception {
		setupFile(7, true);
		ICPPClassTemplate binding;
		ICompositeType ct;
		fIndex.acquireReadLock();
		long pdomid;
		try {
			binding = (ICPPClassTemplate) findBinding("CT");
			assertEquals(ICPPClassType.k_class , binding.getKey());
			ICPPTemplateParameter[] tpars = binding.getTemplateParameters();
			assertEquals(1, tpars.length);
			assertTrue(tpars[0] instanceof ICPPTemplateTypeParameter);
			assertEquals(0, tpars[0].getParameterID());
			assertEquals("T", tpars[0].getName());
			assertNull(tpars[0].getDefaultValue());
			pdomid= ((PDOMNode)((IAdaptable) tpars[0]).getAdapter(PDOMNode.class)).getRecord();
		} finally {
			fIndex.releaseReadLock();
		}

		updateFile();
		fIndex.acquireReadLock();
		try {
			binding = (ICPPClassTemplate) findBinding("CT");
			assertEquals(ICPPClassType.k_class , binding.getKey());
			ICPPTemplateParameter[] tpars = binding.getTemplateParameters();
			assertEquals(1, tpars.length);
			assertTrue(tpars[0] instanceof ICPPTemplateTypeParameter);
			assertEquals(0, tpars[0].getParameterID());
			assertEquals("T", tpars[0].getName());
			assertEquals("int", ASTTypeUtil.getType(tpars[0].getDefaultValue().getTypeValue()));
		} finally {
			fIndex.releaseReadLock();
		}

		updateFile();
		fIndex.acquireReadLock();
		try {
			binding = (ICPPClassTemplate) findBinding("CT");
			assertEquals(ICPPClassType.k_class , binding.getKey());
			ICPPTemplateParameter[] tpars = binding.getTemplateParameters();
			assertEquals(1, tpars.length);
			assertTrue(tpars[0] instanceof ICPPTemplateTypeParameter);
			assertEquals(0, tpars[0].getParameterID());
			assertEquals("T", tpars[0].getName());
			assertEquals("char", ASTTypeUtil.getType(tpars[0].getDefaultValue().getTypeValue()));
		} finally {
			fIndex.releaseReadLock();
		}

		updateFile();
		fIndex.acquireReadLock();
		try {
			binding = (ICPPClassTemplate) findBinding("CT");
			assertEquals(ICompositeType.k_struct , binding.getKey());
			ICPPTemplateParameter[] tpars = binding.getTemplateParameters();
			assertEquals(2, tpars.length);
			assertTrue(tpars[0] instanceof ICPPTemplateNonTypeParameter);
			assertEquals("U", tpars[0].getName());
			assertEquals(0, tpars[0].getParameterID());
			assertTrue(tpars[1] instanceof ICPPTemplateTypeParameter);
			assertEquals("T", tpars[1].getName());
			assertEquals(1, tpars[1].getParameterID());
		} finally {
			fIndex.releaseReadLock();
		}
		
		updateFile();
		fIndex.acquireReadLock();
		try {
			binding = (ICPPClassTemplate) findBinding("CT");
			assertEquals(ICPPClassType.k_class , binding.getKey());
			ICPPTemplateParameter[] tpars = binding.getTemplateParameters();
			assertEquals(1, tpars.length);
			assertTrue(tpars[0] instanceof ICPPTemplateTemplateParameter);
			assertEquals("V", tpars[0].getName());
			assertEquals(0, tpars[0].getParameterID());
			tpars= ((ICPPTemplateTemplateParameter) tpars[0]).getTemplateParameters();
			assertEquals(1, tpars.length);
			assertTrue(tpars[0] instanceof ICPPTemplateTypeParameter);
			assertEquals(0x10000, tpars[0].getParameterID());
			assertEquals("T", tpars[0].getName());
		} finally {
			fIndex.releaseReadLock();
		}

		updateFile();
		fIndex.acquireReadLock();
		try {
			binding = (ICPPClassTemplate) findBinding("CT");
			assertEquals(ICPPClassType.k_class , binding.getKey());
			ICPPTemplateParameter[] tpars = binding.getTemplateParameters();
			assertEquals(1, tpars.length);
			assertTrue(tpars[0] instanceof ICPPTemplateTemplateParameter);
			assertEquals("V", tpars[0].getName());
			assertEquals(0, tpars[0].getParameterID());
			tpars= ((ICPPTemplateTemplateParameter) tpars[0]).getTemplateParameters();
			assertEquals(1, tpars.length);
			assertTrue(tpars[0] instanceof ICPPTemplateTemplateParameter);
			assertEquals(0x10000, tpars[0].getParameterID());
			assertEquals("T", tpars[0].getName());
		} finally {
			fIndex.releaseReadLock();
		}

		updateFile();
		fIndex.acquireReadLock();
		try {
			binding = (ICPPClassTemplate) findBinding("CT");
			assertEquals(ICPPClassType.k_class , binding.getKey());
			ICPPTemplateParameter[] tpars = binding.getTemplateParameters();
			assertEquals(1, tpars.length);
			assertTrue(tpars[0] instanceof ICPPTemplateTypeParameter);
			assertEquals(0, tpars[0].getParameterID());
			assertEquals("U", tpars[0].getName());
			assertEquals(pdomid, ((PDOMNode)((IAdaptable) tpars[0]).getAdapter(PDOMNode.class)).getBindingID());
		} finally {
			fIndex.releaseReadLock();
		}
	}

	// int globalVar;
	
	// #include "header.h"
	// void test() {
	//    globalVar= 1;
	// }
	public void testChangingSourceBeforeHeader_Bug171834() throws Exception {
		setupHeader(2, true);
		setupFile(0, true);
		IBinding binding;
		ICompositeType ct;
		fIndex.acquireReadLock();
		try {
			binding = findBinding("globalVar");
			assertTrue(binding instanceof IVariable);
			assertEquals(2, fIndex.findNames(binding, IIndex.FIND_ALL_OCCURRENCES).length);
		} finally {
			fIndex.releaseReadLock();
		}

		fFile= TestSourceReader.createFile(fFile.getParent(), fFile.getName(), fContents[1].toString().replaceAll("globalVar", "newVar"));
		TestSourceReader.waitUntilFileIsIndexed(fIndex, fFile, INDEXER_WAIT_TIME);
		
		fIndex.acquireReadLock();
		try {
			binding = findBinding("globalVar");
			assertTrue(binding instanceof IVariable);
			assertEquals(1, fIndex.findNames(binding, IIndex.FIND_ALL_OCCURRENCES).length);
		} finally {
			fIndex.releaseReadLock();
		}

		fHeader= TestSourceReader.createFile(fHeader.getParent(), fHeader.getName(), fContents[0].toString().replaceAll("globalVar", "newVar"));
		TestSourceReader.waitUntilFileIsIndexed(fIndex, fHeader, INDEXER_WAIT_TIME);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, npm()));

		fIndex.acquireReadLock();
		try {
			binding = findBinding("newVar");
			assertTrue(binding instanceof IVariable);
			assertEquals(2, fIndex.findNames(binding, IIndex.FIND_ALL_OCCURRENCES).length);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	
	// int globalVar;
	// void func();
	
	// extern "C" {
	//    int globalVar;
	//    void func();
	// }
	
	// int globalVar;
	// void func();
	
	// extern "C" int globalVar;
	// extern "C" void func();

	// int globalVar;
	// void func();
	public void testExternC() throws Exception {
		setupFile(5, true);
		checkExternC(false);
		updateFile();
		checkExternC(true);
		updateFile();
		checkExternC(false);
		updateFile();
		checkExternC(true);
		updateFile();
		checkExternC(false);
	}

	private void checkExternC(boolean value) throws Exception {
		fIndex.acquireReadLock();
		try {
			ICPPVariable var = (ICPPVariable) findBinding("globalVar");
			assertEquals(value, var.isExternC());
			ICPPFunction func = (ICPPFunction) findBinding("func");
			assertEquals(value, func.isExternC());
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	// int global;
	// struct C {int mem;};
	// enum E {e0};
	
	// int global=1;
	// struct C {int mem=1;};
	// enum E {e0=1};
	
	// int global;
	// struct C {int mem;};
	// enum E {e0};
	public void testValuesC() throws Exception {
		setupFile(3, false);
		checkValue("global", null);
		checkValue("C::mem", null);
		checkValue("e0", 0L);
		updateFile();
		checkValue("global", 1L);
		checkValue("C::mem", 1L);
		checkValue("e0", 1L);
		updateFile();
		checkValue("global", null);
		checkValue("C::mem", null);
		checkValue("e0", 0L);
	}
	
	// class A {
	//    public: void foo();
	// };

	// class A {
	//    public: void foo() throw();
	// };

	// class A {
	//    public: void foo() throw(int, double);
	// };

	// class A {
	//    public: void foo() throw();
	// };

	// class A {
	//    public: void foo();
	// };
	public void testExceptionSpecification() throws Exception {
		ICPPMethod method;
		IType[] exceptionSpec;
		setupFile(5, true);
		fIndex.acquireReadLock();
		try {
			method = (ICPPMethod) findBinding("A::foo");
			exceptionSpec = method.getExceptionSpecification();
			assertNull(exceptionSpec);
		} finally {
			fIndex.releaseReadLock();
		}
		
		updateFile();
		fIndex.acquireReadLock();
		try {
			method = (ICPPMethod) findBinding("A::foo");
			exceptionSpec = method.getExceptionSpecification();
			assertEquals(0, exceptionSpec.length);
		} finally {
			fIndex.releaseReadLock();
		}

		updateFile();
		fIndex.acquireReadLock();
		try {
			method = (ICPPMethod) findBinding("A::foo");
			exceptionSpec = method.getExceptionSpecification();
			assertNotNull(exceptionSpec);
			assertEquals(2, exceptionSpec.length);
			assertEquals("int", ASTTypeUtil.getType(exceptionSpec[0]));
			assertEquals("double", ASTTypeUtil.getType(exceptionSpec[1]));
		} finally {
			fIndex.releaseReadLock();
		}

		updateFile();
		fIndex.acquireReadLock();
		try {
			method = (ICPPMethod) findBinding("A::foo");
			exceptionSpec = method.getExceptionSpecification();
			assertEquals(0, exceptionSpec.length);
		} finally {
			fIndex.releaseReadLock();
		}

		updateFile();
		fIndex.acquireReadLock();
		try {
			method = (ICPPMethod) findBinding("A::foo");
			exceptionSpec = method.getExceptionSpecification();
			assertNull(exceptionSpec);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	// int global;
	// struct C {int mem;};
	// enum E {e0};
	
	// int global=1;
	// struct C {int mem=1;};
	// enum E {e0=1};
	
	// int global;
	// struct C {int mem;};
	// enum E {e0};
	public void testValuesCPP() throws Exception {
		setupFile(3, true);
		checkValue("global", null);
		checkValue("C::mem", null);
		checkValue("e0", 0L);
		updateFile();
		checkValue("global", 1L);
		checkValue("C::mem", 1L);
		checkValue("e0", 1L);
		updateFile();
		checkValue("global", null);
		checkValue("C::mem", null);
		checkValue("e0", 0L);
	}
	
	//class A {};
	//class B {friend class A;};

	//class B {};
	public void testFriendClass() throws Exception {
		setupFile(2, true);
		assertFriendRemoval("B", "A");
	}
	
	// class X {public: char* foo(int);};
	// class Y {friend char* X::foo(int);};

	// class Y {};
	public void testFriendMethod() throws Exception {
		setupFile(2, true);
		assertFriendRemoval("Y", "X::foo");
	}
	
	// class X {friend void friend_set(X*, int);};
	// void friend_set(X* p, int i) {}

	// class X {};
	public void testFriendFunction() throws Exception {
		setupFile(2, true);
		assertFriendRemoval("X", "friend_set");
	}
	
	private void assertFriendRemoval(String clientClassBinding, String supplierBinding) throws Exception {
		fIndex.acquireReadLock();
		try {
			IBinding client = findBinding(clientClassBinding);
			IBinding supplier = findBinding(supplierBinding);
			assertNotNull("Unable to find binding with name \""+clientClassBinding+"\"", client);
			assertTrue("Unable to find binding with name \""+clientClassBinding+"\"", client instanceof ICPPClassType);
			assertNotNull("Unable to find binding with name \""+supplierBinding+"\"", supplier);
			assertTrue(((ICPPClassType)client).getFriends().length == 1);
			assertTrue(((ICPPClassType)client).getFriends()[0].equals(supplier));
		} finally {
			fIndex.releaseReadLock();
		}
		
		updateFile();

		fIndex.acquireReadLock();
		try {
			IBinding client = findBinding(clientClassBinding);
			assertNotNull("Unable to find binding with name \""+clientClassBinding+"\"", client);
			assertTrue("Unable to find binding with name \""+clientClassBinding+"\"", client instanceof ICPPClassType);
			assertTrue(((ICPPClassType)client).getFriends().length == 0);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	
	// void funcTypeDeletion(int);

	// #include "header.h"
	// typeof(funcTypeDeletion) storeRef;
	// char funcTypeDeletion(int);		// delete type
	// typeof(storeRef) useRef;         // use reference
	public void testTypedeletion_Bug294306() throws Exception {
		setupHeader(2, true);
		setupFile(2, true);
		checkFunction("useRef", new String[]{"void", "int"}, new String[]{});
		fContentUsed--;
		updateFile();
		checkFunction("useRef", new String[]{"char", "int"}, new String[]{});
	}

	// void f(int a, int b=0);

	// #include "header.h"
	// void f(int a, int b) {}
	// void ref() {
	//   f(1);
	// }
	
	// #include "header.h"
	// void f(int a, int b) {}
	// void ref() {
	//   f(1);
	// }
	public void testDefaultParam_Bug297438() throws Exception {
		setupHeader(3, true);
		setupFile(3, true);
		checkReferenceCount("f", 1);
		updateFile();
		checkReferenceCount("f", 1);
	}

	private void checkReferenceCount(String name, int count) throws InterruptedException, CoreException {
		fIndex.acquireReadLock();
		try { 
			IBinding func = findBinding(name);
			assertEquals(count, fIndex.findReferences(func).length);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	// enum E {e0};
	
	// enum class E;

	// enum E : short {e1};
	
	// enum class E {e2};
	
	// enum class E : short {e1};
	
	// enum E : int;
	public void testEnumCPP() throws Exception {
		setupFile(6, true);
		checkEnum(false, null, "e0");
		updateFile();
		checkEnum(true, "int", null);
		updateFile();
		checkEnum(false, "short int", "e1");
		updateFile();
		checkEnum(true, "int", "e2");
		updateFile();
		checkEnum(true, "short int", "e1");
		updateFile();
		checkEnum(false, "int", null);
	}

	private void checkEnum(boolean scoped, String fixedType, String enumItem) throws Exception {
		fIndex.acquireReadLock();
		try { 
			ICPPEnumeration enumType = (ICPPEnumeration) findBinding("E");
			assertEquals(scoped, enumType.isScoped());
			if (fixedType == null) {
				assertNull(enumType.getFixedType());
			} else {
				assertEquals(fixedType, ASTTypeUtil.getType(enumType.getFixedType()));
			}
			final IEnumerator[] enumItems = enumType.getEnumerators();
			if (enumItem == null) {
				assertEquals(0, enumItems.length);
			} else {
				assertEquals(1, enumItems.length);
				assertEquals(enumItem, enumItems[0].getName());
			}
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	// class X {};

	// class X {};
	public void testFileLocalBinding() throws Exception {
		setupFile(2, true);
		long id1, id2;
		fIndex.acquireReadLock();
		try { 
			final IIndexBinding binding = findBinding("X");
			id1= ((PDOMFile) binding.getLocalToFile()).getRecord();
		} finally {
			fIndex.releaseReadLock();
		}
		
		updateFile();
		fIndex.acquireReadLock();
		try { 
			final IIndexBinding binding = findBinding("X");
			id2= ((PDOMFile) binding.getLocalToFile()).getRecord();
		} finally {
			fIndex.releaseReadLock();
		}
		assertEquals(id1, id2);
	}
}
