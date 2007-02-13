/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core; 

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemorySpaceManagement;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CExpression;
import org.eclipse.cdt.debug.internal.core.model.CMemoryBlockExtension;
import org.eclipse.cdt.debug.internal.core.model.CStackFrame;
import org.eclipse.cdt.debug.internal.core.model.CThread;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implements the memory retrieval features based on the CDI model.
 */
public class CMemoryBlockRetrievalExtension extends PlatformObject implements IMemoryBlockRetrievalExtension {

	private static final String MEMORY_BLOCK_EXPRESSION_LIST = "memoryBlockExpressionList"; //$NON-NLS-1$
	private static final String MEMORY_BLOCK_EXPRESSION_ITEM = "memoryBlockExpressionItem"; //$NON-NLS-1$	
	private static final String MEMORY_BLOCK_EXPRESSION = "expression"; //$NON-NLS-1$
	private static final String MEMORY_BLOCK_MEMSPACEID = "memorySpaceID"; //$NON-NLS-1$
	private static final String ATTR_MEMORY_BLOCK_MEMSPACEID_TEXT = "text"; //$NON-NLS-1$	
	private static final String ATTR_MEMORY_BLOCK_EXPRESSION_TEXT = "text"; //$NON-NLS-1$

	CDebugTarget fDebugTarget;

	/** 
	 * Constructor for CMemoryBlockRetrievalExtension. 
	 */
	public CMemoryBlockRetrievalExtension( CDebugTarget debugTarget ) {
		fDebugTarget = debugTarget;
	}

	protected CDebugTarget getDebugTarget() {
		return fDebugTarget;
	}
	
