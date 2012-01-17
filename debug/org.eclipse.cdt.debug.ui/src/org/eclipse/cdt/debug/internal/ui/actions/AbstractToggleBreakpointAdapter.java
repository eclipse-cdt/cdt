/*******************************************************************************
 * Copyright (c) 2011 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.actions;

import java.math.BigInteger;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IDeclaration;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IVariable;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

abstract public class AbstractToggleBreakpointAdapter implements IToggleBreakpointsTargetExtension {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleLineBreakpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
		String errorMessage = null;
		if ( part instanceof ITextEditor ) {
			ITextEditor textEditor = (ITextEditor)part;
			IEditorInput input = textEditor.getEditorInput();
			if ( input == null ) {
				errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Empty_editor_1" ); //$NON-NLS-1$
			}
			else {
				IDocument document = textEditor.getDocumentProvider().getDocument( input );
				if ( document == null ) {
					errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Missing_document_1" ); //$NON-NLS-1$
				}
				else {
					IResource resource = getResource( textEditor );
					if ( resource == null ) {
						errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Missing_resource_1" ); //$NON-NLS-1$
					}
					else {
						BreakpointLocationVerifier bv = new BreakpointLocationVerifier();
						int lineNumber = bv.getValidLineBreakpointLocation( document, ((ITextSelection)selection).getStartLine() );
						if ( lineNumber == -1 ) {
							errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Invalid_line_1" ); //$NON-NLS-1$
						}
						else {
							String sourceHandle = getSourceHandle( input );
							ICLineBreakpoint breakpoint = findLineBreakpoint( sourceHandle, resource, lineNumber );
							if ( breakpoint != null ) {
								DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( breakpoint, true );
							}
							else {
								createLineBreakpoint( sourceHandle, resource, lineNumber );
							}
							return;
						}
					}
				}
			}
		}
		else {
			errorMessage = ActionMessages.getString( "RunToLineAdapter.Operation_is_not_supported_1" ); //$NON-NLS-1$
		}
		throw new CoreException(
				new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), IInternalCDebugUIConstants.INTERNAL_ERROR, errorMessage, null ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleLineBreakpoints( IWorkbenchPart part, ISelection selection ) {
		return (selection instanceof ITextSelection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleMethodBreakpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
		ICElement element = getCElementFromSelection( part, selection );
		if ( element instanceof IFunction || element instanceof IMethod ) {
			toggleMethodBreakpoints0( (IDeclaration)element );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleMethodBreakpoints( IWorkbenchPart part, ISelection selection ) {
		ICElement element = getCElementFromSelection( part, selection );
		return (element instanceof IFunction || element instanceof IMethod);
	}

	protected ICElement getCElementFromSelection( IWorkbenchPart part, ISelection selection ) {
		if ( selection instanceof ITextSelection ) {
			ITextSelection textSelection = (ITextSelection)selection;
			String text = textSelection.getText();
			if ( text != null ) {
				if ( part instanceof ITextEditor ) {
					ICElement editorElement = CDTUITools.getEditorInputCElement( ((ITextEditor)part).getEditorInput() );
					if ( editorElement instanceof ITranslationUnit ) {
						ITranslationUnit tu = (ITranslationUnit)editorElement;
						try {
							if ( tu.isStructureKnown() && tu.isConsistent() ) {
								return tu.getElementAtOffset( textSelection.getOffset() );
							}
						}
						catch( CModelException exc ) {
							// ignored on purpose
						}
					}
				}
				else {
					IResource resource = getResource( part );
					if ( resource instanceof IFile ) {
						ITranslationUnit tu = getTranslationUnit( (IFile)resource );
						if ( tu != null ) {
							try {
								ICElement element = tu.getElement( text.trim() );
								if ( element == null ) {
									element = tu.getElementAtLine( textSelection.getStartLine() );
								}
								return element;
							}
							catch( CModelException e ) {
							}
						}
					}
				}
			}
		}
		else if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if ( ss.size() == 1 ) {
				Object object = ss.getFirstElement();
				if ( object instanceof ICElement ) {
					return (ICElement)object;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleWatchpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
		IVariable variable = getVariableFromSelection( part, selection );
		if ( variable != null ) {
			toggleVariableWatchpoint( part, variable );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleWatchpoints( IWorkbenchPart part, ISelection selection ) {
		return getVariableFromSelection( part, selection ) != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#canToggleBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleBreakpoints( IWorkbenchPart part, ISelection selection ) {
		return ( canToggleLineBreakpoints( part, selection ) 
				 || canToggleWatchpoints( part, selection ) 
				 || canToggleMethodBreakpoints( part, selection ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#toggleBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleBreakpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
		if ( canToggleLineBreakpoints( part, selection ) ) {
			toggleLineBreakpoints( part, selection );
		}
		else {
			ICElement element = getCElementFromSelection( part, selection );
			if ( element instanceof IFunction || element instanceof IMethod ) {
				toggleMethodBreakpoints0( (IDeclaration)element );
			}
			else if ( element instanceof IVariable ) {
				toggleVariableWatchpoint( part, (IVariable)element );
			}
		}
	}

	protected IVariable getVariableFromSelection( IWorkbenchPart part, ISelection selection ) {
		ICElement element = getCElementFromSelection( part, selection );
		if ( element instanceof IVariable ) {
			return (IVariable)element;
		}
		return null;
	}

	protected void report( String message, IWorkbenchPart part ) {
		IEditorStatusLine statusLine = (IEditorStatusLine)part.getAdapter( IEditorStatusLine.class );
		if ( statusLine != null ) {
			if ( message != null ) {
				statusLine.setMessage( true, message, null );
			}
			else {
				statusLine.setMessage( true, null, null );
			}
		}
		if ( message != null && CDebugUIPlugin.getActiveWorkbenchShell() != null ) {
			CDebugUIPlugin.getActiveWorkbenchShell().getDisplay().beep();
		}
	}

	protected static IResource getResource( IWorkbenchPart part ) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if ( part instanceof IEditorPart ) {
			IEditorInput editorInput = ((IEditorPart)part).getEditorInput();
			IResource resource = null;
			if ( editorInput instanceof IFileEditorInput ) {
				resource = ((IFileEditorInput)editorInput).getFile();
			}
			else if ( editorInput instanceof ExternalEditorInput ) {
				resource = ((ExternalEditorInput)editorInput).getMarkerResource();
			}
			if ( resource != null )
				return resource;
			/* This file is not in a project, let default case handle it */
			ILocationProvider provider = (ILocationProvider)editorInput.getAdapter( ILocationProvider.class );
			if ( provider != null ) {
				IPath location = provider.getPath( editorInput );
				if ( location != null ) {
					IFile[] files = root.findFilesForLocationURI( URIUtil.toURI( location ) );
					if ( files.length > 0 && files[0].isAccessible())
						return files[0];
				}
			}
		}
		return root;
	}

	private String getSourceHandle( IEditorInput input ) throws CoreException {
		return CDebugUIUtils.getEditorFilePath( input );
	}

	private void toggleVariableWatchpoint( IWorkbenchPart part, IVariable variable ) throws CoreException {
		String sourceHandle = getSourceHandle( variable );
		IResource resource = getElementResource( variable );
		String expression = getVariableName( variable );
		ICWatchpoint watchpoint = findWatchpoint( sourceHandle, resource, expression );
		if ( watchpoint != null ) {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( watchpoint, true );
		}
		else {
			AddWatchpointDialog dlg = new AddWatchpointDialog( part.getSite().getShell(), AddWatchpointActionDelegate.getMemorySpaceManagement() );
			dlg.setExpression( expression );
			if ( dlg.open() != Window.OK )
				return;
			expression = dlg.getExpression();
			int lineNumber = -1;
			int charStart = -1;
			int charEnd = -1;
			try {
				ISourceRange sourceRange = variable.getSourceRange();
				if ( sourceRange != null ) {
					charStart = sourceRange.getStartPos();
					charEnd = charStart + sourceRange.getLength();
					if ( charEnd <= 0 ) {
						charStart = -1;
						charEnd = -1;
					}
					lineNumber = sourceRange.getStartLine();
				}
			}
			catch( CModelException e ) {
				DebugPlugin.log( e );
			}
			createWatchpoint( 
					sourceHandle, 
					resource, 
					charStart, 
					charEnd, 
					lineNumber, 
					dlg.getWriteAccess(), 
					dlg.getReadAccess(), 
					expression,
					dlg.getMemorySpace(), 
					dlg.getRange() );
		}
	}

	private String getSourceHandle( IDeclaration declaration ) {
		ITranslationUnit tu = declaration.getTranslationUnit();
		if ( tu != null ) {
			IPath location = tu.getLocation();
			if ( location != null ) {
				return location.toOSString();
			}
		}
		return ""; //$NON-NLS-1$
	}

	private IResource getElementResource( IDeclaration declaration ) {
		return declaration.getUnderlyingResource();
	}

	private String getFunctionName( IFunction function ) {
		String functionName = function.getElementName();
		StringBuffer name = new StringBuffer( functionName );
		ITranslationUnit tu = function.getTranslationUnit();
		if ( tu != null && tu.isCXXLanguage() ) {
			appendParameters( name, function );
		}
		return name.toString();
	}

	private String getMethodName( IMethod method ) {
		StringBuffer name = new StringBuffer();
		String methodName = method.getElementName();
		ICElement parent = method.getParent();
		while( parent != null
				&& (parent.getElementType() == ICElement.C_NAMESPACE || parent.getElementType() == ICElement.C_CLASS
						|| parent.getElementType() == ICElement.C_STRUCT || parent.getElementType() == ICElement.C_UNION) ) {
			name.append( parent.getElementName() ).append( "::" ); //$NON-NLS-1$
			parent = parent.getParent();
		}
		name.append( methodName );
		appendParameters( name, method );
		return name.toString();
	}

	private void appendParameters( StringBuffer sb, IFunctionDeclaration fd ) {
		String[] params = fd.getParameterTypes();
		sb.append( '(' );
		for( int i = 0; i < params.length; ++i ) {
			sb.append( params[i] );
			if ( i != params.length - 1 )
				sb.append( ',' );
		}
		sb.append( ')' );
	}

	private String getVariableName( IVariable variable ) {
		return variable.getElementName();
	}

	private ITranslationUnit getTranslationUnit( IFile file ) {
		Object element = CoreModel.getDefault().create( file );
		if ( element instanceof ITranslationUnit ) {
			return (ITranslationUnit)element;
		}
		return null;
	}

	private void toggleMethodBreakpoints0( IDeclaration declaration ) throws CoreException {
		String sourceHandle = getSourceHandle( declaration );
		IResource resource = getElementResource( declaration );
		String functionName = (declaration instanceof IFunction) ? getFunctionName( (IFunction)declaration ) : getMethodName( (IMethod)declaration );
		ICFunctionBreakpoint breakpoint = findFunctionBreakpoint( sourceHandle, resource, functionName );
		if ( breakpoint != null ) {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( breakpoint, true );
		}
		else {
			int lineNumber = -1;
			int charStart = -1;
			int charEnd = -1;
			try {
				ISourceRange sourceRange = declaration.getSourceRange();
				if ( sourceRange != null ) {
					charStart = sourceRange.getStartPos();
					charEnd = charStart + sourceRange.getLength();
					if ( charEnd <= 0 ) {
						charStart = -1;
						charEnd = -1;
					}
					lineNumber = sourceRange.getStartLine();
				}
			}
			catch( CModelException e ) {
				DebugPlugin.log( e );
			}
			createFunctionBreakpoint( 
					sourceHandle, 
					resource, 
					functionName, 
					charStart, 
					charEnd, 
					lineNumber );
		}
	}

	protected abstract ICLineBreakpoint findLineBreakpoint( String sourceHandle, IResource resource, int lineNumber ) throws CoreException;

	protected abstract void createLineBreakpoint( String sourceHandle, IResource resource, int lineNumber ) throws CoreException;

	protected abstract ICFunctionBreakpoint findFunctionBreakpoint( String sourceHandle, IResource resource, String functionName ) throws CoreException;

	protected abstract void createFunctionBreakpoint( 
			String sourceHandle, 
			IResource resource, 
			String functionName, 
			int charStart, 
			int charEnd, 
			int lineNumber ) throws CoreException;

	protected abstract ICWatchpoint findWatchpoint( String sourceHandle, IResource resource, String expression ) throws CoreException;

	protected abstract void createWatchpoint( 
			String sourceHandle, 
			IResource resource, 
			int charStart, 
			int charEnd, 
			int lineNumber, 
			boolean writeAccess, 
			boolean readAccess, 
			String expression,
			String memorySpace, 
			BigInteger range ) throws CoreException;
}
