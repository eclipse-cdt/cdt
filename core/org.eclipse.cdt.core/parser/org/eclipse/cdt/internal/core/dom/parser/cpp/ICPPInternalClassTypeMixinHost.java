/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;

/**
 * Internal interface for exposing internal methods to {@link ClassTypeHelper}
 */
interface ICPPInternalClassTypeMixinHost extends ICPPClassType, ICPPInternalBinding {
	/**
	 * @return the composite type specifier for the class type
	 */
	 ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier();
	 
	 /**
	  * {@inheritDoc}
	  */
	 @Override
	ICPPClassScope getCompositeScope();

	 /**
	  * Ensures the ICPPInternalBinding definition is set, if this is possible.
	  * @see ICPPInternalBinding#getDefinition()
	  */
	 void checkForDefinition();
}
