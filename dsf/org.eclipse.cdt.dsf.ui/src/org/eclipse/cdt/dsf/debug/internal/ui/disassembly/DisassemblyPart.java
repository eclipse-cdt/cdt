/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Bug fix (326670)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import static org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyUtils.getAddressText;
import static org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyUtils.internalError;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyPosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyUtils;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.ErrorPosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyDocument;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.LabelPosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions.AbstractDisassemblyAction;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions.ActionGotoAddress;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions.ActionGotoProgramCounter;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions.ActionOpenPreferences;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions.AddressBarContributionItem;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions.JumpToAddressAction;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions.TextOperationAction;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.BreakpointsAnnotationModel;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyDocument;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.SourceFileInfo;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.preferences.DisassemblyPreferenceConstants;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation.DisassemblyIPAnnotation;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.util.HSL;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.internal.ui.dnd.TextViewerDragAdapter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.IVerticalRulerExtension;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
/**
 * DisassemblyPart
 */
@SuppressWarnings("restriction")
public abstract class DisassemblyPart extends WorkbenchPart implements IDisassemblyPart, IViewportListener, ITextPresentationListener, IDisassemblyPartCallback  {

	final static boolean DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.cdt.dsf.ui/debug/disassembly"));  //$NON-NLS-1$//$NON-NLS-2$

	/**
	 * Annotation model attachment key for breakpoint annotations.
	 */
	private final static String BREAKPOINT_ANNOTATIONS= "breakpoints"; //$NON-NLS-1$

	private final static BigInteger PC_UNKNOWN = BigInteger.valueOf(-1);
	private final static BigInteger PC_RUNNING = BigInteger.valueOf(-2);

	/** Preference key for highlighting current line. */
	private final static String CURRENT_LINE = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE;
	/** Preference key for highlight color of current line. */
	private final static String CURRENT_LINE_COLOR = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;

	/** The width of the vertical ruler. */
	protected final static int VERTICAL_RULER_WIDTH = 12;

	/** High water mark for cache */
	private final static int fgHighWaterMark = 500;
	/** Low water mark for cache */
	private final static int fgLowWaterMark = 100;

	private static final String COMMAND_ID_GOTO_ADDRESS = "org.eclipse.cdt.dsf.debug.ui.disassembly.commands.gotoAddress"; //$NON-NLS-1$
	private static final String COMMAND_ID_GOTO_PC = "org.eclipse.cdt.dsf.debug.ui.disassembly.commands.gotoPC"; //$NON-NLS-1$
	private static final String COMMAND_ID_TOGGLE_BREAKPOINT = "org.eclipse.cdt.dsf.debug.ui.disassembly.commands.rulerToggleBreakpoint"; //$NON-NLS-1$

	public static final String KEY_BINDING_CONTEXT_DISASSEMBLY = "org.eclipse.cdt.dsf.debug.ui.disassembly.context"; //$NON-NLS-1$

	protected DisassemblyViewer fViewer;

	protected AbstractDisassemblyAction fActionGotoPC;
	protected AbstractDisassemblyAction fActionGotoAddress;
	protected AbstractDisassemblyAction fActionToggleSource;
	private AbstractDisassemblyAction fActionToggleFunctionColumn;
	protected AbstractDisassemblyAction fActionToggleSymbols;
	protected AbstractDisassemblyAction fActionRefreshView;
	protected Action fActionOpenPreferences;
	private AbstractDisassemblyAction fActionToggleAddressColumn;
	private AbstractDisassemblyAction fActionToggleBreakpointEnablement;

	protected DisassemblyDocument fDocument;
	private IAnnotationAccess fAnnotationAccess;
	private AnnotationRulerColumn fAnnotationRulerColumn;
	private MarkerAnnotationPreferences fAnnotationPreferences;
	private IPreferenceStore fPreferenceStore;
	private IOverviewRuler fOverviewRuler;
	private final ListenerList fRulerContextMenuListeners= new ListenerList(ListenerList.IDENTITY);
	private SourceViewerDecorationSupport fDecorationSupport;
	private Font fFont;
	private IVerticalRuler fVerticalRuler;
	private IFindReplaceTarget fFindReplaceTarget;
	private IPropertyChangeListener fPropertyChangeListener= new PropertyChangeListener();
	private Color fInstructionColor;
	private Color fErrorColor;
	private Color fSourceColor;
	private Color fLabelColor;
	private Control fRedrawControl;
	private RGB fPCAnnotationRGB;
	private Composite fComposite;

	private DropTarget fDropTarget;
	private DragSource fDragSource;
	private TextViewerDragAdapter fDragSourceAdapter;
	private DisassemblyDropAdapter fDropTargetAdapter;

	private FunctionOffsetRulerColumn fOpcodeRulerColumn;
	private AddressRulerColumn fAddressRulerColumn;

	private BigInteger fStartAddress;
	private BigInteger fEndAddress;
	private int fAddressSize= 32;

	private volatile boolean fUpdatePending;
	private BigInteger fPCAddress;
	private BigInteger fGotoAddressPending= PC_UNKNOWN;
	private BigInteger fFocusAddress= PC_UNKNOWN;
	private int fBufferZone;
	private String fDebugSessionId;
	private int fTargetFrame;
	private DisassemblyIPAnnotation fPCAnnotation;
	private DisassemblyIPAnnotation fSecondaryPCAnnotation;
	private boolean fPCAnnotationUpdatePending;
	private ArrayList<BigInteger> fPendingPCUpdates = new ArrayList<BigInteger>(5);
	private Position fScrollPos;
	private int fScrollLine;
	private Position fFocusPos;
	private BigInteger fFrameAddress= PC_UNKNOWN;
	protected Map<String, Action> fGlobalActions = new HashMap<String, Action>();
	private List<Action> fSelectionActions = new ArrayList<Action>();
	private List<AbstractDisassemblyAction> fStateDependentActions = new ArrayList<AbstractDisassemblyAction>();
	private boolean fShowSource;
	private boolean fShowOpcodes;
	private boolean fShowSymbols;
	private Map<String, Object> fFile2Storage = new HashMap<String, Object>();
	private boolean fShowDisassembly = true;
	private LinkedList<AddressRangePosition> fPCHistory = new LinkedList<AddressRangePosition>();
	private int fPCHistorySizeMax = 4;
	private boolean fGotoFramePending;

	protected Action fTrackExpressionAction;
	protected Action fSyncAction;
	protected boolean fSynchWithActiveDebugContext = true;
	protected boolean fTrackExpression = false;
	private String fPCLastLocationTxt = DisassemblyMessages.Disassembly_GotoLocation_initial_text;
	private BigInteger fPCLastAddress = PC_UNKNOWN;

	private String fPCAnnotationColorKey;

	private ArrayList<Runnable> fRunnableQueue = new ArrayList<Runnable>();

	protected IPartListener2 fPartListener =
		new IPartListener2() {
			public void partActivated(IWorkbenchPartReference partRef) {
			}
			public void partBroughtToTop(IWorkbenchPartReference partRef) {
			}
			public void partClosed(IWorkbenchPartReference partRef) {
			}
			public void partDeactivated(IWorkbenchPartReference partRef) {
			}
			public void partOpened(IWorkbenchPartReference partRef) {
			}
			public void partHidden(IWorkbenchPartReference partRef) {
				if (partRef.getPart(false) == DisassemblyPart.this) {
					setActive(false);
				}
			}
			public void partVisible(IWorkbenchPartReference partRef) {
				if (partRef.getPart(false) == DisassemblyPart.this) {
					setActive(true);
				}
			}
			public void partInputChanged(IWorkbenchPartReference partRef) {
			}
		};

	private boolean fActive = true;
	private boolean fDoPendingPosted;
	private boolean fUpdateBeforeFocus;

	private boolean fRefreshAll;
	private IMarker fGotoMarkerPending;
	private boolean fUpdateTitlePending;
	private boolean fRefreshViewPending;
	private boolean fUpdateSourcePending;

	private ArrayList<IHandlerActivation> fHandlerActivations;
	private IContextActivation fContextActivation;
	
	private IDisassemblyBackend fBackend;
	
	private AddressBarContributionItem fAddressBar = null;
	private Action fJumpToAddressAction = new JumpToAddressAction(this);

	private final class SyncActiveDebugContextAction extends Action {
		public SyncActiveDebugContextAction() {
			setChecked(DisassemblyPart.this.isSyncWithActiveDebugContext());
			setText(DisassemblyMessages.Disassembly_action_Sync_label);
			setImageDescriptor(DisassemblyImageRegistry.getImageDescriptor(DisassemblyImageRegistry.ICON_Sync_enabled));
			setDisabledImageDescriptor(DisassemblyImageRegistry.getImageDescriptor(DisassemblyImageRegistry.ICON_Sync_disabled));
		}
		
		@Override
		public void run() {
			DisassemblyPart.this.setSyncWithDebugView(this.isChecked());
		}
	}
	
	private final class TrackExpressionAction extends Action {
		public TrackExpressionAction() {
			setChecked(DisassemblyPart.this.isTrackExpression());
			setEnabled(!fSynchWithActiveDebugContext);
			setText(DisassemblyMessages.Disassembly_action_TrackExpression_label);
		}
		
		@Override
		public void run() {
			DisassemblyPart.this.setTrackExpression(this.isChecked());
		}
		
	}
	
	private final class ActionRefreshView extends AbstractDisassemblyAction {
		public ActionRefreshView() {
			super(DisassemblyPart.this);
			setText(DisassemblyMessages.Disassembly_action_RefreshView_label);
			setImageDescriptor(DisassemblyImageRegistry.getImageDescriptor(DisassemblyImageRegistry.ICON_Refresh_enabled));
			setDisabledImageDescriptor(DisassemblyImageRegistry.getImageDescriptor(DisassemblyImageRegistry.ICON_Refresh_disabled));
		}
		@Override
		public void run() {
			fPCLastAddress = getTopAddress();
			refreshView(10);
		}
	}
	
	private final class ActionToggleAddressColumn extends AbstractDisassemblyAction {
		ActionToggleAddressColumn () {
			super(DisassemblyPart.this);
			setText(DisassemblyMessages.Disassembly_action_ShowAddresses_label);
		}
		@Override
		public void run() {
			IPreferenceStore store = DsfUIPlugin.getDefault().getPreferenceStore();
			store.setValue(DisassemblyPreferenceConstants.SHOW_ADDRESS_RULER, !isAddressRulerVisible());
		}
		@Override
		public void update() {
			setChecked(isAddressRulerVisible());
		}
	}

	private final class ActionToggleFunctionColumn extends AbstractDisassemblyAction {
		ActionToggleFunctionColumn() {
			super(DisassemblyPart.this);
			setText(DisassemblyMessages.Disassembly_action_ShowFunctionOffsets_label);
		}
		@Override
		public void run() {
			IPreferenceStore store = DsfUIPlugin.getDefault().getPreferenceStore();
			store.setValue(DisassemblyPreferenceConstants.SHOW_FUNCTION_OFFSETS, !isFunctionOffsetsRulerVisible());
		}
		@Override
		public void update() {
			setChecked(isFunctionOffsetsRulerVisible());
		}
	}

	private final class ActionToggleBreakpointEnablement extends AbstractDisassemblyAction {
		private IBreakpoint fBreakpoint;
		public ActionToggleBreakpointEnablement() {
			super(DisassemblyPart.this);
			setText(DisassemblyMessages.Disassembly_action_EnableBreakpoint_label);
		}
		@Override
		public void run() {
			try {
				fBreakpoint.setEnabled(!fBreakpoint.isEnabled());
			} catch (CoreException e) {
				internalError(e);
			}
		}
		@Override
		public void update() {
			super.update();
			if (isEnabled()) {
				int line = fVerticalRuler.getLineOfLastMouseButtonActivity();
				IBreakpoint[] bps = getBreakpointsAtLine(line);
				if (bps == null || bps.length == 0) {
					setEnabled(false);
				} else {
					fBreakpoint = bps[0];
					try {
						if (fBreakpoint.isEnabled()) {
							setText(DisassemblyMessages.Disassembly_action_DisableBreakpoint_label);
						} else {
							setText(DisassemblyMessages.Disassembly_action_EnableBreakpoint_label);
						}
					} catch (CoreException e) {
						setEnabled(false);
					}
				}
			}
		}
	}

