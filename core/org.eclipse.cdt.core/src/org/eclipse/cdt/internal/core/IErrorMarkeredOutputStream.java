/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dmitry Kozlov (CodeSourcery) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core;

import java.io.IOException;

import org.eclipse.cdt.core.ProblemMarkerInfo;

/** 
 * Output stream for use in build console capable of processing markers info 
 * attached to the output.
 * @since 5.2 
 */
public interface IErrorMarkeredOutputStream {
	
	public void write(String s, ProblemMarkerInfo marker) throws IOException;
	
	public void write(byte[] b, int offset, int len) throws IOException;
	
	public void flush() throws IOException;
	
	public void close() throws IOException;

}
