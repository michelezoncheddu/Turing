package turing.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.TreeSet;

public class AddressManager {
	private static TreeSet<InetAddress> addresses = new TreeSet<>(new InetAddressComparator());
	private static int c = 0, b = 0, a = 0;

	private AddressManager() {}

	public static synchronized InetAddress createAddress() {
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

	public static void freeAddress(InetAddress address) {
		addresses.remove(address);
	}

	private static void nextAddress() {
		a = (a + 1) % 256;
		if (a == 0) {
			b = (b + 1) % 256;
			if (b == 0)
				c = (c + 1) % 256;
		}
	}
}
