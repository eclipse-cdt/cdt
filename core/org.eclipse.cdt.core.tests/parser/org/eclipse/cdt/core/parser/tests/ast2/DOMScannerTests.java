/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author jcamelon
 */
public class DOMScannerTests extends AST2BaseTest {

   public void testSimpleLocation() throws ParserException {
      for( ParserLanguage p = ParserLanguage.C; p != null;  p = ( p == ParserLanguage.C ) ? ParserLanguage.CPP : null )
      {
	      IASTTranslationUnit tu = parse("int x;", p); //$NON-NLS-1$
	      IASTDeclaration declaration = tu.getDeclarations()[0];
	      IASTNodeLocation[] nodeLocations = declaration
	            .getNodeLocations();
	      assertNotNull(nodeLocations);
	      assertEquals(nodeLocations.length, 1);
	      assertTrue(nodeLocations[0] instanceof IASTFileLocation);
	      IASTFileLocation fileLocation = ((IASTFileLocation) nodeLocations[0]);
	      assertEquals(fileLocation.getFileName(), "<text>"); //$NON-NLS-1$
	      assertEquals(fileLocation.getNodeOffset(), 0);
	      assertEquals(fileLocation.getNodeLength(), 6);
	      IASTNodeLocation [] tuLocations = tu.getNodeLocations();
	      assertEquals( tuLocations.length, nodeLocations.length );
	      assertEquals(fileLocation.getFileName(), ((IASTFileLocation)tuLocations[0]).getFileName()); //$NON-NLS-1$
	      assertEquals(fileLocation.getNodeOffset(), tuLocations[0].getNodeOffset());
	      assertEquals(fileLocation.getNodeLength(), tuLocations[0].getNodeLength());
      }
   }

}