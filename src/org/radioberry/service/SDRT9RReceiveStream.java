package org.radioberry.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.g0orx.openhpsdr.wdsp.WDSP;
import org.radioberry.utility.Channel;
import org.radioberry.utility.Display;

public class SDRT9RReceiveStream implements Runnable {

	private Configuration configuration;

	private volatile Thread thread;

	private byte rxbuffer[] = new byte[1026];
	private DatagramSocket socket;
	private DatagramPacket rxdatagram;

	private float[] outlsamples;
	private float[] outrsamples;
	private float[] inlsamples;
	private float[] inrsamples;
	private int isample;
	private int qsample;

	private int inoffset = 0;

	private WDSP wdsp;
	private int[] error = new int[1];

	// Local audio output
	private byte[] audiooutput = new byte[1024 * 4]; // 2 channels of shorts
	private int audiooutputindex = 0;

	public SDRT9RReceiveStream() {
		configuration = Configuration.getInstance();

		inlsamples = new float[configuration.buffersize];
		inrsamples = new float[configuration.buffersize];

		outlsamples = new float[configuration.buffersize];
		outrsamples = new float[configuration.buffersize];

		this.wdsp = WDSP.getInstance();
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
	}

	public void stop() {
		thread = null;
	}

	@Override
	public void run() {
		try {
			InetAddress address = InetAddress
					.getByName(Configuration.IP_SDR_PROGRAM);
			InetSocketAddress socketaddress = new InetSocketAddress(address,
					Configuration.RX_IQ_PORT);

			socket = new DatagramSocket(socketaddress);
			socket.setReuseAddress(false);
			socket.setSoTimeout(1000);
			rxdatagram = new DatagramPacket(rxbuffer, rxbuffer.length, address,
					Configuration.RX_IQ_PORT);

			// required to get the 6th sample from the subsequent buffers
			int index = 0;

			while (thread == Thread.currentThread()) {

				socket.receive(rxdatagram);
				if (rxdatagram.getLength() == 1026) {

					// IQ processing.
					for (int i = 0; i < 1026; i += 6) {

						qsample = rxbuffer[i + 2] << 16;
						qsample |= (rxbuffer[i + 1] & 0xFF) << 8;
						qsample |= (rxbuffer[i] & 0xFF << 0);
						isample = rxbuffer[i + 5] << 16;
						isample |= (rxbuffer[i + 4] & 0xFF) << 8;
						isample |= (rxbuffer[i + 3] & 0xFF << 0);

						inlsamples[inoffset] = (float) isample / 8388607.0F; // 24
																				// bit
																				// sample
																				// convert
																				// to
																				// -1..+1
						inrsamples[inoffset] = (float) qsample / 8388607.0F; // 24
																				// bit
																				// sample
																				// convert
																				// to
																				// -1..+1

						inoffset++;

						if (inoffset == configuration.buffersize) {

							// DSP demodulation
							wdsp.fexchange2(Channel.RX, inlsamples, inrsamples,
									outlsamples, outrsamples, error);

							// Put only the right sample in the buffer (maybe
							// later the other channel as well)
							List<Double> audioBuffer = new ArrayList<Double>();
							// place each 6th sample in the buffer (starting
							// with the first)
							for (int j = 0; j < configuration.buffersize; j++) {
								if (index % 6 == 0) {
									audioBuffer
											.add((double) outlsamples[j] / 32767.0F);
								}
								index++;
							}
							index = index % 6;

							// Fill the stream with the samples
							RadioClients.getInstance().fillAudioStream(
									audioBuffer);

							wdsp.Spectrum(Display.RX, 0, 0, inrsamples,
									inlsamples);

							inoffset = 0;

							for (int j = 0; j < outlsamples.length; j++) {
								
								short lsample;
                                short rsample;
                                lsample = (short) (outlsamples[j] * 32767.0F * configuration.afgain);
                                rsample = (short) (outrsamples[j] * 32767.0F * configuration.afgain);
								
								audiooutput[audiooutputindex++] = (byte) ((lsample >> 8) & 0xFF);
								audiooutput[audiooutputindex++] = (byte) (lsample & 0xFF);
								audiooutput[audiooutputindex++] = (byte) ((rsample >> 8) & 0xFF);
								audiooutput[audiooutputindex++] = (byte) (rsample & 0xFF);
								if (audiooutputindex == audiooutput.length) {
									LocalAudio.getInstance().writeAudio(audiooutput);
									audiooutputindex = 0;
								}
							}

						}
					}
				}

			}
		} catch (SocketException se) {
			System.out.println("Socket stream setup error " + se.getMessage());
		} catch (UnknownHostException uhe) {
		} catch (IOException ioe) {
		}
	}
}