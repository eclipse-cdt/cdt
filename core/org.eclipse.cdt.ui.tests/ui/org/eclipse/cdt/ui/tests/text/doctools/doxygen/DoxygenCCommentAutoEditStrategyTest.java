/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Sergey Prigogin, Google
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text.doctools.doxygen;

import java.util.HashMap;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.tests.text.DefaultCCommentAutoEditStrategyTest;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.doctools.DefaultMultilineCommentAutoEditStrategy;
import org.eclipse.cdt.ui.text.doctools.doxygen.DoxygenMultilineAutoEditStrategy;

import org.eclipse.cdt.internal.core.model.TranslationUnit;

import org.eclipse.cdt.internal.ui.text.CAutoIndentStrategy;
import org.eclipse.cdt.internal.ui.text.CTextTools;


/**
 * Testing the auto indent strategies.
 */
public class DoxygenCCommentAutoEditStrategyTest extends DefaultCCommentAutoEditStrategyTest {
	private HashMap fOptions;
	protected ICProject fCProject;
	
	/**
	 * @param name
	 */
	public DoxygenCCommentAutoEditStrategyTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(DoxygenCCommentAutoEditStrategyTest.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCCProject("test"+System.currentTimeMillis(), null);
		fOptions= CCorePlugin.getOptions();
	}

	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		CProjectHelper.delete(fCProject);
		CCorePlugin.setOptions(fOptions);
		super.tearDown();
	}

	private AutoEditTester createAutoEditTester() {
		CTextTools textTools = CUIPlugin.getDefault().getTextTools();
		IDocument doc = new Document();
		textTools.setupCDocument(doc);
		AutoEditTester tester = new AutoEditTester(doc, ICPartitions.C_PARTITIONING);


		tester.setAutoEditStrategy(IDocument.DEFAULT_CONTENT_TYPE, new CAutoIndentStrategy(ICPartitions.C_PARTITIONING, null));
		tester.setAutoEditStrategy(ICPartitions.C_MULTI_LINE_COMMENT, new DefaultMultilineCommentAutoEditStrategy());
		tester.setAutoEditStrategy(ICPartitions.C_PREPROCESSOR, new CAutoIndentStrategy(ICPartitions.C_PARTITIONING, null));
		return tester;
	}
	
	// /**X
	//  void foo() {}
	
	// /**
	//  * X
	//  */
	//  void foo() {}
	public void testAutoDocCommentContent1() throws CoreException {
		assertAutoEditBehaviour();
	}
	
	// /**   X
	//  void foo() {}
	
	// /**   
	//  * X
	//  */
	//  void foo() {}
	public void testAutoDocCommentContent2() throws CoreException {
		assertAutoEditBehaviour();
	}
	
	// /**X
	//  int foo_bar() {}
	
	// /**
	//  * X
	//  * @return
	//  */
	//  int foo_bar() {}
	public void testAutoDocCommentContent3() throws CoreException {
		assertAutoEditBehaviour();
	}
	
	// /**   X
	//  int foo_bar() {}
	
	// /**   
	//  * X
	//  * @return
	//  */
	//  int foo_bar() {}
	public void testAutoDocCommentContent4() throws CoreException {
		assertAutoEditBehaviour();
	}
	
	// /**X
	//  *
	//  */
	//  int foo_bar() {}
	
	// /**
	//  * X
	//  *
	//  */
	//  int foo_bar() {}
	public void testAutoDocCommentContent5() throws CoreException {
		assertAutoEditBehaviour();
	}
	
	// /**X
	//  void foo_bar(int x) {}
	
	// /**
	//  * X
	//  * @param x
	//  */
	//  void foo_bar(int x) {}
	public void testAutoDocCommentContent6() throws CoreException {
		assertAutoEditBehaviour();
	}
	
	// class A {}; class B {};
	// /**X
	//  C* bar_baz(A a, B b, int c) {}
	
	// class A {}; class B {};
	// /**
	//  * X
	//  * @param a
	//  * @param b
	//  * @param c
	//  * @return
	//  */
	//  C* bar_baz(A a, B b, int c) {}
	public void testAutoDocCommentContent7() throws CoreException {
		assertAutoEditBehaviour();
	}
	
	// /**
	//  * namespace
	//  */
	// namespace NS {
    // class A {
	//    private:
	//    /*
	//     * TODO
	//     */
	//    /*!
	//     * class
	//     */
	//    class B {
	//       public:
	//       /**
	//        *
	//        */
	//        void foo() {}
	//        /*!X
	//        A bar(A as[], B bs[]) {}
	//    };
	// };
	// }
	
	// /**
	//  * namespace
	//  */
	// namespace NS {
    // class A {
	//    private:
	//    /*
	//     * TODO
	//     */
	//    /*!
	//     * class
	//     */
	//    class B {
	//       public:
	//       /**
	//        *
	//        */
	//        void foo() {}
	//        /*!
	//         * X
	//         * @param as
	//         * @param bs
	//         * @return
	//         */
	//        A bar(A as[], B bs[]) {}
	//    };
	// };
	// }
	public void testAutoDocCommentContent8() throws CoreException {
		assertAutoEditBehaviour();
	}
	
	// void foo_bar(int x)
	// /**X
	// {}
	
	// void foo_bar(int x)
	// /**
	//  * X
	//  * @param x
	//  */
	// {}
	public void _testAutoDocCommentContent9() throws CoreException {
		assertAutoEditBehaviour();
		// TODO - desired behaviour when there is a comment preceding the declaration
		// needs defining
	}
	
	// void foo_bar(int x)
	// {
	// /**X
	// }
	
	// void foo_bar(int x)
	// {
	// /**
	//  * X
	//  * @param x
	//  */
	// }
	public void _testAutoDocCommentContent10() throws CoreException {
		assertAutoEditBehaviour();
		// TODO - desired behaviour when there is a comment preceding the declaration
		// or when there is a comment after the signature but before the brace, both need defining
	}
	
	// /*!X
	// enum A { B, C };
	
	// /*!
	//  * X
	//  */
	// enum A { B, C };
	public void testAutoDocCommentContent11() throws CoreException {
		assertAutoEditBehaviour();
	}
	
	// /**X
	// enum A { B,
	//     C };

	// /**
	//  * X
	//  */
	// enum A { B,//!< B
	//     C };   //!< C
	public void testAutoDocCommentContent13() throws CoreException {
		assertAutoEditBehaviour();
	}
	
	// /**X
	// enum A { B,
	//     C };//!< C

	// /**
	//  * X
	//  */
	// enum A { B,//!< B
	//     C };//!< C
	public void testAutoDocCommentContent14() throws CoreException {
		assertAutoEditBehaviour();
	}
	
	// /**X
	// enum A { B,//!< B
	//     C };

	// /**
	//  * X
	//  */
	// enum A { B,//!< B
	//     C };//!< C
	public void testAutoDocCommentContent15() throws CoreException {
		assertAutoEditBehaviour();
	}
	
	// /**X
	// enum A { B,
	// 		C };

	// /**
	//  * X
	//  */
	// enum A { B,//!< B
	// 		C };  //!< C
	public void _testAutoDocCommentContent16() throws CoreException {
		/*
		 * Indenting in the presence of tabs is not handled at the moment.
		 */
		assertAutoEditBehaviour();
	}
	
	protected void assertAutoEditBehaviour() throws CoreException {
		CTextTools textTools = CUIPlugin.getDefault().getTextTools();
		final IDocument doc = new Document();
		textTools.setupCDocument(doc);
		
		StringBuffer[] raw= getTestContents();
		String init= raw[0].toString(), expected= raw[1].toString();

		int caretInit= init.indexOf('X');
		init= init.replaceFirst("X", "");

		int caretExpected= expected.indexOf('X');
		expected= expected.replaceFirst("X", "");
				
		
		DoxygenMultilineAutoEditStrategy ds= new DoxygenMultilineAutoEditStrategy() {
			public IASTTranslationUnit getAST() {
				final IFile file= fCProject.getProject().getFile("testContent.cpp");
				try {
					TestSourceReader.createFile(fCProject.getProject(), "testContent.cpp", doc.get());
					String id = CoreModel.getRegistedContentTypeId(file.getProject(), file.getName());
					return new TranslationUnit(fCProject, file, id).getAST();
				} catch(CoreException ce) {
					assertTrue("Could not get test content AST", false);
					return null;
				}
			}
		};


		doc.set(init);
		int caretActual= -1;
		try {
			TestDocumentCommand dc= new TestDocumentCommand(caretInit, 0, "\n");
			ds.customizeDocumentCommand(doc, dc);
			caretActual= dc.exec(doc);
		} catch(BadLocationException ble) {
			fail(ble.getMessage());
		}
		String actual= doc.get();
		assertEquals(expected, actual);
		assertEquals(caretExpected, caretActual);
	}
}
