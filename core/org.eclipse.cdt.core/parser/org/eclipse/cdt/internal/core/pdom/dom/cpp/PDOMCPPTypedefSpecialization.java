/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPTypedefSpecialization extends PDOMCPPSpecialization
		implements ITypedef, ITypeContainer, IIndexType {


	private static final int TYPE = PDOMCPPSpecialization.RECORD_SIZE + 0;
	
	protected static final int RECORD_SIZE = PDOMCPPSpecialization.RECORD_SIZE + 4;
	
	public PDOMCPPTypedefSpecialization(PDOM pdom, PDOMNode parent, ITypedef typedef, PDOMBinding specialized)
			throws CoreException {
		super(pdom, parent, (ICPPSpecialization) typedef, specialized);

		try {
			IType type = typedef.getType();
			PDOMNode typeNode = parent.getLinkageImpl().addType(this, type);
			if (typeNode != null)
				pdom.getDB().putInt(record + TYPE, typeNode.getRecord());
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public PDOMCPPTypedefSpecialization(PDOM pdom, int record) {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public int getNodeType() {
		return PDOMCPPLinkage.CPP_TYPEDEF_SPECIALIZATION;
	}

	public IType getType() throws DOMException {
		try {
			PDOMNode node = getLinkageImpl().getNode(pdom.getDB().getInt(record + TYPE));
			return node instanceof IType ? (IType)node : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isSameType(IType o) {
        if( this.equals(o) )
            return true;
	    if( o instanceof ITypedef )
            try {
                IType t = getType();
                if( t != null )
                    return t.isSameType( ((ITypedef)o).getType());
                return false;
            } catch ( DOMException e ) {
                return false;
            }
	        
        try {
		    IType t = getType();
		    if( t != null )
		        return t.isSameType( o );
        } catch ( DOMException e ) {
            return false;
        }
	    return false;
	}

	public void setType(IType type) { fail(); }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() {
    	IType t = null;
   		try {
            t = (IType) super.clone();
        } catch ( CloneNotSupportedException e ) {
            //not going to happen
        }
        return t;
    }
}
