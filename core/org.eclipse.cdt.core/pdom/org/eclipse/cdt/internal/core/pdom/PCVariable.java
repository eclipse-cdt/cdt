package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.pdom.PDOMUnimplementedException;

public class PCVariable implements IVariable {

	public IType getType() throws DOMException {
		throw new PDOMUnimplementedException();
	}

	public boolean isStatic() throws DOMException {
		throw new PDOMUnimplementedException();
	}

	public boolean isExtern() throws DOMException {
		throw new PDOMUnimplementedException();
	}

	public boolean isAuto() throws DOMException {
		throw new PDOMUnimplementedException();
	}

	public boolean isRegister() throws DOMException {
		throw new PDOMUnimplementedException();
	}

	public String getName() {
		throw new PDOMUnimplementedException();
	}

	public char[] getNameCharArray() {
		throw new PDOMUnimplementedException();
	}

	public IScope getScope() throws DOMException {
		throw new PDOMUnimplementedException();
	}

}
