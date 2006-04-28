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

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.model.CVariableFormat;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;

/**
 * Represents a register in the CDI model.
 */
public class CRegister extends CVariable implements IRegister {

	private class InternalVariable implements IInternalVariable {
		
		/**
		 * The enclosing <code>CVariable</code> instance.
		 */
		private CVariable fVariable;

		/**
		 * The CDI variable object this variable is based on.
		 */
		private ICDIVariableDescriptor fCDIVariableObject;

		/**
		 * The underlying CDI register.
		 */
		private ICDIRegister fCDIRegister;

		/**
		 * The type of this variable.
		 */
		private CType fType;

		/**
		 * The expression used to eveluate the value of this variable.
		 */
		private String fQualifiedName;

		/**
		 * The cache of the current value.
		 */
		private ICValue fValue = CValueFactory.NULL_VALUE;

		/**
		 * The change flag.
		 */
		private boolean fChanged = false;

		/**
		 * Constructor for InternalVariable.
		 */
		InternalVariable( CVariable var, ICDIVariableDescriptor varObject ) {
			setVariable( var );
			setCDIVariableObject( varObject );
			setCDIRegister( (varObject instanceof ICDIRegister) ? (ICDIRegister)varObject : null );
		}

		public IInternalVariable createShadow( int start, int length ) throws DebugException {
			IInternalVariable iv = null;
			try {
				iv = new InternalVariable( getVariable(), getCDIVariableObject().getVariableDescriptorAsArray( start, length ) );
			}
			catch( CDIException e ) {
				requestFailed( e.getMessage(), null );
			}
			return iv;
		}

		public IInternalVariable createShadow( String type ) throws DebugException {
			IInternalVariable iv = null;
			try {
				iv = new InternalVariable( getVariable(), getCDIVariableObject().getVariableDescriptorAsType( type ) );
			}
			catch( CDIException e ) {
				requestFailed( e.getMessage(), null );
			}
			return iv;
		}

		private synchronized ICDIRegister getCDIRegister() throws DebugException {
			if ( fCDIRegister == null ) {
				try {
					fCDIRegister = getCDITarget().createRegister( (ICDIRegisterDescriptor)getCDIVariableObject() );
				}
				catch( CDIException e ) {
					requestFailed( e.getMessage(), null );
				}
			}
			return fCDIRegister;
		}

		private void setCDIRegister( ICDIRegister register ) {
			fCDIRegister = register;
		}

		private ICDIVariableDescriptor getCDIVariableObject() {
			if ( fCDIRegister != null ) {
				return fCDIRegister;
			}
			return fCDIVariableObject;
		}

		private void setCDIVariableObject( ICDIVariableDescriptor variableObject ) {
			fCDIVariableObject = variableObject;
		}

		public String getQualifiedName() throws DebugException {
			if ( fQualifiedName == null ) {
				try {
					fQualifiedName = (fCDIVariableObject != null) ? fCDIVariableObject.getQualifiedName() : null;
				}
				catch( CDIException e ) {
					requestFailed( e.getMessage(), null );
				}
			}
			return fQualifiedName;
		}

		public CType getType() throws DebugException {
			if ( fType == null ) {
				ICDIVariableDescriptor varObject = getCDIVariableObject();
				if ( varObject != null ) {
					synchronized( this ) {
						if ( fType == null ) {
							try {
								fType = new CType( varObject.getType() );
							}
							catch( CDIException e ) {
								requestFailed( e.getMessage(), null );
							}
						}
					}
				}
			}
			return fType;
		}

		private synchronized void invalidate( boolean destroy ) {
			try {
				if ( destroy && fCDIRegister != null )
					fCDIRegister.dispose();
			}
			catch( CDIException e ) {
				logError( e.getMessage() );
			}
			invalidateValue();
			setCDIRegister( null );
			if ( fType != null )
				fType.dispose();
			fType = null;
		}

		public void dispose( boolean destroy ) {
			invalidate( destroy );
		}

		public boolean isSameVariable( ICDIVariable cdiVar ) {
			return ( fCDIRegister != null ) ? fCDIRegister.equals( cdiVar ) : false;
		}

		public int sizeof() {
			if ( getCDIVariableObject() != null ) {
				try {
					return getCDIVariableObject().sizeof();
				}
				catch( CDIException e ) {
				}
			}
			return 0;
		}

		public boolean isArgument() {
			return ( getCDIVariableObject() instanceof ICDIArgumentDescriptor );
		}

		public void setValue( String expression ) throws DebugException {
			ICDIRegister cdiRegister = null;
			try {
				cdiRegister = getCDIRegister();
				if ( cdiRegister != null )
					cdiRegister.setValue( expression );
				else
					requestFailed( CoreModelMessages.getString( "CModificationVariable.0" ), null ); //$NON-NLS-1$
			}
			catch( CDIException e ) {
				targetRequestFailed( e.getMessage(), null );
			}
		}

