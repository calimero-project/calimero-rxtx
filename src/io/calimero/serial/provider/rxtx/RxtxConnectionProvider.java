/*
    Calimero 3 - A library for KNX network access
    Copyright (c) 2022, 2024 B. Malinowsky

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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;

import gnu.io.CommPortIdentifier;
import io.calimero.KNXException;
import io.calimero.serial.spi.SerialCom;
import io.calimero.serial.spi.SerialConnectionProvider;

/**
 * Serial connection provider for RXTX serial communication.
 */
public final class RxtxConnectionProvider implements SerialConnectionProvider {
	/**
	 * Creates a new RXTX connection provider instance.
	 */
	public RxtxConnectionProvider() {}

	@Override
	public SerialCom open(final Settings settings) throws KNXException {
		return new RxtxAdapter(settings.portId(), settings.baudrate(), settings.databits(), settings.stopbits(),
				settings.parity(), settings.flowControl(), settings.readIntervalTimeout(), settings.receiveTimeout());
	}

	@Override
	public Set<String> portIdentifiers() {
		@SuppressWarnings("unchecked")
		final Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
		return Collections.list(ports).stream().map(CommPortIdentifier::getName).collect(Collectors.toSet());
	}

	@Override
	public String toString() { return "RXTX serial connection provider"; }
}
