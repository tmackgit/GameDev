package com.brackeen.javagamebook.graphics3D;

import com.brackeen.javagamebook.math3D.*;

/**
    A ScanConverter used to draw sorted polygons from
    front-to-back with no overdraw. Polygons are added and clipped
    to a list of what's in the view window. Call clear() before
    drawing every frame.
*/
public class SortedScanConverter extends ScanConverter {

    protected static final int DEFAULT_SCANLIST_CAPACITY = 8;

    private SortedScanList[] viewScans;
    private SortedScanList[] polygonScans;
    private boolean sortedMode;

    /**
        Creates a new SortedScanConverter for the specified
        ViewWindow. The ViewWindow's properties can change in
        between scan conversions. By default, sorted mode is
        off, but can be turned on by calling setSortedMode().
    */
    public SortedScanConverter(ViewWindow view) {
        super(view);
        sortedMode = false;
    }


    /**
        Clears the current view scan. Call this method every frame.
    */
    public void clear() {
        if (viewScans != null) {
            for (int y=0; y<viewScans.length; y++) {
                viewScans[y].clear();
            }
        }
    }


    /**
        Sets sorted mode, so this scan converter can assume
        the polygons are drawn front-to-back, and should be
        clipped against polygons already scanned for this view.
    */
    public void setSortedMode(boolean b) {
        sortedMode = b;
    }


    /**
        Gets the nth scan for the specified row.
    */
    public Scan getScan(int y, int index) {
        return polygonScans[y].getScan(index);
    }


    /**
        Gets the number of scans for the specified row.
    */
    public int getNumScans(int y) {
        return polygonScans[y].getNumScans();
    }


    /**
        Checks if the view is filled.
    */
    public boolean isFilled() {
        if (viewScans == null) {
            return false;
        }

        int left = view.getLeftOffset();
        int right = left + view.getWidth() - 1;
        for (int y=view.getTopOffset(); y<viewScans.length; y++) {
            if (!viewScans[y].equals(left, right)) {
                return false;
            }
        }
        return true;
    }


    protected void ensureCapacity() {
        super.ensureCapacity();
        int height = view.getTopOffset() + view.getHeight();
        int oldHeight = (viewScans == null)?0:viewScans.length;
        if (height != oldHeight) {
            SortedScanList[] newViewScans =
                new SortedScanList[height];
            SortedScanList[] newPolygonScans =
                new SortedScanList[height];
            if (oldHeight != 0) {
                System.arraycopy(viewScans, 0, newViewScans, 0,
                    Math.min(height, oldHeight));
                System.arraycopy(polygonScans, 0, newPolygonScans,
                    0, Math.min(height, oldHeight));
            }
            viewScans = newViewScans;
            polygonScans = newPolygonScans;
            for (int i=oldHeight; i<height; i++) {
                viewScans[i] = new SortedScanList();
                polygonScans[i] = new SortedScanList();
            }
        }
    }


    /**
        Scan-converts a polygon, and if sortedMode is on, adds
        and clips it to a list of what's in the view window.
    */
    public boolean convert(Polygon3D polygon) {
        boolean visible = super.convert(polygon);
        if (!sortedMode || !visible) {
            return visible;
        }

        // clip the scan to what's already in the view
        visible = false;
        for (int y=getTopBoundary(); y<=getBottomBoundary(); y++) {
            Scan scan = getScan(y);
            SortedScanList diff = polygonScans[y];
            diff.clear();
            if (scan.isValid()) {
                viewScans[y].add(scan.left, scan.right, diff);
                visible |= (polygonScans[y].getNumScans() > 0);
            }
        }

        return visible;

    }


    /**
        The SortedScanList class represents a series of scans
        for a row. New scans can be added and clipped to what's
        visible in the row.
    */
    private static class SortedScanList {

        private int length;
        private Scan[] scans;

        /**
            Creates a new SortedScanList with the default
            capacity (number of scans per row).
        */
        public SortedScanList() {
            this(DEFAULT_SCANLIST_CAPACITY);
        }


        /**
            Creates a new SortedScanList with the specified
            capacity (number of scans per row).
        */
        public SortedScanList(int capacity) {
            scans = new Scan[capacity];
            for (int i=0; i<capacity; i++) {
                scans[i] = new Scan();
            }
            length = 0;
        }


        /**
            Clears this list of scans.
        */
        public void clear() {
            length = 0;
        }


        /**
            Clears the number of scans in this list.
        */
        public int getNumScans() {
            return length;
        }


        /**
            Gets the nth scan in this list.
        */
        public Scan getScan(int index) {
            return scans[index];
        }


        /**
            Checks if this scan list has only one scan and that
            scan is equal to the specified left and right values.
        */
        public boolean equals(int left, int right) {
            return (length == 1 && scans[0].equals(left,right));
        }


        /**
            Add and clip the scan to this row, putting what is
            visible (the difference) in the specified
            SortedScanList.
        */
        public void add(int left, int right, SortedScanList diff) {
            for (int i=0; i<length && left <= right; i++) {
                Scan scan = scans[i];
                int maxRight = scan.left - 1;
                if (left <= maxRight) {
                    if (right < maxRight) {
                        diff.add(left, right);
                        insert(left, right, i);
                        return;
                    }
                    else {
                        diff.add(left, maxRight);
                        scan.left = left;
                        left = scan.right + 1;
                        if (merge(i)) {
                            i--;
                        }
                    }
                }
                else if (left <= scan.right) {
                    left = scan.right + 1;
                }
            }
            if (left <= right) {
                insert(left, right, length);
                diff.add(left, right);
            }

        }

        // add() helper methods

        private void growCapacity() {
            int capacity = scans.length;
            int newCapacity = capacity*2;
            Scan[] newScans = new Scan[newCapacity];
            System.arraycopy(scans, 0, newScans, 0, capacity);
            for (int i=length; i<newCapacity; i++) {
                newScans[i] = new Scan();
            }
            scans = newScans;
        }

        private void add(int left, int right) {
            if (length == scans.length) {
                growCapacity();
            }
            scans[length].setTo(left, right);
            length++;
        }

        private void insert(int left, int right, int index) {
            if (index > 0) {
                Scan prevScan = scans[index-1];
                if (prevScan.right == left - 1) {
                    prevScan.right = right;
                    return;
                }
            }

            if (length == scans.length) {
                growCapacity();
            }
            Scan last = scans[length];
            last.setTo(left, right);
            for (int i=length; i>index; i--) {
                scans[i] = scans[i-1];
            }
            scans[index] = last;
            length++;
        }

        private void remove(int index) {
            Scan removed = scans[index];
            for (int i=index; i<length-1; i++) {
                scans[i] = scans[i+1];
            }
            scans[length-1] = removed;
            length--;
        }

        private boolean merge(int index) {
            if (index > 0) {
                Scan prevScan = scans[index-1];
                Scan thisScan = scans[index];
                if (prevScan.right == thisScan.left-1) {
                    prevScan.right = thisScan.right;
                    remove(index);
                    return true;
                }
            }
            return false;
        }

    }

}
