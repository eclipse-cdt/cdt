/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.utils.debug;


/**
 * DebugType
 *  
 */
public class DebugType {

	/**
	 *  
	 */
	protected DebugType() {
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (this instanceof DebugArrayType) {
			DebugArrayType arrayType = (DebugArrayType)this;
			int size = arrayType.getSize();
			DebugType type = arrayType.getComponentType();
			sb.append(type.toString());
			sb.append(" [").append(size).append(']'); //$NON-NLS-1$
		} else if (this instanceof DebugDerivedType) {
			DebugDerivedType derived = (DebugDerivedType)this;
			DebugType component = derived.getComponentType();
			if (component instanceof DebugStructType) {
				DebugStructType structType = (DebugStructType)component;
				sb.append(structType.getName());				
			} else if (component != null){
				sb.append(component.toString());
			}
			if (this instanceof DebugPointerType) {
				sb.append(" *"); //$NON-NLS-1$
			} else if (this instanceof DebugReferenceType) {
				sb.append(" &"); //$NON-NLS-1$
			} else if (this instanceof DebugCrossRefType && component == null) {
				DebugCrossRefType crossRef = (DebugCrossRefType)this;
				sb.append(crossRef.getCrossRefName());
				//sb.append(crossRef.getName());
			}
		} else if (this instanceof DebugBaseType) {
			DebugBaseType base = (DebugBaseType)this;
			String typeName = base.getTypeName();
			sb.append(typeName);
		} else if (this instanceof DebugFunctionType) {
			DebugFunctionType function = (DebugFunctionType)this;
			DebugType type = function.getReturnType();
			sb.append(type.toString());
			sb.append(" (*())"); //$NON-NLS-1$
		} else if (this instanceof DebugEnumType) {
			DebugEnumType enumarator = (DebugEnumType)this;
			DebugEnumField[] fields = enumarator.getDebugEnumFields();
			sb.append("enum ").append(enumarator.getName()).append(" {"); //$NON-NLS-1$ //$NON-NLS-2$
			for (int i = 0; i < fields.length; i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append(' ').append(fields[i].getName());
				sb.append(" = ").append(fields[i].getValue()); //$NON-NLS-1$
			}
			sb.append(" }"); //$NON-NLS-1$
		} else if (this instanceof DebugStructType) {
			DebugStructType struct = (DebugStructType)this;
			if (struct.isUnion()) {
				sb.append("union "); //$NON-NLS-1$
			} else {
				sb.append("struct "); //$NON-NLS-1$
			}
			sb.append(struct.getName()).append(" {"); //$NON-NLS-1$
			DebugField[] fields = struct.getDebugFields();
			for (int i = 0; i < fields.length; i++) {
				if (i > 0) {
					sb.append(';');
				}
				sb.append(' ').append(fields[i].getDebugType());
				sb.append(' ').append(fields[i].getName());
			}
			sb.append(" }"); //$NON-NLS-1$
		} else if (this instanceof DebugUnknownType) {
			DebugUnknownType unknown = (DebugUnknownType)this;
			sb.append(unknown.getName());
		}
		return sb.toString();
	}
}
