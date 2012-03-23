package com.bitmechanic.barrister.conform;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

public class ExitServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
        throws IOException {

        resp.getWriter().println("OK");
        Thread t = new Thread(new Runnable() {
                public void run() { 
                    try { Thread.sleep(100); } catch (Exception e) { }
                    System.exit(0); 
                }
            });
        t.start();
    }

}