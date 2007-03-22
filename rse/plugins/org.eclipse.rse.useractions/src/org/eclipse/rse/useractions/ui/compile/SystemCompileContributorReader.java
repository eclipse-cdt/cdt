package org.eclipse.rse.useractions.ui.compile;

/*
 * (c) Copyright IBM Corp. 2000, 2003.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.ui.RSEUIPlugin;

/*
 * (c) Copyright IBM Corp. 2000, 2003.
 * All Rights Reserved.
 */
/**
 * This class reads configuration elements from the workbench's registry, creates contributors
 * and registers them with the manager.
 */
public class SystemCompileContributorReader {
	private SystemCompileContributorManager manager;
	private static final String COMPILE_COMMAND_ELEMENT_NAME = "compilecommand"; //$NON-NLS-1$

	public SystemCompileContributorReader() {
	}

	public void readCompileContributors(SystemCompileContributorManager mgr) {
		this.manager = mgr;
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(RSEUIPlugin.PLUGIN_ID, "compile"); //$NON-NLS-1$
		if (extensionPoint != null) {
			IExtension[] extensions = extensionPoint.getExtensions();
			for (int m = 0; m < extensions.length; m++) {
				IExtension extension = extensions[m];
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (int n = 0; n < elements.length; n++) {
					IConfigurationElement element = elements[n];
					// if the element is a compile command
					// then create a contributor that represents the element
					// and register the contributor with the contributor manager
					if (element.getName().equals(COMPILE_COMMAND_ELEMENT_NAME)) {
						SystemCompileContributor contributor = new SystemCompileContributor(element);
						manager.registerContributor(contributor);
					}
				}
			}
		}
	}
}