/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.model;


import java.text.MessageFormat;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIFormat;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.model.ICDebugElementErrorStatus;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.cdt.debug.core.model.ICastToArray;
import org.eclipse.cdt.debug.core.model.ICastToType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * 
 * A superclass for all variable classes.
 * 
 * @since Aug 9, 2002
 */
public abstract class CVariable extends CDebugElement 
								implements ICVariable,
										   ICDIEventListener,
										   ICastToType,
										   ICastToArray
{
	/**
	 * The instance of this class is created when the 'getCDIVariable' call throws an exception.
	 * 
	 * @since Jul 22, 2003
	 */
	public static class ErrorVariable implements ICDIVariable
	{
		private ICDIVariableObject fVariableObject;
		private Exception fException;

		public ErrorVariable( ICDIVariableObject varObject, Exception e )
		{
			fVariableObject = varObject;
			fException = e;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getStackFrame()
		 */
		public ICDIStackFrame getStackFrame() throws CDIException
		{
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getName()
		 */
		public String getName()
		{
			return ( fVariableObject != null ) ? fVariableObject.getName() : ""; //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getTypeName()
		 */
		public String getTypeName() throws CDIException
		{
			return ( fVariableObject != null ) ? fVariableObject.getTypeName() : ""; //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getType()
		 */
		public ICDIType getType() throws CDIException
		{
			return ( fVariableObject != null ) ? fVariableObject.getType() : null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getValue()
		 */
		public ICDIValue getValue() throws CDIException
		{
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#isEditable()
		 */
		public boolean isEditable() throws CDIException
		{
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(java.lang.String)
		 */
		public void setValue( String expression ) throws CDIException
		{
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(org.eclipse.cdt.debug.core.cdi.model.ICDIValue)
		 */
		public void setValue( ICDIValue value ) throws CDIException
		{
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setFormat(int)
		 */
		public void setFormat( int format ) throws CDIException
		{
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
		 */
		public ICDITarget getTarget()
		{
			return ( fVariableObject != null ) ? fVariableObject.getTarget() : null;
		}

		public Exception getException()
		{
			return fException;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#sizeof()
		 */
		public int sizeof() throws CDIException
		{
			return 0;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getQualifiedName()
		 */
		public String getQualifiedName() throws CDIException
		{
			return ( fVariableObject != null ) ? fVariableObject.getQualifiedName() : null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject)
		 */
		public boolean equals( ICDIVariableObject varObject )
		{
			return ( fVariableObject != null ) ? fVariableObject.equals( varObject ) : false;
		}
	}

	class InternalVariable
	{
		private ICDIVariableObject fCDIVariableObject;

		private ICDIVariable fCDIVariable;

		private Boolean fEditable = null;

		private ICType fType = null;

		private String fQualifiedName = null;

		public InternalVariable( ICDIVariableObject varObject )
		{
			setCDIVariableObject( varObject );
			setCDIVariable( ( varObject instanceof ICDIVariable ) ? (ICDIVariable)varObject : null );
		}

		protected synchronized ICDIVariable getCDIVariable() throws CDIException
		{
			if ( fCDIVariable == null )
			{
				try
				{
					if ( getCDIVariableObject() instanceof ICDIArgumentObject )
						fCDIVariable = getCDISession().getVariableManager().createArgument( (ICDIArgumentObject)getCDIVariableObject() );
					else
						fCDIVariable = getCDISession().getVariableManager().createVariable( getCDIVariableObject() );
				}
				catch( CDIException e )
				{
					fCDIVariable = new ErrorVariable( getCDIVariableObject(), e );
					setStatus( ICDebugElementErrorStatus.ERROR, MessageFormat.format( CoreModelMessages.getString( "CVariable.0" ), new String[] { e.getMessage() } ) ); //$NON-NLS-1$
				}
			}
			return fCDIVariable;
		}

		protected ICDIVariableObject getCDIVariableObject()
		{
			return fCDIVariableObject;
		}

		protected ICType getType() throws CDIException
		{
			if ( fType == null )
			{
				ICDIVariableObject varObject = getCDIVariableObject();
				if ( varObject != null )
					fType = new CType( varObject.getType() );
			}
			return fType;
		}

		protected boolean isEditable() throws CDIException
		{
			if ( fEditable == null )
			{
				ICDIVariableObject varObject = getCDIVariableObject();
				if ( varObject != null && !(varObject instanceof ErrorVariable) )
					fEditable = new Boolean( varObject.isEditable() );
			}
			return ( fEditable != null ) ? fEditable.booleanValue() : false;
		}

		protected boolean hasChildren() throws CDIException
		{
			CType type = (CType)getType();
			return ( type != null ) ? type.hasChildren() : false;
		}

		private void setCDIVariable( ICDIVariable variable )
		{
			fCDIVariable = variable;
		}

		private void setCDIVariableObject( ICDIVariableObject object )
		{
			fCDIVariableObject = object;
		}

		protected synchronized void invalidate()
		{
			try
			{
				if ( fCDIVariable != null && !(fCDIVariable instanceof ErrorVariable) )
					getCDISession().getVariableManager().destroyVariable( fCDIVariable );
			}
			catch( CDIException e )
			{
				logError( e.getMessage() );
			}
			setCDIVariable( null );
			if ( fType != null )
				fType.dispose();
			fType = null;
			fEditable = null;
		}

		protected void dispose()
		{
			invalidate();
			setCDIVariableObject( null );
		}

		protected boolean isSameVariable( ICDIVariable cdiVar )
		{
			return ( fCDIVariable != null ) ? fCDIVariable.equals( cdiVar ) : false; 
		}

		protected int sizeof()
		{
			if ( getCDIVariableObject() != null )
			{
				try
				{
					return getCDIVariableObject().sizeof();
				}
				catch( CDIException e )
				{
				}
			}
			return 0;
		}

		protected String getQualifiedName() throws CDIException
		{
			if ( fQualifiedName == null )
			{
				fQualifiedName = ( fCDIVariableObject != null ) ? fCDIVariableObject.getQualifiedName() : null;
			}
			return fQualifiedName;
		}

		public boolean hasErrors() {
			return ( fCDIVariable instanceof ErrorVariable );
		}
	}

	/**
	 * The parent object this variable is contained in.
	 */
	private CDebugElement fParent;

	/**
	 * The original internal variable.
	 */
	private InternalVariable fOriginal;
	
	/**
	 * The shadow internal variable used for casting.
	 */
	private InternalVariable fShadow = null;
	
	/**
	 * Cache of current value - see #getValue().
	 */
	protected ICValue fValue;

	/**
	 * The name of this variable.
	 */
	private String fName = null;

	/**
	 * Change flag.
	 */
	protected boolean fChanged = false;

	/**
	 * The current format of this variable.
	 */
	protected int fFormat = ICDIFormat.NATURAL;

	private boolean fIsEnabled = true;

	/*
	 * Temporary solution to avoid NPE in VariablesView. 
	 * This is fixed in the Eclipse 2.1.1 Maintenance Build.
	 */
	static protected IValue fDisabledValue = new IValue()
												{
													public String getReferenceTypeName() throws DebugException
													{
														return null;
													}

													public String getValueString() throws DebugException
													{
														return null;
													}

													public boolean isAllocated() throws DebugException
													{
														return false;
													}

													public IVariable[] getVariables() throws DebugException
													{
														return null;
													}

													public boolean hasVariables() throws DebugException
													{
														return false;
													}

													public String getModelIdentifier()
													{
														return CDebugCorePlugin.getUniqueIdentifier();
													}

													public IDebugTarget getDebugTarget()
													{
														return null;
													}

													public ILaunch getLaunch()
													{
														return null;
													}

													public Object getAdapter( Class adapter )
													{
														return null;
													}
													
												};
	/**
	 * Constructor for CVariable.
	 * @param target
	 */
	public CVariable( CDebugElement parent, ICDIVariableObject cdiVariableObject )
	{
		super( (CDebugTarget)parent.getDebugTarget() );
		fParent = parent;
		fIsEnabled = !enableVariableBookkeeping();
		fOriginal = createOriginal( cdiVariableObject );
		fShadow = null;
		if ( cdiVariableObject instanceof ErrorVariable )
			setStatus( ICDebugElementErrorStatus.ERROR, 
					MessageFormat.format( CoreModelMessages.getString( "CVariable.1" ), new String[] { ((ErrorVariable)cdiVariableObject).getException().getMessage() } ) ); //$NON-NLS-1$
		fFormat = CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT );
		getCDISession().getEventManager().addEventListener( this );
	}

	private InternalVariable createOriginal( ICDIVariableObject varObject )
	{
		return new InternalVariable( varObject );
	}

	/**
	 * Returns the current value of this variable. The value
	 * is cached, but on each access we see if the value has changed
	 * and update if required.
	 * 
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() throws DebugException
	{
		if ( !isEnabled() )
			return fDisabledValue;
		if ( fValue == null )
		{
			ICType type = null;
			try
			{
				type = getType();
			}
			catch( DebugException e )
			{
				// ignore and use default type
			}
			if ( type != null && type.isArray() )
			{
				ICDIVariable var = null;
				try
				{
					var = getInternalVariable().getCDIVariable();
				}
				catch( CDIException e )
				{
					requestFailed( "", e ); //$NON-NLS-1$
				}
				int[] dims = type.getArrayDimensions();
				if ( dims.length > 0 && dims[0] > 0 )
					fValue = CValueFactory.createArrayValue( this, var, 0, dims[0] - 1 );
			}
			else
			{
				ICDIValue cdiValue = getCurrentValue();
				if ( cdiValue != null )
					fValue = CValueFactory.createValue( this, cdiValue );
			}
		}
		return fValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws DebugException
	{
		return fChanged;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(String)
	 */
	public void setValue( String expression ) throws DebugException
	{
		notSupported( CoreModelMessages.getString( "CVariable.2" ) ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(IValue)
	 */
	public void setValue( IValue value ) throws DebugException
	{
		notSupported( CoreModelMessages.getString( "CVariable.3" ) ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(String)
	 */
	public boolean verifyValue( String expression ) throws DebugException
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(IValue)
	 */
	public boolean verifyValue( IValue value ) throws DebugException
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( ICastToType.class ) )
			return this;
		if ( adapter.equals( IVariable.class ) )
			return this;
		if ( adapter.equals( ICVariable.class ) )
			return this;
		return super.getAdapter( adapter );
	}

	/**
	 * Returns this variable's current underlying CDI value.
	 * Subclasses must implement #retrieveValue() and do not
	 * need to guard against CDIException, as this method
	 * handles them.
	 *
	 * @exception DebugException if unable to access the value
	 */
	protected final ICDIValue getCurrentValue() throws DebugException
	{
		try
		{
			return retrieveValue();
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
		return null;
	}

	/**
	 * Returns the last known value for this variable
	 */
	protected ICDIValue getLastKnownValue()
	{
		return ( fValue instanceof CValue ) ? ((CValue)fValue).getUnderlyingValue() : null;
	}
	
	public void dispose()
	{
		if ( fValue != null )
		{
			fValue.dispose();
		}
		getCDISession().getEventManager().removeEventListener( this );
		if ( getShadow() != null )
			getShadow().dispose();
	}
	
	protected synchronized void setChanged( boolean changed ) throws DebugException
	{
		if ( getValue() != null && getValue() instanceof CValue )
		{
			fChanged = changed;
			((CValue)getValue()).setChanged( changed );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(ICDIEvent)
	 */
	public void handleDebugEvents( ICDIEvent[] events )
	{
		for (int i = 0; i < events.length; i++)
		{
			ICDIEvent event = events[i];
			ICDIObject source = event.getSource();
			if (source == null)
				continue;

			if ( source.getTarget().equals( getCDITarget() ) )
			{
				if ( event instanceof ICDIChangedEvent )
				{
					if ( source instanceof ICDIVariable && isSameVariable( (ICDIVariable)source ) )
					{
						handleChangedEvent( (ICDIChangedEvent)event );
					}
				}
				else if ( event instanceof ICDIDestroyedEvent )
				{
					if ( source instanceof ICDIVariable && isSameVariable( (ICDIVariable)source ) )
					{
						handleDestroyedEvent( (ICDIDestroyedEvent)event );
					}
				}
				else if ( event instanceof ICDIResumedEvent )
				{
					handleResumedEvent( (ICDIResumedEvent)event );
				}
			}
		}
	}

	private void handleResumedEvent( ICDIResumedEvent event )
	{
		try
		{
			if ( getCDIVariable() instanceof ErrorVariable )
			{
				getInternalVariable().invalidate();
				setStatus( ICDebugElementErrorStatus.OK, null );
			}
		}
		catch( CDIException e )
		{
		}
	}

	private void handleChangedEvent( ICDIChangedEvent event )
	{
		try
		{
			setChanged( true );
			fireChangeEvent( DebugEvent.STATE );
		}
		catch( DebugException e )
		{
			logError( e );
		}
	}

	private void handleDestroyedEvent( ICDIDestroyedEvent event )
	{
		if ( fOriginal != null )
			fOriginal.invalidate();
		if ( getShadow() != null )
			getShadow().invalidate();
		invalidateValue();
	}

	/**
	 * Returns the stack frame this variable is contained in.
	 * 
	 * @return the stack frame
	 */
	protected CDebugElement getParent()
	{
		return fParent;
	}

	/**
	 * Returns the underlying CDI variable.
	 * 
	 * @return the underlying CDI variable
	 */
	protected ICDIVariable getCDIVariable() throws CDIException
	{
		if ( getShadow() != null )
			return getShadow().getCDIVariable();
		return getOriginalCDIVariable();
	}

	/**
	 * Returns this variable's underlying CDI value.
	 */
	protected ICDIValue retrieveValue() throws DebugException, CDIException
	{
		return ( getParent().getDebugTarget().isSuspended() && getCDIVariable() != null ) ? 
											getCDIVariable().getValue() : getLastKnownValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	public String getName() throws DebugException
	{
		if ( fName == null )
		{
			fName = ( fOriginal != null ) ? fOriginal.getCDIVariableObject().getName() : null;
		}
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException
	{
		return ( getType() != null ) ? getType().getName() : null;
	}

	protected void updateParentVariable( CValue parentValue ) throws DebugException
	{
		parentValue.getParentVariable().setChanged( true );
		parentValue.getParentVariable().fireChangeEvent( DebugEvent.STATE );
	}

	/**
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#getFormat()
	 */
	public int getFormat()
	{
		return fFormat;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#setFormat(int)
	 */
	public void setFormat( int format ) throws DebugException
	{
		doSetFormat( format );
		reset();
	}

	protected void doSetFormat( int format )
	{
		fFormat = format;
	}

	protected void reset() throws DebugException
	{
		IValue value = getValue();
		if ( value instanceof CValue )
		{
			((CValue)getValue()).reset();
			fireChangeEvent( DebugEvent.STATE );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#cast(java.lang.String)
	 */
	public void cast( String type ) throws DebugException
	{
		try
		{
			InternalVariable newVar = createShadow( getOriginalCDIVariable().getStackFrame(), type );
			if ( getShadow() != null )
				getShadow().dispose();
			setShadow( newVar );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
		finally
		{
			invalidateValue();
			fireChangeEvent( DebugEvent.STATE );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#getCurrentType()
	 */
	public String getCurrentType()
	{
		try
		{
			return getReferenceTypeName();
		}
		catch( DebugException e )
		{
			logError( e );
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#restoreDefault()
	 */
	public void restoreDefault() throws DebugException
	{
		InternalVariable oldVar = getShadow();
		setShadow( null );
		if ( oldVar != null )
			oldVar.dispose();
		invalidateValue();
		fireChangeEvent( DebugEvent.STATE );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#supportsCasting()
	 */
	public boolean supportsCasting()
	{
		CDebugTarget target = (CDebugTarget)getDebugTarget().getAdapter( CDebugTarget.class );
		return ( target != null && isEditable() );
	}
	
	protected ICDIVariable getOriginalCDIVariable() throws CDIException
	{
		return ( fOriginal != null ) ? fOriginal.getCDIVariable() : null;
	}

	protected InternalVariable getShadow()
	{
		return fShadow;
	}

	private void setShadow( InternalVariable shadow )
	{
		fShadow = shadow;
	}
	
	private InternalVariable createShadow( ICDIStackFrame cdiFrame, String type ) throws DebugException
	{
		try
		{
			return new InternalVariable( getCDISession().getVariableManager().getVariableObjectAsType( getOriginalCDIVariable(), type ) );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
		return null;
	}
	
	private InternalVariable createShadow( ICDIStackFrame cdiFrame, int start, int length ) throws DebugException
	{
		try
		{
			return new InternalVariable( getCDISession().getVariableManager().getVariableObjectAsArray( getCDIVariable(), start, length ) );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#isCasted()
	 */
	public boolean isCasted()
	{
		return ( getShadow() != null );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToArray#castToArray(int, int)
	 */
	public void castToArray( int startIndex, int length ) throws DebugException
	{
		try
		{
			InternalVariable newVar = createShadow( getOriginalCDIVariable().getStackFrame(), startIndex, length );
			if ( getShadow() != null )
				getShadow().dispose();
			setShadow( newVar );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
		finally
		{
			invalidateValue();
			fireChangeEvent( DebugEvent.STATE );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToArray#supportsCastToArray()
	 */
	public boolean supportsCastToArray()
	{
		CDebugTarget target = (CDebugTarget)getDebugTarget().getAdapter( CDebugTarget.class );
		return ( target != null && isEditable() && hasChildren() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#hasChildren()
	 */
	public boolean hasChildren()
	{
		if ( !isEnabled() )
			return false;
		boolean result = false;
		try
		{
			InternalVariable var = getInternalVariable();
			if ( var != null )
				result = var.hasChildren();
		}
		catch( CDIException e )
		{
			logError( e );
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#isEditable()
	 */
	public boolean isEditable()
	{
		if ( !isEnabled() )
			return false;
		boolean result = false;
		try
		{
			InternalVariable var = getInternalVariable();
			if ( var != null )
				result = var.isEditable();
		}
		catch( CDIException e )
		{
			logError( e );
		}
		return result;
	}

	protected String getQualifiedName() throws DebugException
	{
		String result = null;
		try
		{
			result = getInternalVariable().getQualifiedName();
		}
		catch( CDIException e )
		{
			requestFailed( CoreModelMessages.getString( "CVariable.4" ), e ); //$NON-NLS-1$
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#getType()
	 */
	public ICType getType() throws DebugException
	{
		ICType type = null;
		if ( isEnabled() )
		{
			try
			{
				InternalVariable iv = getInternalVariable();
				if ( iv != null )
					type = iv.getType();
			}
			catch( CDIException e )
			{
				requestFailed( CoreModelMessages.getString( "CVariable.5" ), e ); //$NON-NLS-1$
			}
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#isEnabled()
	 */
	public boolean isEnabled()
	{
		return ( canEnableDisable() ) ? fIsEnabled : true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#setEnabled(boolean)
	 */
	public void setEnabled( boolean enabled ) throws DebugException
	{
		setEnabled0( enabled );
		fireChangeEvent( DebugEvent.STATE );
	}

	private synchronized void setEnabled0( boolean enabled )
	{
		if ( fOriginal != null )
			fOriginal.invalidate();
		if ( getShadow() != null )
			getShadow().invalidate();
		fIsEnabled = enabled;
		invalidateValue();
	}

	private void invalidateValue()
	{
		if ( fValue != null )
		{
			fValue.dispose();
			fValue = null;
		}
	}

	public boolean isArgument()
	{
		return ( fOriginal != null ) ? ( fOriginal.getCDIVariableObject() instanceof ICDIArgumentObject ) : false;
	}

	protected boolean sameVariableObject( ICDIVariableObject object )
	{
		return ( object != null && fOriginal != null ) ? ( object.equals( fOriginal.getCDIVariableObject() ) ) : false;
	}

	private boolean enableVariableBookkeeping()
	{
		boolean result = false;
		try
		{
			result = getLaunch().getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, false );
		}
		catch( CoreException e )
		{
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#canEnableDisable()
	 */
	public boolean canEnableDisable()
	{
		return ( getParent() instanceof CStackFrame );
	}

	protected boolean isSameVariable( ICDIVariable cdiVar )
	{
		return ( ( getShadow() != null && getShadow().isSameVariable( cdiVar ) ) ||
				 ( fOriginal != null && fOriginal.isSameVariable( cdiVar ) ) );
	}

	private InternalVariable getInternalVariable()
	{
		return ( getShadow() != null ) ? getShadow() : fOriginal;
	}

	protected int sizeof()
	{
		return getInternalVariable().sizeof();
	}

	public boolean hasErrors() {
		return ( getShadow() != null ) ? getShadow().hasErrors() : fOriginal.hasErrors();
	}
}
