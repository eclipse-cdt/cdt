/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ken Ryall (Nokia) - Added support for CSourceNotFoundElement ( 167305 )
 * ARM Limited - https://bugs.eclipse.org/bugs/show_bug.cgi?id=186981
 * Ken Ryall (Nokia) - Bug 201165 don't toss images on dispose.
 * Ericsson          - Bug 284286 support for tracepoints
 * Marc Khouzam (Ericsson) - Added dynamic printf support (400628)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import java.io.File;
import java.net.URI;
import java.util.HashMap;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.cdt.debug.core.model.ICDynamicPrintf;
import org.eclipse.cdt.debug.core.model.ICEventBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICTracepoint;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.core.model.IDummyStackFrame;
import org.eclipse.cdt.debug.core.model.IEnableDisableTarget;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceNotFoundElement;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.CSourceNotFoundEditorInput;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.cdt.internal.core.model.ExternalTranslationUnit;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.icu.text.MessageFormat;

/**
 * @see IDebugModelPresentation
 */
public class CDebugModelPresentation extends LabelProvider implements IDebugModelPresentation, IColorProvider {

	public final static String DISPLAY_FULL_PATHS = "DISPLAY_FULL_PATHS"; //$NON-NLS-1$

	private static final String DUMMY_STACKFRAME_LABEL = "..."; //$NON-NLS-1$

	protected HashMap<String, Object> fAttributes = new HashMap<>(3);

	protected CDebugImageDescriptorRegistry fDebugImageRegistry = CDebugUIPlugin.getImageDescriptorRegistry();

	private OverlayImageCache fImageCache = new OverlayImageCache();

	private static CDebugModelPresentation gfInstance = null;

