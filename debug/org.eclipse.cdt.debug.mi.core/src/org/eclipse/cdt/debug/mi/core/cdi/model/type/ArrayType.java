/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SourceManager;

/**
 */
public class ArrayType extends DerivedType implements ICDIArrayType {

	int dimension;

	/**
	 * @param typename
	 */
	public ArrayType(ICDITarget target, String typename) {
		super(target, typename);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIArrayType#getComponentType()
	 */
	public ICDIType getComponentType() {
		if (derivedType == null) {
			String orig = getTypeName();
			String name = orig;
			int lbracket = orig.lastIndexOf('[');
			int rbracket = orig.lastIndexOf(']');
			if (lbracket != -1 && rbracket != -1 && (rbracket > lbracket)) {
				String dim = name.substring(lbracket + 1, rbracket).trim();
				try {
					dimension = Integer.parseInt(dim);
					name = orig.substring(0, lbracket).trim();
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
				} catch (NumberFormatException e) {
				}
			}
			if (derivedType == null) {
				derivedType = new IncompleteType(getTarget(), name);
			}
		}
		return derivedType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType#getDimension()
	 */
	public int getDimension() {
		return dimension;
	}

}
