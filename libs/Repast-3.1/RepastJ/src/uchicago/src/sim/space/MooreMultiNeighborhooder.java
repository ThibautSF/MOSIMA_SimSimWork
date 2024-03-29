/*$$
 * Copyright (c) 1999, Trustees of the University of Chicago
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with 
 * or without modification, are permitted provided that the following 
 * conditions are met:
 *
 *	 Redistributions of source code must retain the above copyright notice,
 *	 this list of conditions and the following disclaimer.
 *
 *	 Redistributions in binary form must reproduce the above copyright notice,
 *	 this list of conditions and the following disclaimer in the documentation
 *	 and/or other materials provided with the distribution.
 *
 * Neither the name of the University of Chicago nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE TRUSTEES OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *$$*/
package uchicago.src.sim.space;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class MooreMultiNeighborhooder extends AbstractNeighborhooder {

    public NeighMaker maker;

    public MooreMultiNeighborhooder(IMulti2DGrid space) {
        super(space);
        if (space instanceof Torus) maker = new NeighMakerTorus(space);
        else maker = new NeighMakerGrid(space);
    }

    public Vector getNeighbors(int x, int y, int[] extents,
        boolean returnNulls) {
        return new Vector(getNeighborsList(x, y, extents, returnNulls));
    }

    public ArrayList getNeighborsList(int x, int y, int[] extents,
        boolean returnNulls) {
        return maker.getNeighbors(x, y, extents[0], extents[1], returnNulls);
    }

    public ArrayList getNeighborsLoc(int x, int y, int[] extents,
        boolean returnNulls) {
        return maker.getNeighborsLoc(x, y, extents[0], extents[1], returnNulls);

    }

    abstract class NeighMaker {

        public IMulti2DGrid grid;
        public int xSize, ySize;

        public NeighMaker(IMulti2DGrid grid) {
            this.grid = grid;
            xSize = grid.getSizeX();
            ySize = grid.getSizeY();
        }

        public abstract ArrayList getNeighbors(int x, int y, int xExtent,
            int yExent, boolean returnNulls);

        public abstract ArrayList getNeighborsLoc(int x, int y, int xExtent,
            int yExent, boolean returnNulls);

        /**
         * Normalize the x value to the toroidal coordinates
         *
         * @param x the value to normalize
         * @return the normalized value
         */
        public int xnorm(int x) {
            if (x > xSize - 1 || x < 0) {
                while (x < 0) x += xSize;
                return x % xSize;
            }
 
            return x;
        }
 
        /**
         * Normalize the y value to the toroidal coordinates
         *
         * @param y the value to normalize
         * @return the normalized value
         */
        public int ynorm(int y) {
            if (y > ySize - 1 || y < 0) {
                while (y < 0) y += ySize;
                return y % ySize;
            }
 
            return y;
        }         
    }


    class NeighMakerTorus extends NeighMaker {
        public NeighMakerTorus(IMulti2DGrid grid) {
            super(grid);
        }

        public ArrayList getNeighbors(int x, int y, int xExtent, int yExtent,
            boolean returnNulls) {
            ArrayList v = new ArrayList(xExtent * yExtent * 4 +
                    (xExtent * 2) + (yExtent * 2));
    
            for (int j = y - yExtent; j <= y + yExtent; j++) {
                for (int i = x - xExtent; i <= x + xExtent; i++) {
                    if (!(j == y && i == x)) {
                        List l = grid.getObjectsAt(i, j);
                        int lsize = l.size();

                        if (lsize == 0 && returnNulls)
                            v.add(null);
                        else if (lsize > 0) {
                            v.addAll(l);
                        }
                    }
                }
            }

            return v;
      
        }

        public ArrayList getNeighborsLoc(int x, int y, int xExtent, int yExtent,
            boolean returnNulls) {
      
            ArrayList v = new ArrayList(xExtent * yExtent * 4 +
                    (xExtent * 2) + (yExtent * 2));
    
            for (int j = y - yExtent; j <= y + yExtent; j++) {
                for (int i = x - xExtent; i <= x + xExtent; i++) {	
                    if (!(j == y && i == x)) {
                        List l = grid.getObjectsAt(i, j);
                        int lsize = l.size();

                        if (lsize == 0 && returnNulls)
                            v.add(new ObjectLocation(null, xnorm(i), ynorm(j)));
                        else if (lsize > 0) {
                            v.addAll(ObjectLocation.makeObjectLocations(l, xnorm(i),
                                    ynorm(j)));
	    
                        }
                    }
                }
            }

            return v;
      
        }

    }


    class NeighMakerGrid extends NeighMaker {

        public NeighMakerGrid(IMulti2DGrid grid) {
            super(grid);
        }
    
        public ArrayList getNeighbors(int x, int y, int xExtent, int yExtent,
            boolean returnNulls) {
            ArrayList v = new ArrayList(xExtent * yExtent * 4 +
                    (xExtent * 2) + (yExtent * 2));

            int xLeft = xExtent;
            int xRight = xExtent;

            if (x - xLeft < 0)
                xLeft = x;

            if (xRight + x > xSize - 1)
                xRight = xSize - 1 - x;

            int yTop = yExtent;
            int yBottom = yExtent;

            if (y + yBottom > ySize - 1)
                yBottom = ySize - 1 - y;

            if (y - yTop < 0)
                yTop = y;

            for (int j = y - yTop; j <= y + yBottom; j++) {
                for (int i = x - xLeft; i <= x + xRight; i++) {
                    if (!(j == y && i == x)) {
                        List l = grid.getObjectsAt(i, j);
                        int lsize = l.size();

                        if (lsize == 0 && returnNulls)
                            v.add(null);
                        else if (lsize > 0) {
                            v.addAll(l);
                        }
                    }
                }
            }

            return v;
     
        }

        public ArrayList getNeighborsLoc(int x, int y, int xExtent, int yExtent,
            boolean returnNulls) {
            ArrayList v = new ArrayList(xExtent * yExtent * 4 +
                    (xExtent * 2) + (yExtent * 2));

            int xLeft = xExtent;
            int xRight = xExtent;

            if (x - xLeft < 0)
                xLeft = x;

            if (xRight + x > xSize - 1)
                xRight = xSize - 1 - x;

            int yTop = yExtent;
            int yBottom = yExtent;

            if (y + yBottom > ySize - 1)
                yBottom = ySize - 1 - y;

            if (y - yTop < 0)
                yTop = y;

            for (int j = y - yTop; j <= y + yBottom; j++) {
                for (int i = x - xLeft; i <= x + xRight; i++) {
                    if (!(j == y && i == x)) {
                        List l = grid.getObjectsAt(i, j);
                        int lsize = l.size();

                        if (lsize == 0 && returnNulls)
                            v.add(new ObjectLocation(null, i, j));
                        else if (lsize > 0) {
                            v.addAll(ObjectLocation.makeObjectLocations(l, i, j));
	    
                        }
                    }
                }
            }

            return v;
      
        }
    }
}
