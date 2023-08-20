module spwing {
    requires java.datatransfer;
    requires java.desktop;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.jthemedetector;
    requires lombok;
    requires org.apache.commons.lang3;
    requires org.apache.commons.text;
    requires org.apache.commons.io;
    requires org.slf4j;
    requires org.slf4j.simple;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.expression;
    requires org.antlr.antlr4.runtime;

    exports com.hablutzel.spwing;
    exports com.hablutzel.spwing.annotations;
    exports com.hablutzel.spwing.aware;
    exports com.hablutzel.spwing.command;
    exports com.hablutzel.spwing.component;
    exports com.hablutzel.spwing.context;
    exports com.hablutzel.spwing.events;
    exports com.hablutzel.spwing.invoke;
    exports com.hablutzel.spwing.laf;
    exports com.hablutzel.spwing.menu;
    exports com.hablutzel.spwing.model;
    exports com.hablutzel.spwing.util;
    exports com.hablutzel.spwing.view;
    exports com.hablutzel.spwing.view.adapter;
    exports com.hablutzel.spwing.view.bind;
    exports com.hablutzel.spwing.view.factory;
    exports com.hablutzel.spwing.view.factory.reflective;
    exports com.hablutzel.spwing.view.factory.svwf;

}