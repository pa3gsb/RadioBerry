package org.radioberry.openhpsdr;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.radioberry.discovery.RequestInfo;
import org.radioberry.discovery.ResponseInfo;


public class Protocol implements Runnable{

	private volatile Thread thread;
	
	InetAddress remoteAddress;
	int remotePort;
	
	InetAddress localAddress;
	int localPort;
	
	private byte buffer[] = new byte[1032];
	private DatagramPacket rxdatagram;
	private DatagramSocket socket;
	
	
	public Protocol(ResponseInfo respInfo){
		RequestInfo reqInfo = respInfo.getRequestInfo();
		this.remoteAddress = reqInfo.getRemoteAddress();
		this.remotePort = reqInfo.getRemotePort();
		this.localAddress = respInfo.getLocalAddress();
		this.localPort = respInfo.getLocalPort();
	}
	
	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}
	}

	public void stop() {
		if (socket != null) {
			socket.close();
			socket = null;
		}
		thread = null;
	}
	
	
	@Override
	public void run() {
		
		
		 DatagramSocket socket = null;
		 DatagramPacket datagram = null;
		
		try {
			InetAddress address = this.remoteAddress;
			InetAddress socketaddress = this.localAddress;
			InetSocketAddress socketaddr = new InetSocketAddress(socketaddress, this.localPort);
			datagram = new DatagramPacket(buffer, buffer.length, address, this.remotePort);
			socket = new DatagramSocket(socketaddr);
			socket.setReuseAddress(false);
//			socket.send(datagram);
		} catch (SocketException se) {
		} catch (IOException ioe) {
		}
		
		
		while (thread == Thread.currentThread()) {
			

			
			
		}
		
	}
	
	
/*    byte[] datarcv = new byte[1032];
    private void receive_Data_From_SDR_Program()
    {
       
       bool data_available = socket.Poll(10, SelectMode.SelectRead);
       
        if (data_available)
        {

            int recv = socket.Receive(datarcv);

            if (datarcv[0] == 0xEF && datarcv[1] == 0xFE && datarcv[2] == 0x04)
            {
                if (datarcv[3] == 0x01 || datarcv[3] == 0x03)
                {
                    _start = true;
                    
                    Console.WriteLine(" SDR Program sends Start command " );
                }
                if (datarcv[3] == 0x00)
                { 
                    _start = false;
                    last_sequence_number = 0;

                    Console.WriteLine("  SDR Program sends Stop command ");
                }
                rx_IQ_buffer.Clear();
                tx_Audio_buffer.Clear();
                return;
            }

            if (isValidFrame(datarcv))
            {
                // packet contains 2 hpsdr frames; so 2 c&c's
                byte[] cc = new byte[5];
                Array.Copy(datarcv, 11, cc, 0, 5);  //index 11 .... 15
                CommandAndControl(cc);
                Array.Copy(datarcv, 523, cc, 0, 5);  //index 523 .... 531   (8 + 512 + 3 sync)
                CommandAndControl(cc);

                //lees data en vul buffers
                for (int frame = 0; frame < 2; frame++)
                {
                    int coarse_pointer = frame * 512 + 8;

                    for (int j = 8; j < 512; j += 8)
                    {
                        int k = coarse_pointer + j;

                        // M
                        rx_Audio_buffer.Write(datarcv[k + 0]);
                        rx_Audio_buffer.Write(datarcv[k + 1]);
                        rx_Audio_buffer.Write(datarcv[k + 2]);
                        rx_Audio_buffer.Write(datarcv[k + 3]);
            
                        // TX IQ
                        tx_IQ_buffer.Write(datarcv[k + 4]);
                        tx_IQ_buffer.Write(datarcv[k + 5]);
                        tx_IQ_buffer.Write(datarcv[k + 6]);
                        tx_IQ_buffer.Write(datarcv[k + 7]);
                    }
                }

                ProgressStatus(new ProgressEventArgs("2"));
            }
            else Console.WriteLine("Received Frame From SDR Program not valid ");
        }
    }*/
	
	
	

}
