/*******************************************************************************
 * Copyright (c) 2009, 2015 Nokia Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia Corporation - initial API and implementation
 *     Wind River Systems - Added support for advanced expression hover
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.ui.editors.AbstractDebugTextHover;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.internal.ui.ExpressionInformationControlCreator;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;

/**
 * An implementation of AbstractDebugTextHover using DSF services
 *
 * @since 2.1
 */
abstract public class AbstractDsfDebugTextHover extends AbstractDebugTextHover implements ITextHoverExtension2 {

	/**
	 * Returns the debug model ID that this debug text hover is to be used for.
	 */
	abstract protected String getModelId();

	/**
	 * Returns the type of format that should be used for the hover.
	 */
	protected String getHoverFormat() {
		return IFormattedValues.NATURAL_FORMAT;
	}

	private class GetExpressionValueQuery extends Query<FormattedValueDMData> {
		private final IFrameDMContext frame;
		private final String expression;
		private DsfServicesTracker dsfServicesTracker;

		public GetExpressionValueQuery(IFrameDMContext frame, String expression,
				DsfServicesTracker dsfServicesTracker) {
			this.frame = frame;
			this.expression = expression;
			this.dsfServicesTracker = dsfServicesTracker;
		}

		@Override
		protected void execute(final DataRequestMonitor<FormattedValueDMData> rm) {
			DsfSession session = DsfSession.getSession(frame.getSessionId());
			IExpressions expressions = dsfServicesTracker.getService(IExpressions.class);
			if (expressions == null) {
				rm.setStatus(
						DsfUIPlugin.newErrorStatus(IDsfStatusConstants.REQUEST_FAILED, "No expression service", null)); //$NON-NLS-1$
				rm.done();
				return;
			}
			IExpressionDMContext expressionDMC = expressions.createExpression(frame, expression);
			FormattedValueDMContext formattedValueContext = expressions.getFormattedValueContext(expressionDMC,
					getHoverFormat());
			expressions.getFormattedExpressionValue(formattedValueContext,
					new DataRequestMonitor<FormattedValueDMData>(session.getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							rm.setData(getData());
							rm.done();
						}

						@Override
						protected void handleFailure() {
							rm.done();
						}
					});
		}
	}

	protected IFrameDMContext getFrame() {
		IAdaptable adaptable = getSelectionAdaptable();
		if (adaptable != null) {
			return adaptable.getAdapter(IFrameDMContext.class);
		}
		return null;
	}

	@Override
	protected boolean canEvaluate() {
		if (getFrame() == null) {
			return false;
		}

		IAdaptable adaptable = getSelectionAdaptable();
		if (adaptable != null) {
			IDebugModelProvider modelProvider = adaptable.getAdapter(IDebugModelProvider.class);
			if (modelProvider != null) {
				String[] models = modelProvider.getModelIdentifiers();
				String myModel = getModelId();
				for (int i = 0; i < models.length; i++) {
					if (models[i].equals(myModel)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	protected String evaluateExpression(String expression) {
		IFrameDMContext frame = getFrame();
		if (frame == null) {
			return null;
		}

		String sessionId = frame.getSessionId();
		DsfServicesTracker dsfServicesTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), sessionId);
		try {
			GetExpressionValueQuery query = new GetExpressionValueQuery(frame, expression, dsfServicesTracker);
			DsfSession session = DsfSession.getSession(sessionId);
			if (session != null) {
				session.getExecutor().execute(query);
				try {
					FormattedValueDMData data = query.get();
					if (data != null)
						return data.getFormattedValue();
				} catch (Exception e) {
				}
			}
		} finally {
			dsfServicesTracker.dispose();
		}
		return null;
	}

	/**
	 * Returns whether the "advanced" expression information control should be used.
	 * The default implementation returns <code>false</code>.
	 */
	protected boolean useExpressionExplorer() {
		return false;
	}

	/**
	 * Create an information control creator for the "advanced" hover.
	 * <p>
	 * Clients can call this method to create an information control creator
	 * with custom options.
	 * </p>
	 * @param showDetailPane  whether the detail pane should be visible
	 * @param defaultExpansionLevel  automatically expand the expression to this level
	 * @return the information control creator
	 */
	protected final IInformationControlCreator createExpressionInformationControlCreator(boolean showDetailPane,
			int defaultExpansionLevel) {
		return new ExpressionInformationControlCreator(showDetailPane, defaultExpansionLevel);
	}

	/**
	 * Create an information control creator for the "advanced" hover.
	 * Called by {@link #getHoverControlCreator()} when {@link #useExpressionExplorer()}
	 * returns <code>true</code>.
	 * <p>
	 * The default implementation returns an information control creator with
	 * details pane enabled and default expansion level = 1.
	 * Clients may override this method to return an instance with different options.
	 * </p>
	 * @return the information control creator
	 * @see {@link #createExpressionInformationControlCreator(boolean, int)}
	 */
	protected IInformationControlCreator createExpressionInformationControlCreator() {
		return createExpressionInformationControlCreator(true, 1);
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		if (useExpressionExplorer()) {
			return createExpressionInformationControlCreator();
		}

		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, EditorsUI.getTooltipAffordanceString());
			}
		};
	}

	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension2#getHoverInfo2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		final String simpleInfo = getHoverInfo(textViewer, hoverRegion);
		if (!useExpressionExplorer() || simpleInfo == null) {
			return simpleInfo;
		}
		// improved version using ExpressionInformationControlCreator
		// see also getHoverControlCreator()
		final String text;
		text = getExpressionText(textViewer, hoverRegion);
		if (text != null && !text.isEmpty()) {
			final IFrameDMContext frameDmc = getFrame();
			if (frameDmc != null) {
				final DsfSession dsfSession = DsfSession.getSession(frameDmc.getSessionId());
				if (dsfSession != null) {
					Callable<IExpressionDMContext> callable = new Callable<IExpressionDMContext>() {
						@Override
						public IExpressionDMContext call() throws Exception {
							DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(),
									frameDmc.getSessionId());
							try {
								IExpressions expressions = tracker.getService(IExpressions.class);
								if (expressions != null) {
									return expressions.createExpression(frameDmc, text);
								}
								return null;
							} finally {
								tracker.dispose();
							}
						}
					};
					try {
						return dsfSession.getExecutor().submit(callable).get();
					} catch (InterruptedException e) {
					} catch (ExecutionException e) {
					}
				}
			}
		}
		return null;
	}

}
