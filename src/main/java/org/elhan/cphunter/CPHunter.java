/**
 *
 */
package org.elhan.cphunter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.SystemUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author alperen
 *
 */
public class CPHunter implements Observer {

    private JSONObject _conf;
    private FileList _foundFiles;

    public CPHunter() {
        _foundFiles = new FileList();
        _foundFiles.addObserver(this);
    }

    public CPHunter(String[] classes, String[] directories, boolean withRunning) throws JSONException, IOException, InterruptedException {
        _foundFiles = new FileList();
        _foundFiles.addObserver(this);
        setConfig(classes, directories, withRunning);
    }

    public JSONObject getConfig() throws JSONException {
        if (null == _conf) {
            return getConfigTemplate();
        } else {
            return _conf;
        }
    }

    public JSONObject setConfig(String[] classes, String[] directories, boolean withRunning) throws JSONException, IOException, InterruptedException {
        if (null == classes || classes.length == 0) {
            return getConfig();
        }

        _conf = new JSONObject();
        _conf.put("classes", new JSONArray(classes));
        _conf.put("withRunning", withRunning);
        JSONArray dirs = null;

        if (withRunning) {
            dirs = new JSONArray();
            if (SystemUtils.IS_OS_LINUX) {
                String [] cmd = {"/bin/bash", "-c", "lsof -u" + SystemUtils.USER_NAME + "| awk '{ if ($9 ~ /\\.jar$/) print $9 }'"};
                Process process = Runtime.getRuntime().exec(cmd);
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                process.waitFor();
                while ((line = input.readLine()) != null) {
                    dirs.put(line);
                }
                input.close();
            } else if (SystemUtils.IS_OS_WINDOWS) {
                throw new NotImplementedException("Not implemented for windows yet");
            }
        } else {
            if (null == directories || directories.length == 0) {
                dirs = new JSONArray();
                Iterable<Path> iterable = FileSystems.getDefault().getRootDirectories();
                Iterator<Path> iterator = iterable.iterator();
                while (iterator.hasNext()) {
                    dirs.put(iterator.next().toAbsolutePath().toString());
                }
            } else {
                dirs = new JSONArray(directories);
            }
        }
        _conf.put("directories", dirs);

        return getConfig();
    }

    private JSONObject getConfigTemplate() throws JSONException {

        JSONObject template = new JSONObject();
        template.put("classes", new JSONArray());
        template.put("withRunning", true);
        template.put("directories", new JSONArray());

        return template;
    }

    /**
     *
     * @param options format is { "classes": [ "org.elhan.CPHunter" ],
     * "directories": [ "directory1", "directory2", "directory3" ] }
     */
    public void hunt() throws JSONException, IOException {
        if (null == _conf) {
            return;
        }

        JSONArray dirs = _conf.getJSONArray("directories");
        boolean withRunning = _conf.getBoolean("withRunning");
        for (int i = 0; i < dirs.length(); i++) {
            File currentDir = new File(dirs.getString(i));
            if (withRunning && currentDir.isFile() && Files.isReadable(currentDir.toPath())) {
                String ext = FilenameUtils.getExtension(currentDir.getCanonicalPath());
                if ("jar".equals(ext)) {
                    _foundFiles.add(currentDir.getCanonicalPath());
                }
            } else {
                walk(currentDir);
            }

        }
    }

    private void walk(File dir) throws IOException, JSONException {

        if (null == dir || null == dir.listFiles() || Files.isSymbolicLink(dir.toPath())) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                walk(file);
            } else {
                String ext = FilenameUtils.getExtension(file.getCanonicalPath());
                if ("jar".equals(ext)) {
                    _foundFiles.add(file.getCanonicalPath());
                }
            }
        }
    }

    public void update(Observable o, Object arg) {
        if (arg instanceof String) {
            try {
                String path = (String) arg;
                JarInputStream jarFile = new JarInputStream(new FileInputStream(new File(path)));
                JarEntry jarEntry;
                while (true) {
                    jarEntry = jarFile.getNextJarEntry();
                    if (jarEntry == null) {
                        break;
                    }
                    if (jarEntry.getName().endsWith(".class")) {
                        String className = jarEntry.getName().split(".class")[0].replaceAll("/", "\\.");

                        if (containsClass(className)) {
                            System.out.println(String.format("Class: %s found in the jar: %s", className, path));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean containsClass(String needle) throws JSONException {
        JSONArray haystack = _conf.getJSONArray("classes");
        for (int i = 0; i < haystack.length(); i++) {
            if (needle.equals(haystack.get(i))) {
                return true;
            }
        }

        return false;
    }

}
