/*******************************************************************************
 * Copyright (c) 2006 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

/**
 * A reader that's able to decipher debug symbol formats.
 * 
 * This initial version only returns a list of source files.
 * 
 */
public interface ISymbolReader {

	String[] getSourceFiles();
}
