/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorUndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class DOMLocationMacroTests extends AST2TestBase {

	final ParserLanguage[] languages = new ParserLanguage[] { ParserLanguage.C, ParserLanguage.CPP };

	public DOMLocationMacroTests() {
	}

	public DOMLocationMacroTests(String name) {
		super(name);
	}

	public void testObjectStyleMacroExpansionSimpleDeclarator() throws Exception {
		StringBuilder buffer = new StringBuilder("#define ABC D\n"); //$NON-NLS-1$
		buffer.append("int ABC;"); //$NON-NLS-1$
		String code = buffer.toString();
		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language);
			IASTPreprocessorObjectStyleMacroDefinition ABC = (IASTPreprocessorObjectStyleMacroDefinition) tu
					.getMacroDefinitions()[0];
			IASTSimpleDeclaration var = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTDeclarator d = var.getDeclarators()[0];
			assertEquals(d.getName().toString(), "D"); //$NON-NLS-1$
			IASTNodeLocation[] declaratorLocations = d.getNodeLocations();
			assertEquals(declaratorLocations.length, 1);
			IASTMacroExpansionLocation expansion = (IASTMacroExpansionLocation) declaratorLocations[0];
			IASTPreprocessorObjectStyleMacroDefinition fromExpansion = (IASTPreprocessorObjectStyleMacroDefinition) expansion
					.getExpansion().getMacroDefinition();
			assertEqualsMacros(fromExpansion, ABC);
			assertEquals(expansion.getNodeOffset(), 0);
			assertEquals(expansion.getNodeLength(), 1);
			IASTNodeLocation[] macroLocation = expansion.getExpansion().getNodeLocations();
			assertEquals(macroLocation.length, 1);
			assertTrue(macroLocation[0] instanceof IASTFileLocation);
			assertEquals(macroLocation[0].getNodeOffset(), code.indexOf("int ABC;") + "int ".length()); //$NON-NLS-1$ //$NON-NLS-2$
			assertEquals(macroLocation[0].getNodeLength(), "ABC".length()); //$NON-NLS-1$
		}
	}

	public void testObjectMacroExpansionModestDeclarator() throws Exception {
		StringBuilder buffer = new StringBuilder("#define ABC * D\n"); //$NON-NLS-1$
		buffer.append("int ABC;"); //$NON-NLS-1$
		String code = buffer.toString();
		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language);
			IASTPreprocessorObjectStyleMacroDefinition ABC = (IASTPreprocessorObjectStyleMacroDefinition) tu
					.getMacroDefinitions()[0];
			IASTSimpleDeclaration var = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTDeclarator d = var.getDeclarators()[0];
			assertEquals(d.getName().toString(), "D"); //$NON-NLS-1$
			assertEquals(d.getPointerOperators().length, 1);
			IASTNodeLocation[] declaratorLocations = d.getNodeLocations();
			assertEquals(declaratorLocations.length, 1);
			IASTMacroExpansionLocation expansion = (IASTMacroExpansionLocation) declaratorLocations[0];
			IASTPreprocessorObjectStyleMacroDefinition fromExpansion = (IASTPreprocessorObjectStyleMacroDefinition) expansion
					.getExpansion().getMacroDefinition();
			assertEqualsMacros(fromExpansion, ABC);
			assertEquals(expansion.getNodeOffset(), 0);
			assertEquals(2, expansion.getNodeLength());
			IASTNodeLocation[] macroLocation = expansion.getExpansion().getNodeLocations();
			assertEquals(macroLocation.length, 1);
			assertTrue(macroLocation[0] instanceof IASTFileLocation);
			assertEquals(macroLocation[0].getNodeOffset(), code.indexOf("int ABC;") + "int ".length()); //$NON-NLS-1$ //$NON-NLS-2$
			assertEquals(macroLocation[0].getNodeLength(), "ABC".length()); //$NON-NLS-1$

			IASTName n = d.getName();
			IASTNodeLocation[] nameLocations = n.getNodeLocations();
			assertEquals(nameLocations.length, 1);
			final IASTMacroExpansionLocation nodeLocation = (IASTMacroExpansionLocation) nameLocations[0];
			assertEquals(nodeLocation.getNodeOffset(), 1);
			assertEquals(nodeLocation.getNodeLength(), 1);

			assertEquals(nodeLocation.getExpansion().getNodeLocations()[0].getNodeOffset(),
					macroLocation[0].getNodeOffset());
			assertEquals(nodeLocation.getExpansion().getNodeLocations()[0].getNodeLength(),
					macroLocation[0].getNodeLength());

			IASTPointer po = (IASTPointer) d.getPointerOperators()[0];
			assertFalse(po.isConst());
			assertFalse(po.isVolatile());
			IASTMacroExpansionLocation pointerLocation = (IASTMacroExpansionLocation) po.getNodeLocations()[0];
			assertEquals(pointerLocation.getNodeOffset(), 0);
			assertEquals(pointerLocation.getNodeLength(), 1);
			assertEquals(pointerLocation.getExpansion().getNodeLocations()[0].getNodeOffset(),
					macroLocation[0].getNodeOffset());
			assertEquals(pointerLocation.getExpansion().getNodeLocations()[0].getNodeLength(),
					macroLocation[0].getNodeLength());
			assertEqualsMacros(pointerLocation.getExpansion().getMacroDefinition(),
					nodeLocation.getExpansion().getMacroDefinition());
		}
	}

	public void testObjectMacroExpansionPartialDeclSpec() throws Exception {
		StringBuilder buffer = new StringBuilder("#define XYZ const\n"); //$NON-NLS-1$
		buffer.append("XYZ int var;"); //$NON-NLS-1$
		String code = buffer.toString();
		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language);
			IASTPreprocessorObjectStyleMacroDefinition defXYZ = (IASTPreprocessorObjectStyleMacroDefinition) tu
					.getMacroDefinitions()[0];
			IASTSimpleDeclaration var = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTSimpleDeclSpecifier declSpec = (IASTSimpleDeclSpecifier) var.getDeclSpecifier();
			IASTNodeLocation[] declSpecLocations = declSpec.getNodeLocations();
			assertEquals(declSpecLocations.length, 2);
			IASTMacroExpansionLocation expansion = (IASTMacroExpansionLocation) declSpecLocations[0];
			assertEqualsMacros(defXYZ, expansion.getExpansion().getMacroDefinition());
			assertEquals(expansion.getNodeOffset(), 0);
			assertEquals(expansion.getNodeLength(), 1);
			IASTNodeLocation[] expansionLocations = expansion.getExpansion().getNodeLocations();
			assertEquals(expansionLocations.length, 1);
			assertTrue(expansionLocations[0] instanceof IASTFileLocation);
			assertEquals(expansionLocations[0].getNodeOffset(), code.indexOf("XYZ int")); //$NON-NLS-1$
			assertEquals(expansionLocations[0].getNodeLength(), "XYZ".length()); //$NON-NLS-1$
			IASTFileLocation second = (IASTFileLocation) declSpecLocations[1];
			assertEquals(second.getNodeOffset(), code.indexOf(" int")); //$NON-NLS-1$
			assertEquals(second.getNodeLength(), " int".length()); //$NON-NLS-1$
		}
	}

	public void testObjectMacroExpansionNested() throws Exception {
		StringBuilder buffer = new StringBuilder("#define XYZ const\n"); //$NON-NLS-1$
		buffer.append("#define PO *\n"); //$NON-NLS-1$
		buffer.append("#define C_PO PO XYZ\n"); //$NON-NLS-1$
		buffer.append("int C_PO var;"); //$NON-NLS-1$
		String code = buffer.toString();

		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language);
			final IASTPreprocessorMacroDefinition[] macroDefinitions = tu.getMacroDefinitions();
			IASTPreprocessorMacroDefinition XYZ = macroDefinitions[0];
			IASTPreprocessorMacroDefinition PO = macroDefinitions[1];
			IASTPreprocessorMacroDefinition C_PO = macroDefinitions[2];
			IASTSimpleDeclaration var = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			assertTrue(var.getDeclarators()[0].getPointerOperators().length > 0);
			IASTNodeLocation[] locations = var.getNodeLocations();
			assertEquals(3, locations.length);
			IASTFileLocation start_loc = (IASTFileLocation) locations[0];
			assertEquals(start_loc.getNodeOffset(), code.indexOf("int")); //$NON-NLS-1$
			assertEquals(start_loc.getNodeLength(), "int ".length()); //$NON-NLS-1$
			IASTMacroExpansionLocation mac_loc = (IASTMacroExpansionLocation) locations[1];
			final IASTPreprocessorMacroDefinition C_PO2 = mac_loc.getExpansion().getMacroDefinition();
			assertEqualsMacros(C_PO, C_PO2);
			assertEquals(0, mac_loc.getNodeOffset());
			assertEquals(2, mac_loc.getNodeLength());
			IASTFileLocation end_loc = (IASTFileLocation) locations[2];
			assertEquals(code.indexOf(" var"), end_loc.getNodeOffset()); //$NON-NLS-1$
			assertEquals(" var;".length(), end_loc.getNodeLength()); //$NON-NLS-1$
		}
	}

	public void testObjectMacroExpansionComplex() throws Exception {
		StringBuilder buffer = new StringBuilder("#define XYZ const\n"); //$NON-NLS-1$
		buffer.append("#define PO *\n"); //$NON-NLS-1$
		buffer.append("#define C_PO PO XYZ\n"); //$NON-NLS-1$
		buffer.append("#define IT int\n"); //$NON-NLS-1$
		buffer.append("#define V var\n"); //$NON-NLS-1$
		buffer.append("XYZ IT C_PO C_PO V;"); //$NON-NLS-1$
		String code = buffer.toString();

		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language);
			IASTPreprocessorObjectStyleMacroDefinition XYZ = (IASTPreprocessorObjectStyleMacroDefinition) tu
					.getMacroDefinitions()[0];
			//            IASTPreprocessorObjectStyleMacroDefinition PO = (IASTPreprocessorObjectStyleMacroDefinition) tu.getMacroDefinitions()[1];
			IASTPreprocessorObjectStyleMacroDefinition C_PO = (IASTPreprocessorObjectStyleMacroDefinition) tu
					.getMacroDefinitions()[2];
			IASTPreprocessorObjectStyleMacroDefinition IT = (IASTPreprocessorObjectStyleMacroDefinition) tu
					.getMacroDefinitions()[3];
			IASTPreprocessorObjectStyleMacroDefinition V = (IASTPreprocessorObjectStyleMacroDefinition) tu
					.getMacroDefinitions()[4];

			IASTSimpleDeclaration var = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			final IASTNodeLocation[] nodeLocations = var.getNodeLocations();

			assertEquals(10, nodeLocations.length);
			IASTMacroExpansionLocation first_loc = (IASTMacroExpansionLocation) nodeLocations[0];
			assertEqualsMacros(first_loc.getExpansion().getMacroDefinition(), XYZ);
			IASTFileLocation second_loc = (IASTFileLocation) nodeLocations[1];
			assertEquals(1, second_loc.getNodeLength());
			IASTMacroExpansionLocation third_loc = (IASTMacroExpansionLocation) nodeLocations[2];
			assertEqualsMacros(third_loc.getExpansion().getMacroDefinition(), IT);
			IASTFileLocation fourth_loc = (IASTFileLocation) nodeLocations[3];
			assertEquals(1, fourth_loc.getNodeLength());
			IASTMacroExpansionLocation fifth_loc = (IASTMacroExpansionLocation) nodeLocations[4];
			assertEqualsMacros(fifth_loc.getExpansion().getMacroDefinition(), C_PO);
			IASTFileLocation sixth_loc = (IASTFileLocation) nodeLocations[5];
			assertEquals(1, sixth_loc.getNodeLength());
			IASTMacroExpansionLocation seventh_loc = (IASTMacroExpansionLocation) nodeLocations[6];
			assertEqualsMacros(seventh_loc.getExpansion().getMacroDefinition(), C_PO);
			IASTFileLocation eighth_loc = (IASTFileLocation) nodeLocations[7];
			assertEquals(1, eighth_loc.getNodeLength());
			IASTMacroExpansionLocation ninth_loc = (IASTMacroExpansionLocation) nodeLocations[8];
			assertEqualsMacros(ninth_loc.getExpansion().getMacroDefinition(), V);
			IASTFileLocation tenth_loc = (IASTFileLocation) nodeLocations[9];
			assertEquals(1, tenth_loc.getNodeLength());

			final IASTFileLocation flatLocation = var.getFileLocation();
			assertNotNull(flatLocation);
			assertEquals(code.indexOf("XYZ IT C_PO C_PO V;"), flatLocation.getNodeOffset()); //$NON-NLS-1$
			assertEquals("XYZ IT C_PO C_PO V;".length(), flatLocation.getNodeLength()); //$NON-NLS-1$

		}
	}

	public void testStdioBug() throws ParserException {
		StringBuilder buffer = new StringBuilder("#define    _PTR        void *\n"); //$NON-NLS-1$
		buffer.append("#define __cdecl __attribute__ ((__cdecl__))\n"); //$NON-NLS-1$
		buffer.append("#define _EXFUN(name, proto)     __cdecl name proto\n"); //$NON-NLS-1$
		buffer.append("_PTR     _EXFUN(memchr,(const _PTR, int, size_t));\n"); //$NON-NLS-1$
		String code = buffer.toString();

		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language, true, true);
			final IASTPreprocessorMacroDefinition[] macroDefinitions = tu.getMacroDefinitions();
			IASTPreprocessorObjectStyleMacroDefinition _PTR = (IASTPreprocessorObjectStyleMacroDefinition) macroDefinitions[0];
			IASTPreprocessorFunctionStyleMacroDefinition _EXFUN = (IASTPreprocessorFunctionStyleMacroDefinition) macroDefinitions[2];
			IASTSimpleDeclaration memchr = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTNodeLocation[] locations = memchr.getNodeLocations();
			assertEquals(locations.length, 4);
			IASTMacroExpansionLocation loc_1 = (IASTMacroExpansionLocation) locations[0];
			assertEqualsMacros(_PTR, loc_1.getExpansion().getMacroDefinition());
			IASTFileLocation loc_2 = (IASTFileLocation) locations[1];
			assertEquals(loc_2.getNodeOffset(), code.indexOf("     _EXFUN(")); //$NON-NLS-1$
			assertEquals(loc_2.getNodeLength(), "     ".length()); //$NON-NLS-1$
			IASTMacroExpansionLocation loc_3 = (IASTMacroExpansionLocation) locations[2];
			assertEqualsMacros(_EXFUN, loc_3.getExpansion().getMacroDefinition());
			IASTFileLocation loc_4 = (IASTFileLocation) locations[3];
			assertEquals(loc_4.getNodeOffset(), code.indexOf(";")); //$NON-NLS-1$
			assertEquals(loc_4.getNodeLength(), 1);
			IASTFileLocation flat = memchr.getFileLocation();
			assertEquals(flat.getNodeOffset(), code.indexOf("_PTR     _EXFUN(memchr,(const _PTR, int, size_t));")); //$NON-NLS-1$
			assertEquals(flat.getNodeLength(), "_PTR     _EXFUN(memchr,(const _PTR, int, size_t));".length()); //$NON-NLS-1$

			IASTDeclarator d = memchr.getDeclarators()[0];
			IASTFileLocation f = d.getFileLocation();
			assertEquals(code.indexOf("_PTR     _EXFUN(memchr,(const _PTR, int, size_t))"), f.getNodeOffset()); //$NON-NLS-1$
			assertEquals("_PTR     _EXFUN(memchr,(const _PTR, int, size_t))".length(), f.getNodeLength()); //$NON-NLS-1$
		}
	}

	private void assertEqualsMacros(IASTPreprocessorMacroDefinition fromExpansion,
			IASTPreprocessorMacroDefinition source) {
		assertEquals(fromExpansion.getExpansion(), source.getExpansion());
		assertEquals(fromExpansion.getName().toString(), source.getName().toString());
	}

	public void testMacroBindings() throws Exception {
		StringBuilder buffer = new StringBuilder("#define ABC def\n"); //$NON-NLS-1$
		buffer.append("int ABC;\n"); //$NON-NLS-1$
		buffer.append("#undef ABC\n"); //$NON-NLS-1$
		buffer.append("#define ABC ghi\n"); //$NON-NLS-1$
		buffer.append("int ABC;\n"); //$NON-NLS-1$
		String code = buffer.toString();
		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language);
			IASTPreprocessorMacroDefinition[] macros = tu.getMacroDefinitions();
			assertEquals(macros.length, 2);
			IASTPreprocessorObjectStyleMacroDefinition ABC1 = (IASTPreprocessorObjectStyleMacroDefinition) macros[0];
			IASTPreprocessorObjectStyleMacroDefinition ABC2 = (IASTPreprocessorObjectStyleMacroDefinition) macros[1];
			IMacroBinding binding1 = (IMacroBinding) ABC1.getName().resolveBinding();
			assertNotNull(binding1);
			IMacroBinding binding2 = (IMacroBinding) ABC2.getName().resolveBinding();
			assertNotNull(binding2);
			assertNotSame(binding1, binding2);
			IASTName[] firstReferences = tu.getReferences(binding1);
			IASTName[] firstDeclarations = tu.getDeclarationsInAST(binding1);
			assertEquals(firstReferences.length, 2);
			assertEquals(firstReferences[0].getPropertyInParent(), IASTPreprocessorMacroExpansion.EXPANSION_NAME);
			assertEquals(firstReferences[0].getParent().getParent(), tu);
			assertEquals(firstReferences[1].getPropertyInParent(), IASTPreprocessorStatement.MACRO_NAME);
			assertTrue(firstReferences[1].getParent() instanceof IASTPreprocessorUndefStatement);
			assertEquals(firstDeclarations.length, 1);
			assertSame(ABC1.getName(), firstDeclarations[0]);
			IASTName[] secondReferences = tu.getReferences(binding2);
			IASTName[] secondDeclarations = tu.getDeclarationsInAST(binding2);
			assertEquals(1, secondReferences.length);
			assertEquals(secondReferences[0].getPropertyInParent(), IASTPreprocessorMacroExpansion.EXPANSION_NAME);
			assertEquals(secondReferences[0].getParent().getParent(), tu);
			assertSame(ABC2.getName(), secondDeclarations[0]);
		}
	}

	public void testBug90978() throws Exception {
		StringBuilder buffer = new StringBuilder("#define MACRO mm\n"); //$NON-NLS-1$
		buffer.append("int MACRO;\n"); //$NON-NLS-1$
		String code = buffer.toString();
		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language);
			IASTPreprocessorObjectStyleMacroDefinition MACRO = (IASTPreprocessorObjectStyleMacroDefinition) tu
					.getMacroDefinitions()[0];
			IASTName macro_name = MACRO.getName();
			IMacroBinding binding = (IMacroBinding) macro_name.resolveBinding();
			IASTName[] references = tu.getReferences(binding);
			assertEquals(references.length, 1);
			IASTName reference = references[0];
			IASTNodeLocation[] nodeLocations = reference.getNodeLocations();
			assertEquals(nodeLocations.length, 1);
			assertTrue(nodeLocations[0] instanceof IASTFileLocation);
			IASTFileLocation loc = (IASTFileLocation) nodeLocations[0];
			assertEquals(code.indexOf("int MACRO") + "int ".length(), loc.getNodeOffset()); //$NON-NLS-1$ //$NON-NLS-2$
			assertEquals("MACRO".length(), loc.getNodeLength()); //$NON-NLS-1$
		}
	}

	public void testBug94933() throws Exception {
		StringBuilder buffer = new StringBuilder("#define API extern\n"); //$NON-NLS-1$
		buffer.append("#define MYAPI API\n"); //$NON-NLS-1$
		buffer.append("MYAPI void func() {}"); //$NON-NLS-1$
		String code = buffer.toString();
		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language);
			IASTFunctionDefinition f = (IASTFunctionDefinition) tu.getDeclarations()[0];
			assertNotNull(f.getFileLocation());
		}
	}

	public void testFunctionMacroExpansionWithNameSubstitution_Bug173637() throws Exception {
		StringBuilder buffer = new StringBuilder("#define PLUS5(x) (x+5)\n"); //$NON-NLS-1$
		buffer.append("#define FUNCTION PLUS5 \n"); //$NON-NLS-1$
		buffer.append("int var= FUNCTION(1);"); //$NON-NLS-1$
		String code = buffer.toString();

		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language);
			IASTSimpleDeclaration var = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTEqualsInitializer initializer = (IASTEqualsInitializer) var.getDeclarators()[0].getInitializer();
			IASTInitializerClause expr = initializer.getInitializerClause();
			assertNotNull(expr.getFileLocation());
			IASTNodeLocation[] locations = expr.getNodeLocations();
			assertEquals(1, locations.length);
			IASTMacroExpansionLocation macroExpansion = (IASTMacroExpansionLocation) locations[0];
			IASTNodeLocation[] expLocations = macroExpansion.getExpansion().getNodeLocations();
			assertEquals(1, expLocations.length);
			assertEquals(code.indexOf("FUNCTION(1)"), expLocations[0].getNodeOffset());
			assertEquals("FUNCTION(1)".length(), expLocations[0].getNodeLength());
		}
	}

	private void assertMacroLocation(IASTDeclaration decl, int index, int length) {
		IASTSimpleDeclaration var = (IASTSimpleDeclaration) decl;
		IASTEqualsInitializer initializer = (IASTEqualsInitializer) var.getDeclarators()[0].getInitializer();
		IASTInitializerClause expr = initializer.getInitializerClause();
		assertNotNull(expr.getFileLocation());
		IASTNodeLocation[] locations = expr.getNodeLocations();
		assertEquals(1, locations.length);
		IASTMacroExpansionLocation macroExpansion = (IASTMacroExpansionLocation) locations[0];
		IASTNodeLocation[] expLocations = macroExpansion.getExpansion().getNodeLocations();
		assertEquals(1, expLocations.length);
		IASTFileLocation fileLocation = expLocations[0].asFileLocation();
		assertEquals(index, fileLocation.getNodeOffset());
		assertEquals(length, fileLocation.getNodeLength());
	}

	private void assertExpressionLocation(IASTDeclaration decl, int index, int length) {
		IASTSimpleDeclaration var = (IASTSimpleDeclaration) decl;
		IASTEqualsInitializer initializer = (IASTEqualsInitializer) var.getDeclarators()[0].getInitializer();
		IASTInitializerClause expr = initializer.getInitializerClause();
		IASTFileLocation fileLocation = expr.getFileLocation();
		assertNotNull(fileLocation);
		assertEquals(index, fileLocation.getNodeOffset());
		assertEquals(length, fileLocation.getNodeLength());
	}

	public void testBug186257() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("typedef char STR; \n"); //$NON-NLS-1$
		sb.append("#define Nullstr Null(STR*) \n"); //$NON-NLS-1$
		sb.append("#define Null(x) ((x)NULL) \n"); //$NON-NLS-1$
		sb.append("int x = Nullstr; \n"); //$NON-NLS-1$
		sb.append("int y = whatever; \n"); //$NON-NLS-1$
		String code = sb.toString();

		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language);
			IASTDeclaration[] decls = tu.getDeclarations();
			assertMacroLocation(decls[1], code.indexOf("Nullstr;"), "Nullstr".length()); //$NON-NLS-1$ //$NON-NLS-2$
			assertExpressionLocation(decls[2], code.indexOf("whatever;"), "whatever".length()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void testArgumentExpansion() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("#define ADD(a,b, c) (a) + (b) + (c) \n"); //$NON-NLS-1$
		sb.append("#define ONEYONENOE 111111   \n"); //$NON-NLS-1$
		sb.append("#define TWO 2 \n"); //$NON-NLS-1$
		sb.append("#define THREE 3 \n"); //$NON-NLS-1$
		sb.append("int x = ADD(ONEYONENOE,TWO,  THREE); \n"); //$NON-NLS-1$
		sb.append("int y = whatever; \n"); //$NON-NLS-1$
		String code = sb.toString();

		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language);
			IASTDeclaration[] decls = tu.getDeclarations();
			assertMacroLocation(decls[0], code.indexOf("ADD(ONEYONENOE,TWO,  THREE)"), //$NON-NLS-1$
					"ADD(ONEYONENOE,TWO,  THREE)".length()); //$NON-NLS-1$
			assertExpressionLocation(decls[1], code.indexOf("whatever;"), "whatever".length()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void testArgumentCapture() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("#define add(x,y) x + y \n"); //$NON-NLS-1$
		sb.append("#define add2 add(x,   \n"); //$NON-NLS-1$
		sb.append("int x = add2 z); \n"); //$NON-NLS-1$
		sb.append("int y = whatever; \n"); //$NON-NLS-1$
		String code = sb.toString();

		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language);
			IASTDeclaration[] decls = tu.getDeclarations();
			assertMacroLocation(decls[0], code.indexOf("add2 z);"), "add2 z)".length()); //$NON-NLS-1$ //$NON-NLS-2$
			assertExpressionLocation(decls[1], code.indexOf("whatever;"), "whatever".length()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void testFunctionMacroNotCalled() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("#define FUNCTION(x) x \n"); //$NON-NLS-1$
		sb.append("#define YO FUNCTION \n"); //$NON-NLS-1$
		sb.append("int x = YO; \n"); //$NON-NLS-1$
		sb.append("int y = whatever; \n"); //$NON-NLS-1$
		String code = sb.toString();

		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language);
			IASTDeclaration[] decls = tu.getDeclarations();
			assertMacroLocation(decls[0], code.indexOf("YO;"), "YO".length()); //$NON-NLS-1$ //$NON-NLS-2$
			assertExpressionLocation(decls[1], code.indexOf("whatever;"), "whatever".length()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void testBuildFunctionMacroName() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("#define FUN1(x) x \n"); //$NON-NLS-1$
		sb.append("#define FUN1(x) x \n"); //$NON-NLS-1$
		sb.append("#define MAKEFUN(num) FUN ## num \n"); //$NON-NLS-1$
		sb.append("int x = MAKEFUN(1)(z); \n"); //$NON-NLS-1$
		sb.append("int y = whatever; \n"); //$NON-NLS-1$
		String code = sb.toString();

		for (ParserLanguage language : languages) {
			IASTTranslationUnit tu = parse(code, language);
			IASTDeclaration[] decls = tu.getDeclarations();
			assertMacroLocation(decls[0], code.indexOf("MAKEFUN(1)(z);"), "MAKEFUN(1)(z)".length()); //$NON-NLS-1$ //$NON-NLS-2$
			assertExpressionLocation(decls[1], code.indexOf("whatever;"), "whatever".length()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

}
