package org.eclipse.cdt.linkerscript.ui.form;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.cdt.linkerscript.linkerScript.InputSection;
import org.eclipse.cdt.linkerscript.linkerScript.LExpression;
import org.eclipse.cdt.linkerscript.linkerScript.LNumberLiteral;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptFactory;
import org.eclipse.cdt.linkerscript.linkerScript.OutputSection;
import org.eclipse.cdt.linkerscript.linkerScript.OutputSectionTypeNoLoad;
import org.eclipse.cdt.linkerscript.linkerScript.StatementInputSection;
import org.eclipse.cdt.linkerscript.ui.internal.LinkerscriptActivator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.util.ITextRegion;

import com.google.common.base.Objects;
import com.google.inject.Injector;

public class OutputSectionDetailsPage extends AbstractFormPart implements IDetailsPage {

	private ILinkerScriptModel model;
	private Text rawText;
	private String inputURI;
	private Text nameText;
	private Button keepCheck;
	private ILinkerScriptModelListener modelListener = this::asyncUpdate;
	private Display display;
	private Button noLoadCheck;
	private Text addressText;
	private Button fixedAddressCheck;
	private IGrammarAccess grammar;
	private IParser parser;
	private Injector injector;
	private Text regionText;
	private Label addressLabel;
	private Button browseMemoryButton;

	public OutputSectionDetailsPage(ILinkerScriptModel model) {
		this.model = model;

		injector = LinkerscriptActivator.getInstance().getInjector("org.eclipse.cdt.linkerscript.LinkerScript");
		grammar = injector.getInstance(IGrammarAccess.class);
		parser = injector.getInstance(IParser.class);
	}

	@Override
	public void dispose() {
		model.removeModelListener(modelListener);
		super.dispose();
	}

	@Override
	public void selectionChanged(IFormPart part, ISelection selection) {
		IStructuredSelection ssel = (IStructuredSelection) selection;
		if (ssel.size() == 1) {
			inputURI = (String) ssel.getFirstElement();
		} else {
			inputURI = null;
		}

		update();
	}

	private void asyncUpdate() {
		if (!display.isDisposed()) {
			display.asyncExec(this::update);
		}
	}

	private Stream<InputSection> getInputSections(OutputSection outputSection) {
		return outputSection.getStatements().stream().filter(t -> t instanceof StatementInputSection)
				.map(t -> ((StatementInputSection) t).getSpec());
	}

	private void update() {
		if (inputURI == null || rawText.isDisposed()) {
			return;
		}

		rawText.setText(model.readModel(inputURI, OutputSection.class, "", outputSection -> {
			return NodeModelUtils.getNode(outputSection).getText();
		}));

		nameText.setText(model.readModel(inputURI, OutputSection.class, "", outputSection -> {
			return outputSection.getName();
		}));

		List<Boolean> keeps = model.readModel(inputURI, OutputSection.class, Collections.emptyList(), outputSection -> {
			return getInputSections(outputSection).map(InputSection::isKeep).collect(Collectors.toList());
		});

		boolean anyKeep = keeps.stream().anyMatch(t -> t);
		boolean anyNotKeep = keeps.stream().anyMatch(t -> !t);

		if (anyKeep && anyNotKeep) {
			keepCheck.setGrayed(true);
			keepCheck.setSelection(true);
		} else if (!anyKeep) {
			keepCheck.setGrayed(false);
			keepCheck.setSelection(false);
		} else {
			keepCheck.setGrayed(false);
			keepCheck.setSelection(true);
		}

		boolean noLoad = model.readModel(inputURI, OutputSection.class, false, outputSection -> {
			return outputSection.getType() instanceof OutputSectionTypeNoLoad;
		});
		noLoadCheck.setSelection(noLoad);

		if (addressText.getData() != Boolean.TRUE) {
			addressText.setText(model.readModel(inputURI, OutputSection.class, "", outputSection -> {
				LExpression address = outputSection.getAddress();
				if (address == null) {
					return "";
				}
				return getText(NodeModelUtils.getNode(address));
			}));
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

		String region = model.readModel(inputURI, OutputSection.class, null, outputSection -> outputSection.getMemory());
		if (region == null) {
			region = "";
		}
		regionText.setText(region);

	}

	public class Pair<S, T> {
		private S first;
		private T second;

		public Pair(S first, T second) {

			this.first = first;
			this.second = second;
		}
	}

	private static String getText(INode node) {
		INode rootNode = node.getRootNode();
		if (rootNode != null) {
			ITextRegion region = node.getTextRegion();
			int offset = region.getOffset();
			int length = region.getLength();
			return rootNode.getText().substring(offset, length + offset);
		}
		return null;
	}

	@Override
	public void createContents(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().create());

		FormToolkit toolkit = getManagedForm().getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		Color foreground = toolkit.getColors().getColor(IFormColors.TITLE);

		section.setText("Details");
		section.setDescription("Details about Output Section");
		section.setLayout(GridLayoutFactory.fillDefaults().create());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

		Composite composite = toolkit.createComposite(section);
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());
		section.setClient(composite);

