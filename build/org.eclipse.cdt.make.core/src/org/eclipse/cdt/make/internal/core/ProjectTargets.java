package org.eclipse.cdt.make.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProjectTargets {

	private static final String BUILD_TARGET_ELEMENT = "buildTargets"; //$NON-NLS-1$
	private static final String TARGET_ELEMENT = "target"; //$NON-NLS-1$
	private static final String TARGET_ATTR_ID = "targetID"; //$NON-NLS-1$
	private static final String TARGET_ATTR_PATH = "path";
	private static final String TARGET_ATTR_NAME = "name";
	private static final String TARGET_STOP_ON_ERROR = "stopOnError";
	private static final String TARGET_USE_DEFAULT_CMD = "useDefaultCommand";
	private static final String TARGET_ARGUMENTS = "buildArguments";
	private static final String TARGET_COMMAND = "buildCommand";
	private static final String TARGET = "buidlTarget";

	private HashMap targetMap = new HashMap();

	private IProject project;
	private MakeTargetManager manager;

	public ProjectTargets(MakeTargetManager manager, IProject project) {
		this.project = project;
		this.manager = manager;
	}

	public ProjectTargets(MakeTargetManager manager, IProject project, InputStream input) {
		this(manager, project);

		Document document = null;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = parser.parse(input);
		} catch (Exception e) {
			MakeCorePlugin.log(e);
		}
		Node node = document.getFirstChild();
		if (node.getNodeName().equals(BUILD_TARGET_ELEMENT)) {
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				node = list.item(i);
				if (node.getNodeName().equals(TARGET_ELEMENT)) {
					IContainer container = null;
					NamedNodeMap attr = node.getAttributes();
					String path = attr.getNamedItem(TARGET_ATTR_PATH).getNodeValue();
					if (path != null && !path.equals("")) {
						container = project.getFolder(path);
					} else {
						container = project;
					}
					try {
						MakeTarget target =
							new MakeTarget(
								manager,
								project,
								attr.getNamedItem(TARGET_ATTR_ID).getNodeValue(),
								attr.getNamedItem(TARGET_ATTR_NAME).getNodeValue());
						target.setContainer(container);
						String option = getString(node, TARGET_STOP_ON_ERROR);
						if (option != null) {
							target.setStopOnError(Boolean.valueOf(option).booleanValue());
						}
						option = getString(node, TARGET_USE_DEFAULT_CMD);
						if (option != null) {
							target.setUseDefaultBuildCmd(Boolean.valueOf(option).booleanValue());
						}
						option = getString(node, TARGET_COMMAND);
						if (option != null) {
							target.setBuildCommand(new Path(option));
						}
						option = getString(node, TARGET_ARGUMENTS);
						if (option != null) {
							target.setBuildArguments(option);
						}
						option = getString(node, TARGET);
						if (option != null) {
							target.setBuildTarget(option);
						}
						add(target);
					} catch (CoreException e) {
						MakeCorePlugin.log(e);
					}
				}
			}
		}
	}

	protected String getString(Node target, String tagName) {
		Node node = searchNode(target, tagName);
		return node != null ? (node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue()) : null;
	}

	protected Node searchNode(Node target, String tagName) {
		NodeList list = target.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals(tagName))
				return list.item(i);
		}
		return null;
	}

	public IMakeTarget[] get(IContainer container) {
		ArrayList list = (ArrayList) targetMap.get(container);
		if (list != null) {
			return (IMakeTarget[]) list.toArray(new IMakeTarget[list.size()]);
		}
		return new IMakeTarget[0];
	}

	public IMakeTarget findTarget(IContainer container, String name) {
		ArrayList list = (ArrayList) targetMap.get(container);
		if (list != null) {
			Iterator targets = list.iterator();
			while (targets.hasNext()) {
				IMakeTarget target = (IMakeTarget) targets.next();
				if (target.getName().equals(name)) {
					return target;
				}
			}
		}
		return null;
	}

	public void add(MakeTarget target) throws CoreException {
		ArrayList list = (ArrayList) targetMap.get(target.getContainer());
		if (list != null && list.contains(target)) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, MakeCorePlugin.getResourceString("MakeTargetManager.target_exists"), null)); //$NON-NLS-1$
		}
		if (list == null) {
			list = new ArrayList();
			targetMap.put(target.getContainer(), list);
		}
		list.add(target);
	}

	public boolean contains(MakeTarget target) {
		ArrayList list = (ArrayList) targetMap.get(target.getContainer());
		if (list != null && list.contains(target)) {
			return true;
		}
		return false;
	}

	public void remove(IMakeTarget target) {
		ArrayList list = (ArrayList) targetMap.get(target.getContainer());
		if (list == null || !list.contains(target)) {
			return;
		}
		list.remove(target);
		if (list.size() == 0) {
			targetMap.remove(list);
		}
	}

	public IProject getProject() {
		return project;
	}

	protected Document getAsXML() throws IOException {
		Document doc = new DocumentImpl();
		Element targetsRootElement = doc.createElement(BUILD_TARGET_ELEMENT);
		doc.appendChild(targetsRootElement);
		Iterator container = targetMap.entrySet().iterator();
		while (container.hasNext()) {
			List targets = (List) ((Map.Entry) container.next()).getValue();
			for (int i = 0; i < targets.size(); i++) {
				MakeTarget target = (MakeTarget) targets.get(i);
				targetsRootElement.appendChild(createTargetElement(doc, target));
			}
		}
		return doc;
	}

	private Node createTargetElement(Document doc, MakeTarget target) {
		Element targetElem = doc.createElement(TARGET_ELEMENT);
		targetElem.setAttribute(TARGET_ATTR_NAME, target.getName());
		targetElem.setAttribute(TARGET_ATTR_ID, target.getTargetBuilderID());
		targetElem.setAttribute(TARGET_ATTR_PATH, target.getContainer().getProjectRelativePath().toString());
		Element elem = doc.createElement(TARGET_COMMAND);
		targetElem.appendChild(elem);
		elem.appendChild(doc.createTextNode(target.getBuildCommand().toString()));

		elem = doc.createElement(TARGET_ARGUMENTS);
		elem.appendChild(doc.createTextNode(target.getBuildArguments()));
		targetElem.appendChild(elem);

		elem = doc.createElement(TARGET);
		elem.appendChild(doc.createTextNode(target.getBuildTarget()));
		targetElem.appendChild(elem);

		elem = doc.createElement(TARGET_STOP_ON_ERROR);
		elem.appendChild(doc.createTextNode(new Boolean(target.isStopOnError()).toString()));
		targetElem.appendChild(elem);

		elem = doc.createElement(TARGET_USE_DEFAULT_CMD);
		elem.appendChild(doc.createTextNode(new Boolean(target.isDefaultBuildCmd()).toString()));
		targetElem.appendChild(elem);
		return targetElem;
	}

	public void saveTargets(OutputStream output) throws IOException {
		Document doc = getAsXML();
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		format.setPreserveSpace(true);
		format.setLineSeparator(System.getProperty("line.separator")); //$NON-NLS-1$
		Serializer serializer =
			SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(new OutputStreamWriter(output, "UTF8"), format);
		serializer.asDOMSerializer().serialize(doc);
	}

}
