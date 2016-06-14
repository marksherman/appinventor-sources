package com.google.appinventor.components.runtime;

import android.content.Context;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import android.webkit.WebView; 
import android.view.View;

/**
 * Component for displaying iSENSE Visualizations
 * Note: This is not a browser. It is intended to work with iSENSE only.
 */

@DesignerComponent(version = YaVersion.ISENSEVIEWER_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "Component for viewing iSENSE visualizations. It is a very " + 
    "simple browser-like component that will display a visualization " + 
    "based on the parameters provided (view mode/project ID).",
    nonVisible = false,
    iconName = "images/isense.png") 
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")

public final class iSENSEViewer extends AndroidViewComponent implements Component {

  // To build iSENSE URL
  private String baseUrl = "https://isenseproject.org/projects/";
  private String preSuffix = "/data_sets?presentation=true"; 
  private String embSuffix = "/data_sets?embed=true"; 
  private int projectId = 5; 
  private boolean presentationMode = true; 

  private WebView wv; 

  /**
   * Creates a new iSENSEViewer component.
   *
   * @param container  container the component will be placed in
   */
  public iSENSEViewer(ComponentContainer container) {
    super(container);
    container.$add(this);
    Width(LENGTH_FILL_PARENT);
    Height(LENGTH_FILL_PARENT);
    // configure WebView
    wv = new WebView(container.$context()); 
    wv.getSettings().setJavaScriptEnabled(true); 
    wv.getSettings().setBuiltInZoomControls(true); 
    wv.setFocusable(true); 
  }

  // Components don't normally override Width and Height, but we do it here so that
  // the automatic width and height will be fill parent.
  @Override
    @SimpleProperty()
    public void Width(int width) {
      if (width == LENGTH_PREFERRED) {
        width = LENGTH_FILL_PARENT;
      }
      super.Width(width);
    }

  @Override
    @SimpleProperty()
    public void Height(int height) {
      if (height == LENGTH_PREFERRED) {
        height = LENGTH_FILL_PARENT;
      }
      super.Height(height);
    }

  @Override
    public View getView() {
      return wv; 
    }

  @SimpleProperty(description = "URL of the current visualization", category = PropertyCategory.BEHAVIOR)
    public String URL() {
      if (presentationMode) { 
        return baseUrl + projectId + preSuffix; 
      } else {
        return baseUrl + projectId + embSuffix;
      }
    }

  @SimpleProperty(description = "iSENSE Project ID", category = PropertyCategory.BEHAVIOR)
    public int ProjectId() {
      return projectId; 
    }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description = "iSENSE Project ID", category = PropertyCategory.BEHAVIOR)
    public void ProjectId(int ProjectId) {
      this.projectId = ProjectId;
      wv.loadUrl(URL()); 
    }

  @SimpleProperty(description = "Presentation mode", category = PropertyCategory.BEHAVIOR)
    public boolean PresentationMode() {
      return presentationMode; 
    }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "Set presentation mode on or off", category = PropertyCategory.BEHAVIOR)
    public void PresentationMode(boolean status) {
      this.presentationMode = status; 
      wv.loadUrl(URL()); 
    }

}

