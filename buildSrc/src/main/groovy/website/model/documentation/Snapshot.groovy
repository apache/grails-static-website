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
package website.model.documentation

//import groovy.transform.CompileStatic

//@CompileStatic Does not compile statically with Groovy 4
class Snapshot implements Comparable<Snapshot> {

    final String text

    Snapshot(String text) {
        this.text = text
    }

    boolean isBuildSnapshot() { text.endsWith('SNAPSHOT') }
    boolean isReleaseCandidate() { text.startsWith('RC') }
    boolean isMilestone() { text.startsWith('M') }

    int getMilestoneVersion() { (text - 'M') as int }
    int getReleaseCandidateVersion() { (text - 'RC') as int }

    private int getRank() {
        if (buildSnapshot) return 3
        if (releaseCandidate) return 2
        if (milestone) return 1
        0
    }

    private int getNumber() {
        if (releaseCandidate) return releaseCandidateVersion
        if (milestone) return milestoneVersion
        0
    }

    @Override
    int compareTo(Snapshot other) {
        (rank <=> other.rank) ?: (number <=> other.number)
    }
}
