/*******************************************************************************
 * Copyright (c) 2007, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructorSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 */
class PDOMCPPConstructorTemplateSpecialization extends PDOMCPPMethodTemplateSpecialization
		implements ICPPConstructorSpecialization {
	/** Offset of the constructor chain execution for constexpr constructors. */
	private static final int CONSTRUCTOR_CHAIN = PDOMCPPMethodTemplateSpecialization.RECORD_SIZE + 0; // Database.EXECUTION_SIZE

	/** The size in bytes of a PDOMCPPConstructorTemplateSpecialization record in the database. */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = CONSTRUCTOR_CHAIN + Database.EXECUTION_SIZE;

	public PDOMCPPConstructorTemplateSpecialization(PDOMCPPLinkage linkage, PDOMNode parent,
			ICPPConstructor constructor, PDOMBinding specialized) throws CoreException {
		super(linkage, parent, constructor, specialized);
		linkage.new ConfigureConstructorTemplateSpecialization(constructor, this);
	}

	public PDOMCPPConstructorTemplateSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	public void initConstructorData(ICPPExecution constructorChain) {
		if (constructorChain == null)
			return;
		try {
			getLinkage().storeExecution(record + CONSTRUCTOR_CHAIN, constructorChain);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CONSTRUCTOR_TEMPLATE_SPECIALIZATION;
	}

	@Override
	@Deprecated
	public ICPPExecution getConstructorChainExecution(IASTNode point) {
		return getConstructorChainExecution();
	}

	@Override
	public ICPPExecution getConstructorChainExecution() {
		if (!isConstexpr())
			return null;

		try {
			ICPPExecution exec = getLinkage().loadExecution(record + CONSTRUCTOR_CHAIN);
			if (exec == null) {
				exec = CPPTemplates.instantiateConstructorChain(this);
			}
			return exec;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
}
