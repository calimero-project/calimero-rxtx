/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2006, 2022 B. Malinowsky

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

package io.calimero.serial.provider.rxtx;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.time.Duration;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.RXTXVersion;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import io.calimero.KNXException;
import io.calimero.serial.spi.SerialCom;

/**
 * Adapter to access a serial communication port using the rxtx (or compatible) library.
 * <p>
 * The rxtx library itself is not part of the Calimero library.<br>
 * For downloads, license, installation and usage of the rxtx library, as well as further documentation, refer to the
 * rxtx library project, https://github.com/rxtx/rxtx.
 *
 * @author B. Malinowsky
 */
final class RxtxAdapter implements SerialCom {
	private static final int OPEN_TIMEOUT = 200;

	private final Logger logger;
	private final SerialPort port;
	private final InputStream is;
	private final OutputStream os;


	static {
		// Workaround wrt initializing some versions of rxtx (+forks), so I can properly use RXTXVersion::getVersion.
		// If getVersion() is the first call into rxtx, initialization fails. This is caused by a wrong sequence in
		// the RXTXVersion static initializer block, with Version being assigned after SerialManager::getInstance.
		// Force initialization via other execution path:
		CommPortIdentifier.getPortIdentifiers();
	}

	/**
	 * Creates a new rxtx library adapter, and opens a serial port using the specified port identifier configuration
	 * settings.
	 *
	 * @param portId port identifier of the serial communication port to use
	 * @param baudrate baud rate to use for communication, 0 &lt; baud rate
	 * @param databits databits
	 * @param stopbits stopbits
	 * @param parity parity
	 * @param flowControl flow control
	 * @param readIntervalTimeout enforce an inter-character timeout after reading the first character
	 * @param receiveTimeout maximum timeout to wait for next character
	 * @throws KNXException on error opening/configuring the rxtx serial port
	 */
	RxtxAdapter(final String portId, final int baudrate, final int databits, final StopBits stopbits,
			final Parity parity, final FlowControl flowControl, final Duration readIntervalTimeout,
			final Duration receiveTimeout) throws KNXException {
		logger = System.getLogger("io.calimero.serial.provider.rxtx:" + portId);

		// rxtx does not recognize the Windows prefix for a resource name
		final String res = portId.startsWith("\\\\.\\") ? portId.substring(4) : portId;
		logger.log(INFO, "open {0} serial port connection for {1}", RXTXVersion.getVersion(), res);
		try {
			final CommPortIdentifier id = CommPortIdentifier.getPortIdentifier(res);
			if (id.getPortType() != CommPortIdentifier.PORT_SERIAL)
				throw new KNXException(res + " is not a serial port ID");

			port = id.open("Calimero", OPEN_TIMEOUT);
			port.enableReceiveThreshold(1024);
			// required to allow a close of the rxtx port, otherwise a read could lock
			try {
				port.enableReceiveTimeout((int) receiveTimeout.toMillis());
			}
			catch (final UnsupportedCommOperationException e) {
				logger.log(WARNING, "receive timeout unsupported: serial port might hang during close");
			}

			port.setSerialPortParams(baudrate, databits, stopbits.value(), parity.value());
			port.setFlowControlMode(flowControl.value());

			is = port.getInputStream();
			os = port.getOutputStream();
		}
		catch (NoSuchPortException | PortInUseException | IOException | UnsupportedCommOperationException e) {
			close();
			throw new KNXException("failed to open serial port " + res, e);
		}
	}

	@Override
	public int baudRate() {
		return port.getBaudRate();
	}

	@Override
	public InputStream inputStream() {
		return is;
	}

	@Override
	public OutputStream outputStream() {
		return os;
	}

	@Override
	public void close() {
		boolean interrupted = false;
		try {
			interrupted = Thread.interrupted();
			if (port != null)
				port.close();
		}
		catch (final RuntimeException e) {
			// RXTXPort might throw IllegalMonitorStateException
			logger.log(DEBUG, "RXTX error while closing serial port", e);
		}
		finally {
			if (interrupted)
				Thread.currentThread().interrupt();
		}
	}

	@Override
	public String toString() {
		return String.format("%s baudrate %d, parity %d, %d databits, %d stopbits, flow control %d", port.getName(),
				port.getBaudRate(), port.getParity(), port.getDataBits(), port.getStopBits(),
				port.getFlowControlMode());
	}
}
