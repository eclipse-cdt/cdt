/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import org.eclipse.cdt.core.model.CModelException;
import org.junit.jupiter.api.Test;

/**
 * @author hamer
 *
 */
public class StructuralStructureTests extends IStructureTests {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testAnonymousStructObject()
	 */
	@Override
	@Test
	public void testAnonymousStructObject() throws CModelException {
		setStructuralParse(true);
		super.testAnonymousStructObject();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetAccessControl()
	 */
	@Override
	@Test
	public void testGetAccessControl() throws CModelException {
		setStructuralParse(true);
		super.testGetAccessControl();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetBaseTypes()
	 */
	@Override
	@Test
	public void testGetBaseTypes() throws CModelException {
		setStructuralParse(true);
		super.testGetBaseTypes();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetChildrenOfTypeClass()
	 */
	@Override
	@Test
	public void testGetChildrenOfTypeClass() throws CModelException {
		setStructuralParse(true);
		super.testGetChildrenOfTypeClass();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetChildrenOfTypeStruct()
	 */
	@Override
	@Test
	public void testGetChildrenOfTypeStruct() throws CModelException {
		setStructuralParse(true);
		super.testGetChildrenOfTypeStruct();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetField()
	 */
	@Override
	@Test
	public void testGetField() throws CModelException {
		setStructuralParse(true);
		super.testGetField();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetFields()
	 */
	@Override
	@Test
	public void testGetFields() throws CModelException {
		setStructuralParse(true);
		super.testGetFields();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetFieldsHack()
	 */
	@Override
	@Test
	public void testGetFieldsHack() throws CModelException {
		setStructuralParse(true);
		super.testGetFieldsHack();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetMethod()
	 */
	@Override
	@Test
	public void testGetMethod() throws CModelException {
		setStructuralParse(true);
		super.testGetMethod();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetMethods()
	 */
	@Override
	@Test
	public void testGetMethods() throws CModelException {
		setStructuralParse(true);
		super.testGetMethods();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetMethodsHack()
	 */
	@Override
	@Test
	public void testGetMethodsHack() throws CModelException {
		setStructuralParse(true);
		super.testGetMethodsHack();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetStructureInfo()
	 */
	@Override
	@Test
	public void testGetStructureInfo() {
		setStructuralParse(true);
		super.testGetStructureInfo();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testInnerStruct()
	 */
	@Override
	@Test
	public void testInnerStruct() throws CModelException {
		setStructuralParse(true);
		super.testInnerStruct();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testIsAbstract()
	 */
	@Override
	@Test
	public void testIsAbstract() throws CModelException {
		setStructuralParse(true);
		super.testIsAbstract();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testIsClass()
	 */
	@Override
	@Test
	public void testIsClass() throws CModelException {
		setStructuralParse(true);
		super.testIsClass();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testIsStruct()
	 */
	@Override
	@Test
	public void testIsStruct() throws CModelException {
		setStructuralParse(true);
		super.testIsStruct();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testIsUnion()
	 */
	@Override
	@Test
	public void testIsUnion() throws CModelException {
		setStructuralParse(true);
		super.testIsUnion();
	}
}
