/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.IAddressFactory2;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemorySpaceEncoder;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemorySpaceManagement;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlock;
import org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CExpression;
import org.eclipse.cdt.debug.internal.core.model.CMemoryBlockExtension;
import org.eclipse.cdt.debug.internal.core.model.CStackFrame;
import org.eclipse.cdt.debug.internal.core.model.CThread;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.icu.text.MessageFormat;

/**
 * Implements the memory retrieval features based on the CDI model.
 */
public class CMemoryBlockRetrievalExtension extends PlatformObject implements IMemorySpaceAwareMemoryBlockRetrieval {

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

	private void parseMementoExprItem(Element element, List<String> expressions, List<String> memorySpaceIDs) {
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
			List<String> expressions = new ArrayList<String>();
			List<String> memorySpaceIDs = new ArrayList<String>();
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
			createMemoryBlocks( expressions.toArray( new String[expressions.size()]) ,
								memorySpaceIDs.toArray( new String[memorySpaceIDs.size()]));
					
			return;
		}
		abort( InternalDebugCoreMessages.getString( "CMemoryBlockRetrievalExtension.3" ), null ); //$NON-NLS-1$
	}

	/**
	 * Convert a simple literal address (e.g., "0x1000") to a BigInteger value
	 * using the debug target's address factory.
	 * 
	 * We throw a NumberFormatException if the string is not a valid literal
	 * address. If the backend implements the new&improved factory interface,
	 * we'll throw a NumberFormatException if the string is a literal address
	 * but is outside of the valid range. Old address factories will simply
	 * truncate the value.
	 * 
	 * @param expression
	 * @return
	 * @throws DebugException if target not available
	 */
	private BigInteger evaluateLiteralAddress(String addr) throws DebugException {
		CDebugTarget target = getDebugTarget();
		if (target == null) {
			throw new DebugException(new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, 
					InternalDebugCoreMessages.getString("CMemoryBlockRetrievalExtension.CDebugTarget_not_available"), null)); //$NON-NLS-1$
		}
		IAddressFactory addrFactory = target.getAddressFactory();
		if (addrFactory instanceof IAddressFactory2) {
			return ((IAddressFactory2)addrFactory).createAddress(addr, false).getValue();
		}
		else {
			return addrFactory.createAddress(addr).getValue();
		}
	}
	
	private void createMemoryBlocks( String[] expressions, String[] memorySpaceIDs ) {
		List<CMemoryBlockExtension> list = new ArrayList<CMemoryBlockExtension>( expressions.length );
		for ( int i = 0; i < expressions.length; ++i ) {
			try {
				IAddress address = getDebugTarget().getAddressFactory().createAddress( expressions[i] );
				if ( address != null ) {
					if (memorySpaceIDs[i] == null) {
						list.add( new CMemoryBlockExtension( getDebugTarget(), address.toHexAddressString(), address.getValue() ) );
					} else {
						list.add( new CMemoryBlockExtension( getDebugTarget(), expressions[i], address.getValue(), memorySpaceIDs[i] ) );
					}
				}
			} catch (NumberFormatException exc) {
				CDebugCorePlugin.log(exc);
			}
		}
		DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks( list.toArray( new IMemoryBlock[list.size()] ) );
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

				String memorySpaceID = null;
				if (memBlockExt instanceof IMemorySpaceAwareMemoryBlock) {
					memorySpaceID = ((IMemorySpaceAwareMemoryBlock)memBlockExt).getMemorySpaceID();
				}
				BigInteger addrBigInt = memBlockExt.getBigBaseAddress();
			
				Element child = document.createElement( MEMORY_BLOCK_EXPRESSION );
				child.setAttribute( ATTR_MEMORY_BLOCK_EXPRESSION_TEXT, "0x" + addrBigInt.toString(16) ); //$NON-NLS-1$
				exprItem.appendChild( child );

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
		return getMemoryBlock(expression, selected,  null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval#getExtendedMemoryBlock(java.lang.String, java.lang.Object, java.lang.String)
	 */
	public IMemorySpaceAwareMemoryBlock getMemoryBlock( String expression, Object selected, String memorySpaceID ) throws DebugException {
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
					return new CMemoryBlockExtension((CDebugTarget)target, expression, evaluateLiteralAddress(expression), memorySpaceID);
				} catch (NumberFormatException nfexc) {}

				// OK, expression is not a simple literal address; keep trucking and try to resolve as expression					
				CStackFrame frame = getStackFrame( debugElement );
				if ( frame != null ) {
					// Get the address of the expression
					ICDIExpression cdiExpression = frame.getCDITarget().createExpression( expression );
					exp = new CExpression( frame, cdiExpression, null );
					IValue value = exp.getValue();
					if ( value instanceof ICValue ) {
						ICType type = ((ICValue)value).getType();
						if ( type != null ) {
							// get the address for the expression, allow all types
							String rawExpr = exp.getExpressionString();
							String voidExpr = "(void *)(" + rawExpr + ')'; //$NON-NLS-1$
							String attempts[] = { rawExpr, voidExpr };
							for (int i = 0; i < attempts.length; i++) {
								String expr = attempts[i];
								address = frame.evaluateExpressionToString(expr);
								if (address != null) {
									try {
										BigInteger a = (address.startsWith("0x")) ? new BigInteger(address.substring(2), 16) : new BigInteger(address); //$NON-NLS-1$
										return new CMemoryBlockExtension((CDebugTarget) target, expression, a, memorySpaceID);
									} catch (NumberFormatException e) {
										// not pointer? lets cast it to void*
										if (i == 0)
											continue;
										throw e;
									}
								}
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
			msg = MessageFormat.format( InternalDebugCoreMessages.getString( "CMemoryBlockRetrievalExtension.0" ), new String[] { expression } ); //$NON-NLS-1$
		}
		finally {
			if (exp != null) {
				exp.dispose();
			}
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
		// Fire a terminate event so our hosts can clean up. See 255120 and 283586
		DebugPlugin.getDefault().fireDebugEventSet( new DebugEvent[]{new DebugEvent( this, DebugEvent.TERMINATE )}); 

		// Minimize leaks in case we are ourselves are leaked
		fDebugTarget = null;
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
	 * @see org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval#getMemorySpaces(java.lang.Object, org.eclipse.cdt.debug.internal.core.model.provisional.IRequestListener)
	 */
	public void getMemorySpaces(final Object context, GetMemorySpacesRequest request) {
		// We're not very asynchronous in CDI. DSF is another story. Also, note
		// that we ignore the context. That's because we know that there's only
		// one instance of this object per process object, and all elements of
		// the project object (process, threads, frames) will have the same
		// memory spaces
		request.setMemorySpaces(getMemorySpaces());
		request.done();
	}

	/**
	 * This variant is called by code that is CDI-specific. This method and its
	 * uses predate the introduction of the DSF/CDI-agnostic
	 * IMemorySpaceAwareMemoryBlockRetrieval
	 * 
	 * @return the memory spaces available in this debug session
	 */
	public String [] getMemorySpaces(){
		if (fDebugTarget != null) {
			ICDITarget cdiTarget = fDebugTarget.getCDITarget(); 
			if (cdiTarget instanceof ICDIMemorySpaceManagement)
				return ((ICDIMemorySpaceManagement)cdiTarget).getMemorySpaces();
		}
		
		return new String[0];
	}

	/**
	 * The default encoding of an {expression, memory space ID} pair into a
	 * string. A CDI client can provide custom decoding by implementing
	 * ICDIMemorySpaceEncoder
	 */
	public static String encodeAddressDefault(String expression, String memorySpaceID) {
		return memorySpaceID + ':' + expression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval#encodeAddress(java.math.BigInteger, java.lang.String)
	 */
	public String encodeAddress(final String expression, final String memorySpaceID) {
		// See if the CDI client provides customized encoding/decoding
		if (fDebugTarget != null) {
			ICDITarget cdiTarget = fDebugTarget.getCDITarget();
			if (cdiTarget instanceof ICDIMemorySpaceEncoder) {
				return ((ICDIMemorySpaceEncoder)cdiTarget).encodeAddress(expression, memorySpaceID);
			}
		}

		// Nope; use default encoding
		return encodeAddressDefault(expression, memorySpaceID);
	}

	/*
	 * The default decoding of a string into an {expression, memory space ID}
	 * pair. A CDI client can provide custom decoding by implementing ICDIMemorySpaceEncoder 
	 */
	public static DecodeResult decodeAddressDefault(String str) throws CoreException {
		int index = str.lastIndexOf(':');
		
		// minimum is "<space>:<expression>"
		if ((index == -1) || (index == str.length()-1)) {
			IStatus s = new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), CDebugCorePlugin.INTERNAL_ERROR, InternalDebugCoreMessages.getString( "CMemoryBlockRetrievalExtension.5" ), null ); //$NON-NLS-1$
			throw new CoreException( s );
		}

		final String memorySpaceID = str.substring(0, index);
		final String expression = str.substring(index+1);

		return new DecodeResult() {
			public String getMemorySpaceId() { return memorySpaceID; }
			public String getExpression() { return expression; }
		};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval#decodeAddress(java.lang.String, java.lang.StringBuffer)
	 */
	public DecodeResult decodeAddress(final String str) throws CoreException {
		
		// See if the CDI client provides customized encoding/decoding
		if (fDebugTarget != null) {
			ICDITarget cdiTarget = fDebugTarget.getCDITarget();
			if (cdiTarget instanceof ICDIMemorySpaceEncoder) {
				try {
					final ICDIMemorySpaceEncoder.DecodeResult result = ((ICDIMemorySpaceEncoder)cdiTarget).decodeAddress(str);
					return new DecodeResult() {
						public String getMemorySpaceId() { return result.getMemorySpaceId(); }
						public String getExpression() { return result.getExpression(); }
					};
				}
				catch (CDIException exc) {
					IStatus s = new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), CDebugCorePlugin.INTERNAL_ERROR, InternalDebugCoreMessages.getString( "CMemoryBlockRetrievalExtension.invalid_encoded_addresses" ), exc); //$NON-NLS-1$
					throw new CoreException(s);
 
				}
			}
		}

		// Nope; use default decoding
		return decodeAddressDefault(str);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval#creatingBlockRequiresMemorySpaceID()
	 */
	public boolean creatingBlockRequiresMemorySpaceID() {
		// A behavioral control we're not extending to CDI clients, but is being
		// extended to DSF ones.
		return false;
	}
}
