package net.machina.ftp.network;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPFile;

public class ObservableFTPFile {
    private FTPFile ftpFile;
    private StringProperty name;
    private StringProperty type;
    private StringProperty permissions;
    private StringProperty size;
    private StringProperty uOwner;
    private StringProperty gOwner;

    public ObservableFTPFile(FTPFile file) {
        this.ftpFile = file;
        this.name = new SimpleStringProperty(file.getName());
        this.type = new SimpleStringProperty(file.isDirectory() ? "<DIR>" : "");
        this.permissions = new SimpleStringProperty(permissionToString(file, 0) + permissionToString(file, 1) + permissionToString(file, 2));
        this.size = new SimpleStringProperty(FileUtils.byteCountToDisplaySize(file.getSize()));
        this.uOwner = new SimpleStringProperty(file.getUser());
        this.gOwner = new SimpleStringProperty(file.getGroup());
    }

    private String permissionToString(FTPFile file, int access) {
        StringBuilder sb = new StringBuilder();
        if (file.hasPermission(access, 0)) {
            sb.append('r');
        } else {
            sb.append('-');
        }

        if (file.hasPermission(access, 1)) {
            sb.append('w');
        } else {
            sb.append('-');
        }

        if (file.hasPermission(access, 2)) {
            sb.append('x');
        } else {
            sb.append('-');
        }

        return sb.toString();
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }


    public String getPermissions() {
        return permissions.get();
    }

    public StringProperty permissionsProperty() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions.set(permissions);
    }

    public String getSize() {
        return size.get();
    }

    public StringProperty sizeProperty() {
        return size;
    }

    public void setSize(String size) {
        this.size.set(size);
    }

    public String getuOwner() {
        return uOwner.get();
    }

    public StringProperty uOwnerProperty() {
        return uOwner;
    }

    public void setuOwner(String uOwner) {
        this.uOwner.set(uOwner);
    }

    public String getgOwner() {
        return gOwner.get();
    }

    public StringProperty gOwnerProperty() {
        return gOwner;
    }

    public void setgOwner(String gOwner) {
        this.gOwner.set(gOwner);
    }

    public String getType() {
        return type.get();
    }

    public StringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }

    @Override
    public String toString() {
        return ftpFile.toString();
    }
}
