/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;

/**
 * Tests for verifying whether the PDOM correctly stores information about
 * C++ structs and unions.
 */
public class CPPCompositeTypeTests extends PDOMTestBase {

	private ICProject project;
	private PDOM pdom;

	public static Test suite() {
		return new TestSuite(CPPCompositeTypeTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		CPPCompositeTypeTests foo = null;
		
		project = createProject("compositeTypeTests");
		pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(project);
		pdom.acquireReadLock();
	}
	
	@Override
	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
	}
	
	public void testSimpleStructure() throws Exception {
		assertType(pdom, "SimpleStructure", ICompositeType.class);
	}
	
	public void testSimpleStructureDeclarations() throws Exception {
		assertDeclarationCount(pdom, "SimpleStructure", 1);
		assertDeclarationCount(pdom, "SimpleStructure::ssa", 1);
	}

	public void testSimpleStructureDefinitions() throws Exception {
		assertDefinitionCount(pdom, "SimpleStructure", 1);
		assertDefinitionCount(pdom, "SimpleStructure::ssa", 1);
	}
	
	public void testSimpleStructureReferences() throws Exception {
		assertReferenceCount(pdom, "SimpleStructure", 2);
		assertReferenceCount(pdom, "SimpleStructure::ssa", 2);
	}

	public void testDeepStructure() throws Exception {
		assertType(pdom, "Structure1", ICompositeType.class);
		assertType(pdom, "Structure1::Structure2", ICompositeType.class);
		assertType(pdom, "Structure1::Structure2::Structure3", ICompositeType.class);
	}
	
	public void testDeepStructureDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Structure1", 1);
		assertDeclarationCount(pdom, "Structure1::Structure2", 1);
		assertDeclarationCount(pdom, "Structure1::Structure2::Structure3", 1);
	}
	
	public void testDeepStructureMemberDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Structure1::s1a", 1);
		assertDeclarationCount(pdom, "Structure1::s1b", 1);
		assertDeclarationCount(pdom, "Structure1::s1c", 1);
		assertDeclarationCount(pdom, "Structure1::Structure2::s2b", 1);
		assertDeclarationCount(pdom, "Structure1::Structure2::Structure3::s3a", 1);
	}
	
	public void testDeepStructureDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Structure1", 1);
		assertDefinitionCount(pdom, "Structure1::Structure2", 1);
		assertDefinitionCount(pdom, "Structure1::Structure2::Structure3", 1);
	}

	public void testDeepStructureMemberDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Structure1::s1a", 1);
		assertDefinitionCount(pdom, "Structure1::s1b", 1);
		assertDefinitionCount(pdom, "Structure1::s1c", 1);
		assertDefinitionCount(pdom, "Structure1::Structure2::s2b", 1);
		assertDefinitionCount(pdom, "Structure1::Structure2::Structure3::s3a", 1);
	}

	public void testDeepStructureReferences() throws Exception {
		assertReferenceCount(pdom, "Structure1", 6);
		assertReferenceCount(pdom, "Structure1::Structure2", 4);
		assertReferenceCount(pdom, "Structure1::Structure2::Structure3", 2);
	}
	
	public void testDeepStructureMemberReferences() throws Exception {
		assertReferenceCount(pdom, "Structure1::s1a", 2);
		assertReferenceCount(pdom, "Structure1::s1b", 3);
		assertReferenceCount(pdom, "Structure1::s1c", 14);
		assertReferenceCount(pdom, "Structure1::Structure2::s2b", 12);
		assertReferenceCount(pdom, "Structure1::Structure2::Structure3::s3a", 8);
	}

	public void testUnionDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Union1", 1);
		assertDeclarationCount(pdom, "Union1::Union2", 1);
	}
	
	public void testUnionDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Union1", 1);
		assertDefinitionCount(pdom, "Union1::Union2", 1);
	}
	
	public void testUnionReferences() throws Exception {
		assertReferenceCount(pdom, "Union1", 4);
		assertReferenceCount(pdom, "Union1::Union2", 2);
	}
	
	public void testUnionMemberDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Union1::u1a", 1);
		assertDeclarationCount(pdom, "Union1::u1d", 1);
	}
	
	public void testUnionMemberDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Union1::u1a", 1);
		assertDefinitionCount(pdom, "Union1::u1d", 1);
	}
	
	public void testUnionMemberReferences() throws Exception {
		assertReferenceCount(pdom, "Union1::u1a", 2);
		assertReferenceCount(pdom, "Union1::u1d", 1);
	}
	
	public void testMixedDeclarations() throws Exception {
		assertDeclarationCount(pdom, "MixedS1::MixedU1", 1);
		assertDeclarationCount(pdom, "MixedS1::MixedU1::MixedS2", 1);
		assertDeclarationCount(pdom, "MixedS1::MixedU1::MixedU2", 1);
		assertDeclarationCount(pdom, "MixedS1::MixedS3", 1);
	}
	
	public void testMixedDefinitions() throws Exception {
		assertDefinitionCount(pdom, "MixedS1::MixedU1", 1);
		assertDefinitionCount(pdom, "MixedS1::MixedU1::MixedS2", 1);
		assertDefinitionCount(pdom, "MixedS1::MixedU1::MixedU2", 1);
		assertDefinitionCount(pdom, "MixedS1::MixedS3", 1);
	}
	
	public void testMixedReferences() throws Exception {
		assertReferenceCount(pdom, "MixedS1::MixedU1", 6);
		assertReferenceCount(pdom, "MixedS1::MixedU1::MixedS2", 2);
		assertReferenceCount(pdom, "MixedS1::MixedU1::MixedU2", 2);
		assertReferenceCount(pdom, "MixedS1::MixedS3", 2);
	}
	
	public void testMixedMemberDeclarations() throws Exception {
		assertDeclarationCount(pdom, "MixedS1::MixedU1::mu1a", 1);
		assertDeclarationCount(pdom, "MixedS1::MixedU1::MixedS2::ms2a", 1);
		assertDeclarationCount(pdom, "MixedS1::MixedU1::MixedU2::mu2a", 1);
		assertDeclarationCount(pdom, "MixedS1::MixedS3::ms3a", 1);
	}
	
	public void testMixedMemberDefinitions() throws Exception {
		assertDefinitionCount(pdom, "MixedS1::MixedU1::mu1a", 1);
		assertDefinitionCount(pdom, "MixedS1::MixedU1::MixedS2::ms2a", 1);
		assertDefinitionCount(pdom, "MixedS1::MixedU1::MixedU2::mu2a", 1);
		assertDefinitionCount(pdom, "MixedS1::MixedS3::ms3a", 1);
	}
	
	public void testMixedMemberReferences() throws Exception {
		assertReferenceCount(pdom, "MixedS1::MixedU1::mu1a", 2);
		assertReferenceCount(pdom, "MixedS1::MixedU1::MixedS2::ms2a", 2);
		assertReferenceCount(pdom, "MixedS1::MixedU1::MixedU2::mu2a", 2);
		assertReferenceCount(pdom, "MixedS1::MixedS3::ms3a", 2);
	}
	
}
