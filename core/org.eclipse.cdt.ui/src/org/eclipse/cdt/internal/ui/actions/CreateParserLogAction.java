/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ASTCache;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.indexer.ProjectIndexerInputAdapter;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;

@SuppressWarnings("nls")
public class CreateParserLogAction implements IObjectActionDelegate {
	private static final String INDENT = "   ";

	private static final class MyVisitor extends ASTVisitor {
		List<IASTProblem> fProblems= new ArrayList<IASTProblem>();
		List<IProblemBinding> fProblemBindings= new ArrayList<IProblemBinding>();
		List<Exception> fExceptions= new ArrayList<Exception>();

		MyVisitor() {
			shouldVisitProblems= true;
			shouldVisitNames= true;
		}

		@Override
		public int visit(IASTProblem problem) {
			fProblems.add(problem);
			return PROCESS_SKIP;
		}

		@Override
		public int visit(IASTName name) {
			if (name instanceof ICPPASTQualifiedName) {
				return PROCESS_CONTINUE;
			}
			try {
				IBinding binding= name.resolveBinding();
				if (binding instanceof IProblemBinding) {
					fProblemBindings.add((IProblemBinding) binding);
				}
			} catch (RuntimeException e) {
				fExceptions.add(e);
			}
			return PROCESS_CONTINUE;
		}
	}

	private static final Comparator<String> COMP_INSENSITIVE= new Comparator<String> () {
		@Override
		public int compare(String o1, String o2) {
			return o1.toUpperCase().compareTo(o2.toUpperCase());
		}
	};

	private ISelection fSelection;
	private IWorkbenchPartSite fSite;

