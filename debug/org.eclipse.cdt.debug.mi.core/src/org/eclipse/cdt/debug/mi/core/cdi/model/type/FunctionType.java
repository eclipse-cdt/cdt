/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFunctionType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SourceManager;

/**
 */
public class FunctionType extends DerivedType implements ICDIFunctionType {

	public FunctionType(ICDITarget target, String typename) {
		super(target, typename);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIDerivedType#getComponentType()
	 */
	public ICDIType getComponentType() {
		if (derivedType != null) {
			String orig = getTypeName();
			String name = orig;
			int lparen = orig.lastIndexOf('(');
			int rparen = orig.lastIndexOf(')');
			if (lparen != -1 && rparen != -1 && (rparen > lparen)) {
				String dim = name.substring(lparen + 1, rparen).trim();
				try {
					name = orig.substring(0, lparen).trim();
					Session session = (Session)(getTarget().getSession());
					SourceManager sourceMgr = (SourceManager)session.getSourceManager();
					derivedType = sourceMgr.getType(getTarget(), name);
				} catch (CDIException e) {
//					// Try after ptype.
//					String ptype = sourceMgr.getDetailTypeName(type);
//					try {
//						type = sourceMgr.getType(ptype);
//					} catch (CDIException ex) {
//						type = new IncompleteType(typename);
//					}
				}
			}
			if (derivedType == null) {
				derivedType = new IncompleteType(getTarget(), name);
			}
		}
		return derivedType; 
	}

}
