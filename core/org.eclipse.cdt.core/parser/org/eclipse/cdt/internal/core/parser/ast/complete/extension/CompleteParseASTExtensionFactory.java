/*******************************************************************************
 * Copyright (c) 2000 - 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete.extension;

import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.extension.IASTExpressionExtension;
import org.eclipse.cdt.core.parser.ast.extension.IASTExtensionFactory;

/**
 * @author jcamelon
 */
public class CompleteParseASTExtensionFactory implements IASTExtensionFactory {

	/**
	 * 
	 */
	public CompleteParseASTExtensionFactory() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.extension.IASTExtensionFactory#createExpressionExtension(org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	public IASTExpressionExtension createExpressionExtension()
		throws ASTNotImplementedException {
		throw new ASTNotImplementedException();
	}

}
