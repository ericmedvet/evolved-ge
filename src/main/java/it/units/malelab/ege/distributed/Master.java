/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class Master implements Runnable {

  private final static int MAX_CLIENTS = 32;

  private final String keyPhrase;
  private final int port;

  private final ExecutorService executor;
  private final Random random;

  private final static Logger L = Logger.getLogger(Master.class.getName());

  public Master(String keyPhrase, int port) {
    this.keyPhrase = keyPhrase;
    this.port = port;
    this.executor = Executors.newFixedThreadPool(MAX_CLIENTS);
    random = new Random();
  }

  public static void main(String[] args) throws IOException {
    LogManager.getLogManager().readConfiguration(Master.class.getClassLoader().getResourceAsStream("logging.properties"));
    Master master = new Master("hi", 9000);
    master.run();
  }

  @Override
  public void run() {
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(port);
      L.fine(String.format("Listening on port %d.", serverSocket.getLocalPort()));
    } catch (IOException ex) {
      L.log(Level.SEVERE, String.format("Cannot start server socket on port %d.", port), ex);
      System.exit(-1);
    }
    while (true) {
      try (Socket socket = serverSocket.accept();) {
        L.fine(String.format("Connection from %s:%d.", socket.getInetAddress(), socket.getPort()));
        executor.submit(getServerRunnable(socket.getInputStream(), socket.getOutputStream()));
      } catch (IOException ex) {
        L.log(Level.WARNING, String.format("Cannot accept socket: %s", ex.getMessage()), ex);
      }
    }
  }

  private Runnable getServerRunnable(final InputStream in, final OutputStream out) {
    return new Runnable() {
      @Override
      public void run() {
        try (ObjectInputStream ois = new ObjectInputStream(in);
                ObjectOutputStream oos = new ObjectOutputStream(out);) {
          //handshake
          String randomData = Double.toHexString(random.nextDouble());
          oos.writeObject(DistributedUtils.encrypt(randomData, keyPhrase));
          String reversed = DistributedUtils.decrypt((byte[]) ois.readObject(), keyPhrase);
          if (!DistributedUtils.reverse(reversed).equals(randomData)) {
            throw new SecurityException("Client did not correctly replied to the challenge!");
          }
          L.fine(String.format("Handshake correctly completed with \"%s\".", randomData));
          //message
          //TODO
        } catch (IOException ex) {
          L.log(Level.WARNING, String.format("Cannot build Object streams: %s", ex.getMessage()), ex);
        } catch (ClassNotFoundException ex) {
          L.log(Level.WARNING, String.format("Cannot decode response: %s", ex.getMessage()), ex);
        }
      }
    };
  }

}
