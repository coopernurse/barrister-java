package com.bitmechanic.barrister.conform;

public class HttpServer {

    public interface Handler {
        public String exec(String post);
    }

    private Handler handler;

    public HttpServer(Handler h) { 
        this.handler = h;
    }

    private java.util.List<Worker> pool;

    public void serve(int port) throws Exception {
        int count = 0;
        pool = new java.util.ArrayList<Worker>();
        java.net.ServerSocket ss = new java.net.ServerSocket(port);
        while (true) {

            java.net.Socket s = ss.accept();

            Worker w = null;
            synchronized (pool) {
                if (pool.isEmpty()) {
                    Worker ws = new Worker(pool);
                    ws.setSocket(s);
                    count++;
                    Thread t = new Thread(ws, "ServiceHttpWorker-"+count);
                    t.setDaemon(true);
                    t.start();
                } else {
                    w = pool.remove(0);
                    w.setSocket(s);
                }
            }
        }
    }

    class Worker implements Runnable {
        final byte[] EOL = {(byte)'\r', (byte)'\n' };
        final String STATUS_OK = "200 OK";
        final String STATUS_ERR = "500 Internal Server Error";

        java.net.Socket s;
        java.util.List<Worker> pool;
        java.util.List<Byte> bytes;

        Worker(java.util.List<Worker> pool) {
            this.s = null;
            this.pool = pool;
            bytes = new java.util.ArrayList<Byte>(2048);
        }

        synchronized void setSocket(java.net.Socket s) {
            this.s = s;
            notify();
        }

        public synchronized void run() {
            while (true) {
                if (s == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
                try {
                    handleClient();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                s = null;
                synchronized (this.pool) {
                    if (this.pool.size() >= 5) {
                        return;
                    } else {
                        this.pool.add(this);
                    }
                }
            }
        }
        
        void handleClient() throws Exception {
            java.io.InputStream in = new java.io.BufferedInputStream(s.getInputStream());
            java.io.PrintStream out = new java.io.PrintStream(s.getOutputStream());
            s.setSoTimeout(30000);
            s.setTcpNoDelay(true);

            String status = STATUS_OK;
            String ctype = "text/plain";
            String outStr = null;
            bytes.clear();

            try {
                StringBuilder sb = new StringBuilder();
                int r = 0;
                int b = 0;
                int lastb = 0;
                boolean inbody = false;
                int contentLen = 0;
                
                while ((b = in.read()) > -1) {
                    if (inbody) {
                        bytes.add((byte)b);
                        if (bytes.size() == contentLen) {
                            break;
                        }
                    }
                    else {
                        if (b == '\n' && lastb == '\r') {
                            String header = 
                                new String(toArr(bytes, bytes.size()-1), 
                                           "utf-8");
                            if (header.trim().equals("")) {
                                inbody = true;
                            }
                            else if (header.toLowerCase().startsWith("content-length")) {
                                header = header.toLowerCase();
                                int start = header.indexOf(":");
                                contentLen = Integer.parseInt(header.substring(start+1).trim());
                            }
                            bytes.clear();
                        } else {
                            bytes.add((byte)b);
                        }
                    }
                    lastb = b;
                }
                String post = new String(toArr(bytes, bytes.size()), "utf-8");

                System.out.println("Server, got: " + post);
                outStr = handler.exec(post);

                out.print("HTTP/1.0 ");
                out.print(status);
                out.write(EOL);
                out.print("Content-Length: ");
                out.print(outStr.length());
                out.write(EOL);
                out.print("Content-Type: ");
                out.print(ctype);
                out.write(EOL);
                out.write(EOL);
                out.print(outStr);
                out.flush();
                s.close();

                System.out.println("Server, sent: " + outStr);
            } finally {
                in.close();
                out.close();
                s.close();
            }
        }

        byte[] toArr(java.util.List<Byte> list, int size) {
            byte[] arr = new byte[size];
            for (int i = 0; i < size; i++) {
                arr[i] = list.get(i);
            }
            return arr;
        }

    }
}