	private final class ActionToggleSource extends AbstractDisassemblyAction {
		public ActionToggleSource() {
			super(DisassemblyPart.this);
			setText(DisassemblyMessages.Disassembly_action_ShowSource_label);
		}
		@Override
		public void run() {
			IPreferenceStore store = DsfUIPlugin.getDefault().getPreferenceStore();
			store.setValue(DisassemblyPreferenceConstants.SHOW_SOURCE, !fShowSource);
		}
		@Override
		public void update() {
			super.update();
			if (isEnabled()) {
				setEnabled(fShowDisassembly);
			}
			setChecked(fShowSource);
		}
	}

	private final class ActionToggleSymbols extends AbstractDisassemblyAction {
		public ActionToggleSymbols() {
			super(DisassemblyPart.this);
			setText(DisassemblyMessages.Disassembly_action_ShowSymbols_label);
		}
		@Override
		public void run() {
			IPreferenceStore store = DsfUIPlugin.getDefault().getPreferenceStore();
			store.setValue(DisassemblyPreferenceConstants.SHOW_SYMBOLS, !fShowSymbols);
		}
		@Override
		public void update() {
			super.update();
			setChecked(fShowSymbols);
		}
	}

	/**
	 * Internal property change listener for handling changes in the
	 * preferences.
	 */
	class PropertyChangeListener implements IPropertyChangeListener {
		/*
		 * @see IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			handlePreferenceStoreChanged(event);
		}
	}


	/**
	 * The constructor.
	 */
	public DisassemblyPart() {
		fAnnotationPreferences = new MarkerAnnotationPreferences();
		setPreferenceStore(new ChainedPreferenceStore(new IPreferenceStore[] {
			DsfUIPlugin.getDefault().getPreferenceStore(), EditorsUI.getPreferenceStore() }));
		fPCAddress = fFrameAddress = PC_UNKNOWN;
		fTargetFrame = -1;
		fBufferZone = 32;
		fPCAnnotation = new DisassemblyIPAnnotation(true, 0);
		fSecondaryPCAnnotation = new DisassemblyIPAnnotation(false, 0);
        IPreferenceStore prefs = getPreferenceStore();
		fStartAddress = new BigInteger(prefs.getString(DisassemblyPreferenceConstants.START_ADDRESS));
		String endAddressString = prefs.getString(DisassemblyPreferenceConstants.END_ADDRESS);
		if(endAddressString.startsWith("0x")) //$NON-NLS-1$
			fEndAddress = new BigInteger(endAddressString.substring(2), 16);
		else
			fEndAddress = new BigInteger(endAddressString, 16);
		fShowSource = prefs.getBoolean(DisassemblyPreferenceConstants.SHOW_SOURCE);
		fShowOpcodes = prefs.getBoolean(DisassemblyPreferenceConstants.SHOW_FUNCTION_OFFSETS);
		fShowSymbols = prefs.getBoolean(DisassemblyPreferenceConstants.SHOW_SYMBOLS);
		fUpdateBeforeFocus = !prefs.getBoolean(DisassemblyPreferenceConstants.AVOID_READ_BEFORE_PC);
		fPCHistorySizeMax = prefs.getInt(DisassemblyPreferenceConstants.PC_HISTORY_SIZE);
	}

