/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.ui.compare.CStructureCreator;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import junit.framework.TestSuite;

/**
 * Tests for the CStructureCreator.
 *
 * @since 5.0
 */
public class CStructureCreatorTest extends BaseUITestCase {

	public static TestSuite suite() {
		return suite(CStructureCreatorTest.class, "_");
	}

	private ICProject fCProject;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fCProject = EditorTestHelper.createCProject("CStructureCreatorTest", "resources/compare", false);
	}

	@Override
	protected void tearDown() throws Exception {
		if (fCProject != null) {
			CProjectHelper.delete(fCProject);
		}
		super.tearDown();
	}

	public void testStructureCreatorNodeTypes() throws Exception {
		IFile file = ResourceTestHelper.findFile("/CStructureCreatorTest/src/CompareTest.cpp");
		assertNotNull(file);
		CStructureCreator creator = new CStructureCreator();
		IStructureComparator node = creator.getStructure(new ResourceNode(file));
		assertNotNull(node);
		Object[] children = node.getChildren();
		// one node below root == translation unit
		assertEquals(1, children.length);
		DocumentRangeNode tuNode = (DocumentRangeNode) children[0];

		// tu children
		children = tuNode.getChildren();
		assertEquals(10, children.length);
		DocumentRangeNode child = (DocumentRangeNode) children[0];
		assertEquals(ICElement.C_INCLUDE, child.getTypeCode());
		child = (DocumentRangeNode) children[1];
		assertEquals(ICElement.C_MACRO, child.getTypeCode());
		DocumentRangeNode namespace = (DocumentRangeNode) children[2];
		assertEquals(ICElement.C_NAMESPACE, namespace.getTypeCode());
		child = (DocumentRangeNode) children[3];
		assertEquals(ICElement.C_VARIABLE, child.getTypeCode());
		child = (DocumentRangeNode) children[4];
		assertEquals(ICElement.C_FUNCTION, child.getTypeCode());
		DocumentRangeNode struct = (DocumentRangeNode) children[5];
		assertEquals(ICElement.C_STRUCT, struct.getTypeCode());
		child = (DocumentRangeNode) children[6];
		assertEquals(ICElement.C_VARIABLE, child.getTypeCode());
		child = (DocumentRangeNode) children[7];
		assertEquals(ICElement.C_VARIABLE, child.getTypeCode());
		child = (DocumentRangeNode) children[8];
		assertEquals(ICElement.C_USING, child.getTypeCode());
		child = (DocumentRangeNode) children[9];
		assertEquals(ICElement.C_FUNCTION, child.getTypeCode());

		// namespace children
		children = namespace.getChildren();
		assertEquals(22, children.length);
		DocumentRangeNode clazz = (DocumentRangeNode) children[0];
		assertEquals(ICElement.C_CLASS, clazz.getTypeCode());
		DocumentRangeNode enum1 = (DocumentRangeNode) children[1];
		assertEquals(ICElement.C_ENUMERATION, enum1.getTypeCode());
		child = (DocumentRangeNode) children[2];
		assertEquals(ICElement.C_ENUMERATION, child.getTypeCode());
		child = (DocumentRangeNode) children[3];
		assertEquals(ICElement.C_VARIABLE, child.getTypeCode());
		child = (DocumentRangeNode) children[4];
		assertEquals(ICElement.C_VARIABLE, child.getTypeCode());
		child = (DocumentRangeNode) children[5];
		assertEquals(ICElement.C_VARIABLE, child.getTypeCode());
		child = (DocumentRangeNode) children[6];
		assertEquals(ICElement.C_VARIABLE_DECLARATION, child.getTypeCode());
		child = (DocumentRangeNode) children[7];
		assertEquals(ICElement.C_VARIABLE, child.getTypeCode());
		child = (DocumentRangeNode) children[8];
		assertEquals(ICElement.C_FUNCTION_DECLARATION, child.getTypeCode());
		child = (DocumentRangeNode) children[9];
		assertEquals(ICElement.C_FUNCTION_DECLARATION, child.getTypeCode());
		child = (DocumentRangeNode) children[10];
		assertEquals(ICElement.C_FUNCTION, child.getTypeCode());
		child = (DocumentRangeNode) children[11];
		assertEquals(ICElement.C_STRUCT, child.getTypeCode());
		child = (DocumentRangeNode) children[12];
		assertEquals(ICElement.C_TYPEDEF, child.getTypeCode());
		child = (DocumentRangeNode) children[13];
		assertEquals(ICElement.C_STRUCT, child.getTypeCode());
		child = (DocumentRangeNode) children[14];
		assertEquals(ICElement.C_TYPEDEF, child.getTypeCode());
		DocumentRangeNode union = (DocumentRangeNode) children[15];
		assertEquals(ICElement.C_UNION, union.getTypeCode());
		child = (DocumentRangeNode) children[16];
		assertEquals(ICElement.C_TEMPLATE_FUNCTION_DECLARATION, child.getTypeCode());
		child = (DocumentRangeNode) children[17];
		assertEquals(ICElement.C_TEMPLATE_FUNCTION, child.getTypeCode());
		DocumentRangeNode clazz2 = (DocumentRangeNode) children[18];
		assertEquals(ICElement.C_CLASS, clazz2.getTypeCode());
		child = (DocumentRangeNode) children[19];
		assertEquals(ICElement.C_TEMPLATE_CLASS, child.getTypeCode());
		child = (DocumentRangeNode) children[20];
		assertEquals(ICElement.C_TEMPLATE_STRUCT, child.getTypeCode());
		child = (DocumentRangeNode) children[21];
		assertEquals(ICElement.C_TEMPLATE_VARIABLE, child.getTypeCode());

		// class children
		children = clazz.getChildren();
		assertEquals(3, children.length);
		child = (DocumentRangeNode) children[0];
		assertEquals(ICElement.C_FIELD, child.getTypeCode());
		child = (DocumentRangeNode) children[1];
		assertEquals(ICElement.C_METHOD, child.getTypeCode());
		namespace = (DocumentRangeNode) children[2];
		assertEquals(ICElement.C_NAMESPACE, namespace.getTypeCode());

		// enum children
		children = enum1.getChildren();
		assertEquals(3, children.length);
		child = (DocumentRangeNode) children[0];
		assertEquals(ICElement.C_ENUMERATOR, child.getTypeCode());
		child = (DocumentRangeNode) children[1];
		assertEquals(ICElement.C_ENUMERATOR, child.getTypeCode());
		child = (DocumentRangeNode) children[2];
		assertEquals(ICElement.C_ENUMERATOR, child.getTypeCode());

		// union children
		children = union.getChildren();
		assertEquals(1, children.length);
		child = (DocumentRangeNode) children[0];
		assertEquals(ICElement.C_FIELD, child.getTypeCode());

		// enclosing class children
		children = clazz2.getChildren();
		assertEquals(1, children.length);
		child = (DocumentRangeNode) children[0];
		assertEquals(ICElement.C_TEMPLATE_METHOD_DECLARATION, child.getTypeCode());

		// nested namespace children
		children = namespace.getChildren();
		assertEquals(2, children.length);
		child = (DocumentRangeNode) children[0];
		assertEquals(ICElement.C_CLASS, child.getTypeCode());
		clazz = (DocumentRangeNode) children[1];
		assertEquals(ICElement.C_CLASS, child.getTypeCode());
		// nested class children
		children = clazz.getChildren();
		assertEquals(3, children.length);
		child = (DocumentRangeNode) children[0];
		assertEquals(ICElement.C_FIELD, child.getTypeCode());
		child = (DocumentRangeNode) children[1];
		assertEquals(ICElement.C_METHOD, child.getTypeCode());
		child = (DocumentRangeNode) children[2];
		assertEquals(ICElement.C_METHOD_DECLARATION, child.getTypeCode());
	}

	public void testStructureCreatorNodeSizes() throws Exception {
		IFile file = ResourceTestHelper.findFile("/CStructureCreatorTest/src/CompareTest.cpp");
		assertNotNull(file);
		CStructureCreator creator = new CStructureCreator();
		IStructureComparator node = creator.getStructure(new ResourceNode(file));
		assertNotNull(node);
		Object[] children = node.getChildren();
		// one node below root == translation unit
		assertEquals(1, children.length);
		DocumentRangeNode tuNode = (DocumentRangeNode) children[0];
		IDocument document = tuNode.getDocument();
		Position range = tuNode.getRange();
		assertEqualPositions(new Position(0, document.getLength()), range);

		verifyStructure(tuNode, CoreModel.getDefault().create(file));
	}

	private void assertEqualPositions(Position expected, Position actual) {
		assertEquals(expected.getOffset(), actual.getOffset());
		assertEquals(expected.getLength(), actual.getLength());
	}

	private void verifyStructure(DocumentRangeNode cNode, ICElement cElement)
			throws CModelException, BadLocationException {
		// verify same type
		assertEquals(cElement.getElementType(), cNode.getTypeCode());

		if (cNode.getTypeCode() != ICElement.C_UNIT && cNode instanceof ITypedElement) {
			Position nodeRange = cNode.getRange();
			IDocument doc = cNode.getDocument();
			ISourceReference sourceRef = (ISourceReference) cElement;
			ISourceRange sourceRange = sourceRef.getSourceRange();
			// verify start and endline of elements match
			assertEquals(sourceRange.getStartLine(), doc.getLineOfOffset(nodeRange.getOffset()) + 1);
			assertEquals(sourceRange.getEndLine(),
					doc.getLineOfOffset(nodeRange.getOffset() + nodeRange.getLength() - 1) + 1);

			assertEqualPositions(new Position(sourceRange.getStartPos(), sourceRange.getLength()), nodeRange);

			if (cElement.getElementName().length() > 0) {
				assertEquals(cElement.getElementName(), ((ITypedElement) cNode).getName());
			}
		}
		Object[] nodeChildren = cNode.getChildren();
		// merge in extern "C" children to match cmodel hierarchy
		for (int i = 0; i < nodeChildren.length; i++) {
			DocumentRangeNode childNode = (DocumentRangeNode) nodeChildren[i];
			//			if (childNode.getTypeCode() == ICElement.C_STORAGE_EXTERN) {
			//				Object[] linkageSpecChildren= childNode.getChildren();
			//				Object[] newArray= new Object[nodeChildren.length - 1 + linkageSpecChildren.length];
			//				System.arraycopy(nodeChildren, 0, newArray, 0, i);
			//				System.arraycopy(linkageSpecChildren, 0, newArray, i, linkageSpecChildren.length);
			//				System.arraycopy(nodeChildren, i+1, newArray, i + linkageSpecChildren.length, nodeChildren.length - i - 1);
			//				nodeChildren= newArray;
			//			}
		}
		ICElement[] cElementChildren;
		if (cElement instanceof IParent) {
			cElementChildren = ((IParent) cElement).getChildren();
		} else {
			cElementChildren = new ICElement[0];
		}
		// verify same number of children
		assertEquals(cElementChildren.length, nodeChildren.length);
		for (int i = 0; i < cElementChildren.length; i++) {
			verifyStructure((DocumentRangeNode) nodeChildren[i], cElementChildren[i]);
		}
	}
}
