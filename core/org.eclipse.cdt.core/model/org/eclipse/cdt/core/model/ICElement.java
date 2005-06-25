/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

 
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

/**
 * Common protocol for all elements provided by the C model.
 */
public interface ICElement extends IAdaptable {

	/**
	 * IResource from 10-20
	 */ 

	/**
	 * Constant representing a C Root workspace (IWorkspaceRoot object).
	 * A C element with this type can be safely cast to <code>ICModel</code>.
	 */
	static final int C_MODEL = 10;

	/**
	 * Constant representing a C project(IProject object).
	 * A C element with this type can be safely cast to <code>ICProject</code>.
	 */
	static final int C_PROJECT = 11;

	/**
	 * Constant representing a folder(ICContainer object).
	 * A C element with this type can be safely cast to <code>ICContainer</code>.
	 */
	static final int C_CCONTAINER = 12;

	static final int C_BINARY = 14;
	
	static final int C_ARCHIVE = 18;
	/**
	 * Virtual container serving as a place holder.
	 */
	static final int C_VCONTAINER = 30;

	/**
	 * Constant representing a C/C++ children of a Translation Unit
	 */
	static final int C_UNIT = 60;

	/**
	 * Namespace.
	 */
	static final int C_NAMESPACE = 61;

	/**
	 * Using.
	 */
	static final int C_USING = 62;

	/**
	 * Enumeration.
	 */
	static final int C_ENUMERATION = 63;

	/**
	 * Declaration of a class without the definition.
	 * class C;
	 */
	static final int C_CLASS_DECLARATION = 64;

	/**
	 * Constant representing a class structure.
	 */
	static final int C_CLASS = 65;

	/**
	 * Declaration of a structure without the definition.
	 * struct C;
	 */
	static final int C_STRUCT_DECLARATION = 66;

	/**
	 * Constant representing a struct structure.
	 */
	static final int C_STRUCT = 67;

	/**
	 * Declaration of a union without the definition.
	 * struct C;
	 */
	static final int C_UNION_DECLARATION = 68;
	
	/**
	 * Constant representing a union structure.
	 */
	static final int C_UNION = 69;

	/**
	 * A method definition part of a structure(class, struct, union).
	 */
	static final int C_METHOD = 70;

	/**
	 * A method declaration part of a structure(class, struct, union).
	 */
	static final int C_METHOD_DECLARATION = 71;

	/**
	 * A Field definition part of a structure(class, struct, union).
	 */
	static final int C_FIELD = 72;

	/**
	 * a C/C++ function prototype.
	 */
	static final int C_FUNCTION_DECLARATION = 73;

	/**
	 * a C/C++ function definition.
	 */
	static final int C_FUNCTION = 74;

	/**
	 * Preprocessor #include directive.
	 */
	static final int C_INCLUDE = 75;

	/**
	 * Global variable.
	 */
	static final int C_VARIABLE = 76;

	/**
	 * variable Declaration.
	 */
	static final int C_VARIABLE_DECLARATION = 77;

	/**
	 * Local Variable.
	 */
	static final int C_VARIABLE_LOCAL = 78;

	/**
	 * A preprocessor macro.
	 */
	static final int C_MACRO = 79;

	/**
	 * a Typedef.
	 */
	static final int C_TYPEDEF = 80;
	
	/**
	 * Enumerator.
	 */
	static final int C_ENUMERATOR = 81;

	/**
	 * C++ template class declaration without a definiton.
	 */
	static final int C_TEMPLATE_CLASS_DECLARATION = 82;

	/**
	 * C++ template class with definition.
	 */
	static final int C_TEMPLATE_CLASS = 83;

	/**
	 * C++ template struct.
	 */
	static final int C_TEMPLATE_STRUCT_DECLARATION = 84;

	/**
	 * C++ template struct.
	 */
	static final int C_TEMPLATE_STRUCT = 85;

	/**
	 * C++ template union.
	 */
	static final int C_TEMPLATE_UNION_DECLARATION = 86;

	/**
	 * C++ template union.
	 */
	static final int C_TEMPLATE_UNION = 87;

	/**
	 * C++ template function declaration.
	 */
	static final int C_TEMPLATE_FUNCTION_DECLARATION = 88;

	/**
	 * C++ template function.
	 */
	static final int C_TEMPLATE_FUNCTION = 89;

	/**
	 * C++ template method.
	 */
	static final int C_TEMPLATE_METHOD_DECLARATION = 90;

	/**
	 * C++ template method.
	 */
	static final int C_TEMPLATE_METHOD = 91;

	/**
	 * C++ template variable.
	 */
	static final int C_TEMPLATE_VARIABLE = 92;

