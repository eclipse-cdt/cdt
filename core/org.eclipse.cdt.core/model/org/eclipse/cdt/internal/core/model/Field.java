package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IField;

public class Field extends VariableDeclaration implements IField {
	
	public Field(ICElement parent, String name) {
		super(parent, name, CElement.C_FIELD);
	}

	public int getAccessControl(){
		return getFieldInfo().getAccessControl();
	}

	public boolean isMutable(){
		return getFieldInfo().isMutable();
	}

	public void setMutable(boolean mutable){
		getFieldInfo().setMutable(mutable);
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

	public void setConst(boolean isConst) {
		getFieldInfo().setConst(isConst);
	}

	public boolean isVolatile() {
		return getFieldInfo().isVolatile();
	}

	public void setVolatile(boolean isVolatile) {
		getFieldInfo().setVolatile(isVolatile);
	}

	public boolean isStatic() {
		return getFieldInfo().isStatic();
	}

	public void setStatic(boolean isStatic) {
		getFieldInfo().setStatic(isStatic);
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

	protected CElementInfo createElementInfo () {
		return new FieldInfo(this);
	}
}
