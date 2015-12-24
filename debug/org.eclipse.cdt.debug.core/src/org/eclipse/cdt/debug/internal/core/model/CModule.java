/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model; 

import java.math.BigInteger;
import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.internal.core.model.Binary;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
 
/**
 * The CDI based implementation of <code>ICModule</code>.
 * 
 * This implementation is experimental and needs to be changed when
 * the CDI level support is available. 
 */
public class CModule extends CDebugElement implements ICModule {

	private int fType = 0;

	private Binary fBinary;

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

	/** 
	 * Constructor for CModule. Used for the program.
	 */
	private CModule( int type, CDebugTarget target, IPath path ) {
		super( target );
		fType = type;
		fBinary = createBinary(path);
		fCDIObject = null;
		fImageName = path;
		fSymbolsFileName = path;
	}

	/** 
	 * Constructor for CModule. Used for shared libraries. 
	 */
	private CModule( int type, CDebugTarget target, ICDIObject cdiObject ) {
		super( target );
		fType = type;
		if ( cdiObject instanceof ICDISharedLibrary ) {
			ICDISharedLibrary cdiSharedLib = (ICDISharedLibrary)cdiObject;
			fBinary = createBinary(new Path(cdiSharedLib.getFileName()));
		}
		fCDIObject = cdiObject;
		fImageName = ( ( cdiObject instanceof ICDISharedLibrary ) ) ? new Path( ((ICDISharedLibrary)cdiObject).getFileName() ) : new Path( CoreModelMessages.getString( "CModule.0" ) ); //$NON-NLS-1$
		fSymbolsFileName = fImageName;
	}

	/**
	 * We used to ask the CoreModel to create the Binary (ICElement) for us but
	 * it will do so only for binary files that are in a project output
	 * directory (for performance reasons). So, we do all the leg work
	 * ourselves, duplicating much of the code, unfortunately.
	 * 
	 * THE OLD WAY... 
	 * 		fCElement = CoreModel.getDefault().create(path);
	 */
	private Binary createBinary(IPath path) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFileForLocation(path); 
		if (file != null && !file.exists()) {
			file = null;
		}

		// In case this is an external resource see if we can find
		// a file for it.
		if (file == null) {
			IFile[] files = root.findFilesForLocation(path);
			if (files.length > 0) {
				file = files[0];
			}
		}

		if (file != null) {
			ICProject cproject = CoreModel.getDefault().create(file.getProject());
			IPath resourcePath = file.getParent().getFullPath();
			
			try {
				ICElement cfolder = cproject.findElement(resourcePath);
				
				// Check if folder is a source root and use that instead
				ISourceRoot sourceRoot = cproject.findSourceRoot(resourcePath);
				if (sourceRoot != null)
					cfolder = sourceRoot;
				
				IBinaryFile bin = CModelManager.getDefault().createBinaryFile(file);
				if (bin != null) {
					return new Binary(cfolder, file, (IBinaryObject)bin);
				}
			} catch (CModelException e) {
				CDebugCorePlugin.log(e);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getType()
	 */
	@Override
	public int getType() {
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getName()
	 */
	@Override
	public String getName() {
		return fImageName.lastSegment().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getImageName()
	 */
	@Override
	public IPath getImageName() {
		return fImageName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getSymbolsFileName()
	 */
	@Override
	public IPath getSymbolsFileName() {
		return fSymbolsFileName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#setSymbolsFileName(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public void setSymbolsFileName( IPath symbolsFile ) throws DebugException {
		loadSymbolsFromFile( symbolsFile );
		fSymbolsFileName = symbolsFile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getBaseAddress()
	 */
	@Override
	public IAddress getBaseAddress() {
		return ( fCDIObject instanceof ICDISharedLibrary ) ? getAddressFactory().createAddress( ((ICDISharedLibrary)fCDIObject).getStartAddress() ) : getAddressFactory().getZero();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getSize()
	 */
	@Override
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
	@Override
	public boolean areSymbolsLoaded() {
		if (fCDIObject instanceof ICDISharedLibrary)
			return ((ICDISharedLibrary)fCDIObject).areSymbolsLoaded();
		
		if (fBinary != null)
			return fBinary.hasDebug();
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#canLoadSymbols()
	 */
	@Override
	public boolean canLoadSymbols() {
		return ( getDebugTarget().isSuspended() && !areSymbolsLoaded() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#loadSymbols()
	 */
	@Override
	public void loadSymbols() throws DebugException {
		loadSymbolsFromFile( getSymbolsFileName() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getPlatform()
	 */
	@Override
	public String getPlatform() {
		return ( fBinary != null ) ? fBinary.getCPU() : CoreModelMessages.getString( "CModule.1" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#isLittleEndian()
	 */
	@Override
	public boolean isLittleEndian() {
		return ( fBinary != null ) ? fBinary.isLittleEndian() : ((CDebugTarget)getDebugTarget()).isLittleEndian();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getAddressFactory()
	 */
	@Override
	public IAddressFactory getAddressFactory() {
		return ((CDebugTarget)getDebugTarget()).getAddressFactory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICModule#getCPU()
	 */
	@Override
	public String getCPU() {
		return ( fBinary != null ) ? fBinary.getCPU() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter( Class adapter ) {
		if ( ICElement.class.equals( adapter ) ) {
			return getCElement();
		}
		if ( IBinary.class.equals( adapter ) && getCElement() instanceof IBinary ) {
			return getCElement();
		}
		return super.getAdapter( adapter );
	}

	public void dispose() {
		
	}

	public boolean equals( ICDIObject cdiObject ) {
		return ( fCDIObject != null ) ? fCDIObject.equals( cdiObject ) : false;
	}

	protected ICElement getCElement() {
		return fBinary;
	}

	private void loadSymbolsFromFile(IPath path) throws DebugException {
		if (fCDIObject instanceof ICDISharedLibrary) {
			if (path == null || path.isEmpty()) {
				requestFailed(CoreModelMessages.getString("CModule.2"), null); //$NON-NLS-1$
			} else {
				if (path.equals(getSymbolsFileName())) {
					try {
						((ICDISharedLibrary) fCDIObject).loadSymbols();
					} catch (CDIException e) {
						targetRequestFailed(e.getMessage(), null);
					}
				} else {
					String message = MessageFormat.format( //
							CoreModelMessages.getString("CModule.5"), //$NON-NLS-1$ 
							new Object[] { path.toString() });
					targetRequestFailed(message, null);
				}
			}
		}
	}
}
