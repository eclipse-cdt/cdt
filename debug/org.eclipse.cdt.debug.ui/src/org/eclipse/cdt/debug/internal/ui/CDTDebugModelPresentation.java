/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.debug.core.IState;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;

/**
 * 
 * Responsible for providing labels, images, and editors associated 
 * with debug elements in the CDT debug model.
 * 
 * @since Jul 22, 2002
 */
public class CDTDebugModelPresentation extends LabelProvider
									   implements IDebugModelPresentation
{
	private static CDTDebugModelPresentation fInstance = null;

	/**
	 * Constructor for CDTDebugModelPresentation.
	 */
	public CDTDebugModelPresentation()
	{
		super();
		fInstance = new CDTDebugModelPresentation();
	}

	public static CDTDebugModelPresentation getDefault()
	{
		return fInstance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(String, Object)
	 */
	public void setAttribute( String attribute, Object value )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(IValue, IValueDetailListener)
	 */
	public void computeDetail( IValue value, IValueDetailListener listener )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(Object)
	 */
	public IEditorInput getEditorInput( Object element )
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(IEditorInput, Object)
	 */
	public String getEditorId( IEditorInput input, Object element )
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILabelProvider#getImage(Object)
	 */
	public Image getImage( Object element )
	{
		return super.getImage( element );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILabelProvider#getText(Object)
	 */
	public String getText( Object element )
	{
		if ( element instanceof IDebugTarget )
			return getTargetText( (IDebugTarget)element );
		if ( element instanceof IThread )
			return getThreadText( (IThread)element );
		return super.getText( element );
	}
	
	protected String getTargetText( IDebugTarget target )
	{
		if ( target instanceof IState )
		{
			IState state = (IState)target.getAdapter( IState.class );
			if ( state != null )
			{
				switch( state.getCurrentStateId() )
				{
					case IState.ATTACHING:
						break;
					case IState.CORE_DUMP_FILE:
						break;
					case IState.DISCONNECTING:
						break;
					case IState.DISCONNECTED:
						break;
					case IState.RUNNING:
						break;
					case IState.STARTING:
						break;
					case IState.STEPPING:
						break;
					case IState.SUSPENDED:
						break;
					case IState.TERMINATED:
						break;
				}
			}
		}
		return super.getText( target );
	}
	
	protected String getThreadText( IThread thread )
	{
		if ( thread instanceof IState )
		{
			IState state = (IState)thread.getAdapter( IState.class );
			if ( state != null )
			{
				switch( state.getCurrentStateId() )
				{
					case IState.CORE_DUMP_FILE:
						break;
					case IState.RUNNING:
						break;
					case IState.STEPPING:
						break;
					case IState.SUSPENDED:
						break;
				}
			}
		}
		return super.getText( thread );
	}
}
