package com.d5.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author code4crafer@gmail.com
 * @since 0.5.0
 */
public abstract class IPUtils {

    public static String getFirstNoLoopbackIPAddresses() throws SocketException {

        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        InetAddress localAddress = null;
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress address = inetAddresses.nextElement();
                if (!address.isLoopbackAddress() && !Inet6Address.class.isInstance(address)) {
                    return address.getHostAddress();
                } else if (!address.isLoopbackAddress()) {
                    localAddress = address;
                }
            }
        }

        return localAddress.getHostAddress();
    }
    
  //获取物理网卡地址
  	public static String getLocalMachineAddress() {
  		InetAddress ia;
  		try {
  			ia = InetAddress.getByName(getLocalIP());
  			byte[] mac = NetworkInterface.getByInetAddress(ia)
  					.getHardwareAddress();
  			return toMacString(mac);
  		} catch (Exception e) {
  			e.printStackTrace();
  		}
  		return null;
  	}

  	/**
  	 * 获取并输出Linux操作系统的硬件地址信息
  	 * @throws SocketException
  	 */
  	static void printHardwareAddresses4Linux() throws SocketException {
  		if (System.getProperty("os.name").equals("Linux")) {

  			List<String> devices = new ArrayList<String>();
  			Pattern pattern = Pattern.compile("^ *(.*):");
  			try (FileReader reader = new FileReader("/proc/net/dev")) {
  				BufferedReader in = new BufferedReader(reader);
  				String line = null;
  				while ((line = in.readLine()) != null) {
  					Matcher m = pattern.matcher(line);
  					if (m.find()) {
  						devices.add(m.group(1));
  					}
  				}
  			} catch (IOException e) {
  				e.printStackTrace();
  			}

  			for (String device : devices) {
  				try (FileReader reader = new FileReader("/sys/class/net/"
  						+ device + "/address")) {
  					BufferedReader in = new BufferedReader(reader);
  					String addr = in.readLine();

  					System.out.println(String.format("%5s: %s", device, addr));
  				} catch (IOException e) {
  					e.printStackTrace();
  				}
  			}

  		} else {
  		}
  	}

  	/**
  	 * 取当前系统站点本地地址 linux下 和 window下可用 add by RWW
  	 * 
  	 * @return
  	 */
  	public static String getLocalIP() {
  		String sIP = "";
  		InetAddress ip = null;
  		try {
  			//如果是Windows操作系统
  			if (isWindowsOS()) {
  				ip = InetAddress.getLocalHost();
  			}
  			//如果是Linux操作系统
  			else {
  				boolean bFindIP = false;
  				Enumeration<NetworkInterface> netInterfaces = (Enumeration<NetworkInterface>) NetworkInterface
  						.getNetworkInterfaces();
  				while (netInterfaces.hasMoreElements()) {
  					if (bFindIP) {
  						break;
  					}
  					NetworkInterface ni = (NetworkInterface) netInterfaces
  							.nextElement();
  					//----------特定情况，可以考虑用ni.getName判断
  					//遍历所有ip
  					Enumeration<InetAddress> ips = ni.getInetAddresses();
  					while (ips.hasMoreElements()) {
  						ip = (InetAddress) ips.nextElement();
  						//127.开头的都是lookback地址
  						if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
  								&& ip.getHostAddress().indexOf(":") == -1) {
  							bFindIP = true;
  							break;
  						}
  					}
  				}
  			}
  		} catch (Exception e) {
  			e.printStackTrace();
  		}
  		if (null != ip) {
  			sIP = ip.getHostAddress();
  		}
  		return sIP;
  	}

  	/**
  	 * 判断当前系统是否windows
  	 * 
  	 * @return
  	 */
  	public static boolean isWindowsOS() {
  		boolean isWindowsOS = false;
  		String osName = System.getProperty("os.name");
  		if (osName.toLowerCase().indexOf("windows") > -1) {
  			isWindowsOS = true;
  		}
  		return isWindowsOS;
  	}

  	/**
  	 * 将字节数组转换为MacString
  	 * @param bys
  	 * @return
  	 */
  	private static String toMacString(byte[] bys) {
  		if (bys == null) {
  			return null;
  		}
  		StringBuffer sb = new StringBuffer();
  		for (int i = 0; i < bys.length; i++) {
  			int temp = bys[i] & 0xff;
  			String str = Integer.toHexString(temp);
  			if (str.length() == 1) {
  				sb.append("0" + str);
  			} else {
  				sb.append(str);
  			}
  		}
  		return sb.toString();
  	}
}
