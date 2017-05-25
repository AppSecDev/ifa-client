/**
 * ©  Copyright HCL Technologies Ltd. 2017.  All Rights Reserved.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.ifa.client;

/**
 * Messages class.
 */
public class Messages {
	
	private static final MessagesBundle BUNDLE = 
			new MessagesBundle("com.ibm.appscan.ifa.client.messages", Messages.class.getClassLoader()); //$NON-NLS-1$
	
	/**
	 * Get a message.
	 * 
	 * @param key The key for the message.
	 * @param args Optional list of objects to be inserted into the message.
	 * @return The message.
	 */
	public static String getMessage(String key, Object... args) {
		return BUNDLE.getMessage(key, args);
	}
}