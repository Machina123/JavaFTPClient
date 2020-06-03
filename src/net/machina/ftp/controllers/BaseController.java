package net.machina.ftp.controllers;

import javafx.fxml.Initializable;

public abstract class BaseController implements Initializable {
    private BaseController context;

    public void setContext(BaseController context) {
        this.context = context;
    }
}
