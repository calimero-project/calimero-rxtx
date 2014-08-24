/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2006, 2014 B. Malinowsky

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    Linking this library statically or dynamically with other modules is
    making a combined work based on this library. Thus, the terms and
    conditions of the GNU General Public License cover the whole
    combination.

    As a special exception, the copyright holders of this library give you
    permission to link this library with independent modules to produce an
    executable, regardless of the license terms of these independent
    modules, and to copy and distribute the resulting executable under terms
    of your choice, provided that you also meet, for each linked independent
    module, the terms and conditions of the license of that module. An
    independent module is a module which is not derived from or based on
    this library. If you modify this library, you may extend this exception
    to your version of the library, but you are not obligated to do so. If
    you do not wish to do so, delete this exception statement from your
    version.
*/

package tuwien.auto.calimero.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;

import tuwien.auto.calimero.exception.KNXException;

/**
 * Adapter to access a serial communication port using the rxtx library.
 * <p>
 * This file is not included in the Calimero library by default, but an adapter which can be used in
 * case the rxtx library is available on the host platform.<br>
 * The rxtx library itself is not part of the Calimero library.<br>
 * For downloads, license, installation and usage of the rxtx library, as well as further
 * documentation, refer to the rxtx library project, https://github.com/rxtx/rxtx.
 *
 * @author B. Malinowsky
 */
public class RxtxAdapter extends LibraryAdapter
{
	private static final int OPEN_TIMEOUT = 200;

	private SerialPort port;
	private InputStream is;
	private OutputStream os;

	/**
	 * Creates a new rxtx library adapter, and opens a serial port using a port identifier
	 * and baud rate.
	 * <p>
	 *
	 * @param logger the logger to use for this adapter
	 * @param portId port identifier of the serial communication port to use
	 * @param baudrate baud rate to use for communication, 0 &lt; baud rate
	 * @throws KNXException
	 */
	public RxtxAdapter(final Logger logger, final String portId, final int baudrate)
		throws KNXException
	{
		super(logger);
		open(portId, baudrate);
	}

	public void setBaudRate(final int baudrate)
	{
		try {
			port.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_EVEN);
		}
		catch (final UnsupportedCommOperationException e) {
			logger.error("failed to configure port settings");
		}
	}

	public int getBaudRate()
	{
		return port.getBaudRate();
	}

	public InputStream getInputStream()
	{
		return is;
	}

	public OutputStream getOutputStream()
	{
		return os;
	}

	public void close()
	{
		port.close();
	}

	private void open(String portId, final int baudrate) throws KNXException
	{
		logger.info("using rxtx library for serial port access");
		try {
			// rxtx does not recognize the Windows prefix for a resource name
			if (portId.startsWith("\\\\.\\"))
				portId = portId.substring(4);
			final CommPortIdentifier id = CommPortIdentifier.getPortIdentifier(portId);
			if (id.getPortType() != CommPortIdentifier.PORT_SERIAL)
				throw new KNXException(portId + " is not a serial port ID");
			port = (SerialPort) id.open("Calimero FT1.2", OPEN_TIMEOUT);
			port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			port.enableReceiveThreshold(1024);
			// required to allow a close of the rxtx port, otherwise a read could lock
			try {
				port.enableReceiveTimeout(250);
			}
			catch (final UnsupportedCommOperationException e) {
				logger.warn("no timeout support: serial port might hang during close");
			}
			setBaudRate(baudrate);
			logger.info("setup serial port: baudrate " + port.getBaudRate()
					+ ", parity even, databits " + port.getDataBits() + ", stopbits "
					+ port.getStopBits() + ", flow control " + port.getFlowControlMode());
			is = port.getInputStream();
			os = port.getOutputStream();
			/*
			// Allows tracing of events, for informational purposes only
			try {
				port.notifyOnBreakInterrupt(true);
				port.notifyOnCarrierDetect(true);
				port.notifyOnCTS(true);
				port.notifyOnDataAvailable(true);
				port.notifyOnDSR(true);
				port.notifyOnFramingError(true);
				port.notifyOnOutputEmpty(true);
				port.notifyOnOverrunError(true);
				port.notifyOnParityError(true);
				port.notifyOnRingIndicator(true);
				port.addEventListener(new SerialPortEventListener() {
					public void serialEvent(SerialPortEvent ev) {
						int type = ev.getEventType();
						if (type == SerialPortEvent.BI)
							System.out.println("SerialPortEvent.BI");
						if (type == SerialPortEvent.CD)
							System.out.println("SerialPortEvent.CD");
						if (type == SerialPortEvent.CTS)
							System.out.println("SerialPortEvent.CTS");
						if (type == SerialPortEvent.DATA_AVAILABLE)
							System.out.println("SerialPortEvent.DATA_AVAILABLE");
						if (type == SerialPortEvent.DSR)
							System.out.println("SerialPortEvent.DSR");
						if (type == SerialPortEvent.FE)
							System.out.println("SerialPortEvent.FE");
						if (type == SerialPortEvent.OE)
							System.out.println("SerialPortEvent.OE");
						if (type == SerialPortEvent.OUTPUT_BUFFER_EMPTY)
							System.out.println("SerialPortEvent.OUTPUT_BUFFER_EMPTY");
						if (type == SerialPortEvent.PE)
							System.out.println("SerialPortEvent.PE");
						if (type == SerialPortEvent.RI)
							System.out.println("SerialPortEvent.RI");
					}
				});
			} catch (TooManyListenersException e) {	}
			*/
			return;
		}
		// we can't let those exceptions bubble up, so log cause and throw our own
		catch (final NoSuchPortException e) {
			logger.error("serial port " + portId + " not found");
		}
		catch (final PortInUseException e) {
			logger.error("serial port " + portId + " in use, " + e.getMessage());
		}
		catch (final IOException e) {
			logger.error("failed to open port streams, " + e.getMessage());
		}
		catch (final UnsupportedCommOperationException e) {
			logger.error("failed to configure port settings");
		}
		port.close();
		try {
			if (is != null)
				is.close();
			if (os != null)
				os.close();
		}
		catch (final Exception ignore) {}
		throw new KNXException("failed to open serial port " + portId);
	}
}
