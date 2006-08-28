/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
	
	private static final Class THIS= SemanticHighlightingTest.class;
	
	public static Test suite() {
		return new SemanticHighlightingTestSetup(new TestSuite(THIS), "/SHTest/src/SHTest.cpp");
	}

//	public void testStaticConstFieldHighlighting() throws Exception {
//		setUpSemanticHighlighting(SemanticHighlightings.STATIC_CONST_FIELD);
//		Position[] expected= new Position[] {
//				createPosition(6, 18, 16),
//				createPosition(35, 37, 16),
//		};
//		Position[] actual= getSemanticHighlightingPositions();
////		System.out.println(toString(actual));
//		assertEqualPositions(expected, actual);
//	}
	
	public void testStaticFieldHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.STATIC_FIELD);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(23, 15, 14),
				createPosition(25, 21, 19),
				createPosition(41, 21, 20),
				createPosition(42, 15, 15),
				createPosition(56, 21, 20),
				createPosition(57, 15, 15),
				createPosition(115, 20, 15),
			};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testFieldHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.FIELD);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(24, 14, 13),
				createPosition(26, 8, 8),
				createPosition(43, 15, 14),
				createPosition(44, 8, 9),
				createPosition(58, 15, 14),
				createPosition(59, 8, 9),
				createPosition(74, 7, 5),
				createPosition(75, 7, 5),
				createPosition(77, 8, 5),
				createPosition(78, 8, 5),
				createPosition(86, 8, 11),
				createPosition(90, 8, 10),
				createPosition(102, 11, 9),
				createPosition(107, 11, 8),
				createPosition(112, 7, 11),
				createPosition(114, 7, 10),
			};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testMethodDeclarationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.METHOD_DECLARATION);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(28, 15, 15),
				createPosition(32, 8, 9),
				createPosition(46, 15, 16),
				createPosition(47, 8, 10),
				createPosition(61, 15, 16),
				createPosition(62, 8, 10),
				createPosition(76, 4, 13),
				createPosition(101, 4, 26),
				createPosition(101, 20, 10),
				createPosition(105, 4, 25),
				createPosition(105, 20, 9),
				createPosition(110, 4, 32),
				createPosition(110, 20, 16),
			};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testMethodInvocationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.METHOD_INVOCATION);
		Position[] expected= new Position[] {
				createPosition(111, 22, 9),
				createPosition(113, 21, 8),
				createPosition(115, 4, 15),
			};
		Position[] actual= getSemanticHighlightingPositions();
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	//	public void testStaticMethodInvocationHighlighting() throws Exception {
//		setUpSemanticHighlighting(SemanticHighlightings.STATIC_METHOD_INVOCATION);
//		Position[] expected= new Position[] {
//		};
//		Position[] actual= getSemanticHighlightingPositions();
//		System.out.println(toString(actual));
//		assertEqualPositions(expected, actual);
//	}
	
	/*
	 */
//	public void testVirtualMethodInvocationHighlighting() throws Exception {
//		setUpSemanticHighlighting(SemanticHighlightings.VIRTUAL_METHOD_INVOCATION);
//		Position[] expected= new Position[] {
//				createPosition(11, 2, 14),
//		};
//		Position[] actual= getSemanticHighlightingPositions();
////		System.out.println(toString(actual));
//		assertEqualPositions(expected, actual);
//	}
	
//	public void testInheritedMethodInvocationHighlighting() throws Exception {
//		setUpSemanticHighlighting(SemanticHighlightings.INHERITED_METHOD_INVOCATION);
//		Position[] expected= new Position[] {
//				createPosition(12, 2, 8),
//				createPosition(15, 17, 8),
//		};
//		Position[] actual= getSemanticHighlightingPositions();
////		System.out.println(toString(actual));
//		assertEqualPositions(expected, actual);
//	}
	
	public void testLocalVariableDeclarationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.LOCAL_VARIABLE_DECLARATION);
		Position[] expected= new Position[] {
				createPosition(106, 8, 8),
				createPosition(111, 14, 2),
				createPosition(113, 13, 2),
			};
		Position[] actual= getSemanticHighlightingPositions();
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testLocalVariableHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.LOCAL_VARIABLE);
		Position[] expected= new Position[] {
				createPosition(112, 4, 2),
				createPosition(114, 4, 2),
			};
		Position[] actual= getSemanticHighlightingPositions();
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testParameterVariableHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.PARAMETER_VARIABLE);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(11, 20, 1),
				createPosition(28, 35, 3),
				createPosition(29, 8, 19),
				createPosition(30, 19, 3),
				createPosition(76, 21, 4),
				createPosition(76, 30, 4),
				createPosition(77, 16, 4),
				createPosition(78, 16, 4),
			};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testTemplateParameterHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.TEMPLATE_PARAMETER);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
		};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
