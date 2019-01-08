/*
 * Copyright (c) 2018, Andreas P. Koenzen <akc at apkc.net>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.apkc.patternsproject.reactor;

import net.apkc.patternsproject.utils.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * This class represents a custom NIO Web Server which is implemented using the
 * Reactor Pattern.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.1
 * @see <a href="http://en.wikipedia.org/wiki/Reactor_pattern">Reactor Pattern</a>
 */
class ReactorServer {
    final static int BUFFER_SIZE = 1024;
    final static boolean DEBUG = true;
    final static Log LOG = new Log();
    Selector selector = null;
    ServerSocketChannel server = null;
    boolean isConfigured = false;

    static ReactorServer newBuild() {
        return new ReactorServer();
    }

    ReactorServer configure(int port) throws IOException {
        this.selector = Selector.open();
        this.server = ServerSocketChannel.open();
        this.server.configureBlocking(false);
        this.server.socket().bind(new InetSocketAddress("0.0.0.0", port));
        this.isConfigured = true;

        LOG.log("Server configured & started.");

        return this;
    }

    void startReactor() throws IOException {
        if (!this.isConfigured) {
            LOG.log("The server wasn't configured!");
        }

        SelectionKey acceptKey = this.server.register(this.selector, SelectionKey.OP_ACCEPT);
        while (acceptKey.selector().select() > 0) { // Here the selector will block for new incoming connections.
            Set readyKeys = this.selector.selectedKeys();
            Iterator it = readyKeys.iterator();
            while (it.hasNext()) {
                SelectionKey key = (SelectionKey) it.next();
                it.remove();

                SocketChannel socket;

                if (key.isAcceptable()) {
                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                    socket = ssc.accept();
                    socket.configureBlocking(false);
                    socket.register(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                }

                if (key.isReadable()) {
                    int read = 0;

                    try {
                        socket = (SocketChannel) key.channel();

                        // first read the data size
                        ByteBuffer dataSizeBuff = ByteBuffer.allocate(4);
                        if (socket.read(dataSizeBuff) != -1) {
                            dataSizeBuff.flip();
                            if (DEBUG) {
                                LOG.log("Data size: " + dataSizeBuff.getInt());
                            }
                            dataSizeBuff.clear();
                        }

                        // now read the data
                        String data = "";
                        ByteBuffer dataBuff = ByteBuffer.allocate(BUFFER_SIZE);
                        if (socket.read(dataBuff) != -1) {
                            dataBuff.flip();
                            data = Charset.forName("UTF-8").decode(dataBuff).toString();
                            if (DEBUG) {
                                LOG.log("Data: " + data);
                            }
                            dataBuff.clear();
                        }

                        // kill the server by closing the selector
                        if (data.equalsIgnoreCase("kill")) {
                            LOG.log("Kill signal received, will exit.");
                            socket.close();
                        }
                    } catch (IOException e) {
                        LOG.log("ERROR: " + e.getMessage());
                        System.exit(1);
                    }
                }

                if (key.isWritable()) {
                    // Do nothing for the moment.
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            ReactorServer.newBuild().configure(9090).startReactor();
        } catch (Exception e) {
            LOG.log("ERROR: " + e);
        }
    }
}