	public void initialize() {
		ILaunchConfiguration config = getDebugTarget().getLaunch().getLaunchConfiguration();
		try {
			String memento = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_MEMORY_BLOCKS, "" ); //$NON-NLS-1$
			if ( memento != null && memento.trim().length() != 0 )
				initializeFromMemento( memento );
		}
		catch( CoreException e ) {
			CDebugCorePlugin.log( e );
		}
	}

	private void parseMementoExprItem(Element element, List expressions, List memorySpaceIDs) {
		NodeList list = element.getChildNodes();
		int length = list.getLength();
		String exp = null;
		String memorySpaceID = null;
		for( int i = 0; i < length; ++i ) {
			Node node = list.item( i );
			if ( node.getNodeType() == Node.ELEMENT_NODE ) {
				Element entry = (Element)node;
				if ( entry.getNodeName().equalsIgnoreCase( MEMORY_BLOCK_EXPRESSION ) ) {
					exp = entry.getAttribute( ATTR_MEMORY_BLOCK_EXPRESSION_TEXT );
				} else if ( entry.getNodeName().equalsIgnoreCase( MEMORY_BLOCK_MEMSPACEID ) ) {
					memorySpaceID = entry.getAttribute( ATTR_MEMORY_BLOCK_MEMSPACEID_TEXT );
				}
			}
		}
		if (exp != null) {
			expressions.add( exp );
			memorySpaceIDs.add( memorySpaceID );
		}
	}

	
	private void initializeFromMemento( String memento ) throws CoreException {
		Element root = DebugPlugin.parseDocument( memento );
		if ( root.getNodeName().equalsIgnoreCase( MEMORY_BLOCK_EXPRESSION_LIST ) ) {
			List expressions = new ArrayList();
			List memorySpaceIDs = new ArrayList();
			NodeList list = root.getChildNodes();
			int length = list.getLength();
			for( int i = 0; i < length; ++i ) {
				Node node = list.item( i );
				if ( node.getNodeType() == Node.ELEMENT_NODE ) {
					Element entry = (Element)node;
					if ( entry.getNodeName().equalsIgnoreCase( MEMORY_BLOCK_EXPRESSION_ITEM ) ) {
						parseMementoExprItem(entry, expressions, memorySpaceIDs);
					}
				}
			}
			createMemoryBlocks( (String[])expressions.toArray( new String[expressions.size()]) ,
								(String[])memorySpaceIDs.toArray( new String[memorySpaceIDs.size()]));
					
			return;
		}
		abort( InternalDebugCoreMessages.getString( "CMemoryBlockRetrievalExtension.3" ), null ); //$NON-NLS-1$
	}

	private void createMemoryBlocks( String[] expressions, String[] memorySpaceIDs ) {
		ArrayList list = new ArrayList( expressions.length );
		for ( int i = 0; i < expressions.length; ++i ) {
			IAddress address = getDebugTarget().getAddressFactory().createAddress( expressions[i] );
			if ( address != null ) {
				if (memorySpaceIDs[i] == null) {
					list.add( new CMemoryBlockExtension( getDebugTarget(), address.toHexAddressString(), address.getValue() ) );
				} else {
					list.add( new CMemoryBlockExtension( getDebugTarget(), address.getValue(), memorySpaceIDs[i] ) );
				}
			}
		}
		DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks( (IMemoryBlock[])list.toArray( new IMemoryBlock[list.size()] ) );
	}

	public String getMemento() throws CoreException {
		IMemoryBlock[] blocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks( getDebugTarget() );
		Document document = DebugPlugin.newDocument();
		Element exprList = document.createElement( MEMORY_BLOCK_EXPRESSION_LIST );
		for ( int i = 0; i < blocks.length; ++i ) {
			if ( blocks[i] instanceof IMemoryBlockExtension ) {
				IMemoryBlockExtension memBlockExt = (IMemoryBlockExtension)blocks[i];
				Element exprItem = document.createElement( MEMORY_BLOCK_EXPRESSION_ITEM );
				exprList.appendChild(exprItem);

				BigInteger addrBigInt = null;
				String memorySpaceID = null;
				if (hasMemorySpaces()) {
					// Can't tell if block was created with a memory-space/address or with an expression.
					// Assume the former and let an exception in the decoding tell us otherwise
					ICDITarget cdiTarget = fDebugTarget.getCDITarget();
					try {
						StringBuffer sbuf = new StringBuffer();
						addrBigInt = ((ICDIMemorySpaceManagement)cdiTarget).stringToAddress(memBlockExt.getExpression(), sbuf);
						if (addrBigInt == null) {
							// Client wants our default decoding; minimum is "<space>:0x?"
							addrBigInt = stringToAddress(memBlockExt.getExpression(), sbuf);
						}
						memorySpaceID = sbuf.toString();
					} 
					catch( CDIException e ) { // thrown by CDI client decoding method
						addrBigInt = null;
					}
					catch (CoreException e) {
						addrBigInt = null; // thrown by our decoding method
					}
				}
				
				Element child = document.createElement( MEMORY_BLOCK_EXPRESSION );
				try {
					if (addrBigInt != null && memorySpaceID != null) {
						child.setAttribute( ATTR_MEMORY_BLOCK_EXPRESSION_TEXT, addrBigInt.toString() );						
					} 
					else {
						child.setAttribute( ATTR_MEMORY_BLOCK_EXPRESSION_TEXT, memBlockExt.getBigBaseAddress().toString() );
					}
					exprItem.appendChild( child );
				}
				catch( DebugException e ) {
					CDebugCorePlugin.log( e.getStatus() );
				}

				if (memorySpaceID != null) { 
					child = document.createElement( MEMORY_BLOCK_MEMSPACEID );
					child.setAttribute( ATTR_MEMORY_BLOCK_MEMSPACEID_TEXT, memorySpaceID);
					exprItem.appendChild( child );
				}
			}
		}
		document.appendChild( exprList );
		return DebugPlugin.serializeDocument( document );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtensionRetrieval#getExtendedMemoryBlock(java.lang.String, org.eclipse.debug.core.model.IDebugElement)
	 */
	public IMemoryBlockExtension getExtendedMemoryBlock( String expression, Object selected ) throws DebugException {
		String address = null;
		CExpression exp = null;
		String msg = null;
		try {
			if (selected instanceof IDebugElement) {
				IDebugElement debugElement = (IDebugElement)selected;
				IDebugTarget target = debugElement.getDebugTarget();
				if (!(target instanceof CDebugTarget)) {
					throw new DebugException( new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, msg, null ) );
				}
				
				// See if the expression is a simple numeric value; if it is, we can avoid some costly
				// processing (calling the backend to resolve the expression)
				try {
					IAddressFactory addrFactory = ((CDebugTarget)target).getAddressFactory();
					String hexstr = addrFactory.createAddress(expression).toString(16);
					return new CMemoryBlockExtension((CDebugTarget)target, expression, new BigInteger(hexstr, 16));
				} catch (NumberFormatException nfexc) {
					// OK, expression is not a simple, absolute numeric value; keep trucking and try to resolve as expression
				}
				
				CStackFrame frame = getStackFrame( debugElement );
				if ( frame != null ) {
					// We need to provide a better way for retrieving the address of expression
					ICDIExpression cdiExpression = frame.getCDITarget().createExpression( expression );
					exp = new CExpression( frame, cdiExpression, null );
					IValue value = exp.getValue();
					if ( value instanceof ICValue ) {
						ICType type = ((ICValue)value).getType();
						if ( type != null && (type.isPointer() || type.isIntegralType()) ) {
							address = value.getValueString();
							exp.dispose();
							if ( address != null ) {
								// ???
								BigInteger a = ( address.startsWith( "0x" ) ) ? new BigInteger( address.substring( 2 ), 16 ) : new BigInteger( address ); //$NON-NLS-1$
								return new CMemoryBlockExtension( (CDebugTarget)target, expression, a );
							}
						}
						else {
							msg = MessageFormat.format( InternalDebugCoreMessages.getString( "CMemoryBlockRetrievalExtension.1" ), new String[] { expression } ); //$NON-NLS-1$
						}
					}
					else {
						msg = MessageFormat.format( InternalDebugCoreMessages.getString( "CMemoryBlockRetrievalExtension.2" ), new String[] { expression } ); //$NON-NLS-1$
					}
				}
			}
		}
		catch( CDIException e ) {
			msg = e.getMessage();
		}
		catch( NumberFormatException e ) {
			msg = MessageFormat.format( InternalDebugCoreMessages.getString( "CMemoryBlockRetrievalExtension.0" ), new String[] { expression, address } ); //$NON-NLS-1$
		}
		throw new DebugException( new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, msg, null ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
	 */
	public boolean supportsStorageRetrieval() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long, long)
	 */
	public IMemoryBlock getMemoryBlock( long startAddress, long length ) throws DebugException {
		String expression = Long.toHexString(startAddress);
		BigInteger address = new BigInteger(expression, 16);
		expression = "0x" + expression; //$NON-NLS-1$
		return new CMemoryBlockExtension( getDebugTarget(), expression, address );
	}

	/**
	 * Variant of getExtendedMemoryBlock that takes a memory space ID. Note that unlike that one, 
	 * this method is not part of IMemoryBlockRetrievalExtension; it is not exercised by the 
	 * platform. We invoke it internally in CDT from our hook into the platform's "add memory 
	 * monitor" action.
	 *   
	 * @param address - a numric address value, hex or decimal. An expression 
	 * (even something simple like 10000 +1) is not allowed.
	 * @param memorySpaceID - identifies the memory space; cannot be null.
	 * @param selected - the object selected in the Debug view
	 * @return
	 * @throws DebugException
	 */
	public IMemoryBlockExtension getMemoryBlockWithMemorySpaceID( String address, String memorySpaceID, Object selected ) throws DebugException {
		String msg = null;
		try {
			if (selected instanceof IDebugElement) {
				IDebugElement debugElement = (IDebugElement)selected;
				IDebugTarget target = debugElement.getDebugTarget();
				if ( target instanceof CDebugTarget ) {
					if ( address != null ) {
						BigInteger addr = ( address.startsWith( "0x" ) ) ? new BigInteger( address.substring( 2 ), 16 ) : new BigInteger( address ); //$NON-NLS-1$
						return new CMemoryBlockExtension( (CDebugTarget)target, addr, memorySpaceID );
					}
				}
			}
		}
		catch( NumberFormatException e ) {
			msg = MessageFormat.format( InternalDebugCoreMessages.getString( "CMemoryBlockRetrievalExtension.4" ), new String[] { address } ); //$NON-NLS-1$
		}
		throw new DebugException( new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, msg, null ) );
	}
	
	private CStackFrame getStackFrame( IDebugElement selected ) throws DebugException {
		if ( selected instanceof CStackFrame ) {
			return (CStackFrame)selected;
		}
		if ( selected instanceof CThread ) {
			IStackFrame frame = ((CThread)selected).getTopStackFrame();
			if ( frame instanceof CStackFrame )
				return (CStackFrame)frame;
		}
		return null;
	}

	public void save() {
		ILaunchConfiguration config = getDebugTarget().getLaunch().getLaunchConfiguration();
		try {
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_MEMORY_BLOCKS, getMemento() );
			wc.doSave();
		}
		catch( CoreException e ) {
			CDebugCorePlugin.log( e.getStatus() );
		}
	}

	/**
	 * Throws an internal error exception
	 */
	private void abort( String message, Throwable e ) throws CoreException {
		IStatus s = new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), CDebugCorePlugin.INTERNAL_ERROR, message, e );
		throw new CoreException( s );
	}

	public void dispose() {
	}

	/**
	 * Checks the CDI backend to see is memory spaces are supported and actually
	 * available for the target process.
	 * 
	 * @return true if the backend supports memory spaces
	 */
	public boolean hasMemorySpaces() {
		return getMemorySpaces().length > 0;
	}

	/**
	 * Get the list of available memory spaces from the CDI backend
	 * 
	 * @return an array of memory space identifiers
	 */
	public String [] getMemorySpaces() {
		ICDITarget cdiTarget = fDebugTarget.getCDITarget(); 
		if (cdiTarget instanceof ICDIMemorySpaceManagement)
			return ((ICDIMemorySpaceManagement)cdiTarget).getMemorySpaces(); 
		
		return new String[0];
	}
	
	/* 
	 * static implementation of
	 *    @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemorySpaceManagement#addressToString(java.math.BigInteger, java.lang.String) 
	 * client may choose not to provide the encoding/decoding and instead use our built-in handling.  
	 * 
	 */
	public static String addressToString(BigInteger address, String memorySpaceID) {
		return memorySpaceID + ":0x" + address.toString(16);
	}

	/*
	 * static implementation of
	 * 	 @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemorySpaceManagement#stringToAddr(java.lang.String, java.math.BigInteger, java.lang.StringBuffer)
	 * client may choose not to provide the encoding/decoding and instead use our built-in handling.  
	 */
	public static BigInteger stringToAddress(String str, StringBuffer memorySpaceID_out) throws CoreException {
		int index = str.lastIndexOf(':');
		
		// minimum is "<space>:0x?"
		if (index == -1 || str.length() <= index + 3 || str.charAt(index+1) != '0' || str.charAt(index+2) != 'x') {
			IStatus s = new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), CDebugCorePlugin.INTERNAL_ERROR, InternalDebugCoreMessages.getString( "CMemoryBlockRetrievalExtension.5" ), null );
			throw new CoreException( s );
		}

		memorySpaceID_out.setLength(0);
		memorySpaceID_out.append(str.substring(0, index));
		return new BigInteger(str.substring(index+3), 16);
	}
}