//	public void testTemplateArgumentHighlighting() throws Exception {
//		setUpSemanticHighlighting(SemanticHighlightings.TEMPLATE_ARGUMENT);
//		Position[] actual= getSemanticHighlightingPositions();
//		Position[] expected= new Position[] {
//				createPosition(41, 8, 6),
//		};
////		System.out.println(toString(actual));
//		assertEqualPositions(expected, actual);
//	}
	
	public void testEnumHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.ENUM);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(3, 5, 11),
				createPosition(34, 9, 14),
				createPosition(49, 9, 15),
				createPosition(64, 9, 15),
			};
//		System.out.println(toString(actual));
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
				createPosition(35, 10, 8),
				createPosition(36, 10, 9),
				createPosition(37, 10, 8),
				createPosition(38, 12, 8),
				createPosition(50, 10, 9),
				createPosition(51, 10, 10),
				createPosition(52, 10, 9),
				createPosition(53, 12, 9),
				createPosition(65, 10, 9),
				createPosition(66, 10, 10),
				createPosition(67, 10, 9),
				createPosition(68, 12, 9),
				createPosition(73, 22, 13),
				createPosition(82, 19, 24),
				createPosition(82, 46, 13),
				createPosition(82, 63, 5),
				createPosition(85, 7, 9),
				createPosition(89, 6, 8),
				createPosition(93, 8, 8),
				createPosition(111, 4, 9),
				createPosition(113, 4, 8),
			};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testFunctionDeclarationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.FUNCTION_DECLARATION);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(11, 5, 10),
				createPosition(12, 12, 16),
				createPosition(19, 16, 10),
				createPosition(28, 15, 15),
				createPosition(32, 8, 9),
				createPosition(46, 15, 16),
				createPosition(47, 8, 10),
				createPosition(61, 15, 16),
				createPosition(62, 8, 10),
				createPosition(76, 4, 13),
				createPosition(97, 8, 13),
				createPosition(101, 4, 26),
				createPosition(101, 20, 10),
				createPosition(105, 4, 25),
				createPosition(105, 20, 9),
				createPosition(110, 4, 32),
				createPosition(110, 20, 16),
			};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testFunctionInvocationHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.FUNCTION_INVOCATION);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(29, 8, 19),
				createPosition(30, 8, 10),
				createPosition(111, 22, 9),
				createPosition(113, 21, 8),
				createPosition(115, 4, 15),
				createPosition(117, 4, 17),
			};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	
	public void testGlobalVariableHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.GLOBAL_VARIABLE);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(7, 10, 14),
				createPosition(8, 4, 14),
				createPosition(9, 11, 20),
				createPosition(96, 8, 14),
			};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testMacroSubstitutionHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.MACRO_SUBSTITUTION);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(29, 8, 19),
				createPosition(117, 4, 17),
			};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testTypedefHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.TYPEDEF);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(38, 21, 10),
				createPosition(53, 22, 11),
				createPosition(68, 22, 11),
				createPosition(93, 17, 6),
			};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testNamespaceHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.NAMESPACE);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(95, 10, 2),
			};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testLabelHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.LABEL);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(116, 0, 5),
			};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testEnumeratorHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.ENUMERATOR);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(4, 4, 10),
				createPosition(34, 25, 13),
				createPosition(49, 26, 14),
				createPosition(64, 26, 14),
			};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testProblemHighlighting() throws Exception {
		setUpSemanticHighlighting(SemanticHighlightings.PROBLEM);
		Position[] actual= getSemanticHighlightingPositions();
		Position[] expected= new Position[] {
				createPosition(73, 9, 2),
				createPosition(73, 12, 2),
				createPosition(74, 4, 2),
				createPosition(75, 4, 2),
				createPosition(76, 18, 2),
				createPosition(76, 27, 2),
				createPosition(82, 9, 2),
				createPosition(82, 60, 2),
			};
//		System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}

}
