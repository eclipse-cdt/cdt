package org.eclipse.cdt.core.parser.tests;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.eclipse.cdt.internal.core.dom.ClassSpecifier;
import org.eclipse.cdt.internal.core.dom.DOMBuilder;
import org.eclipse.cdt.internal.core.dom.Declarator;
import org.eclipse.cdt.internal.core.dom.Name;
import org.eclipse.cdt.internal.core.dom.SimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;
import org.eclipse.cdt.internal.core.newparser.Parser;

import junit.framework.TestCase;

/**
 * Tests the construction of DOMs for snippets of code
 */
public class DOMTests extends TestCase {

	public TranslationUnit parse(String code) throws Exception {
		DOMBuilder domBuilder = new DOMBuilder();
		Parser parser = new Parser(code, domBuilder);
		parser.parse();
		
		return domBuilder.getTranslationUnit();
	}
	
	public void testIntGlobal() throws Exception {
		TranslationUnit translationUnit = parse("int x;");
		List declarations = translationUnit.getDeclarations();
		assertEquals(1, declarations.size());
		SimpleDeclaration declaration = (SimpleDeclaration)declarations.get(0);
		assertEquals(SimpleDeclaration.t_int, declaration.getDeclSpecifierSeq());
		List declarators = declaration.getDeclarators();
		assertEquals(1, declarators.size());
		Declarator declarator = (Declarator)declarators.get(0);
		Name name = declarator.getName();
		assertEquals("x", name.getName());
	}
	
	public void testEmptyClass() throws Exception {
		Writer code = new StringWriter();
		code.write("class A { } a;");
		TranslationUnit translationUnit = parse(code.toString());
		List declarations = translationUnit.getDeclarations();
		assertEquals(1, declarations.size());
		SimpleDeclaration declaration = (SimpleDeclaration)declarations.get(0);
		assertEquals(0, declaration.getDeclSpecifierSeq());
		ClassSpecifier classSpecifier = (ClassSpecifier)declaration.getTypeSpecifier();
		Name className = classSpecifier.getName();
		assertEquals("A", className.getName());
		List declarators = declaration.getDeclarators();
		assertEquals(1, declarators.size());
		Declarator declarator = (Declarator)declarators.get(0);
		Name name = declarator.getName();
		assertEquals("a", name.getName());
	}

}
