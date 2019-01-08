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
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class ReactorServerTest {
    final static Log LOG = new Log();
    Socket client;

    void makeConnection(String text) {
        try {
            this.client = new Socket();
            this.client.connect(new InetSocketAddress("127.0.0.1", 9090));
        } catch (UnknownHostException e) {
            LOG.log("Error: " + e.toString());
        } catch (IOException e) {
            LOG.log("Error: " + e.toString());
        }

        try {
            OutputStream out = this.client.getOutputStream();
            String data = "";
            if (text.equalsIgnoreCase("")) {
                data = " " + ProcessHandle.current().pid() + " ";
            } else {
                data = text;
            }

            out.write(ByteBuffer.allocate(4).putInt(data.length()).array());
            out.write(data.getBytes(Charset.forName("UTF-8")));
            out.flush();
        } catch (IOException e) {
            LOG.log("Error: " + e.toString());
        }

        try {
            this.client.close();
        } catch (IOException e) {
            LOG.log("Error: " + e.toString());
        }
    }

    public static void main(String args[]) {
        ReactorServerTest t = new ReactorServerTest();

        for (int k = 0; k < 1000; k++) {
            t.makeConnection("");
        }

        // kill server
        t.makeConnection("kill");

        System.exit(0);
    }
}
