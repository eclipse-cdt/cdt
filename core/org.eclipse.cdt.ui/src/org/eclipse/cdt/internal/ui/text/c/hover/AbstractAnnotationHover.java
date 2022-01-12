/*******************************************************************************
 * Copyright (c) 2002, 2020 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     IBM Corporation
 *     Alexander Fedorov (ArSysOp) - Bug 561993 - Remove dependency to com.ibm.icu from CDT UI
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.c.hover;

import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.editor.CAnnotationIterator;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IInformationControlExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension2;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

/**
 * AbstractAnnotationHover
 * Abstract super class for annotation hovers.
 */
public class AbstractAnnotationHover extends AbstractCEditorTextHover {

	/**
	 * An annotation info contains information about an {@link Annotation}
	 *
	 * @since 5.0
	 */
	protected static class AnnotationInfo {
		public final Annotation annotation;
		public final Position position;
		public final ITextViewer viewer;

		public AnnotationInfo(Annotation annotation, Position position, ITextViewer textViewer) {
			this.annotation = annotation;
			this.position = position;
			this.viewer = textViewer;
		}

		/**
		 * Create completion proposals which can resolve the given annotation at
		 * the given position. Returns an empty array if no such proposals exist.
		 *
		 * @return the proposals or an empty array
		 */
		public ICompletionProposal[] getCompletionProposals() {
			return new ICompletionProposal[0];
		}

		/**
		 * Adds actions to the given toolbar.
		 *
		 * @param manager the toolbar manager to add actions to
		 * @param infoControl the information control
		 */
		public void fillToolBar(ToolBarManager manager, IInformationControl infoControl) {
			ConfigureAnnotationsAction configureAnnotationsAction = new ConfigureAnnotationsAction(annotation,
					infoControl);
			manager.add(configureAnnotationsAction);
		}
	}

