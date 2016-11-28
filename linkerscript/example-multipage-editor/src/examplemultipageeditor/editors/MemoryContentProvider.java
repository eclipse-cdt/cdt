package examplemultipageeditor.editors;

import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptStatement;
import org.eclipse.cdt.linkerscript.linkerScript.Memory;
import org.eclipse.cdt.linkerscript.linkerScript.MemoryCommand;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;

public class MemoryContentProvider implements IStructuredContentProvider {
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IXtextDocument) {
			IXtextDocument xtextDocument = (IXtextDocument) inputElement;
			return xtextDocument.readOnly((resource) -> {
				if (resource.getContents().isEmpty()) {
					return new Object[0];
				}
				LinkerScript root = (LinkerScript) resource.getContents().get(0);
				EList<LinkerScriptStatement> statements = root.getStatements();
				for (LinkerScriptStatement statement : statements) {
					if (statement instanceof MemoryCommand) {
						EList<Memory> list = ((MemoryCommand)statement).getMemories();
						return list.stream().map(memory -> resource.getURIFragment(memory)).toArray();
					}
				}
				return new Object[0];
			});
		}
		return new Object[0];
	}
}