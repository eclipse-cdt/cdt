/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.parser.IScannerInfo;

/**
 * This interface represents a parser configuration as specified by the client
 * to the parser service.
 * 
 * @author jcamelon
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @deprecated Used only by other deprecated interfaces
 */
@Deprecated
public interface IParserConfiguration {
	/**
	 * @return IScannerInfo representing the build information required to parse.
	 */
	public IScannerInfo getScannerInfo();

	/**
	 * @return String representing dialect name for the language
	 */
	public String getParserDialect();
}
