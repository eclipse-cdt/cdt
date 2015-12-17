/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTAttributeList;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPASTAttributeSpecifier;

/**
 * Represents a C++11 (ISO/IEC 14882:2011 7.6.1 [dcl.attr.grammar]) attribute specifier
 * of the form [[ attribute-list ]].
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.12
 */
public interface ICPPASTAttributeList extends ICPPASTAttributeSpecifier, IASTAttributeList {
}
