/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
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
								implements IVariable,
										   ICDIEventListener
{
	/**
	 * The parent object this variable is contained in.
	 */
	private CDebugElement fParent;

	/**
	 * The underlying CDI variable.
	 */
	private ICDIVariable fCDIVariable;
	
	/**
	 * Cache of current value - see #getValue().
	 */
	protected ICValue fValue;

	/**
	 * Counter corresponding to this variable's debug target
	 * suspend count indicating the last time this value 
	 * changed. This variable's value has changed on the
	 * last suspend event if this counter is equal to the
	 * debug target's suspend count.
	 */
	private int fLastChangeIndex = -1;

	/**
	 * Change flag.
	 */
	protected boolean fChanged = false;

	/**
	 * The type name of this variable.
	 */
	private String fTypeName = null;

	/**
	 * Constructor for CVariable.
	 * @param target
	 */
	public CVariable( CDebugElement parent, ICDIVariable cdiVariable )
	{
		super( (CDebugTarget)parent.getDebugTarget() );
		fParent = parent;
		fCDIVariable = cdiVariable;
		getCDISession().getEventManager().addEventListener( this );
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
		if ( fValue == null )
		{
			fValue = CValueFactory.createValue( this, getCurrentValue() );
		}
		return fValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws DebugException
	{
		IValue value = getValue();
		if ( value != null )
		{
			if ( value instanceof CValue && ((CValue)getValue()).getType() == ICValue.TYPE_POINTER )
				return false; 
			return ( value.hasVariables() ) ? false : fChanged;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(String)
	 */
	public void setValue( String expression ) throws DebugException
	{
		notSupported( "Variable does not support value modification." );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(IValue)
	 */
	public void setValue( IValue value ) throws DebugException
	{
		notSupported( "Variable does not support value modification." );
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
		if ( adapter.equals( IVariable.class ) )
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
			targetRequestFailed( e.toString(), null );
		}
		return null;
	}

	/**
	 * Sets this variable's change counter to the specified value
	 * 
	 * @param count new value
	 */
	protected void setChangeCount( int count )
	{
		fLastChangeIndex = count;
	}

	/**
	 * Returns this variable's change counter. This corresponds to the
	 * last time this variable changed.
	 * 
	 * @return this variable's change counter
	 */
	protected int getChangeCount()
	{
		return fLastChangeIndex;
	}

	/**
	 * Returns the last known value for this variable
	 */
	protected ICDIValue getLastKnownValue()
	{
		if ( fValue == null )
		{
			return null;
		}
		else
		{
			return fValue.getUnderlyingValue();
		}
	}
	
	protected void dispose()
	{
		if ( fValue != null )
		{
			((CValue)fValue).dispose();
		}
		getCDISession().getEventManager().removeEventListener( this );
	}
	
	protected synchronized void setChanged( boolean changed ) throws DebugException
	{
		if ( getValue() != null && getValue() instanceof ICValue )
		{
			((ICValue)getValue()).setChanged( changed );
			if ( !getValue().hasVariables() || ((ICValue)getValue()).getType() == ICValue.TYPE_POINTER )
			{
				fChanged = changed;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvent(ICDIEvent)
	 */
	public void handleDebugEvent( ICDIEvent event )
	{
		ICDIObject source = event.getSource();
		if (source == null)
			return;

		if ( source.getTarget().equals( getCDITarget() ) )
		{
			if ( event instanceof ICDIChangedEvent )
			{
				if ( source instanceof ICDIVariable && source.equals( getCDIVariable() ) )
				{
					handleChangedEvent( (ICDIChangedEvent)event );
				}
			}
		}
	}

	private void handleChangedEvent( ICDIChangedEvent event )
	{
		try
		{
			setChanged( true );
			if ( getValue() != null && 
				 ((CValue)getValue()).getType() == ICValue.TYPE_CHAR &&
				 getParent() instanceof CValue )
			{
				updateParentVariable( (CValue)getParent() );
			}
			getParent().fireChangeEvent( DebugEvent.CONTENT );
		}
		catch( DebugException e )
		{
			logError( e );
		}
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
	protected ICDIVariable getCDIVariable()
	{
		return fCDIVariable;
	}

	protected void setCDIVariable( ICDIVariable newVar )
	{
		fCDIVariable = newVar;
	}

	/**
	 * Returns this variable's underlying CDI value.
	 */
	protected ICDIValue retrieveValue() throws DebugException, CDIException
	{
		return ( ((IDebugTarget)getParent().getDebugTarget()).isSuspended() ) ? 
						getCDIVariable().getValue() : getLastKnownValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	public String getName() throws DebugException
	{
		String name = null;
		try
		{
			name = getCDIVariable().getName();
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException
	{
		if ( fTypeName == null )
		{
			try
			{
				fTypeName = getCDIVariable().getTypeName();
			}
			catch( CDIException e )
			{
				targetRequestFailed( e.getMessage(), null );
			}
		}
		return fTypeName;
	}

	protected void updateParentVariable( CValue parentValue ) throws DebugException
	{
		parentValue.getParentVariable().setChanged( true );
		parentValue.getParentVariable().fireChangeEvent( DebugEvent.STATE );
	}
}
