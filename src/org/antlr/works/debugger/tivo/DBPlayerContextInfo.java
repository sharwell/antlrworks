package org.antlr.works.debugger.tivo;

import java.util.ArrayList;
import java.util.List;

/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

public class DBPlayerContextInfo {

    public List<Integer> subrule = new ArrayList<Integer>();
    public List<Integer> decision = new ArrayList<Integer>();
    public List<Integer> mark = new ArrayList<Integer>();
    public List<Integer> backtrack = new ArrayList<Integer>();

    public void enterSubrule(int i) {
        subrule.add(i);
    }

    public void exitSubrule() {
        subrule.remove(subrule.size() - 1);
    }

    public int getSubrule() {
        return getPeekValue(subrule);
    }

    public void enterDecision(int i) {
        decision.add(i);
    }

    public void exitDecision() {
        decision.remove(decision.size() - 1);
    }

    public int getDecision() {
        return getPeekValue(decision);
    }

    public void mark(int i) {
        mark.add(i);
    }

    public void rewind() {
        mark.remove(mark.size() - 1);
    }

    public int getMark() {
        return getPeekValue(mark);
    }

    public void beginBacktrack(int level) {
        backtrack.add(level);
    }

    public void endBacktrack() {
        backtrack.remove(backtrack.size() - 1);
    }

    public int getBacktrack() {
        return getPeekValue(backtrack);
    }

    public boolean isBacktracking() {
        return !backtrack.isEmpty();
    }

    public int getPeekValue(List<Integer> s) {
        if(s.isEmpty())
            return -1;
        else
            return s.get(s.size() - 1);
    }

    public void clear() {
        subrule.clear();
        decision.clear();
        mark.clear();
        backtrack.clear();
    }
}
