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
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.core.IAddress;
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
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyEditorInput;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyView;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Toggles a line breakpoint in a C/C++ editor.
 */
public class ToggleBreakpointAdapter implements IToggleBreakpointsTarget {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
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
							ICLineBreakpoint breakpoint = CDIDebugModel.lineBreakpointExists( sourceHandle, resource, lineNumber );
							if ( breakpoint != null ) {
								DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( breakpoint, true );
							}
							else {
								CDIDebugModel.createLineBreakpoint( sourceHandle, 
																	resource, 
																	lineNumber, 
																	true, 
																	0, 
																	"", //$NON-NLS-1$
																	true );
							}
							return;
						}
					}
				}
			}
		}
		else if ( part instanceof DisassemblyView ) {
			IEditorInput input = ((DisassemblyView)part).getInput();
			if ( !(input instanceof DisassemblyEditorInput) ) {
				errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Empty_editor_1" ); //$NON-NLS-1$
			}
			else {
				BreakpointLocationVerifier bv = new BreakpointLocationVerifier();
				int lineNumber = bv.getValidAddressBreakpointLocation( null, ((ITextSelection)selection).getStartLine() );
				if ( lineNumber == -1 ) {
					errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Invalid_line_1" ); //$NON-NLS-1$
				}
				else {
					IAddress address = ((DisassemblyEditorInput)input).getAddress( lineNumber );
					if ( address == null ) {
						errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Invalid_line_1" ); //$NON-NLS-1$						
					}
					else {
						ICLineBreakpoint breakpoint = ((DisassemblyEditorInput)input).breakpointExists( address );
						if ( breakpoint != null ) {
							DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( breakpoint, true );
						}
						else {
							String module = ((DisassemblyEditorInput)input).getModuleFile();
							IResource resource = getAddressBreakpointResource( ((DisassemblyEditorInput)input).getSourceFile() );
							String sourceHandle = getSourceHandle( input );
							CDIDebugModel.createAddressBreakpoint( module,
																   sourceHandle, 
																   resource,
																   ((DisassemblyEditorInput)input).getSourceLine( lineNumber ),
																   address, 
																   true, 
																   0, 
																   "", //$NON-NLS-1$
																   true );
						}
						return;
					}
				}
			}
		}
		else {
			errorMessage = ActionMessages.getString( "RunToLineAdapter.Operation_is_not_supported_1" ); //$NON-NLS-1$
		}
		throw new CoreException( new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), IInternalCDebugUIConstants.INTERNAL_ERROR, errorMessage, null ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleLineBreakpoints( IWorkbenchPart part, ISelection selection ) {
		if ( part instanceof DisassemblyView ) {
			IEditorInput input = ((DisassemblyView)part).getInput();
			if ( !(input instanceof DisassemblyEditorInput) || 
				 ((DisassemblyEditorInput)input).equals( DisassemblyEditorInput.EMPTY_EDITOR_INPUT ) ) {
				return false;
			}			
		}
		return ( selection instanceof ITextSelection );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleMethodBreakpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
		if ( selection instanceof ITextSelection ) {
			String text = ((ITextSelection)selection).getText();
			if ( text != null ) {
				IResource resource = getResource( part );
				if ( resource instanceof IFile ) {
					ITranslationUnit tu = getTranslationUnit( (IFile)resource );
					if ( tu != null ) {
						try {
							ICElement element = tu.getElement( text.trim() );
							if ( element instanceof IFunction || element instanceof IMethod ) {
								toggleMethodBreakpoints0( (IDeclaration)element );
							}
						}
						catch( CModelException e ) {
						}
					}
				}
			}
		}
		else if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if ( ss.size() == 1 && (ss.getFirstElement() instanceof IFunction || ss.getFirstElement() instanceof IMethod) ) {
				toggleMethodBreakpoints0( (IDeclaration)ss.getFirstElement() );
			}
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleMethodBreakpoints( IWorkbenchPart part, ISelection selection ) {
		if ( selection instanceof ITextSelection ) {
			String text = ((ITextSelection)selection).getText();
			if ( text != null ) {
				IResource resource = getResource( part );
				if ( resource instanceof IFile ) {
					ITranslationUnit tu = getTranslationUnit( (IFile)resource );
					if ( tu != null ) {
						try {
							ICElement element = tu.getElement( text.trim() );
							return ( element instanceof IFunction || element instanceof IMethod );
						}
						catch( CModelException e ) {
						}
					}
				}
			}
		}
		else if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if ( ss.size() == 1 ) {
				return ( ss.getFirstElement() instanceof IFunction || ss.getFirstElement() instanceof IMethod );
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleWatchpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
		if ( selection instanceof ITextSelection ) {
			String text = ((ITextSelection)selection).getText();
			if ( text != null ) {
				IResource resource = getResource( part );
				if ( resource instanceof IFile ) {
					ITranslationUnit tu = getTranslationUnit( (IFile)resource );
					if ( tu != null ) {
						try {
							ICElement element = tu.getElement( text.trim() );
							if ( element instanceof IVariable ) {
								toggleVariableWatchpoint( part, (IVariable)element );
							}
						}
						catch( CModelException e ) {
						}
					}
				}
			}
		}
		else if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if ( ss.size() == 1 && ss.getFirstElement() instanceof IVariable ) {
				toggleVariableWatchpoint( part, (IVariable)ss.getFirstElement() );
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleWatchpoints( IWorkbenchPart part, ISelection selection ) {
		if ( selection instanceof ITextSelection ) {
			String text = ((ITextSelection)selection).getText();
			if ( text != null ) {
				IResource resource = getResource( part );
				if ( resource instanceof IFile ) {
					ITranslationUnit tu = getTranslationUnit( (IFile)resource );
					if ( tu != null ) {
						try {
							return ( tu.getElement( text.trim() ) instanceof IVariable );
						}
						catch( CModelException e ) {
						}
					}
				}
			}
		}
		else if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if ( ss.size() == 1 ) {
				return ( ss.getFirstElement() instanceof IVariable );
			}
		}
		return false;
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
		if ( part instanceof IEditorPart ) {
			IEditorInput editorInput = ((IEditorPart)part).getEditorInput();
			if ( editorInput instanceof IFileEditorInput ) {
				return ((IFileEditorInput)editorInput).getFile();
			}
		}
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	private String getSourceHandle( IEditorInput input ) throws CoreException {
		if ( input instanceof IFileEditorInput ) {
			return ((IFileEditorInput)input).getFile().getLocation().toOSString();
		}
		if ( input instanceof IStorageEditorInput ) {
			return ((IStorageEditorInput)input).getStorage().getFullPath().toOSString();
		}
		if ( input instanceof DisassemblyEditorInput ) {
			String sourceFile = ((DisassemblyEditorInput)input).getSourceFile();
			if ( sourceFile != null ) {
				return sourceFile;
			}
			return ((DisassemblyEditorInput)input).getModuleFile();
		}
		return ""; //$NON-NLS-1$
	}

	private void toggleVariableWatchpoint( IWorkbenchPart part, IVariable variable ) throws CoreException {
		String sourceHandle = getSourceHandle( variable );
		IResource resource = getElementResource( variable );
		String expression = getVariableName( variable );
		ICWatchpoint watchpoint = CDIDebugModel.watchpointExists( sourceHandle, resource, expression );
		if ( watchpoint != null ) {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( watchpoint, true );
		}
		else {
			AddWatchpointDialog dlg = new AddWatchpointDialog( part.getSite().getShell(), true, false, expression, false );
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
			CDIDebugModel.createWatchpoint( sourceHandle, 
											resource,
											charStart,
											charEnd,
											lineNumber,
											dlg.getWriteAccess(), 
											dlg.getReadAccess(),
											expression, 
											true, 
											0, 
											"", //$NON-NLS-1$
											true );
		}
	}

	private String getSourceHandle( IDeclaration declaration ) {
		ITranslationUnit tu = declaration.getTranslationUnit();
		if ( tu != null ) {
			IResource resource = tu.getResource();
			if ( resource != null )
				return resource.getLocation().toOSString();
			return tu.getPath().toOSString();
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
		while ( parent != null && ( parent.getElementType() == ICElement.C_NAMESPACE || parent.getElementType() == ICElement.C_CLASS ) ) {
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
		String functionName = ( declaration instanceof IFunction ) ? getFunctionName( (IFunction)declaration ) : getMethodName( (IMethod)declaration );
		ICFunctionBreakpoint breakpoint = CDIDebugModel.functionBreakpointExists( sourceHandle, resource, functionName );
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
			CDIDebugModel.createFunctionBreakpoint( sourceHandle, 
													resource, 
													functionName,
													charStart,
													charEnd,
													lineNumber,
													true, 
													0, 
													"", //$NON-NLS-1$
													true );
		}
	}

	private IResource getAddressBreakpointResource( String fileName ) {
		if ( fileName != null ) {
			IPath path = new Path( fileName );
			if ( path.isValidPath( fileName ) ) {
				IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation( path );
				if ( files.length > 0 )
					return files[0];
			}
		}
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}
