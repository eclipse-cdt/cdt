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
package org.eclipse.cdt.core.dom.ast;


/**
 * @author Doug Schaefer
 */
public interface ICompositeType extends IBinding, IType {

	public static final int k_struct = IASTCompositeTypeSpecifier.k_struct;
	public static final int k_union = IASTCompositeTypeSpecifier.k_union;
	/**
	 *  what kind of composite type is this?
	 * @return
	 */
	public int getKey() throws DOMException;
	
	/**
	 * Returns the fields for this type.
	 * 
	 * @return List of IField
	 */
	public IField[] getFields() throws DOMException;
	
	/**
	 * returns the field that matches name,
	 * or null if there is no such field.
	 * 
	 * @param name
	 * @return
	 */
	public IField findField( String name ) throws DOMException;
	
	/**
	 * get the IScope object that is associated with this composite type
	 * @return
	 * @throws DOMException
	 */
	public IScope getCompositeScope() throws DOMException;
}
