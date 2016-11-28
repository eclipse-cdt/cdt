package org.eclipse.cdt.linkerscript.ui.form;

import java.io.StringReader;

import org.eclipse.cdt.linkerscript.linkerScript.LExpression;
import org.eclipse.cdt.linkerscript.linkerScript.LNumberLiteral;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptFactory;
import org.eclipse.cdt.linkerscript.ui.internal.LinkerscriptActivator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.IParser;

import com.google.inject.Injector;

public class AddressText {
	private Text addressText;
	private Button fixedAddressCheck;
	private Label addressLabel;

	private Injector injector;
	private IGrammarAccess grammar;
	private IParser parser;

	private ILinkerScriptModel model;
	private String containerURI;
	private EReference featureReference;

	private LinkerScriptUIUtils util = new LinkerScriptUIUtils();
	private String lastEnabledText;

	public AddressText(Composite composite, FormToolkit toolkit, ILinkerScriptModel model, EReference featureReference,
			String name) {
		this.model = model;
		this.featureReference = featureReference;

		injector = LinkerscriptActivator.getInstance().getInjector("org.eclipse.cdt.linkerscript.LinkerScript");
		grammar = injector.getInstance(IGrammarAccess.class);
		parser = injector.getInstance(IParser.class);

		Color foreground = toolkit.getColors().getColor(IFormColors.TITLE);

		fixedAddressCheck = toolkit.createButton(composite, "Set " + name + " explicitly", SWT.CHECK);
		fixedAddressCheck.setForeground(foreground); // TODO foreground not
														// working
		fixedAddressCheck.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).grab(true, false).create());
		fixedAddressCheck.addListener(SWT.Selection, e -> {
			boolean isFixedAddress = fixedAddressCheck.getSelection();
			if (!isFixedAddress) {
				lastEnabledText = addressText.getText();
			}

			addressText.setEnabled(isFixedAddress);
			addressLabel.setEnabled(isFixedAddress);

			model.writeModel(containerURI, EObject.class, container -> {
				if (isFixedAddress) {
					LNumberLiteral literal = LinkerScriptFactory.eINSTANCE.createLNumberLiteral();
					literal.setValue(0L);
					container.eSet(featureReference, literal);
				} else {
					container.eUnset(featureReference);
				}
			});
			if (isFixedAddress && lastEnabledText != null) {
				composite.getDisplay().asyncExec(() -> addressText.setText(lastEnabledText));
			}
		});

		addressLabel = toolkit.createLabel(composite, name + ":");
		addressLabel.setForeground(foreground);
		addressLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, GridData.CENTER).create());
		addressText = toolkit.createText(composite, "Initial Address");
		addressText.setLayoutData(
				GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, GridData.CENTER).grab(true, false).create());
		addressText.addModifyListener(e -> {
			// TODO this listener is too complicated
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
			String newAddress = addressText.getText();
			ParserRule parserRule = (ParserRule) GrammarUtil.findRuleForName(grammar.getGrammar(), "LExpression");
			IParseResult result = parser.parse(parserRule, new StringReader(newAddress));
			boolean hasErrors = result.getSyntaxErrors().iterator().hasNext();

			if (!hasErrors && !newAddress.equals(address2.getSecond())) {
				addressText.setData(Boolean.FALSE);
				model.writeText(address2.getFirst(), newAddress);
			} else if (hasErrors) {
				addressText.setData(Boolean.TRUE);
			}
		});
	}

	public void refresh() {

		if (addressText.getData() != Boolean.TRUE) {
			String address = model.readModel(containerURI, EObject.class, "", container -> {
				Object addressObj = container.eGet(featureReference);
				if (!(addressObj instanceof LExpression)) {
					return "";
				}
				LExpression addressExp = (LExpression) addressObj;
				return util.getText(NodeModelUtils.getNode(addressExp));
			});
			addressText.setText(address);
		}

		if (addressText.getText().isEmpty()) {
			addressText.setEnabled(false);
			addressLabel.setEnabled(false);
			fixedAddressCheck.setSelection(false);
		} else {
			addressText.setEnabled(true);
			addressLabel.setEnabled(true);
			fixedAddressCheck.setSelection(true);
		}
	}

	public void setInput(String containerURI) {
		this.containerURI = containerURI;
		refresh();
	}

}
