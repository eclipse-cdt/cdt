/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIBoolType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDICharType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDoubleType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIEnumType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFunctionType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongLongType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIShortType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIWCharType;
import org.eclipse.cdt.debug.mi.core.cdi.model.Value;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;

/**
 * Enter type comment.
 * 
 * @since Jun 3, 2003
 */
public class ReferenceValue extends DerivedValue implements ICDIReferenceValue {

	/**
	 * @param v
	 */
	public ReferenceValue(Variable v) {
		super(v);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceValue#referenceValue()
	 */
	public ICDIValue referenceValue() throws CDIException {
		Value value = null;
		ICDIReferenceType rt = (ICDIReferenceType)getType();
		ICDIType t = rt.getComponentType();
		if (t instanceof ICDIBoolType) {
			value = new BoolValue(getVariable());
		} else if (t instanceof ICDICharType) {
			value = new CharValue(getVariable());
		} else if (t instanceof ICDIWCharType) {
			value = new WCharValue(getVariable());
		} else if (t instanceof ICDIShortType) {
			value = new ShortValue(getVariable());
		} else if (t instanceof ICDIIntType) {
			value = new IntValue(getVariable());
		} else if (t instanceof ICDILongType) {
			value = new LongValue(getVariable());
		} else if (t instanceof ICDILongLongType) {
			value = new LongLongValue(getVariable());
		} else if (t instanceof ICDIEnumType) {
			value = new EnumValue(getVariable());
		} else if (t instanceof ICDIFloatType) {
			value = new FloatValue(getVariable());
		} else if (t instanceof ICDIDoubleType) {
			value = new DoubleValue(getVariable());
		} else if (t instanceof ICDIFunctionType) {
			value = new FunctionValue(getVariable());
		} else if (t instanceof ICDIPointerType) {
			value = new PointerValue(getVariable());
//		} else if (t instanceof ICDIReferenceType) {
//			value = new ReferenceValue(getVariable());
		} else if (t instanceof ICDIArrayType) {
			value = new ArrayValue(getVariable());
		} else if (t instanceof ICDIStructType) {
			value = new StructValue(getVariable());
		} else {
			value = new Value(getVariable());
		}
		return value;		
	}
}
