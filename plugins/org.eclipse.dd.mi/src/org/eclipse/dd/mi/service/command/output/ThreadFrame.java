/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service.command.output;

import java.math.BigInteger;

import org.eclipse.dd.dsf.concurrent.Immutable;

/**
 * @since 1.1
 */
@Immutable
public class ThreadFrame implements IThreadFrame {
	final private int        fStackLevel;
	final private BigInteger fAddress;
	final private String     fFunction;
	final private Object[]   fArgs;
	final private String     fFileName;
	final private String     fFullName;
	final private int        fLineNumber;
	
	public ThreadFrame(int stackLevel, BigInteger address, String function,
			Object[] args,	String file, String fullName, int line)
	{
		fStackLevel = stackLevel;
		fAddress    = address;
		fFunction   = function;
		fArgs       = args;
		fFileName   = file;
		fFullName   = fullName;
		fLineNumber = line;
	}

	public int        getStackLevel() { return fStackLevel; }
	public BigInteger getAddress()    { return fAddress;    }
	public String     getFucntion()   { return fFunction;   }
	public Object[]   getArgs() { return fArgs; }
	public String     getFileName()   { return fFileName;   }
	public String     getFullName()   { return fFullName;   }
	public int        getLineNumber() { return fLineNumber; }
}
