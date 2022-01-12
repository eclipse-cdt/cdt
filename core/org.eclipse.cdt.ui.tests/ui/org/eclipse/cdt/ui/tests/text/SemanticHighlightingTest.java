/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *     Nathan Ridge - refactoring
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlighting;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightedPosition;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingPresenter;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightings;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.testplugin.Accessor;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.graphics.RGB;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Semantic highlighting tests.
 *
 * <p>Derived from JDT.<p>
 *
 * @since 4.0
 */
public class SemanticHighlightingTest extends TestCase {
	public static Test suite() {
		return new TestSuite(SemanticHighlightingTest.class);
	}

	private File fExternalFile;
	private ICProject fCProject;
	private CEditor fEditor;
	private SourceViewer fSourceViewer;
	private IIndex fIndex;
	private IASTTranslationUnit fAST;

	// The highlighted positions stored in the document don't store any information
	// that directly identifies which highlighting they are for. To recover this
	// information, we assign a different color to each highlighting, and then
	// look up the highlighting's preference key based on the color.
	private Map<RGB, String> fColorToPreferenceKeyMap;

	private static File createExternalFile(final String code) throws Exception {
		File dest = File.createTempFile("external", ".h");
		FileOutputStream fos = new FileOutputStream(dest);
		fos.write(code.getBytes());
		fos.close();
		return dest;
	}

