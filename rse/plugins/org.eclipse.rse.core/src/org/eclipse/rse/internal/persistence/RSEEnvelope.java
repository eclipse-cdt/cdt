/*********************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * David Dykstal (IBM) - [189274] provide import and export operations for profiles
 * David Dykstal (IBM) - [216858] Need the ability to Import/Export RSE connections for sharing
 * David Dykstal (IBM) - [233876] Filters lost after restart
 * David McKnight (IBM)- [433696] RSE profile merge does not handle property sets
 * David McKnight (IBM) -[439921] profile merge should allow optional host merge
 *********************************************************************************/

package org.eclipse.rse.internal.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.IRSECoreStatusCodes;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.core.RSECoreMessages;
import org.eclipse.rse.internal.core.filters.HostOwnedFilterPoolPattern;
import org.eclipse.rse.internal.persistence.dom.RSEDOMExporter;
import org.eclipse.rse.internal.persistence.dom.RSEDOMImporter;
import org.eclipse.rse.persistence.IRSEPersistenceManager;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;
import org.eclipse.rse.persistence.dom.IRSEDOMConstants;
import org.eclipse.rse.persistence.dom.RSEDOM;
import org.eclipse.rse.persistence.dom.RSEDOMNode;
  
/**
 * An envelope holds a version of a DOM that can be used for import and export of host, filterpool, and propertyset
 * information. The envelope is capable of adding its contents to a profile (an import) and can also be used for generating a 
 * stream of its contents that can be used later for restore (an export).
 */
public class RSEEnvelope {
	
