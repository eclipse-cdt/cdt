/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDICharType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDerivedType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.model.ICType;

/**
 * Enter type comment.
 * 
 * @since Jun 10, 2003
 */
public class CType implements ICType
{
	private ICDIType fCDIType;

	public CType( ICDIType cdiType )
	{
		setCDIType( cdiType );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.type.ICType#getName()
	 */
	public String getName()
	{
		return ( fCDIType != null ) ? fCDIType.getTypeName() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( ICType.class.equals( adapter ) )
			return this;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.type.ICType#dispose()
	 */
	public void dispose()
	{
		fCDIType = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.type.ICType#getArrayDimensions()
	 */
	public int[] getArrayDimensions()
	{
		int length = 0;
		ICDIType type = getCDIType();
		while( type instanceof ICDIArrayType )
		{
			++length;
			type = ( type instanceof ICDIDerivedType ) ? ((ICDIDerivedType)type).getComponentType() : null;
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
	 * @see org.eclipse.cdt.debug.core.model.type.ICType#isArray()
	 */
	public boolean isArray()
	{
		return ( getCDIType() instanceof ICDIArrayType );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.type.ICType#isCharacter()
	 */
	public boolean isCharacter()
	{
		return ( getCDIType() instanceof ICDICharType );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.type.ICType#isFloatingPointType()
	 */
	public boolean isFloatingPointType()
	{
		return ( getCDIType() instanceof ICDIFloatingPointType );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.type.ICType#isPointer()
	 */
	public boolean isPointer()
	{
		return ( getCDIType() instanceof ICDIPointerType );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICType#isReference()
	 */
	public boolean isReference()
	{
		return ( getCDIType() instanceof ICDIReferenceType );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.type.ICType#isStructure()
	 */
	public boolean isStructure()
	{
		return ( getCDIType() instanceof ICDIStructType );
	}

	protected ICDIType getCDIType()
	{
		return fCDIType;
	}

	protected void setCDIType( ICDIType type )
	{
		fCDIType = type;
	}

	protected boolean hasChildren()
	{
		ICDIType type = getCDIType();
		if ( type instanceof ICDIStructType || type instanceof ICDIArrayType || 
			 type instanceof ICDIPointerType || type instanceof ICDIReferenceType )
			return true;
		return false;
	}
}
