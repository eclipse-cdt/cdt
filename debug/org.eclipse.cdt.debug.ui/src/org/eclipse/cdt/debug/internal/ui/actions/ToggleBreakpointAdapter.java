/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IDeclaration;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IVariable;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyEditorInput;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyView;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
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
		if ( part instanceof IEditorPart ) {
			IEditorPart editorPart = (IEditorPart)part;
			IEditorInput input = editorPart.getEditorInput();
			if ( input == null ) {
				errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Empty_editor_1" ); //$NON-NLS-1$
			}
			else {
				ITextEditor textEditor = (ITextEditor)editorPart;
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
					IResource resource = ResourcesPlugin.getWorkspace().getRoot();
					String sourceHandle = getSourceHandle( input );
					long address = ((DisassemblyEditorInput)input).getAddress( lineNumber );
					if ( address != 0 ) {
						ICAddressBreakpoint breakpoint = CDIDebugModel.addressBreakpointExists( sourceHandle, resource, address );
						if ( breakpoint != null ) {
							DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( breakpoint, true );
						}
						else {
							CDIDebugModel.createAddressBreakpoint( sourceHandle, 
																   resource, 
																   address, 
																   true, 
																   0, 
																   "", //$NON-NLS-1$
																   true );
						}
						return;
					}
					errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Invalid_line_1" ); //$NON-NLS-1$						
				}
			}
		}
		else {
			errorMessage = ActionMessages.getString( "RunToLineAdapter.Operation_is_not_supported_1" ); //$NON-NLS-1$
		}
		throw new CoreException( new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), ICDebugUIConstants.INTERNAL_ERROR, errorMessage, null ) );
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
		if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if ( ss.size() == 1 && ss.getFirstElement() instanceof IFunction ) {
				IFunction function = (IFunction)ss.getFirstElement();
				String sourceHandle = getSourceHandle( function );
				IResource resource = getElementResource( function );
				String functionName = getFunctionName( function );
				ICFunctionBreakpoint breakpoint = CDIDebugModel.functionBreakpointExists( sourceHandle, resource, functionName );
				if ( breakpoint != null ) {
					DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( breakpoint, true );
				}
				else {
					int lineNumber = -1;
					int charStart = -1;
					int charEnd = -1;
					try {
						ISourceRange sourceRange = function.getSourceRange();
						if ( sourceRange != null ) {
							charStart = sourceRange.getStartPos();
							charEnd = charStart + sourceRange.getLength();
							// for now
							if ( charEnd == 0 )
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
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleMethodBreakpoints( IWorkbenchPart part, ISelection selection ) {
		if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if ( ss.size() == 1 ) {
				return ( ss.getFirstElement() instanceof IFunction );
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleWatchpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
		if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if ( ss.size() == 1 && ss.getFirstElement() instanceof IVariable ) {
				toggleVariableWatchpoint( part, (IVariable)ss.getFirstElement() );
			}
		}
//		String errorMessage = null;
//		if ( part instanceof IEditorPart ) {
//			IEditorPart editorPart = (IEditorPart)part;
//			IEditorInput input = editorPart.getEditorInput();
//			if ( input == null ) {
//				errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Empty_editor_2" ); //$NON-NLS-1$
//			}
//			else {
//				ITextEditor textEditor = (ITextEditor)editorPart;
//				IDocument document = textEditor.getDocumentProvider().getDocument( input );
//				if ( document == null ) {
//					errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Missing_document_2" ); //$NON-NLS-1$
//				}
//				else {
//					IResource resource = getResource( textEditor );
//					if ( resource == null ) {
//						errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Missing_resource_2" ); //$NON-NLS-1$
//					}
//					else {
//						if ( !(resource instanceof IWorkspaceRoot) )
//							resource = resource.getProject();
//						String expression = ( selection instanceof TextSelection ) ? ((TextSelection)selection).getText().trim() : ""; //$NON-NLS-1$
//						String sourceHandle = getSourceHandle( input );
//						ICWatchpoint watchpoint = CDIDebugModel.watchpointExists( sourceHandle, resource, expression );
//						if ( watchpoint != null ) {
//							DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( watchpoint, true );
//						}
//						else {
//							AddWatchpointDialog dlg = new AddWatchpointDialog( textEditor.getSite().getShell(), true, false, expression );
//							if ( dlg.open() != Window.OK )
//								return;
//							expression = dlg.getExpression();
//							WatchpointExpressionVerifier wev = new WatchpointExpressionVerifier();
//							if ( !wev.isValidExpression( document, expression ) ) {
//								errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Invalid_expression_1" ) + expression; //$NON-NLS-1$
//							}
//							else {
//								CDIDebugModel.createWatchpoint( sourceHandle, 
//																resource,
//																dlg.getWriteAccess(), 
//																dlg.getReadAccess(),
//																expression, 
//																true, 
//																0, 
//																"", //$NON-NLS-1$
//																true );
//							}
//						}
//						return;
//					}
//				}
//			}
//		}
//		throw new CoreException( new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), ICDebugUIConstants.INTERNAL_ERROR, errorMessage, null ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleWatchpoints( IWorkbenchPart part, ISelection selection ) {
		if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if ( ss.size() == 1 ) {
				return ( ss.getFirstElement() instanceof IVariable );
			}
		}
//		if ( selection instanceof ITextSelection ) {
//			return true;
//		}
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
			return ((IStorageEditorInput)input).getStorage().getName();
		}
		if ( input instanceof DisassemblyEditorInput ) {
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
			AddWatchpointDialog dlg = new AddWatchpointDialog( part.getSite().getShell(), true, false, expression );
			if ( dlg.open() != Window.OK )
				return;
			expression = dlg.getExpression();
			CDIDebugModel.createWatchpoint( sourceHandle, 
											resource,
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
			IPath path = tu.getPath();
			if ( path != null ) {
				return path.toOSString();
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
		//??????
		if ( functionName.indexOf( "::" ) != -1 && functionName.indexOf( '(' ) == -1 ) { //$NON-NLS-1$
			String[] params = function.getParameterTypes();
			name.append( '(' );
			if ( params.length == 0 ) {
				name.append( "void" ); //$NON-NLS-1$
			}
			else {
				for( int i = 0; i < params.length; ++i ) {
					name.append( params[i] );
					if ( i != params.length - 1 )
						name.append( ',' );
				}
			}
			name.append( ')' );
		}
		return name.toString();
	}

	private String getVariableName( IVariable variable ) {
		return variable.getElementName();
	}
}
