/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Marc Khouzam (Ericsson) - Specify the larger size in the new constructor
 *                               of PipedInputStream provided by Java 6
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;


class LargePipedInputStream extends PipedInputStream {
	
	private static final int LARGE_BUF_SIZE = 1024 * 1024; // 1M
	
	public LargePipedInputStream(PipedOutputStream pipedoutputstream)
        throws IOException
    {
		super(pipedoutputstream, LARGE_BUF_SIZE);
    }

}
