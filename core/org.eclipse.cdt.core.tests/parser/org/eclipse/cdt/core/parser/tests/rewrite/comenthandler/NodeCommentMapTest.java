/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.comenthandler;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * @author Guido Zgraggen IFS
 */
public class NodeCommentMapTest extends TestCase {
	private NodeCommentMap map;
	
	@Override
	protected void setUp() throws Exception {
		map = new NodeCommentMap();
	}
	
	@Override
	protected void tearDown() throws Exception {
		map = null;
	}
	
	public void testNoComment(){
		ASTNode node = new CPPASTName();
		assertEquals(0, map.getLeadingCommentsForNode(node).size());
		assertEquals(0, map.getTrailingCommentsForNode(node).size());
		assertEquals(0, map.getFreestandingCommentsForNode(node).size());
	}
	
	public void testOneComment(){
		ASTNode node = new CPPASTName();
		IASTComment comm1 = new Comment();
		IASTComment comm2 = new Comment();
		IASTComment comm3 = new Comment();
		
		map.addLeadingCommentToNode(node, comm1);
		map.addTrailingCommentToNode(node, comm2);
		map.addFreestandingCommentToNode(node, comm3);
		
		assertEquals(1, map.getLeadingCommentsForNode(node).size());
		assertEquals(1, map.getTrailingCommentsForNode(node).size());
		assertEquals(1, map.getFreestandingCommentsForNode(node).size());

		assertEquals(comm1, map.getLeadingCommentsForNode(node).get(0));
		assertEquals(comm2, map.getTrailingCommentsForNode(node).get(0));
		assertEquals(comm3, map.getFreestandingCommentsForNode(node).get(0));
	}
	
	public void testTwoComment(){
		ASTNode node = new CPPASTName();
		IASTComment com1 = new Comment();
		IASTComment com2 = new Comment();
		
		map.addLeadingCommentToNode(node, com1);
		map.addLeadingCommentToNode(node, com2);
		map.addTrailingCommentToNode(node, com1);
		map.addTrailingCommentToNode(node, com2);
		map.addFreestandingCommentToNode(node, com1);
		map.addFreestandingCommentToNode(node, com2);
		
		assertEquals(2, map.getLeadingCommentsForNode(node).size());
		assertEquals(2, map.getTrailingCommentsForNode(node).size());
		assertEquals(2, map.getFreestandingCommentsForNode(node).size());

		assertEquals(com1, map.getLeadingCommentsForNode(node).get(0));
		assertEquals(com2, map.getLeadingCommentsForNode(node).get(1));
		assertEquals(com1, map.getTrailingCommentsForNode(node).get(0));
		assertEquals(com2, map.getTrailingCommentsForNode(node).get(1));
		assertEquals(com1, map.getFreestandingCommentsForNode(node).get(0));
		assertEquals(com2, map.getFreestandingCommentsForNode(node).get(1));
	}

	public void testCommentOnDifferentNodes(){
		ASTNode node1 = new CPPASTName();
		ASTNode node2 = new CPPASTName();
		IASTComment com1 = new Comment();
		IASTComment com2 = new Comment();
		IASTComment com3 = new Comment();

		map.addLeadingCommentToNode(node1, com1);
		map.addLeadingCommentToNode(node2, com2);
		map.addLeadingCommentToNode(node1, com3);
		
		map.addTrailingCommentToNode(node1, com1);
		map.addTrailingCommentToNode(node2, com2);
		map.addTrailingCommentToNode(node1, com3);
		
		map.addFreestandingCommentToNode(node1, com1);
		map.addFreestandingCommentToNode(node2, com2);
		map.addFreestandingCommentToNode(node1, com3);

		assertEquals(2, map.getLeadingCommentsForNode(node1).size());
		assertEquals(1, map.getLeadingCommentsForNode(node2).size());
		assertEquals(2, map.getTrailingCommentsForNode(node1).size());
		assertEquals(1, map.getTrailingCommentsForNode(node2).size());
		assertEquals(2, map.getFreestandingCommentsForNode(node1).size());
		assertEquals(1, map.getFreestandingCommentsForNode(node2).size());
		
		assertEquals(com1, map.getLeadingCommentsForNode(node1).get(0));
		assertEquals(com2, map.getLeadingCommentsForNode(node2).get(0));
		assertEquals(com3, map.getLeadingCommentsForNode(node1).get(1));
		
		assertEquals(com1, map.getTrailingCommentsForNode(node1).get(0));
		assertEquals(com2, map.getTrailingCommentsForNode(node2).get(0));
		assertEquals(com3, map.getTrailingCommentsForNode(node1).get(1));
		
		assertEquals(com1, map.getFreestandingCommentsForNode(node1).get(0));
		assertEquals(com2, map.getFreestandingCommentsForNode(node2).get(0));
		assertEquals(com3, map.getFreestandingCommentsForNode(node1).get(1));
	}
	
	//=== InternalComment class for testing 
	private class Comment extends ASTNode implements IASTComment {
		private char[] comment;
		
		public char[] getComment() {
			return comment;
		}

		public void setComment(char[] comment) {
			this.comment = comment;
		}

		// not used
		public boolean isBlockComment() {
			return false;
		}

		public IASTNode copy() {
			return null;
		}

		public IASTNode copy(CopyStyle style) {
			return null;
		}
	}
}
