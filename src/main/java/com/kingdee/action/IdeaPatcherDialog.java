package com.kingdee.action;

import com.intellij.ide.util.PropertiesComponent;

import java.util.Objects;
import java.util.Properties;

public class IdeaPatcherDialog extends PatcherDialog{
    protected void setControlDefultValue() {
        super.setControlDefultValue();
        Properties properties = PropertieUtils.properties;
        try {
            PropertiesComponent instance = PropertiesComponent.getInstance();
            if (Objects.nonNull(instance.getValue(PropertieUtils.webPath))) {
                properties.setProperty(PropertieUtils.webPath, instance.getValue(PropertieUtils.webPath));
            }
            if (Objects.nonNull(instance.getValue(PropertieUtils.savePath))) {
                properties.setProperty(PropertieUtils.savePath, instance.getValue(PropertieUtils.savePath));
            }
            if (Objects.nonNull(instance.getValue(PropertieUtils.version))) {
                properties.setProperty(PropertieUtils.version, instance.getValue(PropertieUtils.version));
            }
            if (Objects.nonNull(instance.getValue(PropertieUtils.describe))) {
                properties.setProperty(PropertieUtils.describe, instance.getValue(PropertieUtils.describe));
            }
        } catch (Exception ignore) {

        }
    }

    protected void writePropertiesValue() {
        super.writePropertiesValue();
        Properties properties = PropertieUtils.properties;
        try {
            PropertiesComponent instance = PropertiesComponent.getInstance();
            instance.setValue(PropertieUtils.webPath, properties.getProperty(PropertieUtils.webPath));
            instance.setValue(PropertieUtils.savePath, properties.getProperty(PropertieUtils.savePath));
            instance.setValue(PropertieUtils.version, properties.getProperty(PropertieUtils.version));
            instance.setValue(PropertieUtils.describe, properties.getProperty(PropertieUtils.describe));
        } catch (Exception ignore) {

        }
    }
}
