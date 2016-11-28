/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form;

import java.io.StringReader;

import org.eclipse.cdt.linkerscript.linkerScript.LExpression;
import org.eclipse.cdt.linkerscript.ui.internal.LinkerscriptActivator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.IParser;

import com.google.inject.Injector;

public class LExpressionText {
	private LinkerScriptUIUtils util = new LinkerScriptUIUtils();
	private Text text;

	private Injector injector;
	private IGrammarAccess grammar;
	private IParser parser;

	private ILinkerScriptModel model;
	private String containerURI;
	private EReference featureReference;

	/**
	 * This class does not handle layoutData for controls it creates
	 */
	public LExpressionText(Composite composite, FormToolkit toolkit, ILinkerScriptModel model,
			EReference featureReference) {
		this.model = model;
		this.featureReference = featureReference;

		injector = LinkerscriptActivator.getInstance().getInjector("org.eclipse.cdt.linkerscript.LinkerScript");
		grammar = injector.getInstance(IGrammarAccess.class);
		parser = injector.getInstance(IParser.class);

		text = toolkit.createText(composite, "Initial Address", SWT.BORDER);
		text.addModifyListener(e -> {
			Pair<String, String> address2 = model.readModel(containerURI, EObject.class, null, container -> {
				Object address = container.eGet(featureReference);
				if (!(address instanceof LExpression)) {
					return null;
				}
				LExpression addressExp = (LExpression) address;
				return new Pair<String, String>(container.eResource().getURIFragment(addressExp),
						util.getText(NodeModelUtils.getNode(addressExp)));
			});
			if (address2 == null) {
				return;
			}
			String newAddress = text.getText();
			ParserRule parserRule = (ParserRule) GrammarUtil.findRuleForName(grammar.getGrammar(), "LExpression");
			IParseResult result = parser.parse(parserRule, new StringReader(newAddress));
			boolean hasErrors = result.getSyntaxErrors().iterator().hasNext();

			if (!hasErrors && !newAddress.equals(address2.getSecond())) {
				text.setData(Boolean.FALSE);
				model.writeText(address2.getFirst(), newAddress);
			} else if (hasErrors) {
				text.setData(Boolean.TRUE);
			}
		});
	}

	public void refresh() {

		if (text.getData() != Boolean.TRUE) {
			String address = model.readModel(containerURI, EObject.class, "", container -> {
				Object addressObj = container.eGet(featureReference);
				if (!(addressObj instanceof LExpression)) {
					return "";
				}
				LExpression addressExp = (LExpression) addressObj;
				return util.getText(NodeModelUtils.getNode(addressExp));
			});
			text.setText(address);
		}
	}

	public void setInput(String containerURI) {
		this.containerURI = containerURI;
		refresh();
	}

	public Text getText() {
		return text;
	}
}
