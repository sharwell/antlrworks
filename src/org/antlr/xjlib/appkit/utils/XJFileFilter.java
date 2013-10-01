/*

[The "BSD licence"]
Copyright (c) 2005 Jean Bovet
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

package org.antlr.xjlib.appkit.utils;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class XJFileFilter extends FileFilter {

    private List<String> exts = null;
    private String description = null;

    public static XJFileFilter createFileFilter(String ext, String description) {
        return createFileFilter(Collections.singletonList(ext), description);
    }

    public static XJFileFilter createFileFilter(List<String> ext, String description) {
        XJFileFilter ff = new XJFileFilter();
        ff.exts = ext;
        ff.description = description;
        return ff;
    }

    public boolean accept(File f) {
        return f.isDirectory() || accept(f.getName());
    }

    public boolean accept(String path) {
        for (String e : exts) {
            if (path.endsWith("."+e)) {
                return true;
            }
        }
        return false;
    }

    public String getDefaultExtension() {
        return exts.get(0);
    }

    public String getDescription() {
        return description;
    }

}
