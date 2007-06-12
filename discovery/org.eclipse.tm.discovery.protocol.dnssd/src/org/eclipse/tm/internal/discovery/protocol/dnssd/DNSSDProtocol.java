/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 *   Javier Montalvo Orus (Symbian) - added transport key
 *   Javier Montalvo Orus (Symbian) - [plan] Improve Discovery and Autodetect in RSE
 *   Javier Montalvo Orus (Symbian) - [191207] DNS-SD adds duplicated transport attribute when discovery is refreshed
 ********************************************************************************/

package org.eclipse.tm.internal.discovery.protocol.dnssd;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.tm.discovery.model.Device;
import org.eclipse.tm.discovery.model.ModelFactory;
import org.eclipse.tm.discovery.model.Network;
import org.eclipse.tm.discovery.model.Pair;
import org.eclipse.tm.discovery.model.Service;
import org.eclipse.tm.discovery.model.ServiceType;
import org.eclipse.tm.discovery.protocol.IProtocol;
import org.eclipse.tm.discovery.transport.ITransport;



/**
 * DNS-based Service Discovery implementation based on <a
 * href="http://files.dns-sd.org/draft-cheshire-dnsext-dns-sd.txt">DNS-Based
 * Service Discovery</a> <br/><br/> The DNS packets supported by the implementation are:
 * 
 * <ul>
 * <li> DNS Pointer resource record (PTR) <br/> <table border="1">
 * <tr>
 * <td>Name</td>
 * <td>Type</td>
 * <td>Class</td>
 * <td>TTL</td>
 * <td>Data Size</td>
 * <td>Domain Name</td>
 * </tr>
 * </table> <br/>
 * <li> DNS Service resource record (SRV)<br/> <table border="1">
 * <tr>
 * <td>Name</td>
 * <td>Type</td>
 * <td>Class</td>
 * <td>TTL</td>
 * <td>Data Size</td>
 * <td>Priority</td>
 * <td>Weight</td>
 * <td>Port</td>
 * <td>Target</td>
 * </tr>
 * </table> <br/>
 * <li> DNS Text resource record (TXT) <br/> <table border="1">
 * <tr>
 * <td>Name</td>
 * <td>Type</td>
 * <td>Class</td>
 * <td>TTL</td>
 * <td>Data Size</td>
 * <td>Data Pairs</td>
 * </tr>
 * </table> <br/>
 * <li> DNS Address resource record (A) <br/> <table border="1">
 * <tr>
 * <td>Name</td>
 * <td>Type</td>
 * <td>Class</td>
 * <td>TTL</td>
 * <td>Data Size</td>
 * <td>Address</td>
 * </tr>
 * </table>
 * </ul>
 * <br/>
 * 
 * 
 */
public class DNSSDProtocol implements IProtocol {

	// DNS Pointer resource record	identifier 
	private final static int PTR = 0x0C;

	// DNS Service resource record identifier 
	private final static int SRV = 0x21;

	// DNS Text resource record identifier 
	private final static int TXT = 0x10;

	// DNS Address resource record	identifier 
	private final static int A = 0x01;
	
	// DNS packet reference.
	private byte[] packet;
	
	// provide 64k for the received packet 
	private final int MAX_PACKET_SIZE = 65535;
	private byte[] buffer = new byte[MAX_PACKET_SIZE];
	
	//IP address identifying the target
	private String address;
	
	// Queries for services and legacy services 
	private final String SERVICE_DISCOVERY_COMMAND = Messages.getString("DNSSDProtocol.ServiceDiscoveryCommand"); //$NON-NLS-1$
	private final  String LEGACY_SERVICE_DISCOVERY_COMMAND = Messages.getString("DNSSDProtocol.legacyServiceDiscoveryCommand"); //$NON-NLS-1$

	// Patterns to parse service name and service type
	
	private final Pattern srvPattern = Pattern.compile("^(.+)\\._(.+)._(.+)\\.local."); //$NON-NLS-1$
	private final Pattern ptrPattern = Pattern.compile("^_(.+)._.+\\.local."); //$NON-NLS-1$
	
	private final String TRANSPORT_KEY = "transport"; //$NON-NLS-1$
	
