package com.funai.drawpen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.util.Log;

public class WifiReceive {
	private ServerSocket sSocket = null;
	private Socket socket = null;
	
	private BufferedReader buffer = null;

	private int port = 0;
	private WifiEventHandler handler;
	private final int timeout = 5000;

	private boolean enable = false;
	private boolean connect = false;

	public WifiReceive(WifiEventHandler handle)
	{
		handler = handle;
	}

	public WifiReceive(WifiEventHandler handle, int portNum)
	{
		handler = handle;
		setPort(portNum);
	}
	
	public void setPort(int portNum)
	{
		port = portNum;
	}

	public void openConnect(){
		enable = true;
		connect = true;
		(new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO 自動生成されたメソッド・スタブ
				while(connect) {
					try {
						sSocket = new ServerSocket(port);
						sSocket.setSoTimeout(timeout);
						socket = sSocket.accept();
						socket.setSoTimeout(timeout);
						buffer = null;
						Log.d("Wifi","Connected IP : "+socket.getRemoteSocketAddress()+" , Port : "+sSocket.getLocalPort());

						enable = true;
						while(enable) {
							try {
								if (buffer == null) {
									buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
								}
								String st = null;
								st = buffer.readLine();
								Log.d("Wifi","Receive : "+st);
								
								// データ抜き取り
								if (st != null) {
									int stPos = 0;
									int edPos = 0;
									int x=0,y=0,z=0;
									boolean push=false;
									boolean chk = true;
									String chkStr = st.substring(stPos);
									if (chk == true && chkStr != null)
									{
										edPos = chkStr.indexOf(',');
										x = Integer.valueOf(chkStr.substring(0, edPos));
										stPos = edPos+1;
										chkStr = chkStr.substring(stPos);
									} else {
										chk = false;
									}
			
									if (chk == true && chkStr != null)
									{
										edPos = chkStr.indexOf(',');
										y = Integer.valueOf(chkStr.substring(0, edPos));
										stPos = edPos+1;
										chkStr = chkStr.substring(stPos);
									} else {
										chk = false;
									}
			
									if (chk == true && chkStr != null)
									{
										edPos = chkStr.indexOf(',');
										z = Integer.valueOf(chkStr.substring(0, edPos));
										stPos = edPos+1;
										chkStr = chkStr.substring(stPos);
									} else {
										chk = false;
									}
			
									if (chk == true && chkStr != null)
									{
										push = Boolean.valueOf(chkStr);
									} else {
										chk = false;
									}
								
									if (chk) handler.receiveData(x, y, z, push);
								} else {
									enable = false;
									if (buffer != null) {
										buffer.close();
										buffer = null;
									}
									if (socket != null) {
										socket.close();
										socket = null;
									}
									if (sSocket != null) {
										sSocket.close();
										sSocket = null;
									}
								}
							} catch (SocketTimeoutException e) {
/*								if (buffer != null) {
									buffer.close();
									buffer = null;
								}*/
							} catch (IOException e) {
								if (buffer != null) {
									buffer.close();
									buffer = null;
								}
							}
						}
					} catch (SocketTimeoutException e) {
						// TimeOut
						Log.d("Wifi","Timeout");
						try {
							if (buffer != null) {
								buffer.close();
								buffer = null;
							}
							if (socket != null) {
								socket.close();
								socket = null;
							}
							if (sSocket != null) {
								sSocket.close();
								sSocket = null;
							}
						} catch (IOException e1) {
							// TODO 自動生成された catch ブロック
							e1.printStackTrace();
							Log.d("Wifi","IOException end");
						}
					} catch (IOException e) {
						// TODO 自動生成された catch ブロック
						try {
							if (buffer != null) {
								buffer.close();
								buffer = null;
							}
							if (socket != null) {
								socket.close();
								socket = null;
							}
							if (sSocket != null) {
								sSocket.close();
								sSocket = null;
							}
						} catch (IOException e1) {
							// TODO 自動生成された catch ブロック
							e1.printStackTrace();
							Log.d("Wifi","IOException end");
						}
						e.printStackTrace();
						Log.d("Wifi","IOException");
					}
				}
				try
				{
					if (buffer != null) {
						buffer.close();
						buffer = null;
					}
					if (socket != null) {
						socket.close();
						socket = null;
					}
					if (sSocket != null) {
						sSocket.close();
						sSocket = null;
					}
				} catch (IOException e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
					Log.d("Wifi","IOException end");
				}
			}
		})).start();
	}

	public void closeConnection() {
		Log.d("Wifi","Close");
		enable = false;
		connect = false;
	}
}
