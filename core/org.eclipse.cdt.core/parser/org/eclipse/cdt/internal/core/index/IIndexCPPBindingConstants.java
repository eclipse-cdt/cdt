/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

/**
 * Constants used by IIndexFragment implementations for identifying persisted binding types.
 */
public interface IIndexCPPBindingConstants {
	int CPPENUMERATOR = IIndexBindingConstants.ENUMERATOR;

	int CPPVARIABLE = IIndexBindingConstants.LAST_CONSTANT + 1;
	int CPPFUNCTION = IIndexBindingConstants.LAST_CONSTANT + 2;
	int CPPCLASSTYPE = IIndexBindingConstants.LAST_CONSTANT + 3;
	int CPPFIELD = IIndexBindingConstants.LAST_CONSTANT + 4;
	int CPPMETHOD = IIndexBindingConstants.LAST_CONSTANT + 5;
	int CPPNAMESPACE = IIndexBindingConstants.LAST_CONSTANT + 6;
	int CPPNAMESPACEALIAS = IIndexBindingConstants.LAST_CONSTANT + 7;
	int CPPPARAMETER = IIndexBindingConstants.LAST_CONSTANT + 9;
	int CPPENUMERATION = IIndexBindingConstants.LAST_CONSTANT + 10;
	int CPPTYPEDEF = IIndexBindingConstants.LAST_CONSTANT + 12;
	int CPP_CONSTRUCTOR= IIndexBindingConstants.LAST_CONSTANT + 14;
	int CPP_FUNCTION_TEMPLATE= IIndexBindingConstants.LAST_CONSTANT + 16;
	int CPP_METHOD_TEMPLATE= IIndexBindingConstants.LAST_CONSTANT + 17;
	int CPP_CONSTRUCTOR_TEMPLATE= IIndexBindingConstants.LAST_CONSTANT + 18;
	int CPP_CLASS_TEMPLATE= IIndexBindingConstants.LAST_CONSTANT + 19;
	int CPP_CLASS_TEMPLATE_PARTIAL_SPEC= IIndexBindingConstants.LAST_CONSTANT + 20;
	int CPP_FUNCTION_INSTANCE= IIndexBindingConstants.LAST_CONSTANT + 21;
	int CPP_METHOD_INSTANCE= IIndexBindingConstants.LAST_CONSTANT + 22;
	int CPP_CONSTRUCTOR_INSTANCE= IIndexBindingConstants.LAST_CONSTANT + 23;
	int CPP_CLASS_INSTANCE= IIndexBindingConstants.LAST_CONSTANT + 25;
	int CPP_DEFERRED_CLASS_INSTANCE= IIndexBindingConstants.LAST_CONSTANT + 26;
	int CPP_PARAMETER_SPECIALIZATION= IIndexBindingConstants.LAST_CONSTANT + 27;
	int CPP_FIELD_SPECIALIZATION= IIndexBindingConstants.LAST_CONSTANT + 28;
	int CPP_FUNCTION_SPECIALIZATION= IIndexBindingConstants.LAST_CONSTANT + 29;
	int CPP_METHOD_SPECIALIZATION= IIndexBindingConstants.LAST_CONSTANT + 30;
	int CPP_CONSTRUCTOR_SPECIALIZATION= IIndexBindingConstants.LAST_CONSTANT + 31;
	int CPP_CLASS_SPECIALIZATION= IIndexBindingConstants.LAST_CONSTANT + 32;
	int CPP_FUNCTION_TEMPLATE_SPECIALIZATION= IIndexBindingConstants.LAST_CONSTANT + 33;
	int CPP_METHOD_TEMPLATE_SPECIALIZATION= IIndexBindingConstants.LAST_CONSTANT + 34;
	int CPP_CONSTRUCTOR_TEMPLATE_SPECIALIZATION= IIndexBindingConstants.LAST_CONSTANT + 35;
	int CPP_CLASS_TEMPLATE_SPECIALIZATION= IIndexBindingConstants.LAST_CONSTANT + 36;
	int CPP_TYPEDEF_SPECIALIZATION= IIndexBindingConstants.LAST_CONSTANT + 37;
	int CPP_TEMPLATE_TYPE_PARAMETER= IIndexBindingConstants.LAST_CONSTANT + 38;
	int CPP_USING_DECLARATION= IIndexBindingConstants.LAST_CONSTANT + 41;
	int CPP_UNKNOWN_CLASS_TYPE= IIndexBindingConstants.LAST_CONSTANT + 42;
	int CPP_UNKNOWN_CLASS_INSTANCE= IIndexBindingConstants.LAST_CONSTANT + 43;
	int CPP_TEMPLATE_NON_TYPE_PARAMETER= IIndexBindingConstants.LAST_CONSTANT + 44;
	int CPP_FRIEND_DECLARATION = IIndexBindingConstants.LAST_CONSTANT + 45;
	int CPP_TEMPLATE_TEMPLATE_PARAMETER= IIndexBindingConstants.LAST_CONSTANT + 46;
	int CPP_CLASS_TEMPLATE_PARTIAL_SPEC_SPEC = IIndexBindingConstants.LAST_CONSTANT + 47;
	int CPP_UNKNOWN_BINDING = IIndexBindingConstants.LAST_CONSTANT + 48;
	int CPP_USING_DECLARATION_SPECIALIZATION= IIndexBindingConstants.LAST_CONSTANT + 49;
}
