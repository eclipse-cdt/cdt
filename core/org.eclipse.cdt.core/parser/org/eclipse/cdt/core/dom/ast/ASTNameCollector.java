/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.core.runtime.Assert;

/**
 * A convenience visitor that collects names.
 * @since 5.1
 */
public final class ASTNameCollector extends ASTVisitor {
	private final char[] fName;
	private final ArrayList<IASTName> fFound = new ArrayList<>(4);

	/**
	 * Constructs a name collector for the given name.
	 */
	public ASTNameCollector(char[] name) {
		Assert.isNotNull(name);
		fName = name;
		shouldVisitNames = true;
	}

	/**
	 * Constructs a name collector for the given name.
	 */
	public ASTNameCollector(String name) {
		this(name.toCharArray());
	}

	@Override
	public int visit(IASTName name) {
		if (name != null && !(name instanceof ICPPASTQualifiedName) && !(name instanceof ICPPASTTemplateId)) {
			if (CharArrayUtils.equals(fName, name.getSimpleID())) {
				fFound.add(name);
			}
		}
		return PROCESS_CONTINUE;
	}

	/**
	 * Returns the array of matching names.
	 */
	public IASTName[] getNames() {
		return fFound.toArray(new IASTName[fFound.size()]);
	}

	/**
	 * Clears the names found, such that the collector can be reused.
	 */
	public void clear() {
		fFound.clear();
	}
}
