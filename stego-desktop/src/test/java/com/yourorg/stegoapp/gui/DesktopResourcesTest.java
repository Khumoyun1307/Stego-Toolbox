package com.yourorg.stegoapp.gui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class DesktopResourcesTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "views/selection.fxml",
            "views/stego.fxml",
            "views/pipeline.fxml"
    })
    void fxmlResourcesExistAndAreWellFormedXml(String path) throws Exception {
        try (InputStream in = resource(path)) {
            assertNotNull(in, "Missing resource: " + path);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.newDocumentBuilder().parse(in);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "styles/dark-theme.css",
            "styles/app.css"
    })
    void cssResourcesExist(String path) throws Exception {
        try (InputStream in = resource(path)) {
            assertNotNull(in, "Missing resource: " + path);
        }
    }

    private static InputStream resource(String path) {
        return DesktopResourcesTest.class.getClassLoader().getResourceAsStream(path);
    }
}

