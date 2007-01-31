/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @author Doug Schaefer
 */
public interface IField extends IVariable {

    public static final IField[] EMPTY_FIELD_ARRAY = new IField[0];

    /**
     * Returns the composite type that owns the field.
     * @throws DOMException 
     * @since 4.0
     */
	ICompositeType getCompositeTypeOwner() throws DOMException;

}
