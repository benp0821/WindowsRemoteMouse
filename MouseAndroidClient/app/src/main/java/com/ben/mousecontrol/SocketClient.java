package com.ben.mousecontrol;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import android.util.Base64;

public class SocketClient implements Runnable{

    private Socket s;
    private String hostname;
    private int port;
    private Activity context;
    private Timer timer;
    private TimerTask pingTask;
    private boolean ping = false;
    private static Toast m_currentToast;
    private static Queue<String> commands = new LinkedList<>();


    SocketClient(Activity context, String host, int port) {
        hostname = host;
        this.port = port;
        this.context = context;
    }

    //TODO: auto-scan of network (pop-up with advanced option for manual entry of ip)
    @Override
    public void run() {
        while (true) {

            timer = new Timer();
            pingTask = new TimerTask() {
                @Override
                public void run() {
                    ping = true;
                }
            };
            timer.schedule(pingTask, 100, 5);

            try {
                s = new Socket();
                s.connect(new InetSocketAddress(hostname, port), 5000);
                InputStream inputStream = s.getInputStream();
                OutputStream outputStream = s.getOutputStream();

                showToast("Connected to " + hostname);

                int inputChar = 0;
                boolean waitForInput = false;
                boolean isPing = false;
                while (s.isConnected() && !s.isClosed() && inputChar != -1) {
                    if (!commands.isEmpty()) {
                        outputStream.write(commands.poll().getBytes(StandardCharsets.UTF_8));
                        waitForInput = true;
                    }else if (ping){
                        outputStream.write("ping".getBytes(StandardCharsets.UTF_8));
                        waitForInput = true;
                        isPing = true;
                        ping = false;
                    }

                    if (waitForInput) {
                        StringBuilder inputString = new StringBuilder("");
                        while ((char) (inputChar = inputStream.read()) != ';' && inputChar != -1) {
                            inputString.append((char) inputChar);
                        }
                        if (inputChar != -1) {
                            if (isPing){
                                try {
                                    byte[] data = Base64.decode(inputString.toString().substring(2), Base64.DEFAULT);

                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    LinearLayout layout = context.findViewById(R.id.layout);
                                    BitmapDrawable bmpDraw = new BitmapDrawable(context.getResources(), bmp);
                                    context.runOnUiThread(() -> {
                                        try {
                                            layout.setBackground(bmpDraw);
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }catch(IllegalArgumentException iae){
                                    iae.printStackTrace();
                                }
                                isPing = false;
                            }
                            else
                            {
                                System.out.println(inputString);
                            }
                        }
                        waitForInput = false;
                    }
                }
            } catch (SocketTimeoutException e) {
                showToast("Failed to Connect to " + hostname);
            } catch (Exception e){
                e.printStackTrace();
            }

            endNetworkingTasks();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    @SuppressLint("ShowToast")
    private void showToast(String text){
        context.runOnUiThread(() -> {
            if(m_currentToast == null)
            {
                m_currentToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            }

            m_currentToast.setText(text);
            m_currentToast.setDuration(Toast.LENGTH_SHORT);
            m_currentToast.show();
        });
    }

    static void addCommand(String command){
        commands.add(command);
        System.out.println("added command " + command);
    }

    void endNetworkingTasks(){
        pingTask.cancel();
        timer.cancel();
        commands.clear();
        ping = false;

        try {
            if (s.isConnected()) {
                showToast("Connection Lost");
            }
            s.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
