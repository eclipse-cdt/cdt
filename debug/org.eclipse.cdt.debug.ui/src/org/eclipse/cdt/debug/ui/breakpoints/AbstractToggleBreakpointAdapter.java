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

package org.eclipse.cdt.debug.ui.breakpoints;

import java.util.Iterator;
import java.util.Map;

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
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.debug.internal.ui.actions.breakpoints.EnableDisableBreakpointRulerAction;
import org.eclipse.cdt.debug.internal.ui.breakpoints.CBreakpointContext;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension2;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

/**
 * Base class for toggle adapter to create/remove CDT breakpoints.  Clients may
 * extend this class to gather additional data prior to creating the breakpoints.
 * 
 * @since 7.2
 */
abstract public class AbstractToggleBreakpointAdapter 
    implements IToggleBreakpointsTargetExtension2,  IToggleBreakpointsTargetCExtension 
{

    
    @Override
    public boolean canToggleBreakpointsWithEvent(IWorkbenchPart part, ISelection selection, Event event) {
        return canToggleBreakpoints(part, selection);
    }
    
    @Override
    public void toggleBreakpointsWithEvent(IWorkbenchPart part, ISelection selection, Event event) throws CoreException {
        if(event != null && (event.stateMask & SWT.MOD2) > 0) {
            if (toggleBreakpointEnable(part)) {
                return;
            }
        }
        else {
            boolean interactive = event != null && (event.stateMask & SWT.MOD1) > 0;
            updateBreakpoints(true, interactive, part, selection);
        }
    }
    
    @Override
    public void toggleLineBreakpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
        updateLineBreakpoints(true, false, part, selection);
    }    

	@Override
	public boolean canToggleLineBreakpoints( IWorkbenchPart part, ISelection selection ) {
		return (selection instanceof ITextSelection);
	}

    @Override
    public void toggleMethodBreakpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
        ICElement element = getCElementFromSelection( part, selection );
        if ( element instanceof IFunction || element instanceof IMethod) {
            updateMethodBreakpoints(true, false, part, (IDeclaration)element);
        }
    }
    
	@Override
	public boolean canToggleMethodBreakpoints( IWorkbenchPart part, ISelection selection ) {
		ICElement element = getCElementFromSelection( part, selection );
		return (element instanceof IFunction || element instanceof IMethod);
	}

    @Override
    public void toggleWatchpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
        IVariable variable = getVariableFromSelection( part, selection );
        if ( variable != null ) {
            updateVariableWatchpoint(true, false, part, variable);
        }
    }

	@Override
	public boolean canToggleWatchpoints( IWorkbenchPart part, ISelection selection ) {
		return getVariableFromSelection( part, selection ) != null;
	}

	@Override
	public boolean canToggleBreakpoints( IWorkbenchPart part, ISelection selection ) {
		return ( canToggleLineBreakpoints( part, selection ) 
				 || canToggleWatchpoints( part, selection ) 
				 || canToggleMethodBreakpoints( part, selection ) );
	}

	@Override
	public void toggleBreakpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
	    updateBreakpoints(true, false, part, selection);
	}

	@Override
	public boolean canCreateLineBreakpointsInteractive(IWorkbenchPart part, ISelection selection) {
	    return canToggleLineBreakpoints( part, selection );
	}
        
	@Override
	public void createLineBreakpointsInteractive(IWorkbenchPart part, ISelection selection) throws CoreException {
	    updateLineBreakpoints(false, true, part, selection);
	}
	
	@Override
	public boolean canCreateWatchpointsInteractive(IWorkbenchPart part, ISelection selection) {
	    // Gather all input from user if needed.
	    return true;
	}
	
	@Override
	public void createWatchpointsInteractive(IWorkbenchPart part, ISelection selection) throws CoreException {
        ICElement element = getCElementFromSelection( part, selection );
        if (element instanceof IVariable) {
            updateVariableWatchpoint(false, true, part, (IVariable)element);
        } else {
            String text = ""; //$NON-NLS-1$
            if (selection instanceof ITextSelection) {
                text = ((ITextSelection)selection).getText();
            }
            createWatchpoint(true, part, null, ResourcesPlugin.getWorkspace().getRoot(), -1, -1, -1, text);
        }
	}

	@Override
	public boolean canCreateFunctionBreakpointInteractive(IWorkbenchPart part, ISelection selection) {
	    return true;
	}
	
	@Override
	public void createFunctionBreakpointInteractive(IWorkbenchPart part, ISelection selection) throws CoreException {
        ICElement element = getCElementFromSelection( part, selection );
        if ( element instanceof IFunction || element instanceof IMethod ) {
            updateMethodBreakpoints(false, true, part, (IDeclaration)element);
        } else {
            String text = ""; //$NON-NLS-1$
            if (selection instanceof ITextSelection) {
                text = ((ITextSelection)selection).getText();
            }
            createFunctionBreakpoint(true, part, null, ResourcesPlugin.getWorkspace().getRoot(), text, -1, -1, -1);
        }
	}
	
    /**
     * Updates the breakpoint for given part and selection.  
     * Depending on the flags and on whether a breakpoint exists, this method 
     * executes the toggle action.   
     * 
     * @param toggle Whether the toggle action is requested.  If 
     * true and the breakpoint currently exists, it will cause the
     * toggle action to either remove breakpoint or edit its properties.  
     * Otherwise a new breakpoint will be created.
     * @param interactive If true the toggle adapter should open a dialog before 
     * creating a breakpoint, or open a properties dialog on an existing 
     * breakpoint. 
     * @param part Workbench part where the toggle action is to be executed.
     * @param selection Current selection on which the toggle action is to be 
     * executed. 
     * @throws CoreException Any error in creating or editing the breakpoint.
     */
    private void updateBreakpoints(boolean toggle, boolean interactive, IWorkbenchPart part, ISelection selection ) throws CoreException {
        if ( canToggleLineBreakpoints( part, selection ) ) {
            updateLineBreakpoints(toggle, interactive, part, selection);
        }
        else {
            ICElement element = getCElementFromSelection( part, selection );
            if ( element instanceof IFunction || element instanceof IMethod ) {
                updateMethodBreakpoints(toggle, interactive, part, (IDeclaration)element);
            }
            else if ( element instanceof IVariable ) {
                updateVariableWatchpoint(toggle, interactive, part, (IVariable)element);
            }
        }
    }
    
    private void updateLineBreakpoints(boolean toggle, boolean interactive, IWorkbenchPart part, ISelection selection)
        throws CoreException {
        String errorMessage = null;
        if (part instanceof ITextEditor) {
            ITextEditor textEditor = (ITextEditor) part;
            IEditorInput input = textEditor.getEditorInput();
            if (input == null) {
                errorMessage = ActionMessages.getString("ToggleBreakpointAdapter.Empty_editor_1"); //$NON-NLS-1$
            } else {
                IDocument document = textEditor.getDocumentProvider().getDocument(input);
                if (document == null) {
                    errorMessage = ActionMessages.getString("ToggleBreakpointAdapter.Missing_document_1"); //$NON-NLS-1$
                } else {
                    IResource resource = getResource(textEditor);
                    if (resource == null) {
                        errorMessage = ActionMessages.getString("ToggleBreakpointAdapter.Missing_resource_1"); //$NON-NLS-1$
                    } else {
                        int lineNumber = ((ITextSelection) selection).getStartLine() + 1;
                        if (lineNumber == -1) {
                            errorMessage = ActionMessages.getString("ToggleBreakpointAdapter.Invalid_line_1"); //$NON-NLS-1$
                        } else {
                            String sourceHandle = getSourceHandle(input);
                            if (interactive && !toggle) {
                                createLineBreakpoint(true, part, sourceHandle, resource, lineNumber);
                            } else {
                                ICLineBreakpoint breakpoint = findLineBreakpoint(sourceHandle, resource, lineNumber);
                                if (breakpoint != null) {
                                    if (interactive) {
                                        CDebugUIUtils.editBreakpointProperties(part, breakpoint);
                                    } else {
                                        DebugPlugin.getDefault().getBreakpointManager()
                                            .removeBreakpoint(breakpoint, true);
                                    }
                                } else {
                                    createLineBreakpoint(interactive, part, sourceHandle, resource, lineNumber);
                                }
                            }
                            return;
                        }
                    }
                }
            }
        } else {
            errorMessage = ActionMessages.getString("RunToLineAdapter.Operation_is_not_supported_1"); //$NON-NLS-1$
        }
        throw new CoreException(new Status(IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(),
            IInternalCDebugUIConstants.INTERNAL_ERROR, errorMessage, null));
    }

    private void updateMethodBreakpoints(boolean toggle, boolean interactive, IWorkbenchPart part,
        IDeclaration declaration) throws CoreException {
        String sourceHandle = getSourceHandle(declaration);
        IResource resource = getElementResource(declaration);
        String functionName = (declaration instanceof IFunction) ? getFunctionName((IFunction) declaration)
            : getMethodName((IMethod) declaration);
        ICFunctionBreakpoint breakpoint = findFunctionBreakpoint(sourceHandle, resource, functionName);
        if (toggle && breakpoint != null) {
            if (interactive) {
                CDebugUIUtils.editBreakpointProperties(part, breakpoint);
            } else {
                DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
            }
        } else {
            int lineNumber = -1;
            int charStart = -1;
            int charEnd = -1;
            try {
                ISourceRange sourceRange = declaration.getSourceRange();
                if (sourceRange != null) {
                    charStart = sourceRange.getStartPos();
                    charEnd = charStart + sourceRange.getLength();
                    if (charEnd <= 0) {
                        charStart = -1;
                        charEnd = -1;
                    }
                    lineNumber = sourceRange.getStartLine();
                }
            } catch (CModelException e) {
                DebugPlugin.log(e);
            }
            createFunctionBreakpoint(interactive, part, sourceHandle, resource, functionName, charStart, charEnd,
                lineNumber);
        }
    }
    
    /**
     * Updates a watchpoint. Depending on the flags and on whether a breakpoint
     * exists, this method executes the toggle action.
     * 
     * @param toggle
     *            Whether the toggle action is requested. If true and the
     *            breakpoint currently exists, it will cause the toggle action
     *            to either remove breakpoint or edit its properties. Otherwise
     *            a new breakpoint will be created.
     * @param interactive
     *            If true the toggle adapter should open a dialog before
     *            creating a breakpoint, or open a properties dialog on an
     *            existing breakpoint.
     * @param part
     *            Workbench part where the toggle action is to be executed.
     * @param selection
     *            Variable on which to execute the toggle action.
     * @throws CoreException
     *             Any error in creating or editing the breakpoint.
     */
    private void updateVariableWatchpoint(boolean toggle, boolean interactive, IWorkbenchPart part, IVariable variable)
        throws CoreException {
        String sourceHandle = getSourceHandle(variable);
        IResource resource = getElementResource(variable);
        String expression = getVariableName(variable);
        ICWatchpoint watchpoint = findWatchpoint(sourceHandle, resource, expression);
        if (toggle && watchpoint != null) {
            if (interactive) {
                CDebugUIUtils.editBreakpointProperties(part, watchpoint);
            } else {
                DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(watchpoint, true);
            }
        } else {
            int lineNumber = -1;
            int charStart = -1;
            int charEnd = -1;
            try {
                ISourceRange sourceRange = variable.getSourceRange();
                if (sourceRange != null) {
                    charStart = sourceRange.getStartPos();
                    charEnd = charStart + sourceRange.getLength();
                    if (charEnd <= 0) {
                        charStart = -1;
                        charEnd = -1;
                    }
                    lineNumber = sourceRange.getStartLine();
                }
            } catch (CModelException e) {
                CDebugUIPlugin.log(e);
            }
            createWatchpoint(interactive, part, sourceHandle, resource, charStart, charEnd, lineNumber, expression);
        }
    }

	/**
     * Returns the C model element at the given selection. 
     * @param part Workbench part where the selection is.
     * @param selection Selection in part.
     * @return C model element if found.
     */
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

    /**
     * Returns the variable object at the given selection.
     * Returns the C model element at the given selection. 
     * @param part Workbench part where the selection is.
     * @param selection Selection in part.
     * @return C model variable, if found.
     */
	protected IVariable getVariableFromSelection( IWorkbenchPart part, ISelection selection ) {
		ICElement element = getCElementFromSelection( part, selection );
		if ( element instanceof IVariable ) {
			return (IVariable)element;
		}
		return null;
	}

	/**
	 * Reports the given error message to the user.
	 * @param message Message to report.
	 * @param part Workbench part where action was invoked.
	 */
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

	/**
	 * Returns the resource being edited in the given workbench part. 
	 * @param part Workbench part to checm.
	 * @return Resource being edited.
	 */
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

	protected String getSourceHandle( IDeclaration declaration ) {
		ITranslationUnit tu = declaration.getTranslationUnit();
		if ( tu != null ) {
			IPath location = tu.getLocation();
			if ( location != null ) {
				return location.toOSString();
			}
		}
		return ""; //$NON-NLS-1$
	}

	protected IResource getElementResource( IDeclaration declaration ) {
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

	private boolean toggleBreakpointEnable(IWorkbenchPart part) {
        ITextEditor textEditor = getTextEditor(part);
        if(textEditor != null) {
            IVerticalRulerInfo info = (IVerticalRulerInfo) textEditor.getAdapter(IVerticalRulerInfo.class);
            if(info != null) {
                EnableDisableBreakpointRulerAction enableAction = new EnableDisableBreakpointRulerAction(part, info);
                enableAction.update();
                enableAction.run();
            }
        }
        return false;
    }

    /**
     * Returns the text editor associated with the given part or <code>null</code>
     * if none. In case of a multi-page editor, this method should be used to retrieve
     * the correct editor to perform the breakpoint operation on.
     * 
     * @param part workbench part
     * @return text editor part or <code>null</code>
     */
    protected ITextEditor getTextEditor(IWorkbenchPart part) {
        if (part instanceof ITextEditor) {
            return (ITextEditor) part;
        }
        return (ITextEditor) part.getAdapter(ITextEditor.class);
    }

    /**
     * Resolves the {@link IBreakpoint} from the given editor and ruler information. Returns <code>null</code>
     * if no breakpoint exists or the operation fails.
     * 
     * @param editor the editor
     * @param info the current ruler information
     * @return the {@link IBreakpoint} from the current editor position or <code>null</code>
     */
    protected IBreakpoint getBreakpointFromEditor(ITextEditor editor, IVerticalRulerInfo info) {
        IAnnotationModel annotationModel = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
        IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        if (annotationModel != null) {
            @SuppressWarnings("unchecked")
            Iterator<Annotation> iterator = annotationModel.getAnnotationIterator();
            while (iterator.hasNext()) {
                Object object = iterator.next();
                if (object instanceof SimpleMarkerAnnotation) {
                    SimpleMarkerAnnotation markerAnnotation = (SimpleMarkerAnnotation) object;
                    IMarker marker = markerAnnotation.getMarker();
                    try {
                        if (marker.isSubtypeOf(IBreakpoint.BREAKPOINT_MARKER)) {
                            Position position = annotationModel.getPosition(markerAnnotation);
                            int line = document.getLineOfOffset(position.getOffset());
                            if (line == info.getLineOfLastMouseButtonActivity()) {
                                IBreakpoint breakpoint = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
                                if (breakpoint != null) {
                                    return breakpoint;
                                }
                            }
                        }
                    } catch (CoreException e) {
                    } catch (BadLocationException e) {
                    }
                }
            }
        }
        return null;
    }

    /**
     * Opens the properties dialog for the given breakpoint. This method can be
     * used on an existing breakpoint or on a blank breakpoint which doesn't
     * have an associated marker yet.
     * 
     * @param bp
     *            The breakpoint to edit. This breakpoint may not have an
     *            associated marker yet.
     * @param part
     *            Workbench part where the action was invoked.
     * @param resource
     *            Workbench resource to create the breakpoint on.
     * @param attributes
     *            Breakpoint attributes to show in properties dialog. If the
     *            breakpoint already exists, this attribute map can be used to
     *            override the attributes currently in the breakpoint. Can be
     *            <code>null</code>.
     */
    protected void openBreakpointPropertiesDialog(ICBreakpoint bp, IWorkbenchPart part, IResource resource,
        Map<String, Object> attributes) {
        ISelection debugContext = DebugUITools.getDebugContextManager()
            .getContextService(part.getSite().getWorkbenchWindow()).getActiveContext(part.getSite().getId());
        CBreakpointContext bpContext = new CBreakpointContext(bp, debugContext, resource, attributes);

        PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(part.getSite().getShell(), bpContext, null,
            null, null);
        if (dialog != null) {
            dialog.open();
        }
    }
    
    /**
     * Finds the line breakpoint at the given location.
     * 
     * @param sourceHandle
     *            Source handle for the line breakpoint.
     * @param resource
     *            Resource of the line breakpoint.
     * @param lineNumber
     *            Line number.
     * @return Line breakpoint with given parameters, if found.
     * @throws CoreException
     *             Exception thrown while reading breakpoints' properties.
     */
    protected abstract ICLineBreakpoint findLineBreakpoint(String sourceHandle, IResource resource, int lineNumber)
        throws CoreException;

	/**
	 * Creates a line breakpoint at the given location.
	 * @param interactive true if action should open a dialog to let user edit
	 * breakpoint properties prior to creation. 
	 * @param part Workbench part where action was invoked.
	 * @param source Handle Source handle for the new breakpoint.
	 * @param resource Resource to create breakpoint on.
	 * @param lineNumber Line number for new breakpoint.
	 * @throws CoreException Exception while creating breakpoint.
	 */
	protected abstract void createLineBreakpoint(boolean interactive, IWorkbenchPart part, String sourceHandle, 
	    IResource resource, int lineNumber) throws CoreException;

    /**
     * Finds the function breakpoint at the given location.
     * 
     * @param sourceHandle
     *            Source handle for the line breakpoint.
     * @param resource
     *            Resource of the line breakpoint.
     * @param functionName
     *            Function for the breakpoint.
     * @return Function breakpoint with given parameters, if found.
     * @throws CoreException
     *             Exception thrown while reading breakpoints' properties.
     */
    protected abstract ICFunctionBreakpoint findFunctionBreakpoint(String sourceHandle, IResource resource,
        String functionName) throws CoreException;

    /**
     * Creates a function breakpoint at the given location.
     * @param interactive true if action should open a dialog to let user edit
     * breakpoint properties prior to creation. 
     * @param part Workbench part where action was invoked.
     * @param source Handle Source handle for the new breakpoint.
     * @param resource Resource to create breakpoint on.
     * @param charStart Beginning of range where function is located. Can be 
     * -1 if not known.
     * @param charStart End of range where function is located. Can be 
     * -1 if not known.
     * @param lineNumber Line number where the function is located.
     * @throws CoreException Exception while creating breakpoint.
     */
    protected abstract void createFunctionBreakpoint(boolean interactive, IWorkbenchPart part, String sourceHandle,
        IResource resource, String functionName, int charStart, int charEnd, int lineNumber) throws CoreException;

	/**
	 * Finds the watchpoint with given expression.
	 * 
     * @param sourceHandle Source handle for the line breakpoint.
     * @param resource Resource of the line breakpoint.
     * @param expression Expression of the breakpoint.
     * @return Watchpoing with given parameters, if found.
     * @throws CoreException Exception thrown while reading breakpoints'  
	 */
	protected abstract ICWatchpoint findWatchpoint( String sourceHandle, IResource resource, String expression ) 
	    throws CoreException;

    /**
     * Creates a watchpoint at the given location.
     * @param interactive true if action should open a dialog to let user edit
     * breakpoint properties prior to creation. 
     * @param part Workbench part where action was invoked.
     * @param source Handle Source handle for the new breakpoint.
     * @param resource Resource to create breakpoint on.
     * @param charStart Beginning of range where variable is located. Can be 
     * -1 if not known.
     * @param charStart End of range where variable is located. Can be 
     * -1 if not known.
     * @param lineNumber Line number where the variable is located.
     * @throws CoreException Exception while creating breakpoint.
     */
    protected abstract void createWatchpoint(boolean interactive, IWorkbenchPart part, String sourceHandle,
        IResource resource, int charStart, int charEnd, int lineNumber, String expression) throws CoreException;
    
}
