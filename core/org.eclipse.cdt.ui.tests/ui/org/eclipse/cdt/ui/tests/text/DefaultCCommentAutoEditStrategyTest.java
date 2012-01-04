/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.HashMap;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.doctools.DefaultMultilineCommentAutoEditStrategy;

import org.eclipse.cdt.internal.ui.text.CAutoIndentStrategy;
import org.eclipse.cdt.internal.ui.text.CTextTools;

/**
 * Testing the auto indent strategies.
 */
public class DefaultCCommentAutoEditStrategyTest extends AbstractAutoEditTest {
	private HashMap<String, String> fOptions;

	/**
	 * @param name
	 */
	public DefaultCCommentAutoEditStrategyTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(DefaultCCommentAutoEditStrategyTest.class);
	}

	/*
	 * @see org.eclipse.cdt.core.testplugin.util.BaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fOptions= CCorePlugin.getOptions();
	}

	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
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

	public void testIsMultilineNew() throws BadLocationException {
		DefaultMultilineCommentAutoEditStrategy ds= new DefaultMultilineCommentAutoEditStrategy();
		CTextTools textTools = CUIPlugin.getDefault().getTextTools();
		IDocument doc = new Document();
		textTools.setupCDocument(doc);

		doc.set(" /*  ");
		assertTrue(ds.shouldCloseMultiline(doc, 3));
		doc.set(" /*  \n");
		assertTrue(ds.shouldCloseMultiline(doc, 3));
		assertTrue(ds.shouldCloseMultiline(doc, 5));
		doc.set(" /*  \n  ");
		assertTrue(ds.shouldCloseMultiline(doc, 3));
		assertTrue(ds.shouldCloseMultiline(doc, 6));
		doc.set(" /*  */");
		assertFalse(ds.shouldCloseMultiline(doc, 5));
		doc.set(" /*  */ ");
		assertFalse(ds.shouldCloseMultiline(doc, 5));
		doc.set(" /* \n\n */ ");
		assertFalse(ds.shouldCloseMultiline(doc, 5));
		doc.set(" /* \n\n */ \n /*");
		assertTrue(ds.shouldCloseMultiline(doc, 14));
		doc.set(" /* \n\n */ \n /* ");
		assertTrue(ds.shouldCloseMultiline(doc, 14));
		doc.set(" /* \n\n */ \n /* \n");
		assertTrue(ds.shouldCloseMultiline(doc, 14));
		doc.set(" /* /* \n\n */ \n /* \n");
		assertTrue(ds.shouldCloseMultiline(doc, 3));
		doc.set(" /* \n/* \n\n */ \n /* \n");
		assertTrue(ds.shouldCloseMultiline(doc, 3));
		doc.set(" /* \n\n/* \n\n */ \n /* \n");
		assertTrue(ds.shouldCloseMultiline(doc, 3));
		doc.set(" /* \n\n/* \n\n */ \n /* \n");
		assertTrue(ds.shouldCloseMultiline(doc, 3));
		doc.set(" /* \n\n                 */ /* \n\n */ \n /* \n");
		assertFalse(ds.shouldCloseMultiline(doc, 3));
		doc.set(" /*\n /*\n * \n * \n */\n");
		assertFalse(ds.shouldCloseMultiline(doc, 8));
	}

	// /*X

	// /*
	//  * X
	//  */
	public void testInsertNewLine1() {
		assertNewLineBehaviour();
	}

	//   /*X

	//   /*
	//    * X
	//    */
	public void testInsertNewLine2() {
		assertNewLineBehaviour();
	}	

	// class A {};  /*X

	// class A {};  /*
	// X
	public void testInsertNewLine3() {
		assertNewLineBehaviour();
	}

	// class A {
	// /*X
	// };  

	// class A {
	// /*
	//  * X
	//  */
	// };  
	public void testInsertNewLine4() {
		assertNewLineBehaviour();
	}

	// class A {
	// /* X
	// };  

	// class A {
	// /* 
	//  * X
	//  */
	// };  
	public void testInsertNewLine5() {
		assertNewLineBehaviour();
	}

	
	
	// class A {
	// /*X
	//  * 
	//  */
	// };

	// class A {
	// /*
	//  * X
	//  * 
	//  */
	// };
	public void testInsertNewLine6() {
		assertNewLineBehaviour();
	}

	// class A {
	// /*  
	//  *X
	//  */
	// };

	// class A {
	// /*  
	//  *
	//  *X
	//  */
	// };
	public void testInsertNewLine7() {
		assertNewLineBehaviour();
	}

	// class A {
	// /*  
	//  *X 
	//  */
	// };

	// class A {
	// /*  
	//  *
	//  *X 
	//  */
	// };
	public void testInsertNewLine8() {
		assertNewLineBehaviour();
	}

	// class A {
	// /*  
	//  * abcd def ghiX
	//  */
	// };

	// class A {
	// /*  
	//  * abcd def ghi
	//  * X
	//  */
	// };
	public void testInsertNewLine9() {
		assertNewLineBehaviour();
	}

	// class A {
	// /*  
	//  * abcd deXf ghi
	//  */
	// };

	// class A {
	// /*  
	//  * abcd de
	//  * Xf ghi
	//  */
	// };
	public void testInsertNewLine10() {
		assertNewLineBehaviour();
	}

	// class A {
	// /* 
	//  * 
	//  */X
	// };

	// class A {
	// /* 
	//  * 
	//  */
	// X
	// };
	public void _testInsertNewLine11() { // this is correct - we're not in a dccaes partition
		assertNewLineBehaviour();
	}

	// /*X*/	

	// /*
	//  * X 
	//  */
	public void _testInsertNewLine12() {
		assertNewLineBehaviour();
	}

	// class A {
	// /*Xfoo
	// };  

	// class A {
	// /*
	//  * X
	//  */foo
	// };  
	public void testInsertNewLine13() {
		assertNewLineBehaviour();
	}

	// class A {
	// /*fooX
	// };  

	// class A {
	// /*foo
	//  * X
	//  */
	// };  
	public void testInsertNewLine14() {
		assertNewLineBehaviour();
	}


	// /*
	//  *
	//  *X

	// /*
	//  *
	//  *
	//  *X
	public void testInsertNewLine15() {
		assertNewLineBehaviour();
	}

	// /*
	//  *
	//  *Xxx

	// /*
	//  *
	//  *
	//  *Xxx
	public void testInsertNewLine16() {
		assertNewLineBehaviour();
	}

	// /*
	//   X

	// /*
	//   
	//   X
	public void testInsertNewLine17() {
		assertNewLineBehaviour();
	}

	// /*
	//   X
	//  */

	// /*
	//   
	//   X
	//  */
	public void testInsertNewLine18() {
		assertNewLineBehaviour();
	}

	//        /*
	//         *
	//         */   /*X

	//        /*
	//         *
	//         */   /*
	//               * X
	//               */
	public void _testInsertNewLine19() {
		assertNewLineBehaviour();
	}
	
	//        /*
	//        /*X
	//         *
	//         */

	//        /*
	//        /*
	//         * X
	//         *
	//         */
	public void testInsertNewLine20() {
		assertNewLineBehaviour();
	}


	//
	//  X
	//
	//  void foo() {}
	public void testFollowingDeclaration1() {
		assertDeclarationFollowingX("void foo() {}");
	}
	
	//  X
	//
	//  void foo() {}
	//  void bar() {}
	public void testFollowingDeclaration1b() {
		assertDeclarationFollowingX("void foo() {}");
	}


	//
	//  X
	//  class C {
	//  void foo() {}
	//  };
	public void testFollowingDeclaration2() {
		assertDeclarationFollowingX("class C {\n  void foo() {}\n  };");
	}

	//  class C {
	//  X
	//  void foo() {}
	//  };
	public void testFollowingDeclaration3() {
		assertDeclarationFollowingX("void foo() {}");
	}
	
	//  class C {
	//  void foo() {X}
	//  void bar(int x);
	//  };
	public void testFollowingDeclaration4() {
		assertDeclarationFollowingX(null);
	}

	//  class C {
	//  void foo() {} X
	//  void bar(int x);
	//  };
	public void testFollowingDeclaration4a() {
		assertDeclarationFollowingX("void bar(int x);");
	}
	
	//  class C {
	//  void foo()X{}
	//  void bar(int x);
	//  };
	public void _testFollowingDeclaration4b() { // XXX - this is likely invalid anyhow
		assertDeclarationFollowingX("void foo(){}"); // (X is just the cursor position)
	}
	
	//  class C {
	//  void foo()
	//  X
	//  {
	//     int x;
	//  }
	//  void bar(int x);
	//  };
	public void _testFollowingDeclaration4c() { // XXX - this is likely invalid anyhow
		assertDeclarationFollowingX("void foo()\n  {\n     int x;\n  }\n"); // (X is just the cursor position)
	}
	
	//  namespace n1 { X
	//		namespace n2 {
	//  		void foo() {}
	//  		void bar(int x) {}
	//          class C {
	//              int y;
	//              void baz(int x) {}
	//          };
	//		}
	//  }
	public void _testFollowingDeclaration5() {
		assertDeclarationFollowingX("namespace n2 {\n  		void foo() {}\n  		void bar(int x) {}\n          class C {\n              int y;\n              void baz(int x) {}\n          };\n		}");
	}
	
	//  namespace n1 {
	//		namespace n2 {X
	//  		void foo() {}
	//  		void bar(int x) {}
	//          class C {
	//              int y;
	//              void baz(int x) {}
	//          };
	//		}
	//  }
	public void testFollowingDeclaration6() {
		assertDeclarationFollowingX("void foo() {}");
	}
	
	//  namespace n1 {
	//		namespace n2 {
	//  		void foo() {}X
	//  		void bar(int x) {}
	//          class C {
	//              int y;
	//              void baz(int x) {}
	//          };
	//		}
	//  }
	public void testFollowingDeclaration7() {
		assertDeclarationFollowingX("void bar(int x) {}");
	}
	
	//  namespace n1 {
	//		namespace n2 {
	//  		void foo() {}
	//  		void bar(int x) {}
	//          class C {X
	//              int y;
	//              void baz(int x) {}
	//          };
	//		}
	//  }
	public void testFollowingDeclaration8() {
		assertDeclarationFollowingX("int y;");
	}
	
	//  namespace n1 {
	//		namespace n2 {
	//  		void foo() {}
	//  		void bar(int x) {}
	//          class C {
	//              int y;X
	//              void baz(int x) {}
	//          };
	//		}
	//  }
	public void testFollowingDeclaration9() {
		assertDeclarationFollowingX("void baz(int x) {}");
	}
	
	//	#define STATIC static
	//
	//	class D {
	//	public:
	//		X
	//		STATIC void D::foo(int x) {
	//			
	//		}
	//	};
	public void testFollowingDeclaration13() throws CoreException {
		assertDeclarationFollowingX("STATIC void D::foo(int x) {\n			\n		}");
	}

	//  #define MM void foo()
	//  X
	//  MM {}
	public void testFollowingDeclaration10() {
		assertDeclarationFollowingX("MM {}");
	}
	
	//  #define NAME foo
	//  #define MM(V) void V(int y)
	//  X
	//  MM(NAME) {}
	public void testFollowingDeclaration11() {
		assertDeclarationFollowingX("MM(NAME) {}");
	}
	
	//  #define MAKEFUNC(V) void V()
	//  #define B(V) V
	//  #define C(V) foo ## x
	//  X
	//  MAKEFUNC(B(C(y))) {}
	public void testFollowingDeclaration12() {
		assertDeclarationFollowingX("MAKEFUNC(B(C(y))) {}");
	}
	
	/**
	 * @param rs - the raw signature of the declaration that should be returned
	 * or <code>null</code> if no declaration should be returned.
	 */
	protected void assertDeclarationFollowingX(String rs) {
		try {
			ICProject cproject= CProjectHelper.createCCProject("test"+System.currentTimeMillis(), "bin");
			try {
				String init= getTestContents1()[0].toString(); 
				int caretInit= init.indexOf('X');
				init= init.replaceFirst("X", "");
				IFile file= TestSourceReader.createFile(cproject.getProject(), "this.cpp", init);
				IASTTranslationUnit ast= TestSourceReader.createIndexBasedAST(null, cproject, file);
				assertNotNull(ast);
				IASTDeclaration decl= DefaultMultilineCommentAutoEditStrategy.findFollowingDeclaration(ast, caretInit);
				if(rs!=null) {
					assertNotNull(decl);
					assertEquals(rs, decl.getRawSignature());
				} else {
					assertNull(decl);
				}
			} finally {
				if(cproject!=null) {
					cproject.getProject().delete(true, npm());
				}
			}
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}

	protected void assertNewLineBehaviour() {
		DefaultMultilineCommentAutoEditStrategy ds= new DefaultMultilineCommentAutoEditStrategy();
		CTextTools textTools = CUIPlugin.getDefault().getTextTools();
		IDocument doc = new Document();
		textTools.setupCDocument(doc);

		CharSequence[] raw= getTestContents();
		String init= raw[0].toString(), expected= raw[1].toString();

		int caretInit= init.indexOf('X');
		init= init.replaceFirst("X", "");

		int caretExpected= expected.indexOf('X');
		expected= expected.replaceFirst("X", "");

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
