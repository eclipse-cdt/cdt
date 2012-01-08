/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.pdom.tests;

import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Tests for verifying whether the PDOM correctly stores information about
 * C structs and unions.
 */
public class CCompositeTypeTests extends PDOMTestBase {

	private ICProject project;
	private PDOM pdom;

	public static Test suite() {
		return new TestSuite(CCompositeTypeTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		CCompositeTypeTests foo = null;
		
		project = createProject("compositeTypeTests");
		pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(project);
		pdom.acquireReadLock();
	}
	
	@Override
	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
		if (project != null) {
			project.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
	}
	
	//TODO PDOM does not distinguish between a struct or union in C
	public void _testSimpleCStructureDistinction() throws Exception {
		assertType(pdom, "SimpleCStructure", ICompositeType.class);
		IIndexBinding[] bindings = pdom.findBindings(Pattern.compile("SimpleCStructure"), false, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(1, bindings.length);
		assertEquals(ICompositeType.k_struct, ((ICompositeType)bindings[0]).getKey());
	}
	
	// test struct definitions and struct member declarations in C
	public void testSimpleCStructureDeclarations() throws Exception {
		assertDeclarationCount(pdom, "SimpleCStructure", 1);
		assertDeclarationCount(pdom, "SimpleCStructure::scsa", 1);
	}
	
	// test struct definitions and struct member definitions in C
	public void testSimpleCStructureDefinitions() throws Exception {
		assertDefinitionCount(pdom, "SimpleCStructure", 1);
		assertDefinitionCount(pdom, "SimpleCStructure::scsa", 1);
	}
	
	// test struct definitions and struct member references in C
	public void testSimpleCStructureReferences() throws Exception {
		assertReferenceCount(pdom, "SimpleCStructure", 2);
		assertReferenceCount(pdom, "SimpleCStructure::scsa", 2);
	}

	// test nesting of structs in C, they should not nest
	public void testDeepCStructure() throws Exception {
		assertType(pdom, "CStructure1", ICompositeType.class);
		assertType(pdom, "CStructure2", ICompositeType.class);
		assertType(pdom, "CStructure3", ICompositeType.class);
	}
	
	// test "nested" struct declarations in C, they should not nest
	public void testDeepCStructureDeclarations() throws Exception {
		assertDeclarationCount(pdom, "CStructure1", 1);
		assertDeclarationCount(pdom, "CStructure1::CStructure2", 0);
		assertDeclarationCount(pdom, "CStructure2", 1);
		assertDeclarationCount(pdom, "CStructure1::CStructure2::CStructure3", 0);
		assertDeclarationCount(pdom, "CStructure3", 1);
	}
	
	// test "nested" struct member declarations in C, they should not nest
	public void testDeepCStructureMemberDeclarations() throws Exception {
		assertDeclarationCount(pdom, "CStructure1::cs1a", 1);
		assertDeclarationCount(pdom, "CStructure1::cs1b", 1);
		assertDeclarationCount(pdom, "CStructure1::cs1c", 1);
		assertDeclarationCount(pdom, "CStructure1::CStructure2::cs2b", 0);
		assertDeclarationCount(pdom, "CStructure2::cs2b", 1);
		assertDeclarationCount(pdom, "CStructure1::CStructure2::CStructure3::cs3a", 0);
		assertDeclarationCount(pdom, "CStructure3::cs3a", 1);
	}
	
	// test "nested" struct definitions in C, they should not nest
	public void testDeepCStructureDefinitions() throws Exception {
		assertDefinitionCount(pdom, "CStructure1", 1);
		assertDefinitionCount(pdom, "CStructure1::CStructure2", 0);
		assertDefinitionCount(pdom, "CStructure2", 1);
		assertDefinitionCount(pdom, "CStructure1::CStructure2::CStructure3", 0);
		assertDefinitionCount(pdom, "CStructure3", 1);
	}

	// test "nested" struct member definitions in C, they should not nest
	public void testDeepCStructureMemberDefinitions() throws Exception {
		assertDefinitionCount(pdom, "CStructure1::cs1a", 1);
		assertDefinitionCount(pdom, "CStructure1::cs1b", 1);
		assertDefinitionCount(pdom, "CStructure1::cs1c", 1);
		assertDefinitionCount(pdom, "CStructure1::CStructure2::cs2b", 0);
		assertDefinitionCount(pdom, "CStructure2::cs2b", 1);
		assertDefinitionCount(pdom, "CStructure1::CStructure2::CStructure3::cs3a", 0);
		assertDefinitionCount(pdom, "CStructure3::cs3a", 1);
	}

	// test "nested" struct references in C, they should not nest
	public void testDeepCStructureReferences() throws Exception {
		assertReferenceCount(pdom, "CStructure1", 2);
		assertReferenceCount(pdom, "CStructure1::CStructure2", 0);
		assertReferenceCount(pdom, "CStructure2", 2);
		assertReferenceCount(pdom, "CStructure1::CStructure2::CStructure3", 0);
		assertReferenceCount(pdom, "CStructure3", 2);
	}
	
	// test "nested" struct member references in C, they should not nest
	public void testDeepCStructureMemberReferences() throws Exception {
		assertReferenceCount(pdom, "CStructure1::cs1a", 2);
		assertReferenceCount(pdom, "CStructure1::cs1b", 3);
		assertReferenceCount(pdom, "CStructure1::cs1c", 14);
		assertReferenceCount(pdom, "CStructure1::CStructure2::cs2b", 0);
		assertReferenceCount(pdom, "CStructure2::cs2b", 12);
		assertReferenceCount(pdom, "CStructure1::CStructure2::CStructure3::cs3a", 0);
		assertReferenceCount(pdom, "CStructure3::cs3a", 8);
	}
	
//	TODO PDOM does not distinguish between a struct or union in C
	public void _testCUnionDistinction() throws Exception {
		IIndexBinding[] bindings = pdom.findBindings(Pattern.compile("CUnion1"), false, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(1, bindings.length);
		assertEquals(ICompositeType.k_union, ((ICompositeType)bindings[0]).getKey());
	}
	
	//test union and "nested" union declarations in C, but there is no nesting in C
	public void testCUnionDeclarations() throws Exception {
		assertDeclarationCount(pdom, "CUnion1", 1);
		assertDeclarationCount(pdom, "CUnion1::CUnion2", 0);
		assertDeclarationCount(pdom, "CUnion2", 1);
	}
	
	//test union and "nested" union definitons in C, but there is no nesting in C
	public void testCUnionDefinitions() throws Exception {
		assertDefinitionCount(pdom, "CUnion1", 1);
		assertDefinitionCount(pdom, "CUnion1::CUnion2", 0);
		assertDefinitionCount(pdom, "CUnion2", 1);
	}
	
	//test union and "nested" union references in C, but there is no nesting in C
	public void testCUnionReferences() throws Exception {
		assertReferenceCount(pdom, "CUnion1", 2);
		assertReferenceCount(pdom, "CUnion1::CUnion2", 0);
		assertReferenceCount(pdom, "CUnion2", 2);
	}
	
	//test union member declarations in C
	public void testCUnionMemberDeclarations() throws Exception {
		assertDeclarationCount(pdom, "CUnion1::cu1a", 1);
		assertDeclarationCount(pdom, "CUnion1::cu1d", 1);
	}
	
	//test union member defintions in C
	public void testCUnionMemberDefinitions() throws Exception {
		assertDefinitionCount(pdom, "CUnion1::cu1a", 1);
		assertDefinitionCount(pdom, "CUnion1::cu1d", 1);
	}
	
	//test union member references in C
	public void testCUnionMemberReferences() throws Exception {
		assertReferenceCount(pdom, "CUnion1::cu1a", 2);
		assertReferenceCount(pdom, "CUnion1::cu1d", 1);
	}
	
	// test "nested" unions and structs declarations in C, they should not nest
	public void testCMixedDeclarations() throws Exception {
		assertDeclarationCount(pdom, "CMixedS1::CMixedU1", 0);
		assertDeclarationCount(pdom, "CMixedS1::CMixedU1::CMixedS2", 0);
		assertDeclarationCount(pdom, "CMixedS1::CMixedU1::CMixedU2", 0);
		assertDeclarationCount(pdom, "CMixedS1::CMixedS3", 0);
		
		assertDeclarationCount(pdom, "CMixedU1", 1);
		assertDeclarationCount(pdom, "CMixedS2", 1);
		assertDeclarationCount(pdom, "CMixedU2", 1);
		assertDeclarationCount(pdom, "CMixedS3", 1);
	}
	
	// test "nested" unions and structs definitions in C, they should not nest
	public void testCMixedDefinitions() throws Exception {
		assertDefinitionCount(pdom, "CMixedS1::CMixedU1", 0);
		assertDefinitionCount(pdom, "CMixedS1::CMixedU1::CMixedS2", 0);
		assertDefinitionCount(pdom, "CMixedS1::CMixedU1::CMixedU2", 0);
		assertDefinitionCount(pdom, "CMixedS1::CMixedS3", 0);
		
		assertDefinitionCount(pdom, "CMixedU1", 1);
		assertDefinitionCount(pdom, "CMixedS2", 1);
		assertDefinitionCount(pdom, "CMixedU2", 1);
		assertDefinitionCount(pdom, "CMixedS3", 1);
	}
	
	// test "nested" unions and structs references in C, they should not nest
	public void testCMixedReferences() throws Exception {
		assertReferenceCount(pdom, "CMixedS1::CMixedU1", 0);
		assertReferenceCount(pdom, "CMixedS1::CMixedU1::CMixedS2", 0);
		assertReferenceCount(pdom, "CMixedS1::CMixedU1::CMixedU2", 0);
		assertReferenceCount(pdom, "CMixedS1::CMixedS3", 0);
		
		assertReferenceCount(pdom, "CMixedU1", 2);
		assertReferenceCount(pdom, "CMixedS2", 2);
		assertReferenceCount(pdom, "CMixedU2", 2);
		assertReferenceCount(pdom, "CMixedS3", 2);
	}
	
	// test "nested" union members and struct members declarations in C, they should not nest
	public void testCMixedMemberDeclarations() throws Exception {
		assertDeclarationCount(pdom, "CMixedS1::CMixedU1::cmu1a", 0);
		assertDeclarationCount(pdom, "CMixedS1::CMixedU1::CMixedS2::cms2a", 0);
		assertDeclarationCount(pdom, "CMixedS1::CMixedU1::CMixedU2::cmu2a", 0);
		assertDeclarationCount(pdom, "CMixedS1::CMixedS3::cms3a", 0);
		
		assertDeclarationCount(pdom, "CMixedU1::cmu1a", 1);
		assertDeclarationCount(pdom, "CMixedS2::cms2a", 1);
		assertDeclarationCount(pdom, "CMixedU2::cmu2a", 1);
		assertDeclarationCount(pdom, "CMixedS3::cms3a", 1);
	}
	
	// test "nested" union members and struct members definitions in C, they should not nest
	public void testCMixedMemberDefinitions() throws Exception {
		assertDefinitionCount(pdom, "CMixedS1::CMixedU1::cmu1a", 0);
		assertDefinitionCount(pdom, "CMixedS1::CMixedU1::CMixedS2::cms2a", 0);
		assertDefinitionCount(pdom, "CMixedS1::CMixedU1::CMixedU2::cmu2a", 0);
		assertDefinitionCount(pdom, "CMixedS1::CMixedS3::cms3a", 0);
		
		assertDefinitionCount(pdom, "CMixedU1::cmu1a", 1);
		assertDefinitionCount(pdom, "CMixedS2::cms2a", 1);
		assertDefinitionCount(pdom, "CMixedU2::cmu2a", 1);
		assertDefinitionCount(pdom, "CMixedS3::cms3a", 1);
	}
	
	// test "nested" union members and struct members references in C, they should not nest
	public void testCMixedMemberReferences() throws Exception {
		assertReferenceCount(pdom, "CMixedS1::CMixedU1::cmu1a", 0);
		assertReferenceCount(pdom, "CMixedS1::CMixedU1::CMixedS2::cms2a", 0);
		assertReferenceCount(pdom, "CMixedS1::CMixedU1::CMixedU2::cmu2a", 0);
		assertReferenceCount(pdom, "CMixedS1::CMixedS3::cms3a", 0);
		
		assertReferenceCount(pdom, "CMixedU1::cmu1a", 2);
		assertReferenceCount(pdom, "CMixedS2::cms2a", 2);
		assertReferenceCount(pdom, "CMixedU2::cmu2a", 2);
		assertReferenceCount(pdom, "CMixedS3::cms3a", 2);
	}	
}
