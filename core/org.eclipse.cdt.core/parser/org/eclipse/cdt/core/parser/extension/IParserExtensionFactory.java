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
package org.eclipse.cdt.core.parser.extension;

import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.extension.IASTExtensionFactory;


/**
 * @author jcamelon
 */
public interface IParserExtensionFactory {

	public IScannerExtension createScannerExtension() throws ParserFactoryError;
	public IASTExtensionFactory createASTExtensionFactory(ParserMode mode) throws ParserFactoryError;
	public IParserExtension createParserExtension() throws ParserFactoryError;
}