	/**
	 * An unknown ICElement.  Mainly used to determine what elements are not yet implemented.
	 * i.e. the new DOM Parser supports open declaration on labels, while the old parser did not
	 */
	static final int C_UNKNOWN_DECLARATION = 93;

	/**
	 * Modifier indicating a class constructor
	 */
	static final int C_CLASS_CTOR = 0x100;
	
	/**
	 * Modifier indicating a class destructor
	 */
	static final int C_CLASS_DTOR = 0x200;
		
	/**
	 * Modifier indicating a static storage attribute
	 */
	static final int C_STORAGE_STATIC = 0x400;
		
	/**
	 * Modifier indicating an extern storage attribute
	 */
	static final int C_STORAGE_EXTERN = 0x800;

	/**
	 * Modifier indicating a private class
	 */
	static final int CPP_PRIVATE = 0x1000;

	/**
	 * Modifier indicating a public class
	 */

	static final int CPP_PUBLIC = 0x2000;

	/**
	 * Modifier indicating a protected class
	 */
	static final int CPP_PROTECTED = 0x4000;
	/**
	 * Modifier indicating a friend class
	 */
	static final int CPP_FRIEND = 0x8000;

	/**
	 * Returns whether this C element exists in the model.
	 *
	 * @return <code>true</code> if this element exists in the C model
	 */
	boolean exists();

	/**
	 * Returns the first ancestor of this C element that has the given type.
	 * Returns <code>null</code> if no such an ancestor can be found.
	 * This is a handle-only method.
	 *
	 * @param ancestorType the given type
	 * @return the first ancestor of this C element that has the given type, null if no such an ancestor can be found
	 * @since 2.0
	 */
	ICElement getAncestor(int ancestorType);

	/**
	 * Returns the name of this element.
	 *
	 * @return the element name
	 */
	String getElementName();

	/**
	 * Returns this element's kind encoded as an integer.
	 * This is a handle-only method.
	 *
	 * @return the kind of element; one of the constants declared in
	 *   <code>ICElement</code>
	 * @see ICElement
	 */
	int getElementType();

	/**
	 * Returns the C model.
	 *
	 * @return the C model
	 */
	ICModel getCModel();

	/**
	 * Returns the C project this element is contained in,
	 * or <code>null</code> if this element is not contained in any C project
	 *
	 * @return the containing C project, or <code>null</code> if this element is
	 *   not contained in a C project
	 */
	ICProject getCProject();

	/**
	 * Returns the element directly containing this element,
	 * or <code>null</code> if this element has no parent.
	 *
	 * @return the parent element, or <code>null</code> if this element has no parent
	 */
	ICElement getParent();

	/**
	 * Returns the path to the innermost resource enclosing this element. 
	 * If this element is not included in an external archive, 
	 * the path returned is the full, absolute path to the underlying resource, 
	 * relative to the workbench. 
	 * If this element is included in an external archive, 
	 * the path returned is the absolute path to the archive in the file system.
	 * This is a handle-only method.
	 * 
	 */
	IPath getPath();

	/**
	 * Returns the underlying resource that contains
	 * this element, or <code>null</code> if this element is not contained
	 * in a resource.
	 *
	 * @return the underlying resource, or <code>null</code> if none
	 * @exception CModelException if this element does not exist or if an
	 *		exception occurs while accessing its underlying resource
	 */
	IResource getUnderlyingResource();

	/**
	 * Returns the Corresponding resource for
	 * this element, or <code>null</code> if this element does not have
	 * a corresponding resource.
	 *
	 * @return the corresponding resource, or <code>null</code> if none
	 * @exception CModelException if this element does not exist or if an
	 *		exception occurs while accessing its resource
	 */
	IResource getResource() ;
	/**
	 * Returns whether this C element is read-only. An element is read-only
	 * if its structure cannot be modified by the C model. 
	 *
	 * @return <code>true</code> if this element is read-only
	 */
	boolean isReadOnly();

	/**
	 * Returns whether the structure of this element is known. For example, for a
	 * translation unit that could not be parsed, <code>false</code> is returned.
	 * If the structure of an element is unknown, navigations will return reasonable
	 * defaults. For example, <code>getChildren</code> will return an empty collection.
	 * <p>
	 * Note: This does not imply anything about consistency with the
	 * underlying resource/buffer contents.
	 * </p>
	 *
	 * @return <code>true</code> if the structure of this element is known
	 * @exception CModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	boolean isStructureKnown() throws CModelException;
	
	/**
	 * Accept a visitor and walk the ICElement tree with it.
	 * 
	 * @param visitor
	 * @throws CModelException
	 */
	void accept(ICElementVisitor visitor) throws CoreException;
}
