/*******************************************************************************
 * Copyright (c) 2013,2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.IBuiltinFunction;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.internal.core.makefile.Directive;

/**
 * Represents GNUmakefile built-in internal functions.
 */
public class BuiltinFunction implements IBuiltinFunction {
	private String name;
	private Directive parent;
	private StringBuffer sample;

	public BuiltinFunction(Directive parent, String sample) {
		this.name = getNameFromSample(sample);
		this.parent = parent;
		this.sample = new StringBuffer(sample);
	}

	private static String getNameFromSample(String sample) {
		String name = sample;
		if (sample.startsWith("$(") && sample.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
			name = sample.substring(2, sample.length() - 1);
		}
		return name;
	}

	@Override
	public IDirective getParent() {
		return parent;
	}

	@Override
	public int getStartLine() {
		return -1;
	}

	@Override
	public int getEndLine() {
		return -1;
	}

	@Override
	public IMakefile getMakefile() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public StringBuffer getValue() {
		return sample;
	}

	@Override
	public String toString() {
		return name;
	}

}
