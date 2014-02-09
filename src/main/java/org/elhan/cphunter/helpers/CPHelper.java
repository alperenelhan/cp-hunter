package org.elhan.cphunter.helpers;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.SystemUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CPHelper {


    
    public static JSONObject getConfigTemplate() throws JSONException {
        
        JSONObject template = new JSONObject();
        template.put("filetype", "");
        template.put("directories", new JSONArray());
        
        return template;
    }
    


}
