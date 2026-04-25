package com.locus.ui;

import javafx.scene.image.Image;

import java.io.File;
import java.util.List;

/**
 * Loads brand assets from known local paths or classpath.
 */
public final class BrandAssets {

    private static final List<String> LOGO_CANDIDATE_PATHS = List.of(
            "C:/Users/user/.cursor/projects/c-Users-user-OneDrive-FAST-National-University-Desktop-uni-sem-4-sda-sda-project-Locus-Analytics/assets/c__Users_user_AppData_Roaming_Cursor_User_workspaceStorage_d873fc6245a64444a8cba788635693ba_images_logo_black-removebg-preview-5fcbf3ea-69f5-4f29-b712-b75d5ddda559.png"
    );

    private BrandAssets() {
    }

    public static Image loadLogoImage() {
        for (String path : LOGO_CANDIDATE_PATHS) {
            File file = new File(path);
            if (file.exists()) {
                return new Image(file.toURI().toString(), true);
            }
        }
        try {
            var resource = BrandAssets.class.getResource("/images/locus-logo.png");
            if (resource != null) {
                return new Image(resource.toExternalForm(), true);
            }
        } catch (Exception ignored) {
            // Fallback will return null.
        }
        return null;
    }
}
