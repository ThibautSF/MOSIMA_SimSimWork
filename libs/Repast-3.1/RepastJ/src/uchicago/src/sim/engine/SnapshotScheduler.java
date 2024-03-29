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
package uchicago.src.sim.engine;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import uchicago.src.sim.gui.MediaProducer;

public class SnapshotScheduler implements MediaScheduler {

  public MediaProducer producer;
  public BasicAction takeSnapshot;
  public String name;
  public Action buttonAction;

  class TakeSnapshot extends BasicAction {
    String fileName;
    boolean nameSet = false;

    public TakeSnapshot(String fname) {
      fileName = fname;
    }

    public void execute() {
      producer.setSnapshotFileName(fileName);
      producer.takeSnapshot();
    }
  }

  public SnapshotScheduler(String fileName, MediaProducer producer, String name) {
    this.name = name;
    this.producer = producer;
    takeSnapshot = new TakeSnapshot(fileName);
  }

  public void setProducer(MediaProducer producer) {
    this.producer = producer;
  }

  public String getName() {
    return name;
  }

  public void scheduleAtPauseAndEnd(Schedule schedule) {
    schedule.scheduleActionAtPause(takeSnapshot);
    schedule.scheduleActionAtEnd(takeSnapshot);
  }

  public void scheduleAtInterval(Schedule schedule, int interval) {
    schedule.scheduleActionAtInterval(interval, takeSnapshot, Schedule.LAST);
  }

  public void onButtonClick(String name) {
    buttonAction = new AbstractAction(name) {
      public void actionPerformed(ActionEvent evt) {
        takeSnapshot.execute();
      }
    };
  }

  public Action getButtonAction() {
    return buttonAction;
  }
}