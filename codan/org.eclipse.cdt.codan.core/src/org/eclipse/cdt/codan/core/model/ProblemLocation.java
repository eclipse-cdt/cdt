/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IFile;

/**
 * Implementation of IProblemLocation
 * 
 */
public class ProblemLocation implements IProblemLocation {
	protected IFile file;
	protected int line;
	protected int posStart;
	protected int posEnd;
	protected Object extra;

	/**
	 * @param file
	 * @param line
	 * @param lineEnd
	 * @param posStart
	 * @param posEnd
	 */
	public ProblemLocation(IFile file, int line) {
		this.file = file;
		this.line = line;
		this.posStart = -1;
		this.posEnd = -1;
	}

	/**
	 * @param file
	 * @param startingLineNumber
	 * @param endingLineNumber
	 */
	public ProblemLocation(IFile file, int startChar, int endChar) {
		this.file = file;
		this.line = -1;
		this.posStart = startChar;
		this.posEnd = endChar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemLocation#getData()
	 */
	public Object getData() {
		return extra;
	}

	public void setData(Object data) {
		this.extra = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemLocation#getFile()
	 */
	public IFile getFile() {
		return file;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemLocation#getLine()
	 */
	public int getLineNumber() {
		return getStartingLineNumber();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemLocation#getStartLine()
	 */
	public int getStartingLineNumber() {
		return line;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemLocation#getStartPos()
	 */
	public int getStartingChar() {
		return posStart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemLocation#getEndingChar()
	 */
	public int getEndingChar() {
		return posEnd;
	}
}
