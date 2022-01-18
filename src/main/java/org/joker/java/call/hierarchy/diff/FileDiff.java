package org.joker.java.call.hierarchy.diff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileDiff {
    public String filename;

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
