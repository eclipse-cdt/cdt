/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType;

/**
 */
public class StructType extends AggregateType implements ICDIStructType {

	/**
	 * @param typename
	 */
	public StructType(ICDITarget target, String typename) {
		super(target, typename);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType#isClass()
	 */
	public boolean isClass() {
		return getDetailTypeName().startsWith("class");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType#isStruct()
	 */
	public boolean isStruct() {
		return getDetailTypeName().startsWith("struct");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType#isUnion()
	 */
	public boolean isUnion() {
		return getDetailTypeName().startsWith("union");
	}

}
