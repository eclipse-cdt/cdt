/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.core.model; 

import java.math.BigInteger;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
 
/**
 * The CDI based implementation of <code>ICModule</code>.
 */
public class CModule extends CDebugElement implements ICModule {

	private int fType = 0;

	private ICElement fCElement;

	private ICDIObject fCDIObject;
	
	private IPath fImageName;

	private IPath fSymbolsFileName;
	
	public static CModule createExecutable( CDebugTarget target, IPath path ) {
		// TODO Add support for executables to CDI.
		return new CModule( EXECUTABLE, target, path );
	}

	public static CModule createSharedLibrary( CDebugTarget target, ICDISharedLibrary lib ) {
		return new CModule( SHARED_LIBRARY, target, lib );
	}

	public static CModule createCore( CDebugTarget target, IPath path ) {
		// TODO Add support for core file to CDI.
		return new CModule( CORE, target, path );
	}

	/** 
	 * Constructor for CModule. 
	 */
	private CModule( int type, CDebugTarget target, IPath path ) {
		super( target );
		fType = type;
		fCElement = CoreModel.getDefault().create( path );
		fCDIObject = null;
		fImageName = path;
		fSymbolsFileName = path;
	}

	/** 
	 * Constructor for CModule. 
	 */
	private CModule( int type, CDebugTarget target, ICDIObject cdiObject ) {
		super( target );
		fType = type;
		if ( cdiObject instanceof ICDISharedLibrary ) {
			fCElement = CoreModel.getDefault().create( new Path( ((ICDISharedLibrary)cdiObject).getFileName() ) );
		}
		fCDIObject = cdiObject;
		fImageName = ( ( cdiObject instanceof ICDISharedLibrary ) ) ? new Path( ((ICDISharedLibrary)cdiObject).getFileName() ) : new Path( CoreModelMessages.getString( "CModule.0" ) ); //$NON-NLS-1$
		fSymbolsFileName = fImageName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getType()
	 */
	public int getType() {
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getName()
	 */
	public String getName() {
		return fImageName.lastSegment().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getImageName()
	 */
	public IPath getImageName() {
		return fImageName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getSymbolsFileName()
	 */
	public IPath getSymbolsFileName() {
		return fSymbolsFileName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#setSymbolsFileName(org.eclipse.core.runtime.IPath)
	 */
	public void setSymbolsFileName( IPath symbolsFile ) {
		fSymbolsFileName = symbolsFile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getBaseAddress()
	 */
	public IAddress getBaseAddress() {
		return ( fCDIObject instanceof ICDISharedLibrary ) ? getAddressFactory().createAddress( ((ICDISharedLibrary)fCDIObject).getStartAddress() ) : getAddressFactory().getZero();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getSize()
	 */
	public long getSize() {
		long result = 0;
		if ( fCDIObject instanceof ICDISharedLibrary ) { 
			BigInteger start = ((ICDISharedLibrary)fCDIObject).getStartAddress();
			BigInteger end = ((ICDISharedLibrary)fCDIObject).getEndAddress();
			if ( end.compareTo( start ) > 0 )
				result = end.subtract( start ).longValue(); 
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#areSymbolsLoaded()
	 */
	public boolean areSymbolsLoaded() {
		return ( fCElement instanceof IBinary ) ? ((IBinary)fCElement).hasDebug() : 
			( ( fCDIObject instanceof ICDISharedLibrary ) ? ((ICDISharedLibrary)fCDIObject).areSymbolsLoaded() : false );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#loadSymbols()
	 */
	public void loadSymbols() throws DebugException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getPlatform()
	 */
	public String getPlatform() {
		return ( fCElement instanceof IBinary ) ? ((IBinary)fCElement).getCPU() : CoreModelMessages.getString( "CModule.1" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#isLittleEndian()
	 */
	public boolean isLittleEndian() {
		return ( fCElement instanceof IBinary ) ? ((IBinary)fCElement).isLittleEndian() : ((CDebugTarget)getDebugTarget()).isLittleEndian();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getAddressFactory()
	 */
	public IAddressFactory getAddressFactory() {
		return ((CDebugTarget)getDebugTarget()).getAddressFactory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getCPU()
	 */
	public String getCPU() {
		return ( fCElement instanceof IBinary ) ? ((IBinary)fCElement).getCPU() : null;
	}

	public void dispose() {
		
	}

	public boolean equals( ICDIObject cdiObject ) {
		return ( fCDIObject != null ) ? fCDIObject.equals( cdiObject ) : false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( ICElement.class.equals( adapter ) ) {
			return getCElement();
		}
		if ( IBinary.class.equals( adapter ) && getCElement() instanceof IBinary ) {
			return (IBinary)getCElement();
		}
		return super.getAdapter( adapter );
	}

	protected ICElement getCElement() {
		return fCElement;
	}
}
