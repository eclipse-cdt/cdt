package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethod;

public class Method extends SourceManipulation implements IMethod {
	
	public Method(ICElement parent, String name) {
		super(parent, name, CElement.C_METHOD);
	}

	/**
	 * @see IMethod
	 */
	public boolean isConstructor() throws CModelException {
		return getElementName().equals(getParent().getElementName());
	}

	/**
	 * @see IMethod
	 */
	public boolean isDestructor() throws CModelException {
		return getElementName().startsWith("~");
	}

	/**
	 * @see IMethod
	 */
	public boolean isOperator() throws CModelException {
		return getElementName().startsWith("operator");
	}

	/**
	 * @see IMethod
	 */
	public boolean isAbstract() throws CModelException {
		return false;
	}

	/**
	 * @see IMethod
	 */
	public boolean isVirtual() throws CModelException {
		return false;
	}

	/**
	 * @see IMethod
	 */
	public boolean isFriend() throws CModelException {
		return false;
	}

	/**
	 * @see IMethod
	 */
	public String[] getExceptions() {
		return new String[0];
	}

	/**
	 * @see IMethod
	 */
	public int getNumberOfParameters() {
		return 0;
	}

	public String getParameterInitializer(int pos) {
		return "";
	}

	public String[] getParameterTypes() {
		return new String[0];
	}

	public String getReturnType() throws CModelException {
		return "";
	}

	/**
	 * Returns true if the member as class scope.
	 * For example static methods in C++ have class scope 
	 *
	 * @see IMember
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	public boolean hasClassScope() throws CModelException {
		return false;
	}

	/**
	 * Returns whether this method/field is declared constant.
	 *
	 * @see IMember
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	public boolean isConst() throws CModelException {
		return false;
	}

	/**
	 * Returns the access Control of the member. The access qualifier
	 * can be examine using the AccessControl class.
	 *
	 * @see IMember
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	public int getAccessControl() throws CModelException {
		return 0;
	}

	protected CElementInfo createElementInfo () {
		return new SourceManipulationInfo(this);
	}
}