		Label nameLabel = toolkit.createLabel(composite, "Output Section Name:");
		nameLabel.setForeground(foreground);
		nameLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, GridData.CENTER).create());

		nameText = toolkit.createText(composite, "Initial Name");
		nameText.setLayoutData(
				GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, GridData.CENTER).grab(true, false).create());
		nameText.addModifyListener(e -> model.writeModel(inputURI, OutputSection.class, sec -> {
			String newName = nameText.getText();
			if (!newName.equals(sec.getName())) {
				sec.setName(newName);
			}
		}));

		fixedAddressCheck = toolkit.createButton(composite, "Set Virtual Memory Address (VMA) explicitly", SWT.CHECK);
		fixedAddressCheck.setForeground(foreground); // TODO foreground not
														// working
		fixedAddressCheck.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).grab(true, false).create());
		fixedAddressCheck.addListener(SWT.Selection, e -> {
			boolean isFixedAddress = fixedAddressCheck.getSelection();
			addressText.setEnabled(isFixedAddress);

			model.writeModel(inputURI, OutputSection.class, outputSection -> {
				if (isFixedAddress) {
					LNumberLiteral literal = LinkerScriptFactory.eINSTANCE.createLNumberLiteral();
					literal.setValue(0L);
					outputSection.setAddress(literal);
				} else {
					outputSection.setAddress(null);
				}
			});
		});

		addressLabel = toolkit.createLabel(composite, "Virtual Memory Address:");
		addressLabel.setForeground(foreground);
		addressLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, GridData.CENTER).create());
		addressText = toolkit.createText(composite, "Initial Address");
		addressText.setLayoutData(
				GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, GridData.CENTER).grab(true, false).create());
		addressText.addModifyListener(e -> {
			// TODO this listener is too complicated
			Pair<String, String> address2 = model.readModel(inputURI, OutputSection.class, null, s -> {
				LExpression address = s.getAddress();
				if (address == null) {
					return null;
				}
				return new Pair<String, String>(s.eResource().getURIFragment(address),
						getText(NodeModelUtils.getNode(address)));
			});
			if (address2 == null) {
				return;
			}
			String newAddress = addressText.getText();
			ParserRule parserRule = (ParserRule) GrammarUtil.findRuleForName(grammar.getGrammar(), "LExpression");
			IParseResult result = parser.parse(parserRule, new StringReader(newAddress));
			boolean hasErrors = result.getSyntaxErrors().iterator().hasNext();

			if (!hasErrors && !newAddress.equals(address2.second)) {
				addressText.setData(Boolean.FALSE);
				model.writeText(address2.first, newAddress);
			} else if (hasErrors) {
				addressText.setData(Boolean.TRUE);
			}
		});

		Label regionLabel = toolkit.createLabel(composite, "Virtual Memory Region:");
		regionLabel.setForeground(foreground);
		regionLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, GridData.CENTER).create());
		regionText = toolkit.createText(composite, "Initial Region");
		regionText.setLayoutData(
				GridDataFactory.fillDefaults().span(1, 1).align(SWT.FILL, GridData.CENTER).grab(true, false).create());
		regionText.addModifyListener(e -> model.writeModel(inputURI, OutputSection.class, sec -> {
			String newRegion = regionText.getText();
			if (newRegion.isEmpty()) {
				newRegion = null;
			}
			if (!Objects.equal(sec.getMemory(), newRegion)) {
				sec.setMemory(newRegion);
			}
		}));

		browseMemoryButton = toolkit.createButton(composite, "Browse...", SWT.PUSH);
		browseMemoryButton.setLayoutData(
				GridDataFactory.fillDefaults().span(1, 1).align(SWT.FILL, GridData.CENTER).grab(true, false).create());

		keepCheck = toolkit.createButton(composite, "KEEP (Group will not be omitted even if unreferenced)", SWT.CHECK);
		keepCheck.setForeground(foreground); // TODO foreground not working
		keepCheck.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).grab(true, false).create());
		keepCheck.addListener(SWT.Selection, e -> {
			/* any change turns off the gray state */
			keepCheck.setGrayed(false);
		});
		keepCheck.addListener(SWT.Selection, e -> {
			model.writeModel(inputURI, OutputSection.class, outputSection -> {
				boolean newKeep = keepCheck.getSelection();
				Stream<InputSection> inputSections = getInputSections(outputSection);
				inputSections.forEach(inputSection -> {
					if (inputSection.isKeep() != newKeep) {
						inputSection.setKeep(newKeep);
					}
				});
			});
		});

		noLoadCheck = toolkit.createButton(composite, "Mark as \"NOLOAD\" (Group will not be loaded at runtime)",
				SWT.CHECK);
		noLoadCheck.setForeground(foreground); // TODO foreground not working
		noLoadCheck.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).grab(true, false).create());
		noLoadCheck.addListener(SWT.Selection, e -> {
			model.writeModel(inputURI, OutputSection.class, outputSection -> {
				boolean newNoLoad = noLoadCheck.getSelection();
				outputSection.setType(newNoLoad ? LinkerScriptFactory.eINSTANCE.createOutputSectionTypeNoLoad() : null);
			});
		});

		Label createLabel2 = toolkit.createLabel(composite, "Raw From File:");
		createLabel2.setForeground(foreground);
		rawText = toolkit.createText(composite, "Initial Raw From File", SWT.MULTI | SWT.READ_ONLY);
		rawText.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());
		rawText.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		display = parent.getDisplay();
		model.addModelListener(modelListener);
		update();
	}

}
