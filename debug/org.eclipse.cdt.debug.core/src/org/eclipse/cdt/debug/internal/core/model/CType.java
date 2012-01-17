/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDICharType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDerivedType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntegralType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.model.ICType;

/**
 * The CDI-based implementation of <code>ICType</code>.
 */
public class CType implements ICType {

	/**
	 * The underlying CDI type.
	 */
	private ICDIType fCDIType;

	/** 
	 * Constructor for CType. 
	 */
	public CType( ICDIType cdiType ) {
		setCDIType( cdiType );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICType#getName()
	 */
	@Override
	public String getName() {
		return ( fCDIType != null ) ? fCDIType.getTypeName() : null;
	}

	public void dispose() {
		fCDIType = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICType#getArrayDimensions()
	 */
	@Override
	public int[] getArrayDimensions() {
		int length = 0;
		ICDIType type = getCDIType();
		while( type instanceof ICDIArrayType ) {
			++length;
			type = ((ICDIDerivedType)type).getComponentType();
		}
		int[] dims = new int[length];
		type = getCDIType();
		for (int i = 0; i < length; i++) {
			dims[i] = ((ICDIArrayType)type).getDimension();
			type = ((ICDIDerivedType)type).getComponentType();
		}
		return dims;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICType#isArray()
	 */
	@Override
	public boolean isArray() {
		return ( getCDIType() instanceof ICDIArrayType );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICType#isCharacter()
	 */
	@Override
	public boolean isCharacter() {
		return ( getCDIType() instanceof ICDICharType );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICType#isFloatingPointType()
	 */
	@Override
	public boolean isFloatingPointType() {
		return ( getCDIType() instanceof ICDIFloatingPointType );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICType#isPointer()
	 */
	@Override
	public boolean isPointer() {
		return ( getCDIType() instanceof ICDIPointerType );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICType#isReference()
	 */
	@Override
	public boolean isReference() {
		return ( getCDIType() instanceof ICDIReferenceType );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICType#isStructure()
	 */
	@Override
	public boolean isStructure() {
		return ( getCDIType() instanceof ICDIStructType );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICType#isUnsigned()
	 */
	@Override
	public boolean isUnsigned() {
		return ( isIntegralType() ) ? ((ICDIIntegralType)getCDIType()).isUnsigned() : false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICType#isIntegralType()
	 */
	@Override
	public boolean isIntegralType() {
		return ( getCDIType() instanceof ICDIIntegralType );
	}

	protected ICDIType getCDIType() {
		return fCDIType;
	}

	protected void setCDIType( ICDIType type ) {
		fCDIType = type;
	}

	protected boolean isAggregate() {
		return ( isArray() || isStructure() || isPointer() || isReference() );
	}
}
