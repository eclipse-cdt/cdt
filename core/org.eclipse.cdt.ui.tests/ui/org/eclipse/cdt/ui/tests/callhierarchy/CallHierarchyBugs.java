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
package org.eclipse.cdt.ui.tests.callhierarchy;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.callhierarchy.CHViewPart;
import org.eclipse.cdt.internal.ui.callhierarchy.CallHierarchyUI;
import org.eclipse.cdt.internal.ui.editor.CEditor;


public class CallHierarchyBugs extends CallHierarchyBaseTest {
	
	public CallHierarchyBugs(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(CallHierarchyBugs.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		restoreAllParts();
	}
	
	// class SomeClass {
	// public:
	//    void method();
	//    int field;
	// };

	// #include "SomeClass.h"
	// void SomeClass::method() {
	//    field= 1;
	// }
	public void testCallHierarchyFromOutlineView_183941() throws Exception {
		StringBuilder[] contents = getContentsForTest(2);
		IFile file1= createFile(getProject(), "SomeClass.h", contents[0].toString());
		IFile file2= createFile(getProject(), "SomeClass.cpp", contents[1].toString());
		waitForIndexer(fIndex, file2, CallHierarchyBaseTest.INDEXER_WAIT_TIME);

		final CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);
		final IViewPart outline= activateView(IPageLayout.ID_OUTLINE);
		final IWorkbenchWindow workbenchWindow = ch.getSite().getWorkbenchWindow();

		// open editor, check outline
		openEditor(file1);
		Tree outlineTree= checkTreeNode(outline, 0, "SomeClass").getParent();
		expandTreeItem(outlineTree, 0);
		TreeItem node= checkTreeNode(outlineTree, 0, 0, "method() : void");

		openCH(workbenchWindow, node);
		Tree chTree= checkTreeNode(ch, 0, "SomeClass::method() : void").getParent();
		checkTreeNode(chTree, 0, 1, null);
		
		ch.onSetShowReferencedBy(false);
		checkTreeNode(chTree, 0, "SomeClass::method() : void");
		checkTreeNode(chTree, 0, 0, "SomeClass::field : int");
	}
	
	// class SomeClass {
	// public:
	//    void ambiguous_impl();
	//    int ref1;
	//	  int ref2;
	// };
	//
	// void SomeClass::ambiguous_impl() {
	//    ref1= 1;
	// }
	// void other() {}

	// #include "SomeClass.h"
	// void SomeClass::ambiguous_impl() {
	//    ref2= 0;
	// }
	public void testCallHierarchyFromOutlineViewAmbiguous_183941() throws Exception {
		StringBuilder[] contents = getContentsForTest(2);
		IFile file1= createFile(getProject(), "SomeClass.h", contents[0].toString());
		IFile file2= createFile(getProject(), "SomeClass.cpp", contents[1].toString());
		waitForIndexer(fIndex, file2, CallHierarchyBaseTest.INDEXER_WAIT_TIME);

		final CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);
		final IViewPart outline= activateView(IPageLayout.ID_OUTLINE);
		final IWorkbenchWindow workbenchWindow = ch.getSite().getWorkbenchWindow();

		// open editor, check outline
		openEditor(file1);
		TreeItem node1= checkTreeNode(outline, 1, "SomeClass::ambiguous_impl() : void");
		Tree outlineTree= node1.getParent();
		TreeItem node2= checkTreeNode(outlineTree, 2, "other() : void");

		// open and check call hierarchy
		openCH(workbenchWindow, node1);
		ch.onSetShowReferencedBy(false);

		Tree chTree= checkTreeNode(ch, 0, "SomeClass::ambiguous_impl() : void").getParent();
		checkTreeNode(chTree, 0, 0, "SomeClass::ref1 : int");

		// open and check call hierarchy
		openCH(workbenchWindow, node2);
		checkTreeNode(chTree, 0, "other() : void");

		
		// open editor, check outline
		openEditor(file2);
		outlineTree= checkTreeNode(outline, 0, "SomeClass.h").getParent();
		node1= checkTreeNode(outlineTree, 1, "SomeClass::ambiguous_impl() : void");
		
