/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ILanguage;
import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.SavedCodeReaderFactory;
import org.eclipse.cdt.internal.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBlockScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.DOMScanner;
import org.eclipse.cdt.internal.core.parser.scanner2.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.IScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPFunction;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPVariable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class GPPLanguage implements ILanguage {

	protected static final GPPScannerExtensionConfiguration CPP_GNU_SCANNER_EXTENSION = new GPPScannerExtensionConfiguration();
	
	public String getId() {
		return CCorePlugin.PLUGIN_ID + ".g++"; //$NON-NLS-1$
	}
	
	public IASTTranslationUnit getTranslationUnit(ITranslationUnit tu, int style) {
		IFile file = (IFile)tu.getResource();
        IProject project = file.getProject();
		IScannerInfo scanInfo = null;
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		if (provider != null){
			IScannerInfo buildScanInfo = provider.getScannerInformation(file);
			if (buildScanInfo != null)
				scanInfo = buildScanInfo;
			else
				scanInfo = new ScannerInfo();
		}
		
		// TODO - use different factories if we are working copy, or style
		// is skip headers.
		ICodeReaderFactory fileCreator = SavedCodeReaderFactory.getInstance();
		CodeReader reader = fileCreator.createCodeReaderForTranslationUnit(tu);
        if( reader == null )
            return null;

	    IScannerExtensionConfiguration scannerExtensionConfiguration = CPP_GNU_SCANNER_EXTENSION;
	    
		IScanner scanner = new DOMScanner(reader, scanInfo, ParserMode.COMPLETE_PARSE,
                ParserLanguage.CPP, ParserFactory.createDefaultLogService(), scannerExtensionConfiguration, fileCreator );
		ISourceCodeParser parser = new GNUCPPSourceParser( scanner, ParserMode.COMPLETE_PARSE, ParserUtil.getParserLogService(),
				new GPPParserExtensionConfiguration()  );

	    // Parse
		IASTTranslationUnit ast = parser.parse();

		if ((style & AST_USE_INDEX) != 0) 
			ast.setIndex(tu.getCProject().getIndex());
		
		return ast;
	}

	public ASTCompletionNode getCompletionNode(IWorkingCopy workingCopy,
			int offset) {
		return null;
	}
	
	// Binding types
	public static final int CPPVARIABLE = 1;
	public static final int CPPFUNCTION = 2;
	
	public PDOMBinding getPDOMBinding(PDOMDatabase pdom, int languageId, IASTName name) throws CoreException {
		IBinding binding = name.resolveBinding();
		if (binding == null)
			return null;
	
		if (binding instanceof CPPField) {
			return null;
		} else if (binding instanceof CPPVariable) {
			IScope scope = binding.getScope();
			if (!(scope instanceof CPPBlockScope))
				return new PDOMCPPVariable(pdom, languageId, name, (CPPVariable)binding);
		} else if (binding instanceof CPPMethod) {
			return null;
		} else if (binding instanceof CPPFunction) {
			return new PDOMCPPFunction(pdom, languageId, name, (CPPFunction)binding);
		}
		
		return null;
	}
	
	public PDOMBinding getPDOMBinding(PDOMDatabase pdom, PDOMBinding binding) throws CoreException {
		switch (binding.getBindingType()) {
		case CPPVARIABLE:
			return new PDOMCPPVariable(pdom, binding.getRecord());
		case CPPFUNCTION:
			return new PDOMCPPFunction(pdom, binding.getRecord());
		}
		
		return binding;
	}
	
}
