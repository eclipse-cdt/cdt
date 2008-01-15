package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;

/**
 * Internal interface for exposing internal methods to ClassTypeMixin
 */
interface ICPPInternalClassTypeMixinHost extends ICPPInternalClassType, ICPPClassType {
	/**
	 * @return the composite type specifier for the class type
	 */
	 ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier();
	 
	 /**
	  * Ensures the ICPPInternalBinding definition is set, if this is possible.
	  * @see ICPPInternalBinding#getDefinition()
	  */
	 void checkForDefinition();
}
