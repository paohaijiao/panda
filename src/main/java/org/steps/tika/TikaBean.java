package org.steps.tika;

/**
 * @program: kettle-sdk-step-plugin
 * @description: ${description}
 * @author: Gou Ding Cheng
 * @create: 2019-09-10 11:04
 **/
public class TikaBean {
    private String fieldName;
    private String filepath;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }
}