	private Resource resource = null;
	private ITransport transport = null;
	private String query = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.tm.discovery.protocol.IProtocol#getQueries()
	 */
	public String[] getQueries()
	{
		return new String[]{
				SERVICE_DISCOVERY_COMMAND,
				LEGACY_SERVICE_DISCOVERY_COMMAND
		};
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.tm.discovery.protocol.IProtocol#getDiscoveryJob(java.lang.String, org.eclipse.emf.ecore.resource.Resource, org.eclipse.tm.discovery.transport.ITransport)
	 */
	public Job getDiscoveryJob(String aQuery, Resource aResource, ITransport aTransport){
		
		resource = aResource;
		transport = aTransport;
		query = aQuery;
		
		return new Job(Messages.getString("DNSSDProtocol.JobName")) { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				if (transport != null) {
					sendQuery(transport, query, PTR);

					Vector discoveredServices = new Vector();

					if (!resource.getContents().isEmpty()) {

						Iterator deviceIterator = ((Network) resource.getContents().get(0)).getDevice().iterator();
						while (deviceIterator.hasNext()) {
							Device device = (Device) deviceIterator.next();
							Iterator serviceTypeIterator = device.getServiceType().iterator();
							while (serviceTypeIterator.hasNext()) {
								ServiceType serviceType = (ServiceType) serviceTypeIterator.next();
								if (serviceType.getName().equals(SERVICE_DISCOVERY_COMMAND) || 
									serviceType.getName().equals(LEGACY_SERVICE_DISCOVERY_COMMAND)) {
									Iterator serviceIterator = serviceType.getService().iterator();
									while (serviceIterator.hasNext()) {
										Service service = (Service) serviceIterator.next();

										if (!discoveredServices.contains(service.getName())) {
											discoveredServices.add(service.getName());
										}

									}
									serviceTypeIterator.remove();
								}
							}
						}

						for (int i = 0; i < discoveredServices.size(); i++) {
							sendQuery(transport,(String) discoveredServices.elementAt(i),	PTR);
						}
					}
				}
				return new Status(IStatus.OK,
						"org.eclipse.rse.discovery.engine", IStatus.OK, //$NON-NLS-1$
						Messages.getString("DNSSDProtocol.FinishedJobName"), null); //$NON-NLS-1$
			}
		};
	}
	
