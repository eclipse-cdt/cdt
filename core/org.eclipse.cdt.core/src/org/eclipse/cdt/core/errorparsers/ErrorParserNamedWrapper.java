/*******************************************************************************
 * Copyright (c) 2009, 2009 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.errorparsers;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IErrorParserNamed;
import org.eclipse.core.runtime.Assert;

/**
 * Class to wrap any {@link IErrorParser} to {@link IErrorParserNamed}.
 * @since 5.2
 */
public class ErrorParserNamedWrapper implements IErrorParserNamed {
	private String fId;
	private String fName;
	private final IErrorParser fErrorParser;

	/**
	 * Constructor.
	 *
	 * @param id - assigned ID
	 * @param name - assigned name.
	 * @param errorParser - error parser to assign name and ID.
	 */
	public ErrorParserNamedWrapper(String id, String name, IErrorParser errorParser) {
		Assert.isNotNull(errorParser);

		this.fId = id;
		this.fName = name;
		this.fErrorParser = errorParser;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IErrorParser#processLine(java.lang.String, org.eclipse.cdt.core.ErrorParserManager)
	 */
	@Override
	public boolean processLine(String line, ErrorParserManager epm) {
		return fErrorParser.processLine(line, epm);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IErrorParserNamed#getId()
	 */
	@Override
	public String getId() {
		return fId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IErrorParserNamed#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}

	/**
	 * @return original error parser which is being wrapped
	 */
	public IErrorParser getErrorParser() {
		return fErrorParser;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IErrorParserNamed#setId(java.lang.String)
	 */
	@Override
	public void setId(String id) {
		this.fId = id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IErrorParserNamed#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.fName = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof ErrorParserNamedWrapper) {
			ErrorParserNamedWrapper that = (ErrorParserNamedWrapper)o;
			return this.fId.equals(that.fId)
				&& this.fName.equals(that.fName)
				// can't be more specific than that since IErrorParser may not implement equals()...
				&& this.getClass()==that.getClass();
		}
		return false;

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		// shallow copy since IErrorParser is not {@link Cloneable} in general.
		return new ErrorParserNamedWrapper(fId, fName, fErrorParser);
	}
}