	// IStatus is immutable so we can do this safely
	private static IStatus INVALID_FORMAT = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, IRSECoreStatusCodes.INVALID_FORMAT, RSECoreMessages.RSEEnvelope_IncorrectFormat, null);
	private static IStatus MODEL_NOT_EXPORTED = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, RSECoreMessages.RSEEnvelope_ModelNotExported);
	
	private RSEDOM dom = null;
	
	/**
	 * Creates an import/export envelope.
	 */
	public RSEEnvelope() {
	}
	
	/**
	 * Replaces the contents of this envelope with the contents found on the input stream.
	 * The format of the stream is determined by the persistence provider used to write the contents of that stream.
	 * The stream is closed at the end of the operation.
	 * This operation is performed in the thread of the caller.
	 * If asynchronous operation is desired place this invocation inside a job.
	 * @param in the input stream which is read into the envelope.
	 * @param monitor a monitor used for tracking progress and cancelation.
	 * If the monitor is cancelled this envelope will be empty.
	 * @throws CoreException if a problem occur reading the stream.
	 */
	public void get(InputStream in, IProgressMonitor monitor) throws CoreException {
		File envelopeFolder = getTemporaryFolder();
		IStatus status = unzip(in, envelopeFolder);
		if (status.isOK()) {
			String providerId = loadProviderId(envelopeFolder);
			IRSEPersistenceManager manager = RSECorePlugin.getThePersistenceManager();
			IRSEPersistenceProvider provider = manager.getPersistenceProvider(providerId);
			if (provider != null) {
				if (provider instanceof IRSEImportExportProvider) {
					IRSEImportExportProvider ieProvider = (IRSEImportExportProvider) provider;
					dom = ieProvider.importRSEDOM(envelopeFolder, monitor);
					if (dom == null) {
						status = INVALID_FORMAT;
					}
				} else {
					// invalid format due to bad persistence provider specfied
					status = INVALID_FORMAT;
				}
			} else {
				// invalid format due to provider not installed in this workbench
				status = INVALID_FORMAT;
			}
		}
		deleteFileSystemObject(envelopeFolder);
		if (!status.isOK()) {
			throw new CoreException(status);
		}
	}
	
	/**
	 * Exports the contents of the envelope to output stream.
	 * The format of the stream is determined by the persistence provider used.
	 * The id of the persistence provider is also recorded in the stream.
	 * The stream is closed at the end of the operation.
	 * This operation is performed in the same thread as the caller.
	 * If asynchronous operation is desired place this invocation inside a job.
	 * @param out the output stream into which the contents of this envelope will be written
	 * @param provider the persistence provider used to write the contents of this envelope
	 * @param monitor a monitor used for tracking progress and cancelation. If the monitor is cancelled the 
	 * receiving location is deleted.
	 * @throws CoreException containing a status describing the error, in particular this may be causes by 
	 * an IOException while preparing the contents or if the provider does not support export.
	 */
	public void put(OutputStream out, IRSEPersistenceProvider provider, IProgressMonitor monitor) throws CoreException {
		IStatus status = Status.OK_STATUS;
		if (provider instanceof IRSEImportExportProvider) {
			IRSEImportExportProvider exportProvider = (IRSEImportExportProvider) provider;
			File envelopeFolder = getTemporaryFolder();
			boolean saved = exportProvider.exportRSEDOM(envelopeFolder, dom, monitor);
			if (saved) {
				status = saveProviderId(envelopeFolder, exportProvider);
				if (status.isOK()) {
					status = zip(envelopeFolder, out);
				}
			deleteFileSystemObject(envelopeFolder);
			} else {
				status = MODEL_NOT_EXPORTED;
			}
		} else {
			status = MODEL_NOT_EXPORTED;
		}
		try {
			out.close();
		} catch (IOException e) {
			status = makeStatus(e);
		}
		if (!status.isOK()) {
			throw new CoreException(status);
		}
	}

	/**
	 * Adds a host to the envelope.
	 * If a host of the same name is already present in the envelope the new host will
	 * be renamed prior to adding it.
	 * @param host the host to be added to the envelope
	 */
	public void add(final IHost host) {
		// find and add the host-unique filter pools
		ISubSystem[] subsystems = host.getSubSystems();
		for (int i = 0; i < subsystems.length; i++) {
			ISubSystem subsystem = subsystems[i];
			ISystemFilterPool pool = subsystem.getUniqueOwningSystemFilterPool(false);
			if (pool != null) {
				add(pool);
			}
		}
		// add the host
		String type = IRSEDOMConstants.TYPE_HOST;
		String name = host.getName();
		Runnable action = new Runnable() {
			public void run() {
				RSEDOMExporter.getInstance().createNode(dom, host, true);
			}
		};
		addNode(type, name, action);
	}

	/**
	 * Adds a filter pool to the envelope.
	 * If a filter pool of the same name is already present in the envelope the new filter pool will
	 * be renamed prior to adding it.
	 * @param pool the filter pool to be added to the envelope
	 */
	public void add(final ISystemFilterPool pool) {
		// add the pool
		String type = IRSEDOMConstants.TYPE_FILTER_POOL;
		String name = pool.getName();
		Runnable action = new Runnable() {
			public void run() {
				RSEDOMExporter.getInstance().createNode(dom, pool, true);
			}
		};
		addNode(type, name, action);
	}

	/**
	 * Adds a property set to the envelope.
	 * If a property set of the same name is already present in the envelope the new property set will
	 * be renamed prior to adding it.
	 * @param propertySet the property set to be added to the envelope
	 */
	public void add(final IPropertySet propertySet) {
		// add the property set
		String type = IRSEDOMConstants.TYPE_FILTER_POOL;
		String name = propertySet.getName();
		Runnable action = new Runnable() {
			public void run() {
				RSEDOMExporter.getInstance().createNode(dom, propertySet, true);
			}
		};
		addNode(type, name, action);
	}

	/**
	 * Merges the contents of the envelope into the profile.
	 * @param profile the profile which is updated with the changes. The profile may be active or inactive.
	 */
	public void mergeWith(ISystemProfile profile) throws CoreException {
		mergeWith(profile, true);
	}
		
		
   /**
    * Merges the contents of the envelope into the profile.
    * @param profile the profile which is updated with the changes. The profile may be active or inactive.
    * @param allowDuplicateHosts indicates whether existing hosts should be merged or appended
	*/
	public void mergeWith(ISystemProfile profile, boolean allowDuplicateHosts) throws CoreException {
		List hostNodes = new ArrayList(10);
		List filterPoolNodes = new ArrayList(10);
		List propertySetNodes = new ArrayList(10);
		Map hostMap = new HashMap(10); // associates an original host name with a HostRecord
		if (dom != null) {
			RSEDOMNode[] children = dom.getChildren();
			for (int i = 0; i < children.length; i++) {
				RSEDOMNode child = children[i];
				String nodeType = child.getType();
				if (nodeType.equals(IRSEDOMConstants.TYPE_HOST)) {
					hostNodes.add(child);
				} else if (nodeType.equals(IRSEDOMConstants.TYPE_FILTER_POOL)) {
					filterPoolNodes.add(child);
				} else if (nodeType.equals(IRSEDOMConstants.TYPE_PROPERTY_SET)) {
					propertySetNodes.add(child);
				} else {
					throw new IllegalArgumentException("invalid dom node type"); //$NON-NLS-1$
				}
			}
			// create the hosts
			for (Iterator z = hostNodes.iterator(); z.hasNext();) {
				RSEDOMNode hostNode = (RSEDOMNode) z.next();
				String originalName = hostNode.getName();
				IHost host = mergeHost(profile, hostNode, allowDuplicateHosts);
				hostMap.put(originalName, host);
			}
			// create the filter pools
			for (Iterator z = filterPoolNodes.iterator(); z.hasNext();) {
				RSEDOMNode filterPoolNode = (RSEDOMNode) z.next();
				String filterPoolName = filterPoolNode.getName();
				String configurationId = filterPoolNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_ID).getValue();
				HostOwnedFilterPoolPattern pattern = new HostOwnedFilterPoolPattern(configurationId);
				String hostName = pattern.extract(filterPoolName);
				if (hostName != null) {
					IHost host = (IHost) hostMap.get(hostName);
					if (host != null) {
						mergeHostFilterPool(profile, host, filterPoolNode);
					} else {
						mergeFilterPool(profile, filterPoolNode);
					}
				} else {
					mergeFilterPool(profile, filterPoolNode);
				}
			}
			// create the property sets
			for (Iterator z = propertySetNodes.iterator(); z.hasNext();){
				RSEDOMNode propertySetNode = (RSEDOMNode) z.next();
				mergePropertySet(profile, propertySetNode);
			}
		}
	}
	
	private IHost mergeHost(ISystemProfile profile, RSEDOMNode hostNode) {
		return mergeHost(profile, hostNode, true);
	}
	
	
	private IHost mergeHost(ISystemProfile profile, RSEDOMNode hostNode, boolean allowDuplicates) {
		IHost host = null;
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		String baseHostName = hostNode.getName();
		String hostName = baseHostName;
		if (allowDuplicates && registry.getHost(profile, hostName) != null) {
			int n = 0;
			while (registry.getHost(profile, hostName) != null) {
				n++;
				hostName = baseHostName + "-" + n; //$NON-NLS-1$
			}
			hostNode.setName(hostName);
		}
		RSEDOMImporter importer = RSEDOMImporter.getInstance();
		host = importer.restoreHost(profile, hostNode);
		return host;
	}
	
	private void mergeHostFilterPool(ISystemProfile profile, IHost host, RSEDOMNode filterPoolNode) {
		String configurationId = filterPoolNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_ID).getValue();
		HostOwnedFilterPoolPattern pattern = new HostOwnedFilterPoolPattern(configurationId);
		String hostName = host.getAliasName();
		String filterPoolName = pattern.make(hostName);
		filterPoolNode.setName(filterPoolName);
		RSEDOMImporter importer = RSEDOMImporter.getInstance();
		ISystemFilterPool filterPool = importer.restoreFilterPool(profile, filterPoolNode);
		filterPool.setOwningParentName(hostName);
	}
	
	private IPropertySet mergePropertySet(ISystemProfile profile, RSEDOMNode propertySetNode) {
		RSEDOMImporter importer = RSEDOMImporter.getInstance();
		return importer.restorePropertySet(profile, propertySetNode);
	}
		
	private ISystemFilterPool mergeFilterPool(ISystemProfile profile, RSEDOMNode filterPoolNode) {
		ISystemFilterPool filterPool = getMatchingFilterPool(profile, filterPoolNode);
		if (filterPool != null) {
			String filterPoolName = filterPoolNode.getName();
			int n = 0;
			while (filterPool != null) {
				n++;
				filterPoolName = filterPoolName + "-" + n; //$NON-NLS-1$
				filterPoolNode.setName(filterPoolName);
				filterPool = getMatchingFilterPool(profile, filterPoolNode);
			}
		}
		RSEDOMImporter importer = RSEDOMImporter.getInstance();
		filterPool = importer.restoreFilterPool(profile, filterPoolNode);
		return filterPool;
	}
	
	private ISystemFilterPool getMatchingFilterPool(ISystemProfile profile, RSEDOMNode filterPoolNode) {
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		String filterPoolName = filterPoolNode.getName();
		String configurationId = filterPoolNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_ID).getValue();
		ISubSystemConfiguration subsystemConfiguration = registry.getSubSystemConfiguration(configurationId);
		ISystemFilterPoolManager manager = subsystemConfiguration.getFilterPoolManager(profile);
		ISystemFilterPool filterPool = manager.getSystemFilterPool(filterPoolName);
		return filterPool;
	}
	
	private IStatus saveProviderId(File parent, IRSEImportExportProvider provider) {
		IStatus status = Status.OK_STATUS;
		String providerId = provider.getId();
		File idFile = new File(parent, "provider.id"); //$NON-NLS-1$
		try {
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(idFile));
			out.write(providerId);
			out.close();
		} catch (IOException e) {
			status = makeStatus(e);
		}
		return status;
	}
	
	private String loadProviderId(File parent) throws CoreException {
		String providerId = null;
		File idFile = new File(parent, "provider.id"); //$NON-NLS-1$
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(idFile)));
			providerId = in.readLine();
			in.close();
		} catch (IOException e) {
			IStatus status = INVALID_FORMAT;
			throw new CoreException(status);
		}
		return providerId;
	}
	
	private void addNode(String type, String name, Runnable action) {
		ensureDOM();
		RSEDOMNode existingNode = dom.getChild(type, name);
		if (existingNode != null) {
			dom.removeChild(existingNode);
		}
		action.run();
	}

	private void ensureDOM() {
		if (dom == null) {
			dom = new RSEDOM("dom"); //$NON-NLS-1$
		}
	}
	
	private String generateName(List usedNames) {
		String prefix = "env_"; //$NON-NLS-1$
		int n = 0;
		String name = prefix + n;
		while (usedNames.contains(name)) {
			n += 1;
			name = prefix + n;
		}
		return name;
	}
	
	private IStatus zip(File source, OutputStream target) {
		IStatus status = Status.OK_STATUS;
		try {
			ZipOutputStream out = new ZipOutputStream(target);
			zipEntry(out, source, ""); //$NON-NLS-1$
			out.close();
		} catch (IOException e) {
			status = makeStatus(e);
		}
		return status;
	}
	
	private void zipEntry(ZipOutputStream out, File file, String entryName) {
		if (file.isDirectory()) {
			zipDirectoryEntry(out, file, entryName);
		} else {
			zipFileEntry(out, file, entryName);
		}
	}

	private void zipDirectoryEntry(ZipOutputStream out, File file, String entryName) {
		String fileName = file.getName();
		if (!(fileName.equals(".") || fileName.equals(".."))) { //$NON-NLS-1$ //$NON-NLS-2$
			if (entryName.length() > 0) {
				try {
					ZipEntry entry = new ZipEntry(entryName + "/"); //$NON-NLS-1$
					out.putNextEntry(entry);
					out.closeEntry();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				File child = files[i];
				String childName = child.getName();
				String childEntryName = entryName + "/" + childName; //$NON-NLS-1$
				zipEntry(out, child, childEntryName);
			}
		}
	}
	
	private void zipFileEntry(ZipOutputStream out, File file, String entryName) {
		try {
			ZipEntry entry = new ZipEntry(entryName);
			out.putNextEntry(entry);
			byte[] buffer = new byte[4096];
			FileInputStream in = new FileInputStream(file);
			for (int n = in.read(buffer); n >= 0; n = in.read(buffer)) {
				out.write(buffer, 0, n);
			}
			in.close();
			out.closeEntry();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private IStatus unzip(InputStream in, File root) {
		IStatus status = Status.OK_STATUS;
		try {
			ZipInputStream inZip = new ZipInputStream(in);
			ZipEntry entry = inZip.getNextEntry();
			while (entry != null) {
				String name = entry.getName();
				File target = new File(root, name);
				if (entry.isDirectory()) {
					target.mkdir();
				} else {
					byte[] buffer = new byte[4096];
					FileOutputStream out = new FileOutputStream(target);
					for (int n = inZip.read(buffer); n >= 0; n = inZip.read(buffer)) {
						out.write(buffer, 0, n);
					}
					out.close();
				}
				entry = inZip.getNextEntry();
			}
		} catch (FileNotFoundException e) {
			status = makeStatus(e);
		} catch (ZipException e) {
			RSECorePlugin.getDefault().getLogger().logError(RSECoreMessages.RSEEnvelope_IncorrectFormat, e);
			status = INVALID_FORMAT;
		} catch (IOException e) {
			status = makeStatus(e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				status = makeStatus(e);
			}
		}
		return status;
	}
	
	private IStatus deleteFileSystemObject(File file) {
		IStatus status = Status.OK_STATUS;
		String fileName = file.getName();
		if (!(fileName.equals(".") || fileName.equals(".."))) { //$NON-NLS-1$ //$NON-NLS-2$
			if (file.exists()) {
				if (file.isDirectory()) {
					File[] files = file.listFiles();
					for (int i = 0; i < files.length; i++) {
						File child = files[i];
						deleteFileSystemObject(child);
					}
				}
				file.delete();
			}
		}
		return status;
	}
	
	private IStatus makeStatus(Exception e) {
		IStatus status = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, "Unexpected exception", e); //$NON-NLS-1$
		return status;
	}

	/**
	 * @return a file handle to a temporary directory
	 */
	private File getTemporaryFolder() {
		IPath stateLocation = RSECorePlugin.getDefault().getStateLocation();
		File stateFolder = new File(stateLocation.toOSString());
		File envelopesFolder = new File(stateFolder, "envelopes"); //$NON-NLS-1$
		envelopesFolder.mkdir();
		List envelopeNames = Arrays.asList(envelopesFolder.list());
		String envelopeName = generateName(envelopeNames);
		File envelopeFolder = new File(envelopesFolder, envelopeName);
		envelopeFolder.mkdir();
		return envelopeFolder;
	}
	
}
