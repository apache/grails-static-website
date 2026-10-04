/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package website.gradle

import spock.lang.Specification

class RenderGuidesPluginSnippetSpec extends Specification {

    private static final String CONTROLLER = '''\
        //tag::save-full[]
        //tag::save[]
        def save(Player player) {
            //end::save[]
            //tag::save-handleErrors[]
            if (player.hasErrors()) {
                return
            }
            //end::save-handleErrors[]
            player.save flush: true
            //tag::save[]
        }
        //end::save[]
        //end::save-full[]

        //tag::playerInfo[]
        class PlayerInfo {
            String name
        //end::playerInfo[]
            //tag::playerInfo-constraints[]
            static constraints = {
                name blank: false
            }
            //end::playerInfo-constraints[]
        //tag::playerInfo[]
        }
        //end::playerInfo[]
        '''.stripIndent()

    def 'semicolon-separated tags concatenate in document order'() {
        when:
        String save = RenderGuidesPlugin.extractTaggedRegions(CONTROLLER, 'save;save-handleErrors')
        String playerInfo = RenderGuidesPlugin.extractTaggedRegions(CONTROLLER, 'playerInfo;playerInfo-constraints')

        then:
        save.contains('def save(Player player) {')
        save.contains('if (player.hasErrors()) {')
        !save.contains('player.save flush: true')
        playerInfo.contains('class PlayerInfo {')
        playerInfo.contains('static constraints = {')
        playerInfo.contains('name blank: false')
    }

    def 'comma-separated tags still concatenate'() {
        expect:
        RenderGuidesPlugin.extractTaggedRegions(CONTROLLER, 'save,save-handleErrors')
                .contains('if (player.hasErrors()) {')
    }
}
