/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.qt.tests;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.internal.core.model.ext.SourceRange;
import org.eclipse.cdt.internal.qt.core.index.IQMethod;
import org.eclipse.cdt.internal.qt.core.index.IQObject;
import org.eclipse.cdt.internal.qt.core.index.QtIndex;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchResult;
import org.eclipse.cdt.internal.ui.search.CSearchTextSelectionQuery;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

@SuppressWarnings("restriction")
public class QtRegressionTests extends BaseQtTestCase {

	private static Map<String, Set<String>> buildExpectedMap(String mocOutput) {
		Map<String, Set<String>> expected = new HashMap<>();
		for (String moc_signature : mocOutput.split("\0")) {
			String name = moc_signature.split("\\(")[0];
			Set<String> set = expected.get(name);
			if (set == null) {
				set = new HashSet<>();
				expected.put(name, set);
			}
			set.add(moc_signature);
		}

		return expected;
	}

	// #include "junit-QObject.hh"
	// struct T {};
	// class Q : public QObject
	// {
	// Q_OBJECT
	// public:
	//     void func();
	// signals:
	//     void sig_int(int i = 5);
	//     void sig_const_int(const int i = 5);
	//     void sig_T(T * t = 0);
	//     void sig_const_T(const T * const t = 0);
	// };
	public void testDefaultParameters() throws Exception {
		loadComment("defaultParams.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject qobj = qtIndex.findQObject(new String[] { "Q" });
		if (!isIndexOk("Q", qobj))
			return;
		assertNotNull(qobj);

		// Based on the moc output, but modified to manage our handling for default parameters.  The
		// moc generates two signatures, sig(N::TS::M) and sig(), we just mark optional parameters
		// with a trailing =.  However, QMethod#getSignature is currently modified to strip the
		// default value indication.  So, we're only dealing with the full signature here.
		String moc = "sig_int(int)\0sig_int()\0sig_const_int(int)\0" + "sig_const_int()\0sig_T(T*)\0sig_T()\0"
				+ "sig_const_T(T*const)\0sig_const_T()\0";
		Map<String, Set<String>> expected = buildExpectedMap(moc);

		IQObject.IMembers<IQMethod> sigs = qobj.getSignals();
		assertNotNull(sigs);
		Collection<IQMethod> locals = sigs.locals();
		assertNotNull(locals);
		for (IQMethod method : locals) {
			Set<String> set = expected.get(method.getName());
			assertNotNull("unexpected method " + method.getName() + " (" + method.getSignatures() + ')', set);
			for (String signature : method.getSignatures()) {
				assertTrue(set.remove(signature));
			}
			assertTrue("did not find all signatures for " + method.getName(), set.isEmpty());
			expected.remove(method.getName());
		}
		assertEquals(0, expected.size());
	}

	// #include "junit-QObject.hh"
	// typedef int Tl1;
	// typedef Tl1 Tl2;
	// enum _E {};
	// struct S { typedef int M; typedef char Mb; };
	// template<typename T> struct S_TEMPLATE { };
	// namespace N
	// {
	//     typedef int Ib;
	//     typedef _E  Eb;
	//     enum E {};
	//     namespace N2 { enum E2 {}; typedef E2 TE2; }
	//     typedef E   TEa;
	//     typedef TEa TEb;
	//     typedef N2::E2  N2_E2;
	//     typedef N2::TE2 N2_TE2;
	//     typedef S TS;
	// }
	// typedef N::E N_E;
	// namespace N2 { typedef N::Ib Ic; }
	// class Q : public QObject
	// {
	// Q_OBJECT
	// public:
	//     void func();
	// signals:
	//     void sig_int(int);
	//     void sig_Tl1(Tl1);
	//     void sig_Tl2(Tl2);
	//     void sig_int_ptr(int *);
	//     void sig_Tl1_ptr(Tl1 *);
	//     void sig_Tl2_ptr(Tl2 *);
	//
	//     void sig_qual1(N::E);
	//     void sig_qual2(N::N2::E2);
	//     void sig_typedef1(N_E);
	//     void sig_typedef2(N::TEa);
	//     void sig_typedef3(N::TEb);
	//     void sig_typedef4(N::N2_E2);
	//     void sig_typedef5(N::N2_TE2);
	//     void sig_typedef6(N::N2::TE2);
	//
	//     void sig_nested1(S::Mb);
	//     void sig_nested2(N::TS::M);
	//     void sig_nested3(N::Ib);
	//     void sig_nested4(N::Eb);
	//     void sig_nested5(N2::Ic);
	//
	//     void   sig_S          (      S        ); //   sig_S          (      S        )
	//     void const_S          (const S        ); // const_S          (      S        )
	//     void       S_const    (      S const  ); //       S_const    (      S        )
	//     void       S_ref      (      S &      ); //       S_ref      (      S &      )
	//     void const_S_ref      (const S &      ); // const_S_ref      (      S        )
	//     void       S_const_ref(      S const &); //       S_const_ref(      S        )
	//     void       S_ptr      (      S *      );
	//     void       S_ptr_const(      S * const);
	//     void const_S_ptr      (const S *      );
	//     void const_S_ptr_const(const S * const);
	//     void const_S_ptr_const_def(const S * const s = 0);
	//     void       S_ptr_ref      (      S *       &);
	//     void       S_ptr_const_ref(      S * const &);
	//     void const_S_ptr_const_ref(const S * const &);
	//     void       S_ptr_ptr      (      S *       *);
	//     void       S_ptr_const_ptr(      S * const *);
	//     void const_S_ptr_ptr      (const S *       *);
	//     void const_S_ptr_const_ptr(const S * const *);
	//     void       S_ptr_ptr_const      (      S *       * const);
	//     void       S_ptr_const_ptr_const(      S * const * const);
	//     void const_S_ptr_ptr_const      (const S *       * const);
	//     void const_S_ptr_const_ptr_const(const S * const * const);
	//
	//     void S_template_1(const S_TEMPLATE<const S *> & p);
	//     void S_template_2(const S_TEMPLATE<S const *> & p);
	//     void S_template_3(S_TEMPLATE<const S *> const & p);
	//     void S_template_4(S_TEMPLATE<S const *> const & p);
	//     void S_template_X(const S_TEMPLATE<const S const *> const & p);
	// };
	public void testBug338930() throws Exception {
		loadComment("bug338930.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject qobj = qtIndex.findQObject(new String[] { "Q" });
		if (!isIndexOk("Q", qobj))
			return;
		assertNotNull(qobj);

		// Copy and pasted moc output (signals only) to make sure we're getting an exact match.
		String moc_output = "sig_int(int)\0" + "sig_Tl1(Tl1)\0sig_Tl2(Tl2)\0sig_int_ptr(int*)\0"
				+ "sig_Tl1_ptr(Tl1*)\0sig_Tl2_ptr(Tl2*)\0" + "sig_qual1(N::E)\0sig_qual2(N::N2::E2)\0"
				+ "sig_typedef1(N_E)\0sig_typedef2(N::TEa)\0" + "sig_typedef3(N::TEb)\0sig_typedef4(N::N2_E2)\0"
				+ "sig_typedef5(N::N2_TE2)\0" + "sig_typedef6(N::N2::TE2)\0sig_nested1(S::Mb)\0"
				+ "sig_nested2(N::TS::M)\0sig_nested3(N::Ib)\0" + "sig_nested4(N::Eb)\0sig_nested5(N2::Ic)\0"

				+ "sig_S(S)\0const_S(S)\0S_const(S)\0S_ref(S&)\0" + "const_S_ref(S)\0S_const_ref(S)\0S_ptr(S*)\0"
				+ "S_ptr_const(S*const)\0const_S_ptr(const S*)\0"
				+ "const_S_ptr_const(S*const)\0const_S_ptr_const_def(S*const)\0"
				+ "const_S_ptr_const_def()\0S_ptr_ref(S*&)\0" + "S_ptr_const_ref(S*)\0const_S_ptr_const_ref(S*const)\0"
				+ "S_ptr_ptr(S**)\0S_ptr_const_ptr(S*const*)\0" + "const_S_ptr_ptr(const S**)\0"
				+ "const_S_ptr_const_ptr(const S*const*)\0" + "S_ptr_ptr_const(S**const)\0"
				+ "S_ptr_const_ptr_const(S*const*const)\0" + "const_S_ptr_ptr_const(S**const)\0"
				+ "const_S_ptr_const_ptr_const(S*const*const)\0" + "S_template_1(S_TEMPLATE<const S*>)\0"
				+ "S_template_2(S_TEMPLATE<const S*>)\0" + "S_template_3(S_TEMPLATE<const S*>)\0"
				+ "S_template_4(S_TEMPLATE<const S*>)\0" + "S_template_X(S_TEMPLATE<const S*>)";

		Map<String, Set<String>> expected = buildExpectedMap(moc_output);

		IQObject.IMembers<IQMethod> sigs = qobj.getSignals();
		assertNotNull(sigs);
		Collection<IQMethod> locals = sigs.locals();
		assertNotNull(locals);
		for (IQMethod method : locals) {
			Set<String> set = expected.get(method.getName());
			assertNotNull("unexpected signal " + method.getName() + " (" + method.getSignatures() + ')', set);
			for (String signature : method.getSignatures())
				assertTrue(set.remove(signature));
			assertTrue("did not find all signatures for " + method.getName(), set.isEmpty());
			expected.remove(method.getName());
		}
		assertEquals(0, expected.size());
	}

	// #include "junit-QObject.hh"
	// class Q : public QObject
	// {
	// Q_OBJECT
	// Q_SLOT void const_ref(const QString &);
	// Q_SLOT void const_val(const QString  );
	// Q_SLOT void reference(      QString &);
	// Q_SLOT void value(          QString  );
	// enum E { };
	// Q_SIGNAL void signalEnum_const_ref(const E &);
	// Q_SIGNAL void signalEnum_reference(E &);
	// Q_SIGNAL void signalEnum_qualified(Q::E);
	//     void func()
	//     {
	//         connect(this, SIGNAL(destroyed(QObject*),      this, SLOT(const_ref(QString))));
	//         connect(this, SIGNAL(destroyed(QObject*),      this, SLOT(const_val(QString))));
	//         connect(this, SIGNAL(signalEnum_const_ref(E),  this, SLOT(reference(QString&))));
	//         connect(this, SIGNAL(signalEnum_reference(E&), this, SLOT(value(QString))));
	//     }
	// };
	public void testBug344931() throws Exception {
		loadComment("bug344931.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject qobj = qtIndex.findQObject(new String[] { "Q" });
		if (!isIndexOk("Q", qobj))
			return;
		assertNotNull(qobj);

		IQObject.IMembers<IQMethod> slotMembers = qobj.getSlots();
		assertNotNull(slotMembers);

		Collection<IQMethod> slots = slotMembers.locals();
		assertNotNull(slots);
		assertEquals(4, slots.size());

		for (IQMethod slot : slots) {
			if ("const_ref".equals(slot.getName()))
				assertTrue(slot.getSignatures().contains("const_ref(QString)"));
			else if ("const_val".equals(slot.getName()))
				assertTrue(slot.getSignatures().contains("const_val(QString)"));
			else if ("reference".equals(slot.getName()))
				assertTrue(slot.getSignatures().contains("reference(QString&)"));
			else if ("value".equals(slot.getName()))
				assertTrue(slot.getSignatures().contains("value(QString)"));
			else
				fail("unexpected slot " + slot.getName());
		}

		IQObject.IMembers<IQMethod> signalMembers = qobj.getSignals();
		assertNotNull(signalMembers);

		Collection<IQMethod> signals = signalMembers.locals();
		assertNotNull(signals);
		assertEquals(3, signals.size());

		for (IQMethod signal : signals) {
			if ("signalEnum_const_ref".equals(signal.getName()))
				assertTrue(signal.getSignatures().contains("signalEnum_const_ref(E)"));
			else if ("signalEnum_reference".equals(signal.getName()))
				assertTrue(signal.getSignatures().contains("signalEnum_reference(E&)"));
			else if ("signalEnum_qualified".equals(signal.getName()))
				assertTrue(signal.getSignatures().contains("signalEnum_qualified(Q::E)"));
			else
				fail("unexpected signal " + signal.getName());
		}
	}

	// #include "junit-QObject.hh"
	// class Q : public QObject
	// {
	// Q_OBJECT
	// public:
	//     void func();
	// private slots:
	//     void slot1();
	// private:
	// Q_SLOT void slot2();
	// Q_SLOT void slot3();
	// };
	// void Q::slot1() { }
	// void Q::slot2() { }
	// void Q::func()
	// {
	//     QObject::connect( this, destroyed( QObject * ), this, slot1() );
	//     QObject::connect( this, destroyed( QObject * ), this, slot2() );
	//     QObject::connect( this, destroyed( QObject * ), this, slot3() );
	// }
	public void testSlotDefn() throws Exception {
		loadComment("slotDefn.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject qobj = qtIndex.findQObject(new String[] { "Q" });
		if (!isIndexOk("Q", qobj))
			return;
		assertNotNull(qobj);

		IQObject.IMembers<IQMethod> slots = qobj.getSlots();
		assertNotNull(slots);
		Collection<IQMethod> localSlots = slots.locals();
		assertNotNull(localSlots);

		// make sure that the three slot functions are found, but none of the inherited or
		// non-slot functions
		Set<String> expected = new HashSet<>(Arrays.asList("slot1", "slot2", "slot3"));
		for (IQMethod method : localSlots)
			assertTrue("unexpected slot " + method.getName(), expected.remove(method.getName()));
		assertEquals("missing slots " + expected.toString(), 0, expected.size());
	}

	// #include "junit-QObject.hh"
	// class Q : public QObject
	// {
	// Q_OBJECT
	// Q_SIGNAL void signal1();
	// Q_SLOT   void slot1();
	//     void function()
	//     {
	//         signal1();
	//         QObject::connect(
	//             this, SIGNAL( signal1() ),
	//             this, SLOT( slot1() ) );
	//     }
	// };
	public void testBug424499_FindQMethodReferences() throws Exception {
		String filename = "signalRefs.hh";
		loadComment(filename);
		waitForIndexer(fCProject);

		// The search query uses the ASTProvider which relies on the target translation unit being
		// loaded in a CEditor.  The following opens a CEditor for the test file so that it will
		// be the one used by the ASTProvider.
		IFile file = fProject.getFile(filename);
		assertNotNull(file);
		assertTrue(file.exists());
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		assertNotNull(page);
		IEditorPart editor = IDE.openEditor(page, file, CUIPlugin.EDITOR_ID);
		assertNotNull(editor);
		CEditor ceditor = editor.getAdapter(CEditor.class);
		assertNotNull(ceditor);

		// NOTE: This offset relies on the above comment being exactly as expected.  If it is edited,
		//       then this offset should be adjusted to match.  It needs to put the cursor in the
		//       declaration for signal1.
		ceditor.setSelection(new SourceRange(86, 0), true);
		ISelection sel = ceditor.getSelectionProvider().getSelection();
		assertNotNull(sel);
		assertTrue(sel instanceof ITextSelection);

		// Now a query is created and executed.
		CSearchTextSelectionQuery query = new CSearchTextSelectionQuery(null, ceditor.getInputCElement(),
				(ITextSelection) sel, IIndex.FIND_REFERENCES);

		// The query sometimes fails (with Status.CANCEL_STATUS) if the TU is not open.  I
		// haven't found a way to be notified when the TU gets "opened" -- the test case just
		// looks that case and then try again.
		IStatus status = null;
		long end_ms = System.currentTimeMillis() + 1000;
		do {
			status = query.run(npm());
			if (status == Status.CANCEL_STATUS) {
				Thread.sleep(100);
			}
		} while (!status.isOK() && System.currentTimeMillis() < end_ms);
		assertTrue("query failed: " + status.getMessage(), status.isOK());

		// The query should have found two references, one for the function call and another
		// for the SIGNAL expansion.
		ISearchResult result = query.getSearchResult();
		assertNotNull(result);
		assertTrue(result instanceof CSearchResult);

		CSearchResult searchResult = (CSearchResult) result;
		assertEquals(2, searchResult.getMatchCount());
	}
}
