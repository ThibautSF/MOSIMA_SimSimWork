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
package uchicago.src.repastdemos.life;

import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

/**
 * The agent in this implementation of Life. Its sole behavoir is to
 * check if the number of its neighbors is > 3 or < 2 and die if
 * so. Dying here is the removal of the agent from the Space. Note that
 * this removal doesn't actually occur until every agent has performed
 * this check. See LifeModel for more.
 */

public class LifeAgent implements Drawable {
  
  public int x, y;
  public Space space;

  /**
   * Creates this LifeAgent with the specified coordinates and space.
   */
  public LifeAgent(int x, int y, Space space) {
    this.space = space;
    this.x = x;
    this.y = y;
  }

  /**
   * Check if the number of neighbors is > 3 or < 2 and die if so. Die =
   * remove from the space.
   */
  public void step() { 
    int numNeighbors = space.getNumNeighbors(x, y);
    if (numNeighbors > 3 || numNeighbors < 2) {
      space.remove(this);
    }
  }

  // Drawable interface
  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
  
  public void draw(SimGraphics g) {
    g.drawFastRoundRect(Color.red);
  }
}
