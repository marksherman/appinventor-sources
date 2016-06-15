package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.shared.settings.SettingsConstants;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

import java.util.ArrayList;

import com.google.appinventor.client.output.OdeLog;

/**
 * Mock iSENSEViewer component.
 *
 */
public final class MockiSENSEViewer extends MockVisibleComponent {

  /**
   * Component type name.
   */
  public static final String TYPE = "iSENSEViewer";

  // Large icon image for use in designer.  Smaller version is in the palette.
  private final Image largeImage = new Image(images.isenselarge());

  /**
   * Creates a new MockiSENSEViewer component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockiSENSEViewer(SimpleEditor editor) {
    super(editor, TYPE, images.isense());

    // Initialize mock WebViewer UI
    SimplePanel webViewerWidget = new SimplePanel();
    webViewerWidget.setStylePrimaryName("ode-SimpleMockContainer");
    webViewerWidget.addStyleDependentName("centerContents");
    webViewerWidget.setWidget(largeImage);
    initComponent(webViewerWidget);
  }

  // If these are not here, then we don't see the icon as it's
  // being dragged from the pelette
  @Override
  public int getPreferredWidth() {
    return largeImage.getWidth();
  }

  @Override
  public int getPreferredHeight() {
    return largeImage.getHeight();
  }


  // override the width and height hints, so that automatic will in fact be fill-parent
  @Override
  int getWidthHint() {
    int widthHint = super.getWidthHint();
    if (widthHint == LENGTH_PREFERRED) {
      widthHint = LENGTH_FILL_PARENT;
    }
    return widthHint;
  }

  @Override int getHeightHint() {
    int heightHint = super.getHeightHint();
    if (heightHint == LENGTH_PREFERRED) {
      heightHint = LENGTH_FILL_PARENT;
    }
    return heightHint;
  }
}
