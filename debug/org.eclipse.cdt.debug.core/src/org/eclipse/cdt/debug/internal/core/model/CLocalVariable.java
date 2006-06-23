/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model; 

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration2;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.debug.core.DebugException;
 

public class CLocalVariable extends CVariable {

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
		 * The underlying CDI variable.
		 */
		private ICDIVariable fCDIVariable;

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
			setCDIVariable( (varObject instanceof ICDIVariable) ? (ICDIVariable)varObject : null );
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

		private synchronized ICDIVariable getCDIVariable() throws DebugException {
			if ( fCDIVariable == null ) {
				try {
					fCDIVariable = ((CStackFrame)getStackFrame()).getCDIStackFrame().createLocalVariable( (ICDILocalVariableDescriptor)getCDIVariableObject() );
				}
				catch( CDIException e ) {
					requestFailed( e.getMessage(), null );
				}
			}
			return fCDIVariable;
		}

		private void setCDIVariable( ICDIVariable variable ) {
			fCDIVariable = variable;
		}

		private ICDIVariableDescriptor getCDIVariableObject() {
			if ( fCDIVariable != null ) {
				return fCDIVariable;
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
				if ( destroy && fCDIVariable != null )
					fCDIVariable.dispose();
			}
			catch( CDIException e ) {
				logError( e.getMessage() );
			}
			invalidateValue();
			setCDIVariable( null );
			if ( fType != null )
				fType.dispose();
			fType = null;
		}

		public void dispose( boolean destroy ) {
			invalidate( destroy );
		}

		public boolean isSameVariable( ICDIVariable cdiVar ) {
			return ( fCDIVariable != null ) ? fCDIVariable.equals( cdiVar ) : false;
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
			ICDIVariable cdiVariable = null;
			try {
				cdiVariable = getCDIVariable();
				if ( cdiVariable != null )
					cdiVariable.setValue( expression );
				else
					requestFailed( CoreModelMessages.getString( "CModificationVariable.0" ), null ); //$NON-NLS-1$
			}
			catch( CDIException e ) {
				targetRequestFailed( e.getMessage(), null );
			}
		}

		public synchronized ICValue getValue() throws DebugException {
			if ( fValue.equals( CValueFactory.NULL_VALUE ) ) {
				ICDIVariable var = getCDIVariable();
				if ( var != null ) {
					try {
						ICDIValue cdiValue = var.getValue();
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
						if (getCDITarget().getConfiguration() instanceof ICDITargetConfiguration2 &&
								((ICDITargetConfiguration2)getCDITarget().getConfiguration()).supportsRuntimeTypeIdentification())
							fType = null; // When the debugger supports RTTI getting a new value may also mean a new type.
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
			ICDIVariable var = getCDIVariable();
			if ( var != null ) {
				try {
					return var.isEditable();
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
	 * Constructor for CLocalVariable. 
	 */
	public CLocalVariable( CDebugElement parent, ICDIVariableDescriptor cdiVariableObject, String errorMessage ) {
		super( parent, cdiVariableObject, errorMessage );
	}

	/** 
	 * Constructor for CLocalVariable. 
	 */
	public CLocalVariable( CDebugElement parent, ICDIVariableDescriptor cdiVariableObject ) {
		super( parent, cdiVariableObject );
	}

	protected void createOriginal( ICDIVariableDescriptor vo ) {
		if ( vo != null ) {
			setName( vo.getName() );
			setOriginal( new InternalVariable( this, vo ) );
		}
	}
}
