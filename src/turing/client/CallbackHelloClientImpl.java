package turing.client;

import turing.CallbackHelloClientInterface;

import java.rmi.RemoteException;

public class CallbackHelloClientImpl implements CallbackHelloClientInterface {

	public void notifyMe(String message) throws RemoteException {
		String returnMessage = "Call back received: " + message;
		System.out.println(returnMessage);
	}
}
