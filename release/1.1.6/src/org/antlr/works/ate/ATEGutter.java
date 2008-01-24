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

package org.antlr.works.ate;

import org.antlr.works.ate.breakpoint.ATEBreakpointEntity;
import org.antlr.works.ate.folding.ATEFoldingEntity;
import org.antlr.works.ate.syntax.misc.ATELine;
import org.antlr.works.utils.IconManager;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ATEGutter extends JComponent {

    private static final int BREAKPOINT_WIDTH = 9;
    private static final int BREAKPOINT_HEIGHT = 9;
    private static final int FOLDING_ICON_WIDTH = 9;
    private static final int FOLDING_ICON_HEIGHT = 9;

    private static final int OFFSET_FROM_TEXT = 2;

    private static final Color BACKGROUND_COLOR = new Color(240,240,240);
    private static final Stroke FOLDING_DASHED_STROKE = new BasicStroke(0.0f, BasicStroke.CAP_BUTT,
                                                    BasicStroke.JOIN_MITER, 1.0f, new float[] { 1.0f}, 0.0f);
    private static final Font LINE_NUMBER_FONT = new Font("Courier", Font.PLAIN, 12);

    private ATEPanel textEditor;

    private List<BreakpointInfo> breakpoints = new ArrayList<BreakpointInfo>();

    private List<FoldingInfo> foldingInfos = new ArrayList<FoldingInfo>();
    private boolean foldingEnabled = false;

    private FontMetrics lineNumberMetrics;
    private int offsetForLineNumber;
    private boolean lineNumberEnabled;

    private transient Image collapseDown;
    private transient Image collapseUp;
    private transient Image collapse;
    private transient Image expand;
    private transient Image delimiter;
    private transient Image delimiterUp;
    private transient Image delimiterDown;

    public ATEGutter(ATEPanel textEditor) {
        this.textEditor = textEditor;

        collapseDown = IconManager.shared().getIconCollapseDown().getImage();
        collapseUp = IconManager.shared().getIconCollapseUp().getImage();
        collapse = IconManager.shared().getIconCollapse().getImage();
        expand = IconManager.shared().getIconExpand().getImage();
        delimiter = IconManager.shared().getIconDelimiter().getImage();
        delimiterUp = IconManager.shared().getIconDelimiterUp().getImage();
        delimiterDown = IconManager.shared().getIconDelimiterDown().getImage();

        addMouseListener(new MyMouseAdapter());
        addMouseMotionListener(new MyMouseMotionAdapter());
    }

    public void setFoldingEnabled(boolean foldingEnabled) {
        // @todo currently disabled
        //this.foldingEnabled = foldingEnabled;
    }

    public void setLineNumberEnabled(boolean lineNumberEnabled) {
        this.lineNumberEnabled = lineNumberEnabled;
    }

    public void markDirty() {
        repaint();
    }

    public Set<Integer> getBreakpoints() {
        // Returns a set containing all lines which contains a breakpoint
        Set<Integer> set = new HashSet<Integer>();
        for (BreakpointInfo info : breakpoints) {
            if (info.entity.breakpointEntityIsBreakpoint())
                set.add(info.entity.breakpointEntityLine());
        }
        return set;
    }

    protected void toggleBreakpoint(BreakpointInfo info) {
        if(info == null)
            return;

        info.entity.breakpointEntitySetBreakpoint(!info.entity.breakpointEntityIsBreakpoint());
        repaint();
    }

    protected void toggleFolding(FoldingInfo info) {
        if(info == null || !info.entity.foldingEntityCanBeCollapsed())
            return;

        textEditor.foldingManager.toggleFolding(info.entity);
        markDirty();
    }

    protected int getLineYPixelPosition(int indexInText) {
        try {
            Rectangle r = textEditor.textPane.modelToView(indexInText);
            return r.y + r.height / 2;
        } catch (BadLocationException e) {
            return -1;
        }
    }

    public void updateInfo(Rectangle clip) {

        /** Make sure we are only updating objects in the current visible range
         *
         */

        int startIndex = textEditor.textPane.viewToModel(new Point(clip.x, clip.y));
        int endIndex = textEditor.textPane.viewToModel(new Point(clip.x+clip.width, clip.y+clip.height));

        breakpoints.clear();
        if(textEditor.breakpointManager != null) {
            List<? extends ATEBreakpointEntity> entities = textEditor.breakpointManager.getBreakpointEntities();
            for (ATEBreakpointEntity entity : entities) {
                int index = entity.breakpointEntityIndex();
                if (index >= startIndex && index <= endIndex) {
                    int y = getLineYPixelPosition(entity.breakpointEntityIndex());
                    Rectangle r = new Rectangle(offsetForLineNumber, y - BREAKPOINT_HEIGHT / 2, BREAKPOINT_WIDTH, BREAKPOINT_HEIGHT);
                    breakpoints.add(new BreakpointInfo(entity, r));
                }
            }
        }

        foldingInfos.clear();
        if(textEditor.foldingManager != null) {
            List<ATEFoldingEntity> entities = textEditor.foldingManager.getFoldingEntities();
            for (ATEFoldingEntity entity : entities) {
                int entityStartIndex = entity.foldingEntityGetStartIndex();
                int entityEndIndex = entity.foldingEntityGetEndIndex();
                if (!(entityStartIndex > endIndex || entityEndIndex < startIndex)) {
                    int top_y = getLineYPixelPosition(entity.foldingEntityGetStartIndex());
                    int bottom_y = getLineYPixelPosition(entity.foldingEntityGetEndIndex());

                    Point top = new Point(getWidth() - getOffsetFromText(), top_y);
                    Point bottom = new Point(getWidth() - getOffsetFromText(), bottom_y);
                    foldingInfos.add(new FoldingInfo(entity, top, bottom));
                }
            }
        }

    }

    public void updateSize() {
        if(lineNumberMetrics == null) {
            lineNumberMetrics = textEditor.textPane.getFontMetrics(LINE_NUMBER_FONT);
        }

        offsetForLineNumber = 0;
        if(lineNumberEnabled) {
            List<ATELine> lines = textEditor.getLines();
            if(lines != null) {
                offsetForLineNumber = lineNumberMetrics.stringWidth(String.valueOf(lines.size()));
            }
        }
    }

    public BreakpointInfo getBreakpointInfoAtPoint(Point p) {
        for (BreakpointInfo info : breakpoints) {
            if (info.contains(p))
                return info;
        }
        return null;
    }

    public FoldingInfo getFoldingInfoAtPoint(Point p) {
        for (FoldingInfo info : foldingInfos) {
            if (info.contains(p))
                return info;
        }
        return null;
    }

    public int getOffsetFromText() {
        return OFFSET_FROM_TEXT+FOLDING_ICON_WIDTH/2;
    }

    public void paintComponent(Graphics g) {
        Rectangle r = g.getClipBounds();

        updateInfo(r);
        updateSize();

        paintGutter(g, r);
        paintFolding((Graphics2D)g, r);
        paintBreakpoints((Graphics2D)g, r);

        if(lineNumberEnabled) {
            paintLineNumbers((Graphics2D)g, r);
        }
    }

    private void paintGutter(Graphics g, Rectangle clip) {
        g.setColor(textEditor.textPane.getBackground());
        g.fillRect(clip.x+clip.width-getOffsetFromText(), clip.y, getOffsetFromText(), clip.height);

        g.setColor(BACKGROUND_COLOR);
        g.fillRect(clip.x, clip.y, clip.width-getOffsetFromText(), clip.height);

        g.setColor(Color.lightGray);
        g.drawLine(clip.x+clip.width-getOffsetFromText(), clip.y, clip.x+clip.width-getOffsetFromText(), clip.y+clip.height);
    }

    private void paintBreakpoints(Graphics2D g, Rectangle clip) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.red);
        for (BreakpointInfo info : breakpoints) {
            if (info.entity.breakpointEntityIsBreakpoint()) {
                Rectangle r = info.r;
                if (clip.intersects(r)) {
                    g.fillArc(r.x, r.y, r.width, r.height, 0, 360);
                }
            }
        }
    }

    private void paintLineNumbers(Graphics2D g, Rectangle clip) {
        g.setColor(Color.black);
        g.setFont(LINE_NUMBER_FONT);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);

        int lineCount = textEditor.getLines().size();
        int lineHeight = textEditor.textPane.getFontMetrics(textEditor.textPane.getFont()).getHeight();
        int number = Math.max(0, (Math.round(clip.y / lineHeight) - 1));
        int y = number*lineHeight;
        while(number <= lineCount && y-lineHeight <= clip.getY()+clip.getHeight()) {
            String s = String.valueOf(number++);
            g.drawString(s, offsetForLineNumber-lineNumberMetrics.stringWidth(s), y - 4);
            y += lineHeight;
        }
    }

    private void paintFolding(Graphics2D g, Rectangle clip) {
        // Do not alias otherwise the dotted line between collapsed icon doesn't show up really well
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);

        for (FoldingInfo info : foldingInfos) {
            if (!clip.intersects(info.top_r) && !clip.intersects(info.bottom_r)) continue;

            Point top = info.top;
            Point bottom = info.bottom;
            if (foldingEnabled && info.entity.foldingEntityCanBeCollapsed()) {
                if (info.entity.foldingEntityIsExpanded()) {
                    drawFoldingLine(g, top, bottom);

                    if (top.equals(bottom)) {
                        drawCenteredImageAtPoint(g, collapse, top);
                    } else {
                        drawCenteredImageAtPoint(g, collapseUp, top);
                        drawCenteredImageAtPoint(g, collapseDown, bottom);
                    }
                } else {
                    drawCenteredImageAtPoint(g, expand, top);
                }
            } else {
                drawFoldingLine(g, top, bottom);

                if (top.equals(bottom)) {
                    drawCenteredImageAtPoint(g, delimiter, top);
                } else {
                    drawCenteredImageAtPoint(g, delimiterUp, top);
                    drawCenteredImageAtPoint(g, delimiterDown, bottom);
                }
            }
        }
    }

    private void drawFoldingLine(Graphics2D g, Point top, Point bottom) {
        g.setColor(Color.white);
        g.drawLine(top.x, top.y, bottom.x, bottom.y);

        Stroke s = g.getStroke();
        g.setStroke(FOLDING_DASHED_STROKE);
        g.setColor(Color.black);
        g.drawLine(top.x, top.y, bottom.x, bottom.y);
        g.setStroke(s);
    }

    public Dimension getPreferredSize() {
        Dimension d = textEditor.textPane.getSize();
        d.width = 25+offsetForLineNumber;
        return d;
    }

    public static void drawCenteredImageAtPoint(Graphics g, Image image, Point p) {
        g.drawImage(image, p.x-image.getWidth(null)/2, p.y-image.getHeight(null)/2, null);
    }

    protected static class BreakpointInfo {
        public ATEBreakpointEntity entity;
        public Rectangle r;

        public BreakpointInfo(ATEBreakpointEntity entity, Rectangle r) {
            this.entity = entity;
            this.r = r;
        }

        public boolean contains(Point p) {
            return r.contains(p);
        }
    }

    protected static class FoldingInfo {
        public ATEFoldingEntity entity;

        public Point top;
        public Point bottom;

        public Rectangle top_r;
        public Rectangle bottom_r;

        public FoldingInfo(ATEFoldingEntity entity, Point top, Point bottom) {
            this.entity = entity;
            this.top = top;
            this.bottom = bottom;
            this.top_r = new Rectangle(top.x-FOLDING_ICON_WIDTH/2, top.y-FOLDING_ICON_HEIGHT/2, FOLDING_ICON_WIDTH, FOLDING_ICON_HEIGHT);
            this.bottom_r = new Rectangle(bottom.x-FOLDING_ICON_WIDTH/2, bottom.y-FOLDING_ICON_HEIGHT/2, FOLDING_ICON_WIDTH, FOLDING_ICON_HEIGHT);
        }

        public boolean contains(Point p) {
            if(entity.foldingEntityIsExpanded())
                return top_r.contains(p) || bottom_r.contains(p);
            else
                return top_r.contains(p);
        }
    }

    protected class MyMouseAdapter extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            toggleBreakpoint(getBreakpointInfoAtPoint(e.getPoint()));
            toggleFolding(getFoldingInfoAtPoint(e.getPoint()));
        }

        public void mouseExited(MouseEvent e) {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    protected class MyMouseMotionAdapter extends MouseMotionAdapter {
        public void mouseMoved(MouseEvent e) {
            if(!foldingEnabled)
                return;

            FoldingInfo info = getFoldingInfoAtPoint(e.getPoint());
            if(info != null && info.entity.foldingEntityCanBeCollapsed())
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            else
                setCursor(Cursor.getDefaultCursor());
        }
    }
}
