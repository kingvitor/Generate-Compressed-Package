package com.kingdee.action;

import com.intellij.ide.util.PropertiesComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PatcherDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    protected JTextField txtSavePath;
    private JButton btnSavePath;
    private JPanel filePanel;
    protected JTextField txtWebPath;
    private JButton btnWebPath;
    protected JTextField txtVersion;
    protected JTextField txtDescribe;
    private JList<File> fieldList;

    PatcherDialog() {
        setTitle("Generate Compressed Package Dialog");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setControlDefultValue();
        if (!txtWebPath.getText().isEmpty()) {
            showFileList(txtWebPath.getText());
        }
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // 资源文件路径
        btnWebPath.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userDir = System.getProperty("user.home");
                JFileChooser fileChooser = new JFileChooser(userDir + File.separator + "Desktop");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int flag = fileChooser.showOpenDialog(null);
                if (flag == JFileChooser.APPROVE_OPTION) {
                    String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                    txtWebPath.setText(filePath);
                    showFileList(filePath);
                }
            }
        });

        // 保存路径按钮事件
        btnSavePath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userDir = System.getProperty("user.home");
                JFileChooser fileChooser = new JFileChooser(userDir + File.separator + "Desktop");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int flag = fileChooser.showOpenDialog(null);
                if (flag == JFileChooser.APPROVE_OPTION) {
                    String path = fileChooser.getSelectedFile().getAbsolutePath();
                    txtSavePath.setText(path);
                }
            }
        });
    }

    /**
     * @param path
     */
    private void showFileList(String path) {
        Vector<File> filePaths = new Vector<>();
        List<File> files = new ArrayList<>();
        files.add(new File(path));
        while (!files.isEmpty()) {
            File file = files.remove(0);
            if (file.isDirectory()) {
                files.addAll(0, Arrays.stream(Objects.requireNonNull(file.getAbsoluteFile().listFiles())).collect(Collectors.toList()));
            } else {
                filePaths.add(new File(file.getAbsoluteFile().getAbsolutePath()));
            }
        }
        if (filePanel != null) {
            Component[] components = filePanel.getComponents();
            for (Component component : components) {
                filePanel.remove(component);
            }
        }
        fieldList = new JList<File>(filePaths);
        filePanel.add(fieldList);
        filePanel.updateUI();
        contentPane.updateUI();
    }

    private void onOK() {
        // 輸入校驗
        if (!checkInputValue()) {
            return;
        }

        // 緩存輸入值
        writePropertiesValue();

        ListModel<File> model = fieldList.getModel();
        try {

            Map<String, Map<String, KdpkgsProperty>> md5Map = new LinkedHashMap<>();
            AtomicInteger index = new AtomicInteger();
            String cloud = "";
            for (int i = 0; i < model.getSize(); i++) {
                File file = model.getElementAt(i);
                String fileName = file.getName();
                String[] fileNameSplit = fileName.substring(0, fileName.lastIndexOf(".")).split("-");
                if (fileNameSplit.length > 1) {
                    cloud = fileNameSplit[0];
                    String app = fileNameSplit[1];
                    md5Map.computeIfAbsent(app, x -> new LinkedHashMap<>());
                    Map<String, KdpkgsProperty> nameTupe2Map = md5Map.get(app);
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] fileBytes = Files.readAllBytes(Paths.get(file.getPath()));
                    byte[] bytes = md.digest(fileBytes);
                    StringBuffer byteToString = new StringBuffer();
                    for (byte b : bytes) {
                        byteToString.append(String.format("%02x", b));
                    }
                    String kdpkgID = new SimpleDateFormat("MMddHHssmm").format(new Date()) + index.getAndAdd(1);
                    String sourcePath = file.getAbsolutePath().contains("dm") ? "dm" : "jar/biz";
                    String md5 = byteToString.toString();
                    nameTupe2Map.computeIfAbsent(fileName, x -> new KdpkgsProperty(kdpkgID, sourcePath, md5));
                }
            }

            // 生成Kdpkge文件
            String xmlContent = createKdpkgsXml(cloud, md5Map);
            String xmlPath = txtWebPath.getText() + File.separator + "kdpkgs.xml";
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(xmlPath))) {
                bufferedWriter.append(xmlContent);
            }

            // 壓縮文件夾
            String zipFileName = txtSavePath.getText() + File.separator + cloud + "_" + String.join("_", md5Map.keySet()) + "_std_" + new SimpleDateFormat("yyyyMMddHH").format(new Date()) + ".zip";
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFileName))) {
                compressFolder(txtWebPath.getText(), "", zipOutputStream);
            }
        } catch (Exception e) {
            showErrorDialog("Create Patcher Error!");
            e.printStackTrace();
        }

        dispose();
    }


    private Boolean checkInputValue() {
        // 保存路徑校驗
        if (null == txtSavePath.getText() || "".equals(txtSavePath.getText())) {
            showErrorDialog("Please Select Save Path!");
            return false;
        }

        // 壓縮文件校驗
        if (fieldList.getModel().getSize() == 0) {
            showErrorDialog("Please Select Export File!");
            return false;
        }

        // 版本校驗
        if (null == txtVersion.getText() || "".equals(txtVersion.getText())) {
            showErrorDialog("Please Enter Verison!");
            return false;
        }

        // 版本校驗
        if (null == txtDescribe.getText() || "".equals(txtDescribe.getText())) {
            showErrorDialog("Please Enter Describe!");
            return false;
        }

        // 壓縮包路徑不能跟文件路徑相同
        if (txtWebPath.getText().equals(txtSavePath.getText())) {
            showErrorDialog("The compressed package path cannot be the same as the file path!");
            return false;
        }
        return true;
    }

    private void showErrorDialog(String message) {
        //Messages.showErrorDialog(this, message, "Error");
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void compressFolder(String sourceFolder, String folderName, ZipOutputStream zipOutputStream) throws IOException {
        File folder = new File(sourceFolder);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 压缩子文件夹
                    folderName = folderName.isEmpty() ? folderName : folderName + File.separator;
                    compressFolder(file.getAbsolutePath(), folderName + file.getName(), zipOutputStream);
                } else {
                    // 压缩文件
                    folderName = folderName.isEmpty() || folderName.endsWith(File.separator) ? folderName : folderName + File.separator;
                    addToZipFile(folderName + file.getName(), file.getAbsolutePath(), zipOutputStream);
                }
            }
        }
    }

    private void addToZipFile(String fileName, String fileAbsolutePath, ZipOutputStream zipOutputStream) throws IOException {
        // 创建ZipEntry对象并设置文件名
        ZipEntry entry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(entry);

        // 读取文件内容并写入Zip文件
        try (FileInputStream fileInputStream = new FileInputStream(fileAbsolutePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, bytesRead);
            }
        }

        zipOutputStream.closeEntry();
    }

    private void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        filePanel = new JPanel();
        filePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    }

    private String createKdpkgsXml(String cloud, Map<String, Map<String, KdpkgsProperty>> xmlProValue) {
        String appNode = "";
        String kdpkgNode = "";
        for (Map.Entry<String, Map<String, KdpkgsProperty>> stringMapEntry : xmlProValue.entrySet()) {
            String resourceIds = "";
            for (Map.Entry<String, KdpkgsProperty> stringTuple2Entry : stringMapEntry.getValue().entrySet()) {
                String outputPath = stringTuple2Entry.getValue().getSourcePath().equals("dm") ? "    <outputPath/>\n" : "    <outputPath>biz</outputPath>\n";
                String type = stringTuple2Entry.getValue().getSourcePath().equals("dm") ? "dm" : "jar";
                kdpkgNode += "  <kdpkg>\n" +
                        "    <ID>" + stringTuple2Entry.getValue().getKdpkgID() + "</ID>\n" +
                        "    <sourcePath>" + stringTuple2Entry.getValue().getSourcePath() + "</sourcePath>\n" +
                        outputPath +
                        "    <name>" + stringTuple2Entry.getKey() + "</name>\n" +
                        "    <md5>" + stringTuple2Entry.getValue().getMd5() + "</md5>\n" +
                        "    <type>" + type + "</type>\n" +
                        "  </kdpkg>\n";
                resourceIds += stringTuple2Entry.getValue().getKdpkgID() + ",";
            }
            appNode += "    <app>\n" +
                    "      <name>" + cloud + "-" + stringMapEntry.getKey() + "</name>\n" +
                    "      <ver>" + txtVersion.getText() + "</ver>\n" +
                    "      <appids>" + stringMapEntry.getKey() + "</appids>\n" +
                    "      <force>true</force>\n" +
                    "      <resource>" + resourceIds.substring(0, resourceIds.lastIndexOf(",")) + "</resource>\n" +
                    "    </app>\n";
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\n" +
                "<kdpkgs isv=\"kingdee\" ver=\"" + txtVersion.getText() + "\">\n" +
                "  <format ver=\"1.0\"/>\n" +
                "  <description>\n" +
                "    <time>" + new SimpleDateFormat("yyyy-MM-dd HH:ss:mm").format(new Date()) + "</time>\n" +
                "    <content/>\n" +
                "  </description>\n" +
                "  <product name=\"cosmic_fi_" + String.join("_", xmlProValue.keySet()) + "\" nameCN=\"" + txtDescribe.getText() + "\" ver=\"" + txtVersion.getText() + "\">\n" +
                "    <force>true</force>\n" + appNode +
                "  </product>\n" + kdpkgNode +
                "</kdpkgs>\n";
    }

    protected void setControlDefultValue() {
        Properties properties = PropertieUtils.loadPropertiesValue();
        txtWebPath.setText(properties.getProperty(PropertieUtils.webPath));
        txtSavePath.setText(properties.getProperty(PropertieUtils.savePath));
        txtVersion.setText(properties.getProperty(PropertieUtils.version));
        txtDescribe.setText(properties.getProperty(PropertieUtils.describe));
    }

    protected void writePropertiesValue() {
        Properties properties = PropertieUtils.properties;
        properties.setProperty(PropertieUtils.webPath, txtWebPath.getText());
        properties.setProperty(PropertieUtils.savePath, txtSavePath.getText());
        properties.setProperty(PropertieUtils.version, txtVersion.getText());
        properties.setProperty(PropertieUtils.describe, txtDescribe.getText());
        PropertieUtils.writePropertiesValue(properties);
    }
}