	/**
	 * The annotation information control shows informations about a given
	 * {@link AbstractAnnotationHover.AnnotationInfo}. It can also show a toolbar
	 * and a list of {@link ICompletionProposal}s.
	 *
	 * @since 5.0
	 */
	private static class AnnotationInformationControl extends AbstractInformationControl
			implements IInformationControlExtension2 {
		private final DefaultMarkerAnnotationAccess fMarkerAnnotationAccess;
		private Control fFocusControl;
		private AnnotationInfo fInput;
		private Composite fParent;

		public AnnotationInformationControl(Shell parentShell, String statusFieldText) {
			super(parentShell, statusFieldText);

			fMarkerAnnotationAccess = new DefaultMarkerAnnotationAccess();
			create();
		}

		public AnnotationInformationControl(Shell parentShell, ToolBarManager toolBarManager) {
			super(parentShell, toolBarManager);

			fMarkerAnnotationAccess = new DefaultMarkerAnnotationAccess();
			create();
		}

		/*
		 * @see org.eclipse.jface.text.IInformationControl#setInformation(java.lang.String)
		 */
		@Override
		public void setInformation(String information) {
			//replaced by IInformationControlExtension2#setInput
		}

		/*
		 * @see org.eclipse.jface.text.IInformationControlExtension2#setInput(java.lang.Object)
		 */
		@Override
		public void setInput(Object input) {
			Assert.isLegal(input instanceof AnnotationInfo);
			fInput = (AnnotationInfo) input;
			disposeDeferredCreatedContent();
			deferredCreateContent();
		}

		/*
		 * @see org.eclipse.jface.text.IInformationControlExtension#hasContents()
		 */
		@Override
		public boolean hasContents() {
			return fInput != null;
		}

		private AnnotationInfo getAnnotationInfo() {
			return fInput;
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractAnnotationHover.AbstractInformationControl#setFocus()
		 */
		@Override
		public void setFocus() {
			super.setFocus();
			if (fFocusControl != null && !fFocusControl.isDisposed())
				fFocusControl.setFocus();
		}

		/*
		 * @see org.eclipse.jface.text.AbstractInformationControl#setVisible(boolean)
		 */
		@Override
		public final void setVisible(boolean visible) {
			if (!visible)
				disposeDeferredCreatedContent();
			super.setVisible(visible);
		}

		protected void disposeDeferredCreatedContent() {
			Control[] children = fParent.getChildren();
			for (Control element : children) {
				element.dispose();
			}
			ToolBarManager toolBarManager = getToolBarManager();
			if (toolBarManager != null)
				toolBarManager.removeAll();
		}

		/*
		 * @see org.eclipse.jface.text.AbstractInformationControl#createContent(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected void createContent(Composite parent) {
			fParent = parent;
			GridLayout layout = new GridLayout(1, false);
			layout.verticalSpacing = 0;
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			fParent.setLayout(layout);
		}

		/*
		 * @see org.eclipse.jface.text.AbstractInformationControl#computeSizeHint()
		 */
		@Override
		public Point computeSizeHint() {
			Point preferedSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

			Point constrains = getSizeConstraints();
			if (constrains == null)
				return preferedSize;

			Point constrainedSize = getShell().computeSize(constrains.x, SWT.DEFAULT, true);

			int width = Math.min(preferedSize.x, constrainedSize.x);
			int height = Math.max(preferedSize.y, constrainedSize.y);

			return new Point(width, height);
		}

		/**
		 * Fills the toolbar actions, if a toolbar is available. This
		 * is called after the input has been set.
		 */
		protected void fillToolbar() {
			ToolBarManager toolBarManager = getToolBarManager();
			if (toolBarManager == null)
				return;
			fInput.fillToolBar(toolBarManager, this);
			toolBarManager.update(true);
		}

		/**
		 * Create content of the hover. This is called after
		 * the input has been set.
		 */
		protected void deferredCreateContent() {
			fillToolbar();

			createAnnotationInformation(fParent, getAnnotationInfo().annotation);
			setColorAndFont(fParent, fParent.getForeground(), fParent.getBackground(), JFaceResources.getDialogFont());

			ICompletionProposal[] proposals = getAnnotationInfo().getCompletionProposals();
			if (proposals.length > 0)
				createCompletionProposalsControl(fParent, proposals);

			fParent.layout(true);
		}

		private void setColorAndFont(Control control, Color foreground, Color background, Font font) {
			control.setForeground(foreground);
			control.setBackground(background);
			control.setFont(font);

			if (control instanceof Composite) {
				Control[] children = ((Composite) control).getChildren();
				for (Control element : children) {
					setColorAndFont(element, foreground, background, font);
				}
			}
		}

		private void createAnnotationInformation(Composite parent, final Annotation annotation) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = 2;
			layout.marginWidth = 2;
			layout.horizontalSpacing = 0;
			composite.setLayout(layout);

			final Canvas canvas = new Canvas(composite, SWT.NO_FOCUS);
			GridData gridData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
			gridData.widthHint = 17;
			gridData.heightHint = 16;
			canvas.setLayoutData(gridData);
			canvas.addPaintListener(new PaintListener() {
				@Override
				public void paintControl(PaintEvent e) {
					e.gc.setFont(null);
					fMarkerAnnotationAccess.paint(annotation, e.gc, canvas, new Rectangle(0, 0, 16, 16));
				}
			});

			StyledText text = new StyledText(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			text.setLayoutData(data);
			String annotationText = annotation.getText();
			if (annotationText == null) {
				// This shouldn't happen, but it really does sometimes. See Bug 447622
				// Therefore we have to provide a sensible fallback
				text.setText(CHoverMessages.AbstractAnnotationHover_annotation_text_null_message);
			} else {
				text.setText(annotationText);
			}
		}

		private void createCompletionProposalsControl(Composite parent, ICompletionProposal[] proposals) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout layout2 = new GridLayout(1, false);
			layout2.marginHeight = 0;
			layout2.marginWidth = 0;
			layout2.verticalSpacing = 2;
			composite.setLayout(layout2);

			Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
			GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			separator.setLayoutData(gridData);

			Label quickFixLabel = new Label(composite, SWT.NONE);
			GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			layoutData.horizontalIndent = 4;
			quickFixLabel.setLayoutData(layoutData);
			String text;
			if (proposals.length == 1) {
				text = CHoverMessages.AbstractAnnotationHover_message_singleQuickFix;
			} else {
				text = MessageFormat.format(CHoverMessages.AbstractAnnotationHover_message_multipleQuickFix,
						new Object[] { String.valueOf(proposals.length) });
			}
			quickFixLabel.setText(text);

			setColorAndFont(composite, parent.getForeground(), parent.getBackground(), JFaceResources.getDialogFont());
			createCompletionProposalsList(composite, proposals);
		}

		private void createCompletionProposalsList(Composite parent, ICompletionProposal[] proposals) {
			final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			scrolledComposite.setLayoutData(gridData);
			scrolledComposite.setExpandVertical(false);
			scrolledComposite.setExpandHorizontal(false);

			Composite composite = new Composite(scrolledComposite, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout layout = new GridLayout(3, false);
			layout.verticalSpacing = 2;
			composite.setLayout(layout);

			final Link[] links = new Link[proposals.length];
			for (int i = 0; i < proposals.length; i++) {
				Label indent = new Label(composite, SWT.NONE);
				GridData gridData1 = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
				gridData1.widthHint = 0;
				indent.setLayoutData(gridData1);

				links[i] = createCompletionProposalLink(composite, proposals[i]);
			}

			scrolledComposite.setContent(composite);
			setColorAndFont(scrolledComposite, parent.getForeground(), parent.getBackground(),
					JFaceResources.getDialogFont());

			Point contentSize = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			composite.setSize(contentSize);

			Point constraints = getSizeConstraints();
			if (constraints != null && contentSize.x < constraints.x) {
				ScrollBar horizontalBar = scrolledComposite.getHorizontalBar();

				int scrollBarHeight;
				if (horizontalBar == null) {
					Point scrollSize = scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					scrollBarHeight = scrollSize.y - contentSize.y;
				} else {
					scrollBarHeight = horizontalBar.getSize().y;
				}
				gridData.heightHint = contentSize.y - scrollBarHeight;
			}

			fFocusControl = links[0];
			for (int i = 0; i < links.length; i++) {
				final int index = i;
				final Link link = links[index];
				link.addKeyListener(new KeyListener() {
					@Override
					public void keyPressed(KeyEvent e) {
						switch (e.keyCode) {
						case SWT.ARROW_DOWN:
							if (index + 1 < links.length) {
								links[index + 1].setFocus();
							}
							break;
						case SWT.ARROW_UP:
							if (index > 0) {
								links[index - 1].setFocus();
							}
							break;
						default:
							break;
						}
					}

					@Override
					public void keyReleased(KeyEvent e) {
					}
				});

				link.addFocusListener(new FocusListener() {
					@Override
					public void focusGained(FocusEvent e) {
						int currentPosition = scrolledComposite.getOrigin().y;
						int hight = scrolledComposite.getSize().y;
						int linkPosition = link.getLocation().y;

						if (linkPosition < currentPosition) {
							if (linkPosition < 10)
								linkPosition = 0;

							scrolledComposite.setOrigin(0, linkPosition);
						} else if (linkPosition + 20 > currentPosition + hight) {
							scrolledComposite.setOrigin(0, linkPosition - hight + link.getSize().y);
						}
					}

					@Override
					public void focusLost(FocusEvent e) {
					}
				});
			}
		}

		private Link createCompletionProposalLink(Composite parent, final ICompletionProposal proposal) {
			Label proposalImage = new Label(parent, SWT.NONE);
			proposalImage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			Image image = proposal.getImage();
			if (image != null) {
				proposalImage.setImage(image);

				proposalImage.addMouseListener(new MouseListener() {

					@Override
					public void mouseDoubleClick(MouseEvent e) {
					}

					@Override
					public void mouseDown(MouseEvent e) {
					}

					@Override
					public void mouseUp(MouseEvent e) {
						if (e.button == 1) {
							apply(proposal, fInput.viewer, fInput.position.offset);
						}
					}

				});
			}

			Link proposalLink = new Link(parent, SWT.WRAP);
			proposalLink.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			proposalLink.setText("<a>" + proposal.getDisplayString() + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
			proposalLink.addSelectionListener(new SelectionAdapter() {
				/*
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				@Override
				public void widgetSelected(SelectionEvent e) {
					apply(proposal, fInput.viewer, fInput.position.offset);
				}
			});

			return proposalLink;
		}

		private void apply(ICompletionProposal p, ITextViewer viewer, int offset) {
			//Focus needs to be in the text viewer, otherwise linked mode does not work
			dispose();

			IRewriteTarget target = null;
			try {
				IDocument document = viewer.getDocument();

				if (viewer instanceof ITextViewerExtension) {
					ITextViewerExtension extension = (ITextViewerExtension) viewer;
					target = extension.getRewriteTarget();
				}

				if (target != null)
					target.beginCompoundChange();

				if (p instanceof ICompletionProposalExtension2) {
					ICompletionProposalExtension2 e = (ICompletionProposalExtension2) p;
					e.apply(viewer, (char) 0, SWT.NONE, offset);
				} else if (p instanceof ICompletionProposalExtension) {
					ICompletionProposalExtension e = (ICompletionProposalExtension) p;
					e.apply(document, (char) 0, offset);
				} else {
					p.apply(document);
				}

				Point selection = p.getSelection(document);
				if (selection != null) {
					viewer.setSelectedRange(selection.x, selection.y);
					viewer.revealRange(selection.x, selection.y);
				}
			} finally {
				if (target != null)
					target.endCompoundChange();
			}
		}
	}

	/**
	 * Presenter control creator.
	 *
	 * @since 5.0
	 */
	private static final class PresenterControlCreator extends AbstractReusableInformationControlCreator {
		/*
		 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#doCreateInformationControl(org.eclipse.swt.widgets.Shell)
		 */
		@Override
		public IInformationControl doCreateInformationControl(Shell parent) {
			return new AnnotationInformationControl(parent, new ToolBarManager(SWT.FLAT));
		}
	}

	/**
	 * Hover control creator.
	 *
	 * @since 5.0
	 */
	private static final class HoverControlCreator extends AbstractReusableInformationControlCreator {
		private final IInformationControlCreator fPresenterControlCreator;

		public HoverControlCreator(IInformationControlCreator presenterControlCreator) {
			fPresenterControlCreator = presenterControlCreator;
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#doCreateInformationControl(org.eclipse.swt.widgets.Shell)
		 */
		@Override
		public IInformationControl doCreateInformationControl(Shell parent) {
			return new AnnotationInformationControl(parent, EditorsUI.getTooltipAffordanceString()) {
				/*
				 * @see org.eclipse.jface.text.IInformationControlExtension5#getInformationPresenterControlCreator()
				 */
				@Override
				public IInformationControlCreator getInformationPresenterControlCreator() {
					return fPresenterControlCreator;
				}
			};
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#canReuse(org.eclipse.jface.text.IInformationControl)
		 */
		@Override
		public boolean canReuse(IInformationControl control) {
			if (!super.canReuse(control))
				return false;

			if (control instanceof IInformationControlExtension4)
				((IInformationControlExtension4) control).setStatusText(EditorsUI.getTooltipAffordanceString());

			return true;
		}
	}

	/**
	 * Action to configure the annotation preferences.
	 *
	 * @since 5.0
	 */
	private static final class ConfigureAnnotationsAction extends Action {

		private final Annotation fAnnotation;
		private final IInformationControl fInfoControl;

		public ConfigureAnnotationsAction(Annotation annotation, IInformationControl infoControl) {
			super();
			fAnnotation = annotation;
			fInfoControl = infoControl;
			setImageDescriptor(CPluginImages.DESC_ELCL_CONFIGURE_ANNOTATIONS);
			setDisabledImageDescriptor(CPluginImages.DESC_DLCL_CONFIGURE_ANNOTATIONS);
			setToolTipText(CHoverMessages.AbstractAnnotationHover_action_configureAnnotationPreferences);
		}

		/*
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
		public void run() {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

			Object data = null;
			AnnotationPreference preference = getAnnotationPreference(fAnnotation);
			if (preference != null)
				data = preference.getPreferenceLabel();

			fInfoControl.dispose(); //FIXME: should have protocol to hide, rather than dispose
			PreferencesUtil
					.createPreferenceDialogOn(shell, "org.eclipse.ui.editors.preferencePages.Annotations", null, data) //$NON-NLS-1$
					.open();
		}
	}

	private final IPreferenceStore fStore = CUIPlugin.getDefault().getCombinedPreferenceStore();
	private final DefaultMarkerAnnotationAccess fAnnotationAccess = new DefaultMarkerAnnotationAccess();
	private final boolean fAllAnnotations;

	/**
	 * The hover control creator.
	 *
	 * @since 5.0
	 */
	private IInformationControlCreator fHoverControlCreator;
	/**
	 * The presentation control creator.
	 *
	 * @since 5.0
	 */
	private IInformationControlCreator fPresenterControlCreator;

	public AbstractAnnotationHover(boolean allAnnotations) {
		fAllAnnotations = allAnnotations;
	}

	/*
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		return null;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractJavaEditorTextHover#getHoverInfo2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 * @since 5.0
	 */
	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		IPath path;
		IAnnotationModel model;
		if (textViewer instanceof ISourceViewer) {
			path = null;
			model = ((ISourceViewer) textViewer).getAnnotationModel();
		} else {
			// Get annotation model from file buffer manager
			path = getEditorInputPath();
			model = getAnnotationModel(path);
		}
		if (model == null)
			return null;

		try {
			@SuppressWarnings("rawtypes")
			Iterator parent;
			if (model instanceof IAnnotationModelExtension2)
				parent = ((IAnnotationModelExtension2) model).getAnnotationIterator(hoverRegion.getOffset(),
						hoverRegion.getLength(), true, true);
			else
				parent = model.getAnnotationIterator();
			@SuppressWarnings("unchecked")
			Iterator<Annotation> e = new CAnnotationIterator(parent, fAllAnnotations);

			int layer = -1;
			Annotation annotation = null;
			Position position = null;
			while (e.hasNext()) {
				Annotation a = e.next();

				AnnotationPreference preference = getAnnotationPreference(a);
				if (preference == null || !(preference.getTextPreferenceKey() != null
						&& fStore.getBoolean(preference.getTextPreferenceKey())
						|| (preference.getHighlightPreferenceKey() != null
								&& fStore.getBoolean(preference.getHighlightPreferenceKey()))))
					continue;

				Position p = model.getPosition(a);

				int l = fAnnotationAccess.getLayer(a);

				if (l > layer && p != null && p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
					String msg = a.getText();
					if (msg != null && msg.trim().length() > 0) {
						layer = l;
						annotation = a;
						position = p;
					}
				}
			}
			if (layer > -1)
				return createAnnotationInfo(annotation, position, textViewer);

		} finally {
			try {
				if (path != null) {
					ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
					manager.disconnect(path, LocationKind.NORMALIZE, null);
				}
			} catch (CoreException e) {
				CUIPlugin.log(e.getStatus());
			}
		}

		return null;
	}

	protected AnnotationInfo createAnnotationInfo(Annotation annotation, Position position, ITextViewer textViewer) {
		return new AnnotationInfo(annotation, position, textViewer);
	}

	/*
	 * @see ITextHoverExtension#getHoverControlCreator()
	 * @since 5.0
	 */
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		if (fHoverControlCreator == null)
			fHoverControlCreator = new HoverControlCreator(getInformationPresenterControlCreator());
		return fHoverControlCreator;
	}

	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension2#getInformationPresenterControlCreator()
	 * @since 5.0
	 */
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		if (fPresenterControlCreator == null)
			fPresenterControlCreator = new PresenterControlCreator();
		return fPresenterControlCreator;
	}

	private IPath getEditorInputPath() {
		if (getEditor() == null)
			return null;

		IEditorInput input = getEditor().getEditorInput();
		if (input instanceof IStorageEditorInput) {
			try {
				return ((IStorageEditorInput) input).getStorage().getFullPath();
			} catch (CoreException e) {
				CUIPlugin.log(e.getStatus());
			}
		}
		return null;
	}

	private IAnnotationModel getAnnotationModel(IPath path) {
		if (path == null)
			return null;

		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		try {
			manager.connect(path, LocationKind.NORMALIZE, null);
		} catch (CoreException e) {
			CUIPlugin.log(e.getStatus());
			return null;
		}

		IAnnotationModel model = null;
		try {
			model = manager.getTextFileBuffer(path, LocationKind.NORMALIZE).getAnnotationModel();
			return model;
		} finally {
			if (model == null) {
				try {
					manager.disconnect(path, LocationKind.NORMALIZE, null);
				} catch (CoreException e) {
					CUIPlugin.log(e.getStatus());
				}
			}
		}
	}

	/**
	 * Returns the annotation preference for the given annotation.
	 *
	 * @param annotation the annotation
	 * @return the annotation preference or <code>null</code> if none
	 */
	private static AnnotationPreference getAnnotationPreference(Annotation annotation) {
		if (annotation.isMarkedDeleted())
			return null;
		return EditorsUI.getAnnotationPreferenceLookup().getAnnotationPreference(annotation);
	}
}
