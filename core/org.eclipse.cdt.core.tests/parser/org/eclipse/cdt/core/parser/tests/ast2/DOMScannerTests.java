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
      IASTTranslationUnit tu = parse("int x;", ParserLanguage.C); //$NON-NLS-1$
      IASTNodeLocation[] nodeLocations = tu.getDeclarations()[0]
            .getNodeLocations();
      assertNotNull(nodeLocations);
      assertEquals(nodeLocations.length, 1);
      assertTrue(nodeLocations[0] instanceof IASTFileLocation);
      IASTFileLocation fileLocation = ((IASTFileLocation) nodeLocations[0]);
      assertEquals(fileLocation.getFileName(), "<text>"); //$NON-NLS-1$
      assertEquals(fileLocation.getNodeOffset(), 0);
      assertEquals(fileLocation.getNodeLength(), 6);
   }

}