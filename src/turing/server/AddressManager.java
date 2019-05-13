package turing.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.TreeSet;

/**
 * Implements a concurrent multicast address manager
 */
public class AddressManager {
	private TreeSet<InetAddress> addresses = new TreeSet<>(new InetAddressComparator()); // set of addresses
	private int c = 0, b = 0, a = 0; // address parts

	/**
	 * Creates a new address manager
	 */
	public AddressManager() {
		super();
	}

	/**
	 * Creates and reserves a new multicast address
	 *
	 * @return the generated address, if free
	 *         null otherwise
	 */
	public synchronized InetAddress createAddress() {
		InetAddress address;

		// search for a free address
		for (long i = 0; i < Math.pow(256, 3); i++, nextAddress()) {
			try {
				// 239.0.0.0 - 239.255.255.255 Organization-Local Scope
				address = InetAddress.getByName("239." + c + "." + b + "." + a);
			} catch (UnknownHostException e) {
				continue;
			}

			// valid address?
			if (address.isMulticastAddress() && !addresses.contains(address)) {
				nextAddress();
				addresses.add(address);
				return address;
			}
		}

		// all addresses already used
		return null;
	}

	/**
	 * Removes the multicast address from the saved ones
	 *
	 * @param address the address to free
	 */
	public void freeAddress(InetAddress address) {
		addresses.remove(address);
	}

	/**
	 * Generates the next multicast address
	 */
	private void nextAddress() {
		a = (a + 1) % 256;
		if (a == 0) {
			b = (b + 1) % 256;
			if (b == 0)
				c = (c + 1) % 256;
		}
	}
}
