/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.IRunToLine;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 19, 2002
 */
public class RunToLineRulerAction extends Action 
								  implements IUpdate,
								  			 ISelectionListener,
								  			 INullSelectionListener
{
	private IVerticalRulerInfo fInfo;
	private ITextEditor fTextEditor;
	private IRunToLine fTarget = null;

	/**
	 * Constructor for RunToLineRulerAction.
	 */
	public RunToLineRulerAction( IVerticalRulerInfo info, ITextEditor editor )
	{
		setInfo( info );
		setTextEditor( editor );
		setText( "Run To Line" );
		initializeTarget();
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update()
	{
		boolean enabled = false;
		IResource resource = getResource();
		int lineNumber = getLineNumber();
		IDocumentProvider provider = getTextEditor().getDocumentProvider();
		IDocument doc = provider.getDocument( getTextEditor().getEditorInput() );
		if ( resource != null && lineNumber <= doc.getNumberOfLines() && lineNumber > 0 )
		{
			enabled = ( getTarget() != null && ((IRunToLine)getTarget()).canRunToLine( resource, lineNumber ) );
		}
		setEnabled( enabled );
	}

	/**
	 * @see Action#run()
	 */
	public void run()
	{
		runToLine( getResource(), getLineNumber() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection )
	{
		IRunToLine target = null;
		if ( part.getSite().getId().equals( IDebugUIConstants.ID_DEBUG_VIEW ) )
		{
			if ( selection != null && selection instanceof IStructuredSelection )
			{
				Object element = ((IStructuredSelection)selection).getFirstElement();
				if ( element != null && element instanceof IRunToLine )
				{
					target = (IRunToLine)element;
				}
			}
			setTarget( target );
			update();
		}
	}
	
	protected void initializeTarget()
	{
		setTarget( null );
		IAdaptable context = DebugUITools.getDebugContext();
		if ( context != null && context instanceof IRunToLine )
		{
			setTarget( (IRunToLine)context );
		}
	}

	protected void setTarget( IRunToLine target )
	{
		fTarget = target;
	}
	
	protected IRunToLine getTarget()
	{
		return fTarget;
	}

	protected IVerticalRulerInfo getInfo()
	{
		return fInfo;
	}

	protected void setInfo( IVerticalRulerInfo info )
	{
		fInfo = info;
	}

	protected ITextEditor getTextEditor()
	{
		return fTextEditor;
	}

	protected void setTextEditor( ITextEditor textEditor )
	{
		fTextEditor = textEditor;
	}
	
	protected IResource getResource()
	{
		IEditorInput input = getTextEditor().getEditorInput();
		if ( input != null && input instanceof IFileEditorInput )
		{
			return ((IFileEditorInput)input).getFile();
		}
		return null;
	}
	
	protected int getLineNumber()
	{
		return getInfo().getLineOfLastMouseButtonActivity() + 1;
	} 

	protected void runToLine( IResource resource, int lineNumber )
	{
		if ( !((IRunToLine)getTarget()).canRunToLine( resource, lineNumber ) )
		{
			getTextEditor().getSite().getShell().getDisplay().beep();
			return;
		}
		try
		{
			((IRunToLine)getTarget()).runToLine( resource, lineNumber );
		}
		catch( DebugException e )
		{
			CDebugUIPlugin.errorDialog( e.getMessage(), e );
		}
	}
}
