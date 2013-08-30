package org.eclipse.cdt.core.parser.tests.doxygen;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDoxygenComment;
import org.eclipse.cdt.core.dom.ast.IASTDoxygenTag;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import static org.eclipse.cdt.core.parser.ParserLanguage.*;
import org.eclipse.cdt.core.parser.tests.ast2.AST2TestBase;
import org.eclipse.cdt.internal.core.doxygen.DoxygenMap;

public class DoxygenMapTest extends AST2TestBase {
	//	/**
	//	 * A doxygen comment.
	//	 * Second line.
	//	 *
	//	 * @param arg1
	//	 * @param arg2
	//	 *  description
	//	 * @return return value
	//	 */
	public void testDoxygenComments() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment(), C);

		IASTComment[] comments = tu.getComments();
		assertEquals(1, comments.length);
		assertTrue(comments[0] instanceof IASTDoxygenComment);

		IASTDoxygenComment doxygenComment = (IASTDoxygenComment)comments[0];
		List<?extends IASTDoxygenTag> tags = doxygenComment.tags();
		assertEquals(4, tags.size());

		assertEquals("", tags.get(0).getName());
		assertEquals("A doxygen comment. Second line.", tags.get(0).getValue());
		assertEquals("param", tags.get(1).getName());
		assertEquals("arg1", tags.get(1).getValue());
		assertEquals("param", tags.get(2).getName());
		assertEquals("arg2 description", tags.get(2).getValue());
		assertEquals("return", tags.get(3).getName());
		assertEquals("return value", tags.get(3).getValue());
	}

	//	/**
	//	 * A test function.
	//	 *
	//	 * @param arg1 first argument
	//	 * @return return value
	//	 */
	//	int test(int arg1);
	public void testDoxygenCommentsBeforeFunction() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment(), C);
		assertEquals(1, tu.getDeclarations().length);

		IASTSimpleDeclaration decl = getDeclaration(tu, 0);
		IASTDeclarator [] decls = decl.getDeclarators();
		IASTFunctionDeclarator fd = (IASTFunctionDeclarator)decls[0];
		assertTrue(fd instanceof IASTStandardFunctionDeclarator);
		IASTStandardFunctionDeclarator sfd = (IASTStandardFunctionDeclarator) fd;
		assertEquals(1, sfd.getParameters().length);

		IASTComment[] comments = tu.getComments();
		assertEquals(1, comments.length);
		assertTrue(comments[0] instanceof IASTDoxygenComment);

		IASTDoxygenComment doxygenComment = (IASTDoxygenComment)comments[0];
		List<?extends IASTDoxygenTag> tags = doxygenComment.tags();
		assertEquals(3, tags.size());

		assertEquals("", tags.get(0).getName());
		assertEquals("A test function.", tags.get(0).getValue());
		assertEquals("param", tags.get(1).getName());
		assertEquals("arg1 first argument", tags.get(1).getValue());
		assertEquals("return", tags.get(2).getName());
		assertEquals("return value", tags.get(2).getValue());

		DoxygenMap doxygenMap = DoxygenMap.resolveDoxygen(tu);
		assertEquals("A test function.", doxygenMap.get(sfd));

		IASTParameterDeclaration [] params = sfd.getParameters();
		assertEquals("first argument", doxygenMap.get(params[0]));
	}

}
