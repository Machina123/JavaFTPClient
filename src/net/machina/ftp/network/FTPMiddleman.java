package net.machina.ftp.network;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;

public class FTPMiddleman {
    private static FTPMiddleman instance;
    private FTPClient client;
    private boolean connected = false;

    public static synchronized FTPMiddleman getInstance() {
        if(instance == null) instance = new FTPMiddleman();
        return instance;
    }

    private FTPMiddleman() {
        client = new FTPClient();
        client.setAutodetectUTF8(true);
    }

    public boolean connect(String address, int port, String username, String password) {
        try {
            boolean loggedIn = false;
            client.connect(address, port);
            if(client.isConnected()) {
                System.err.println(client.getReplyString());
                loggedIn = client.login(username, password);
                System.err.println(client.getReplyString());
                client.setControlKeepAliveTimeout(60);
                client.setDataTimeout(60000);
                client.enterLocalPassiveMode();
            }
            connected = loggedIn;
            return loggedIn;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void disconnect() {
        try {
            client.logout();
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean toParent() {
        try {
            return client.changeToParentDirectory();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean goToDirectory(String dir) {
        try {
            return client.changeWorkingDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendFile(String localPath, String remotePath) {
        try {
            File localFile = new File(localPath);
            if(!localFile.exists()) return false;
            InputStream stream = new FileInputStream(localFile);
            boolean res = client.storeFile(remotePath, stream);
            stream.close();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean downloadFile(String remotePath, String localPath) {
        try {
            File localFile = new File(localPath);
            if(!localFile.exists()) localFile.createNewFile();
            OutputStream stream = new FileOutputStream(localFile);
            boolean res = client.retrieveFile(remotePath, stream);
            stream.close();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean rename(String before, String after) {
        try {
            return client.rename(before, after);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeFile(String thing) {
        try {
            return client.deleteFile(thing);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeDirectory(String dir) {
        try {
            return client.removeDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public FTPFile[] listFiles() {
        try {
            return client.listFiles();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public FTPFile[] listDirectories() {
        try {
            return client.listDirectories();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getCwd() {
        try {
            return client.printWorkingDirectory();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean makeDirectory(String name) {
        try {
            return client.makeDirectory(name);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

}
