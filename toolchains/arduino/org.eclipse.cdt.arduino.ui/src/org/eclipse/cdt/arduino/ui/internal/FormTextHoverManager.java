/**
 * Copyright (c) 2012,2016 Eclipse contributors and others.
 *
 *   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.arduino.ui.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.internal.text.InformationControlReplacer;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractHoverInformationControlManager;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension3;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

/**
 * A utility class for showing rich JDT-style HTML content in tool tip hovers. This class is final
 * to avoid long term API commitments. If you feel the need to specialize it, please open a bugzilla
 * to explain what your use case and requirements.
 */
@SuppressWarnings("restriction")
public abstract class FormTextHoverManager extends AbstractHoverInformationControlManager {

	private static class FormTextInformationControl extends AbstractInformationControl {
		private ScrolledComposite comp;
		private FormText formText;
		private String text;

		public FormTextInformationControl(Shell parentShell, boolean inFocus) {
			super(parentShell, true);

			// Need to do our own status bar if not in focus
			if (!inFocus) {
				Shell shell = getShell();
				Composite statusComposite = new Composite(shell, SWT.NONE);
				GridData gridData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
				statusComposite.setLayoutData(gridData);
				GridLayout statusLayout = new GridLayout(1, false);
				statusLayout.marginHeight = 0;
				statusLayout.marginWidth = 0;
				statusLayout.verticalSpacing = 1;
				statusComposite.setLayout(statusLayout);

				Label separator = new Label(statusComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
				separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

				Label statusLabel = new Label(statusComposite, SWT.RIGHT);
				statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				statusLabel.setText("Press F2 for focus");

				FontData[] fontDatas = JFaceResources.getDialogFont().getFontData();
				for (int i = 0; i < fontDatas.length; i++) {
					fontDatas[i].setHeight(fontDatas[i].getHeight() * 9 / 10);
				}
				Font statusLabelFont = new Font(statusLabel.getDisplay(), fontDatas);
				statusLabel.setFont(statusLabelFont);
			}

			create();
		}

		@Override
		public boolean hasContents() {
			return text != null;
		}

		@Override
		public void setInformation(String information) {
			this.text = information;
			if (text != null) {
				formText.setText(text, true, true);
				comp.setMinSize(formText.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		}

		@Override
		protected void createContent(Composite parent) {
			comp = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
			comp.setExpandHorizontal(true);
			comp.setExpandVertical(true);

			formText = new FormText(comp, SWT.NONE);
			comp.setContent(formText);
			formText.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent event) {
					try {
						PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
								.openURL(new URL((String) event.getHref()));
					} catch (MalformedURLException | PartInitException e) {
						Activator.log(e);
					}
				}
			});
		}

		@Override
		public IInformationControlCreator getInformationPresenterControlCreator() {
			return parent -> new FormTextInformationControl(parent, true);
		}

		public Control getControl() {
			return getShell();
		}
	}

	protected IInformationControlCloser closer;

