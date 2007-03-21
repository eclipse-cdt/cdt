/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jface.text.Position;

import org.eclipse.cdt.internal.ui.editor.SemanticHighlightings;

/**
 * Semantic highlighting tests.
 * 
 * <p>Derived from JDT.<p>
 *
 * @since 4.0
 */
public class SemanticHighlightingTest extends AbstractSemanticHighlightingTest {
	
	private static final boolean PRINT_POSITIONS= false;

	private static final Class THIS= SemanticHighlightingTest.class;
	
	public static Test suite() {
		return new SemanticHighlightingTestSetup(new TestSuite(THIS), "/SHTest/src/SHTest.cpp");
	}

	public void testStaticFieldHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.STATIC_FIELD);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(23, 15, 14),
				createPosition(25, 21, 19),
				createPosition(42, 21, 20),
				createPosition(43, 15, 15),
				createPosition(57, 21, 20),
				createPosition(58, 15, 15),
				createPosition(118, 20, 15),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testFieldHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.FIELD);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(23, 15, 14),
				createPosition(24, 14, 13),
				createPosition(25, 21, 19),
				createPosition(26, 8, 8),
				createPosition(42, 21, 20),
				createPosition(43, 15, 15),
				createPosition(44, 15, 14),
				createPosition(45, 8, 9),
				createPosition(57, 21, 20),
				createPosition(58, 15, 15),
				createPosition(59, 15, 14),
				createPosition(60, 8, 9),
				createPosition(75, 7, 5),
				createPosition(76, 7, 5),
				createPosition(78, 8, 5),
				createPosition(79, 8, 5),
				createPosition(87, 8, 11),
				createPosition(91, 8, 10),
				createPosition(105, 11, 9),
				createPosition(110, 11, 8),
				createPosition(115, 8, 11),
				createPosition(117, 7, 10),
				createPosition(118, 20, 15),
				createPosition(121, 11, 10),
				createPosition(121, 28, 11),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testMethodDeclarationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.METHOD_DECLARATION);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(28, 15, 15),
				createPosition(33, 8, 9),
				createPosition(47, 15, 16),
				createPosition(48, 8, 10),
				createPosition(62, 15, 16),
				createPosition(63, 8, 10),
				createPosition(77, 4, 13),
				createPosition(104, 4, 26),
				createPosition(108, 4, 25),
				createPosition(113, 4, 32),
		};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testMethodHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.METHOD);
		Position[] expected= new Position[] {
				createPosition(28, 15, 15),
				createPosition(33, 8, 9),
				createPosition(47, 15, 16),
				createPosition(48, 8, 10),
				createPosition(62, 15, 16),
				createPosition(63, 8, 10),
				createPosition(77, 4, 13),
				createPosition(104, 4, 26),
				createPosition(108, 4, 25),
				createPosition(113, 4, 32),
				createPosition(114, 23, 9),
				createPosition(118, 4, 15),
		};
		Position[] actual= getSemanticHighlightingPositions();
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testStaticMethodInvocationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.STATIC_METHOD_INVOCATION);
		Position[] expected= new Position[] {
				createPosition(118, 4, 15),
		};
		Position[] actual= getSemanticHighlightingPositions();
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testLocalVariableDeclarationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.LOCAL_VARIABLE_DECLARATION);
		Position[] expected= new Position[] {
				createPosition(109, 8, 8),
				createPosition(114, 15, 2),
				createPosition(116, 13, 2),
			};
		Position[] actual= getSemanticHighlightingPositions();
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testLocalVariableReferencesHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.LOCAL_VARIABLE);
		Position[] expected= new Position[] {
				createPosition(110, 22, 8),
				createPosition(115, 4, 2),
				createPosition(117, 4, 2),
				createPosition(121, 8, 2),
				createPosition(121, 24, 2),
			};
		Position[] actual= getSemanticHighlightingPositions();
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testParameterVariableHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.PARAMETER_VARIABLE);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(11, 20, 1),
				createPosition(28, 35, 3),
				createPosition(30, 19, 3),
				createPosition(77, 21, 4),
				createPosition(77, 30, 4),
				createPosition(78, 16, 4),
				createPosition(79, 16, 4),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testTemplateParameterHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.TEMPLATE_PARAMETER);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(74, 15, 2),
				createPosition(74, 25, 2),
				createPosition(75, 4, 2),
				createPosition(76, 4, 2),
				createPosition(77, 18, 2),
				createPosition(77, 27, 2),
				createPosition(83, 15, 2),
				createPosition(83, 66, 2),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testEnumHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.ENUM);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(3, 5, 11),
				createPosition(35, 9, 14),
				createPosition(50, 9, 15),
				createPosition(65, 9, 15),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testClassHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.CLASS);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(15, 6, 5),
				createPosition(16, 6, 5),
				createPosition(18, 6, 14),
				createPosition(18, 23, 5),
				createPosition(18, 30, 5),
				createPosition(20, 17, 11),
				createPosition(36, 10, 8),
				createPosition(37, 10, 9),
				createPosition(38, 10, 8),
				createPosition(39, 12, 8),
				createPosition(51, 10, 9),
				createPosition(52, 10, 10),
				createPosition(53, 10, 9),
				createPosition(54, 12, 9),
				createPosition(66, 10, 9),
				createPosition(67, 10, 10),
				createPosition(68, 10, 9),
				createPosition(69, 12, 9),
				createPosition(74, 35, 13),
				createPosition(83, 25, 24),
				createPosition(83, 52, 13),
				createPosition(83, 70, 5),
				createPosition(86, 7, 9),
				createPosition(90, 6, 8),
				createPosition(94, 8, 8),
				createPosition(104, 4, 14),
				createPosition(108, 4, 14),
				createPosition(113, 4, 14),
				createPosition(114, 4, 9),
				createPosition(116, 4, 8),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testFunctionDeclarationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.FUNCTION_DECLARATION);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(11, 5, 10),
				createPosition(12, 12, 16),
				createPosition(19, 16, 10),
				createPosition(98, 8, 13),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testFunctionHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.FUNCTION);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(11, 5, 10),
				createPosition(12, 12, 16),
				createPosition(19, 16, 10),
				createPosition(30, 8, 10),
				createPosition(98, 8, 13),
				createPosition(99, 1, 16),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	
	public void testGlobalVariableHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.GLOBAL_VARIABLE);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(7, 10, 14),
				createPosition(8, 4, 14),
				createPosition(9, 11, 20),
				createPosition(31, 15, 20),
				createPosition(97, 8, 12),
				createPosition(100, 8, 12),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testMacroDefinitionHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.MACRO_DEFINITION);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(0, 8, 3),
				createPosition(1, 8, 14),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testMacroReferencesHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.MACRO_REFERENCE);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(29, 8, 14),
				createPosition(104, 0, 3),
				createPosition(108, 0, 3),
				createPosition(113, 0, 3),
				createPosition(120, 4, 14),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testTypedefHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.TYPEDEF);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(39, 21, 10),
				createPosition(54, 22, 11),
				createPosition(69, 22, 11),
				createPosition(94, 17, 6),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testNamespaceHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.NAMESPACE);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(96, 10, 2),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testLabelHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.LABEL);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(119, 0, 5),
				createPosition(121, 46, 5),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testEnumeratorHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.ENUMERATOR);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(4, 4, 10),
				createPosition(35, 25, 13),
				createPosition(50, 26, 14),
				createPosition(65, 26, 14),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testProblemHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.PROBLEM);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(122, 4, 13),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

}