	public static CDebugModelPresentation getDefault() {
		if (gfInstance == null)
			gfInstance = new CDebugModelPresentation();
		return gfInstance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setAttribute(String attribute, Object value) {
		if (value == null)
			return;
		getAttributes().put(attribute, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(org.eclipse.debug.core.model.IValue, org.eclipse.debug.ui.IValueDetailListener)
	 */
	@Override
	public void computeDetail(IValue value, IValueDetailListener listener) {
		CValueDetailProvider.getDefault().computeDetail(value, listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(java.lang.Object)
	 */
	@Override
	public IEditorInput getEditorInput(Object element) {
		if (element instanceof IMarker) {
			IResource resource = ((IMarker) element).getResource();
			if (resource instanceof IFile)
				return new FileEditorInput((IFile) resource);
		}
		if (element instanceof IFile) {
			return new FileEditorInput((IFile) element);
		}
		if (element instanceof ICBreakpoint) {
			ICBreakpoint b = (ICBreakpoint) element;
			IMarker marker = b.getMarker();
			if (marker == null || !marker.exists())
				return null;
			// If the BP's marker is on an IFile, job done
			IFile file = marker.getResource().getAdapter(IFile.class);
			if (file == null) {
				try {
					// Not backed by an IFile, try its source handle (may be workspace / project based)
					String handle = b.getSourceHandle();
					if (handle != null && Path.ROOT.isValidPath(handle)) {
						Path path = new Path(handle);
						IProject project = marker.getResource().getProject();
						// Select the most 'relevant' IFile for an external location
						file = ResourceLookup.selectFileForLocation(path, project);
						if (file == null || !file.isAccessible()) {
							// Try resolving the path to a real io.File
							File fsfile = new File(handle);
							if (fsfile.isFile() && fsfile.exists()) {
								// create an ExternalEditorInput with an external tu so when you
								// open the file from the breakpoints view it opens in the
								// proper editor.
								if (project != null) {
									ICProject cproject = CoreModel.getDefault().create(project);
									String id = CoreModel.getRegistedContentTypeId(project, path.lastSegment());
									ExternalTranslationUnit tu = new ExternalTranslationUnit(cproject,
											URIUtil.toURI(path), id);
									return new ExternalEditorInput(tu);
								} else {
									return new ExternalEditorInput(path);
								}
							}
						}
					}
				} catch (CoreException e) {
					CDebugCorePlugin.log(e);
				}
			}
			if (file != null)
				return new FileEditorInput(file);
			// There is no file associated with this breakpoint. See if another editor is available from an adapter
			ISourcePresentation srcPres = Platform.getAdapterManager().getAdapter(b, ISourcePresentation.class);
			if (srcPres != null) {
				IEditorInput editor = srcPres.getEditorInput(b);
				if (editor != null) {
					return editor;
				}
			}
		}
		if (element instanceof FileStorage || element instanceof LocalFileStorage) {
			return new ExternalEditorInput(((IStorage) element).getFullPath());
		}
		if (element instanceof ExternalTranslationUnit) {
			ExternalTranslationUnit etu = (ExternalTranslationUnit) element;
			return new ExternalEditorInput(etu);
		}
		if (element instanceof CSourceNotFoundElement) {
			return new CSourceNotFoundEditorInput(element);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(org.eclipse.ui.IEditorInput, java.lang.Object)
	 */
	@Override
	public String getEditorId(IEditorInput input, Object element) {
		if (element instanceof CSourceNotFoundElement)
			return ICDebugUIConstants.CSOURCENOTFOUND_EDITOR_ID;
		String id = null;
		if (input != null) {
			IEditorDescriptor descriptor = null;
			if (input instanceof IFileEditorInput) {
				IFileEditorInput fileEditorInput = (IFileEditorInput) input;
				IFile file = fileEditorInput.getFile();
				descriptor = IDE.getDefaultEditor(file);
			} else if (input instanceof IURIEditorInput) {
				IURIEditorInput uriEditorInput = (IURIEditorInput) input;
				URI uri = uriEditorInput.getURI();
				try {
					IFileStore fileStore = EFS.getStore(uri);
					id = IDE.getEditorDescriptorForFileStore(fileStore, false).getId();
				} catch (CoreException e) {
					// fallback to default case
				}
			}
			if (id == null) {
				if (descriptor == null) {
					IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
					descriptor = registry.getDefaultEditor(input.getName());
				}

				id = CUIPlugin.EDITOR_ID;
				if (descriptor != null) {
					id = descriptor.getId();
				}
			}

			if (id == null && element instanceof ICBreakpoint) {
				// There is no associated editor ID for this breakpoint, see if an alternative can be supplied from an adapter.
				ISourcePresentation sourcePres = Platform.getAdapterManager().getAdapter(element,
						ISourcePresentation.class);
				if (sourcePres != null) {
					String lid = sourcePres.getEditorId(input, element);
					if (lid != null) {
						id = lid;
					}
				}
			}
		}
		return id;
	}

	@Override
	public Image getImage(Object element) {
		Image baseImage = getBaseImage(element);
		if (baseImage != null) {
			ImageDescriptor[] overlays = new ImageDescriptor[] { null, null, null, null };
			if (element instanceof ICDebugElementStatus && !((ICDebugElementStatus) element).isOK()) {
				switch (((ICDebugElementStatus) element).getSeverity()) {
				case ICDebugElementStatus.WARNING:
					overlays[OverlayImageDescriptor.BOTTOM_LEFT] = CDebugImages.DESC_OVRS_WARNING;
					break;
				case ICDebugElementStatus.ERROR:
					overlays[OverlayImageDescriptor.BOTTOM_LEFT] = CDebugImages.DESC_OVRS_ERROR;
					break;
				}
			}
			if (element instanceof IWatchExpression && ((IWatchExpression) element).hasErrors())
				overlays[OverlayImageDescriptor.BOTTOM_LEFT] = CDebugImages.DESC_OVRS_ERROR;
			return getImageCache().getImageFor(new OverlayImageDescriptor(baseImage, overlays));
		}
		return null;
	}

	private Image getBaseImage(Object element) {
		if (element instanceof IMarker) {
			IBreakpoint bp = getBreakpoint((IMarker) element);
			if (bp != null && bp instanceof ICBreakpoint) {
				return getBreakpointImage((ICBreakpoint) bp);
			}
		}
		if (element instanceof ICBreakpoint) {
			return getBreakpointImage((ICBreakpoint) element);
		}
		if (element instanceof IRegisterGroup) {
			return getRegisterGroupImage((IRegisterGroup) element);
		}
		if (element instanceof IExpression) {
			return getExpressionImage((IExpression) element);
		}
		if (element instanceof ICModule) {
			return getModuleImage((ICModule) element);
		}
		if (element instanceof ICSignal) {
			return getSignalImage((ICSignal) element);
		}
		return super.getImage(element);
	}

	protected Image getSignalImage(ICSignal signal) {
		return CDebugUIPlugin.getImageDescriptorRegistry().get(CDebugImages.DESC_OBJS_SIGNAL);
	}

	protected Image getRegisterGroupImage(IRegisterGroup element) {
		IEnableDisableTarget target = element.getAdapter(IEnableDisableTarget.class);
		if (target != null && !target.isEnabled())
			return fDebugImageRegistry.get(CDebugImages.DESC_OBJS_REGISTER_GROUP_DISABLED);
		return fDebugImageRegistry.get(CDebugImages.DESC_OBJS_REGISTER_GROUP);
	}

	protected Image getBreakpointImage(ICBreakpoint breakpoint) {
		// if adapter installed for breakpoint, call the adapter
		ILabelProvider adapter = Platform.getAdapterManager().getAdapter(breakpoint, ILabelProvider.class);
		if (adapter != null) {
			Image image = adapter.getImage(breakpoint);
			if (image != null)
				return image;
		}
		try {
			// Check for ICTracepoint first because they are also ICLineBreakpoint
			if (breakpoint instanceof ICTracepoint) {
				return getTracepointImage((ICTracepoint) breakpoint);
			}
			// Check for ICDynamicPrintf first because they are also ICLineBreakpoint
			if (breakpoint instanceof ICDynamicPrintf) {
				return getDynamicPrintfImage((ICDynamicPrintf) breakpoint);
			}
			if (breakpoint instanceof ICLineBreakpoint) {
				// checks if the breakpoint type is a hardware breakpoint,
				// if so, return the hardware breakpoint image
				if (breakpoint instanceof ICBreakpointType) {
					ICBreakpointType breakpointType = (ICBreakpointType) breakpoint;
					if ((breakpointType.getType() & ICBreakpointType.HARDWARE) != 0)
						return getHWBreakpointImage((ICLineBreakpoint) breakpoint);
				}
				return getLineBreakpointImage((ICLineBreakpoint) breakpoint);
			}
			if (breakpoint instanceof ICWatchpoint) {
				return getWatchpointImage((ICWatchpoint) breakpoint);
			}
			if (breakpoint instanceof ICEventBreakpoint) {
				return getEventBreakpointImage((ICEventBreakpoint) breakpoint);
			}

		} catch (CoreException e) {
		}
		return null;
	}

	protected Image getHWBreakpointImage(ICLineBreakpoint breakpoint) throws CoreException {
		ImageDescriptor descriptor = null;
		if (breakpoint.isEnabled()) {
			descriptor = CDebugImages.DESC_OBJS_HWBREAKPOINT_ENABLED;
		} else {
			descriptor = CDebugImages.DESC_OBJS_HWBREAKPOINT_DISABLED;
		}
		return getImageCache().getImageFor(
				new OverlayImageDescriptor(fDebugImageRegistry.get(descriptor), computeOverlays(breakpoint)));
	}

	protected Image getDynamicPrintfImage(ICDynamicPrintf dynamicPrintf) throws CoreException {
		ImageDescriptor descriptor = null;
		if (dynamicPrintf.isEnabled()) {
			descriptor = CDebugImages.DESC_OBJS_DYNAMICPRINTF_ENABLED;
		} else {
			descriptor = CDebugImages.DESC_OBJS_DYNAMICPRINTF_DISABLED;
		}
		return getImageCache().getImageFor(
				new OverlayImageDescriptor(fDebugImageRegistry.get(descriptor), computeOverlays(dynamicPrintf)));
	}

	protected Image getTracepointImage(ICTracepoint tracepoint) throws CoreException {
		ImageDescriptor descriptor = null;
		if (tracepoint.isEnabled()) {
			descriptor = CDebugImages.DESC_OBJS_TRACEPOINT_ENABLED;
		} else {
			descriptor = CDebugImages.DESC_OBJS_TRACEPOINT_DISABLED;
		}
		return getImageCache().getImageFor(
				new OverlayImageDescriptor(fDebugImageRegistry.get(descriptor), computeOverlays(tracepoint)));
	}

	protected Image getLineBreakpointImage(ICLineBreakpoint breakpoint) throws CoreException {
		ImageDescriptor descriptor = null;
		if (breakpoint.isEnabled()) {
			descriptor = CDebugImages.DESC_OBJS_BREAKPOINT_ENABLED;
		} else {
			descriptor = CDebugImages.DESC_OBJS_BREAKPOINT_DISABLED;
		}
		return getImageCache().getImageFor(
				new OverlayImageDescriptor(fDebugImageRegistry.get(descriptor), computeOverlays(breakpoint)));
	}

	protected Image getWatchpointImage(ICWatchpoint watchpoint) throws CoreException {
		ImageDescriptor descriptor = null;
		if (watchpoint.isEnabled()) {
			if (watchpoint.isReadType() && !watchpoint.isWriteType())
				descriptor = CDebugImages.DESC_OBJS_READ_WATCHPOINT_ENABLED;
			else if (!watchpoint.isReadType() && watchpoint.isWriteType())
				descriptor = CDebugImages.DESC_OBJS_WRITE_WATCHPOINT_ENABLED;
			else
				descriptor = CDebugImages.DESC_OBJS_WATCHPOINT_ENABLED;
		} else {
			if (watchpoint.isReadType() && !watchpoint.isWriteType())
				descriptor = CDebugImages.DESC_OBJS_READ_WATCHPOINT_DISABLED;
			else if (!watchpoint.isReadType() && watchpoint.isWriteType())
				descriptor = CDebugImages.DESC_OBJS_WRITE_WATCHPOINT_DISABLED;
			else
				descriptor = CDebugImages.DESC_OBJS_WATCHPOINT_DISABLED;
		}
		return getImageCache().getImageFor(
				new OverlayImageDescriptor(fDebugImageRegistry.get(descriptor), computeOverlays(watchpoint)));
	}

	protected Image getEventBreakpointImage(ICEventBreakpoint evtBreakpoint) throws CoreException {
		ImageDescriptor descriptor = evtBreakpoint.isEnabled() ? CDebugImages.DESC_OBJS_EVENTBREAKPOINT_ENABLED
				: CDebugImages.DESC_OBJS_EVENTBREAKPOINT_DISABLED;
		return getImageCache().getImageFor(
				new OverlayImageDescriptor(fDebugImageRegistry.get(descriptor), computeOverlays(evtBreakpoint)));
	}

	@Override
	public String getText(Object element) {
		String bt = getBaseText(element);
		if (bt == null)
			return null;
		return CDebugUIUtils.decorateText(element, bt);
	}

	private String getBaseText(Object element) {
		boolean showQualified = isShowQualifiedNames();
		StringBuilder label = new StringBuilder();
		try {
			if (element instanceof ICModule) {
				label.append(getModuleText((ICModule) element, showQualified));
				return label.toString();
			}
			if (element instanceof ICSignal) {
				label.append(getSignalText((ICSignal) element));
				return label.toString();
			}
			if (element instanceof IRegisterGroup) {
				label.append(((IRegisterGroup) element).getName());
				return label.toString();
			}
			if (element instanceof IWatchExpression) {
				return getWatchExpressionText((IWatchExpression) element);
			}
			if (element instanceof IValue) {
				label.append(getValueText((IValue) element));
				return label.toString();
			}
			if (element instanceof IStackFrame) {
				label.append(getStackFrameText((IStackFrame) element, showQualified));
				return label.toString();
			}
			if (element instanceof CSourceNotFoundElement) {
				return getBaseText(((CSourceNotFoundElement) element).getElement());
			}
			if (element instanceof IMarker) {
				IBreakpoint breakpoint = getBreakpoint((IMarker) element);
				if (breakpoint != null) {
					return CDebugUtils.getBreakpointText(breakpoint, showQualified);
				}
				return null;
			}
			if (element instanceof IBreakpoint) {
				// if adapter installed for breakpoint, call adapter
				ILabelProvider adapter = Platform.getAdapterManager().getAdapter(element, ILabelProvider.class);
				if (adapter != null) {
					String text = adapter.getText(element);
					if (text != null)
						return text;
				}
				return CDebugUtils.getBreakpointText((IBreakpoint) element, showQualified);
			}
			if (element instanceof IDebugTarget)
				label.append(getTargetText((IDebugTarget) element, showQualified));
			if (element instanceof ITerminate) {
				if (((ITerminate) element).isTerminated()) {
					label.insert(0, CDebugUIMessages.getString("CDTDebugModelPresentation.0")); //$NON-NLS-1$
					return label.toString();
				}
			}
			if (element instanceof IDisconnect) {
				if (((IDisconnect) element).isDisconnected()) {
					label.insert(0, CDebugUIMessages.getString("CDTDebugModelPresentation.1")); //$NON-NLS-1$
					return label.toString();
				}
			}
			if (label.length() > 0) {
				return label.toString();
			}
		} catch (DebugException e) {
			return MessageFormat.format(CDebugUIMessages.getString("CDTDebugModelPresentation.2"), e.getMessage()); //$NON-NLS-1$
		} catch (CoreException e) {
			CDebugUIPlugin.log(e);
		}
		return null;
	}

	protected String getModuleText(ICModule module, boolean qualified) {
		StringBuilder sb = new StringBuilder();
		IPath path = module.getImageName();
		if (!path.isEmpty()) {
			sb.append(path.lastSegment());
		} else {
			sb.append(CDebugUIMessages.getString("CDebugModelPresentation.unknown_1")); //$NON-NLS-1$
		}
		return sb.toString();
	}

	protected String getRegisterGroupText(IRegisterGroup group) {
		String name = CDebugUIMessages.getString("CDebugModelPresentation.not_available_1"); //$NON-NLS-1$
		try {
			name = group.getName();
		} catch (DebugException e) {
			CDebugUIPlugin.log(e.getStatus());
		}
		return name;
	}

	protected boolean isShowQualifiedNames() {
		Boolean showQualified = (Boolean) getAttributes().get(DISPLAY_FULL_PATHS);
		showQualified = showQualified == null ? Boolean.FALSE : showQualified;
		return showQualified.booleanValue();
	}

	private HashMap<String, Object> getAttributes() {
		return this.fAttributes;
	}

	private OverlayImageCache getImageCache() {
		return this.fImageCache;
	}

	private boolean isEmpty(String string) {
		return (string == null || string.trim().length() == 0);
	}

	protected IBreakpoint getBreakpoint(IMarker marker) {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
	}

	private ImageDescriptor[] computeOverlays(ICBreakpoint breakpoint) {
		ImageDescriptor[] overlays = new ImageDescriptor[] { null, null, null, null };
		try {
			if (CDebugCorePlugin.getDefault().getBreakpointActionManager().breakpointHasActions(breakpoint)) {
				overlays[OverlayImageDescriptor.BOTTOM_RIGHT] = (breakpoint.isEnabled())
						? CDebugImages.DESC_OVRS_BREAKPOINT_WITH_ACTIONS
						: CDebugImages.DESC_OVRS_BREAKPOINT_WITH_ACTIONS_DISABLED;
			}
			if (breakpoint.isConditional()) {
				overlays[OverlayImageDescriptor.TOP_LEFT] = (breakpoint.isEnabled())
						? CDebugImages.DESC_OVRS_BREAKPOINT_CONDITIONAL
						: CDebugImages.DESC_OVRS_BREAKPOINT_CONDITIONAL_DISABLED;
			}
			if (breakpoint.isInstalled()) {
				overlays[OverlayImageDescriptor.BOTTOM_LEFT] = (breakpoint.isEnabled())
						? CDebugImages.DESC_OVRS_BREAKPOINT_INSTALLED
						: CDebugImages.DESC_OVRS_BREAKPOINT_INSTALLED_DISABLED;
			}
			if (breakpoint instanceof ICAddressBreakpoint) {
				overlays[OverlayImageDescriptor.TOP_RIGHT] = (breakpoint.isEnabled())
						? CDebugImages.DESC_OVRS_ADDRESS_BREAKPOINT
						: CDebugImages.DESC_OVRS_ADDRESS_BREAKPOINT_DISABLED;
			}
			if (breakpoint instanceof ICFunctionBreakpoint) {
				overlays[OverlayImageDescriptor.TOP_RIGHT] = (breakpoint.isEnabled())
						? CDebugImages.DESC_OVRS_FUNCTION_BREAKPOINT
						: CDebugImages.DESC_OVRS_FUNCTION_BREAKPOINT_DISABLED;
			}
		} catch (CoreException e) {
			CDebugUIPlugin.log(e);
		}
		return overlays;
	}

	protected Image getExpressionImage(IExpression element) {
		return fDebugImageRegistry.get(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_EXPRESSION));
	}

	protected Image getModuleImage(ICModule element) {
		switch (element.getType()) {
		case ICModule.EXECUTABLE:
			if (element.areSymbolsLoaded()) {
				return CDebugUIPlugin.getImageDescriptorRegistry().get(CDebugImages.DESC_OBJS_EXECUTABLE_WITH_SYMBOLS);
			}
			return CDebugUIPlugin.getImageDescriptorRegistry().get(CDebugImages.DESC_OBJS_EXECUTABLE);
		case ICModule.SHARED_LIBRARY:
			if (element.areSymbolsLoaded()) {
				return CDebugUIPlugin.getImageDescriptorRegistry()
						.get(CDebugImages.DESC_OBJS_SHARED_LIBRARY_WITH_SYMBOLS);
			}
			return CDebugUIPlugin.getImageDescriptorRegistry().get(CDebugImages.DESC_OBJS_SHARED_LIBRARY);
		}
		return null;
	}

	protected String getValueText(IValue value) {
		return CDebugUIUtils.getValueText(value);
	}

	protected String getSignalText(ICSignal signal) {
		StringBuilder sb = new StringBuilder(CDebugUIMessages.getString("CDTDebugModelPresentation.12")); //$NON-NLS-1$
		try {
			String name = signal.getName();
			sb.append(" \'").append(name).append('\''); //$NON-NLS-1$
		} catch (DebugException e) {
		}
		return sb.toString();
	}

	protected String getWatchExpressionText(IWatchExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append('"').append(expression.getExpressionText()).append('"');
		if (expression.isPending()) {
			result.append(" = ").append("..."); //$NON-NLS-1$//$NON-NLS-2$
		} else {
			IValue value = expression.getValue();
			if (value instanceof ICValue) {
				ICType type = null;
				try {
					type = ((ICValue) value).getType();
				} catch (DebugException e1) {
				}
				if (type != null && isShowVariableTypeNames()) {
					String typeName = CDebugUIUtils.getVariableTypeName(type);
					if (!isEmpty(typeName)) {
						result.insert(0, typeName + ' ');
					}
				}
				if (expression.isEnabled()) {
					String valueString = getValueText(value);
					if (valueString.length() > 0) {
						result.append(" = ").append(valueString); //$NON-NLS-1$
					}
				}
			}
		}
		if (!expression.isEnabled()) {
			result.append(' ');
			result.append(CDebugUIMessages.getString("CDTDebugModelPresentation.22")); //$NON-NLS-1$
		}
		return result.toString();
	}

	protected String getTargetText(IDebugTarget target, boolean qualified) throws DebugException {
		return target.getName();
	}

	protected String getStackFrameText(IStackFrame f, boolean qualified) throws DebugException {
		if (f instanceof ICStackFrame) {
			ICStackFrame frame = (ICStackFrame) f;
			StringBuilder label = new StringBuilder();
			label.append(frame.getLevel());
			label.append(' ');
			String function = frame.getFunction();
			if (isEmpty(function)) {
				label.append(CDebugUIMessages.getString("CDTDebugModelPresentation.21")); //$NON-NLS-1$
			} else {
				function = function.trim();
				label.append(function);
				label.append("() "); //$NON-NLS-1$
				if (frame.getFile() != null) {
					IPath path = new Path(frame.getFile());
					if (!path.isEmpty()) {
						label.append((qualified ? path.toOSString() : path.lastSegment()));
						label.append(':');
						if (frame.getFrameLineNumber() != 0)
							label.append(frame.getFrameLineNumber());
					}
				}
			}
			IAddress address = frame.getAddress();
			if (address != null) {
				label.append(' ');
				label.append(address.toHexAddressString());
			}
			return label.toString();
		}
		return (f.getAdapter(IDummyStackFrame.class) != null) ? getDummyStackFrameLabel(f) : f.getName();
	}

	private String getDummyStackFrameLabel(IStackFrame stackFrame) {
		return DUMMY_STACKFRAME_LABEL;
	}

	protected boolean isShowVariableTypeNames() {
		Boolean show = (Boolean) fAttributes.get(DISPLAY_VARIABLE_TYPE_NAMES);
		show = show == null ? Boolean.FALSE : show;
		return show.booleanValue();
	}

	public static String getFormattedString(String key, String arg) {
		return getFormattedString(key, new String[] { arg });
	}

	public static String getFormattedString(String string, String[] args) {
		return MessageFormat.format(string, (Object[]) args);
	}

	@Override
	public Color getForeground(Object element) {
		IColorProvider colorProv = Platform.getAdapterManager().getAdapter(element, IColorProvider.class);
		if (colorProv != null) {
			return colorProv.getForeground(element);
		}
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		IColorProvider colorProv = Platform.getAdapterManager().getAdapter(element, IColorProvider.class);
		if (colorProv != null) {
			return colorProv.getBackground(element);
		}
		return null;
	}
}
