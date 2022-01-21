package org.joker.java.call.hierarchy.diff;

import java.util.ArrayList;
import java.util.List;

public class FileDiff {
    public String filename;

    private String module = null;

    public String getModule() {
        if (module != null) {
            return  module;
        }

        int index = filename.indexOf("/eagle-parent/");
        String temp = filename.substring(index + "/eagle-parent/".length());
        int end = temp.indexOf("/");
        module = temp.substring(0, end);

        if (module.startsWith("eagle-")) {
            module = module.substring(6);
        } else if (module.startsWith("zxjt-")) {
            module = module.substring(5);
        }

        return module;
    }

    public List<LineDiff> diffSet = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(filename).append(":\n");

        for (LineDiff line : diffSet) {
            sb.append(line.lineNum).append("\t").append(line.line).append("\n");
        }

        return sb.toString();
    }
}
