/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.csenk.gwtws.server;

import java.io.IOException;

import org.eclipse.jetty.websocket.WebSocket;

import de.csenk.gwtws.shared.IoConnection;
import de.csenk.gwtws.shared.IoFilterChain;
import de.csenk.gwtws.shared.IoHandler;
import de.csenk.gwtws.shared.IoService;

/**
 * @author senk.christian@googlemail.com
 * @date 25.08.2010
 * @time 13:56:30
 *
 */
public class JettyWebSocket implements WebSocket, IoService {

	private final IoHandler handler;
	
	private Outbound outbound;
	private IoConnection outboundConnection;
	
	/**
	 * @param handler
	 */
	public JettyWebSocket(final IoHandler handler) {
		this.handler = handler;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jetty.websocket.WebSocket#onConnect(org.eclipse.jetty.websocket.WebSocket.Outbound)
	 */
	@Override
	public void onConnect(Outbound outbound) {
		this.outbound = outbound;
		outboundConnection = createOutboundConnection(outbound);
		
		try {
			handler.onConnectionOpened(outboundConnection);
		} catch (Throwable e) {
			handler.onExceptionCaught(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jetty.websocket.WebSocket#onDisconnect()
	 */
	@Override
	public void onDisconnect() {
		try {
			handler.onConnectionClosed(outboundConnection);
		} catch (Throwable e) {
			handler.onExceptionCaught(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jetty.websocket.WebSocket#onMessage(byte, java.lang.String)
	 */
	@Override
	public void onMessage(byte arg0, String message) {
		receiveMessage(message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jetty.websocket.WebSocket#onMessage(byte, byte[], int, int)
	 */
	@Override
	public void onMessage(byte arg0, byte[] messageBytes, int messageOffset, int messageLength) {
		receiveMessage(new String(messageBytes, messageOffset, messageLength));
	}

	/* (non-Javadoc)
	 * @see de.csenk.websocket.shared.IoService#getFilterChain()
	 */
	@Override
	public IoFilterChain getFilterChain() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.csenk.websocket.shared.IoService#getHandler()
	 */
	@Override
	public IoHandler getHandler() {
		return handler;
	}

	/**
	 * @param message
	 */
	private void sendMessage(Object message) {
		//TODO Filter message
		
		Object filteredMessage = message;
		assert filteredMessage instanceof String;
		
		try {
			outbound.sendMessage(WebSocket.SENTINEL_FRAME, (String) filteredMessage);
		} catch (IOException e) {
			handler.onExceptionCaught(e);
		}
		
		try {
			handler.onMessageSent(outboundConnection, message);
		} catch (Throwable e) {
			handler.onExceptionCaught(e);
		}
	}
	
	/**
	 * @param message
	 */
	private void receiveMessage(String message) {
		//TODO Filter message
		
		Object filteredMessage = message;
		
		try {
			handler.onMessageReceived(outboundConnection, filteredMessage);
		} catch (Throwable e) {
			handler.onExceptionCaught(e);
		}
	}
	
	/**
	 * @param outbound
	 * @return
	 */
	private IoConnection createOutboundConnection(final Outbound outbound) {
		return new IoConnection() {

			@Override
			public void close() {
				outbound.disconnect();
			}

			@Override
			public void sendMessage(Object message) {
				JettyWebSocket.this.sendMessage(message);
			}
			
		};
	}
	
}