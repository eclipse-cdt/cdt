/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.ui.refactoring.utils.PseudoNameGenerator;

/**
 * @author Mirko Stocker
 *
 */
public class PseudoNameGeneratorTest extends TestCase {

	private static final String CHAR2 = "char2"; //$NON-NLS-1$
	private static final String INT3 = "int3"; //$NON-NLS-1$
	private static final String INT2 = "int2"; //$NON-NLS-1$
	private static final String CHAR = "char"; //$NON-NLS-1$
	private static final String CHAR1 = "char1"; //$NON-NLS-1$
	private static final String INT = "int"; //$NON-NLS-1$
	private static final String INT1 = "int1"; //$NON-NLS-1$
	private PseudoNameGenerator pseudoNameGenerator;

	@Override
	protected void setUp() throws Exception {
		pseudoNameGenerator = new PseudoNameGenerator();
	}
	
	public void testNonConflictingCase() {
		assertEquals(INT1, pseudoNameGenerator.generateNewName(INT));
	}

	public void testMultipleNonConflictingCase() {
		assertEquals(INT1, pseudoNameGenerator.generateNewName(INT));
		assertEquals(CHAR1, pseudoNameGenerator.generateNewName(CHAR));
	}

	public void testConflictingCase() {
		pseudoNameGenerator.addExistingName(INT1);
		assertEquals(INT2, pseudoNameGenerator.generateNewName(INT));
	}

	public void testMultipleConflictsCase() {
		pseudoNameGenerator.addExistingName(INT1);
		pseudoNameGenerator.addExistingName(CHAR1);
		assertEquals(INT2, pseudoNameGenerator.generateNewName(INT));
		assertEquals(INT3, pseudoNameGenerator.generateNewName(INT));
		assertEquals(CHAR2, pseudoNameGenerator.generateNewName(CHAR));
	}
	
	public void testWithNamespace() {
		assertEquals("string", pseudoNameGenerator.generateNewName("std::string"));  //$NON-NLS-1$//$NON-NLS-2$
	}
	
	public void testBug288736TemplateParam() {
		assertEquals("tempClass", pseudoNameGenerator.generateNewName("tempClass<int>"));
	}
}
