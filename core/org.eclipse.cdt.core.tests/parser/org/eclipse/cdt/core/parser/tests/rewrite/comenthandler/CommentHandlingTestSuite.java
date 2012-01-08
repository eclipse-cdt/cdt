/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.tests.rewrite.RewriteTester;

/**
 * @author Guido Zgraggen IFS
 */
public class CommentHandlingTestSuite extends TestSuite {
	
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(CommentHandlingTestSuite.class.getName());
		suite.addTest(RewriteTester.suite("CommentTests",
				"resources/rewrite/CommentHandlingTestSource.rts")); //$NON-NLS-1$ 
		suite.addTestSuite(NodeCommentMapTest.class);
		return suite;
	}
}