		// open and check call hierarchy
		openCH(workbenchWindow, node1);
		ch.onSetShowReferencedBy(false);
		chTree= checkTreeNode(ch, 0, "SomeClass::ambiguous_impl() : void").getParent();
		checkTreeNode(chTree, 0, 0, "SomeClass::ref2 : int");
	}

	private void openCH(final IWorkbenchWindow workbenchWindow, TreeItem node1) {
		Object obj= node1.getData();
		assertTrue(obj instanceof ICElement);
		CallHierarchyUI.open(workbenchWindow, (ICElement) obj);
	}
	
	// class Base {
	// public:
	//    virtual void vmethod();
	//    void method();
	// };
	// class Derived : public Base {
	// public:
	//    void vmethod();
	//    void method();
	// };
	// void vrefs() {
	//    Base* b= 0;
	//    b->vmethod(); b->method();
	// }
	// void regRefs() {
	//    Base* b= 0;
	//    b->Base::vmethod(); b->Base::method(); 
	// }
	public void testPolyMorphicMethodCalls_156689() throws Exception {
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject(), "SomeClass.cpp", content);
		waitForIndexer(fIndex, file, CallHierarchyBaseTest.INDEXER_WAIT_TIME);

		final CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);
		final IWorkbenchWindow workbenchWindow = ch.getSite().getWorkbenchWindow();

		// open editor, check outline
		CEditor editor= openEditor(file);
		int idx = content.indexOf("vmethod");
		editor.selectAndReveal(idx, 0);
		openCallHierarchy(editor);

		Tree chTree= checkTreeNode(ch, 0, "Base::vmethod() : void").getParent();
		checkTreeNode(chTree, 0, 0, "regRefs() : void");
		checkTreeNode(chTree, 0, 1, "vrefs() : void");
		checkTreeNode(chTree, 0, 2, null);

		idx = content.indexOf("vmethod", idx+1);
		editor.selectAndReveal(idx, 0);
		openCallHierarchy(editor);

		chTree= checkTreeNode(ch, 0, "Derived::vmethod() : void").getParent();
		checkTreeNode(chTree, 0, 0, "vrefs() : void");
		checkTreeNode(chTree, 0, 1, null);

		idx = content.indexOf(" method")+1;
		editor.selectAndReveal(idx, 0);
		openCallHierarchy(editor);

		chTree= checkTreeNode(ch, 0, "Base::method() : void").getParent();
		checkTreeNode(chTree, 0, 0, "regRefs() : void");
		checkTreeNode(chTree, 0, 1, "vrefs() : void");
		checkTreeNode(chTree, 0, 2, null);

		idx = content.indexOf(" method", idx+1)+1;
		editor.selectAndReveal(idx, 0);
		openCallHierarchy(editor);

		chTree= checkTreeNode(ch, 0, "Derived::method() : void").getParent();
		checkTreeNode(chTree, 0, 0, null);
	}

	// class Base {
	// public:
	//    virtual void vmethod();
	// };
	// class Derived : public Base {
	// public:
	//    void vmethod();
	// };
	// void vrefs() {
	//    Base* b= 0;
	//    b->vmethod();
	// }
	public void testReversePolyMorphicMethodCalls_156689() throws Exception {
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject(), "SomeClass.cpp", content);
		waitForIndexer(fIndex, file, CallHierarchyBaseTest.INDEXER_WAIT_TIME);

		final CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);
		final IWorkbenchWindow workbenchWindow = ch.getSite().getWorkbenchWindow();

		// open editor, check outline
		CEditor editor= openEditor(file);
		int idx = content.indexOf("vrefs");
		editor.selectAndReveal(idx, 0);
		openCallHierarchy(editor, false);

		Tree chTree= checkTreeNode(ch, 0, "vrefs() : void").getParent();
		TreeItem item= checkTreeNode(chTree, 0, 0, "Base::vmethod() : void");
		checkTreeNode(chTree, 0, 1, null);

		expandTreeItem(item);
		checkTreeNode(item, 0, "Base::vmethod() : void");
		checkTreeNode(item, 1, "Derived::vmethod() : void");
		checkTreeNode(item, 2, null);
	}
	
	//	template <class T> class CSome {
	//		public:
	//			T Foo (const T& x) { return 2*x; }
	//	};
	//	template <> class CSome <int> {
	//		public:
	//			int Foo (const int& x) { return 3*x; }
	//	};
	//	void test() {
	//		CSome <int> X;
	//		X.Foo(3);
	//	}
	public void testMethodInstance_Bug240599() throws Exception {
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject(), "CSome.cpp", content);
		waitForIndexer(fIndex, file, CallHierarchyBaseTest.INDEXER_WAIT_TIME);

		final CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);
		final IWorkbenchWindow workbenchWindow = ch.getSite().getWorkbenchWindow();

		// open editor, check outline
		CEditor editor= openEditor(file);
		int idx = content.indexOf("Foo(3)");
		editor.selectAndReveal(idx, 0);
		openCallHierarchy(editor, true);
		Tree chTree= checkTreeNode(ch, 0, "CSome<int>::Foo(const int &) : int").getParent();
		TreeItem item= checkTreeNode(chTree, 0, 0, "test() : void");
		checkTreeNode(chTree, 0, 1, null);
	}
	
	//	class Base {
	//	public:
	//	   virtual void First() {}
	//	   virtual void Second() {}
	//	};
	//
	//	class Derived: public Base {
	//  public:
	//	   virtual void First() {}
	//	   virtual void Second() {}
	//	};
	//
	//	void func(Base *base) {
	//		base->First();
	//		base->Second();
	//	}
	//
	//	int main() {
	//		Derived derived;
	//		func(&derived);
	//		return 0;
	//	}
	public void testMultiplePolyMorphicMethodCalls_244987() throws Exception {
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject(), "SomeClass244987.cpp", content);
		waitForIndexer(fIndex, file, CallHierarchyBaseTest.INDEXER_WAIT_TIME);

		final CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);
		final IWorkbenchWindow workbenchWindow = ch.getSite().getWorkbenchWindow();

		// open editor, check outline
		CEditor editor= openEditor(file);
		int idx = content.indexOf("main");
		editor.selectAndReveal(idx, 0);
		openCallHierarchy(editor, false);

		Tree chTree= checkTreeNode(ch, 0, "main() : int").getParent();
		TreeItem ti= checkTreeNode(chTree, 0, 0, "func(Base *) : void");
		expandTreeItem(ti);
		checkTreeNode(chTree, 0, 1, null);

		TreeItem ti1= checkTreeNode(ti, 0, "Base::First() : void");
		expandTreeItem(ti1);
		TreeItem ti2= checkTreeNode(ti, 1, "Base::Second() : void");
		expandTreeItem(ti2);
		checkTreeNode(ti, 2, null);
		
		checkTreeNode(ti1, 0, "Base::First() : void");
		checkTreeNode(ti1, 1, "Derived::First() : void");
		checkTreeNode(ti1, 2, null);

		checkTreeNode(ti2, 0, "Base::Second() : void");
		checkTreeNode(ti2, 1, "Derived::Second() : void");
		checkTreeNode(ti2, 2, null);

	}

	//	#define MACRO(name) void PREFIX_ ## name(char *a , char *b)
	//	#define CALL(x) call(x)
	//
	//	void call(int);
	//	MACRO(Test) {
	//		CALL(0);
	//	}
	public void testMacrosHidingCall_249801() throws Exception {
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject(), "file249801.cpp", content);
		waitForIndexer(fIndex, file, CallHierarchyBaseTest.INDEXER_WAIT_TIME);

		final CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);

		// open editor, check outline
		CEditor editor= openEditor(file);
		int idx = content.indexOf("MACRO(Test");
		editor.selectAndReveal(idx, 0);
		openCallHierarchy(editor, false);

		Tree chTree= checkTreeNode(ch, 0, "PREFIX_Test(char *, char *) : void").getParent();
		TreeItem ti= checkTreeNode(chTree, 0, 0, "call(int) : void");

		idx = content.indexOf("CALL(0");
		editor.selectAndReveal(idx+4, 0);
		openCallHierarchy(editor, true);
		chTree= checkTreeNode(ch, 0, "call(int) : void").getParent();
		ti= checkTreeNode(chTree, 0, 0, "PREFIX_Test(char *, char *) : void");
	}

	//	void shared_func();

	//  #include "260262.h"

	//	void call() {
	//     shared_func();
	//	}
	public void testMultiLanguageWithPrototype_260262() throws Exception {
		final StringBuilder[] contents = getContentsForTest(3);
		final String hcontent = contents[0].toString();
		final String content_inc = contents[1].toString();
		final String content_full = content_inc + contents[2].toString();
		IFile header= createFile(getProject(), "260262.h", hcontent);
		IFile f1= createFile(getProject(), "260262.c", content_full);
		IFile f2= createFile(getProject(), "260262.cpp", content_inc);
		waitForIndexer(fIndex, f2, CallHierarchyBaseTest.INDEXER_WAIT_TIME);

		final CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);

		// open editor, check outline
		CEditor editor= openEditor(header);
		int idx = hcontent.indexOf("shared_func()");
		editor.selectAndReveal(idx, 0);
		openCallHierarchy(editor, true);

		Tree chTree= checkTreeNode(ch, 0, "shared_func() : void").getParent();
		TreeItem ti= checkTreeNode(chTree, 0, 0, "call() : void");
		checkTreeNode(chTree, 0, 1, null);
	}

	//	inline void shared_func() {}

	//  #include "260262.h"

	//	void call() {
	//     shared_func();
	//	}
	public void testMultiLanguageWithInlinedfunc_260262() throws Exception {
		final StringBuilder[] contents = getContentsForTest(3);
		final String hcontent = contents[0].toString();
		final String content_inc = contents[1].toString();
		final String content_full = content_inc + contents[2].toString();
		IFile header= createFile(getProject(), "260262.h", hcontent);
		IFile f1= createFile(getProject(), "260262.c", content_full);
		IFile f2= createFile(getProject(), "260262.cpp", content_inc);
		waitForIndexer(fIndex, f2, CallHierarchyBaseTest.INDEXER_WAIT_TIME);

		final CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);

		// open editor, check outline
		CEditor editor= openEditor(header);
		int idx = hcontent.indexOf("shared_func()");
		editor.selectAndReveal(idx, 0);
		openCallHierarchy(editor, true);

		Tree chTree= checkTreeNode(ch, 0, "shared_func() : void").getParent();
		TreeItem ti= checkTreeNode(chTree, 0, 0, "call() : void");
		checkTreeNode(chTree, 0, 1, null);
	}
	
	//	namespace {
	//		void doNothing()
	//		{
	//		}
	//	}
	//	int main() {
	//      doNothing();
	//		return 0;
	//	}
	public void testUnnamedNamespace_283679() throws Exception {
		final StringBuilder[] contents = getContentsForTest(1);
		final String content = contents[0].toString();
		IFile f2= createFile(getProject(), "testUnnamedNamespace_283679.cpp", content);
		waitForIndexer(fIndex, f2, CallHierarchyBaseTest.INDEXER_WAIT_TIME);

		final CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);

		// open editor, check outline
		CEditor editor= openEditor(f2);
		int idx = content.indexOf("doNothing()");
		editor.selectAndReveal(idx, 0);
		openCallHierarchy(editor, true);

		Tree chTree= checkTreeNode(ch, 0, "doNothing() : void").getParent();
		TreeItem ti= checkTreeNode(chTree, 0, 0, "main() : int");
		checkTreeNode(chTree, 0, 1, null);
	}

	
	//	class Base {
	//		public:
	//			virtual void dosomething() {}
	//	};
	//
	//	class Derived : public Base {
	//		public:
	//			void dosomething() { }
	//	};
	//
	//	void test() {
	//		Base *dbPtr = new Derived();
	//		dbPtr->dosomething();
	//		delete dbPtr;
	//	}
	public void testCallsToFromVirtualMethod_246064() throws Exception {
		final StringBuilder[] contents = getContentsForTest(1);
		final String content = contents[0].toString();
		IFile f2= createFile(getProject(), "testCallsToFromVirtualMethod_246064.cpp", content);
		waitForIndexer(fIndex, f2, CallHierarchyBaseTest.INDEXER_WAIT_TIME);

		final CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);

		// open editor, check outline
		CEditor editor= openEditor(f2);
		int idx = content.indexOf("dosomething();");
		editor.selectAndReveal(idx, 0);
		openCallHierarchy(editor, false);

		Tree chTree= checkTreeNode(ch, 0, "Base::dosomething() : void").getParent();
		TreeItem item= checkTreeNode(chTree, 0, 0, "Base::dosomething() : void");
		expandTreeItem(item);
		checkTreeNode(chTree, 0, 1, "Derived::dosomething() : void");
		checkTreeNode(chTree, 0, 2, null);
	}
	
	//	template<typename T> struct Array {
	//	      template<typename TIterator> void erase(TIterator it) {}
	//	};
	//
	//	int main() {
	//		Array<int> test;
	//		test.erase(1); 
	//	}
	public void testCallsToInstanceofSpecializedTemplate_361999() throws Exception {
		final String content = getAboveComment();
		IFile f2= createFile(getProject(), "testCallsToInstanceofSpecializedTemplate_361999.cpp", content);
		waitForIndexer(fIndex, f2, CallHierarchyBaseTest.INDEXER_WAIT_TIME);

		final CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);

		// open editor, check outline
		CEditor editor= openEditor(f2);
		int idx = content.indexOf("erase(TIterator it)");
		editor.selectAndReveal(idx, 0);
		openCallHierarchy(editor, true);

		Tree chTree= checkTreeNode(ch, 0, "Array<T>::erase(TIterator) : void").getParent();
		TreeItem ti= checkTreeNode(chTree, 0, 0, "main() : int");
		checkTreeNode(chTree, 0, 1, null);
	}
}
