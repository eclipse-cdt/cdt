/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 9, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.core.model.IVariable;


/**
 * @author bnicolle
 *
 */
public class DeclaratorsTests extends IntegratedCModelTest {
	/**
	 * @param name
	 */
	public DeclaratorsTests(String name) {
		super(name);
	}
	
	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	public String getSourcefileSubdir() {
		return "resources/cmodel/";
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	public String getSourcefileResource() {
		return "DeclaratorsTests.cpp";
	}
	
	/**
	 * @returns a test suite named after this class
	 *          containing all its public members named "test*"
	 */
	public static Test suite() {
		TestSuite suite= new TestSuite(DeclaratorsTests.class);
		return suite;
	}


    public void testDeclarators_0001() throws CModelException {
        ITranslationUnit tu = getTU();
        ICElement element = tu.getElement("decl_0001");
        assertNotNull(element);
        assertEquals(element.getElementType(), ICElement.C_FUNCTION_DECLARATION);
        IFunctionDeclaration decl = (IFunctionDeclaration)element;
        assertEquals(decl.getSignature(), "decl_0001(char)");
        assertEquals(decl.getReturnType(), "void");
    }

    public void testDeclarators_0002() throws CModelException {
        ITranslationUnit tu = getTU();
        ICElement element = tu.getElement("decl_0002");
        assertNotNull(element);
        assertEquals(element.getElementType(), ICElement.C_FUNCTION_DECLARATION);
        IFunctionDeclaration decl = (IFunctionDeclaration)element;
        assertEquals(decl.getSignature(), "decl_0002(char)");
        assertEquals(decl.getReturnType(), "void");
    }

  

    public void testDeclarators_0004() throws CModelException {
        ITranslationUnit tu = getTU();
        ICElement element = tu.getElement("decl_0004");
        assertNotNull(element);
        assertEquals(element.getElementType(), ICElement.C_FUNCTION_DECLARATION);
        IFunctionDeclaration decl = (IFunctionDeclaration)element;
        assertEquals(decl.getSignature(), "decl_0004(char)");
        assertEquals(decl.getReturnType(), "void*");
    }

    public void testDeclarators_0005() throws CModelException {
        ITranslationUnit tu = getTU();
        ICElement element = tu.getElement("decl_0005");
        assertNotNull(element);
        assertEquals(element.getElementType(), ICElement.C_VARIABLE);
        IVariable decl = (IVariable)element;
        assertEquals(decl.getTypeName(), "void(*)(char)");
    }

    public void testDeclarators_0015() throws CModelException {
        ITranslationUnit tu = getTU();
        ICElement element = tu.getElement("decl_0015");
        assertNotNull(element);
        assertEquals(element.getElementType(), ICElement.C_TYPEDEF);
        ITypeDef decl = (ITypeDef)element;
        assertEquals(decl.getTypeName(), "void(*)(char)");
    }
}
