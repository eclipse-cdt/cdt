/*******************************************************************************
 * Copyright (c) 2009, 2016 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * Abstract Implementation of IProblemLocation
 *
 * Clients may extend this class.
 */
public abstract class AbstractProblemLocation implements IProblemLocation {
	protected IResource file;
	protected int line;
	protected int posStart;
	protected int posEnd;
	protected Object extra;

	protected AbstractProblemLocation(IFile file, int line) {
		this((IResource) file, line);
	}

	/**
	 * @since 2.0
	 */
	protected AbstractProblemLocation(IResource file, int line) {
		this.file = file;
		this.line = line;
		this.posStart = -1;
		this.posEnd = -1;
	}

	protected AbstractProblemLocation(IFile file, int startChar, int endChar) {
		this((IResource) file, startChar, endChar);
	}

	/**
	 * @since 2.0
	 */
	protected AbstractProblemLocation(IResource file, int startChar, int endChar) {
		this.file = file;
		this.line = -1;
		this.posStart = startChar;
		this.posEnd = endChar;
	}

	/**
	 * Gets the extra data for the location. It is checker specific,
	 * for example can be problem backtrace.
	 *
	 * @return data object or null if non set
	 */
	@Override
	public Object getData() {
		return extra;
	}

	/**
	 * Sets extra data for the problem location.
	 *
	 * @param data
	 */
	public void setData(Object data) {
		this.extra = data;
	}

	@Override
	public IResource getFile() {
		return file;
	}

	/**
	 * @return resource for which marker is created
	 * @since 2.0
	 */
	public IResource getResource() {
		return file;
	}

	/**
	 * Problem line number referenced in problem view in location field
	 */
	@Override
	public int getLineNumber() {
		return getStartingLineNumber();
	}

	/**
	 * @return line number where problem starts
	 */
	public int getStartingLineNumber() {
		return line;
	}

	@Override
	public int getStartingChar() {
		return posStart;
	}

	@Override
	public int getEndingChar() {
		return posEnd;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((extra == null) ? 0 : extra.hashCode());
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + line;
		result = prime * result + posEnd;
		result = prime * result + posStart;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof AbstractProblemLocation))
			return false;
		AbstractProblemLocation other = (AbstractProblemLocation) obj;
		if (line != other.line)
			return false;
		if (posEnd != other.posEnd)
			return false;
		if (posStart != other.posStart)
			return false;
		if (extra == null) {
			if (other.extra != null)
				return false;
		} else if (!extra.equals(other.extra))
			return false;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}
}
