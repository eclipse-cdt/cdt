/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * @since 6.0
 */
public interface ICPPASTAttributeList extends ICPPASTAttributeSpecifier, IASTAttributeList {
}
