package org.eclipse.cdt.debug.core.tests;

import java.util.List;

import org.eclipse.cdt.internal.core.dom.DOMBuilder;
import org.eclipse.cdt.internal.core.dom.SimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;
import org.eclipse.cdt.internal.core.newparser.Parser;

import junit.framework.TestCase;

/**
 * Tests the construction of DOMs for snippets of code
 */
public class DOMTests extends TestCase {

	public void testIntX() throws Exception {
		DOMBuilder domBuilder = new DOMBuilder();
		Parser parser = new Parser("int x;", domBuilder);
		parser.parse();
		
		TranslationUnit translationUnit = domBuilder.getTranslationUnit();
		List declarations = translationUnit.getDeclarations();
		assertEquals(1, declarations.size());
		SimpleDeclaration declaration = (SimpleDeclaration)declarations.get(0);
		assertEquals(SimpleDeclaration.t_int, declaration.getDeclSpecifierSeq());
	}
}
