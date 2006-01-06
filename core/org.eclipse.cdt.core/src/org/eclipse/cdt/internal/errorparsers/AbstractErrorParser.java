/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.errorparsers;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;

/**
 * @author Doug Schaefer
 *
 */
public class AbstractErrorParser implements IErrorParser {

	private ErrorPattern[] patterns;
	
	protected AbstractErrorParser(ErrorPattern[] patterns) {
		this.patterns = patterns;
	}
	
	public boolean processLine(String line, ErrorParserManager eoParser) {
		for (int i = 0; i < patterns.length; ++i)
			if (patterns[i].processLine(line, eoParser))
				break;
		// Should this return true if we processed a line?
		return false;
	}
}
