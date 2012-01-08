/*******************************************************************************
 * Copyright (c) 2008, 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	  Sergey Prigogin (Google) - initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents "using" declaration in PDOM. A single "using" declaration resolving to multiple
 * objects, e.g. functions with the same name but different signatures, is represented by multiple
 * chained PDOMCPPUsingDeclaration records.
 * 
 * @see ICPPUsingDeclaration
 */
class PDOMCPPUsingDeclaration extends PDOMCPPBinding implements	ICPPUsingDeclaration {
	
	private static final int TARGET_BINDING = PDOMCPPBinding.RECORD_SIZE + 0;
	// Using declarations for functions may have multiple delegates. We model such case
	// by creating a chain of PDOMCPPUsingDeclaration objects linked by NEXT_DELEGATE field.
	
	private static final int NEXT_DELEGATE = PDOMCPPBinding.RECORD_SIZE + 4;
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + 8;
	
	private volatile IBinding[] delegates;
	
	public PDOMCPPUsingDeclaration(PDOMLinkage linkage, PDOMNode parent, ICPPUsingDeclaration using)
			throws CoreException {
		super(linkage, parent, using.getNameCharArray());

		final Database db = getDB();
		final char[] name = using.getNameCharArray();
		Set<PDOMBinding> targets= new LinkedHashSet<PDOMBinding>();
		PDOMCPPUsingDeclaration last= null;
		for (IBinding delegate : using.getDelegates()) {
			PDOMBinding target = getLinkage().addPotentiallyUnknownBinding(delegate);
			if (target != null && targets.add(target)) {
				if (last == null) {
					setTargetBinding(linkage, target);
					last= this;
				} else {
					PDOMCPPUsingDeclaration next= new PDOMCPPUsingDeclaration(linkage, parent, name);
					next.setTargetBinding(linkage, target);
					db.putRecPtr(last.getRecord() + NEXT_DELEGATE, next.record);
					last= next;
				}
			}
		}
	}

	public PDOMCPPUsingDeclaration(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	private PDOMCPPUsingDeclaration(PDOMLinkage linkage, PDOMNode parent, char[] name) throws CoreException {
		super(linkage, parent, name);
	}

	private void setTargetBinding(PDOMLinkage linkage, PDOMBinding delegate) throws CoreException {
		getDB().putRecPtr(record + TARGET_BINDING, delegate != null ? delegate.getRecord() : 0);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_USING_DECLARATION;
	}

	@Override
	public IBinding[] getDelegates() {
		if (delegates == null) {
			delegates = new IBinding[1];
			int i = 0;
			PDOMCPPUsingDeclaration alias = this;
			try {
				do {
					IBinding delegate = alias.getBinding();
					if (delegate != null) {
						delegates= ArrayUtil.appendAt(IBinding.class, delegates, i++, delegate);
					}
				} while ((alias = alias.getNext()) != null);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
			delegates = ArrayUtil.trim(IBinding.class, delegates);
		}
		return delegates;
	}

	private PDOMCPPUsingDeclaration getNext() throws CoreException {
		long nextRecord = getDB().getRecPtr(record + NEXT_DELEGATE);
		return nextRecord != 0 ? new PDOMCPPUsingDeclaration(getLinkage(), nextRecord) : null;
	}

	private IBinding getBinding() {
		try {
			return (IBinding) getLinkage().getNode(
					getPDOM().getDB().getRecPtr(record + TARGET_BINDING));
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
}
