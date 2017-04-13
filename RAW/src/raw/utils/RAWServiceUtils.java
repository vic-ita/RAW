/*******************************************************************************
 *  Copyright 2017 Vincenzo-Maria Cappelleri <vincenzo.cappelleri@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package raw.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.Random;

/**
 * Collection of utilities to be used 
 *  
 * @author vic
 *
 */
public class RAWServiceUtils {
	
	/**
	 * Try to resolve public IP of this machine.
	 * 
	 * @return the public IP of this machine.
	 * @throws Exception 
	 */
	public static InetAddress resolveIP() throws Exception {
		Random rand = new Random(System.currentTimeMillis());
		switch (rand.nextInt(3)) {
		case 0:
			return resolveIPviaAWS();
		case 1:
			return resolveIPviaIChanHaz();
		case 2:
			return resolveIPviaCurlMyIP();
		default:
			return resolveIPviaAWS();
		}
	}
	
	private static InetAddress resolveIPviaAWS() throws Exception{
		String URL = "http://checkip.amazonaws.com";

		return resolveIPviaURL(URL);
	}
	
	private static InetAddress resolveIPviaIChanHaz() throws Exception{
		String URL = "http://icanhazip.com";

		return resolveIPviaURL(URL);
	}
	
	private static InetAddress resolveIPviaCurlMyIP() throws Exception{
//		String URL = "http://curlmyip.com";
		String URL = "http://curlmyip.net";

		return resolveIPviaURL(URL);
	}
	
	private static InetAddress resolveIPviaURL(String URL) throws Exception{
		URL whatismyip = new URL(URL);
		BufferedReader in = new BufferedReader(new InputStreamReader(
		                whatismyip.openStream()));

		String ip = in.readLine();
		
		InetAddress myIP = InetAddress.getByName(ip);
		
		return myIP;
	}

}
