/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;
import org.eclipse.cdt.core.model.CModelException;

import junit.framework.*;

/**
 * @author hamer
 *
 */
public class StructuralStructureTests extends IStructureTests {
	/**
	 * @param name
	 */
	public StructuralStructureTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite= new TestSuite( StructuralStructureTests.class.getName() );
		
		// TODO check C-only behaviour using C_NATURE vs CC_NATURE
		
		// Interface tests:
		suite.addTest( new StructuralStructureTests("testGetChildrenOfTypeStruct"));
		suite.addTest( new StructuralStructureTests("testGetChildrenOfTypeClass")); // C++ only
		suite.addTest( new StructuralStructureTests("testGetFields"));
		suite.addTest( new StructuralStructureTests("testGetField"));
		suite.addTest( new StructuralStructureTests("testGetMethods")); // C++ only
		suite.addTest( new StructuralStructureTests("testGetMethod")); // C++ only
		suite.addTest( new StructuralStructureTests("testIsStruct"));
		suite.addTest( new StructuralStructureTests("testIsClass")); // C++ only
		suite.addTest( new StructuralStructureTests("testIsUnion"));
		suite.addTest( new StructuralStructureTests("testIsAbstract")); // C++ only
		suite.addTest( new StructuralStructureTests("testGetBaseTypes")); // C++ only
		suite.addTest( new StructuralStructureTests("testGetAccessControl")); // C++ only
		
		// Language Specification tests:		
		suite.addTest( new StructuralStructureTests("testAnonymousStructObject"));
		suite.addTest( new StructuralStructureTests("testInnerStruct"));
				
		return suite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testAnonymousStructObject()
	 */
	@Override
	public void testAnonymousStructObject() throws CModelException {
		setStructuralParse(true);
		super.testAnonymousStructObject();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetAccessControl()
	 */
	@Override
	public void testGetAccessControl() throws CModelException {
		setStructuralParse(true);
		super.testGetAccessControl();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetBaseTypes()
	 */
	@Override
	public void testGetBaseTypes() throws CModelException {
		setStructuralParse(true);
		super.testGetBaseTypes();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetChildrenOfTypeClass()
	 */
	@Override
	public void testGetChildrenOfTypeClass() throws CModelException {
		setStructuralParse(true);
		super.testGetChildrenOfTypeClass();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetChildrenOfTypeStruct()
	 */
	@Override
	public void testGetChildrenOfTypeStruct() throws CModelException {
		setStructuralParse(true);
		super.testGetChildrenOfTypeStruct();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetField()
	 */
	@Override
	public void testGetField() throws CModelException {
		setStructuralParse(true);
		super.testGetField();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetFields()
	 */
	@Override
	public void testGetFields() throws CModelException {
		setStructuralParse(true);
		super.testGetFields();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetFieldsHack()
	 */
	@Override
	public void testGetFieldsHack() throws CModelException {
		setStructuralParse(true);
		super.testGetFieldsHack();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetMethod()
	 */
	@Override
	public void testGetMethod() throws CModelException {
		setStructuralParse(true);
		super.testGetMethod();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetMethods()
	 */
	@Override
	public void testGetMethods() throws CModelException {
		setStructuralParse(true);
		super.testGetMethods();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetMethodsHack()
	 */
	@Override
	public void testGetMethodsHack() throws CModelException {
		setStructuralParse(true);
		super.testGetMethodsHack();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testGetStructureInfo()
	 */
	@Override
	public void testGetStructureInfo() {
		setStructuralParse(true);
		super.testGetStructureInfo();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testInnerStruct()
	 */
	@Override
	public void testInnerStruct() throws CModelException {
		setStructuralParse(true);
		super.testInnerStruct();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testIsAbstract()
	 */
	@Override
	public void testIsAbstract() throws CModelException {
		setStructuralParse(true);
		super.testIsAbstract();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testIsClass()
	 */
	@Override
	public void testIsClass() throws CModelException {
		setStructuralParse(true);
		super.testIsClass();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testIsStruct()
	 */
	@Override
	public void testIsStruct() throws CModelException {
		setStructuralParse(true);
		super.testIsStruct();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IStructureTests#testIsUnion()
	 */
	@Override
	public void testIsUnion() throws CModelException {
		setStructuralParse(true);
		super.testIsUnion();
	}
}
