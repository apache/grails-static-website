package org.grails.plugin

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable
import org.jetbrains.annotations.NotNull

import java.time.LocalDateTime

@EqualsAndHashCode
@CompileStatic
@Sortable(includes=['updated'])
class Plugin implements Comparable{
    String name
    Owner owner
    String desc
    List<String> labels
    String latestVersion
    String vcsUrl
    LocalDateTime updated
    String license
    Integer githubStars

    static String getUpdated(Plugin plugin){
        plugin.updated
    }


    @Override
    int compareTo(@NotNull Object o) {
        return 0
    }

}
