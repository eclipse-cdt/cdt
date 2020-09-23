package org.eclipse.cdt.core.parser.tests.ast2;

import static org.eclipse.cdt.core.parser.ParserLanguage.CPP;

import java.io.IOException;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class AST2TemplateRValueRValueTests extends AST2CPPTestBase {

	public AST2TemplateRValueRValueTests() {
	}

	public AST2TemplateRValueRValueTests(String name) {
		super(name);
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testLValue()
	//	{
	//	  clazz c;
	//	  demo(c);
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo(getClazz());
	//	}
	public void test_lvalue_rvalue_caller_templateLvalue_templateRvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	void demo(clazz &&cont)
	//	{
	//	}
	//
	//	void testLValue()
	//	{
	//	  clazz c;
	//	  demo(c);
	//	}
	public void test_lvalue_caller_rvalue_function() throws Exception {
		parseAndCheckBindingsForOneProblem();
	}

	//	class clazz {
	//	};
	//
	//	void demo(clazz &cont)
	//	{
	//	}
	//
	//	void testLValue()
	//	{
	//	  clazz c;
	//	  demo(c);
	//	}
	public void test_lvalue_caller_lvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	void demo(clazz &&cont)
	//	{
	//	}
	//
	//	void demo(clazz &cont)
	//	{
	//	}
	//
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo(getClazz());
	//	}
	public void test_rvalue_caller_lvalue_rvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//	void demo(clazz &cont)
	//	{
	//	}
	//
	//	void testLValue()
	//	{
	//	  clazz c;
	//	  demo(c);
	//	}
	public void test_lvalue_caller_templateRvalue_lvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//	void demo(clazz &cont)
	//	{
	//	}
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo(getClazz());
	//	}
	public void test_rvalue_caller_templateRvalue_lvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	void demo(clazz &cont)
	//	{
	//	}
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo(getClazz());
	//	}
	public void test_rvalue_caller_lvalue_function() throws Exception {
		parseAndCheckBindingsForOneProblem();
	}

	//	class clazz {
	//	};
	//
	//	void demo(clazz &&cont)
	//	{
	//	}
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo(getClazz());
	//	}
	public void test_rvalue_caller_rvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo(getClazz());
	//	}
	public void test_rvalue_caller_templateLvalue_function() throws Exception {
		parseAndCheckBindingsForOneProblem();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo(getClazz());
	//	}
	public void test_rvalue_caller_templateRvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo<clazz>(getClazz());
	//	}
	public void test_templatedRvalue_caller__templateRvalue_lvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//  void demo(clazz &cont) {
	//  }
	//
	//	void testLValue()
	//	{
	//    clazz c;
	//	  demo<clazz>(c);
	//	}
	public void test_templatedLvalue_caller__templateRvalue_lvalue_function() throws Exception {
		parseAndCheckBindingsForProblem();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//  void demo(clazz &cont) {
	//  }
	//
	//	void testLValue()
	//	{
	//    clazz c;
	//	  demo(c);
	//	}
	public void test_lvalue_caller_templateLvalue_lvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//  void demo(clazz &cont) {
	//  }
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo(getClazz());
	//	}
	public void test_rvalue_caller_templateLvalue_lvalue_function() throws Exception {
		parseAndCheckBindingsForOneProblem();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//  void demo(clazz &cont) {
	//  }
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo<clazz>(getClazz());
	//	}
	public void test_templatedRvalue_caller_templatedLValue_lvalAST2TemplateRValueRValueTestsue_function()
			throws Exception {
		parseAndCheckBindingsForProblem();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//  void demo(clazz &cont) {
	//  }
	//
	//	void testRValue()
	//	{
	//    clazz c;
	//	  demo<clazz>(c);
	//	}
	public void test_templatedLvalue_caller_templatedLValue_lvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//  void demo(clazz &&cont) {
	//  }
	//
	//	void testLValue()
	//	{
	//    clazz c;
	//	  demo(c);
	//	}
	public void test_lvalue_caller_templatedRvalue_rvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//  void demo(clazz &&cont) {
	//  }
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo(getClazz());
	//	}
	public void test_rvalue_caller_templatedRvalue_rvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//  void demo(clazz &&cont) {
	//  }
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo<clazz>(getClazz());
	//	}
	public void test_templatedRvalue_caller_templatedRvalue_rvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//  void demo(clazz &&cont) {
	//  }
	//
	//	void testLValue()
	//	{
	//    clazz c;
	//	  demo<clazz>(c);
	//	}
	public void test_templatedLvalue_caller_templatedRvalue_rvalue_function() throws Exception {
		parseAndCheckBindingsForProblem();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//  void demo(clazz &&cont) {
	//  }
	//
	//	void testLValue()
	//	{
	//    clazz c;
	//	  demo(c);
	//	}
	public void test_lvalue_caller_templatedLvalue_rvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//  void demo(clazz &&cont) {
	//  }
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo(getClazz());
	//	}
	public void test_rvalue_caller_templatedLvalue_rvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//  void demo(clazz &&cont) {
	//  }
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo<clazz>(getClazz());
	//	}
	public void test_templatedRvalue_caller_templatedLvalue_rvalue_function() throws Exception {
		parseAndCheckBindingsForProblem();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//  void demo(clazz &&cont) {
	//  }
	//
	//	void testRValue()
	//	{
	//    clazz c;
	//	  demo<clazz>(c);
	//	}
	public void test_templatedLvalue_caller_templatedLvalue_rvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//	void testLValue()
	//	{
	//    clazz c;
	//	  demo(c);
	//	}
	public void test_lvalue_caller_templatedLvalue_templatedRvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo(getClazz());
	//	}
	public void test_rvalue_caller_templatedLvalue_templatedRvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo<clazz>(getClazz());
	//	}
	public void test_templatedRvalue_caller_templatedLvalue_templatedRvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//	void testLValue()
	//	{
	//    clazz c;
	//	  demo<clazz>(c);
	//	}
	public void test_templatedLvalue_caller_templatedLvalue_templatedRvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo<clazz>(getClazz());
	//	}
	public void test_templatedRvalue_caller_templatedLvalue_function() throws Exception {
		parseAndCheckBindingsForProblem();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//	clazz getClazz()
	//	{
	//	  clazz c;
	//	  return c;
	//	}
	//
	//	void testRValue()
	//	{
	//	  demo<clazz>(getClazz());
	//	}
	public void test_templatedRvalue_caller_templatedRvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &cont)
	//	{
	//	}
	//
	//	void testLValue()
	//	{
	//    clazz c;
	//	  demo<clazz>(c);
	//	}
	public void test_templatedLvalue_caller_templatedLvalue_function() throws Exception {
		parseAndCheckBindings();
	}

	//	class clazz {
	//	};
	//
	//	template<class C> void demo(C &&cont)
	//	{
	//	}
	//
	//	void testLValue()
	//	{
	//    clazz c;
	//	  demo<clazz>(c);
	//	}
	public void test_templatedLvalue_caller_templatedRvalue_function() throws Exception {
		parseAndCheckBindingsForProblem();
	}

	private void parseAndCheckBindingsForOneProblem() throws IOException, ParserException {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parse(code, CPP, true, false);
		NameCollector nc = new NameCollector();
		tu.accept(nc);
		assertProblemBindings(nc, 1);
	}

	private void parseAndCheckBindingsForProblem() throws IOException, ParserException {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parse(code, CPP, true, false);
		NameCollector nc = new NameCollector();
		tu.accept(nc);
		assertProblemBindings(nc);
	}

}
