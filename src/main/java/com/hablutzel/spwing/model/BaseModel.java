/*
 * Copyright © 2023, Hablutzel Consulting, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hablutzel.spwing.model;

import com.hablutzel.spwing.annotations.EnablerFor;
import com.hablutzel.spwing.aware.DocumentEventPublisherAware;
import com.hablutzel.spwing.view.WindowTitleSupplier;
import com.hablutzel.spwing.events.DocumentEventPublisher;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;


@Slf4j
@RequiredArgsConstructor
public abstract class BaseModel implements DocumentEventPublisherAware,
        WindowTitleSupplier,
        Saveable,
        Serializable {

    public static long serialVersionUID = 230982340980923L;

    private transient boolean dirty;

    @Setter
    protected transient DocumentEventPublisher documentEventPublisher;

    @Getter
    private transient String title;

    @Getter
    private transient File file;

    @EnablerFor("cmdSave")
    public boolean needsSave() {
        return dirty;
    }

    public void setTitle(String title) {
        this.title = title;
        if (Objects.nonNull(documentEventPublisher)) {
            documentEventPublisher.publish(this, "evtModelTitleChanged");
        }
    }

    public void setFile(File file) {
        this.file = file;
        this.setTitle(file.getName());
        if (Objects.nonNull(documentEventPublisher)) {
            documentEventPublisher.publish(this, "evtModelFileChanged");
        }
    }

    public void postFileWrite(File file) {
        this.dirty = false;
    }

    protected void stateChanged(String changeName) {
        documentEventPublisher.publish(this, changeName);
        this.dirty = true;
    }
}
