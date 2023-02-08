package org.joker.java.call.hierarchy.diff;

import com.google.common.collect.Lists;

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
        if (index < 0) {
            module = "";
            return module;
        }
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

    public FileDiff typeDiff(LineDiff.DiffType type) {
        FileDiff fileDiff = new FileDiff();

        fileDiff.filename = this.filename;
        fileDiff.module = this.module;
        fileDiff.diffSet = Lists.newArrayList(this.diffSet);

        return fileDiff;
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
