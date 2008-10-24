/*******************************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for template non-type parameter in the index.
 */
class PDOMCPPTemplateNonTypeParameter extends PDOMCPPVariable implements IPDOMMemberOwner,
		ICPPTemplateNonTypeParameter {

	private static final int MEMBERLIST = PDOMCPPVariable.RECORD_SIZE;
	private static final int PARAMETERPOS= PDOMCPPVariable.RECORD_SIZE + 4;
	private static final int DEFAULTVAL= PDOMCPPVariable.RECORD_SIZE + 8;


	/**
	 * The size in bytes of a PDOMCPPTemplateTypeParameter record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPVariable.RECORD_SIZE + 12;
	
	public PDOMCPPTemplateNonTypeParameter(PDOM pdom, PDOMNode parent,
			ICPPTemplateNonTypeParameter param) throws CoreException {
		super(pdom, parent, param);
		final Database db = pdom.getDB();
		db.putInt(record + PARAMETERPOS, param.getParameterPosition());
		ICPPTemplateArgument val= param.getDefaultValue();
		if (val != null) {
			IValue sval= val.getNonTypeValue();
			if (sval != null) {
				IString s= db.newString(sval.getCanonicalRepresentation());
				db.putInt(record + DEFAULTVAL, s.getRecord());
			}
		}
	}

	public PDOMCPPTemplateNonTypeParameter(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_TEMPLATE_NON_TYPE_PARAMETER;
	}
	
	public ICPPTemplateArgument getDefaultValue() {
		try {
			final Database db = pdom.getDB();
			int rec= db.getInt(record + DEFAULTVAL);
			if (rec == 0)
				return null;
			String val= db.getString(rec).getString();
			if (val == null) 
				return null;
			return new CPPTemplateArgument(Value.fromCanonicalRepresentation(val), getType());
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public int getParameterPosition() {
		try {
			final Database db = pdom.getDB();
			return db.getInt(record + PARAMETERPOS);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return -1;
		}
	}

	@Override
	public void addChild(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.addMember(member);
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.accept(visitor);
	}
	
	@Override
	public Object clone() { fail(); return null; }


	public IASTExpression getDefault() {
		return null;
	}
}
