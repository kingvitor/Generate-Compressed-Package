package com.kingdee.action;

import javax.swing.*;
import java.awt.*;

public class Client {
    public static void main(String[] args) {
        start(new PatcherDialog());
    }

    public static void start(PatcherDialog dialog){
        try {
            dialog.setSize(500, 400);
            dialog.setLocationRelativeTo((Component)null);
            dialog.setVisible(true);
            dialog.requestFocus();
        } catch (Exception var2) {
            JOptionPane.showMessageDialog((Component)null, "Generate Compressed Package failed to start:" + var2.getMessage(), "Error", 0);
        }
    }
}
