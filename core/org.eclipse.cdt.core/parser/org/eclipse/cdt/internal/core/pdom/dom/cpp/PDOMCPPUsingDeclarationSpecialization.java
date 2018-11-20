/*******************************************************************************
 * Copyright (c) 2011, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

class PDOMCPPUsingDeclarationSpecialization extends PDOMCPPSpecialization implements ICPPUsingDeclaration {
	private static final int TARGET_BINDINGS = PDOMCPPSpecialization.RECORD_SIZE;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = TARGET_BINDINGS + Database.PTR_SIZE;

	private volatile IBinding[] delegates;

	public PDOMCPPUsingDeclarationSpecialization(PDOMCPPLinkage linkage, PDOMNode parent, ICPPUsingDeclaration using,
			PDOMBinding specialized) throws CoreException {
		super(linkage, parent, (ICPPSpecialization) using, specialized);

		Set<PDOMBinding> targets = new LinkedHashSet<>();
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + TARGET_BINDINGS);
		for (IBinding delegate : using.getDelegates()) {
			PDOMBinding target = getLinkage().adaptBinding(delegate);
			if (target != null && targets.add(target)) {
				list.addMember(target);
			}
		}
	}

	public PDOMCPPUsingDeclarationSpecialization(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_USING_DECLARATION_SPECIALIZATION;
	}

	@Override
	public IBinding[] getDelegates() {
		if (delegates == null) {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + TARGET_BINDINGS);
			final List<IBinding> result = new ArrayList<>();
			try {
				list.accept(new IPDOMVisitor() {
					@Override
					public boolean visit(IPDOMNode node) {
						if (node instanceof IBinding) {
							result.add((IBinding) node);
						}
						return true;
					}

					@Override
					public void leave(IPDOMNode node) {
					}
				});
			} catch (CoreException e) {
			}
			delegates = result.toArray(new IBinding[result.size()]);
		}
		return delegates;
	}
}
