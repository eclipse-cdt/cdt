package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IField;

public class Field extends SourceManipulation implements IField {
	
	public Field(ICElement parent, String name) {
		super(parent, name, CElement.C_FIELD);
	}

	public boolean isMutable() throws CModelException {
		return false;
	}

	/*
	 * @IVariable
	 */
	public String getType() {
		return "";
	}

	/*
	 * @IVariable
	 */
	public String getInitializer() {
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
