/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed.master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class ServerRunnable implements Runnable {

  private final Master master;

  private final static Logger L = Logger.getLogger(ServerRunnable.class.getName());

  public ServerRunnable(Master master) {
    this.master = master;
  }

  @Override
  public void run() {
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(master.getPort());
      L.fine(String.format("Listening on port %d.", serverSocket.getLocalPort()));
    } catch (IOException ex) {
      L.log(Level.SEVERE, String.format("Cannot start server socket on port %d.", master.getPort()), ex);
      System.exit(-1);
    }
    while (true) {
      try {
        Socket socket = serverSocket.accept();
        L.finer(String.format("Connection from %s:%d.", socket.getInetAddress(), socket.getPort()));
        master.startClientRunnable(socket);
      } catch (IOException ex) {
        L.log(Level.WARNING, String.format("Cannot accept socket: %s", ex.getMessage()), ex);
      }
    }
  }

}
