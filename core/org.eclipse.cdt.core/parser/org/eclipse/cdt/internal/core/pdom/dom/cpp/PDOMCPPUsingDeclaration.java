/*******************************************************************************
 * Copyright (c) 2008, 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
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
	private static final int TARGET_BINDING = PDOMCPPBinding.RECORD_SIZE;
	// Using declarations for functions may have multiple delegates. We model such case
	// by creating a chain of PDOMCPPUsingDeclaration objects linked by NEXT_DELEGATE field.
	
	private static final int NEXT_DELEGATE = TARGET_BINDING + Database.TYPE_SIZE;
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = NEXT_DELEGATE + Database.PTR_SIZE;
	
	private volatile IBinding[] delegates;
	
	public PDOMCPPUsingDeclaration(PDOMLinkage linkage, PDOMNode parent, ICPPUsingDeclaration using)
			throws CoreException {
		super(linkage, parent, using.getNameCharArray());

		final Database db = getDB();
		final char[] name = using.getNameCharArray();
		PDOMCPPUsingDeclaration last= null;
		for (IBinding delegate : using.getDelegates()) {
			if (delegate != null) {
				if (last == null) {
					setTargetBinding(linkage, delegate);
					last= this;
				} else {
					PDOMCPPUsingDeclaration next= new PDOMCPPUsingDeclaration(linkage, parent, name);
					next.setTargetBinding(linkage, delegate);
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

	private void setTargetBinding(PDOMLinkage linkage, IBinding delegate) throws CoreException {
		getLinkage().storeBinding(record + TARGET_BINDING, delegate);
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
			return getLinkage().loadBinding(record + TARGET_BINDING);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
}
