/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunction;

/**
 */
public class BinaryFunction extends BinaryElement implements IFunction {

	public BinaryFunction(ICElement parent, String name, long a) {
		super(parent, name, ICElement.C_FUNCTION, a);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IFunctionDeclaration#getExceptions()
	 */
	public String[] getExceptions() {
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IFunctionDeclaration#getNumberOfParameters()
	 */
	public int getNumberOfParameters() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IFunctionDeclaration#getParameterInitializer(int)
	 */
	public String getParameterInitializer(int pos) {
		// TODO Auto-generated method stub
		return new String();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IFunctionDeclaration#getParameterTypes()
	 */
	public String[] getParameterTypes() {
		// TODO Auto-generated method stub
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IFunctionDeclaration#getReturnType()
	 */
	public String getReturnType() {
		// TODO Auto-generated method stub
		return new String();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IFunctionDeclaration#getSignature()
	 */
	public String getSignature() {
		return getElementName();
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

}
