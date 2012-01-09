/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.text.c.hover.SourceViewerInformationControl;

/**
 * Abstract class for "quick" compare views in light-weight controls.
 *
 * @since 5.0
 */
public abstract class AbstractCompareViewerInformationControl extends org.eclipse.jface.text.AbstractInformationControl implements IInformationControlExtension2, DisposeListener {

	protected class CompareViewerControl extends ViewForm {
		private CompareConfiguration fCompareConfiguration;
		private Viewer fViewer;
		public CompareViewerControl(Composite parent, int styles, CompareConfiguration cc) {
			super(parent, styles & ~SWT.BORDER);
			verticalSpacing= 0;
			fCompareConfiguration= cc;
		}
		public CompareConfiguration getCompareConfiguration() {
			return fCompareConfiguration;
		}
		public void setInput(ICompareInput input) {
			if (fViewer == null) {
				fViewer= createContentViewer(this, input, fCompareConfiguration);
				setContent(fViewer.getControl());
			}
			fViewer.setInput(input);
		}
	}

	private CompareViewerControl fCompareViewerControl;
	private ICompareInput fCompareInput;

	private Color fBackgroundColor;
	private boolean fIsSystemBackgroundColor = true;

	private Label fTitleLabel;

	/**
	 * Creates a compare viewer information control with the given shell as parent.
	 *
	 * @param parent  the parent shell
	 * @param isResizable  flag indicating whether the control is resizable
	 */
	public AbstractCompareViewerInformationControl(Shell parent, boolean isResizable) {
		super(parent, isResizable);

		// Create all controls
		create();
	}

	/**
	 * Creates a compare viewer information control with the given shell as parent.
	 *
	 * @param parent
	 * @param toolBarManager
	 */
	public AbstractCompareViewerInformationControl(Shell parent, ToolBarManager toolBarManager) {
		super(parent, toolBarManager);

		// Create all controls
		create();
	}

	private void initializeColors() {
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		RGB bgRGB;
		if (store.getBoolean(PreferenceConstants.EDITOR_SOURCE_HOVER_BACKGROUND_COLOR_SYSTEM_DEFAULT)) {
			bgRGB= SourceViewerInformationControl.getVisibleBackgroundColor(getShell().getDisplay());
		} else {
			bgRGB= PreferenceConverter.getColor(store, PreferenceConstants.EDITOR_SOURCE_HOVER_BACKGROUND_COLOR);
		}
		if (bgRGB != null) {
			fBackgroundColor= new Color(getShell().getDisplay(), bgRGB);
			fIsSystemBackgroundColor= false;
		} else {
			fBackgroundColor= getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
			fIsSystemBackgroundColor= true;
		}
	}
	
	@Override
	protected void createContent(Composite parent) {
		initializeColors();
		Composite content= new Composite(parent, SWT.NONE);
		final GridLayout gridLayout= new GridLayout();
		gridLayout.marginWidth= 0;
		gridLayout.marginHeight= 0;
		gridLayout.verticalSpacing= 0;
		content.setLayout(gridLayout);

		if (hasHeader()) {
			createTitleLabel(content);
		}
		CompareConfiguration compareConfig= new CompareConfiguration();
		compareConfig.setLeftEditable(false);
		compareConfig.setRightEditable(false);
		fCompareViewerControl= createCompareViewerControl(content, SWT.NONE, compareConfig);

		addDisposeListener(this);
	}

	protected CompareViewerControl createCompareViewerControl(Composite parent, int style, CompareConfiguration compareConfig) {
		CompareViewerControl compareViewer= new CompareViewerControl(parent, style, compareConfig);
		return compareViewer;
	}

	protected Viewer createContentViewer(Composite parent, ICompareInput input, CompareConfiguration cc) {
		return CompareUI.findContentViewer(null, input, parent, cc);
	}

	private void createTitleLabel(Composite parent) {
		fTitleLabel= new Label(parent, SWT.LEFT);
		fTitleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label separator= new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fTitleLabel.setFont(JFaceResources.getDialogFont());

		Display display= parent.getDisplay();
		Color foreground= display.getSystemColor(SWT.COLOR_TITLE_FOREGROUND);
		Color background= display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
		fTitleLabel.setForeground(foreground);
		fTitleLabel.setBackground(background);
		
		addMoveSupport(fTitleLabel);
	}

	public void setTitleText(String titleText) {
		if (fTitleLabel != null) {
			fTitleLabel.setText(titleText);
		}
	}

	/**
	 * Returns the compare viewer.
	 * 
	 * @return the compare viewer.
	 */
	protected final CompareViewerControl getCompareViewer() {
		return fCompareViewerControl;
	}

