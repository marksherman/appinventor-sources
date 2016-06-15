package com.google.appinventor.components.runtime;

import android.content.Context;
import android.webkit.JavascriptInterface;
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

import com.google.appinventor.components.runtime.util.FroyoUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Component for displaying iSENSE Visualizations
 *
 */

@DesignerComponent(version = YaVersion.ISENSEVIEWER_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "Component for viewing iSENSE visualizations. " + 
    "The user provides a project ID and whether the vis should appear " + 
    "in presentation mode (or embedded mode). This is intended to work " + 
    "only with isenseproject.org.")

@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public final class iSENSEViewer extends AndroidViewComponent {

  private final WebView webview;

  // To build iSENSE URL
  private String baseUrl = "https://isenseproject.org/projects/";
  private String preSuffix = "/data_sets?presentation=true"; 
  private String embSuffix = "/data_sets?embed=true"; 
  private int projectID = 5; 
  private boolean presentationMode = true; 

  /**
   * Creates a new iSENSEViewer component.
   *
   * @param container  container the component will be placed in
   */
  public iSENSEViewer(ComponentContainer container) {
    super(container);

    webview = new WebView(container.$context());
    resetWebViewClient();
    webview.getSettings().setJavaScriptEnabled(true);
    webview.setFocusable(true);
    webview.getSettings().setBuiltInZoomControls(true);

    container.$add(this);

    webview.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
          case MotionEvent.ACTION_UP:
            if (!v.hasFocus()) {
              v.requestFocus();
            }
            break;
        }
        return false;
      }
    });

    // set the initial default properties.  Height and Width
    // will be fill-parent, which will be the default for the web viewer.

    Width(LENGTH_FILL_PARENT);
    Height(LENGTH_FILL_PARENT);
  }

  @Override
    public View getView() {
      return webview;
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

  @SimpleProperty(description = "iSENSE Project ID", category = PropertyCategory.BEHAVIOR)
    public int ProjectID() {
      return projectID; 
    }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description = "iSENSE Project ID", category = PropertyCategory.BEHAVIOR)
    public void ProjectID(int ProjectID) {
      this.projectID = ProjectID;
      webview.loadUrl(URL()); 
    }

  @SimpleProperty(description = "Presentation mode", category = PropertyCategory.BEHAVIOR)
    public boolean PresentationMode() {
      return presentationMode; 
    }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "Set presentation mode on or off", category = PropertyCategory.BEHAVIOR)
    public void PresentationMode(boolean status) {
      this.presentationMode = status; 
      webview.loadUrl(URL()); 
    }

  /**
   * Returns the URL of the page the iSENSEViewer should load
   *
   * @return URL of the page the iSENSEViewer should load
   */
  @SimpleProperty(description="Current URL", category = PropertyCategory.BEHAVIOR)
    public String URL() {
      if (presentationMode) { 
        return baseUrl + projectID + preSuffix; 
      } else {
        return baseUrl + projectID + embSuffix;
      }
    }

  /**
   * Returns the title of the page currently being viewed
   *
   * @return title of the page being viewed
   */
  @SimpleProperty(
      description = "Title of the page currently viewed",
      category = PropertyCategory.BEHAVIOR)
    public String CurrentPageTitle() {
      return (webview.getTitle() == null) ? "" : webview.getTitle();
    }

  private void resetWebViewClient() {
    boolean ignoreSslErrors = false;
    boolean followLinks = false;
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_FROYO) {
      webview.setWebViewClient(FroyoUtil.getWebViewClient(ignoreSslErrors, followLinks, container.$form(), this));
    } else {
      webview.setWebViewClient(new WebViewClient());
    }
  }
}