	public void logWarning(String message, Throwable error) {
		DsfUIPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, DsfUIPlugin.PLUGIN_ID, message, error));
	}

	/*
	 * @see IAdaptable#getAdapter(java.lang.Class)
	 */
    @SuppressWarnings("rawtypes")
	@Override
    public Object getAdapter(Class required) {
		if (IVerticalRulerInfo.class.equals(required)) {
			if (fVerticalRuler != null) {
				return fVerticalRuler;
			}
		} else if (IDisassemblyPart.class.equals(required)) {
			return this;
		} else if (IFindReplaceTarget.class.equals(required)) {
			if (fFindReplaceTarget == null) {
				fFindReplaceTarget = (fViewer == null ? null : fViewer.getFindReplaceTarget());
			}
			return fFindReplaceTarget;
		} else if (ITextOperationTarget.class.equals(required)) {
			return (fViewer == null ? null : fViewer.getTextOperationTarget());
		} else if (Control.class.equals(required)) {
			return fViewer != null ? fViewer.getTextWidget() : null;
		} else if (IGotoMarker.class.equals(required)) {
			return new IGotoMarker() {
				public void gotoMarker(IMarker marker) {
					DisassemblyPart.this.gotoMarker(marker);
				}};
		}
		return super.getAdapter(required);
	}

	private void setPreferenceStore(IPreferenceStore store) {
		if (fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(fPropertyChangeListener);
		}

		fPreferenceStore = store;

		if (fPreferenceStore != null) {
			fPreferenceStore.addPropertyChangeListener(fPropertyChangeListener);
		}
	}

	/**
	 * Handles a property change event describing a change of the editor's
	 * preference store and updates the preference related editor properties.
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 *
	 * @param event
	 *            the property change event
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {

		if (fViewer == null)
			return;

		String property = event.getProperty();
		IPreferenceStore store = getPreferenceStore();

		if (getFontPropertyPreferenceKey().equals(property)) {
			initializeViewerFont(fViewer);
		} else if (property.equals(DisassemblyPreferenceConstants.SHOW_ADDRESS_RULER)) {
			fActionToggleAddressColumn.update();
			if (isAddressRulerVisible()) {
				showAddressRuler();
			} else {
				hideAddressRuler();
			}
		} else if (property.equals(DisassemblyPreferenceConstants.ADDRESS_RADIX)) {
			if (fAddressRulerColumn != null) {
				hideAddressRuler();
				showAddressRuler();
			}
		} else if (property.equals(DisassemblyPreferenceConstants.SHOW_ADDRESS_RADIX)) {
			if (fAddressRulerColumn != null) {
				hideAddressRuler();
				showAddressRuler();
			}
		} else if (property.equals(DisassemblyPreferenceConstants.SHOW_SOURCE)) {
			boolean showSource = store.getBoolean(property);
			if (fShowSource == showSource) {
				return;
			}
			fShowSource = showSource;
			fActionToggleSource.update();
			refreshView(10);
		} else if (property.equals(DisassemblyPreferenceConstants.SHOW_SYMBOLS)) {
			boolean showSymbols = store.getBoolean(property);
			if (fShowSymbols == showSymbols) {
				return;
			}
			fShowSymbols = showSymbols;
			fActionToggleSymbols.update();
			refreshView(10);
		} else if (property.equals(DisassemblyPreferenceConstants.SHOW_FUNCTION_OFFSETS)) {
			fShowOpcodes = store.getBoolean(property);
			fActionToggleFunctionColumn.update();
			if (isFunctionOffsetsRulerVisible()) {
				showFunctionOffsetsRuler();
			} else {
				hideFunctionOffsetsRuler();
			}
		} else if (property.equals(DisassemblyPreferenceConstants.AVOID_READ_BEFORE_PC)) {
			fUpdateBeforeFocus = !store.getBoolean(property);
			updateVisibleArea();
		} else if (property.equals(fPCAnnotationColorKey)) {
			fPCAnnotationRGB = PreferenceConverter.getColor(store, fPCAnnotationColorKey);
			// redraw
			for (Iterator<AddressRangePosition> it=fPCHistory.iterator(); it.hasNext();) {
				AddressRangePosition pos = it.next();
				fViewer.invalidateTextPresentation(pos.offset, pos.length);
			}
		} else if (property.equals(DisassemblyPreferenceConstants.PC_HISTORY_SIZE)) {
			fPCHistorySizeMax = store.getInt(property);
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		fComposite = parent;
		FillLayout layout = new FillLayout();
		layout.marginHeight = 2;
		parent.setLayout(layout);
		fVerticalRuler = createVerticalRuler();
		int styles = SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION;
		fViewer = new DisassemblyViewer(parent, fVerticalRuler, getOverviewRuler(), true, styles);
		SourceViewerConfiguration sourceViewerConfig = new DisassemblyViewerConfiguration(this);
		fViewer.addTextPresentationListener(this);
		fViewer.configure(sourceViewerConfig);
		fDecorationSupport = new SourceViewerDecorationSupport(fViewer, getOverviewRuler(), getAnnotationAccess(),
			getSharedColors());
		configureSourceViewerDecorationSupport(fDecorationSupport);
		fDecorationSupport.install(getPreferenceStore());
		if (fPCAnnotationColorKey != null) {
			fPCAnnotationRGB = PreferenceConverter.getColor(getPreferenceStore(), fPCAnnotationColorKey);
		} else {
			fPCAnnotationRGB = parent.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB();
		}

		initializeViewerFont(fViewer);
		createActions();
		hookRulerContextMenu();
		hookContextMenu();
		contributeToActionBars();
		
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateSelectionDependentActions();
			}
		});

		fDocument = createDocument();
		fViewer.setDocument(fDocument, new AnnotationModel());
		JFaceResources.getFontRegistry().addListener(fPropertyChangeListener);

		fErrorColor = getSharedColors().getColor(new RGB(96, 0, 0));
		fInstructionColor = getSharedColors().getColor(new RGB(0, 0, 96));
		fSourceColor = getSharedColors().getColor(new RGB(64, 0, 80));
		fLabelColor = getSharedColors().getColor(new RGB(0, 0, 96));

		if (isAddressRulerVisible()) {
			showAddressRuler();
		}
		if (isFunctionOffsetsRulerVisible()) {
			showFunctionOffsetsRuler();
		}
		initDragAndDrop();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(fViewer.getControl(), IDisassemblyHelpContextIds.DISASSEMBLY_VIEW);
		updateTitle();
		updateStateDependentActions();
		
		if (fDebugSessionId != null) {
			debugContextChanged();
		} else {
			updateDebugContext();
		}
	}

	/*
	 * @see org.eclipse.ui.part.WorkbenchPart#setSite(org.eclipse.ui.IWorkbenchPartSite)
	 */
	@Override
	protected void setSite(IWorkbenchPartSite site) {
		super.setSite(site);
        site.getPage().addPartListener(fPartListener);
	}

	private DisassemblyDocument createDocument() {
		DisassemblyDocument doc = new DisassemblyDocument();
		return doc;
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		IWorkbenchPartSite site = getSite();
		site.setSelectionProvider(null);
		site.getPage().removePartListener(fPartListener);
		if (fHandlerActivations != null) {
			IHandlerService handlerService = (IHandlerService)site.getService(IHandlerService.class);
			handlerService.deactivateHandlers(fHandlerActivations);
			fHandlerActivations = null;
		}
		
		deactivateDisassemblyContext();
		
		fViewer = null;
		if (fBackend != null) {
			fBackend.clearDebugContext();
			fBackend.dispose();
			fBackend = null;
		}
		
		fAnnotationAccess = null;
		fAnnotationPreferences = null;
		fAnnotationRulerColumn = null;
		fComposite = null;
		if (fDecorationSupport != null) {
			fDecorationSupport.uninstall();
			fDecorationSupport = null;
		}
		if (fFont != null) {
			fFont.dispose();
			fFont = null;
		}
		if (fDropTarget != null) {
			fDropTarget.dispose();
			fDropTarget = null;
			fDragSource.dispose();
			fDragSource = null;
		}
		if (fPropertyChangeListener != null) {
			if (fPreferenceStore != null) {
				fPreferenceStore.removePropertyChangeListener(fPropertyChangeListener);
				fPreferenceStore = null;
			}
			fPropertyChangeListener = null;
		}

		fDocument.dispose();
		fDocument = null;
		super.dispose();
	}

	private void initDragAndDrop() {
		if (fDropTarget == null) {
			Transfer[] dropTypes = new Transfer[] { FileTransfer.getInstance(), TextTransfer.getInstance() };
			Transfer[] dragTypes = new Transfer[] { TextTransfer.getInstance() };
			Control dropControl = getSourceViewer().getTextWidget();
			Control dragControl = dropControl;
			int dropOps = DND.DROP_COPY | DND.DROP_DEFAULT;
			int dragOps = DND.DROP_COPY | DND.DROP_DEFAULT;

			fDropTarget = new DropTarget(dropControl, dropOps);
			fDropTarget.setTransfer(dropTypes);
			fDropTargetAdapter = new DisassemblyDropAdapter(this);
			fDropTarget.addDropListener(fDropTargetAdapter);

			fDragSource = new DragSource(dragControl, dragOps);
			fDragSource.setTransfer(dragTypes);
			fDragSourceAdapter = new TextViewerDragAdapter(getSourceViewer());
			fDragSource.addDragListener(fDragSourceAdapter);
		}
	}

	private ISourceViewer getSourceViewer() {
		return fViewer;
	}

	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		Iterator<?> e = fAnnotationPreferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference pref = (AnnotationPreference)e.next();
			support.setAnnotationPreference(pref);
			if (pref.getAnnotationType().equals(fPCAnnotation.getType())) {
				fPCAnnotationColorKey = pref.getColorPreferenceKey();
			}
		}
		support.setCursorLinePainterPreferenceKeys(CURRENT_LINE, CURRENT_LINE_COLOR);
		support.setSymbolicFontName(getFontPropertyPreferenceKey());
	}

	/**
	 * Returns the symbolic font name for this view as defined in XML.
	 *
	 * @return a String with the symbolic font name or <code>null</code> if
	 *         none is defined
	 */
	private String getSymbolicFontName() {
		if (getConfigurationElement() != null)
			return getConfigurationElement().getAttribute("symbolicFontName"); //$NON-NLS-1$
		else
			return null;
	}

	protected final String getFontPropertyPreferenceKey() {
		String symbolicFontName = getSymbolicFontName();

		if (symbolicFontName != null)
			return symbolicFontName;
		else
			return JFaceResources.TEXT_FONT;
	}

	/**
	 * Initializes the given viewer's font.
	 *
	 * @param viewer
	 *            the viewer
	 */
	private void initializeViewerFont(ISourceViewer viewer) {

		boolean isSharedFont = true;
		Font font = null;
		String symbolicFontName = getSymbolicFontName();

		if (symbolicFontName != null)
			font = JFaceResources.getFont(symbolicFontName);
		else if (fPreferenceStore != null) {
			// Backward compatibility
			if (fPreferenceStore.contains(JFaceResources.TEXT_FONT)
				&& !fPreferenceStore.isDefault(JFaceResources.TEXT_FONT)) {
				FontData data = PreferenceConverter.getFontData(fPreferenceStore, JFaceResources.TEXT_FONT);

				if (data != null) {
					isSharedFont = false;
					font = new Font(viewer.getTextWidget().getDisplay(), data);
				}
			}
		}
		if (font == null)
			font = JFaceResources.getTextFont();

		setFont(viewer, font);

		if (fFont != null) {
			fFont.dispose();
			fFont = null;
		}

		if (!isSharedFont)
			fFont = font;
	}

	/**
	 * Sets the font for the given viewer sustaining selection and scroll
	 * position.
	 *
	 * @param sourceViewer
	 *            the source viewer
	 * @param font
	 *            the font
	 */
	private void setFont(ISourceViewer sourceViewer, Font font) {
		if (sourceViewer.getDocument() != null) {

			Point selection = sourceViewer.getSelectedRange();
			int topIndex = sourceViewer.getTopIndex();

			StyledText styledText = sourceViewer.getTextWidget();
			Control parent = styledText;
			if (sourceViewer instanceof ITextViewerExtension) {
				ITextViewerExtension extension = (ITextViewerExtension) sourceViewer;
				parent = extension.getControl();
			}

			parent.setRedraw(false);

			styledText.setFont(font);

			if (fVerticalRuler instanceof IVerticalRulerExtension) {
				IVerticalRulerExtension e = (IVerticalRulerExtension) fVerticalRuler;
				e.setFont(font);
			}

			sourceViewer.setSelectedRange(selection.x, selection.y);
			sourceViewer.setTopIndex(topIndex);

			if (parent instanceof Composite) {
				Composite composite = (Composite) parent;
				composite.layout(true);
			}

			parent.setRedraw(true);

		} else {

			StyledText styledText = sourceViewer.getTextWidget();
			styledText.setFont(font);

			if (fVerticalRuler instanceof IVerticalRulerExtension) {
				IVerticalRulerExtension e = (IVerticalRulerExtension) fVerticalRuler;
				e.setFont(font);
			}
		}
	}

	protected IVerticalRuler createVerticalRuler() {
		CompositeRuler ruler = createCompositeRuler();
		IPreferenceStore store = getPreferenceStore();
		if (ruler != null && store != null) {
			for (Iterator<?> iter = ruler.getDecoratorIterator(); iter.hasNext();) {
				IVerticalRulerColumn column = (IVerticalRulerColumn) iter.next();
				if (column instanceof AnnotationRulerColumn) {
					fAnnotationRulerColumn = (AnnotationRulerColumn) column;
					for (Iterator<?> iter2 = fAnnotationPreferences.getAnnotationPreferences().iterator(); iter2.hasNext();) {
						AnnotationPreference preference = (AnnotationPreference) iter2.next();
						String key = preference.getVerticalRulerPreferenceKey();
						boolean showAnnotation = true;
						if (key != null && store.contains(key))
							showAnnotation = store.getBoolean(key);
						if (showAnnotation)
							fAnnotationRulerColumn.addAnnotationType(preference.getAnnotationType());
					}
					fAnnotationRulerColumn.addAnnotationType(Annotation.TYPE_UNKNOWN);
					break;
				}
			}
		}
		return ruler;
	}

	/**
	 * Returns the vertical ruler.
	 *
	 * @return the vertical ruler
	 */
	protected IVerticalRuler getVerticalRuler() {
		return fVerticalRuler;
	}

	/**
	 * Returns the overview ruler.
	 *
	 * @return the overview ruler
	 */
	protected IOverviewRuler getOverviewRuler() {
		if (fOverviewRuler == null)
			fOverviewRuler = createOverviewRuler(getSharedColors());
		return fOverviewRuler;
	}

	protected ISharedTextColors getSharedColors() {
		return EditorsUI.getSharedTextColors();
	}

	protected IOverviewRuler createOverviewRuler(ISharedTextColors sharedColors) {
		IOverviewRuler ruler = new OverviewRuler(getAnnotationAccess(), VERTICAL_RULER_WIDTH, sharedColors);
		Iterator<?> e = fAnnotationPreferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference preference = (AnnotationPreference) e.next();
			if (preference.contributesToHeader())
				ruler.addHeaderAnnotationType(preference.getAnnotationType());
		}
		return ruler;
	}

	/**
	 * Creates a new address ruler column that is appropriately initialized.
	 *
	 * @return the created line number column
	 */
	protected IVerticalRulerColumn createAddressRulerColumn() {
		fAddressRulerColumn= new AddressRulerColumn();
		initializeRulerColumn(fAddressRulerColumn, DisassemblyPreferenceConstants.ADDRESS_COLOR);
		IPreferenceStore prefs = getPreferenceStore();
		fAddressRulerColumn.setRadix(prefs.getInt(DisassemblyPreferenceConstants.ADDRESS_RADIX));
		fAddressRulerColumn.setShowRadixPrefix(prefs.getBoolean(DisassemblyPreferenceConstants.SHOW_ADDRESS_RADIX));
		return fAddressRulerColumn;
	}

	/**
	 * Creates a new ruler column that is appropriately initialized.
	 *
	 * @return the created line number column
	 */
	protected IVerticalRulerColumn createFunctionOffsetsRulerColumn() {
		fOpcodeRulerColumn= new FunctionOffsetRulerColumn();
		initializeRulerColumn(fOpcodeRulerColumn, DisassemblyPreferenceConstants.FUNCTION_OFFSETS_COLOR);
		return fOpcodeRulerColumn;
	}

	/**
	 * Initializes the given address ruler column from the preference store.
	 *
	 * @param rulerColumn the ruler column to be initialized
	 */
	protected void initializeRulerColumn(DisassemblyRulerColumn rulerColumn, String colorPrefKey) {
		ISharedTextColors sharedColors= getSharedColors();
		IPreferenceStore store= getPreferenceStore();
		if (store != null) {

			RGB rgb=  null;
			// foreground color
			if (store.contains(colorPrefKey)) {
				if (store.isDefault(colorPrefKey))
					rgb= PreferenceConverter.getDefaultColor(store, colorPrefKey);
				else
					rgb= PreferenceConverter.getColor(store, colorPrefKey);
			}
			if (rgb == null)
				rgb= new RGB(0, 0, 0);
			rulerColumn.setForeground(sharedColors.getColor(rgb));

			rgb= null;

			rulerColumn.redraw();
		}
	}


	/**
	 * @return the preference store
	 */
	private IPreferenceStore getPreferenceStore() {
		return fPreferenceStore;
	}

	/**
	 * Creates a composite ruler to be used as the vertical ruler by this
	 * editor. Subclasses may re-implement this method.
	 *
	 * @return the vertical ruler
	 */
	protected CompositeRuler createCompositeRuler() {
		CompositeRuler ruler = new CompositeRuler();
		ruler.addDecorator(0, new AnnotationRulerColumn(VERTICAL_RULER_WIDTH, getAnnotationAccess()));
		return ruler;
	}

	private boolean isAddressRulerVisible() {
		return getPreferenceStore().getBoolean(DisassemblyPreferenceConstants.SHOW_ADDRESS_RULER);
	}

	/**
	 * Shows the address ruler column.
	 */
	private void showAddressRuler() {
		if (fAddressRulerColumn == null) {
			IVerticalRuler v= getVerticalRuler();
			if (v instanceof CompositeRuler) {
				CompositeRuler c= (CompositeRuler) v;
				c.addDecorator(1, createAddressRulerColumn());
			}
		}
	}

	/**
	 * Hides the address ruler column.
	 */
	private void hideAddressRuler() {
		if (fAddressRulerColumn != null) {
			IVerticalRuler v= getVerticalRuler();
			if (v instanceof CompositeRuler) {
				CompositeRuler c= (CompositeRuler) v;
				c.removeDecorator(fAddressRulerColumn);
			}
			fAddressRulerColumn = null;
		}
	}

	private boolean isFunctionOffsetsRulerVisible() {
		return fShowOpcodes;
	}

	/**
	 * Shows the opcode ruler column.
	 */
	private void showFunctionOffsetsRuler() {
		if (fOpcodeRulerColumn == null) {
			IVerticalRuler v= getVerticalRuler();
			if (v instanceof CompositeRuler) {
				CompositeRuler c= (CompositeRuler) v;
				c.addDecorator(2, createFunctionOffsetsRulerColumn());
			}
		}
	}

	/**
	 * Hides the opcode ruler column.
	 */
	private void hideFunctionOffsetsRuler() {
		if (fOpcodeRulerColumn != null) {
			IVerticalRuler v= getVerticalRuler();
			if (v instanceof CompositeRuler) {
				CompositeRuler c= (CompositeRuler) v;
				c.removeDecorator(fOpcodeRulerColumn);
			}
			fOpcodeRulerColumn = null;
		}
	}

	/**
	 * Returns the annotation access.
	 *
	 * @return the annotation access
	 */
	protected IAnnotationAccess getAnnotationAccess() {
		if (fAnnotationAccess == null)
			fAnnotationAccess = createAnnotationAccess();
		return fAnnotationAccess;
	}

	/**
	 * Creates the annotation access for this editor.
	 *
	 * @return the created annotation access
	 */
	protected IAnnotationAccess createAnnotationAccess() {
		return new DefaultMarkerAnnotationAccess();
	}

	private void hookContextMenu() {
		String id = "#DisassemblyPartContext"; //$NON-NLS-1$
		MenuManager menuMgr = new MenuManager(id, id);
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				DisassemblyPart.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(fViewer.getTextWidget());
		fViewer.getTextWidget().setMenu(menu);
		getSite().registerContextMenu(id, menuMgr, fViewer);
	}

	private void hookRulerContextMenu() {
		String id = "#DisassemblyPartRulerContext"; //$NON-NLS-1$
		MenuManager menuMgr = new MenuManager(id, id);
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				DisassemblyPart.this.fillRulerContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(fVerticalRuler.getControl());
		fVerticalRuler.getControl().setMenu(menu);
		getSite().registerContextMenu(id, menuMgr, fViewer);
	}

	private void contributeToActionBars() {
		IWorkbenchPartSite site = getSite();
		site.setSelectionProvider(new DisassemblySelectionProvider(this));
		activateDisassemblyContext();
		contributeToActionBars(getActionBars());
	}

	protected abstract IActionBars getActionBars();

	protected void contributeToActionBars(IActionBars bars) {
		for (Iterator<String> iter = fGlobalActions.keySet().iterator(); iter.hasNext();) {
			String key = iter.next();
			IAction action = fGlobalActions.get(key);
			bars.setGlobalActionHandler(key, action);
		}
		IMenuManager menu = bars.getMenuManager();
		IMenuManager navigateMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if (navigateMenu != null) {
			navigateMenu.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, fActionGotoPC);
			navigateMenu.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, fActionGotoAddress);
		}
		bars.updateActionBars();
	}

	protected void fillContextMenu(IMenuManager manager) {
		Point cursorLoc = getSite().getShell().getDisplay().getCursorLocation();
		fViewer.getTextWidget().toControl(cursorLoc);
		fActionToggleSource.update();
		fActionToggleSymbols.update();
		manager.add(new GroupMarker("group.top")); // ICommonMenuConstants.GROUP_TOP //$NON-NLS-1$
		manager.add(new Separator("group.breakpoints")); //$NON-NLS-1$
		manager.add(new Separator("group.debug")); //$NON-NLS-1$
		manager.add(new Separator(ITextEditorActionConstants.GROUP_EDIT));
		manager.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, fGlobalActions.get(ITextEditorActionConstants.COPY));
		manager.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, fGlobalActions.get(ITextEditorActionConstants.SELECT_ALL));
		// TODO add only if this is an editor
		manager.add(new Separator(ITextEditorActionConstants.GROUP_SETTINGS));
		manager.add(fActionToggleSource);
		manager.add(fActionToggleSymbols);
		manager.add(fActionOpenPreferences);
		manager.add(new Separator("group.bottom")); //$NON-NLS-1$
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void fillRulerContextMenu(IMenuManager manager) {
		fActionToggleBreakpointEnablement.update();
		fActionToggleAddressColumn.update();
		fActionToggleFunctionColumn.update();

		manager.add(new GroupMarker("group.top")); // ICommonMenuConstants.GROUP_TOP //$NON-NLS-1$
		manager.add(new Separator("group.breakpoints")); //$NON-NLS-1$
		manager.add(fActionToggleBreakpointEnablement);
		manager.add(new GroupMarker("debug")); //$NON-NLS-1$
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new GroupMarker(ITextEditorActionConstants.GROUP_RESTORE));
		manager.add(new Separator("add")); //$NON-NLS-1$
		manager.add(new Separator(ITextEditorActionConstants.GROUP_RULERS));
		manager.add(fActionToggleAddressColumn);
		manager.add(fActionToggleFunctionColumn);
		manager.add(new Separator(ITextEditorActionConstants.GROUP_REST));

		for (Object listener : fRulerContextMenuListeners.getListeners())
			((IMenuListener) listener).menuAboutToShow(manager);

		manager.add(new Separator(ITextEditorActionConstants.GROUP_EDIT));
		manager.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, fGlobalActions.get(ITextEditorActionConstants.COPY));
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
		final int ADDRESS_BAR_WIDTH = 190;
		ToolBar toolbar = ((ToolBarManager)manager).getControl();
		fAddressBar = new AddressBarContributionItem(fJumpToAddressAction);
		fAddressBar.createAddressBox(toolbar, ADDRESS_BAR_WIDTH, DisassemblyMessages.Disassembly_GotoLocation_initial_text, DisassemblyMessages.Disassembly_GotoLocation_warning);
		manager.add(fAddressBar);
		fJumpToAddressAction.setEnabled(fDebugSessionId!=null);
		
		manager.add(new Separator());
		manager.add(fActionRefreshView);
		manager.add(fActionGotoPC);
		manager.add(fSyncAction);
		manager.add(fActionToggleSource);
        // Other plug-ins can contribute their actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void updateSelectionDependentActions() {
		Iterator<Action> iterator= fSelectionActions.iterator();
		while (iterator.hasNext()) {
			IUpdate action = (IUpdate)iterator.next();
			action.update();
		}
	}

	protected void updateStateDependentActions() {
		Iterator<AbstractDisassemblyAction> iterator= fStateDependentActions.iterator();
		while (iterator.hasNext()) {
			IUpdate action = iterator.next();
			action.update();
		}
	}

	protected void createActions() {
		Action action;
		action= new TextOperationAction(fViewer, ITextOperationTarget.COPY);
		action.setText(DisassemblyMessages.Disassembly_action_Copy_label);
		action.setImageDescriptor(DisassemblyImageRegistry.getImageDescriptor(DisassemblyImageRegistry.ICON_Copy_enabled));
		action.setDisabledImageDescriptor(DisassemblyImageRegistry.getImageDescriptor(DisassemblyImageRegistry.ICON_Copy_disabled));
		action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);
		fGlobalActions.put(ITextEditorActionConstants.COPY, action);
		fSelectionActions.add(action);

		action= new TextOperationAction(fViewer, ITextOperationTarget.SELECT_ALL);
		action.setText(DisassemblyMessages.Disassembly_action_SelectAll_label);
		action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_SELECT_ALL);
		fGlobalActions.put(ITextEditorActionConstants.SELECT_ALL, action);

		action= new TextOperationAction(fViewer, ITextOperationTarget.PRINT);
		action.setActionDefinitionId(IWorkbenchCommandConstants.FILE_PRINT);
		fGlobalActions.put(ITextEditorActionConstants.PRINT, action);

		action= new FindReplaceAction(DisassemblyMessages.getBundleForConstructedKeys(), "FindReplaceAction.", this); //$NON-NLS-1$
		action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE);
		fGlobalActions.put(ActionFactory.FIND.getId(), action);
		fSelectionActions.add(action);

		fActionGotoPC = new ActionGotoProgramCounter(this);
		fActionGotoPC.setActionDefinitionId(COMMAND_ID_GOTO_PC);
		fActionGotoPC.setImageDescriptor(DisassemblyImageRegistry.getImageDescriptor(DisassemblyImageRegistry.ICON_Home_enabled));
		fActionGotoPC.setDisabledImageDescriptor(DisassemblyImageRegistry.getImageDescriptor(DisassemblyImageRegistry.ICON_Home_disabled));
		fStateDependentActions.add(fActionGotoPC);
		registerWithHandlerService(fActionGotoPC);
		
		fActionGotoAddress = new ActionGotoAddress(this);
		fActionGotoAddress.setActionDefinitionId(COMMAND_ID_GOTO_ADDRESS);
		fStateDependentActions.add(fActionGotoAddress);
		registerWithHandlerService(fActionGotoAddress);

		fActionToggleSource = new ActionToggleSource();
		fStateDependentActions.add(fActionToggleSource);
		fActionToggleSource.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(DsfUIPlugin.PLUGIN_ID, "icons/source.gif"));  //$NON-NLS-1$
		fVerticalRuler.getControl().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// invoke toggle breakpoint
				IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
				if (handlerService != null) {
					try {
						handlerService.executeCommand(COMMAND_ID_TOGGLE_BREAKPOINT, null);
					} catch (org.eclipse.core.commands.ExecutionException exc) {
						DsfUIPlugin.log(exc);
					} catch (NotDefinedException exc) {
					} catch (NotEnabledException exc) {
					} catch (NotHandledException exc) {
					}
				}
			}
		});
		fActionToggleBreakpointEnablement = new ActionToggleBreakpointEnablement();
		fActionToggleAddressColumn = new ActionToggleAddressColumn();
		fActionToggleFunctionColumn = new ActionToggleFunctionColumn();
		fActionToggleSymbols = new ActionToggleSymbols();
		fActionRefreshView = new ActionRefreshView();
		fSyncAction = new SyncActiveDebugContextAction();
		fTrackExpressionAction = new TrackExpressionAction();
		fStateDependentActions.add(fActionRefreshView);
		fGlobalActions.put(ActionFactory.REFRESH.getId(), fActionRefreshView);
		fActionOpenPreferences = new ActionOpenPreferences(getSite().getShell());
	}

	/**
	 * Register given action with the handler service for key bindings.
	 * 
	 * @param action
	 */
	private void registerWithHandlerService(IAction action) {
		if (fHandlerActivations == null) {
			fHandlerActivations = new ArrayList<IHandlerActivation>(5);
		}
		IHandlerService handlerService = (IHandlerService)getSite().getService(IHandlerService.class);
		fHandlerActivations.add(handlerService.activateHandler(action.getActionDefinitionId(), new ActionHandler(action)));
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyPart#gotoProgramCounter()
	 */
	public final void gotoProgramCounter() {
		if (fPCAddress != PC_RUNNING) {
			fPCLastAddress = PC_UNKNOWN;
			gotoFrame(getActiveStackFrame());
		}
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyPart#gotoAddress(java.math.BigInteger)
	 */
	public final void gotoAddress(IAddress address) {
		if (address != null) {
			gotoAddress(address.getValue());
		}
	}
	
	public final void gotoLocationByUser(BigInteger address, String locationTxt) {
		fPCLastAddress = address;
		fPCLastLocationTxt = locationTxt;
		gotoAddress(address);
	}
	
	public final void gotoActiveFrameByUser() {
		gotoFrame(getActiveStackFrame());
	}
	
	/*
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#gotoAddress(java.math.BigInteger)
	 */
	public final void gotoAddress(BigInteger address) {
		fFocusAddress = address;
		if (fDebugSessionId == null) {
			return;
		}
		if (DEBUG) System.out.println("gotoAddress " + getAddressText(address)); //$NON-NLS-1$
		if (fGotoAddressPending == PC_UNKNOWN) {
			fGotoAddressPending = address;
		}
		if (fUpdatePending) {
			return;
		}
		AddressRangePosition pos = getPositionOfAddress(address);
		if (pos != null) {
			if (pos.fValid) {
				if (fGotoAddressPending.equals(address)) {
                	fGotoAddressPending = PC_UNKNOWN;
                }
                gotoPosition(pos, false);
			} else {
				int lines = fBufferZone+3;
				BigInteger endAddress = pos.fAddressOffset.add(pos.fAddressLength).min(
						address.add(BigInteger.valueOf(lines * fDocument.getMeanSizeOfInstructions())));
				retrieveDisassembly(address, endAddress, lines);
			}
		}
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyPart#gotoSymbol(java.lang.String)
	 */
	public final void gotoSymbol(final String symbol) {
		if (!fActive || fBackend == null || !fBackend.hasFrameContext()) {
			return;
		}
		fBackend.gotoSymbol(symbol);
	}

	private void gotoPosition(Position pos, boolean select) {
		if (fViewer == null) {
			return;
		}
		setFocusPosition(pos);
		fViewer.setSelectedRange(pos.offset, select ? Math.max(pos.length-1, 0) : 0);
		int revealOffset = pos.offset;
		boolean onTop = false;
		if (/* !fUpdateBeforeFocus && */ pos.offset > 0) {
			try {
				AddressRangePosition previousPos = fDocument.getModelPosition(pos.offset - 1);
				if (previousPos instanceof LabelPosition) {
					revealOffset = previousPos.offset;
					onTop = true;
				} else if (previousPos == null || !previousPos.fValid) {
					onTop = true;
				}
			} catch (BadLocationException e) {
				// cannot happen
			}
		}
		fViewer.revealOffset(revealOffset, onTop);
	}

	private void gotoMarker(final IMarker marker) {
		if (marker == null) {
			return;
		}
		if (fDebugSessionId == null || fUpdatePending) {
			fGotoMarkerPending = marker;
			return;
		}
		fGotoMarkerPending = null;

		//TLETODO [disassembly] goto (breakpoint) marker
	}

	/*
	 * @see org.eclipse.jface.text.IViewportListener#viewportChanged(int)
	 */
	public void viewportChanged(int verticalOffset) {
		if (fDebugSessionId != null && fGotoAddressPending == PC_UNKNOWN && fScrollPos == null && !fUpdatePending && !fRefreshViewPending) {
			fUpdatePending = true;
			invokeLater(new Runnable() {
				public void run() {
					assert fUpdatePending;
					if (fUpdatePending) {
						fUpdatePending = false;
						updateVisibleArea();
					}
				}
			});
		}
	}

	/**
	 * Update lines of currently visible area + one page buffer zone below.
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#updateVisibleArea()
	 */
	public void updateVisibleArea() {
		assert isGuiThread();
		if (!fActive || fUpdatePending || fViewer == null || fDebugSessionId == null) {
			return;
		}
		if (fBackend == null || !fBackend.hasDebugContext() || !fBackend.isSuspended() || fFrameAddress == PC_UNKNOWN) {
			return;
		}
		StyledText styledText = fViewer.getTextWidget();
		Rectangle clientArea = styledText.getClientArea();
		fBufferZone = Math.max(8, clientArea.height / styledText.getLineHeight());
		int topIndex = fViewer.getTopIndex();
		int bottomIndex = fViewer.getBottomIndex();
		int focusIndex = -1;
		boolean focusVisible = false;
		boolean isScrollingUp = fViewer.isUserTriggeredScrolling() && fViewer.getLastTopPixel() >= styledText.getTopPixel();
		if (fFocusPos != null) {
			try {
				int focusOffset = fFocusPos.offset;
				focusIndex = fDocument.getLineOfOffset(focusOffset);
				focusVisible = focusIndex >= topIndex && focusIndex <= bottomIndex;
				// workaround for: Clicking the IP annotation in the right ruler has no effect.
				// we deselect the IP location if it is scrolled outside the visible area
				if (!focusVisible) {
					Point selection = fViewer.getSelectedRange();
					if (selection.x == focusOffset && selection.y > 0) {
						fViewer.setSelectedRange(selection.x, 0);
					}
				}
			} catch (BadLocationException e) {
				setFocusPosition(null);
			}
		}
		if (!focusVisible) {
			focusIndex = topIndex + fScrollLine;
		}
		BigInteger focusAddress = getAddressOfLine(focusIndex);
		bottomIndex += 2;
		AddressRangePosition bestPosition = null;
		int bestLine = -1;
		BigInteger bestDistance = null;
		if (DEBUG) System.out.println("DisassemblyPart.updateVisibleArea() called. There are " + fDocument.getInvalidAddressRanges().length + " invalid ranges to consider updating"); //$NON-NLS-1$ //$NON-NLS-2$
		for (AddressRangePosition p : fDocument.getInvalidAddressRanges()) {
			try {
				int line = fDocument.getLineOfOffset(p.offset);
				if (line >= topIndex && line <= bottomIndex) {
					if (p instanceof DisassemblyPosition || p.fAddressLength.compareTo(
							BigInteger.valueOf(fBufferZone * 2)) <= 0) {
						// small areas and known areas are OK to update
					} else if (!isScrollingUp && !fUpdateBeforeFocus
							&& p.fAddressOffset.compareTo(focusAddress) < 0) {
						continue;
					}
					BigInteger distance = p.fAddressOffset.subtract(focusAddress).abs();
					if (bestDistance == null || distance.compareTo(bestDistance) < 0) {
						bestPosition = p;
						bestLine = line;
						bestDistance = distance;
						if (bestDistance.compareTo(BigInteger.valueOf(fBufferZone * 2)) <= 0) {
							break;
						}
					}
				}
			} catch (BadLocationException e) {
				continue;
			}
		}
		if (bestPosition != null) {
			if (DEBUG) System.out.println("...and the best candidate is: " + bestPosition); //$NON-NLS-1$
			int lines = fBufferZone+3;
			BigInteger startAddress = bestPosition.fAddressOffset;
			BigInteger endAddress = bestPosition.fAddressOffset.add(bestPosition.fAddressLength);
			BigInteger addressRange = BigInteger.valueOf(lines * fDocument.getMeanSizeOfInstructions());
			if (bestLine > focusIndex || bestLine == focusIndex && startAddress.compareTo(focusAddress) >= 0) {
				// insert at start of range
				if (endAddress.subtract(startAddress).compareTo(addressRange) < 0) {
					// try to increase range to reduce number of requests
					Iterator<?> iter = fDocument.getModelPositionIterator(endAddress);
					while (iter.hasNext()) {
						AddressRangePosition p = (AddressRangePosition)iter.next();
						if (p.fValid) {
							endAddress = endAddress.add(p.fAddressLength);
							if (endAddress.subtract(startAddress).compareTo(addressRange) >= 0) {
								break;
							}
						} else {
							break;
						}
					}
				}
			} else {
				// insert at end of range
				startAddress = startAddress.max(endAddress.subtract(addressRange));
				// make sure we get all disassembly lines until endAddress
				lines = endAddress.subtract(startAddress).intValue();
			}
			retrieveDisassembly(startAddress, endAddress, lines);
		}
		else {
			if (DEBUG) {
				System.out.println("...but alas we didn't deem any of them worth updating. They are:"); //$NON-NLS-1$
				int i = 0;
				for (AddressRangePosition p : fDocument.getInvalidAddressRanges()) {
					System.out.println("[" + i++ + "] " + p); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		scheduleDoPending();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#asyncExec(java.lang.Runnable)
	 */
	public void asyncExec(Runnable runnable) {
		if (fViewer != null) {
			fViewer.getControl().getDisplay().asyncExec(runnable);
		}
	}
	private void invokeLater(Runnable runnable) {
		invokeLater(10, runnable);
	}
	private void invokeLater(int delay, Runnable runnable) {
		if (fViewer != null) {
			fViewer.getControl().getDisplay().timerExec(delay, runnable);
		}
	}

	/**
	 * Insert source lines if available.
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#updateInvalidSource()
	 */
	public void updateInvalidSource() {
		assert isGuiThread();
		if (fViewer == null) {
			return;
		}
		boolean unlock = false;
		try {
			if (fScrollPos == null) {
				if (fUpdatePending) {
					fUpdateSourcePending= true;
					return;
				}
				fUpdateSourcePending= false;
				unlock = true;
				fUpdatePending = true;
				lockScroller();
			}
			SourcePosition[] invalidSources = fDocument.getInvalidSourcePositions();
			for (SourcePosition p : invalidSources) {
				if (!p.fValid) {
					insertSource(p);
				} else if (DEBUG && fDocument.removeInvalidSourcePosition(p)) {
					System.err.println("!!! valid source position in invalid source list at "+ getAddressText(p.fAddressOffset)); //$NON-NLS-1$
				}
			}
		} finally {
			if (unlock) {
				fUpdatePending = false;
				unlockScroller();
				doPending();
			}
		}
	}

	/**
	 * Show disassembly for given (source) file. Retrieves disassembly starting
	 * at the beginning of the file, for as many lines as are specified. If
	 * [lines] == -1, the entire file is disassembled.
	 * 
	 * @param file
	 * @param lines
	 */
	void retrieveDisassembly(final String file, final int lines, final boolean mixed) {
		if (fDebugSessionId == null) {
			return;
		}
		if (fUpdatePending) {
			invokeLater(new Runnable() {
				public void run() {
					retrieveDisassembly(file, lines, mixed);
				}});
			return;
		}
		if (DEBUG) System.out.println("retrieveDisassembly "+file); //$NON-NLS-1$
		fBackend.retrieveDisassembly(file, lines, fEndAddress, mixed, fShowSymbols, fShowDisassembly);
	}

	private void retrieveDisassembly(BigInteger startAddress, BigInteger endAddress, int lines) {
		if (fDebugSessionId == null) {
			return;
		}
		if (DEBUG) System.out.println("retrieveDisassembly "+getAddressText(startAddress)+" "+lines+" lines"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retrieveDisassembly(startAddress, endAddress, lines, fShowSource, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#retrieveDisassembly(java.math.BigInteger, java.math.BigInteger, int, boolean, boolean)
	 */
	public void retrieveDisassembly(final BigInteger startAddress, BigInteger endAddress, final int linesHint, boolean mixed, boolean ignoreFile) {
		assert isGuiThread();
		assert !fUpdatePending;
		fUpdatePending = true;
		final int lines= linesHint + 2;
		final BigInteger addressLength= BigInteger.valueOf(lines * 4);
		if (endAddress.subtract(startAddress).compareTo(addressLength) > 0) {
			endAddress= startAddress.add(addressLength);
		}
		boolean insideActiveFrame= startAddress.equals(fFrameAddress);
		String file= null;
		int lineNumber= -1;
		if (!ignoreFile && insideActiveFrame && fBackend != null) {
			file= fBackend.getFrameFile();
			if (file != null && file.trim().length() == 0) {
				file = null;
			}
			if (file != null) {
				lineNumber= fBackend.getFrameLine();
			}
		}
		if (DEBUG) System.out.println("Asking backend to retrieve disassembly: sa=" + startAddress + ",ea=" + endAddress + ",file=" + file + ",lineNumber=" + lineNumber + ",lines=" + lines); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		fBackend.retrieveDisassembly(startAddress, endAddress, file, lineNumber, lines, mixed, fShowSymbols, fShowDisassembly, linesHint);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#insertError(java.math.BigInteger, java.lang.String)
	 */
	public void insertError(BigInteger address, String message) {
		assert isGuiThread();
		AddressRangePosition p = null;
		p = getPositionOfAddress(address);
		if (p.fValid) {
			return;
		}
		try {
			fDocument.insertErrorLine(p, address, BigInteger.ONE, message);
		} catch (BadLocationException exc) {
			internalError(exc);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#getAddressSize()
	 */
	public int getAddressSize() {
		assert isGuiThread();
		return fAddressSize;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#addressSizeChanged(int)
	 */
	public void addressSizeChanged(int addressSize) {
		assert isGuiThread();
		BigInteger oldEndAddress= fEndAddress;
		fEndAddress= BigInteger.ONE.shiftLeft(addressSize);
		int oldAddressSize= fAddressSize;
		fAddressSize= addressSize;
		if (addressSize < oldAddressSize) {
			fDocument.deleteDisassemblyRange(fEndAddress, oldEndAddress, true, true);
			List<AddressRangePosition> toRemove= new ArrayList<AddressRangePosition>();
			for (AddressRangePosition position : fDocument.getInvalidAddressRanges()) {
				if (position.fAddressOffset.compareTo(fEndAddress) >= 0) {
					try {
						fDocument.replace(position, position.length, ""); //$NON-NLS-1$
						fDocument.removeModelPosition(position);
						toRemove.add(position);
					} catch (BadLocationException exc) {
						internalError(exc);
					}
				} else if (position.containsAddress(fEndAddress)){
					position.fAddressLength= fEndAddress.subtract(position.fAddressOffset);
				}
			}
			fDocument.removeInvalidAddressRanges(toRemove);
		} else if (addressSize > oldAddressSize) {
			fDocument.insertInvalidAddressRange(fDocument.getLength(), 0, oldEndAddress, fEndAddress);
		} else {
			return;
		}
		if (fAddressRulerColumn != null) {
			fAddressRulerColumn.setAddressSize(addressSize);
			if (fComposite != null) {
				fComposite.layout(true);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#getPositionOfAddress(java.math.BigInteger)
	 */
	public AddressRangePosition getPositionOfAddress(BigInteger address) {
		assert isGuiThread();
		if (address == null || address.compareTo(BigInteger.ZERO) < 0) {
			return null;
		}
		AddressRangePosition pos = fDocument.getPositionOfAddress(address);
		assert !(pos instanceof SourcePosition);
		return pos;
	}

	private BigInteger getAddressOfLine(int line) {
		return fDocument.getAddressOfLine(line);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		fViewer.getControl().setFocus();
	}

	protected void setActive(boolean active) {
		if (DEBUG) System.out.println("setActive("+ active +")"); //$NON-NLS-1$ //$NON-NLS-2$
		fActive = active;
		if (fActive) {
			if (fRefreshAll) {
				fRefreshAll = false;
				refreshView(0);
			} else {
				doPendingPCUpdates();
				if (fBackend != null && fBackend.hasDebugContext()) {
					int frame = getActiveStackFrame();
					if (frame < 0 && isSuspended()) {
						frame= 0;
					}
					if (frame != fTargetFrame) {
						gotoFrameIfActive(frame);
					}
				}
			}
		} else {
			fGotoAddressPending= fFocusAddress= PC_UNKNOWN;
		}
		firePropertyChange(PROP_ACTIVE);
	}

	private int getActiveStackFrame() {
		if (fBackend != null) {
			return fBackend.getFrameLevel();
		}
		return -1;
	}

	protected void updateDebugContext() {
		IAdaptable context = DebugUITools.getDebugContext();
		final IDisassemblyBackend prevBackend = fBackend;
		fDebugSessionId = null;
		if (context != null) {
			if (fBackend == null || !fBackend.supportsDebugContext(context)) {
				if (fBackend != null) {
					fBackend.clearDebugContext();
					fBackend.dispose();
				}
				fBackend = (IDisassemblyBackend)context.getAdapter(IDisassemblyBackend.class);
				if (fBackend != null) {
					fBackend.init(this);
				}
			}
			
			if (fBackend != null) {
				IDisassemblyBackend.SetDebugContextResult result = fBackend.setDebugContext(context);
				if (result != null) {
					fDebugSessionId = result.sessionId;
			        if (result.contextChanged && fViewer != null) {
						debugContextChanged();
						if (prevBackend != null && fBackend != prevBackend) {
							prevBackend.clearDebugContext();
						}
					}
				}
			}
		}
	}

	private void debugContextChanged() {
		if (DEBUG) System.out.println("DisassemblyPart.debugContextChanged()"); //$NON-NLS-1$
		fRunnableQueue.clear();
		fUpdatePending = false;
		resetViewer();
		if (fDebugSessionId != null) {
			fJumpToAddressAction.setEnabled(true);
			fAddressBar.enableAddressBox(true);

			int activeFrame = getActiveStackFrame();
			if (activeFrame > 0) {
				gotoFrame(activeFrame);
			} else {
				updatePC(PC_UNKNOWN);
			}

        	if (fGotoAddressPending != PC_UNKNOWN) {
	        	gotoAddress(fGotoAddressPending);
	        }
	        if (fGotoMarkerPending != null) {
	        	gotoMarker(fGotoMarkerPending);
	        }
			fViewer.addViewportListener(this);
        } else {
        	fJumpToAddressAction.setEnabled(false);
        	fAddressBar.enableAddressBox(false);
			fViewer.removeViewportListener(this);
        	fGotoMarkerPending = null;
        }
		updateTitle();
		updateStateDependentActions();
		firePropertyChange(PROP_CONNECTED);
		firePropertyChange(PROP_SUSPENDED);
	}



	private void attachBreakpointsAnnotationModel() {
		IAnnotationModel annotationModel = fViewer.getAnnotationModel();
		if (annotationModel instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension ame= (IAnnotationModelExtension) annotationModel;
			ame.addAnnotationModel(BREAKPOINT_ANNOTATIONS, new BreakpointsAnnotationModel());
		}
	}

	private void refreshView(int delay) {
		if (fViewer == null || fRefreshViewPending || fRefreshAll) {
			return;
		}
		fRunnableQueue.clear();
		fRefreshViewPending = true;
		final long refreshViewScheduled = System.currentTimeMillis() + delay;
		final Runnable refresh = new Runnable() {
			public void run() {
				fRefreshViewPending = false;
				long now = System.currentTimeMillis();
				if (now >= refreshViewScheduled) {
					if (DEBUG) System.err.println("*** refreshing view ***"); //$NON-NLS-1$
					
					// save viewport position and frame info
					BigInteger topAddress = getTopAddress();
					int targetFrame= fTargetFrame;
					BigInteger frameAddress = fFrameAddress;
					BigInteger pcAddress = fPCAddress;
					
					// clear viewer
					resetViewer();
					if (fScrollPos != null) {
						fScrollPos.isDeleted = true;
					}

					// restore frame info and viewport
					fPCAnnotationUpdatePending = true;
					fTargetFrame = targetFrame;
					fFrameAddress = frameAddress;
					fPCAddress = pcAddress;
					gotoAddress(topAddress);
				} else {
					refreshView((int)(refreshViewScheduled - now));
				}
			}};
		if (delay > 0) {
			invokeLater(delay, new Runnable() {
				public void run() {
					doScrollLocked(refresh);
				}});
		} else {
			doScrollLocked(refresh);
		}
	}

	private BigInteger getTopAddress() {
		BigInteger topAddress = getAddressOfLine(fViewer.getTopIndex());
		if (topAddress.equals(fStartAddress)) {
			// in rare cases, the top line can be '...'
			// don't use it as reference, take the next line
			topAddress = getAddressOfLine(fViewer.getTopIndex() + 1);
		}
		return topAddress;
	}

	private void resetViewer() {
		// clear all state and cache
		fPCAnnotationUpdatePending = false;
		fGotoFramePending = false;
		fPCAddress = fFrameAddress = PC_RUNNING;
		fTargetFrame = -1;
		fGotoAddressPending = PC_UNKNOWN;
		fFocusAddress = PC_UNKNOWN;
		setFocusPosition(null);
		fPCHistory.clear();
		fPendingPCUpdates.clear();
		fFile2Storage.clear();
		fDocument.clear();
		fViewer.setDocument(fDocument, new AnnotationModel());
        if (fDebugSessionId != null) {
            attachBreakpointsAnnotationModel();
			fDocument.insertInvalidAddressRange(0, 0, fStartAddress, fEndAddress);
        }
	}

    private AddressRangePosition getPCPosition(BigInteger address) {
		if (address.compareTo(BigInteger.ZERO) < 0) {
			// invalid address
			return null;
		}
		AddressRangePosition pos = getPositionOfAddress(address);
		if (pos == null || !pos.fValid) {
			// invalid disassembly line
			return null;
		}
		if (pos.length > 0) {
			// valid disassembly line
			return pos;
		}
		// hidden disassembly
		if (!(pos instanceof DisassemblyPosition)) {
			return pos;
		}
		String srcFile = ((DisassemblyPosition)pos).getFile();
		if (srcFile == null) {
			return pos;
		}
		SourceFileInfo fi = fDocument.getSourceInfo(srcFile);
		if (fi == null) {
			return pos;
		}
		if (fi.fSource == null) {
			if (fi.fError != null) {
				// could not read source
				return pos;
			}
			return null;
		}
		int stmtLine = ((DisassemblyPosition)pos).getLine();
		if (stmtLine < 0) {
			return pos;
		}
		Position srcPos = fDocument.getSourcePosition(fi, stmtLine);
		if (srcPos == null) {
			return pos;
		}
		int offset = srcPos.offset;
		int length = srcPos.length;
		return new AddressRangePosition(offset, length, address, BigInteger.ZERO);
    }

    /**
	 * Update the annotation indicating the given address.
	 * @return a position which denotes the documents position
	 */
	private AddressRangePosition updateAddressAnnotation(Annotation annotation, BigInteger address) {
		if (fViewer == null) {
			return null;	// can happen during session shutdown
		}
		IAnnotationModel annotationModel = fViewer.getAnnotationModel();
		annotationModel.removeAnnotation(annotation);
		AddressRangePosition pos = getPCPosition(address);
		if (pos != null) {
			annotationModel.addAnnotation(annotation, new Position(pos.offset, Math.max(0, pos.length-1)));
		}
		return pos;
	}

	public IBreakpoint[] getBreakpointsAtLine(int line) {
		BreakpointsAnnotationModel bpModel= null;
		IAnnotationModel am= fViewer.getAnnotationModel();
		if (am instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension ame= (IAnnotationModelExtension) am;
			bpModel= (BreakpointsAnnotationModel) ame.getAnnotationModel(BREAKPOINT_ANNOTATIONS);
			if (bpModel != null) {
				IRegion lineRegion;
				try {
					lineRegion= fDocument.getLineInformation(line);
				} catch (BadLocationException exc) {
					return null;
				}
				int offset= lineRegion.getOffset();
				int length= lineRegion.getLength();
				@SuppressWarnings("unchecked")
				Iterator<SimpleMarkerAnnotation> it= bpModel.getAnnotationIterator(offset, length, true, true);
				List<IBreakpoint> bpList= new ArrayList<IBreakpoint>(5);
				final IBreakpointManager bpMgr= DebugPlugin.getDefault().getBreakpointManager();
				while (it.hasNext()) {
					final SimpleMarkerAnnotation annotation= it.next();
					IBreakpoint bp= bpMgr.getBreakpoint(annotation.getMarker());
					if (bp != null) {
						bpList.add(bp);
					}
				}
				if (bpList.size() > 0) {
					return bpList.toArray(new IBreakpoint[bpList.size()]);
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#gotoFrame(int)
	 */
	public void gotoFrame(int frame) {
		assert isGuiThread();
        fGotoAddressPending = PC_UNKNOWN;
		gotoFrame(frame, PC_UNKNOWN);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#gotoFrameIfActive(int)
	 */
	public void gotoFrameIfActive(int frame) {
		assert isGuiThread();
		if (fActive) {
			gotoFrame(frame);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#gotoFrame(int, java.math.BigInteger)
	 */
	public void gotoFrame(int frame, BigInteger address) {
		assert isGuiThread();
		if (DEBUG) System.out.println("gotoFrame " + frame + " " + getAddressText(address)); //$NON-NLS-1$ //$NON-NLS-2$
		
		// cache the last PC address
		if (!isSyncWithActiveDebugContext()) {
			if (isTrackExpression()) {
				if (!DisassemblyMessages.Disassembly_GotoLocation_initial_text.equals(fPCLastLocationTxt))
					fPCLastAddress = eval(fPCLastLocationTxt);
			}
			if (fPCLastAddress != PC_UNKNOWN) {
				address = fPCLastAddress;
			} else if (address != PC_UNKNOWN) {
			    fPCLastAddress = address;
			}
			
			frame = -2; // clear the annotation
		} else {
			fPCLastAddress = address;
		}
		
        if (fGotoAddressPending == fFrameAddress) {
            // cancel goto address from previous goto frame
            fGotoAddressPending = PC_UNKNOWN;
        }
		fTargetFrame = frame;
		fFrameAddress = address;
		if (fTargetFrame == -1) {
			fTargetFrame = getActiveStackFrame();
			if (fTargetFrame < 0 && fBackend != null && fBackend.isSuspended()) {
				fTargetFrame= 0;
			}
			if (fTargetFrame == -1) {
				fGotoFramePending = false;
				return;
			}
		}
		fGotoFramePending = true;
		if (frame == 0) {
			fPCAddress = fFrameAddress;
		}
		if (fFrameAddress.compareTo(PC_UNKNOWN) == 0) {
			if (!fUpdatePending) {
				fGotoFramePending = false;
				if (fBackend != null && fBackend.hasDebugContext() && fBackend.isSuspended()) {
					if (DEBUG) System.out.println("retrieveFrameAddress "+frame); //$NON-NLS-1$
					fUpdatePending = true;
					fBackend.retrieveFrameAddress(fTargetFrame);
				}
			}
			return;
		}
		AddressRangePosition pcPos = updatePCAnnotation();
		if (pcPos == null && fFrameAddress.compareTo(BigInteger.ZERO) >= 0) {
			pcPos = getPCPosition(fFrameAddress);
			if (pcPos == null) {
				gotoAddress(fFrameAddress);
				return;
			}
		}
		if (pcPos != null) {
			if (frame == 0) {
				addToPCHistory(pcPos);
			}
			fGotoFramePending = false;
			gotoPosition(pcPos, false);
			updateVisibleArea();
		} else {
			// give up
			fGotoFramePending = false;
			fGotoAddressPending = PC_UNKNOWN;
		}
		doPendingPCUpdates();
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyPart#isActive()
	 */
	public final boolean isActive() {
		return fActive;
	}
	
	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyPart#isConnected()
	 */
	public final boolean isConnected() {
		if (fDebugSessionId == null) {
			return false;
		}
		
		return (fBackend != null) ? fBackend.hasDebugContext() : false;
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyPart#isSuspended()
	 */
	public final boolean isSuspended() {
		return isConnected() && fBackend.isSuspended();
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyPart#getTextViewer()
	 */
	public final ISourceViewer getTextViewer() {
		return fViewer;
	}
	
	public final boolean hasViewer() {
		return fViewer != null;
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyPart#addRulerContextMenuListener(org.eclipse.jface.action.IMenuListener)
	 */
	public final void addRulerContextMenuListener(IMenuListener listener) {
		fRulerContextMenuListeners.add(listener);
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyPart#removeRulerContextMenuListener(org.eclipse.jface.action.IMenuListener)
	 */
	public final void removeRulerContextMenuListener(IMenuListener listener) {
		fRulerContextMenuListeners.remove(listener);
	}

	/**
	 * Schedule the retrieval of a module time stamp for the given address.
	 * Should return a <code>Long</code> object in case the value was computed,
	 * another object to be waited on if the retrieval is in progress, <code>null</code>
	 * if no time stamp could be retrieved.
	 * 
	 * @param address
	 * @return Long, Object or <code>null</code>
	 */
	synchronized Object retrieveModuleTimestamp(BigInteger address) {
		// TLETODO [disassembly] retrieve and cache module time stamp
		return null;
	}

	private void setFocusPosition(Position pcPos) {
		if (fFocusPos != null) {
			fDocument.removePosition(fFocusPos);
			fFocusPos = null;
		}
		if (pcPos != null) {
			fFocusPos = new Position(pcPos.offset, pcPos.length);
			try {
				fDocument.addPosition(fFocusPos);
			} catch (BadLocationException e) {
				internalError(e);
			}
		} else {
			fFocusAddress = PC_UNKNOWN;
		}
	}

	/**
	 * Act on the first PC in the pending list that is not a special value
	 * (UNKNOWN, RUNNING), discarding all special value entries leading up to
	 * it. If the list only has special values, act on the last one and clear
	 * the list.
	 */
	private void doPendingPCUpdates() {
		if (fPendingPCUpdates.isEmpty()) {
			return;
		}
		BigInteger pc;
		do {
			pc = fPendingPCUpdates.remove(0);
			if (pc.compareTo(BigInteger.ZERO) >= 0) {
				break;
			}
		} while (!fPendingPCUpdates.isEmpty());
		gotoFrame(0, pc);
	}

	private void addToPCHistory(AddressRangePosition pcPos) {
		if (DEBUG) System.out.println("addToPCHistory "+getAddressText(pcPos.fAddressOffset)); //$NON-NLS-1$
		if (fPCHistorySizeMax <= 1) {
			return;
		}
		AddressRangePosition first = null;
		if (fPCHistory.size() > 0) {
			first = fPCHistory.getFirst();
			if (first.fAddressOffset == pcPos.fAddressOffset) {
				if (first.offset != pcPos.offset || first.length != pcPos.length) {
					fPCHistory.removeFirst();
					fViewer.invalidateTextPresentation(first.offset, first.length);
				} else {
					return;
				}
			}
		}
		// clone and add
		pcPos = new AddressRangePosition(pcPos.offset, pcPos.length, pcPos.fAddressOffset, BigInteger.ZERO);
		fPCHistory.addFirst(pcPos);
		try {
			fDocument.addPosition(pcPos);
		} catch (BadLocationException e) {
			internalError(e);
		}
		// limit to max size
		if (fPCHistory.size() > fPCHistorySizeMax) {
			AddressRangePosition last = fPCHistory.removeLast();
			fDocument.removePosition(last);
			fViewer.invalidateTextPresentation(last.offset, last.length);
		}
		// redraw
		for (Iterator<AddressRangePosition> it=fPCHistory.iterator(); it.hasNext();) {
			AddressRangePosition pos = it.next();
			fViewer.invalidateTextPresentation(pos.offset, pos.length);
		}
	}

	/**
	 * Update current pc. If a pc update is currently under way, adds this
	 * address to a list of pending pc updates.
	 *
	 * @param pc  Current pc address. -1 means retrieve pc from top frame, -2
	 *            means target resumed
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#updatePC(java.math.BigInteger)
	 */
	public void updatePC(BigInteger pc) {
		assert isGuiThread();
		if (!fPendingPCUpdates.isEmpty()) {
			BigInteger last = fPendingPCUpdates.get(fPendingPCUpdates.size()-1);
			if (last.compareTo(BigInteger.ZERO) < 0) {
				fPendingPCUpdates.remove(fPendingPCUpdates.size()-1);
			}
		}
		fPendingPCUpdates.add(pc);
		if (fPendingPCUpdates.size() > fPCHistorySizeMax) {
			if (!fActive) {
				// if not active, we can savely remove
				// the pc updates before the history range
				fPendingPCUpdates.remove(0);
			}
			// we ignore the current goto frame request
			// and continue with the pending updates
			fGotoFramePending = false;
		}
		if (fActive) {
			if (fGotoFramePending) {
				if (!fUpdatePending) {
					gotoFrame(0, fFrameAddress);
				}
			} else {
				doPendingPCUpdates();
			}
		}
	}

	private AddressRangePosition updatePCAnnotation() {
		if (fUpdatePending) {
			fPCAnnotationUpdatePending = true;
			return null;
		}
		AddressRangePosition pos = null;
		if (fTargetFrame == 0) {
			// clear secondary
			updateAddressAnnotation(fSecondaryPCAnnotation, PC_UNKNOWN);
			// set primary
			pos = updateAddressAnnotation(fPCAnnotation, fPCAddress);
		} else if (fTargetFrame < 0) {
		    // clear both
			updateAddressAnnotation(fPCAnnotation, PC_UNKNOWN);
			updateAddressAnnotation(fSecondaryPCAnnotation, PC_UNKNOWN);
		} else {
			// clear primary
			updateAddressAnnotation(fPCAnnotation, PC_UNKNOWN);
			// set secondary
			pos = updateAddressAnnotation(fSecondaryPCAnnotation, fFrameAddress);
		}
		fPCAnnotationUpdatePending = pos == null && fFrameAddress.compareTo(BigInteger.ZERO) >= 0;
		return pos;
	}

	private void scheduleDoPending() {
		if (!fUpdatePending && !fDoPendingPosted) {
			fDoPendingPosted = true;
			invokeLater(new Runnable() {
				public void run() {
					doPending();
					fDoPendingPosted = false;
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#doPending()
	 */
	public void doPending() {
		assert isGuiThread();
		if (fViewer == null || fDocument == null) {
			return;
		}
		if (fUpdateSourcePending) {
			updateInvalidSource();
		}
		boolean sourceValid= !fDocument.hasInvalidSourcePositions();
		if (sourceValid || fShowDisassembly) {
			if (fGotoFramePending) {
				gotoFrame(fTargetFrame, fFrameAddress);
			}
		}
		if (sourceValid) {
			if (fGotoAddressPending != PC_UNKNOWN) {
				gotoAddress(fGotoAddressPending);
			} else if (fGotoMarkerPending != null) {
				gotoMarker(fGotoMarkerPending);
			}
			if (fPCAnnotationUpdatePending && !fGotoFramePending) {
				updatePCAnnotation();
			}
			if (fUpdateTitlePending) {
				updateTitle();
			}
		}
	}

	/**
	 * Safely run given runnable in a state when no update is pending.
	 * Delays execution by 10 ms if update is currently pending.
	 * @param doit
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#doScrollLocked(java.lang.Runnable)
	 */
	public void doScrollLocked(final Runnable doit) {
		assert isGuiThread();
		if (fViewer == null || fDebugSessionId == null) {
			// disposed
			return;
		}
		if (!fActive) {
			// refresh all when becoming active again
			fRefreshViewPending= false;
			fRefreshAll = true;
			return;
		}
		if (doit != null) {
			fRunnableQueue.add(doit);
		}
		if (fUpdatePending) {
			if (fRunnableQueue.size() == 1) {
				Runnable doitlater = new Runnable() {
					public void run() {
						doScrollLocked(null);
					}};
				invokeLater(doitlater);
			}
		} else {
			fUpdatePending = true;
			lockScroller();
			try {
				ArrayList<Runnable> copy = new ArrayList<Runnable>(fRunnableQueue);
				fRunnableQueue.clear();
				for (Iterator<Runnable> iter = copy.iterator(); iter.hasNext();) {
					Runnable doitnow = iter.next();
					try {
						doitnow.run();
					} catch(Exception e) {
						internalError(e);
					}
				}
			} finally {
				fUpdatePending = false;
				unlockScroller();
				doPending();
				updateVisibleArea();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#lockScroller()
	 */
	public void lockScroller() {
		assert isGuiThread();
		assert fScrollPos == null;
		if (isFunctionOffsetsRulerVisible()) {
			fRedrawControl = fViewer.getControl();
		} else {
			fRedrawControl = fViewer.getTextWidget();
		}
		fRedrawControl.setRedraw(false);
		try {
			int topOffset = fViewer.getTopIndexStartOffset();
			int topIndex = fViewer.getTopIndex();
			int bottomIndex = fViewer.getBottomIndex();
			int bottomOffset = fViewer.getBottomIndexEndOffset();
			int focusLine;
			int focusOffset;
			if (fFocusPos != null && fFocusPos.isDeleted) {
				fFocusPos = null;
			}
			if (fFocusPos != null && fFocusPos.offset >= topOffset && fFocusPos.offset <= bottomOffset) {
				focusOffset = fFocusPos.offset;
				focusLine = fDocument.getLineOfOffset(focusOffset);
			} else {
				focusLine = Math.max(0, (topIndex + bottomIndex) / 2);
				focusOffset = fDocument.getLineOffset(focusLine);
				AddressRangePosition pos = fDocument.getDisassemblyPosition(focusOffset);
				if (pos != null && !pos.fValid) {
					// don't lock position of invalid range
					focusOffset = pos.offset+pos.length;
					focusLine = fDocument.getLineOfOffset(focusOffset);
				}
			}
			fScrollPos = new Position(focusOffset);
			fScrollLine = focusLine - topIndex;
			fDocument.addPosition(fScrollPos);
		} catch (BadLocationException e) {
			// should not happen
			internalError(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#unlockScroller()
	 */
	public void unlockScroller() {
		assert isGuiThread();
		try {
			if (fScrollPos == null) {
				return;
			}
			if (fScrollPos.isDeleted) {
				fScrollPos.isDeleted = false;
				if (fScrollPos.offset >= fDocument.getLength()) {
					fScrollPos.offset = 0;
					fScrollLine = 0;
				}
			}
			if (fFocusPos != null && (fFocusPos.isDeleted || fFocusPos.length == 0)) {
				if (fFocusAddress.compareTo(BigInteger.ZERO) >= 0) {
					fGotoAddressPending = fFocusAddress;
					setFocusPosition(getPositionOfAddress(fFocusAddress));
				}
			}
			int topLine = fDocument.getLineOfOffset(fScrollPos.offset) - fScrollLine;
			// limit text size
			int lineCount = fDocument.getNumberOfLines();
			if (lineCount > fgHighWaterMark*fBufferZone) {
				int startLine = Math.max(0, topLine-fgLowWaterMark/2*fBufferZone);
				int endLine = Math.min(lineCount-1, topLine+fgLowWaterMark/2*fBufferZone);
				fDocument.deleteLineRange(endLine, lineCount-1);
				fDocument.deleteLineRange(0, startLine);
			}
			int lineHeight = fViewer.getTextWidget().getLineHeight();
			int topPixel = topLine * lineHeight;
			if (Math.abs(fViewer.getTextWidget().getTopPixel() - topPixel) >= lineHeight) {
				fViewer.setTopIndex(topLine);
			}
		} catch (BadLocationException e) {
			// should not happen
			internalError(e);
		} finally {
			if (fScrollPos != null && fDocument != null) {
				fDocument.removePosition(fScrollPos);
				fScrollPos = null;
			}
			if (fViewer != null) {
				fRedrawControl.setRedraw(true);
				getVerticalRuler().update();
				getOverviewRuler().update();
			}
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#insertSource(org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition)
     */
    public void insertSource(AddressRangePosition _pos) {
    	assert isGuiThread();
		// IDisassemblyPartCallback does not have visibility to the
		// SourcePosition type, which is DSF-specific, so it uses the base type
    	if (!(_pos instanceof SourcePosition)) {
    		assert false : "Caller should have passed in a SourcePosition";  //$NON-NLS-1$
    		return;
    	}
    	SourcePosition pos = (SourcePosition)_pos;
    	
    	if (!fShowSource) {
    		fDocument.insertSource(pos, "", pos.fLine, true); //$NON-NLS-1$
    		return;
    	}
    	SourceFileInfo fi = pos.fFileInfo;
		if (fi.fSource != null || fi.fError != null) {
	    	int lineNr = pos.fLine;
			if (fi.fSource != null && lineNr >= 0 && lineNr < fi.fSource.getNumberOfLines()) {
				fi.fStartAddress = fi.fStartAddress.min(pos.fAddressOffset);
				fi.fEndAddress = fi.fEndAddress.max(pos.fAddressOffset.add(pos.fAddressLength));
				final BigInteger lineAddr = fi.fLine2Addr[lineNr];
				if (lineAddr == null) {
					fi.fLine2Addr[lineNr] = pos.fAddressOffset;
					String sourceLine = fi.getLine(lineNr);
					fDocument.insertSource(pos, sourceLine, lineNr, true);
				} else {
					final int comparison = lineAddr.compareTo(pos.fAddressOffset);
					if (comparison > 0) {
						// new source position is before old position
						SourcePosition oldPos = fDocument.getSourcePosition(lineAddr);
						if (oldPos != null) {
							// test if source positions are consecutive
							try {
								int index = fDocument.computeIndexInCategory(DisassemblyDocument.CATEGORY_SOURCE, pos.fAddressOffset);
								if (index >= 0) {
									SourcePosition nextPos = (SourcePosition) fDocument.getPositionOfIndex(DisassemblyDocument.CATEGORY_SOURCE, index+1);
									if (nextPos.fFileInfo == fi && nextPos.fLine == lineNr) {
										fDocument.replace(oldPos, oldPos.length, ""); //$NON-NLS-1$
										fDocument.removeSourcePosition(oldPos);
									}
								}
							} catch (BadLocationException e) {
								internalError(e);
							} catch (BadPositionCategoryException e) {
								internalError(e);
							}
						}
						fi.fLine2Addr[lineNr] = pos.fAddressOffset;
						String sourceLine = fi.getLine(lineNr);
						fDocument.insertSource(pos, sourceLine, lineNr, true);
					} else if (comparison == 0) {
						String sourceLine = fi.getLine(lineNr);
						fDocument.insertSource(pos, sourceLine, lineNr, true);
					} else {
						// new source position is after old position
						try {
							// test if source positions are consecutive
							int index = fDocument.computeIndexInCategory(DisassemblyDocument.CATEGORY_SOURCE, pos.fAddressOffset);
							if (index > 0) {
								SourcePosition prevPos = (SourcePosition) fDocument.getPositionOfIndex(DisassemblyDocument.CATEGORY_SOURCE, index-1);
								if (prevPos.fFileInfo == fi && prevPos.fLine == lineNr) {
									fDocument.insertSource(pos, "", lineNr, true); //$NON-NLS-1$
									fDocument.removeSourcePosition(pos);
								} else {
									String sourceLine = fi.getLine(lineNr);
									fDocument.insertSource(pos, sourceLine, lineNr, true);
								}
							} else {
								String sourceLine = fi.getLine(lineNr);
								fDocument.insertSource(pos, sourceLine, lineNr, true);
							}
						} catch (BadPositionCategoryException e) {
							internalError(e);
						}
					}
				}
			} else {
				// no source at all
				fDocument.insertSource(pos, "", lineNr, true); //$NON-NLS-1$
				fDocument.removeSourcePosition(pos);
			}
		}
	}

    private void updateTitle() {
        if (fDebugSessionId == null) {
        	String descr = DisassemblyMessages.Disassembly_message_notConnected;
        	String title = getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
        	setPartName(title);
        	setContentDescription(descr);
        	setTitleToolTip(title);
        } else {
        	// TLETODO Proper content description
        	setContentDescription(""); //$NON-NLS-1$
        }
    }

    /**
	 * Close this part
	 */
	protected abstract void closePart();

	/*
	 * @see org.eclipse.jface.text.ITextPresentationListener#applyTextPresentation(org.eclipse.jface.text.TextPresentation)
	 */
	@SuppressWarnings("unchecked")
    public void applyTextPresentation(TextPresentation textPresentation) {
		IRegion coverage = textPresentation.getExtent();
		if (coverage == null) {
			coverage= new Region(0, fDocument.getLength());
		}
		int startOffset = coverage.getOffset();
		int length = coverage.getLength();
		int endOffset = startOffset + length;
		Iterator<Position> it;
		try {
			// make sure we start with first overlapping position
			AddressRangePosition pos = fDocument.getModelPosition(startOffset);
			if (pos == null) {
				assert false;
				return;
			}
			it = fDocument.getPositionIterator(DisassemblyDocument.CATEGORY_MODEL, pos.offset);
		} catch (BadPositionCategoryException e) {
			return;
		} catch (BadLocationException e) {
			return;
		}
		ArrayList<StyleRange> styleRanges = new ArrayList<StyleRange>();
		while(it.hasNext()) {
			AddressRangePosition pos = (AddressRangePosition)it.next();
			if (pos.offset >= endOffset) {
				break;
			}
			if (pos.offset+pos.length <= startOffset) {
				continue;
			}
			if (pos.fValid && pos.length > 0) {
				if (pos instanceof DisassemblyPosition) {
					DisassemblyPosition disPos = (DisassemblyPosition)pos;
					styleRanges.add(new StyleRange(pos.offset, disPos.length, fInstructionColor, null, SWT.NULL));
				} else if (pos instanceof ErrorPosition) {
					styleRanges.add(new StyleRange(pos.offset, pos.length, fErrorColor, null, SWT.NULL));
				} else if (pos instanceof LabelPosition) {
					styleRanges.add(new StyleRange(pos.offset, pos.length, fLabelColor, null, SWT.BOLD));
				} else if (pos instanceof SourcePosition) {
					SourcePosition srcPos = (SourcePosition)pos;
					TextPresentation presentation = null;
					if (srcPos.fFileInfo.fSource != null) {
						 presentation = srcPos.fFileInfo.getPresentation(srcPos.fFileInfo.getRegion(srcPos.fLine, pos.length));
					}
					if (presentation != null) {
						// clip result window to coverage
						int start = Math.max(startOffset, srcPos.offset);
						int end = Math.min(endOffset, srcPos.offset + srcPos.length);
						int srcOffset = srcPos.fFileInfo.getLineOffset(srcPos.fLine);
						int clipOffset = start - srcPos.offset;
						presentation.setResultWindow(new Region(srcOffset + clipOffset, end-start));
						for (Iterator<StyleRange> iter = presentation.getNonDefaultStyleRangeIterator(); iter.hasNext();) {
							StyleRange styleRange = iter.next();
							styleRange.start += srcPos.offset + clipOffset;
							styleRanges.add(styleRange);
						}
					} else {
						styleRanges.add(new StyleRange(pos.offset, pos.length, fSourceColor, null, SWT.NULL));
					}
				}
			}
		}
		if (styleRanges.size() > 0) {
			for (Iterator<StyleRange> iter = styleRanges.iterator(); iter.hasNext();) {
				textPresentation.addStyleRange(iter.next());
			}
		}
		// update pc history trail
		if (fPCHistory.size() > 1) {
			HSL hsv = new HSL(fPCAnnotationRGB);
			double luminanceStep = (1-hsv.luminance)/(fPCHistorySizeMax+1);
			hsv.luminance = 1 - luminanceStep * (fPCHistorySizeMax - fPCHistory.size());
			for (ListIterator<AddressRangePosition> listIt = fPCHistory.listIterator(fPCHistory.size()); listIt.hasPrevious();) {
				AddressRangePosition pcPos = listIt.previous();
				hsv.luminance -= luminanceStep;
				if (pcPos.isDeleted) {
					listIt.remove();
					continue;
				}
				if (!pcPos.fValid) {
					continue;
				}
				if (pcPos.overlapsWith(startOffset, length)) {
					RGB rgb = hsv.toRGB();
					Color pcColor = getSharedColors().getColor(rgb);
					Color textColor = null;
					// experimental: if color is dark, use white (background) as text color
//					Color textColor = hsv.luminance < 0.7 ? fViewer.getTextWidget().getBackground() : null;
					textPresentation.mergeStyleRange(new StyleRange(pcPos.offset, pcPos.length, textColor, pcColor));
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#insertSource(org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition, java.math.BigInteger, java.lang.String, int)
	 */
	public AddressRangePosition insertSource(AddressRangePosition pos, BigInteger address, final String file, int lineNumber) {
		assert isGuiThread();
		Object sourceElement = null;
		if (fFile2Storage.containsKey(file)) {
			sourceElement = fFile2Storage.get(file);
		} else {
			sourceElement = fBackend.insertSource(pos, address, file, lineNumber);
		}
		if (sourceElement instanceof File) {
			sourceElement = new LocalFileStorage((File)sourceElement);
        } else if (sourceElement instanceof ITranslationUnit) {
            IPath location = ((ITranslationUnit) sourceElement).getLocation();
            if (location != null) {
                sourceElement = new LocalFileStorage(location.toFile());
            }
		}
		if (sourceElement instanceof IStorage) {
			if (!(sourceElement instanceof IFile)) {
				// try to resolve as resource
				final IPath location= ((IStorage) sourceElement).getFullPath();
				if (location != null) {
					IFile iFile = ResourceLookup.selectFileForLocation(location, null);
					if (iFile != null) {
						sourceElement = iFile;
					}
				}
			}
			fFile2Storage.put(file, sourceElement);
		} else if (sourceElement == null) {
			logWarning(DisassemblyMessages.Disassembly_log_error_locateFile+file, null);
		} else {
            fFile2Storage.put(file, null);
            assert false : "missing support for source element of type " + sourceElement.getClass().toString(); //$NON-NLS-1$
		}
		
		if (sourceElement instanceof IStorage) {
			SourceFileInfo fi = fDocument.getSourceInfo((IStorage)sourceElement);
			if (fi == null) {
				IStorage storage = (IStorage)sourceElement;
				Display display = getSite().getShell().getDisplay();
				Runnable done = new SourceColorerJob(display, storage, this);
				fi = fDocument.createSourceInfo(file, storage, done);
				EditionFinderJob editionJob = null;
				if (storage instanceof IFile) {
					editionJob = new EditionFinderJob(fi, address, this);
					editionJob.schedule();
				}
				fi.fReadingJob.schedule();
			}
			pos = fDocument.insertInvalidSource(pos, address, fi, lineNumber);
		}
		
		return pos;
	}
	
	public AddressBarContributionItem getAddressBar() {
		return fAddressBar;
	}
	
	public void generateErrorDialog(String message) {
		MessageDialog messageDialog = new MessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), DisassemblyMessages.Disassembly_Error_Dialog_title, null, message, MessageDialog.ERROR, new String[]{DisassemblyMessages.Disassembly_Error_Dialog_ok_button}, 0);
		messageDialog.open();
	}
	
	public void activateDisassemblyContext() {
		IContextService ctxService = (IContextService)getSite().getService(IContextService.class);
		if (ctxService!=null)
			fContextActivation = ctxService.activateContext(KEY_BINDING_CONTEXT_DISASSEMBLY);
	}
	
	public void deactivateDisassemblyContext() {
		if (fContextActivation != null) {
			IContextService ctxService = (IContextService)getSite().getService(IContextService.class);
			ctxService.deactivateContext(fContextActivation);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#handleTargetSuspended()
	 */
	public void handleTargetSuspended() {
		asyncExec(new Runnable() {
			public void run() {
				updatePC(PC_UNKNOWN);
				firePropertyChange(PROP_SUSPENDED);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#handleTargetResumed()
	 */
	public void handleTargetResumed() {
		asyncExec(new Runnable() {
			public void run() {
				updatePC(PC_RUNNING);
				firePropertyChange(PROP_SUSPENDED);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#handleTargetEnded()
	 */
	public void handleTargetEnded() {
		asyncExec(new Runnable() {
			public void run() {
				fDebugSessionId = null;
				debugContextChanged();
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#setUpdatePending(boolean)
	 */
	public void setUpdatePending(boolean pending) {
		fUpdatePending = pending;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#getUpdatePending()
	 */
	public boolean getUpdatePending() {
		assert isGuiThread();
		return fUpdatePending;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#setGotoAddressPending(java.math.BigInteger)
	 */
	public void setGotoAddressPending(BigInteger address) {
		assert isGuiThread();
		fGotoAddressPending = address;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#getGotoAddressPending()
	 */
	public BigInteger getGotoAddressPending() {
		assert isGuiThread();
		return fGotoAddressPending;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#getDocument()
	 */
	public IDisassemblyDocument getDocument() {
		assert isGuiThread();
		return fDocument;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback#getStorageForFile(java.lang.String)
	 */
	public Object getStorageForFile(String file) {
		assert isGuiThread();
		return fFile2Storage.get(file);
	}

	/**
	 * A passthru from the text hover code to the backend.
	 */
	public String evaluateExpression(String expr) {
		if (fBackend != null) {
			return fBackend.evaluateExpression(expr);
		}
		return ""; //$NON-NLS-1$
	}
	
	public BigInteger eval(String expr) {
		String location = evaluateExpression(expr);
    	if (location != null) {
    		StringTokenizer st = new StringTokenizer(location);
    		if (st.hasMoreTokens()) {
    			try {
    				return DisassemblyUtils.decodeAddress(st.nextToken());
    			} catch (Exception e) {
    				logWarning("Failed to evaluate expression " + expr, e); //$NON-NLS-1$
    			}
    		}
    	}
    	return PC_UNKNOWN;
	}

	protected boolean isTrackExpression() {
		return fTrackExpression;
	}

	private void setTrackExpression(boolean track) {
		fTrackExpression = track;
	}
	
	protected boolean isSyncWithActiveDebugContext() {
		return fSynchWithActiveDebugContext;
	}
	
	private void setSyncWithDebugView(boolean sync) {
		fSynchWithActiveDebugContext = sync;
		fTrackExpressionAction.setEnabled(!sync);
		
		if (sync) {
			gotoActiveFrameByUser();
		} else {
			// redraw
			while (!fPCHistory.isEmpty()) {
			    AddressRangePosition pos = fPCHistory.removeFirst();
				fViewer.invalidateTextPresentation(pos.offset, pos.length);
			}
			
			fTargetFrame = -2; // clear the annotation
			updatePCAnnotation();
		}
	}
	
	/**
	 * Most methods in IDisassemblyPartCallback require execution on the GUI thread.
	 */
	private static boolean isGuiThread() {
		return Display.getCurrent() != null;
	}
}
