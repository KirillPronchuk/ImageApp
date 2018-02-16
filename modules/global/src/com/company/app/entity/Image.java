/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.company.app.entity;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;

/**
 * Created by Kirill on 22.11.2017.
 */
@Table(name = "APP_IMAGE")
@Entity(name = "app$Image")
public class Image extends StandardEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IMAGE_FILE_ID")
    protected FileDescriptor imageFile;

    public void setImageFile(FileDescriptor imageFile) {
        this.imageFile = imageFile;
    }

    public FileDescriptor getImageFile() {
        return imageFile;
    }
}