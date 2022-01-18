package org.joker.java.call.hierarchy.diff;

import java.util.List;

public interface DiffAdapter {

    /**
     * 每一行表示svn diff or git diff输出的一行
     *
     * @param diff
     * @return
     */
    List<FileDiff> toDiff(List<String> diff);
}
