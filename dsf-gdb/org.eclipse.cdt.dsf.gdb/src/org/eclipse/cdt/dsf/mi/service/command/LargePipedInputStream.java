/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;


class LargePipedInputStream extends PipedInputStream {
	
	private final int LARGE_BUF_SIZE = 1024 * 1024; // 1 megs
	
	public LargePipedInputStream(PipedOutputStream pipedoutputstream)
        throws IOException
    {
		super(pipedoutputstream);
		buffer = new byte[LARGE_BUF_SIZE];
    }

}