	/*
	 * Creates and sends the specified query in a DNS-SD packet and call the function to populate the model with the received data
	 */
	private void sendQuery(ITransport transport, String query, int type) {
		try {
			
			//clean buffer
			for (int i = 0; i < buffer.length; i++) {
				buffer[i]=0;
			}
			
			// number of queries (1) 
			buffer[4] = (byte) 0x00;
			buffer[5] = (byte) 0x01;
			
			//jump to the data section of the packet letting the other fields as 0s
			int index = 12;
			
			StringTokenizer tokenizer = new StringTokenizer(query, "."); //$NON-NLS-1$

			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();

				buffer[index] = (byte) token.length();
				index++;

				for (int subIndex = 0; subIndex < token.getBytes().length; subIndex++) {
					buffer[index + subIndex] = token.getBytes()[subIndex];
				}
				index += token.getBytes().length;
			}

			//end of data section
			buffer[index++] = 0x00;
			
			// type TXT-SRV-PTR
			buffer[index++] = 0x00;
			buffer[index++] = (byte) (type & 0xFF);

			//inet
			buffer[index++] = 0x00;
			buffer[index++] = 0x01;
			
			packet = new byte[index];
			for (int position = 0; position < index; position++)
				packet[position] = buffer[position];
			
			//send the packet using the provided ITransport implementation
			transport.send(packet);
			
			// wait to receive data until timeout
			while (true) {
				address = transport.receive(buffer);
				packet = buffer;
				populateModel(resource);
			}
			
		} catch (Exception e) {
			// timeout, no more services to discover
		}
	}
	

	/*
	 * Populates the provided model with the contents of the received packet
	 */
	private void populateModel(Resource resource) {
		Network network = null;
		Device device = null;
		boolean found = false;

		Iterator deviceIterator = null;
		
		if (resource.getContents().isEmpty()) {
			network = ModelFactory.eINSTANCE.createNetwork();
			resource.getContents().add(network);
		} else {
			network = (Network) resource.getContents().get(0);
		}

		deviceIterator = network.getDevice().iterator();
		while (deviceIterator.hasNext()) {
			Device aDevice = (Device) deviceIterator.next();
			if (aDevice.getAddress().equals(address)) {
				device = aDevice;
				found = true;
				break;
			}
		}
		if (!found) {
			device = ModelFactory.eINSTANCE.createDevice();
			device.setAddress(address);

			network.getDevice().add(device);
		}

		ByteArrayInputStream packetInputStream = new ByteArrayInputStream(packet);

		//skip transactionID+flags
		packetInputStream.skip(4);

		int queriesNumber = packetInputStream.read() << 8 | packetInputStream.read();
		int answersNumber = packetInputStream.read() << 8 | packetInputStream.read();

		//skip authority RRs
		packetInputStream.skip(2);

		int additionalRecordsNumber = packetInputStream.read() << 8 | packetInputStream.read();

		// read queries
		for (int i = 0; i < queriesNumber; i++) {
			getName(packetInputStream, packet);
			//skip type
			packetInputStream.skip(2);
			//skip class
			packetInputStream.skip(2);
		}

		//read answers
		for (int i = 0; i < answersNumber + additionalRecordsNumber; i++) {
			found = false;

			String name = getName(packetInputStream, packet);

			//packet type
			int type = packetInputStream.read() << 8 | packetInputStream.read();

			//skip class
			packetInputStream.skip(2);

			//skip TTL
			packetInputStream.skip(4);

			switch (type) {

			/*
			 *  A PACKET (DNS Address resource record identifier)
			 */
			case DNSSDProtocol.A:
				handleARecord(packetInputStream, device, name);
				break;

			/*
			 *  PTR PACKET (DNS Pointer resource record	identifier)
			 */
			case DNSSDProtocol.PTR:
				handlePTRRecord(packetInputStream, device, name);
				break;

			/*
			 *  SRV PACKET (DNS Service resource record	identifier)
			 */
			case DNSSDProtocol.SRV:
				handleSRVRecord(packetInputStream, device, name);
				break;

			/*
			 *  TXT PACKET (DNS Text resource record identifier)
			 */
			case DNSSDProtocol.TXT:
				handleTXTRecord(packetInputStream, device, name);
				break;
			}
		}
	}
	
	private void handleARecord(ByteArrayInputStream packetInputStream, Device device, String name) {
		int dataLength = packetInputStream.read() << 8 | packetInputStream.read();
		
		//skip address
		packetInputStream.skip(dataLength);
		
		if (device != null) {
			device.setName(name.substring(0, name.indexOf('.') ));
		}	
	}
	
	
	
	private void handlePTRRecord(ByteArrayInputStream packetInputStream, Device device, String name) {
		
		Service service = ModelFactory.eINSTANCE.createService();
		ServiceType serviceType = ModelFactory.eINSTANCE.createServiceType();
		
		//skip dataLength
		packetInputStream.skip(2);
	
		String ptrDataName = getName(packetInputStream, packet);
		
		String serviceTypeName = name;

		//parse the service type name
		if(!(serviceTypeName.equals(SERVICE_DISCOVERY_COMMAND) || serviceTypeName.equals(LEGACY_SERVICE_DISCOVERY_COMMAND)))
		{
			Matcher matcher = ptrPattern.matcher(name);
		  		if (matcher.matches())
		  			serviceTypeName = matcher.group(1);
		}
		
		//find if we have a serviceType with this name...
		Iterator serviceTypeIterator = device.getServiceType().iterator();
		boolean found = false;
		while (serviceTypeIterator.hasNext()) {
			ServiceType aServiceType = (ServiceType) serviceTypeIterator.next();
			if (aServiceType.getName().equals(serviceTypeName)) {
				serviceType = aServiceType;
				found = true;
				break;
			}
		}
		if (!found) {
			serviceType = ModelFactory.eINSTANCE.createServiceType();
			serviceType.setName(serviceTypeName);
			device.getServiceType().add(serviceType);
		}
	
		if (!ptrDataName.equals("")) { //$NON-NLS-1$
			//find if we have a service with this name...
			
			String serviceName = null;

			if(serviceTypeName.equals(SERVICE_DISCOVERY_COMMAND) || serviceTypeName.equals(LEGACY_SERVICE_DISCOVERY_COMMAND))
			{
				serviceName = ptrDataName;
			}
			else
			{
				//parse the service type name
				Matcher matcher = srvPattern.matcher(ptrDataName);
		  			if (matcher.matches()) 
		  				serviceName = matcher.group(1);
			}
			
			Iterator serviceIterator = serviceType.getService().iterator();
			found = false;
			while (serviceIterator.hasNext()) {
				Service aService = (Service) serviceIterator.next();
				if (aService.getName().equals(serviceName)) {
					service = aService;
					found = true;
					break;
				}
			}
	
			if (!found) {
				service = ModelFactory.eINSTANCE.createService();
				service.setName(serviceName);
				serviceType.getService().add(service);
			}
		}
	}
	
	private void handleSRVRecord(ByteArrayInputStream packetInputStream, Device device, String name) {

		Service service = ModelFactory.eINSTANCE.createService();
		ServiceType serviceType = ModelFactory.eINSTANCE.createServiceType();
		
		//data of the packet, without bytes for priority-weight-port
		int dataLength = (packetInputStream.read() << 8 | packetInputStream.read()) - 6;

		int priority = packetInputStream.read() << 8 | packetInputStream.read();
		int weight = packetInputStream.read() << 8 | packetInputStream.read();
		int port = packetInputStream.read() << 8 | packetInputStream.read();

		byte[] data = new byte[dataLength];
		try {
			packetInputStream.read(data);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String serviceTypeName = null;
		String serviceName = null;

		//parse the service type name and the service name
		Matcher matcher = srvPattern.matcher(name);
		if (matcher.matches()) 
		{
			serviceTypeName = matcher.group(2);
			serviceName = matcher.group(1);
		}
	  	
		//	find if we have a serviceType with this name...
		
		Iterator serviceTypeIterator = device.getServiceType().iterator();
		boolean found = false;
		while (serviceTypeIterator.hasNext()) {
			ServiceType aServiceType = (ServiceType) serviceTypeIterator.next();
			if (aServiceType.getName().equals(serviceTypeName)) {
				serviceType = aServiceType;
				found = true;
				break;
			}
		}

		if (!found) {
			serviceType = ModelFactory.eINSTANCE.createServiceType();
			serviceType.setName(serviceTypeName);
			device.getServiceType().add(serviceType);
		}

		//find if we have a service with this name...
		Iterator serviceIterator = serviceType.getService().iterator();
		found = false;
		while (serviceIterator.hasNext()) {
			Service temp = (Service) serviceIterator.next();
			if (temp.getName().equals(serviceName)) {
				service = temp;
				found = true;
				break;
			}
		}

		if (!found) {
			service = ModelFactory.eINSTANCE.createService();
			service.setName(serviceName);
			serviceType.getService().add(service);
		}

		service.setName(serviceName);
		
		String[] keys = new String[]{"port","priority","weight"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String[] values = new String[]{port+"",priority+"",weight+""}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		for (int i = 0; i < keys.length; i++) {
		
			Pair text = null;
			Iterator pairIterator = service.getPair().iterator();
			found = false;
			while (pairIterator.hasNext()) {
				Pair aPair = (Pair) pairIterator.next();
				if (aPair != null)
				{
					if (aPair.getKey().equals(keys[i])) {
						String current = aPair.getValue();
						if (!current.equals(values[i]))
							aPair.setValue(values[i]);
						found = true;
						break;
					}
				}
			}

			if (!found) {
				text = ModelFactory.eINSTANCE.createPair();
				text.setKey(keys[i]);
				text.setValue(values[i]);
				service.getPair().add(text);
			}
		}
	}
	
	private void handleTXTRecord(ByteArrayInputStream packetInputStream, Device device, String recordName) {
		ServiceType serviceType = ModelFactory.eINSTANCE.createServiceType();
		Service service = ModelFactory.eINSTANCE.createService();
		int dataLength = packetInputStream.read() << 8 | packetInputStream.read();

		byte [] data = new byte[dataLength];
		try {
			packetInputStream.read(data);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String serviceName = null;
		String serviceTypeName = null;
		String serviceTransport = null;
		
		// Find if we have a serviceType with this name...
		
		Matcher matcher = srvPattern.matcher(recordName);
			if (matcher.matches())
			{
				serviceName = matcher.group(1);
				serviceTypeName = matcher.group(2);
				serviceTransport = matcher.group(3);
			}
	
		
	  	Iterator serviceTypeIterator = device.getServiceType().iterator();
		boolean found = false;
		while (serviceTypeIterator.hasNext()) {
			ServiceType aServiceType = (ServiceType) serviceTypeIterator.next();
			if (aServiceType != null) {
				if (aServiceType.getName().equals(serviceTypeName)) {
					serviceType = aServiceType;
					found = true;
					break;
				}
			}
		}

		if (!found) {
			serviceType = ModelFactory.eINSTANCE.createServiceType();
			serviceType.setName(serviceTypeName);
			device.getServiceType().add(serviceType);
		}

		// Find if we have a service with this name...
			
		Iterator serviceIterator = serviceType.getService().iterator();
		found = false;
		while (serviceIterator.hasNext()) {
			Service aService = (Service) serviceIterator.next();
			if (aService != null) {
				if (aService.getName().equals(serviceName)) {
					service = aService;
					found = true;
					break;
				}
			}
		}

		if (!found) {
			service = ModelFactory.eINSTANCE.createService();
			service.setName(recordName);
			serviceType.getService().add(service);
		}
		
		//add or update discovered transport if available in response
		if(serviceTransport != null)
		{
			
			Iterator pairIterator = service.getPair().iterator();
			found = false;
			while (pairIterator.hasNext()) {
				Pair aPair = (Pair) pairIterator.next();
				if (aPair != null) {
					if (TRANSPORT_KEY.equals(aPair.getKey())) {

						//update transport value
						aPair.setValue(serviceTransport);
						found = true;
						break;
					}
				}
			}

			if (!found) {
				Pair transportPair = ModelFactory.eINSTANCE.createPair();
				transportPair.setKey(TRANSPORT_KEY);
				transportPair.setValue(serviceTransport);
				service.getPair().add(transportPair);
			}
			
			
		}
		
		//process "key=value" pairs
		
		StringBuffer dataBuffer = new StringBuffer();
		int entryLength = 0;
		
		for (int j = 0; j < dataLength; j += entryLength + 1) {

			dataBuffer.setLength(0);

			entryLength = data[j];

			for (int k = 1; k <= entryLength; k++)
				dataBuffer.append((char) data[j + k]);

			StringTokenizer stk = new StringTokenizer(dataBuffer.toString(),"="); //$NON-NLS-1$

			String key = stk.nextToken();

			// DNS-Based Service Discovery
			// 6.4 Rules for Names in DNS-SD Name/Value Pairs
			// If a key has no value, assume "true"
			String value = "true"; //$NON-NLS-1$

			try {
				value = stk.nextToken();
			} catch (Exception e) {
				// no value, assume "true"
			}

			// find if we are updating the value of a key...
			Pair text = null;
			Iterator pairIterator = service.getPair().iterator();
			found = false;
			while (pairIterator.hasNext()) {
				Pair aPair = (Pair) pairIterator.next();
				if (aPair != null) {
					if (aPair.getKey().equals(key)) {
						String current = aPair.getValue();
						if (!current.equals(value))
							aPair.setValue(value);
						found = true;
						break;
					}
				}
			}

			if (!found) {
				text = ModelFactory.eINSTANCE.createPair();
				text.setKey(key);
				text.setValue(value);
				service.getPair().add(text);
			}
		}
	}

	// returns the name, that can be compressed using DNS compression
	// For more information about DNS compression: RFC 1035 (4.1.4. Message compression)
	private String getName(ByteArrayInputStream packetInputStream, byte[] packet) {
		StringBuffer buffer = new StringBuffer();
		int nextByte = 0;

		while (true) {
			nextByte = packetInputStream.read();

			//check if it's a pointer
			//pointer: |11xxxxxx|-|xxxxxxxx| where xxx...x is the pointer in the packet
			if ((nextByte & 0xC0) == 0xC0) {
				int upperByte = (nextByte & (byte) 0x3F) << 8;
				int offset = packetInputStream.read() | upperByte;
				buffer.append(getReference(packet, offset));
				break;
			} else if (nextByte == 0x00) {
				break;
			} else {
				for (int i = 0; i < nextByte; i++) {
					buffer.append((char)packetInputStream.read());
				}
				buffer.append('.');
			}
		}
		return buffer.toString();
	}

	private String getReference(byte[] packet, int offset) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; packet[offset + i] != 0x00;) {
			int numReads = packet[offset + i];
			
			//check if it's a pointer
			//pointer: |11xxxxxx|-|xxxxxxxx| where xxx...x is the pointer in the packet
			if ((numReads & 0xC0) == 0xC0) {
				int upperByte = (numReads & (byte) 0x3F) << 8;
				int nextOffset = packet[offset + i + 1] | upperByte;
				buffer.append(getReference(packet, nextOffset));
				break;
			} else {
				for (int j = 0; j < numReads; j++) {
					buffer.append((char) packet[offset + i + j + 1]);
				}
				buffer.append('.');
			}
			i += (numReads + 1);
		}
		return buffer.toString();
	}

	

}
