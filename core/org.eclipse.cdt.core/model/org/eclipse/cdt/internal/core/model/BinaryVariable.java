/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IVariable;

/**
 *
 */
public class BinaryVariable extends BinaryElement implements IVariable {

	public BinaryVariable(ICElement parent, String name) {
		super(parent, name, ICElement.C_VARIABLE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IVariable#getInitializer()
	 */
	public String getInitializer() {
		// TODO Auto-generated method stub
		return "";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IVariableDeclaration#getTypeName()
	 */
	public String getTypeName() {
		// TODO Auto-generated method stub
		return "";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IVariableDeclaration#setTypeName(java.lang.String)
	 */
	public void setTypeName(String type) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IDeclaration#isStatic()
	 */
	public boolean isStatic() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IDeclaration#isConst()
	 */
	public boolean isConst() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IDeclaration#isVolatile()
	 */
	public boolean isVolatile() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IDeclaration#getAccessControl()
	 */
	public int getAccessControl() {
		// TODO Auto-generated method stub
		return 0;
	}

}