	public FormTextHoverManager() {
		super(new AbstractReusableInformationControlCreator() {
			@Override
			protected IInformationControl doCreateInformationControl(Shell parent) {
				return new FormTextInformationControl(parent, false);
			}
		});

		getInternalAccessor().setInformationControlReplacer(
				new InformationControlReplacer(new AbstractReusableInformationControlCreator() {
					@Override
					protected IInformationControl doCreateInformationControl(Shell parent) {
						return new FormTextInformationControl(parent, true);
					}
				}) {
					{
						this.setCloser(new Closer());
					}

					class Closer implements IInformationControlCloser, ControlListener, MouseListener, KeyListener,
							FocusListener, Listener {
						protected boolean isActive;
						protected Display display;
						protected Control subjectControl;
						protected IInformationControl informationControl;

						@Override
						public void setSubjectControl(Control control) {
							subjectControl = control;
						}

						@Override
						public void setInformationControl(IInformationControl control) {
							this.informationControl = control;
						}

						@Override
						public void start(Rectangle informationArea) {
							if (!isActive) {
								isActive = true;

								if (subjectControl != null && !subjectControl.isDisposed()) {
									subjectControl.addControlListener(this);
									subjectControl.addMouseListener(this);
									subjectControl.addKeyListener(this);
								}

								if (informationControl != null) {
									informationControl.addFocusListener(this);
								}

								display = subjectControl.getDisplay();
								if (!display.isDisposed()) {
									display.addFilter(SWT.MouseMove, this);
									display.addFilter(SWT.FocusOut, this);
								}
							}
						}

						@Override
						public void stop() {
							if (isActive) {
								isActive = false;

								if (subjectControl != null && !subjectControl.isDisposed()) {
									subjectControl.removeControlListener(this);
									subjectControl.removeMouseListener(this);
									subjectControl.removeKeyListener(this);
								}

								if (informationControl != null) {
									informationControl.removeFocusListener(this);
								}

								if (display != null && !display.isDisposed()) {
									display.removeFilter(SWT.MouseMove, this);
									display.removeFilter(SWT.FocusOut, this);
								}
								display = null;
							}
						}

						@Override
						public void controlResized(ControlEvent event) {
							hideInformationControl();
						}

						@Override
						public void controlMoved(ControlEvent event) {
							hideInformationControl();
						}

						@Override
						public void mouseDown(MouseEvent event) {
							hideInformationControl();
						}

						@Override
						public void mouseUp(MouseEvent event) {
							// Ignore.
						}

						@Override
						public void mouseDoubleClick(MouseEvent event) {
							hideInformationControl();
						}

						@Override
						public void keyPressed(KeyEvent event) {
							hideInformationControl();
						}

						@Override
						public void keyReleased(KeyEvent event) {
							// Ignore.
						}

						@Override
						public void focusGained(FocusEvent event) {
							// Ignore.
						}

						@Override
						public void focusLost(FocusEvent event) {
							if (display != null && !display.isDisposed()) {
								display.asyncExec(() -> hideInformationControl());
							}
						}

						@Override
						public void handleEvent(Event event) {
							if (event.type == SWT.MouseMove) {
								if (event.widget instanceof Control && event.widget.isDisposed()) {
									if (informationControl != null && !informationControl.isFocusControl()
											&& informationControl instanceof IInformationControlExtension3) {
										Rectangle controlBounds = ((IInformationControlExtension3) informationControl)
												.getBounds();
										if (controlBounds != null) {
											Point mouseLocation = event.display.map((Control) event.widget, null,
													event.x, event.y);
											if (!controlBounds.contains(mouseLocation)) {
												hideInformationControl();
											}
										}
									} else {
										if (display != null && !display.isDisposed()) {
											display.removeFilter(SWT.MouseMove, this);
										}
									}
								}
							} else if (event.type == SWT.FocusOut) {
								if (informationControl != null && !informationControl.isFocusControl()) {
									hideInformationControl();
								}
							}
						}
					}
				});
	}

	@Override
	protected void setCloser(IInformationControlCloser closer) {
		this.closer = closer;
		super.setCloser(closer);
	}

	@Override
	protected boolean canClearDataOnHide() {
		return false;
	}

	protected KeyListener keyListener = new KeyListener() {
		@Override
		public void keyReleased(KeyEvent event) {
			if (event.keyCode == SWT.F2) {
				IInformationControl informationControl = getInformationControl();
				if (informationControl instanceof FormTextInformationControl) {
					Control myControl = ((FormTextInformationControl) informationControl).getControl();
					Event mouseEvent = new Event();
					mouseEvent.display = myControl.getDisplay();
					mouseEvent.widget = myControl;
					mouseEvent.type = SWT.MouseUp;
					((Listener) closer).handleEvent(mouseEvent);
					event.doit = false;
				}
			}
		}

		@Override
		public void keyPressed(KeyEvent event) {
			// Ignore.
		}
	};

	@Override
	public void install(Control subjectControl) {
		Control oldSubjectControl = getSubjectControl();

		if (oldSubjectControl != null && !oldSubjectControl.isDisposed()) {
			oldSubjectControl.removeKeyListener(keyListener);
		}

		if (subjectControl != null) {
			subjectControl.addKeyListener(keyListener);
		}

		super.install(subjectControl);
		getInternalAccessor().getInformationControlReplacer().install(subjectControl);
	}

	@Override
	public void dispose() {
		Control subjectControl = getSubjectControl();
		if (subjectControl != null && !subjectControl.isDisposed()) {
			subjectControl.removeKeyListener(keyListener);
		}
		super.dispose();
	}

}
