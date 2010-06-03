/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
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
 * Abstract Implementation of IProblemLocation
 * 
 * Clients may extend this class.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same.
 * </p>
 */
public abstract class AbstractProblemLocation implements IProblemLocation {
	protected IFile file;
	protected int line;
	protected int posStart;
	protected int posEnd;
	protected Object extra;

	protected AbstractProblemLocation(IFile file, int line) {
		this.file = file;
		this.line = line;
		this.posStart = -1;
		this.posEnd = -1;
	}

	protected AbstractProblemLocation(IFile file, int startChar, int endChar) {
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

	/**
	 * Sets extra data for the problem location
	 * 
	 * @param data
	 */
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

	/**
	 * Problem line number referenced in problem view in location field
	 */
	public int getLineNumber() {
		return getStartingLineNumber();
	}

	/**
	 * @return line number where problem starts
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
