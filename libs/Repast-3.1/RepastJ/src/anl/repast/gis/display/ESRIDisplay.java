/*$$
 * Copyright (c) 2004, Argonne National Laboratory (ANL)
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
 * Neither the name of ANL nor the names of its
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
package anl.repast.gis.display;


import java.io.IOException;

//import org.geotools.pt.Envelope


//import org.geotools.feature.FeatureType;

/**
 *  @author Robert Najlis
 *  Created on Jul 15, 2004
 *
 *
 */
public class ESRIDisplay  {

  public static ESRIDisplay instance = new ESRIDisplay();

  public ESRIDisplay() {
  	super();
  }

  public static ESRIDisplay getInstance() {
    return instance;
  }

 


  public void updateDisplay() {
      this.updateDisplay(".\\Refresh\\");
  }
  
  public void updateDisplay(String path) {
      
   
    try {
      //String refresh = System.getProperty("refresh.path");
      //System.out.println("refresh = " + refresh);
      Process p = Runtime.getRuntime().exec(path + "Refresh.exe");
      p.waitFor();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch(IOException e) {
      e.printStackTrace();
    }    
    //System.out.println("update display finish");
  }
  
 

  public static void main(String[] args) {
  }
}
