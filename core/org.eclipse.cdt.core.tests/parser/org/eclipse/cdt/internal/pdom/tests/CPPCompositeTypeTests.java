/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation.
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

package org.eclipse.cdt.internal.pdom.tests;

import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for verifying whether the PDOM correctly stores information about
 * C++ structs and unions.
 */
public class CPPCompositeTypeTests extends PDOMTestBase {

	private ICProject project;
	private PDOM pdom;

	@BeforeEach
	protected void beforeEach() throws Exception {
		CPPCompositeTypeTests foo = null;

		project = createProject("compositeTypeTests");
		pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(project);
		pdom.acquireReadLock();
	}

	@AfterEach
	protected void afterEach() throws Exception {
		pdom.releaseReadLock();
	}

	@Test
	public void testSimpleStructure() throws Exception {
		assertType(pdom, "SimpleStructure", ICompositeType.class);
	}

	@Test
	public void testSimpleStructureDeclarations() throws Exception {
		assertDeclarationCount(pdom, "SimpleStructure", 1);
		assertDeclarationCount(pdom, "SimpleStructure::ssa", 1);
	}

	@Test
	public void testSimpleStructureDefinitions() throws Exception {
		assertDefinitionCount(pdom, "SimpleStructure", 1);
		assertDefinitionCount(pdom, "SimpleStructure::ssa", 1);
	}

	@Test
	public void testSimpleStructureReferences() throws Exception {
		assertReferenceCount(pdom, "SimpleStructure", 2);
		assertReferenceCount(pdom, "SimpleStructure::ssa", 2);
	}

	@Test
	public void testDeepStructure() throws Exception {
		assertType(pdom, "Structure1", ICompositeType.class);
		assertType(pdom, "Structure1::Structure2", ICompositeType.class);
		assertType(pdom, "Structure1::Structure2::Structure3", ICompositeType.class);
	}

	@Test
	public void testDeepStructureDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Structure1", 1);
		assertDeclarationCount(pdom, "Structure1::Structure2", 1);
		assertDeclarationCount(pdom, "Structure1::Structure2::Structure3", 1);
	}

	@Test
	public void testDeepStructureMemberDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Structure1::s1a", 1);
		assertDeclarationCount(pdom, "Structure1::s1b", 1);
		assertDeclarationCount(pdom, "Structure1::s1c", 1);
		assertDeclarationCount(pdom, "Structure1::Structure2::s2b", 1);
		assertDeclarationCount(pdom, "Structure1::Structure2::Structure3::s3a", 1);
	}

	@Test
	public void testDeepStructureDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Structure1", 1);
		assertDefinitionCount(pdom, "Structure1::Structure2", 1);
		assertDefinitionCount(pdom, "Structure1::Structure2::Structure3", 1);
	}

	@Test
	public void testDeepStructureMemberDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Structure1::s1a", 1);
		assertDefinitionCount(pdom, "Structure1::s1b", 1);
		assertDefinitionCount(pdom, "Structure1::s1c", 1);
		assertDefinitionCount(pdom, "Structure1::Structure2::s2b", 1);
		assertDefinitionCount(pdom, "Structure1::Structure2::Structure3::s3a", 1);
	}

	@Test
	public void testDeepStructureReferences() throws Exception {
		assertReferenceCount(pdom, "Structure1", 6);
		assertReferenceCount(pdom, "Structure1::Structure2", 4);
		assertReferenceCount(pdom, "Structure1::Structure2::Structure3", 2);
	}

	@Test
	public void testDeepStructureMemberReferences() throws Exception {
		assertReferenceCount(pdom, "Structure1::s1a", 2);
		assertReferenceCount(pdom, "Structure1::s1b", 3);
		assertReferenceCount(pdom, "Structure1::s1c", 14);
		assertReferenceCount(pdom, "Structure1::Structure2::s2b", 12);
		assertReferenceCount(pdom, "Structure1::Structure2::Structure3::s3a", 8);
	}

	@Test
	public void testUnionDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Union1", 1);
		assertDeclarationCount(pdom, "Union1::Union2", 1);
	}

	@Test
	public void testUnionDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Union1", 1);
		assertDefinitionCount(pdom, "Union1::Union2", 1);
	}

	@Test
	public void testUnionReferences() throws Exception {
		assertReferenceCount(pdom, "Union1", 4);
		assertReferenceCount(pdom, "Union1::Union2", 2);
	}

	@Test
	public void testUnionMemberDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Union1::u1a", 1);
		assertDeclarationCount(pdom, "Union1::u1d", 1);
	}

	@Test
	public void testUnionMemberDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Union1::u1a", 1);
		assertDefinitionCount(pdom, "Union1::u1d", 1);
	}

	@Test
	public void testUnionMemberReferences() throws Exception {
		assertReferenceCount(pdom, "Union1::u1a", 2);
		assertReferenceCount(pdom, "Union1::u1d", 1);
	}

	@Test
	public void testMixedDeclarations() throws Exception {
		assertDeclarationCount(pdom, "MixedS1::MixedU1", 1);
		assertDeclarationCount(pdom, "MixedS1::MixedU1::MixedS2", 1);
		assertDeclarationCount(pdom, "MixedS1::MixedU1::MixedU2", 1);
		assertDeclarationCount(pdom, "MixedS1::MixedS3", 1);
	}

	@Test
	public void testMixedDefinitions() throws Exception {
		assertDefinitionCount(pdom, "MixedS1::MixedU1", 1);
		assertDefinitionCount(pdom, "MixedS1::MixedU1::MixedS2", 1);
		assertDefinitionCount(pdom, "MixedS1::MixedU1::MixedU2", 1);
		assertDefinitionCount(pdom, "MixedS1::MixedS3", 1);
	}

	@Test
	public void testMixedReferences() throws Exception {
		assertReferenceCount(pdom, "MixedS1::MixedU1", 6);
		assertReferenceCount(pdom, "MixedS1::MixedU1::MixedS2", 2);
		assertReferenceCount(pdom, "MixedS1::MixedU1::MixedU2", 2);
		assertReferenceCount(pdom, "MixedS1::MixedS3", 2);
	}

	@Test
	public void testMixedMemberDeclarations() throws Exception {
		assertDeclarationCount(pdom, "MixedS1::MixedU1::mu1a", 1);
		assertDeclarationCount(pdom, "MixedS1::MixedU1::MixedS2::ms2a", 1);
		assertDeclarationCount(pdom, "MixedS1::MixedU1::MixedU2::mu2a", 1);
		assertDeclarationCount(pdom, "MixedS1::MixedS3::ms3a", 1);
	}

	@Test
	public void testMixedMemberDefinitions() throws Exception {
		assertDefinitionCount(pdom, "MixedS1::MixedU1::mu1a", 1);
		assertDefinitionCount(pdom, "MixedS1::MixedU1::MixedS2::ms2a", 1);
		assertDefinitionCount(pdom, "MixedS1::MixedU1::MixedU2::mu2a", 1);
		assertDefinitionCount(pdom, "MixedS1::MixedS3::ms3a", 1);
	}

	@Test
	public void testMixedMemberReferences() throws Exception {
		assertReferenceCount(pdom, "MixedS1::MixedU1::mu1a", 2);
		assertReferenceCount(pdom, "MixedS1::MixedU1::MixedS2::ms2a", 2);
		assertReferenceCount(pdom, "MixedS1::MixedU1::MixedU2::mu2a", 2);
		assertReferenceCount(pdom, "MixedS1::MixedS3::ms3a", 2);
	}

}
