/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
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

	private static final Class<?> THIS= SemanticHighlightingTest.class;
	
	public static Test suite() {
		return new SemanticHighlightingTestSetup(new TestSuite(THIS), TESTFILE);
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
				createPosition(122, 20, 15),
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
				createPosition(109, 11, 9),
				createPosition(114, 11, 8),
				createPosition(119, 8, 11),
				createPosition(121, 7, 10),
				createPosition(122, 20, 15),
				createPosition(126, 11, 10),
				createPosition(126, 28, 11),
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
				createPosition(94, 13, 9),
				createPosition(95, 13, 10),
				createPosition(108, 4, 26),
				createPosition(112, 4, 25),
				createPosition(117, 4, 32),
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
				createPosition(94, 13, 9),
				createPosition(95, 13, 10),
				createPosition(108, 4, 26),
				createPosition(112, 4, 25),
				createPosition(117, 4, 32),
				createPosition(118, 23, 9),
				createPosition(122, 4, 15),
				createPosition(130, 13, 9),
		};
		Position[] actual= getSemanticHighlightingPositions();
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testStaticMethodInvocationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.STATIC_METHOD_INVOCATION);
		Position[] expected= new Position[] {
				createPosition(122, 4, 15),
		};
		Position[] actual= getSemanticHighlightingPositions();
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testLocalVariableDeclarationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.LOCAL_VARIABLE_DECLARATION);
		Position[] expected= new Position[] {
				createPosition(113, 8, 8),
				createPosition(118, 15, 2),
				createPosition(120, 13, 2),
				createPosition(129, 13, 8),
			};
		Position[] actual= getSemanticHighlightingPositions();
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testLocalVariableReferencesHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.LOCAL_VARIABLE);
		Position[] expected= new Position[] {
				createPosition(114, 22, 8),
				createPosition(119, 4, 2),
				createPosition(121, 4, 2),
				createPosition(123, 4, 2),
				createPosition(123, 9, 2),
				createPosition(126, 8, 2),
				createPosition(126, 24, 2),
				createPosition(130, 4, 8),
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
				createPosition(136, 14, 1),
				createPosition(139, 9, 1),
				createPosition(147, 32, 1),
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
				createPosition(94, 4, 8),
				createPosition(94, 23, 8),
				createPosition(95, 4, 8),
				createPosition(98, 8, 8),
				createPosition(108, 4, 14),
				createPosition(112, 4, 14),
				createPosition(117, 4, 14),
				createPosition(118, 4, 9),
				createPosition(120, 4, 8),
				createPosition(129, 4, 8),
				createPosition(147, 42, 7),
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
				createPosition(102, 8, 13),
				createPosition(137, 4, 1),
				createPosition(144, 10, 1),
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
				createPosition(102, 8, 13),
				createPosition(103, 1, 16),
				createPosition(131, 4, 11),
				createPosition(137, 4, 1),
				createPosition(144, 10, 1),
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
				createPosition(101, 8, 12),
				createPosition(104, 8, 12),
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
				createPosition(143, 8, 5),
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
				createPosition(108, 0, 3),
				createPosition(112, 0, 3),
				createPosition(117, 0, 3),
				createPosition(125, 4, 14),
				createPosition(144, 0, 5),
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
				createPosition(98, 17, 6),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testNamespaceHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.NAMESPACE);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(100, 10, 2),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testLabelHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.LABEL);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(124, 0, 5),
				createPosition(126, 46, 5),
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
				createPosition(127, 4, 13),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testExternalSDKHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.EXTERNAL_SDK);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(130, 13, 9),
				createPosition(131, 4, 11),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testOverloadedOperatorHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.OVERLOADED_OPERATOR);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(123, 7, 1),
				createPosition(123, 11, 1),
				createPosition(123, 13, 1),
			};
		if (PRINT_POSITIONS) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
}
