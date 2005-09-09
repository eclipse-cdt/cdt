package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;
import org.eclipse.cdt.core.pdom.PDOMUnimplementedException;

public class PCBasicType implements ICBasicType {

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	public boolean isComplex() {
		throw new PDOMUnimplementedException();
	}

	public boolean isImaginary() {
		throw new PDOMUnimplementedException();
	}

	public boolean isLongLong() throws DOMException {
		throw new PDOMUnimplementedException();
	}

	public int getType() throws DOMException {
		throw new PDOMUnimplementedException();
	}

	public IASTExpression getValue() throws DOMException {
		throw new PDOMUnimplementedException();
	}

	public boolean isSigned() throws DOMException {
		throw new PDOMUnimplementedException();
	}

	public boolean isUnsigned() throws DOMException {
		throw new PDOMUnimplementedException();
	}

	public boolean isShort() throws DOMException {
		throw new PDOMUnimplementedException();
	}

	public boolean isLong() throws DOMException {
		throw new PDOMUnimplementedException();
	}

	public boolean isSameType(IType type) {
		throw new PDOMUnimplementedException();
	}

}
