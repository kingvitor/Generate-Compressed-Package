package com.kingdee.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;
import java.awt.*;

public class CreateBackageAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Client.start(new PatcherDialog());
    }
}
