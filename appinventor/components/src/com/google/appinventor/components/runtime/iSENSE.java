package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.annotations.SimpleObject; 

@DesignerComponent(version = YaVersion.ISENSE_COMPONENT_VERSION,
    description = "A deprecated component",
    category = ComponentCategory.SOCIAL,
    nonVisible = true,
    iconName = "images/isense.png")
@SimpleObject

public final class iSENSE extends AndroidNonvisibleComponent implements Component {

  public iSENSE(ComponentContainer container) {
    super(container.$form());
  } 
}