	private boolean fWroteUnresolvedTitle;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fSite= targetPart.getSite();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = selection;
	}

	@Override
	public void run(IAction action) {
		if (!(fSelection instanceof IStructuredSelection))
			return;

		final String title= action.getText().replace("&", "");
		IStructuredSelection cElements= SelectionConverter.convertSelectionToCElements(fSelection);
		Iterator<?> i= cElements.iterator();
		ArrayList<ITranslationUnit> tuSelection= new ArrayList<ITranslationUnit>();
		while (i.hasNext()) {
			Object o= i.next();
			if (o instanceof ITranslationUnit) {
				tuSelection.add((ITranslationUnit) o);
			}
		}
		ITranslationUnit[] tuArray= tuSelection.toArray(new ITranslationUnit[tuSelection.size()]);
		if (tuArray.length == 0) {
			return;
		}
		FileDialog dlg= new FileDialog(fSite.getShell(), SWT.SAVE);
		dlg.setText(title);
		dlg.setFilterExtensions(new String[]{"*.log"});
		String path= null;
		while(path == null) {
			path= dlg.open();
			if (path == null)
				return;

			File file= new File(path);
			if (file.exists()) {
				if (!file.canWrite()) {
					final String msg= NLS.bind(ActionMessages.CreateParserLogAction_readOnlyFile, path);
					MessageDialog.openError(fSite.getShell(), title, msg);
					path= null;
				}
				else {
					final String msg = NLS.bind(ActionMessages.CreateParserLogAction_existingFile, path);
					if (!MessageDialog.openQuestion(fSite.getShell(), title, msg)) {
						path= null;
					}
				}
			}
		}

		try {
			PrintStream out= new PrintStream(path);
			try {
				boolean needsep= false;
				for (ITranslationUnit tu : tuArray) {
					if (needsep) {
						out.println(); out.println();
					}
					createLog(out, tu, new NullProgressMonitor());
					needsep= true;
				}
			}
			finally {
				out.close();
			}
		} catch (IOException e) {
			MessageDialog.openError(fSite.getShell(), action.getText(), e.getMessage());
		}
	}

	private void createLog(final PrintStream out, final ITranslationUnit tu, IProgressMonitor pm) {
		try {
			tu.open(pm);
		} catch (CModelException e) {
			CUIPlugin.log(e);
		}
		ASTProvider.getASTProvider().runOnAST(tu, ASTProvider.WAIT_IF_OPEN, pm, new ASTCache.ASTRunnable() {
			@Override
			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
				if (ast != null)
					return createLog(out, tu, lang, ast);
				return Status.CANCEL_STATUS;
			}
		});
	}

	protected IStatus createLog(PrintStream out, ITranslationUnit tu, ILanguage lang, IASTTranslationUnit ast) {
		IStatus status = Status.OK_STATUS;
		final ICProject cproject = tu.getCProject();
		final String projectName= cproject == null ? null : cproject.getElementName();
		final IIndex index = ast.getIndex();

		ITranslationUnit configureWith = tu;
		int ctxLinkage= 0;
		ISignificantMacros ctxSigMacros= null;
		if (tu instanceof TranslationUnit) {
			TranslationUnit itu= (TranslationUnit) tu;
			IIndexFile[] ctxToHeader = itu.getContextToHeader(index, ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT);
			if (ctxToHeader != null) {
				try {
					final IIndexFile ctxFile = ctxToHeader[0];
					ctxLinkage= ctxToHeader[0].getLinkageID();
					ctxSigMacros= ctxFile.getSignificantMacros();
					configureWith = CoreModelUtil.findTranslationUnitForLocation(ctxFile.getLocation(), cproject);
				} catch (CoreException e) {
				}
				if (configureWith == null) {
					configureWith= tu;
					ctxToHeader= null;
				}
			}
		}

		final ExtendedScannerInfo scfg= new ExtendedScannerInfo(configureWith.getScannerInfo(true));
		final MyVisitor visitor= new MyVisitor();
		ast.accept(visitor);

		out.println("Project:               " + projectName);
		out.println("File:                  " + tu.getLocationURI());
		out.println("Language:              " + lang.getName());
		out.println("Index Version:         " + PDOM.versionString(PDOM.getDefaultVersion()));
		out.println("Build Configuration:   " + getBuildConfig(cproject));
		if (configureWith == tu) {
			out.println("Context:               none");
		} else {
			out.println("Context:               " + configureWith.getLocationURI());
			out.println(INDENT + getLinkageName(ctxLinkage) + ", " + ctxSigMacros);
		}

		try {
			IIndexFile[] versions= index.getFiles(IndexLocationFactory.getIFL(tu));
			out.println("Versions in Index:     " + versions.length);
			for (IIndexFile f : versions) {
				out.println(INDENT + getLinkageName(f.getLinkageID()) + ": " + f.getSignificantMacros());
			}
		} catch (CoreException e) {
			status= e.getStatus();
		}
		out.println();

		output(out, "Include Search Path (option -I):", scfg.getIncludePaths());
		output(out, "Local Include Search Path (option -iquote):", scfg.getLocalIncludePath());
		output(out, "Preincluded files (option -include):", scfg.getIncludeFiles());
		output(out, "Preincluded macro files (option -imacros):", scfg.getMacroFiles());

		HashSet<String> reported= new HashSet<String>();
		output(out, "Macro definitions (option -D):", scfg.getDefinedSymbols(), reported);
		output(out, "Macro definitions (from language + headers in index):", ast.getBuiltinMacroDefinitions(), reported);
		output(out, "Macro definitions (from files actually parsed):", ast.getMacroDefinitions(), reported);

		try {
			outputUnresolvedIncludes(cproject, ast.getIndex(), out, ast.getIncludeDirectives(), ast.getLinkage().getLinkageID());
		} catch (CoreException e) {
			status= e.getStatus();
		}
		output(out, "Scanner problems:", ast.getPreprocessorProblems());
		output(out, "Parser problems:", visitor.fProblems.toArray(new IASTProblem[0]));
		output(out, "Unresolved names:", visitor.fProblemBindings.toArray(new IProblemBinding[0]));
		output(out, "Exceptions in name resolution:", visitor.fExceptions);
		out.println("Written on " + new Date().toString());
		return status;
	}

	private String getLinkageName(int linkageID) {
		switch(linkageID) {
		case ILinkage.NO_LINKAGE_ID: return ILinkage.NO_LINKAGE_NAME;
		case ILinkage.C_LINKAGE_ID: return ILinkage.C_LINKAGE_NAME;
		case ILinkage.CPP_LINKAGE_ID: return ILinkage.CPP_LINKAGE_NAME;
		case ILinkage.FORTRAN_LINKAGE_ID: return ILinkage.FORTRAN_LINKAGE_NAME;
		case ILinkage.OBJC_LINKAGE_ID: return ILinkage.OBJC_LINKAGE_NAME;
		}
		return String.valueOf(linkageID);
	}

	private String getBuildConfig(ICProject cproject) {
    	ICProjectDescriptionManager prjDescMgr= CCorePlugin.getDefault().getProjectDescriptionManager();
    	ICProjectDescription prefs= prjDescMgr.getProjectDescription(cproject.getProject(), false);
    	if (prefs != null) {
    		ICConfigurationDescription cfg= prefs.getDefaultSettingConfiguration();
    		if (cfg != null)
    			return cfg.getName();
    	}
    	return "unknown";
	}

	private void outputUnresolvedIncludes(ICProject prj, IIndex index, PrintStream out,
			IASTPreprocessorIncludeStatement[] includeDirectives, int linkageID) throws CoreException {
		fWroteUnresolvedTitle= false;
		ASTFilePathResolver resolver= new ProjectIndexerInputAdapter(prj);
		HashSet<IIndexFile> handled= new HashSet<IIndexFile>();
		for (IASTPreprocessorIncludeStatement include : includeDirectives) {
			if (include.isResolved()) {
				IIndexFileLocation ifl = resolver.resolveASTPath(include.getPath());
				IIndexFile ifile= index.getFile(linkageID, ifl, include.getSignificantMacros());
				outputUnresolvedIncludes(index, out, ifl, ifile, handled);
			}
		}
		if (fWroteUnresolvedTitle)
			out.println();
	}

	private void outputUnresolvedIncludes(IIndex index, PrintStream out,
			IIndexFileLocation ifl, IIndexFile ifile, Set<IIndexFile> handled) throws CoreException {
		if (ifile == null) {
			writeUnresolvedTitle(out);
			out.println(INDENT + ifl.getURI() + " is not indexed");
		} else if (handled.add(ifile)) {
			IIndexInclude[] includes = ifile.getIncludes();
			for (IIndexInclude inc : includes) {
				if (inc.isActive()) {
					if (inc.isResolved()) {
						IIndexFile next = index.resolveInclude(inc);
						outputUnresolvedIncludes(index, out, inc.getIncludesLocation(), next, handled);
					} else {
						writeUnresolvedTitle(out);
						out.println(INDENT + "Unresolved inclusion: " + inc.getFullName() + " in file " +
								inc.getIncludedByLocation().getURI());
					}
				}
			}
		}
	}

	public void writeUnresolvedTitle(PrintStream out) {
		if (!fWroteUnresolvedTitle) {
			fWroteUnresolvedTitle= true;
			out.println("Unresolved includes (from headers in index):");
		}
	}

	private void output(PrintStream out, String label, String[] list) {
		if (list.length > 0) {
			out.println(label);
			for (String line : list) {
				out.println(INDENT + line);
			}
			out.println();
		}
	}

	private void output(PrintStream out, String label, Map<String, String> definedSymbols, HashSet<String> reported) {
		if (!definedSymbols.isEmpty()) {
			out.println(label);

			SortedMap<String, String> sorted= new TreeMap<String, String>(COMP_INSENSITIVE);
			sorted.putAll(definedSymbols);
			for (Entry<String, String> entry : sorted.entrySet()) {
				final String macro = entry.getKey() + '=' + entry.getValue();
				if (reported.add(macro)) {
					out.println(INDENT + macro);
				}
			}
			out.println();
		}
	}

	private void output(PrintStream out, String label, IASTPreprocessorMacroDefinition[] defs, HashSet<String> reported) {
		if (defs.length > 0) {
			out.println(label);
			SortedSet<String> macros= new TreeSet<String>(COMP_INSENSITIVE);
			for (IASTPreprocessorMacroDefinition def : defs) {
				macros.add(def.toString());
			}

			for (String macro : macros) {
				if (reported.add(macro)) {
					out.println(INDENT + macro);
				}
			}
			out.println();
		}
	}

	private void output(PrintStream out, String label, IASTProblem[] preprocessorProblems) {
		if (preprocessorProblems.length > 0) {
			out.println(label);
			for (IASTProblem problem : preprocessorProblems) {
				out.println(INDENT + problem.getMessageWithLocation());
			}
			out.println();
		}
	}

	private void output(PrintStream out, String label, IProblemBinding[] list) {
		if (list.length > 0) {
			out.println(label);
			for (IProblemBinding problem : list) {
				String file= problem.getFileName();
				int line = problem.getLineNumber();
				out.println(INDENT + problem.getMessage() + " in file " + file + ':' + line);
			}
			out.println();
		}
	}

	private void output(PrintStream out, String label, List<Exception> list) {
		if (!list.isEmpty()) {
			out.println(label);
			for (Exception problem : list) {
				problem.printStackTrace(out);
			}
			out.println();
		}
	}
}
