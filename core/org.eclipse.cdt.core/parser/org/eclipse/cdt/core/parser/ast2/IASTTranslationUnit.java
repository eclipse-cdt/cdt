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
 * The translation unit represents a compilable unit of source. For all
 * languages, a translation unit is expected to be the global scope.
 *
 * @author Doug Schaefer
 */
public interface IASTTranslationUnit extends IASTScope, IASTNode {
}
