package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IField;

public class Field extends SourceManipulation implements IField {
	
	public Field(ICElement parent, String name) {
		super(parent, name, CElement.C_FIELD);
	}

	public int getAccessControl(){
		return getFieldInfo().getAccessControl();
	}

	public boolean isMutable(){
		return getFieldInfo().isMutable();
	}

	public void setIsMutable(boolean mutable){
		getFieldInfo().setIsMutable(mutable);
	}

	public String getTypeName() {
		return getFieldInfo().getTypeName();
	}

	public void setTypeName(String type) {
		getFieldInfo().setTypeName(type);
	}

	public boolean isConst() {
		return getFieldInfo().isConst();
	}

	public void setIsConst(boolean isConst) {
		getFieldInfo().setIsConst(isConst);
	}

	public boolean isVolatile() {
		return getFieldInfo().isVolatile();
	}

	public void setIsVolatile(boolean isVolatile) {
		getFieldInfo().setIsVolatile(isVolatile);
	}

	public int getVisibility() {
		return getFieldInfo().getVisibility();
	}

	public void setVisibility(int visibility) {
		getFieldInfo().setVisibility(visibility);
	}

	public FieldInfo getFieldInfo(){
		return (FieldInfo) getElementInfo();
	}

	/**
	 * Returns true if the member as class scope.
	 * For example static methods in C++ have class scope 
	 *
	 * @see IMember
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	public boolean hasClassScope(){
		return false;
	}

	protected CElementInfo createElementInfo () {
		return new FieldInfo(this);
	}

	// tests both info stored in element and element info
	public boolean isIdentical(Field other){
		FieldInfo otherInfo= other.getFieldInfo();
		if ( (this.equals(other))
		 && (getFieldInfo().hasSameContentsAs(otherInfo))
		 )
			return true;
		else
			return false;
	}
}
