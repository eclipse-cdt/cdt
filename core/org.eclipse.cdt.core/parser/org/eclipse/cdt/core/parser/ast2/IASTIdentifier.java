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
package org.eclipse.cdt.core.parser.ast2;

/**
 * This class represents a name used to introduce declarations and to
 * refer back to them from references.
 * 
 * This is an opaque type to allow for various representation techniques
 * for the actual name.
 *
 * The toString method provided by any implementation of this interface
 * must return a String representation of the underlying identifier.
 *
 * The equals method provided by any implementation of this interface
 * must be able to compare this identifier to a String representation.
 *  
 * @author Doug Schaefer
 */
public interface IASTIdentifier extends IASTNode {
}