	/**
	 * Returns the compare configuration.
	 * 
	 * @return the compare configuration.
	 */
	protected final CompareConfiguration getCompareConfiguration() {
		return fCompareViewerControl.getCompareConfiguration();
	}

	/**
	 * Returns <code>true</code> if the control has a header, <code>false</code> otherwise.
	 * <p>
	 * The default is to return <code>false</code>.
	 * </p>
	 * 
	 * @return <code>true</code> if the control has a header
	 */
	protected boolean hasHeader() {
		// default is to have no header
		return false;
	}

	protected Color getBackgroundColor() {
		return fBackgroundColor;
	}

	@Override
	public void setInformation(String content) {
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof ICompareInput) {
			fCompareInput= (ICompareInput) input;
			if (fCompareViewerControl != null) {
				fCompareViewerControl.setInput(fCompareInput);
			}
		} else if (input instanceof String) {
			// do nothing
		} else {
			fCompareInput= null;
			if (fCompareViewerControl != null) {
				fCompareViewerControl.setInput(fCompareInput);
			}
		}
	}

	@Override
	public void dispose() {
		if (!fIsSystemBackgroundColor) {
			fBackgroundColor.dispose();
		}
		super.dispose();
	}

	/**
	 * {@inheritDoc}
	 * @param event can be null
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 */
	@Override
	public void widgetDisposed(DisposeEvent event) {
		fCompareViewerControl= null;
	}

	@Override
	public boolean hasContents() {
		return fCompareViewerControl != null && fCompareInput != null;
	}

	@Override
	public Point computeSizeHint() {
		// compute the preferred size
		int x= SWT.DEFAULT;
		int y= SWT.DEFAULT;
		Point size= getShell().computeSize(x, y);
		Point constraints= getSizeConstraints();
		if (constraints != null) {
			if (size.x < constraints.x)
				x= constraints.x;
			if (size.y < constraints.y)
				y= constraints.y;
		}
		// recompute using the constraints if the preferred size is smaller than the constraints
		if (x != SWT.DEFAULT || y != SWT.DEFAULT)
			size= getShell().computeSize(x, y, false);

		return size;
	}

	@Override
	public void setFocus() {
		super.setFocus();
		fCompareViewerControl.setFocus();
	}

	@Override
	public Rectangle computeTrim() {
		Rectangle trim= super.computeTrim();
		addInternalTrim(trim);
		return trim;
	}

	/**
	 * Adds the internal trimmings to the given trim of the shell.
	 * 
	 * @param trim the shell's trim, will be updated
	 * @since 5.0
	 */
	private void addInternalTrim(Rectangle trim) {
		Rectangle textTrim= fCompareViewerControl.computeTrim(0, 0, 0, 0);
		trim.x+= textTrim.x;
		trim.y+= textTrim.y;
		trim.width+= textTrim.width;
		trim.height+= textTrim.height;
		if (fTitleLabel != null) {
			trim.height+= fTitleLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		}
	}

	@Override
	public Point computeSizeConstraints(int widthInChars, int heightInChars) {
		Font font= JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
		GC gc= new GC(fCompareViewerControl);
		gc.setFont(font);
		int width= gc.getFontMetrics().getAverageCharWidth();
		int height= gc.getFontMetrics().getHeight();
		gc.dispose();

		return new Point(widthInChars * width, heightInChars * height);
	}

	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return null;
	}

	protected final void addMoveSupport(final Control control) {
		MouseAdapter moveSupport= new MouseAdapter() {
			private MouseMoveListener fMoveListener;
			private final Control fShell= getShell();

			@Override
			public void mouseDown(MouseEvent e) {
				Point shellLoc= fShell.getLocation();
				final int shellX= shellLoc.x;
				final int shellY= shellLoc.y;
				Point mouseLoc= control.toDisplay(e.x, e.y);
				final int mouseX= mouseLoc.x;
				final int mouseY= mouseLoc.y;
				fMoveListener= new MouseMoveListener() {
					@Override
					public void mouseMove(MouseEvent e2) {
						Point mouseLoc2= control.toDisplay(e2.x, e2.y);
						int dx= mouseLoc2.x - mouseX;
						int dy= mouseLoc2.y - mouseY;
						fShell.setLocation(shellX + dx, shellY + dy);
					}
				};
				control.addMouseMoveListener(fMoveListener);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				control.removeMouseMoveListener(fMoveListener);
				fMoveListener= null;
			}
		};
		control.addMouseListener(moveSupport);
	}

}
