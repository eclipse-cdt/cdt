/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import static org.eclipse.cdt.ui.tests.text.contentassist2.AbstractContentAssistTest.CompareType.CONTEXT;
import static org.eclipse.cdt.ui.tests.text.contentassist2.AbstractContentAssistTest.CompareType.REPLACEMENT;

import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class CompletionTestBase extends AbstractContentAssistTest {
	private static final String HEADER_FILE_NAME = "CompletionTest.h";
	private static final String SOURCE_FILE_NAME = "CompletionTest.cpp";
	private static final String CURSOR_LOCATION_TAG = "/*cursor*/";

	protected int fCursorOffset;
	protected IProject fProject;

	public CompletionTestBase(String name) {
		super(name, true);
	}

	/*
	 * @see org.eclipse.cdt.ui.tests.text.contentassist2.AbstractCompletionTest#setUpProjectContent(org.eclipse.core.resources.IProject)
	 */
	@Override
	protected IFile setUpProjectContent(IProject project) throws Exception {
		fProject = project;
		String headerContent = readTaggedComment(CompletionTestBase.class, HEADER_FILE_NAME);
		StringBuilder sourceContent = getContentsForTest(1)[0];
		sourceContent.insert(0, "#include \"" + HEADER_FILE_NAME + "\"\n");
		fCursorOffset = sourceContent.indexOf(CURSOR_LOCATION_TAG);
		assertTrue("No cursor location specified", fCursorOffset >= 0);
		sourceContent.delete(fCursorOffset, fCursorOffset + CURSOR_LOCATION_TAG.length());
		assertNotNull(createFile(project, HEADER_FILE_NAME, headerContent));
		return createFile(project, SOURCE_FILE_NAME, sourceContent.toString());
	}

	protected static final int DEFAULT_FLAGS = AbstractContentAssistTest.DEFAULT_FLAGS | IS_COMPLETION;

	protected void assertCompletionResults(int offset, String[] expected, CompareType compareType) throws Exception {
		assertContentAssistResults(offset, expected, DEFAULT_FLAGS, compareType);
	}

	protected void assertMinimumCompletionResults(int offset, String[] expected, CompareType compareType)
			throws Exception {
		assertContentAssistResults(offset, expected, DEFAULT_FLAGS | ALLOW_EXTRA_RESULTS, compareType);
	}

	protected void assertOrderedCompletionResults(int offset, String[] expected, CompareType compareType)
			throws Exception {
		assertContentAssistResults(offset, expected, DEFAULT_FLAGS | CHECK_ORDER, compareType);
	}

	protected void assertCompletionResults(String[] expected) throws Exception {
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	protected void assertOrderedCompletionResults(String[] expected) throws Exception {
		assertOrderedCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	protected void assertParameterHint(String[] expected) throws Exception {
		assertContentAssistResults(fCursorOffset, expected, AbstractContentAssistTest.DEFAULT_FLAGS, CONTEXT);
	}

	protected void assertCursorPositionsAfterReplacement(int[] expected) throws Exception {
		Object[] results = invokeContentAssist(fCursorOffset, 0, true, false, true).results;
		assertEquals(expected.length, results.length);
		for (int i = 0; i < results.length; ++i) {
			assertInstance(results[i], ICompletionProposal.class);
			assertEquals(expected[i], ((ICompletionProposal) results[i]).getSelection(getDocument()).x);
		}
	}

	protected void assertDotReplacedWithArrow() throws Exception {
		assertEquals("->", getDocument().get(fCursorOffset - 1, 2));
	}

	protected static void setDisplayDefaultArguments(boolean value) {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(ContentAssistPreference.DEFAULT_ARGUMENT_DISPLAY_ARGUMENTS, value);
	}

	protected void setReplaceDotWithArrow(boolean value) {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_REPLACE_DOT_WITH_ARROW, value);
		fProcessorNeedsConfiguring = true; // to pick up the modified auto-activation preference
	}

	protected static void setDisplayDefaultedParameters(boolean value) {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(ContentAssistPreference.DEFAULT_ARGUMENT_DISPLAY_PARAMETERS_WITH_DEFAULT_ARGUMENT,
				value);
	}

	protected static void enableParameterGuessing(boolean value) {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(ContentAssistPreference.GUESS_ARGUMENTS, value);
	}

	//	{CompletionTest.h}
	//	class C1;
	//	class C2;
	//	class C3;
	//
	//	extern C1* gC1;
	//	C2* gC2 = 0;
	//
	//	extern C1* gfC1();
	//	C2* gfC2();
	//
	//	enum E1 {e11, e12};
	//
	//	class C1 {
	//		public:
	//			enum E2 {e21, e22};
	//
	//			C1* fMySelf;
	//			void iam1();
	//
	//			C1* m123();
	//			C1* m12();
	//			C1* m13();
	//
	//			protected:
	//				void m1protected();
	//			private:
	//				void m1private();
	//	};
	//	typedef C1 T1;
	//	using A1 = C1;
	//
	//	class C2 : public T1 {
	//		public:
	//			C2* fMySelf;
	//	void iam2();
	//
	//	C2* m123();
	//	C2* m12();
	//	C2* m23();
	//	C1* operator()(int x);
	//
	//	protected:
	//		void m2protected();
	//	private:
	//		void m2private();
	//	friend void _friend_function(C3* x);
	//	friend class _friend_class;
	//	};
	//	typedef C2 T2;
	//
	//	class C3 : public C2 {
	//		public:
	//			C3* fMySelf;
	//	void iam3();
	//
	//	C3* m123();
	//	C3* m13();
	//
	//	template<typename T> T tConvert();
	//	protected:
	//		void m3protected();
	//	private:
	//		void m3private();
	//	};
	//	typedef C3 T3;
	//
	//	namespace ns {
	//		const int NSCONST= 1;
	//		class CNS {
	//			void mcns();
	//		};
	//	};
	//	template <class T> class TClass {
	//		T fTField;
	//		public:
	//			TClass(T tArg) : fTField(tArg) {
	//			}
	//			T add(T tOther) {
	//				return fTField + tOther;
	//			}
	//		class NestedClass{};
	//	};
	//	// bug 109480
	//	class Printer
	//	{
	//		public:
	//			static void InitPrinter(unsigned char port);
	//	private:
	//		//Storage for port printer is on
	//		static unsigned char port;
	//	protected:
	//	};
	//	struct Struct1;
	//	struct Struct2;
	//	union Union1;
	//	union Union2;
	//	struct s206450 {
	//		struct {int a1; int a2;};
	//		union {int u1; char u2;};
	//		struct {int a3;} a4;
	//		int b;
	//	};
	//	typedef enum {__nix} _e204758;
	//	void _f204758(_e204758 x);
	//
	// // Bug 331056
	//	namespace _A_331056 {
	//		class Reference {};
	//	}
	//	namespace _B_331056 {
	//		using ::_A_331056::Reference;
	//	}
	//
	//	template<typename T1, typename T2>
	//	struct Specialization {
	//	};
	//	template<typename T2>
	//	struct Specialization<int, T2> {
	//	};
	//	template<>
	//	struct Specialization<int, int> {
	//	};
	//
	//	template<typename T1, typename T2>
	//	using AliasForSpecialization = Specialization<T1, T2>;
	//
	//	template<typename T1, typename T2>
	//	using AliasForTemplateAlias = AliasForSpecialization<T1, T2>;
	//
	//  namespace bug521016 {
	//    template <int TPA>
	//    struct A {
	//      template <int TPAA>
	//      struct AA {
	//        struct AAA {
	//          static void test();
	//        };
	//      };
	//    };
	//
	//    struct B {
	//      using Test = A<0>::AA<0>;
	//    };
	//  }
}
