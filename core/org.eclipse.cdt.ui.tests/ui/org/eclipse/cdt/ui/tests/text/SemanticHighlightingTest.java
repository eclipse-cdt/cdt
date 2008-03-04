/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
				createPosition(25, 15, 14),
				createPosition(27, 21, 19),
				createPosition(44, 21, 20),
				createPosition(45, 15, 15),
				createPosition(59, 21, 20),
				createPosition(60, 15, 15),
				createPosition(120, 20, 15),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testFieldHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.FIELD);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(25, 15, 14),
				createPosition(26, 14, 13),
				createPosition(27, 21, 19),
				createPosition(28, 8, 8),
				createPosition(44, 21, 20),
				createPosition(45, 15, 15),
				createPosition(46, 15, 14),
				createPosition(47, 8, 9),
				createPosition(59, 21, 20),
				createPosition(60, 15, 15),
				createPosition(61, 15, 14),
				createPosition(62, 8, 9),
				createPosition(77, 7, 5),
				createPosition(78, 7, 5),
				createPosition(80, 8, 5),
				createPosition(81, 8, 5),
				createPosition(89, 8, 11),
				createPosition(93, 8, 10),
				createPosition(107, 11, 9),
				createPosition(112, 11, 8),
				createPosition(117, 8, 11),
				createPosition(119, 7, 10),
				createPosition(120, 20, 15),
				createPosition(123, 11, 10),
				createPosition(123, 28, 11),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testMethodDeclarationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.METHOD_DECLARATION);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(30, 15, 15),
				createPosition(35, 8, 9),
				createPosition(49, 15, 16),
				createPosition(50, 8, 10),
				createPosition(64, 15, 16),
				createPosition(65, 8, 10),
				createPosition(79, 4, 13),
				createPosition(106, 4, 26),
				createPosition(110, 4, 25),
				createPosition(115, 4, 32),
		};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testMethodHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.METHOD);
		Position[] expected= new Position[] {
				createPosition(30, 15, 15),
				createPosition(35, 8, 9),
				createPosition(49, 15, 16),
				createPosition(50, 8, 10),
				createPosition(64, 15, 16),
				createPosition(65, 8, 10),
				createPosition(79, 4, 13),
				createPosition(106, 4, 26),
				createPosition(110, 4, 25),
				createPosition(115, 4, 32),
				createPosition(116, 23, 9),
				createPosition(120, 4, 15),
				createPosition(127, 13, 9),
		};
		Position[] actual= getSemanticHighlightingPositions();
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testStaticMethodInvocationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.STATIC_METHOD_INVOCATION);
		Position[] expected= new Position[] {
				createPosition(120, 4, 15),
		};
		Position[] actual= getSemanticHighlightingPositions();
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testLocalVariableDeclarationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.LOCAL_VARIABLE_DECLARATION);
		Position[] expected= new Position[] {
				createPosition(111, 8, 8),
				createPosition(116, 15, 2),
				createPosition(118, 13, 2),
				createPosition(126, 13, 8),
			};
		Position[] actual= getSemanticHighlightingPositions();
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testLocalVariableReferencesHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.LOCAL_VARIABLE);
		Position[] expected= new Position[] {
				createPosition(112, 22, 8),
				createPosition(117, 4, 2),
				createPosition(119, 4, 2),
				createPosition(123, 8, 2),
				createPosition(123, 24, 2),
				createPosition(127, 4, 8),
			};
		Position[] actual= getSemanticHighlightingPositions();
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testParameterVariableHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.PARAMETER_VARIABLE);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(12, 20, 1),
				createPosition(30, 35, 3),
				createPosition(31, 23, 3),
				createPosition(32, 19, 3),
				createPosition(79, 21, 4),
				createPosition(79, 30, 4),
				createPosition(80, 16, 4),
				createPosition(81, 16, 4),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testTemplateParameterHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.TEMPLATE_PARAMETER);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(76, 15, 2),
				createPosition(76, 25, 2),
				createPosition(77, 4, 2),
				createPosition(78, 4, 2),
				createPosition(79, 18, 2),
				createPosition(79, 27, 2),
				createPosition(85, 15, 2),
				createPosition(85, 66, 2),
				createPosition(133, 14, 1),
				createPosition(136, 9, 1),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testEnumHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.ENUM);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(4, 5, 11),
				createPosition(37, 9, 14),
				createPosition(52, 9, 15),
				createPosition(67, 9, 15),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testClassHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.CLASS);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(17, 6, 5),
				createPosition(18, 6, 5),
				createPosition(20, 6, 14),
				createPosition(20, 23, 5),
				createPosition(20, 30, 5),
				createPosition(22, 17, 11),
				createPosition(38, 10, 8),
				createPosition(39, 10, 9),
				createPosition(40, 10, 8),
				createPosition(41, 12, 8),
				createPosition(53, 10, 9),
				createPosition(54, 10, 10),
				createPosition(55, 10, 9),
				createPosition(56, 12, 9),
				createPosition(68, 10, 9),
				createPosition(69, 10, 10),
				createPosition(70, 10, 9),
				createPosition(71, 12, 9),
				createPosition(76, 35, 13),
				createPosition(85, 25, 24),
				createPosition(85, 52, 13),
				createPosition(85, 70, 5),
				createPosition(88, 7, 9),
				createPosition(92, 6, 8),
				createPosition(96, 8, 8),
				createPosition(106, 4, 14),
				createPosition(110, 4, 14),
				createPosition(115, 4, 14),
				createPosition(116, 4, 9),
				createPosition(118, 4, 8),
				createPosition(126, 4, 8),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testFunctionDeclarationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.FUNCTION_DECLARATION);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(12, 5, 10),
				createPosition(13, 12, 16),
				createPosition(21, 16, 10),
				createPosition(100, 8, 13),
				createPosition(134, 4, 1),
				createPosition(141, 10, 1),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testFunctionHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.FUNCTION);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(12, 5, 10),
				createPosition(13, 12, 16),
				createPosition(21, 16, 10),
				createPosition(32, 8, 10),
				createPosition(100, 8, 13),
				createPosition(101, 1, 16),
				createPosition(128, 4, 11),
				createPosition(134, 4, 1),
				createPosition(141, 10, 1),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	
	public void testGlobalVariableHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.GLOBAL_VARIABLE);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(8, 10, 14),
				createPosition(9, 4, 14),
				createPosition(10, 11, 20),
				createPosition(33, 15, 20),
				createPosition(99, 8, 12),
				createPosition(102, 8, 12),
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
				createPosition(2, 8, 11),
				createPosition(140, 8, 5),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testMacroReferencesHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.MACRO_REFERENCE);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(14, 4, 11),
				createPosition(31, 8, 14),
				createPosition(106, 0, 3),
				createPosition(110, 0, 3),
				createPosition(115, 0, 3),
				createPosition(122, 4, 14),
				createPosition(141, 0, 5),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testTypedefHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.TYPEDEF);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(41, 21, 10),
				createPosition(56, 22, 11),
				createPosition(71, 22, 11),
				createPosition(96, 17, 6),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testNamespaceHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.NAMESPACE);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(98, 10, 2),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testLabelHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.LABEL);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(121, 0, 5),
				createPosition(123, 46, 5),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testEnumeratorHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.ENUMERATOR);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(5, 4, 10),
				createPosition(37, 25, 13),
				createPosition(52, 26, 14),
				createPosition(67, 26, 14),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testProblemHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.PROBLEM);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(124, 4, 13),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testExternalSDKHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.EXTERNAL_SDK);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(127, 13, 9),
				createPosition(128, 4, 11),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
}
