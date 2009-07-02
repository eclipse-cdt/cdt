/*******************************************************************************
 * Copyright (c) 2007, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedefSpecialization;
import org.eclipse.cdt.internal.core.index.CPPTypedefClone;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 */
class PDOMCPPTypedefSpecialization extends PDOMCPPSpecialization
		implements ITypedef, ITypeContainer, IIndexType {

	private static final int TYPE = PDOMCPPSpecialization.RECORD_SIZE + 0;
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPSpecialization.RECORD_SIZE + 4;
	
	public PDOMCPPTypedefSpecialization(PDOMLinkage linkage, PDOMNode parent, ITypedef typedef, PDOMBinding specialized)
			throws CoreException {
		super(linkage, parent, (ICPPSpecialization) typedef, specialized);

		try {
			if (typedef instanceof CPPTypedefSpecialization) {
				if (((CPPTypedefSpecialization) typedef).incResolutionDepth(1) >
						CPPTypedefSpecialization.MAX_RESOLUTION_DEPTH) {
					return;
				}
			}
			IType type = typedef.getType();
			// The following may try to add the same typedef specialization to the index again.
			// We protect against infinite recursion using a counter inside typedef.
			PDOMNode typeNode = parent.getLinkage().addType(this, type);
			if (typeNode != null)
				getDB().putRecPtr(record + TYPE, typeNode.getRecord());
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		} finally {
			if (typedef instanceof CPPTypedefSpecialization) {
				((CPPTypedefSpecialization) typedef).incResolutionDepth(-1);
			}
		}
	}

	public PDOMCPPTypedefSpecialization(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_TYPEDEF_SPECIALIZATION;
	}

	public IType getType() throws DOMException {
		try {
			PDOMNode node = getLinkage().getNode(getDB().getRecPtr(record + TYPE));
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
    @Override
	public Object clone() {
		return new CPPTypedefClone(this);
    }
}