	private void enableHighlightingsAndAssignColors(Set<String> ignoredHighlightings) {
		fColorToPreferenceKeyMap = new HashMap<>();
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED, true);
		SemanticHighlighting[] semanticHighlightings = SemanticHighlightings.getSemanticHighlightings();
		int blue = 0; // for assigning colors to preferences below
		for (SemanticHighlighting semanticHighlighting : semanticHighlightings) {
			String enabledPreferenceKey = SemanticHighlightings.getEnabledPreferenceKey(semanticHighlighting);
			// Make sure ignored highlightings are disabled.
			if (ignoredHighlightings.contains(semanticHighlighting.getPreferenceKey())) {
				store.setValue(enabledPreferenceKey, false);
				continue;
			}

			// Enable the highlighting.
			if (!store.getBoolean(enabledPreferenceKey)) {
				store.setValue(enabledPreferenceKey, true);
			}

			// Choose a unique color for the highlighting, and save the mapping
			// from the color to the highlighting's preference key .
			String colorPreferenceKey = SemanticHighlightings.getColorPreferenceKey(semanticHighlighting);
			RGB color = new RGB(0, 0, blue++); // every highlighting gets a different shade of blue
			PreferenceConverter.setValue(store, colorPreferenceKey, color);
			fColorToPreferenceKeyMap.put(color, semanticHighlighting.getPreferenceKey());
		}
	}

	private void restorePreferencesToDefaults() {
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		store.setToDefault(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED);
		SemanticHighlighting[] semanticHighlightings = SemanticHighlightings.getSemanticHighlightings();
		for (SemanticHighlighting semanticHighlighting : semanticHighlightings) {
			String enabledPreferenceKey = SemanticHighlightings.getEnabledPreferenceKey(semanticHighlighting);
			if (!store.isDefault(enabledPreferenceKey))
				store.setToDefault(enabledPreferenceKey);
			String colorPreferenceKey = SemanticHighlightings.getColorPreferenceKey(semanticHighlighting);
			store.setToDefault(colorPreferenceKey);
		}
		fColorToPreferenceKeyMap.clear();
	}

	// Note: This is not an override of the TestCase.setUp(), but a method called directly
	// by the tests, so that they can specify a value for 'isCpp' on a per-test basis.
	private void setup(boolean isCpp, Set<String> ignoredHighlightings) throws Exception {
		enableHighlightingsAndAssignColors(ignoredHighlightings);

		StringBuilder[] testData = TestSourceReader.getContentsForTest(CTestPlugin.getDefault().getBundle(), "ui",
				getClass(), getName(), 0);

		if (testData.length == 2) {
			fExternalFile = createExternalFile(testData[0].toString());
			assertNotNull(fExternalFile);
			// Load the file using option -include to make it part of the index.
			TestScannerProvider.sIncludeFiles = new String[] { fExternalFile.getAbsolutePath() };
		}

		fCProject = CProjectHelper.createCCProject("SHTest", "bin", IPDOMManager.ID_FAST_INDEXER);
		String sourceFileName = isCpp ? "SHTest.cpp" : "SHTest.c";
		IFile sourceFile = TestSourceReader.createFile(fCProject.getProject(), new Path(sourceFileName),
				testData.length == 2 ? testData[1].toString() : testData[0].toString());
		IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.joinIndexer(5000, new NullProgressMonitor());

		BaseTestCase.waitForIndexer(fCProject);
		fEditor = (CEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile("/SHTest/" + sourceFileName),
				true);
		fSourceViewer = EditorTestHelper.getSourceViewer(fEditor);
		assertTrue(EditorTestHelper.joinReconciler(fSourceViewer, 0, 10000, 100));
		EditorTestHelper.joinBackgroundActivities();

		fIndex = CCorePlugin.getIndexManager().getIndex(fCProject);
		fIndex.acquireReadLock();
		fAST = TestSourceReader.createIndexBasedAST(fIndex, fCProject, sourceFile);
	}

	private void teardown() throws Exception {
		fIndex.releaseReadLock();

		EditorTestHelper.closeEditor(fEditor);

		if (fCProject != null)
			CProjectHelper.delete(fCProject);

		if (fExternalFile != null) {
			fExternalFile.delete();
		}

		TestScannerProvider.sIncludeFiles = null;

		restorePreferencesToDefaults();
	}

	private Position[] getSemanticHighlightingPositions() throws BadPositionCategoryException {
		SemanticHighlightingManager manager = (SemanticHighlightingManager) new Accessor(fEditor, CEditor.class)
				.get("fSemanticManager");
		SemanticHighlightingPresenter presenter = (SemanticHighlightingPresenter) new Accessor(manager,
				manager.getClass()).get("fPresenter");
		String positionCategory = (String) new Accessor(presenter, presenter.getClass()).invoke("getPositionCategory",
				new Object[0]);
		IDocument document = fSourceViewer.getDocument();
		return document.getPositions(positionCategory);
	}

	private void doMakeAssertions() throws Exception {
		IDocument document = fSourceViewer.getDocument();
		int lines = document.getNumberOfLines();

		List<String>[] expected = new List[lines];
		for (int i = 0; i < lines; ++i) {
			expected[i] = new ArrayList<>();
		}
		for (IASTComment comment : fAST.getComments()) {
			String contents = new String(comment.getComment());
			if (contents.length() > 2 && contents.substring(0, 3).equals("//$")) {
				for (String component : contents.substring(3).split(",")) {
					// subtract 1 to make it into a 0-based line number
					expected[comment.getFileLocation().getStartingLineNumber() - 1].add(component);
				}
			}
		}

		List<String>[] actual = new List[lines];
		for (int i = 0; i < lines; ++i) {
			actual[i] = new ArrayList<>();
		}
		for (Position p : getSemanticHighlightingPositions()) {
			assertTrue(p instanceof HighlightedPosition);
			RGB color = ((HighlightedPosition) p).getHighlighting().getTextAttribute().getForeground().getRGB();
			assertTrue(fColorToPreferenceKeyMap.containsKey(color));
			int line = document.getLineOfOffset(p.getOffset());
			actual[line].add(fColorToPreferenceKeyMap.get(color));
		}

		assertEqualMaps(actual, expected);
	}

	private void makeAssertions(boolean isCpp, Set<String> ignoredHighlightings) throws Exception {
		setup(isCpp, ignoredHighlightings);
		try {
			doMakeAssertions();
		} finally {
			teardown();
		}
	}

	private void makeAssertions(Set<String> ignoredHighlightings) throws Exception {
		makeAssertions(true, ignoredHighlightings); // default to C++
	}

	private void makeAssertions(boolean isCpp) throws Exception {
		makeAssertions(isCpp, new HashSet<String>());
	}

	private void makeAssertions() throws Exception {
		makeAssertions(true); // default to C++
	}

	private void assertEqualMaps(List<String>[] actual, List<String>[] expected) {
		assertEquals(expected.length, actual.length);
		for (int i = 0; i < actual.length; ++i) {
			assertEquals("Expected " + expected[i].size() + " positions on line " + i + ", got " + actual[i].size(),
					expected[i].size(), actual[i].size());
			for (int j = 0; j < actual[i].size(); ++j) {
				assertEquals(expected[i].get(j), actual[i].get(j));
			}
		}
	}

	//  void SDKFunction();
	//	class SDKClass { public: void SDKMethod(); };\n\n";

	//#define INT      int                               //$macroDefinition
	//#define FUNCTION_MACRO(arg) globalFunc(arg)        //$macroDefinition
	//#define EMPTY_MACRO(arg)                           //$macroDefinition
	//#include "SHTest.h"
	//enum Enumeration {                                 //$enum
	//    enumerator                                     //$enumerator
	//};
	//
	//const int globalConstant = 0;                      //$globalVariable
	//int globalVariable = 0;                            //$globalVariable
	//static int globalStaticVariable = 0;               //$globalVariable
	//
	//void globalFunc(int a);                            //$functionDeclaration,parameterVariable
	//static void globalStaticFunc() {                   //$functionDeclaration
	//    EMPTY_MACRO(n);                                //$macroSubstitution
	//};
	//
	//class Base1 {};                                    //$class
	//class Base2 {};                                    //$class
	//
	//class ClassContainer : Base1, Base2 {              //$class,class,class
	//    friend void friendFunc();                      //$functionDeclaration
	//    friend class FriendClass;                      //$class
	//
	//public:
	//    static int staticPubField;                     //$staticField
	//    const int constPubField;                       //$field
	//    const static int constStaticPubField;          //$staticField
	//    int pubField;                                  //$field
	//
	//    static int staticPubMethod(int arg) {          //$methodDeclaration,parameterVariable
	//        FUNCTION_MACRO(arg);                       //$macroSubstitution,parameterVariable
	//        globalFunc(arg);                           //$function,parameterVariable
	//        return globalStaticVariable;               //$globalVariable
	//    }
	//    int pubMethod();                               //$methodDeclaration
	//
	//    enum pubEnumeration {pubEnumerator};           //$enum,enumerator
	//    class pubClass{};                              //$class
	//    class pubStruct{};                             //$class
	//    class pubUnion{};                              //$class
	//    typedef pubClass pubTypedef;                   //$class,typedef
	//
	//protected:
	//    static const int constStaticProtField = 12;    //$staticField
	//    static int staticProtField;                    //$staticField
	//    const  int constProtField;                     //$field
	//    int protField;                                 //$field
	//
	//    static int staticProtMethod();                 //$methodDeclaration
	//    int protMethod();                              //$methodDeclaration
	//
	//    enum protEnumeration {protEnumerator};         //$enum,enumerator
	//    class protClass{};                             //$class
	//    class protStruct{};                            //$class
	//    class protUnion{};                             //$class
	//    typedef protClass protTypedef;                 //$class,typedef
	//
	//private:
	//    static const int constStaticPrivField = 12;    //$staticField
	//    static int staticPrivField;                    //$staticField
	//    const  int constPrivField;                     //$field
	//    int privField;                                 //$field
	//
	//    static int staticPrivMethod();                 //$methodDeclaration
	//    int privMethod();                              //$methodDeclaration
	//
	//    enum privEnumeration {privEnumerator};         //$enum,enumerator
	//    class privClass{};                             //$class
	//    class privStruct{};                            //$class
	//    class privUnion{};                             //$class
	//    typedef privClass privTypedef;                 //$class,typedef
	//
	//
	//};
	//
	//template<class T1, class T2> class TemplateClass { //$templateParameter,templateParameter,class
	//    T1 tArg1;                                      //$templateParameter,field
	//    T2 tArg2;                                      //$templateParameter,field
	//    TemplateClass(T1 arg1, T2 arg2) {              //$methodDeclaration,templateParameter,parameterVariable,templateParameter,parameterVariable
	//        tArg1 = arg1;                              //$field,parameterVariable
	//        tArg2 = arg2;                              //$field,parameterVariable
	//    }
	//};
	//
	//template<class T1> class PartialInstantiatedClass  //$templateParameter,class
	//    : TemplateClass<T1, Base1> {};                 //$class,templateParameter,class
	//
	//
	//struct CppStruct {                                 //$class
	//    int structField;                               //$field
	//};
	//
	//union CppUnion {                                   //$class
	//    int unionField;                                //$field
	//    CppUnion operator+(CppUnion);                  //$class,methodDeclaration,class
	//    CppUnion operator[](int);                      //$class,methodDeclaration
	//};
	//
	//typedef CppUnion TUnion;                           //$class,typedef
	//
	//namespace ns {                                     //$namespace
	//    int namespaceVar = 0;                          //$globalVariable
	//    int namespaceFunc() {                          //$functionDeclaration
	//	globalStaticFunc();                              //$function
	//	return namespaceVar;                             //$globalVariable
	//    }
	//}
	//
	//INT ClassContainer::protMethod() {                 //$macroSubstitution,methodDeclaration
	//    return protField;                              //$field
	//}
	//
	//INT ClassContainer::pubMethod() {                  //$macroSubstitution,methodDeclaration
	//    int localVar = 0;                              //$localVariableDeclaration
	//    return pubField + localVar;                    //$field,localVariable
	//}
	//
	//INT ClassContainer::staticPrivMethod() {           //$macroSubstitution,methodDeclaration
	//    CppStruct* st= new CppStruct();                //$class,localVariableDeclaration,class
	//    st->structField= 1;                            //$localVariable,field
	//    CppUnion un;                                   //$class,localVariableDeclaration
	//    un.unionField= 2;                              //$localVariable,field
	//    staticPubMethod(staticPrivField);              //$staticMethod,staticField
	//    un + un[6];                                    //$localVariable,overloadedOperator,localVariable,overloadedOperator,overloadedOperator
	//label:                                             //$label
	//    FUNCTION_MACRO(0);                             //$macroSubstitution
	//    if (un.unionField < st->structField)           //$localVariable,field,localVariable,field
	//      goto label;                                  //$label
	//    problemMethod();                               //$problem
	//    // external SDK
	//    SDKClass sdkClass;                             //$class,localVariableDeclaration
	//    sdkClass.SDKMethod();                          //$localVariable,externalSDK
	//    SDKFunction();                                 //$externalSDK
	//    return 0;
	//}
	//
	////http://bugs.eclipse.org/209203
	//template <int n>                                   //$templateParameter
	//int f()                                            //$functionDeclaration
	//{
	//  return n;                                        //$templateParameter
	//}
	//
	////http://bugs.eclipse.org/220392
	//#define EMPTY                                      //$macroDefinition
	//EMPTY int f();                                     //$macroSubstitution,functionDeclaration
	//
	////http://bugs.eclipse.org/340492
	//template< template<class> class U >                //$templateParameter
	//class myClass {};                                  //$class
	//
	////http://bugs.eclipse.org/372004
	//void g() {                                         //$functionDeclaration
	//    // declared as global near top
	//    extern int globalVariable;                     //$globalVariable
	//}
	//
	////http://bugs.eclipse.org/399149
	//class C final {                                    //$class,c_keyword
	//    void finalMethod() final;                      //$methodDeclaration,c_keyword
	//    void overrideMethod() override;                //$methodDeclaration,c_keyword
	//
	//    // ordinary field, happens to be named 'final'
	//    int final;                                     //$field
	//};
	public void testVariousHighlightings() throws Exception {
		makeAssertions();
	}

	//enum class EnumerationClass {                      //$enumClass
	//    enumerator                                     //$enumerator
	//};
	//
	//class Base1 {};                                    //$class
	//class Base2 {};                                    //$class
	//
	//class ClassContainer : Base1, Base2 {              //$class,class,class
	//
	//public:
	//    enum class pubEnumerationClass {pubEnum};      //$enumClass,enumerator
	//
	//protected:
	//    enum class protEnumerationClass {protEnum};    //$enumClass,enumerator
	//
	//private:
	//    enum class privEnumerationClass {privEnum};    //$enumClass,enumerator
	//
	//};
	//
	public void testEnumClassHighlightings() throws Exception {
		makeAssertions();
	}

	//  class C {                                        //$class
	//    template <typename T> void bar(T);             //$templateParameter,methodDeclaration,templateParameter
	//  };
	//
	//  template <typename U>                            //$templateParameter
	//  void foo(U u) {                                  //$functionDeclaration,templateParameter,parameterVariable
	//    C().bar(u);                                    //$class,method,parameterVariable
	//  }
	public void testDependentMethodCall_379626() throws Exception {
		makeAssertions();
	}

	//	struct S {};                                     //$class
	//	struct S waldo;                                  //$class,globalVariable
	public void testCStructureName_451772() throws Exception {
		makeAssertions(false /* parse as C file */);
	}

	//	template <typename T>                            //$templateParameter
	//	void foo(T t) {                                  //$functionDeclaration,templateParameter,parameterVariable
	//		bar(t);                                      //$function,parameterVariable
	//	}
	public void testNPE_458317() throws Exception {
		makeAssertions();
	}

	//	struct S { };                                    //$class
	//	alignas(S) int x;                                //$class,globalVariable
	public void testHighlightingInsideAlignmentSpecifier_451082() throws Exception {
		makeAssertions();
	}

	//	struct Duration {};                              //$class
	//	Duration operator "" _d(unsigned long long);     //$class,functionDeclaration
	//	Duration dur = 1000_d;                           //$class,globalVariable,udlSuffix
	public void testUserDefinedLiteralSuffix_484617() throws Exception {
		makeAssertions();
	}

	//	template<typename T, typename U>                 //$templateParameter,templateParameter
	//	struct Pair {};                                  //$class
	//
	//	template<typename T>                             //$templateParameter
	//	using PairIntX = Pair<int, T>;                   //$typedef,class,templateParameter
	//
	//	struct Waldo {};                                 //$class
	//
	//	int main() {                                     //$functionDeclaration
	//		PairIntX<Waldo> pair;                        //$typedef,class,localVariableDeclaration
	//	}
	public void testAliasTemplates_416748() throws Exception {
		makeAssertions();
	}

	//	namespace N {                                    //$namespace
	//		class C {                                    //$class
	//			enum E1 {};                              //$enum
	//			enum class EC1 {};                       //$enumClass
	//		};
	//		C::E1 e1;                                    //$class,enum,globalVariable
	//		C::EC1 ec1;                                  //$class,enumClass,globalVariable
	//		enum E2 {};                                  //$enum
	//		enum class EC2 {};                           //$enumClass
	//	}
	//	N::C::E1 e1;                                     //$namespace,class,enum,globalVariable
	//	N::C::EC1 ec1;                                   //$namespace,class,enumClass,globalVariable
	//	N::E2 e2;                                        //$namespace,enum,globalVariable
	//	N::EC2 ec2;                                      //$namespace,enumClass,globalVariable
	public void testQualifiedEnum_485709() throws Exception {
		makeAssertions();
	}

	//	class Base {};                                   //$class
	//	class Derived : Base {                           //$class,class
	//		using Base::Base;                            //$class,method
	//	};
	public void testInheritingConstructor_484898() throws Exception {
		makeAssertions();
	}

	//	void foo(int param) {                            //$functionDeclaration,parameterVariable
	//		int local;                                   //$localVariableDeclaration
	//		[local, param](){};                          //$class,localVariable,parameterVariable
	//	}
	public void testLocalVariableInLambdaCapture_486679() throws Exception {
		makeAssertions();
	}

	//	template <typename T>                            //$templateParameter
	//	struct Base {                                    //$class
	//		enum E { A };                                //$enum,enumerator
	//		enum class F { B };                          //$enumClass,enumerator
	//	};
	//	template <typename T>                            //$templateParameter
	//	struct Derived : Base<T> {                       //$class,class,templateParameter
	//		static typename Base<T>::E x                 //$class,templateParameter,enum,staticField
	//          = Base<T>::A;                            //$class,templateParameter,enumerator
	//		static typename Base<T>::F y                 //$class,templateParameter,enumClass,staticField
	//          = Base<T>::F::B;                         //$class,templateParameter,enumClass,enumerator
	//	};
	public void testDependentEnum_486688() throws Exception {
		makeAssertions();
	}

	//	#define WALDO(name) const char* Name() override { return name; }  //$macroDefinition
	//	class S {                                        //$class
	//		WALDO("name")                                //$macroSubstitution
	//	};
	public void testOverrideInMacroExpansion_486683a() throws Exception {
		// This tests that the 'override' does not cause the entire invocation
		// to be colored with the keyword highlighting.
		makeAssertions();
	}

	//	#define MIRROR(arg) arg                          //$macroDefinition
	//	MIRROR(class S { void foo() override; })         //$macroSubstitution,class,methodDeclaration,c_keyword
	public void testOverrideInMacroExpansion_486683b() throws Exception {
		// This tests that the 'override' *does* cause the 'override' keyword
		// in the argument to be colored with the keyword highlighting.
		makeAssertions();
	}

	//	#define MIRROR(arg) arg                          //$macroDefinition
	//	struct Foo {                                     //$class
	//		bool operator==(const Foo&) const;           //$methodDeclaration,class
	//	};
	//	int main() {                                     //$functionDeclaration
	//		Foo a, b;                                    //$class,localVariableDeclaration,localVariableDeclaration
	//		MIRROR(a == b);                              //$macroSubstitution,localVariable,overloadedOperator,localVariable
	//	}
	public void testOverloadedOperatorInMacroExpansion_371839() throws Exception {
		makeAssertions();
	}

	//	template<unsigned... _Indexes>                   //$templateParameter
	//	struct _Index_tuple {                            //$class
	//		typedef _Index_tuple<_Indexes..., sizeof...(_Indexes)> __next;  //$class,templateParameter,templateParameter,typedef
	//	};
	//	template<unsigned _Num>                          //$templateParameter
	//	struct _Build_index_tuple {                      //$class
	//		typedef typename _Build_index_tuple<_Num - 1>::__type::__next __type;  //$class,templateParameter,class,class,typedef
	//	};
	//
	//	template<>
	//	struct _Build_index_tuple<0> {                   //$class
	//		typedef _Index_tuple<> __type;               //$class,typedef
	//	};
	public void testRecursion_491834() throws Exception {
		makeAssertions();
	}

	//	template <typename T>                            //$templateParameter
	//	bool templ = true;                               //$globalVariable
	//	struct A {};                                     //$class
	//	bool x = templ<A>;                               //$globalVariable,globalVariable,class
	//	struct S {                                       //$class
	//		template <typename U>                        //$templateParameter
	//		static bool templ = true;                    //$staticField
	//		void bar() {                                 //$methodDeclaration
	//			bool y = templ<A>;                       //$localVariableDeclaration,staticField,class
	//		}
	//	};
	public void testVariableTemplates_486672() throws Exception {
		makeAssertions();
	}

	//	#define MACRO(Name, Type) Type Name();           //$macroDefinition
	//	typedef int Int;                                 //$typedef
	//	class S {                                        //$class
	//		MACRO(foo, Int)                              //$macroSubstitution,methodDeclaration,typedef
	//	};
	public void testMethodNameInsideMacro_486682() throws Exception {
		makeAssertions();
	}

	//	#define IF_0(t, f) f                             //$macroDefinition
	//	#define IF(bit, t, f) IF_ ## bit(t, f)           //$macroDefinition
	//	#define WALDO                                    //$macroDefinition
	//	#define MAIN(...) int main() { __VA_ARGS__ }     //$macroDefinition
	//
	//	MAIN                                             //$macroSubstitution
	//	(
	//	    int x;                                       //$localVariableDeclaration
	//	    IF(0, WALDO, WALDO)                          //$macroSubstitution,macroSubstitution,macroSubstitution
	//	)
	public void testLexicalColoringInsideMacroExpansion_490415() throws Exception {
		makeAssertions();
	}

	//	#define N1(x) x
	//	#define M1(x) N1(x)

	//	int main() {                                     //$functionDeclaration
	//		M1(0);                                       //$macroSubstitution
	//	}
	public void testLexicalColoringInsideMacroExpansion_496696() throws Exception {
		makeAssertions();
	}

	//	void foo(int&);                                  //$functionDeclaration
	//	struct S {                                       //$class
	//		int x;                                       //$field
	//  };
	//	void bar(int x) {                                //$functionDeclaration,parameterVariable
	//		foo(x);                                      //$function,variablePassedByNonconstRef
	//		S s;                                         //$class,localVariableDeclaration
	//	    foo(s.x);                                    //$function,variablePassedByNonconstRef
	//	}
	public void testVariablePassedByNonconstRef_487764a() throws Exception {
		makeAssertions();
	}

	//	template <typename... Args>                      //$templateParameter
	//	void foo(Args&&... args);                        //$functionDeclaration,templateParameter,parameterVariable
	//	void bar() {                                     //$functionDeclaration
	//		const int x;                                 //$localVariableDeclaration
	//		int y;                                       //$localVariableDeclaration
	//		foo(x, y, "waldo");                          //$function,localVariable,variablePassedByNonconstRef
	//	}
	public void testVariablePassedByNonconstRef_487764b() throws Exception {
		makeAssertions();
	}

	//	void foo(int* const &);                          //$functionDeclaration
	//	void bar(int* waldo) {                           //$functionDeclaration,parameterVariable
	//		foo(waldo);                                  //$function,parameterVariable
	//	}
	public void testReferenceToConstPointer_509619() throws Exception {
		makeAssertions();
	}

	//	struct S {};                                     //$class
	//	template <typename>
	//	void waldo() {}                                  //$functionDeclaration
	//	template <>
	//	void waldo<S>() {}                               //$functionDeclaration,class
	public void testArgumentsOfFunctionTemplateSpecialization_510788() throws Exception {
		makeAssertions();
	}

	//	struct Waldo {                                   //$class
	//		static void find();                          //$methodDeclaration
	//	};
	//	int main() {                                     //$functionDeclaration
	//		Waldo::search();                             //$class,problem
	//	}
	public void testQualifiedName_511331() throws Exception {
		makeAssertions();
	}

	//	void foo(unsigned i) {                           //$functionDeclaration,parameterVariable
	//		__builtin_assume_aligned(i, 4);              //$problem,parameterVariable
	//	}
	public void testMisuseOfKnownBuiltin_512932() throws Exception {
		makeAssertions();
	}

	//	struct S {};                                     //$class
	//
	//	template <typename T>                            //$templateParameter
	//	void bar(T, S&);                                 //$functionDeclaration,templateParameter,class
	//
	//	template <typename T>                            //$templateParameter
	//	void foo(T t) {                                  //$functionDeclaration,templateParameter,parameterVariable
	//	    S state;                                     //$class,localVariableDeclaration
	//	    bar(t, state);                               //$function,parameterVariable,variablePassedByNonconstRef
	//	}
	public void testVariablePassedByNonConstRef_529958() throws Exception {
		makeAssertions();
	}

	//	float operator""if(long double) {                //$functionDeclaration
	//		return 1.6f;
	//	}
	//	int main() {                                     //$functionDeclaration
	//		auto k = 1.3if;                              //$localVariableDeclaration,udlSuffix
	//	}
	public void testUDLOperatorIfCall_527954() throws Exception {
		makeAssertions();
	}

	//	float operator""if(long double) {                //$functionDeclaration
	//		return 1.6f;
	//	}
	//	int main() {                                     //$functionDeclaration
	//		auto k = 1.3if;                              //$localVariableDeclaration,overloadedOperator
	//	}
	public void testOverriddenUDLOperatorIfCallnoUDL_539535() throws Exception {
		Set<String> ignoredHighlightings = new HashSet<>();
		ignoredHighlightings.add(SemanticHighlightings.UDL_SUFFIX);
		makeAssertions(ignoredHighlightings);
	}

	//	float operator""if(long double) {                //$functionDeclaration
	//		return 1.6f;
	//	}
	//	int main() {                                     //$functionDeclaration
	//		auto k = 1.3if;                              //$localVariableDeclaration
	//	}
	public void testUDLOperatorIfCallnoUDLnoOperator_539535() throws Exception {
		Set<String> ignoredHighlightings = new HashSet<>();
		ignoredHighlightings.add(SemanticHighlightings.UDL_SUFFIX);
		ignoredHighlightings.add(SemanticHighlightings.OVERLOADED_OPERATOR);
		makeAssertions(ignoredHighlightings);
	}

	//	int operator""int(long double) {                 //$functionDeclaration
	//		return -1;
	//	}
	//	int main() {                                     //$functionDeclaration
	//		auto k = 1.3int;                             //$localVariableDeclaration,udlSuffix
	//	}
	public void testUDLOperatorIntCall_527954() throws Exception {
		makeAssertions();
	}

	//	int operator""int(long double) {                 //$functionDeclaration
	//		return -1;
	//	}
	//	int main() {                                     //$functionDeclaration
	//		auto k = 1.3int;                             //$localVariableDeclaration,overloadedOperator
	//	}
	public void testUDLOperatorIntCallnoUDL_539535() throws Exception {
		Set<String> ignoredHighlightings = new HashSet<>();
		ignoredHighlightings.add(SemanticHighlightings.UDL_SUFFIX);
		makeAssertions(ignoredHighlightings);
	}

	//	int operator""int(long double) {                 //$functionDeclaration
	//		return -1;
	//	}
	//	int main() {                                     //$functionDeclaration
	//		auto k = 1.3int;                             //$localVariableDeclaration
	//	}
	public void testUDLOperatorIntCallnoUDLnoOperator_539535() throws Exception {
		Set<String> ignoredHighlightings = new HashSet<>();
		ignoredHighlightings.add(SemanticHighlightings.UDL_SUFFIX);
		ignoredHighlightings.add(SemanticHighlightings.OVERLOADED_OPERATOR);
		makeAssertions(ignoredHighlightings);
	}

	//	struct S {                                       //$class
	//	    int waldo;                                   //$field
	//	};
	//	struct Iter {                                    //$class
	//	    S operator*();                               //$class,methodDeclaration
	//	};
	//	int main() {                                     //$functionDeclaration
	//	    Iter it;                                     //$class,localVariableDeclaration
	//	    1 + (*it).waldo;                             //$overloadedOperator,localVariable,field
	//	}
	public void testOverloadedOperatorStar_539535() throws Exception {
		makeAssertions();
	}
}