		public synchronized ICValue getValue() throws DebugException {
			if ( fValue.equals( CValueFactory.NULL_VALUE ) ) {
				ICDIRegister reg = getCDIRegister();
				if ( reg != null ) {
					try {
						ICDIValue cdiValue = reg.getValue( getCurrentStackFrame().getCDIStackFrame() );
						if ( cdiValue != null ) {
							ICDIType cdiType = cdiValue.getType();
							if ( cdiValue instanceof ICDIArrayValue && cdiType != null ) {
								ICType type = new CType( cdiType );
								if ( type.isArray() ) {
									int[] dims = type.getArrayDimensions();
									if ( dims.length > 0 && dims[0] > 0 )
										fValue = CValueFactory.createIndexedValue( getVariable(), (ICDIArrayValue)cdiValue, 0, dims[0] );
								}
							}
							else {
								fValue = CValueFactory.createValue( getVariable(), cdiValue );
							}
						}
					}
					catch( CDIException e ) {
						requestFailed( e.getMessage(), e );
					}
				}
			}
			return fValue;
		}
		
		public void invalidateValue() {
			if ( fValue instanceof AbstractCValue ) {
				((AbstractCValue)fValue).dispose();
				fValue = CValueFactory.NULL_VALUE;
			}
		}

		public boolean isChanged() {
			return fChanged;
		}

		public synchronized void setChanged( boolean changed ) {
			if ( changed ) {
				invalidateValue();
			}
			if ( fValue instanceof AbstractCValue ) {
				((AbstractCValue)fValue).setChanged( changed );
			}
			fChanged = changed;
		}

		public synchronized void preserve() {
			setChanged( false );
			if ( fValue instanceof AbstractCValue ) {
				((AbstractCValue)fValue).preserve();
			}
		}

		CVariable getVariable() {
			return fVariable;
		}

		private void setVariable( CVariable variable ) {
			fVariable = variable;
		}

		public void resetValue() {
			if ( fValue instanceof AbstractCValue ) {
				((AbstractCValue)fValue).reset();
			}
		}

		public boolean isEditable() throws DebugException {
			ICDIRegister reg = getCDIRegister();
			if ( reg != null ) {
				try {
					return reg.isEditable();
				}
				catch( CDIException e ) {
				}
			}
			return false;
		}
		/**
		 * Compares the underlying variable objects.
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals( Object obj ) {
			if ( obj instanceof InternalVariable ) {
				return getCDIVariableObject().equals( ((InternalVariable)obj).getCDIVariableObject() );
			}
			return false;
		}

		public boolean isSameDescriptor( ICDIVariableDescriptor desc ) {
			return getCDIVariableObject().equals( desc );
		}
	}

	/**
	 * Constructor for CRegister.
	 */
	protected CRegister( CRegisterGroup parent, IRegisterDescriptor descriptor ) {
		super( parent, ((CRegisterDescriptor)descriptor).getCDIDescriptor() );
		setFormat( CVariableFormat.getFormat( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT ) ) );
	}

	/**
	 * Constructor for CRegister.
	 */
	protected CRegister( CRegisterGroup parent, IRegisterDescriptor descriptor, String message ) {
		super( parent, ((CRegisterDescriptor)descriptor).getCDIDescriptor(), message );
		setFormat( CVariableFormat.getFormat( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT ) ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegister#getRegisterGroup()
	 */
	public IRegisterGroup getRegisterGroup() throws DebugException {
		return (IRegisterGroup)getParent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.CVariable#isBookkeepingEnabled()
	 */
	protected boolean isBookkeepingEnabled() {
		boolean result = false;
		try {
			result = getLaunch().getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, false );
		}
		catch( CoreException e ) {
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#canEnableDisable()
	 */
	public boolean canEnableDisable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
	 */
	public void handleDebugEvents( ICDIEvent[] events ) {
		for( int i = 0; i < events.length; i++ ) {
			ICDIEvent event = events[i];
			if ( event instanceof ICDIResumedEvent ) {
				ICDIObject source = event.getSource();
				if ( source != null ) {
					ICDITarget cdiTarget = source.getTarget();
					if (  getCDITarget().equals( cdiTarget ) ) {
						setChanged( false );
					}
				}
			}
		}
		super.handleDebugEvents( events );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#dispose()
	 */
	public void dispose() {
		internalDispose( true );
		setDisposed( true );
	}

	protected ICStackFrame getStackFrame() {
		ICStackFrame frame = super.getStackFrame();
		if (frame == null)
			frame = getCurrentStackFrame();
		return frame;
	}

	protected CStackFrame getCurrentStackFrame() {
		return ((CDebugTarget)getDebugTarget()).getRegisterManager().getCurrentFrame();
	}

	protected void createOriginal( ICDIVariableDescriptor vo ) {
		if ( vo != null ) {
			setName( vo.getName() );
			setOriginal( new InternalVariable( this, vo ) );
		}
	}
}
