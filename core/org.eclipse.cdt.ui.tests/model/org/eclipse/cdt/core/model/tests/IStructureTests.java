/*
 * Created on Jun 9, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.IField;

import junit.framework.*;

import java.util.ArrayList;

/**
 * @author bnicolle
 *
 */
public class IStructureTests extends IntegratedCModelTest {
	/**
	 * @param name
	 */
	public IStructureTests(String name) {
		super(name);
	}
	
	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	public String getSourcefileSubdir() {
		return "model/org.eclipse.cdt.core.model.tests.resources/";
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	public String getSourcefileResource() {
		return "IStructure.c";
	}
	
	/**
	 * @returns a test suite named after this class
	 *          containing all its public members named "test*"
	 */
	public static Test suite() {
		TestSuite suite= new TestSuite( IStructureTests.class.getName() );
		// TODO: suite.addTest( new IStructureTests("testGetField"));
		
		// TODO: implement the other tests here once IStructure is properly implemented!
		return suite;
	}


	public void testGetAccessControl() {
		// test getAccessControl()
		// test getAccessControl(int)
	}
	public void testGetBaseTypes() {		
	}
	public void testGetField() {
		ITranslationUnit tu = getTU();
		ArrayList arrayStructs = tu.getChildrenOfType(ITranslationUnit.C_STRUCT);
		ArrayList arrayClasses = tu.getChildrenOfType(ITranslationUnit.C_CLASS);
		IStructure myIStruct = (IStructure) arrayStructs.get(0);
		assertNotNull(myIStruct);
		IField myIField = myIStruct.getField("bar");
		assertNotNull(myIField);
	}
	public void testGetFields() {
	}
	public void testGetInitializer() {
	}
	public void testGetMethod() {		
	}
	public void testGetMethods() {		
	}
	public void testGetStructureInfo() {
	}
	public void testGetTypeName() {		
	}
	public void testIsAbstract() {		
	}
	public void testIsClass() {		
	}
	public void testIsConst() {		
	}
	public void testIsStatic() {		
	}
	public void testIsStruct() {		
	}
	public void testIsUnion() {		
	}
	public void testIsVolatile() {		
	}
}
